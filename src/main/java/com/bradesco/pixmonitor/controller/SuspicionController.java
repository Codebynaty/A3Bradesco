package com.bradesco.pixmonitor.controller;

import com.bradesco.pixmonitor.dto.SuspicionRequest;
import com.bradesco.pixmonitor.service.PixSuspicionPredictor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SuspicionController {

    private final PixSuspicionPredictor predictor;

    public SuspicionController(PixSuspicionPredictor predictor) {
        this.predictor = predictor;
    }

    @PostMapping("/predict-suspicion")
    public ResponseEntity<Map<String, Object>> predictSuspicion(@Valid @RequestBody SuspicionRequest request) {
        Map<String, Object> resultado = predictor.analisarConta(
            request.getQuantidadeDenuncias(),
            request.getTempoEntreDenuncias(),
            request.getFrequenciaDenuncias(),
            request.getQuantidadeRecebimentos(),
            request.getValorTotalRecebido(),
            request.getTempoDesdeCriacao()
        );

        return ResponseEntity.ok(resultado);
    }
    
    @PostMapping("/analisar-conta")
    public ResponseEntity<Map<String, Object>> analisarConta(@Valid @RequestBody SuspicionRequest request) {
        Map<String, Object> analise = predictor.analisarConta(
            request.getQuantidadeDenuncias(),
            request.getTempoEntreDenuncias(),
            request.getFrequenciaDenuncias(),
            request.getQuantidadeRecebimentos(),
            request.getValorTotalRecebido(),
            request.getTempoDesdeCriacao()
        );

        return ResponseEntity.ok(analise);
    }
} 