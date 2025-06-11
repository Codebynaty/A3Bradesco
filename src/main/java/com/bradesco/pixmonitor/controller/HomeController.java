package com.bradesco.pixmonitor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/web")
    public String index() {
        return "redirect:/index.html";
    }
    
    @GetMapping("/denuncia")
    public String denuncia() {
        return "redirect:/denuncia.html";
    }
    
    @GetMapping("/admin")
    public String admin() {
        return "redirect:/funcionario.html";
    }
    
    @GetMapping("/cliente") 
    public String cliente() {
        return "redirect:/cliente.html";
    }
    
    @GetMapping("/funcionario")
    public String funcionario() {
        return "redirect:/funcionario.html";
    }
} 