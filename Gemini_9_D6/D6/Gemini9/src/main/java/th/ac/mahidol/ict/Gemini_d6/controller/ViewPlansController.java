package th.ac.mahidol.ict.Gemini_d6.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


import th.ac.mahidol.ict.Gemini_d6.model.SciencePlan;
import th.ac.mahidol.ict.Gemini_d6.repository.SciencePlanRepository;
import java.util.List; // Use List interface

@Controller
public class ViewPlansController {


    @Autowired
    private SciencePlanRepository sciencePlanRepository;

    @GetMapping("/view-plans") // Path to access this page
    public String showAllPlans(Model model) {
        try {

            List<SciencePlan> allPlans = sciencePlanRepository.findAll();


            model.addAttribute("sciencePlans", allPlans);

        } catch (Exception e) {

            System.err.println("Error retrieving plans from local DB: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error retrieving plans from database: " + e.getMessage());
        }

        return "view_science_plans";
    }
}