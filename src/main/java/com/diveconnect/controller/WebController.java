package com.diveconnect.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    // Cualquier ruta del frontend que no sea /api/... devuelve index.html
    @GetMapping(value = {"/", "/pages/**"})
    public String index() {
        return "forward:/index.html";
    }
}
