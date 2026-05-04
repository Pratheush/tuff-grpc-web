package com.mylearning.oauth_server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OAuth2Controller {

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }
}
