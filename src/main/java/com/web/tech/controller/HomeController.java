package com.web.tech.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/landing")
    public String home(Model model) {
        return "pages/landing";
    }
}