package com.springboot.project.community.controller.auth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/web")
public class WebController {

    /**
     * 세션 기반 웹 홈
     */
    @GetMapping("/home")
    public String home() {
        return "home";  // home.html 반환
    }

    /**
     * 세션 기반 프로필 페이지
     */
    @GetMapping("/profile")
    public String profile() {
        return "profile";  // profile.html 반환
    }
}
