package com.bradesco.pixmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
public class PixMonitorApplication {

    public static void main(String[] args) {
        System.out.println("=== BRADESCO PIX MONITOR ===");
        System.out.println("Iniciando aplicação...");
        SpringApplication.run(PixMonitorApplication.class, args);
        System.out.println("Aplicação iniciada com sucesso!");
        System.out.println("Acesse: http://localhost:8080/");
    }

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("aplicacao", "Bradesco PIX Monitor");
        response.put("status", "FUNCIONANDO");
        response.put("timestamp", LocalDateTime.now());
        response.put("versao", "1.0.0");
        response.put("endpoints", new String[]{
            "/api/test/ping",
            "/api/test/health",
            "/h2-console",
            "/web"
        });
        return response;
    }

    @GetMapping("/api/test/ping")
    public String ping() {
        return "pong - Bradesco PIX Monitor funcionando!";
    }

    @GetMapping("/api/test/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("servico", "Bradesco PIX Monitor");
        response.put("database", "H2 (em memória)");
        return response;
    }
} 