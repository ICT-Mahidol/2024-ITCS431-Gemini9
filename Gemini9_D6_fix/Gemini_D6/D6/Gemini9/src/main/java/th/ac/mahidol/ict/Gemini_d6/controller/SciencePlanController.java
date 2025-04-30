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

    @Autowired private OCS ocs; // Your OCS service instance
    @Autowired private SciencePlanRepository sciencePlanRepository;
    @Autowired private UserRepository userRepository;

    // == Use Case 1: Create Science Plan ==
    @GetMapping("/create")
    @PreAuthorize("hasRole('ASTRONOMER')")
    public String showCreatePlanForm(Model model) {
        SciencePlan plan = new SciencePlan();
        if (plan.getDataProcessingRequirements() == null) {
            plan.setDataProcessingRequirements(new DataProcessingRequirements());
        }
        model.addAttribute("sciencePlan", plan);
        populateDropdownLists(model);
        model.addAttribute("starSystemValidationData", getStarSystemValidationData());
        return "create_science_plan";
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ASTRONOMER')")
    public String saveSciencePlan(@Valid @ModelAttribute("sciencePlan") SciencePlan sciencePlan, BindingResult bindingResult, Authentication authentication, RedirectAttributes redirectAttributes, Model model) {
        // Validate date range
        if (sciencePlan.getStartDate() != null && sciencePlan.getEndDate() != null && sciencePlan.getStartDate().isAfter(sciencePlan.getEndDate())) {
            bindingResult.rejectValue("startDate", "date.invalidRange", "Start date must be before end date");
        }

        if (bindingResult.hasErrors()) {
            populateDropdownLists(model);
            model.addAttribute("starSystemValidationData", getStarSystemValidationData());
            return "create_science_plan";
        }

        String username = authentication.getName();
        User creatorUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        sciencePlan.setCreator(creatorUser);
        sciencePlan.setStatus(SciencePlanStatus.CREATED); // Set initial status for local plan

        try {
            // Map local plan to OCS plan object (planNo will be 0 or null initially)
            edu.gemini.app.ocs.model.SciencePlan ocsPlan = mapToOcsPlan(sciencePlan, username);

            // Call OCS to create the plan. OCS will set the planNo on the ocsPlan object.
            String ocsCreateResult = ocs.createSciencePlan(ocsPlan);
            System.out.println("OCS create result: " + ocsCreateResult);

            // *** FIXED: Get the planNo assigned by OCS ***
            int generatedOcsPlanNo = ocsPlan.getPlanNo(); // Assuming getPlanNo() returns the int ID

            if (generatedOcsPlanNo > 0) {
                // *** FIXED: Store the OCS planNo in our local entity ***
                sciencePlan.setOcsPlanNo(generatedOcsPlanNo); // Use the setter (needs to be added to your SciencePlan model)
                System.out.println("Stored OCS Plan No: " + generatedOcsPlanNo + " locally.");

                try {
                    // Save the local plan *with* the ocsPlanNo
                    sciencePlanRepository.save(sciencePlan);
                    redirectAttributes.addFlashAttribute("successMessage", "Plan created and saved locally (OCS Plan No: " + generatedOcsPlanNo + "). OCS Message: " + ocsCreateResult);
                } catch (Exception dbEx) {
                    System.err.println("Error saving plan locally: " + dbEx.getMessage());
                    // Handle case where OCS plan exists but local save failed
                    redirectAttributes.addFlashAttribute("warningMessage", "Plan sent to OCS (No: " + generatedOcsPlanNo + "), but failed to save locally. OCS Message: " + ocsCreateResult);
                }
            } else {
                // Handle cases where OCS might not return a valid planNo
                System.err.println("OCS did not return a valid planNo. Result: " + ocsCreateResult);
                // Populate model attributes for the form again and show error
                model.addAttribute("ocsError", "OCS Error: Failed to get a valid Plan Number from OCS. Result: " + ocsCreateResult);
                populateDropdownLists(model);
                model.addAttribute("starSystemValidationData", getStarSystemValidationData());
                return "create_science_plan"; // Return to form with error
            }

            return "redirect:/view-plans"; // Redirect after successful save

        } catch (Exception ocsEx) {
            // Catch exceptions during OCS communication
            System.err.println("OCS Error during create: " + ocsEx.getMessage());
            model.addAttribute("ocsError", "OCS Error: " + ocsEx.getMessage());
            populateDropdownLists(model);
            model.addAttribute("starSystemValidationData", getStarSystemValidationData());
            return "create_science_plan"; // Return to form with OCS error
        }
    }

    // == Use Case 2: Submit Science Plan ==
    @PostMapping("/submit/{planId}")
    @PreAuthorize("hasRole('ASTRONOMER')")
    public String submitPlan(@PathVariable Long planId, RedirectAttributes redirectAttributes, Authentication authentication) {
        Optional<SciencePlan> optionalPlan = sciencePlanRepository.findById(planId);
        if (optionalPlan.isEmpty()) {
            redirectAttributes.addFlashAttribute("submitError", "Science Plan not found: " + planId);
            return "redirect:/view-plans";
        }
        SciencePlan plan = optionalPlan.get();
        String planName = plan.getPlanName() != null ? plan.getPlanName() : "ID " + planId;

        if (plan.getStatus() != SciencePlanStatus.TESTED) {
            redirectAttributes.addFlashAttribute("submitError", "Plan '" + planName + "' must be TESTED to submit.");
            return "redirect:/view-plans";
        }
        // Check if OCS Plan No exists
        if (plan.getOcsPlanNo() == null || plan.getOcsPlanNo() <= 0) {
            redirectAttributes.addFlashAttribute("submitError", "Plan '" + planName + "' is missing a valid OCS Plan Number. Cannot submit.");
            return "redirect:/view-plans";
        }

        try {
            String username = authentication.getName();
            // Map to OCS plan, mapToOcsPlan will now include the correct planNo and status
            edu.gemini.app.ocs.model.SciencePlan ocsPlanToSubmit = mapToOcsPlan(plan, username);

            System.out.println("Attempting to submit Plan ID: " + planId + " (OCS No: " + ocsPlanToSubmit.getPlanNo() + ") to OCS with status: " + ocsPlanToSubmit.getStatus());

            // Submit to OCS
            String ocsSubmitResult = ocs.submitSciencePlan(ocsPlanToSubmit);
            System.out.println("OCS Submission Result: " + ocsSubmitResult);

            // Update local status
            plan.setStatus(SciencePlanStatus.SUBMITTED);
            sciencePlanRepository.save(plan);
            redirectAttributes.addFlashAttribute("submitSuccess", "Plan '" + planName + "' submitted. OCS Message: " + ocsSubmitResult); // Adjusted message

        } catch (Exception e) {
            System.err.println("Error submitting plan ID " + planId + ": " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed debugging
            redirectAttributes.addFlashAttribute("submitError", "Error submitting plan: " + e.getMessage());
        }
        return "redirect:/view-plans";
    }

    // == Use Case 3: Test Science Plan ==
    @PostMapping("/test/{planId}")
    @PreAuthorize("hasRole('ASTRONOMER')")
    public String testPlan(@PathVariable Long planId, RedirectAttributes redirectAttributes, Authentication authentication) {
        Optional<SciencePlan> optionalPlan = sciencePlanRepository.findById(planId);
        if (optionalPlan.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Science Plan not found: " + planId);
            return "redirect:/view-plans";
        }
        SciencePlan plan = optionalPlan.get();
        String planName = plan.getPlanName() != null ? plan.getPlanName() : "ID " + planId;

        // Allow testing only if CREATED (or maybe TESTED again?)
        if (plan.getStatus() != SciencePlanStatus.CREATED) {
            redirectAttributes.addFlashAttribute("warningMessage", "Plan '" + planName + "' must be in CREATED status to test. Current status: " + plan.getStatus());
            return "redirect:/view-plans";
        }
        // Check if OCS Plan No exists
        if (plan.getOcsPlanNo() == null || plan.getOcsPlanNo() <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Plan '" + planName + "' is missing a valid OCS Plan Number. Cannot test. Please try creating it again or check logs.");
            return "redirect:/view-plans";
        }

        String rawOcsTestResult = "";
        List<String> ocsErrorMessages = new ArrayList<>();
        boolean ocsTestPassed = false;

        try {
            String username = authentication.getName();
            // Map to OCS plan, mapToOcsPlan will now include the correct planNo and SAVED status
            edu.gemini.app.ocs.model.SciencePlan ocsPlanToTest = mapToOcsPlan(plan, username);

            // Logging before sending to OCS
            System.out.println("--- Sending Plan to OCS Test ---");
            System.out.println("Local Plan ID: " + plan.getPlanId());
            System.out.println("OCS Plan No: " + ocsPlanToTest.getPlanNo()); // Should be > 0 now
            System.out.println("Start Date Str: " + ocsPlanToTest.getStartDate());
            System.out.println("End Date Str: " + ocsPlanToTest.getEndDate());
            System.out.println("OCS Status Sent: " + (ocsPlanToTest.getStatus() != null ? ocsPlanToTest.getStatus().name() : "null")); // Should be SAVED
            System.out.println("---------------------------------");

            // Call OCS test method
            rawOcsTestResult = ocs.testSciencePlan(ocsPlanToTest);
            System.out.println("OCS Raw Test Result:\n" + rawOcsTestResult);

            // Process OCS result
            if (rawOcsTestResult != null && !rawOcsTestResult.isEmpty()) {
                boolean foundError = false;
                String[] lines = rawOcsTestResult.split("\\r?\\n");
                for (String line : lines) {
                    // Check common error patterns from ocs.jar test methods
                    if (line.contains(": ERROR:") || line.contains("unsuccessful") || line.contains("failed") || line.contains("Not found planNo")) {
                        ocsErrorMessages.add(line.trim());
                        foundError = true;
                    }
                }
                // OCS testSciencePlan returns the summary string on success, check if it contains errors
                ocsTestPassed = !foundError;
            } else {
                ocsErrorMessages.add("OCS returned an empty or null result during testing.");
                ocsTestPassed = false;
            }

            // Update local status and provide feedback
            if (ocsTestPassed) {
                plan.setStatus(SciencePlanStatus.TESTED); // Update local status
                sciencePlanRepository.save(plan);
                redirectAttributes.addFlashAttribute("testResultMessage", "Plan '" + planName + "' tested successfully (Local status updated to TESTED).\n--- OCS Test Details ---\n" + rawOcsTestResult);
                System.out.println("Plan " + planId + " (OCS No: " + plan.getOcsPlanNo() + ") updated to TESTED locally.");
            } else {
                String formattedErrors = ocsErrorMessages.isEmpty() ? "Unknown failure reported by OCS." : String.join("\n- ", ocsErrorMessages);
                redirectAttributes.addFlashAttribute("testResultMessage", "Plan '" + planName + "' testing failed.\n--- OCS Error Details ---\n- " + formattedErrors + "\n\n--- Full OCS Response ---\n" + rawOcsTestResult);
                System.out.println("Plan " + planId + " (OCS No: " + plan.getOcsPlanNo() + ") testing failed.");
            }
        } catch (Exception e) {
            // Catch exceptions during the testing process
            System.err.println("Error testing plan ID " + planId + ": " + e.getMessage());
            e.printStackTrace(); // Print stack trace for detailed debugging
            String detailedError = "Application Exception: " + e.getMessage();
            redirectAttributes.addFlashAttribute("testResultMessage", "Error during testing process for plan '" + planName + "': " + detailedError + "\n--- OCS Raw Response (if any) ---\n" + rawOcsTestResult);
        }
        return "redirect:/view-plans";
    }

    // --- EDIT Functionality ---
    @GetMapping("/edit/{planId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASTRONOMER')")
    public String showEditPlanForm(@PathVariable Long planId, Model model, RedirectAttributes redirectAttributes, Authentication authentication) {
        Optional<SciencePlan> optionalPlan = sciencePlanRepository.findById(planId);
        if (optionalPlan.isPresent()) {
            SciencePlan plan = optionalPlan.get();

            if (!canModifyPlan(plan, authentication)) {
                String planName = plan.getPlanName() != null ? plan.getPlanName() : "ID " + planId;
                redirectAttributes.addFlashAttribute("errorMessage", "Plan '" + planName + "' cannot be edited due to its status: " + plan.getStatus() + ".");
                return "redirect:/view-plans";
            }

            model.addAttribute("sciencePlan", plan);
            populateDropdownLists(model);
            model.addAttribute("starSystemValidationData", getStarSystemValidationData());
            return "edit_science_plan";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Science Plan not found: " + planId);
            return "redirect:/view-plans";
        }
    }

    @PostMapping("/update/{planId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASTRONOMER')")
    public String updateSciencePlan(@PathVariable Long planId, @Valid @ModelAttribute("sciencePlan") SciencePlan formPlanData, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model, Authentication authentication) {
        Optional<SciencePlan> optionalExistingPlan = sciencePlanRepository.findById(planId);
        if (optionalExistingPlan.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Science Plan not found for update: " + planId);
            return "redirect:/view-plans";
        }
        SciencePlan existingPlan = optionalExistingPlan.get();

        if (!canModifyPlan(existingPlan, authentication)) {
            String planName = existingPlan.getPlanName() != null ? existingPlan.getPlanName() : "ID " + planId;
            redirectAttributes.addFlashAttribute("errorMessage", "Plan '" + planName + "' cannot be updated due to its status: " + existingPlan.getStatus() + ".");
            return "redirect:/view-plans";
        }

        // Check data validity (e.g., date range)
        if (formPlanData.getStartDate() != null && formPlanData.getEndDate() != null && formPlanData.getStartDate().isAfter(formPlanData.getEndDate())) {
            bindingResult.rejectValue("startDate", "date.invalidRange", "Start date must be before end date");
        }

        if (bindingResult.hasErrors()) {
            populateDropdownLists(model);
            model.addAttribute("starSystemValidationData", getStarSystemValidationData());
            // Preserve non-editable fields and ID before returning to form
            formPlanData.setPlanId(existingPlan.getPlanId());
            formPlanData.setStatus(existingPlan.getStatus()); // Keep existing status
            formPlanData.setCreator(existingPlan.getCreator()); // Keep existing creator
            formPlanData.setOcsPlanNo(existingPlan.getOcsPlanNo()); // Keep existing OCS plan number
            model.addAttribute("sciencePlan", formPlanData); // Use form data to show errors on fields
            return "edit_science_plan";
        }

        // Update fields from form data
        existingPlan.setPlanName(formPlanData.getPlanName());
        existingPlan.setFunding(formPlanData.getFunding());
        existingPlan.setObjective(formPlanData.getObjective());
        existingPlan.setStartDate(formPlanData.getStartDate());
        existingPlan.setEndDate(formPlanData.getEndDate());
        existingPlan.setTelescopeLocation(formPlanData.getTelescopeLocation());
        existingPlan.setTargetStarSystem(formPlanData.getTargetStarSystem());

        // Update DataProcessingRequirements safely
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


        // Save changes to the local database
        try {
            sciencePlanRepository.save(existingPlan);
            redirectAttributes.addFlashAttribute("successMessage", "Plan '" + existingPlan.getPlanName() + "' updated locally. Please re-test if necessary before submitting.");
        } catch (Exception dbEx) {
            System.err.println("Error updating plan: " + dbEx.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating plan locally.");
        }
        return "redirect:/view-plans";
    }


    // --- DELETE Functionality ---
    @PostMapping("/delete/{planId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASTRONOMER')")
    public String deletePlan(@PathVariable Long planId, RedirectAttributes redirectAttributes, Authentication authentication) {
        Optional<SciencePlan> optionalPlan = sciencePlanRepository.findById(planId);
        if (optionalPlan.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Science Plan not found for deletion: " + planId);
            return "redirect:/view-plans";
        }
        SciencePlan planToDelete = optionalPlan.get();

        if (!canModifyPlan(planToDelete, authentication)) {
            String planName = planToDelete.getPlanName() != null ? planToDelete.getPlanName() : "ID " + planId;
            redirectAttributes.addFlashAttribute("errorMessage", "Plan '" + planName + "' cannot be deleted due to its status: " + planToDelete.getStatus() + ".");
            return "redirect:/view-plans";
        }

        String planName = planToDelete.getPlanName() != null ? planToDelete.getPlanName() : "ID " + planId;


        System.out.println("Skipping OCS deletion as delete method is not confirmed in OCS.jar or ocsPlanNo might be missing.");

        // Delete locally
        try {
            sciencePlanRepository.deleteById(planId);
            redirectAttributes.addFlashAttribute("successMessage", "Plan '" + planName + "' deleted locally.");
            System.out.println("Deleted local plan ID: " + planId);
        } catch (Exception e) {
            System.err.println("Error deleting local plan ID " + planId + ": " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting local plan: " + e.getMessage());
        }
        return "redirect:/view-plans";
    }


    // --- Helper methods ---

    private edu.gemini.app.ocs.model.SciencePlan mapToOcsPlan(th.ac.mahidol.ict.Gemini_d6.model.SciencePlan ourPlan, String creatorUsername) {
        edu.gemini.app.ocs.model.SciencePlan ocsPlan = new edu.gemini.app.ocs.model.SciencePlan();

        if (ourPlan.getOcsPlanNo() != null && ourPlan.getOcsPlanNo() > 0) {
            ocsPlan.setPlanNo(ourPlan.getOcsPlanNo());
        } else {
            // If no ocsPlanNo is stored locally, don't set it (or set to 0 if required by OCS constructor/logic)
            // This happens during the initial mapping in the /save endpoint before OCS assigns an ID.
            System.out.println("Mapping to OCS Plan: Local ocsPlanNo is null or zero for local plan ID: " + ourPlan.getPlanId());
        }

        // Map other fields
        ocsPlan.setCreator(creatorUsername); // Or fetch if needed based on your logic
        ocsPlan.setSubmitter(creatorUsername); // Or fetch if needed based on your logic
        ocsPlan.setFundingInUSD(ourPlan.getFunding() != null ? ourPlan.getFunding().doubleValue() : 0.0);
        ocsPlan.setObjectives(ourPlan.getObjective() != null ? ourPlan.getObjective() : "");

        // Map StarSystem Enum
        if (ourPlan.getTargetStarSystem() != null) {
            try {
                ocsPlan.setStarSystem(StarSystem.CONSTELLATIONS.valueOf(ourPlan.getTargetStarSystem()));
            } catch (IllegalArgumentException e) {
                System.err.println("Warn: StarSystem mapping failed for value: " + ourPlan.getTargetStarSystem());
                // Handle error - maybe set a default or throw exception?
            }
        }

        // Map Dates using the format OCS expects in its setter
        DateTimeFormatter ocsSetterFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        if (ourPlan.getStartDate() != null) {
            try {
                ocsPlan.setStartDate(ourPlan.getStartDate().format(ocsSetterFormat));
            } catch (Exception e) {
                System.err.println("Warn: StartDate format/mapping failed: " + e.getMessage());
                ocsPlan.setStartDate("-1"); // OCS seems to use "-1" for invalid dates
            }
        } else {
            ocsPlan.setStartDate("-1");
        }
        if (ourPlan.getEndDate() != null) {
            try {
                ocsPlan.setEndDate(ourPlan.getEndDate().format(ocsSetterFormat));
            } catch (Exception e) {
                System.err.println("Warn: EndDate format/mapping failed: " + e.getMessage());
                ocsPlan.setEndDate("-1");
            }
        } else {
            ocsPlan.setEndDate("-1");
        }

        // Map TelescopeLocation Enum
        if (ourPlan.getTelescopeLocation() != null) {
            try {
                ocsPlan.setTelescopeLocation(edu.gemini.app.ocs.model.SciencePlan.TELESCOPELOC.valueOf(ourPlan.getTelescopeLocation().name()));
            } catch (IllegalArgumentException e) {
                System.err.println("Warn: TelescopeLocation mapping failed for value: " + ourPlan.getTelescopeLocation().name());
            }
        }

        // Map DataProcessingRequirements
        th.ac.mahidol.ict.Gemini_d6.model.DataProcessingRequirements ourDpr = ourPlan.getDataProcessingRequirements();
        if (ourDpr != null) {
            DataProcRequirement ocsDpr = new DataProcRequirement(
                    ourDpr.getFileType() != null ? ourDpr.getFileType().name() : "", // Assuming FileType enum matches OCS strings (PNG, JPEG, RAW)
                    ourDpr.getFileQuality() != null ? mapFileQualityToOcsString(ourDpr.getFileQuality()) : "",
                    ourDpr.getColorType() != null ? mapColorTypeToOcsString(ourDpr.getColorType()) : "",
                    ourDpr.getContrast() != null ? ourDpr.getContrast().doubleValue() : 0.0,
                    ourDpr.getBrightness() != null ? ourDpr.getBrightness().doubleValue() : 0.0,
                    ourDpr.getSaturation() != null ? ourDpr.getSaturation().doubleValue() : 0.0,
                    ourDpr.getHighlights() != null ? ourDpr.getHighlights().doubleValue() : 0.0,
                    ourDpr.getExposure() != null ? ourDpr.getExposure().doubleValue() : 0.0,
                    ourDpr.getShadows() != null ? ourDpr.getShadows().doubleValue() : 0.0,
                    ourDpr.getWhites() != null ? ourDpr.getWhites().doubleValue() : 0.0,
                    ourDpr.getBlacks() != null ? ourDpr.getBlacks().doubleValue() : 0.0,
                    ourDpr.getLuminance() != null ? ourDpr.getLuminance().doubleValue() : 0.0,
                    ourDpr.getHue() != null ? ourDpr.getHue().doubleValue() : 0.0
            );
            ocsPlan.setDataProcRequirements(ocsDpr);
        } else {
            System.err.println("Warn: DataProcessingRequirements are null for local plan ID: " + ourPlan.getPlanId());
            // Optionally create a default OCS DPR object if required by OCS
            ocsPlan.setDataProcRequirements(new DataProcRequirement()); // Set default empty requirements
        }

        // *** FIXED: Map Status correctly based on the intended OCS operation ***
        th.ac.mahidol.ict.Gemini_d6.model.SciencePlanStatus localStatus = ourPlan.getStatus();
        edu.gemini.app.ocs.model.SciencePlan.STATUS ocsStatusToSet = edu.gemini.app.ocs.model.SciencePlan.STATUS.SAVED; // Sensible default

        if (localStatus == SciencePlanStatus.CREATED) {
            // When testing or creating initially, OCS expects SAVED status
            ocsStatusToSet = edu.gemini.app.ocs.model.SciencePlan.STATUS.SAVED;
        } else if (localStatus == SciencePlanStatus.TESTED) {
            // When submitting, OCS expects TESTED status
            ocsStatusToSet = edu.gemini.app.ocs.model.SciencePlan.STATUS.TESTED;
        }

        ocsPlan.setStatus(ocsStatusToSet);

        return ocsPlan;
    }

    private List<Map<String, String>> getStarSystemValidationData() {
        List<Map<String, String>> validationData = new ArrayList<>();
        try {
            for (StarSystem.CONSTELLATIONS c : StarSystem.CONSTELLATIONS.values()) {
                Map<String, String> d = new HashMap<>();
                d.put("name", c.name());
                d.put("month", String.valueOf(c.getmonth())); // Visibility month

                Quadrant.QUADRANT q = c.getQuadrant();
                String requiredLocation = "N/A"; // Required Telescope Location based on quadrant
                String quadrantName = "N/A"; // Quadrant Name

                if (q != null) {
                    quadrantName = q.name();
                    // Determine required location based on OCS logic (N -> HAWAII, S -> CHILE)
                    if (quadrantName.toUpperCase().startsWith("N")) {
                        requiredLocation = TelescopeLocation.HAWAII.name();
                    } else if (quadrantName.toUpperCase().startsWith("S")) {
                        requiredLocation = TelescopeLocation.CHILE.name();
                    }
                }
                d.put("location", requiredLocation); // e.g., "HAWAII" or "CHILE"
                d.put("quadrant", quadrantName);    // e.g., "NQ1", "SQ3"
                validationData.add(d);
            }
        } catch (Exception e) {
            // Log the error if fetching star system data fails
            System.err.println("ERROR retrieving/processing star system validation data: " + e.getMessage());
            e.printStackTrace();
            // Return empty list or handle error appropriately
        }
        return validationData;
    }

    private void populateDropdownLists(Model model) {
        model.addAttribute("telescopeLocations", TelescopeLocation.values());
        model.addAttribute("fileTypes", FileType.values());
        model.addAttribute("fileQualities", FileQuality.values());
        model.addAttribute("colorTypes", ColorType.values());
        try {
            // Add star systems from OCS enum
            model.addAttribute("starSystemEnums", StarSystem.CONSTELLATIONS.values());
        } catch (Exception e) {
            System.err.println("Error populating star system dropdown: " + e.getMessage());
            model.addAttribute("starSystemEnums", new ArrayList<>()); // Provide empty list on error
        }
    }

    // Helper to map local FileQuality enum to OCS string
    private String mapFileQualityToOcsString(FileQuality fq) {
        if (fq == null) return "";
        return fq == FileQuality.LOW ? "Low" : (fq == FileQuality.FINE ? "Fine" : "");
    }

    // Helper to map local ColorType enum to OCS string
    private String mapColorTypeToOcsString(ColorType ct) {
        if (ct == null) return "";
        return ct == ColorType.COLOR ? "Color mode" : (ct == ColorType.BW ? "B&W mode" : "");
    }

    // Helper method to check if a user can modify a plan based on role and status
    private boolean canModifyPlan(SciencePlan plan, Authentication authentication) {
        if (plan == null || authentication == null) {
            return false;
        }

        // Define statuses that prevent modification by astronomers
        List<SciencePlanStatus> restrictedStatusesForAstronomer = List.of(
                SciencePlanStatus.SUBMITTED,
                SciencePlanStatus.VALIDATED,
                SciencePlanStatus.INVALIDATED,
                SciencePlanStatus.RUNNING,
                SciencePlanStatus.COMPLETE,
                SciencePlanStatus.CANCELLED
        );

        // Check if the user is an ADMIN
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        // Admins can always modify/delete (adjust this rule if needed)
        if (isAdmin) {
            return true;
        }

        // Check if the user is an ASTRONOMER
        boolean isAstronomer = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ASTRONOMER"::equals);

        // Astronomers can modify only if the status is NOT in the restricted list
        if (isAstronomer) {
            return !restrictedStatusesForAstronomer.contains(plan.getStatus());
        }

        // Other roles cannot modify/delete
        return false;
    }

} // End of Class