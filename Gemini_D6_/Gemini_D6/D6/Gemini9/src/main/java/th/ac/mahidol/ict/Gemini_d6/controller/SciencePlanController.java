package th.ac.mahidol.ict.Gemini_d6.controller;

import edu.gemini.app.ocs.OCS;
import edu.gemini.app.ocs.model.DataProcRequirement; // OCS model
import edu.gemini.app.ocs.model.StarSystem; // OCS model
import edu.gemini.app.ocs.model.Quadrant;  // Needed for Quadrant enum

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize; // Import for method security
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority; // Needed for checking roles
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import th.ac.mahidol.ict.Gemini_d6.model.*; // Our models
import th.ac.mahidol.ict.Gemini_d6.repository.SciencePlanRepository;
import th.ac.mahidol.ict.Gemini_d6.repository.UserRepository;
import th.ac.mahidol.ict.Gemini_d6.model.SciencePlanStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap; // For Map
import java.util.List;
import java.util.Map; // For Map
import java.util.Optional;

@Controller
@RequestMapping("/science-plans")
public class SciencePlanController {

    @Autowired private OCS ocs;
    @Autowired private SciencePlanRepository sciencePlanRepository;
    @Autowired private UserRepository userRepository;

    // == Use Case 1: Create Science Plan ==
    @GetMapping("/create")
    @PreAuthorize("hasRole('ASTRONOMER')") // Ensure only astronomers can access create page
    public String showCreatePlanForm(Model model) {
        SciencePlan plan = new SciencePlan();
        if (plan.getDataProcessingRequirements() == null) { plan.setDataProcessingRequirements(new DataProcessingRequirements()); }
        model.addAttribute("sciencePlan", plan);
        populateDropdownLists(model);
        model.addAttribute("starSystemValidationData", getStarSystemValidationData());
        return "create_science_plan";
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ASTRONOMER')") // Ensure only astronomers can save
    public String saveSciencePlan(@Valid @ModelAttribute("sciencePlan") SciencePlan sciencePlan, BindingResult bindingResult, Authentication authentication, RedirectAttributes redirectAttributes, Model model) {
        if (sciencePlan.getStartDate() != null && sciencePlan.getEndDate() != null && sciencePlan.getStartDate().isAfter(sciencePlan.getEndDate())) { bindingResult.rejectValue("startDate", "date.invalidRange", "Start date must be before end date"); }
        if (bindingResult.hasErrors()) { populateDropdownLists(model); model.addAttribute("starSystemValidationData", getStarSystemValidationData()); return "create_science_plan"; }
        String username = authentication.getName();
        User creatorUser = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        sciencePlan.setCreator(creatorUser); sciencePlan.setStatus(SciencePlanStatus.CREATED);
        try {
            edu.gemini.app.ocs.model.SciencePlan ocsPlan = mapToOcsPlan(sciencePlan, username);
            String ocsCreateResult = ocs.createSciencePlan(ocsPlan); System.out.println("OCS create result: " + ocsCreateResult);
            try { sciencePlanRepository.save(sciencePlan); redirectAttributes.addFlashAttribute("OCS Message: " + ocsCreateResult); }
            catch (Exception dbEx) { System.err.println("Error saving plan locally: " + dbEx.getMessage()); redirectAttributes.addFlashAttribute("warningMessage", "Sent to OCS, but failed to save locally."); }
            return "redirect:/view-plans";
        } catch (Exception ocsEx) {
            System.err.println("OCS Error during create: " + ocsEx.getMessage()); model.addAttribute("ocsError", "OCS Error: " + ocsEx.getMessage());
            populateDropdownLists(model); model.addAttribute("starSystemValidationData", getStarSystemValidationData()); return "create_science_plan";
        }
    }

    // == Use Case 2: Submit Science Plan ==
    @PostMapping("/submit/{planId}")
    @PreAuthorize("hasRole('ASTRONOMER')") // Ensure only astronomers can submit
    public String submitPlan(@PathVariable Long planId, RedirectAttributes redirectAttributes, Authentication authentication) {
        Optional<SciencePlan> optionalPlan = sciencePlanRepository.findById(planId);
        if (optionalPlan.isEmpty()) { redirectAttributes.addFlashAttribute("submitError", "Science Plan not found: " + planId); return "redirect:/view-plans"; }
        SciencePlan plan = optionalPlan.get(); String planName = plan.getPlanName() != null ? plan.getPlanName() : "ID " + planId;
        if (plan.getStatus() != SciencePlanStatus.TESTED) { redirectAttributes.addFlashAttribute("submitError", "Plan '" + planName + "' must be TESTED to submit."); return "redirect:/view-plans"; }
        try {
            String username = authentication.getName(); edu.gemini.app.ocs.model.SciencePlan ocsPlanToSubmit = mapToOcsPlan(plan, username);
            System.out.println("Attempting to submit Plan ID: " + planId + " to OCS...");
            String ocsSubmitResult = ocs.submitSciencePlan(ocsPlanToSubmit); System.out.println("OCS Submission Result: " + ocsSubmitResult);
            plan.setStatus(SciencePlanStatus.SUBMITTED); sciencePlanRepository.save(plan);
            redirectAttributes.addFlashAttribute("submitSuccess", "Plan '" + planName + "'OCS Message: " + ocsSubmitResult);
        } catch (Exception e) {
            System.err.println("Error submitting plan ID " + planId + ": " + e.getMessage()); redirectAttributes.addFlashAttribute("submitError", "Error submitting plan: " + e.getMessage());
        }
        return "redirect:/view-plans";
    }

    // == Use Case 3: Test Science Plan ==
    @PostMapping("/test/{planId}")
    @PreAuthorize("hasRole('ASTRONOMER')") // Ensure only astronomers can test
    public String testPlan(@PathVariable Long planId, RedirectAttributes redirectAttributes, Authentication authentication) {
        Optional<SciencePlan> optionalPlan = sciencePlanRepository.findById(planId);
        if (optionalPlan.isEmpty()) { redirectAttributes.addFlashAttribute("errorMessage", "Not found: " + planId); return "redirect:/view-plans"; }
        SciencePlan plan = optionalPlan.get(); String planName = plan.getPlanName() != null ? plan.getPlanName() : "ID " + planId;
        if (plan.getStatus() != SciencePlanStatus.CREATED) { redirectAttributes.addFlashAttribute("testResultMessage", "Plan '" + planName + "' must be CREATED to test."); return "redirect:/view-plans"; }
        String rawOcsTestResult = ""; List<String> ocsErrorMessages = new ArrayList<>(); boolean ocsTestPassed = false;
        try {
            String username = authentication.getName(); edu.gemini.app.ocs.model.SciencePlan ocsPlanToTest = mapToOcsPlan(plan, username);
            System.out.println("--- Sending Plan to OCS Test ---"); System.out.println("Local Plan ID: " + plan.getPlanId()); System.out.println("Start Date Str: " + ocsPlanToTest.getStartDate()); System.out.println("End Date Str: " + ocsPlanToTest.getEndDate()); System.out.println("OCS Status Sent: " + (ocsPlanToTest.getStatus() != null ? ocsPlanToTest.getStatus().name() : "null")); System.out.println("---------------------------------"); // Logging
            rawOcsTestResult = ocs.testSciencePlan(ocsPlanToTest); System.out.println("OCS Raw Test Result:\n" + rawOcsTestResult);
            if (rawOcsTestResult != null && !rawOcsTestResult.isEmpty()) { boolean foundError = false; String[] lines = rawOcsTestResult.split("\\r?\\n"); for (String line : lines) { if (line.trim().startsWith("ERROR:") || line.contains(": ERROR:")) { ocsErrorMessages.add(line.trim()); foundError = true; } } ocsTestPassed = !foundError; }
            else { ocsErrorMessages.add("OCS returned no result."); ocsTestPassed = false; }
            if (ocsTestPassed) {
                plan.setStatus(SciencePlanStatus.TESTED); sciencePlanRepository.save(plan);
                redirectAttributes.addFlashAttribute("testResultMessage", "Plan '" + planName + "' tested successfully (Local status updated to TESTED).\n--- OCS Test Details ---\n" + rawOcsTestResult); System.out.println("Plan " + planId + " updated to TESTED.");
            } else {
                String formattedErrors = ocsErrorMessages.isEmpty() ? "Unknown failure." : String.join("\n- ", ocsErrorMessages);
                redirectAttributes.addFlashAttribute("testResultMessage", "Plan '" + planName + "' testing failed.\n--- OCS Error Details ---\n- " + formattedErrors + "\n\n--- Full OCS Response ---\n" + rawOcsTestResult); System.out.println("Plan " + planId + " testing failed.");
            }
        } catch (Exception e) {
            System.err.println("Error testing plan ID " + planId + ": " + e.getMessage()); String detailedError = "App Exception: " + e.getMessage(); /*...*/ redirectAttributes.addFlashAttribute("testResultMessage", "Error testing plan: " + detailedError);
        }
        return "redirect:/view-plans";
    }

    /**
     *
     *
     *
     * @param plan
     * @param authentication
     * @return true
     */
    private boolean canModifyPlan(SciencePlan plan, Authentication authentication) {
        if (plan == null || authentication == null) {
            return false;
        }

        List<SciencePlanStatus> restrictedStatusesForAstronomer = List.of(
                SciencePlanStatus.SUBMITTED,
                SciencePlanStatus.VALIDATED,
                SciencePlanStatus.INVALIDATED,
                SciencePlanStatus.RUNNING,
                SciencePlanStatus.COMPLETE,
                SciencePlanStatus.CANCELLED
        );

        // Check if you are an Admin.
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        // Admin can always edit/delete
        if (isAdmin) {
            return true;
        }

        //  Check if it is an Astronomer.
        boolean isAstronomer = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ASTRONOMER"::equals);

        if (isAstronomer) {
            return !restrictedStatusesForAstronomer.contains(plan.getStatus());

        }

        // Other roles cannot be edited/deleted.
        return false;
    }


    // --- EDIT Functionality ---
    @GetMapping("/edit/{planId}")

    @PreAuthorize("hasAnyRole('ADMIN', 'ASTRONOMER')")
    public String showEditPlanForm(@PathVariable Long planId, Model model, RedirectAttributes redirectAttributes, Authentication authentication) { // เพิ่ม Authentication
        Optional<SciencePlan> optionalPlan = sciencePlanRepository.findById(planId);
        if (optionalPlan.isPresent()) {
            SciencePlan plan = optionalPlan.get();


            if (!canModifyPlan(plan, authentication)) {
                String planName = plan.getPlanName() != null ? plan.getPlanName() : "ID " + planId;
                // Send a notification message back to the view page.
                redirectAttributes.addFlashAttribute("errorMessage", "Plan '" + planName + "' ไม่สามารถแก้ไขได้ เนื่องจากมีสถานะเป็น " + plan.getStatus() + ".");
                return "redirect:/view-plans"; // Return to the list page.
            }

            //  If you have permission, show the edit form as usual.
            model.addAttribute("sciencePlan", plan);
            populateDropdownLists(model);
            model.addAttribute("starSystemValidationData", getStarSystemValidationData());
            return "edit_science_plan";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "ไม่พบ Science Plan ID: " + planId);
            return "redirect:/view-plans";
        }
    }

    @PostMapping("/update/{planId}")

    @PreAuthorize("hasAnyRole('ADMIN', 'ASTRONOMER')")
    public String updateSciencePlan(@PathVariable Long planId, @Valid @ModelAttribute("sciencePlan") SciencePlan formPlanData, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model, Authentication authentication) { // เพิ่ม Authentication
        Optional<SciencePlan> optionalExistingPlan = sciencePlanRepository.findById(planId);
        if (optionalExistingPlan.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "ไม่พบ Science Plan สำหรับการอัปเดต ID: " + planId);
            return "redirect:/view-plans";
        }
        SciencePlan existingPlan = optionalExistingPlan.get();


        if (!canModifyPlan(existingPlan, authentication)) {
            String planName = existingPlan.getPlanName() != null ? existingPlan.getPlanName() : "ID " + planId;
            // Send a notification message back to the view page.
            redirectAttributes.addFlashAttribute("errorMessage", "Plan '" + planName + "' ไม่สามารถอัปเดตได้ เนื่องจากมีสถานะเป็น " + existingPlan.getStatus() + ".");
            return "redirect:/view-plans";
        }

            // Check the validity of data (e.g. date)
        if (formPlanData.getStartDate() != null && formPlanData.getEndDate() != null && formPlanData.getStartDate().isAfter(formPlanData.getEndDate())) {
            bindingResult.rejectValue("startDate", "date.invalidRange", "Start date must be before end date");
        }
        // If there is a validation error, return to the edit form with an error message displayed.
        if (bindingResult.hasErrors()) {
            populateDropdownLists(model);
            model.addAttribute("starSystemValidationData", getStarSystemValidationData());
            formPlanData.setPlanId(existingPlan.getPlanId());
            formPlanData.setStatus(existingPlan.getStatus());
            formPlanData.setCreator(existingPlan.getCreator());
            model.addAttribute("sciencePlan", formPlanData); // ใช้ข้อมูลจากฟอร์มเพื่อให้แสดง error ที่ field นั้นๆ
            return "edit_science_plan";
        }

        // Update field data
        existingPlan.setPlanName(formPlanData.getPlanName());
        existingPlan.setFunding(formPlanData.getFunding());
        existingPlan.setObjective(formPlanData.getObjective());
        existingPlan.setStartDate(formPlanData.getStartDate());
        existingPlan.setEndDate(formPlanData.getEndDate());
        existingPlan.setTelescopeLocation(formPlanData.getTelescopeLocation());
        existingPlan.setTargetStarSystem(formPlanData.getTargetStarSystem());

        // update DataProcessingRequirements
        if (existingPlan.getDataProcessingRequirements() == null) {
            existingPlan.setDataProcessingRequirements(new DataProcessingRequirements());
        }
        if (formPlanData.getDataProcessingRequirements() != null) {
            DataProcessingRequirements targetDpr = existingPlan.getDataProcessingRequirements();
            DataProcessingRequirements sourceDpr = formPlanData.getDataProcessingRequirements();
            targetDpr.setFileType(sourceDpr.getFileType());
            targetDpr.setFileQuality(sourceDpr.getFileQuality());
            targetDpr.setColorType(sourceDpr.getColorType());
            targetDpr.setContrast(sourceDpr.getContrast());
            targetDpr.setBrightness(sourceDpr.getBrightness());
            targetDpr.setSaturation(sourceDpr.getSaturation());
            targetDpr.setHighlights(sourceDpr.getHighlights());
            targetDpr.setExposure(sourceDpr.getExposure());
            targetDpr.setShadows(sourceDpr.getShadows());
            targetDpr.setWhites(sourceDpr.getWhites());
            targetDpr.setBlacks(sourceDpr.getBlacks());
            targetDpr.setLuminance(sourceDpr.getLuminance());
            targetDpr.setHue(sourceDpr.getHue());
        }

        //Save changes to the database.
        try {
            sciencePlanRepository.save(existingPlan);
            redirectAttributes.addFlashAttribute("successMessage", "Plan '" + existingPlan.getPlanName() + "' updated (Local only).");
        } catch (Exception dbEx) {
            System.err.println("Error updating plan: " + dbEx.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating plan.");
        }
        // Return to the list page.
        return "redirect:/view-plans";
    }

    // --- DELETE Functionality ---
    @PostMapping("/delete/{planId}")

    @PreAuthorize("hasAnyRole('ADMIN', 'ASTRONOMER')")
    public String deletePlan(@PathVariable Long planId, RedirectAttributes redirectAttributes, Authentication authentication) { // เพิ่ม Authentication
        Optional<SciencePlan> optionalPlan = sciencePlanRepository.findById(planId);
        if (optionalPlan.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "ไม่พบ Science Plan สำหรับการลบ ID: " + planId);
            return "redirect:/view-plans";
        }
        SciencePlan planToDelete = optionalPlan.get();


        if (!canModifyPlan(planToDelete, authentication)) {
            String planName = planToDelete.getPlanName() != null ? planToDelete.getPlanName() : "ID " + planId;
            redirectAttributes.addFlashAttribute("errorMessage", "Plan '" + planName + "' ไม่สามารถลบได้ เนื่องจากมีสถานะเป็น " + planToDelete.getStatus() + ".");
            return "redirect:/view-plans"; // Return to the list page.
        }

        // If you have permission, proceed with deletion.
        String planName = planToDelete.getPlanName() != null ? planToDelete.getPlanName() : "ID " + planId;
        System.out.println("Skipping OCS deletion (planNo not stored)."); // หมายเหตุ: ยังไม่มีการลบใน OCS

        try {
            sciencePlanRepository.deleteById(planId);
            redirectAttributes.addFlashAttribute("successMessage", "Plan '" + planName + "' deleted (Local only).");
            System.out.println("Deleted local plan ID: " + planId);
        } catch (Exception e) {
            System.err.println("Error deleting plan ID " + planId + ": " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting plan.");
        }
        // Return to the list page.
        return "redirect:/view-plans";
    }

    // --- Helper methods ---
    private edu.gemini.app.ocs.model.SciencePlan mapToOcsPlan(th.ac.mahidol.ict.Gemini_d6.model.SciencePlan ourPlan, String creatorUsername) {
        User creatorUser = userRepository.findByUsername(creatorUsername).orElseThrow(() -> new UsernameNotFoundException("User not found: " + creatorUsername));
        edu.gemini.app.ocs.model.SciencePlan ocsPlan = new edu.gemini.app.ocs.model.SciencePlan();
        ocsPlan.setCreator(creatorUser.getUsername()); ocsPlan.setSubmitter(creatorUser.getUsername()); ocsPlan.setFundingInUSD(ourPlan.getFunding().doubleValue()); ocsPlan.setObjectives(ourPlan.getObjective());
        if (ourPlan.getTargetStarSystem() != null) { try { ocsPlan.setStarSystem(StarSystem.CONSTELLATIONS.valueOf(ourPlan.getTargetStarSystem())); } catch (Exception e) {System.err.println("Warn: StarSystem mapping failed");} }
        DateTimeFormatter ocsSetterFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"); // Corrected format
        if (ourPlan.getStartDate() != null) { try { ocsPlan.setStartDate(ourPlan.getStartDate().format(ocsSetterFormat)); } catch (Exception e) {System.err.println("Warn: StartDate format failed"); ocsPlan.setStartDate("-1");} } else { ocsPlan.setStartDate("-1"); }
        if (ourPlan.getEndDate() != null) { try { ocsPlan.setEndDate(ourPlan.getEndDate().format(ocsSetterFormat)); } catch (Exception e) {System.err.println("Warn: EndDate format failed"); ocsPlan.setEndDate("-1");} } else { ocsPlan.setEndDate("-1"); }
        if (ourPlan.getTelescopeLocation() != null) { try { ocsPlan.setTelescopeLocation(edu.gemini.app.ocs.model.SciencePlan.TELESCOPELOC.valueOf(ourPlan.getTelescopeLocation().name())); } catch (Exception e) {System.err.println("Warn: Telescope mapping failed");} }
        th.ac.mahidol.ict.Gemini_d6.model.DataProcessingRequirements ourDpr = ourPlan.getDataProcessingRequirements();
        if (ourDpr != null) { DataProcRequirement ocsDpr = new DataProcRequirement( ourDpr.getFileType() != null ? ourDpr.getFileType().name() : "", ourDpr.getFileQuality() != null ? mapFileQualityToOcsString(ourDpr.getFileQuality()) : "", ourDpr.getColorType() != null ? mapColorTypeToOcsString(ourDpr.getColorType()) : "", ourDpr.getContrast() != null ? ourDpr.getContrast().doubleValue() : 0.0, ourDpr.getBrightness() != null ? ourDpr.getBrightness().doubleValue() : 0.0, ourDpr.getSaturation() != null ? ourDpr.getSaturation().doubleValue() : 0.0, ourDpr.getHighlights() != null ? ourDpr.getHighlights().doubleValue() : 0.0, ourDpr.getExposure() != null ? ourDpr.getExposure().doubleValue() : 0.0, ourDpr.getShadows() != null ? ourDpr.getShadows().doubleValue() : 0.0, ourDpr.getWhites() != null ? ourDpr.getWhites().doubleValue() : 0.0, ourDpr.getBlacks() != null ? ourDpr.getBlacks().doubleValue() : 0.0, ourDpr.getLuminance() != null ? ourDpr.getLuminance().doubleValue() : 0.0, ourDpr.getHue() != null ? ourDpr.getHue().doubleValue() : 0.0 ); ocsPlan.setDataProcRequirements(ocsDpr); } else { System.err.println("Warn: DPR is null."); }
        th.ac.mahidol.ict.Gemini_d6.model.SciencePlanStatus localStatus = ourPlan.getStatus();
        edu.gemini.app.ocs.model.SciencePlan.STATUS ocsStatusToSet = edu.gemini.app.ocs.model.SciencePlan.STATUS.SAVED;
        if (localStatus != null) { if (localStatus == SciencePlanStatus.CREATED) { ocsStatusToSet = edu.gemini.app.ocs.model.SciencePlan.STATUS.SAVED; } else { try { ocsStatusToSet = edu.gemini.app.ocs.model.SciencePlan.STATUS.valueOf(localStatus.name()); } catch (IllegalArgumentException e) { ocsStatusToSet = edu.gemini.app.ocs.model.SciencePlan.STATUS.SAVED; } } } else { ocsStatusToSet = edu.gemini.app.ocs.model.SciencePlan.STATUS.SAVED; }
        ocsPlan.setStatus(ocsStatusToSet);
        return ocsPlan;
    }

    private List<Map<String, String>> getStarSystemValidationData() {
        List<Map<String, String>> validationData = new ArrayList<>(); try { for (StarSystem.CONSTELLATIONS c : StarSystem.CONSTELLATIONS.values()) { Map<String, String> d = new HashMap<>(); d.put("name", c.name()); d.put("month", String.valueOf(c.getmonth())); Quadrant.QUADRANT q = c.getQuadrant(); String rL = "N/A"; String qN = "N/A"; if (q != null) { qN = q.name(); if (qN.toUpperCase().startsWith("N")) rL = TelescopeLocation.HAWAII.name(); else if (qN.toUpperCase().startsWith("S")) rL = TelescopeLocation.CHILE.name(); } d.put("location", rL); d.put("quadrant", qN); validationData.add(d); } } catch (Exception e) { System.err.println("ERROR getting star system data: " + e.getMessage());} return validationData;
    }

    private void populateDropdownLists(Model model) {
        model.addAttribute("telescopeLocations", TelescopeLocation.values()); model.addAttribute("fileTypes", FileType.values()); model.addAttribute("fileQualities", FileQuality.values()); model.addAttribute("colorTypes", ColorType.values()); try { model.addAttribute("starSystemEnums", StarSystem.CONSTELLATIONS.values()); } catch (Exception e) { model.addAttribute("starSystemEnums", new ArrayList<>()); }
    }

    private String mapColorTypeToOcsString(ColorType ct) { if (ct == null) return ""; return ct == ColorType.COLOR ? "Color mode" : (ct == ColorType.BW ? "B&W mode" : ""); }
    private String mapFileQualityToOcsString(FileQuality fq) { if (fq == null) return ""; return fq == FileQuality.LOW ? "Low" : (fq == FileQuality.FINE ? "Fine" : ""); }

} // End of Class
