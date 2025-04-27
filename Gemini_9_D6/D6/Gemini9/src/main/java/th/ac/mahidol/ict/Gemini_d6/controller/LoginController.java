package th.ac.mahidol.ict.Gemini_d6.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {


    @GetMapping("/login")
    public String login() {

        return "login";
    }


    @GetMapping("/welcome")
    public String welcome() {

        return "welcome";
    }


    @GetMapping("/")
    public String home() {
        return "redirect:/welcome";
    }
}