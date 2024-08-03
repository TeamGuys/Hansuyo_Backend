package com.ardkyer.rion.controller;

import com.ardkyer.rion.entity.User;
import com.ardkyer.rion.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@Tag(name = "Web", description = "Web page controller")
public class WebController {

    private final UserService userService;

    @Autowired
    public WebController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    @Operation(summary = "Show registration form", description = "Displays the user registration form")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    @Operation(summary = "Register user", description = "Processes the user registration")
    public String registerUser(@ModelAttribute User user) {
        userService.registerUser(user);
        return "redirect:/login";
    }

    @GetMapping({"/", "/home"})
    @Operation(summary = "Home page", description = "Displays the home page")
    public String home() {
        return "home";
    }

    @GetMapping("/login")
    @Operation(summary = "Login page", description = "Displays the login page")
    public String login() {
        return "login";
    }

    @GetMapping("/upload")
    @Operation(summary = "Upload page", description = "Displays the upload page")
    public String upload() {
        return "upload";
    }

    @GetMapping("/swagger")
    @Operation(summary = "Swagger UI", description = "Redirects to Swagger UI")
    public String swagger() {
        return "redirect:/swagger-ui/index.html";
    }

    @GetMapping("/api-docs")
    @Operation(summary = "API docs", description = "Redirects to API documentation")
    public String apiDocs() {
        return "redirect:/v2/api-docs";
    }
}