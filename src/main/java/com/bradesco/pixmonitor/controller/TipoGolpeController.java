package com.bradesco.pixmonitor.controller;

import com.bradesco.pixmonitor.model.TipoGolpe;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tipos-golpes")
@CrossOrigin(origins = "*")
public class TipoGolpeController {

    /**
     * Lista todos os tipos de golpes disponíveis
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listarTiposGolpes() {
        Map<String, Object> response = new HashMap<>();
        
        List<Map<String, Object>> tipos = Arrays.stream(TipoGolpe.values())
            .map(this::convertToMap)
            .collect(Collectors.toList());
        
        response.put("tipos", tipos);
        response.put("total", tipos.size());
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Lista tipos de golpes agrupados por categoria
     */
    @GetMapping("/por-categoria")
    public ResponseEntity<Map<String, Object>> listarPorCategoria() {
        Map<String, Object> response = new HashMap<>();
        
        Map<String, List<Map<String, Object>>> categorias = Arrays.stream(TipoGolpe.values())
            .collect(Collectors.groupingBy(
                tipo -> tipo.getCategoria().name(),
                Collectors.mapping(this::convertToMap, Collectors.toList())
            ));
        
        // Ordenar categorias e adicionar metadados
        Map<String, Object> categoriasFormatadas = new LinkedHashMap<>();
        
        for (TipoGolpe.CategoriaGolpe categoria : TipoGolpe.CategoriaGolpe.values()) {
            String key = categoria.name();
            if (categorias.containsKey(key)) {
                Map<String, Object> categoriaInfo = new HashMap<>();
                categoriaInfo.put("nome", categoria.getNome());
                categoriaInfo.put("emoji", getEmojiCategoria(categoria));
                categoriaInfo.put("tipos", categorias.get(key));
                categoriaInfo.put("total", categorias.get(key).size());
                
                categoriasFormatadas.put(key, categoriaInfo);
            }
        }
        
        response.put("categorias", categoriasFormatadas);
        response.put("totalCategorias", categoriasFormatadas.size());
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Busca tipos de golpes por severidade
     */
    @GetMapping("/por-severidade/{severidade}")
    public ResponseEntity<Map<String, Object>> listarPorSeveridade(@PathVariable String severidade) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            TipoGolpe.SeveridadeGolpe sev = TipoGolpe.SeveridadeGolpe.valueOf(severidade.toUpperCase());
            
            List<Map<String, Object>> tipos = Arrays.stream(TipoGolpe.values())
                .filter(tipo -> tipo.getSeveridade() == sev)
                .map(this::convertToMap)
                .collect(Collectors.toList());
            
            response.put("tipos", tipos);
            response.put("severidade", sev.getNome());
            response.put("total", tipos.size());
            response.put("success", true);
            
        } catch (IllegalArgumentException e) {
            response.put("error", "Severidade inválida: " + severidade);
            response.put("success", false);
            response.put("severidadesDisponiveis", Arrays.stream(TipoGolpe.SeveridadeGolpe.values())
                .map(s -> s.name().toLowerCase())
                .collect(Collectors.toList()));
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Busca tipo de golpe específico por código
     */
    @GetMapping("/{codigo}")
    public ResponseEntity<Map<String, Object>> buscarPorCodigo(@PathVariable String codigo) {
        Map<String, Object> response = new HashMap<>();
        
        TipoGolpe tipo = TipoGolpe.porCodigo(codigo.toUpperCase());
        
        if (tipo != null) {
            response.put("tipo", convertToMap(tipo));
            response.put("success", true);
        } else {
            response.put("error", "Tipo de golpe não encontrado: " + codigo);
            response.put("success", false);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Busca tipos de golpes por termo de pesquisa
     */
    @GetMapping("/buscar")
    public ResponseEntity<Map<String, Object>> buscarTipos(@RequestParam String termo) {
        Map<String, Object> response = new HashMap<>();
        
        String termoBusca = termo.toLowerCase().trim();
        
        List<Map<String, Object>> tipos = Arrays.stream(TipoGolpe.values())
            .filter(tipo -> 
                tipo.getNome().toLowerCase().contains(termoBusca) ||
                tipo.getDescricao().toLowerCase().contains(termoBusca) ||
                tipo.getCategoria().getNome().toLowerCase().contains(termoBusca)
            )
            .map(this::convertToMap)
            .collect(Collectors.toList());
        
        response.put("tipos", tipos);
        response.put("termo", termo);
        response.put("total", tipos.size());
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Estatísticas dos tipos de golpes
     */
    @GetMapping("/estatisticas")
    public ResponseEntity<Map<String, Object>> obterEstatisticas() {
        Map<String, Object> response = new HashMap<>();
        
        // Estatísticas por categoria
        Map<String, Long> estatisticasCategoria = Arrays.stream(TipoGolpe.values())
            .collect(Collectors.groupingBy(
                tipo -> tipo.getCategoria().getNome(),
                Collectors.counting()
            ));
        
        // Estatísticas por severidade
        Map<String, Long> estatisticasSeveridade = Arrays.stream(TipoGolpe.values())
            .collect(Collectors.groupingBy(
                tipo -> tipo.getSeveridade().getNome(),
                Collectors.counting()
            ));
        
        // Tipos mais críticos
        List<Map<String, Object>> tiposCriticos = Arrays.stream(TipoGolpe.values())
            .filter(tipo -> tipo.getSeveridade() == TipoGolpe.SeveridadeGolpe.CRITICA)
            .map(this::convertToMap)
            .collect(Collectors.toList());
        
        response.put("totalTipos", TipoGolpe.values().length);
        response.put("porCategoria", estatisticasCategoria);
        response.put("porSeveridade", estatisticasSeveridade);
        response.put("tiposCriticos", tiposCriticos);
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Converte TipoGolpe para Map para serialização JSON
     */
    private Map<String, Object> convertToMap(TipoGolpe tipo) {
        Map<String, Object> map = new HashMap<>();
        map.put("codigo", tipo.getCodigo());
        map.put("nome", tipo.getNome());
        map.put("descricao", tipo.getDescricao());
        map.put("categoria", tipo.getCategoria().getNome());
        map.put("severidade", tipo.getSeveridade().getNome());
        map.put("nivelSeveridade", tipo.getSeveridade().getNivel());
        map.put("emoji", tipo.getEmoji());
        map.put("corSeveridade", tipo.getCorSeveridade());
        
        // Informações adicionais para o frontend
        map.put("categoriaKey", tipo.getCategoria().name());
        map.put("severidadeKey", tipo.getSeveridade().name());
        
        return map;
    }

    /**
     * Retorna emoji específico para cada categoria
     */
    private String getEmojiCategoria(TipoGolpe.CategoriaGolpe categoria) {
        return switch (categoria) {
            case RELACIONAMENTO -> "💔";
            case FINANCEIRO -> "💰";
            case IDENTIDADE -> "🎭";
            case COMERCIAL -> "🛍️";
            case TECNOLOGICO -> "💻";
            case PREMIO -> "🎁";
            case GOVERNO -> "🏛️";
            case SOCIAL -> "🤝";
            case OUTROS -> "❓";
        };
    }
} 