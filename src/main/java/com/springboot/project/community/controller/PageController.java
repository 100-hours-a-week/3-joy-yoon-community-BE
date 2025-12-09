package com.springboot.project.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 정적 페이지 컨트롤러
 * - 서비스 이용약관, 개인정보처리방침 등
 */
@Controller
public class PageController {

    @GetMapping("/service")
    public String service() {
        return "service";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }
}




