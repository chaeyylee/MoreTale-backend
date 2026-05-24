package com.moretale.global.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @GetMapping("/")
    public String home() {
        return "redirect:" + frontendUrl;
    }

    @GetMapping("/login")
    public String login() {
        return "redirect:" + frontendUrl + "/login";
    }
}
