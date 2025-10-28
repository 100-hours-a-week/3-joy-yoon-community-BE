package com.springboot.project.community.controller.service;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ServiceController {

    @RequestMapping("/service")
    public String service() {
        return "/service.html";
    }

    @RequestMapping("/privacy")
    public String privacy() {
        return "/privacy.html";
    }

}
