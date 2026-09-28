package com.leave.management.common.config;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError() {
        // Forward to the index.html on 404 for SPA routing
        return "forward:/index.html";
    }
}
