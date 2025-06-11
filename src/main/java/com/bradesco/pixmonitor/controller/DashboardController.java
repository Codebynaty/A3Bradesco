package com.bradesco.pixmonitor.controller;

import com.bradesco.pixmonitor.service.BancoDadosService;
import com.bradesco.pixmonitor.service.DenunciaService;
import com.bradesco.pixmonitor.service.PixSuspicionPredictor;
import com.bradesco.pixmonitor.config.BradescoPixConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    
    @Autowired
    private BancoDadosService bancoDadosService;
    
    @Autowired
    private DenunciaService denunciaService;
    
    @Autowired
    private PixSuspicionPredictor pixSuspicionPredictor;
    
    @Autowired
    private BradescoPixConfig config;
    
    /**
     * Retorna métricas principais para o dashboard
     */
    @GetMapping("/metricas")
    public ResponseEntity<Map<String, Object>> getMetricasPrincipais() {
        logger.info("🔔 Buscando métricas principais do dashboard");
        
        try {
            Map<String, Object> relatorio = bancoDadosService.gerarRelatorioCompleto();
            
            // Calcula métricas em tempo real
            long denunciasHoje = denunciaService.contarDenunciasHoje();
            long contasBloqueadas = bancoDadosService.listarContasBloqueadas().size();
            long contasRiscoAlto = bancoDadosService.listarScoresRiscoAlto().size();
            
            // Métricas de performance da IA
            double taxaDeteccaoIA = calcularTaxaDeteccaoIA();
            double tempoMedioAnalise = calcularTempoMedioAnalise();
            
            Map<String, Object> metricas = new HashMap<>();
            metricas.put("denunciasHoje", denunciasHoje);
            metricas.put("denunciasOntem", denunciasHoje - 12); // Simulado para comparação
            metricas.put("contasBloqueadas", contasBloqueadas);
            metricas.put("contasNovasBloqueadas", Math.max(0, contasBloqueadas - 40));
            metricas.put("taxaDeteccaoIA", Math.round(taxaDeteccaoIA * 10.0) / 10.0);
            metricas.put("melhoriaDeteccao", 2.1);
            metricas.put("tempoMedioAnalise", tempoMedioAnalise);
            metricas.put("contasRiscoAlto", contasRiscoAlto);
            metricas.put("timestamp", LocalDateTime.now());
            
            logger.info("✅ Métricas calculadas: {} denúncias hoje, {} contas bloqueadas", 
                       denunciasHoje, contasBloqueadas);
            
            return ResponseEntity.ok(metricas);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao calcular métricas: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                Map.of("error", "Erro interno do servidor", "message", e.getMessage())
            );
        }
    }
    
    /**
     * Retorna dados para gráfico de denúncias por hora
     */
    @GetMapping("/graficos/denuncias-hora")
    public ResponseEntity<Map<String, Object>> getDenunciasPorHora() {
        logger.info("📊 Gerando dados do gráfico de denúncias por hora");
        
        try {
            List<Map<String, Object>> dadosHorarios = denunciaService.getDenunciasPorHora();
            
            List<String> horarios = new ArrayList<>();
            List<Integer> quantidades = new ArrayList<>();
            
            // Gera dados das últimas 24 horas
            LocalDateTime agora = LocalDateTime.now();
            for (int i = 23; i >= 0; i--) {
                LocalDateTime hora = agora.minusHours(i);
                String horaStr = hora.format(DateTimeFormatter.ofPattern("HH:mm"));
                horarios.add(horaStr);
                
                // Simula dados baseados na hora (mais denúncias durante horário comercial)
                int quantidade = simularDenunciasPorHora(hora.getHour());
                quantidades.add(quantidade);
            }
            
            Map<String, Object> grafico = new HashMap<>();
            grafico.put("labels", horarios);
            grafico.put("dados", quantidades);
            grafico.put("total", quantidades.stream().mapToInt(Integer::intValue).sum());
            grafico.put("maiorPico", Collections.max(quantidades));
            grafico.put("horaPico", horarios.get(quantidades.indexOf(Collections.max(quantidades))));
            
            return ResponseEntity.ok(grafico);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao gerar gráfico por hora: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                Map.of("error", "Erro ao gerar gráfico", "message", e.getMessage())
            );
        }
    }
    
    /**
     * Retorna dados para gráfico de tipos de fraude
     */
    @GetMapping("/graficos/tipos-fraude")
    public ResponseEntity<Map<String, Object>> getTiposFraude() {
        logger.info("🎯 Gerando dados do gráfico de tipos de fraude");
        
        try {
            Map<String, Integer> tiposFraude = new HashMap<>();
            tiposFraude.put("Phishing", 45);
            tiposFraude.put("Engenharia Social", 32);
            tiposFraude.put("Conta Clonada", 28);
            tiposFraude.put("Transação Não Autorizada", 23);
            tiposFraude.put("Chave PIX Comprometida", 19);
            tiposFraude.put("Outros", 15);
            
            Map<String, Object> grafico = new HashMap<>();
            grafico.put("labels", new ArrayList<>(tiposFraude.keySet()));
            grafico.put("dados", new ArrayList<>(tiposFraude.values()));
            grafico.put("cores", Arrays.asList(
                "#cc092f", "#e8314d", "#ff6b6b", "#ffa500", "#32cd32", "#6495ed"
            ));
            grafico.put("total", tiposFraude.values().stream().mapToInt(Integer::intValue).sum());
            
            return ResponseEntity.ok(grafico);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao gerar gráfico de tipos: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                Map.of("error", "Erro ao gerar gráfico", "message", e.getMessage())
            );
        }
    }
    
    /**
     * Retorna alertas urgentes para o dashboard
     */
    @GetMapping("/alertas")
    public ResponseEntity<List<Map<String, Object>>> getAlertasUrgentes() {
        logger.info("🚨 Buscando alertas urgentes");
        
        try {
            List<Map<String, Object>> alertas = new ArrayList<>();
            
            // Busca contas com muitas denúncias recentes
            List<Object[]> contasProblematicas = bancoDadosService.obterTopContasComMaisDenuncias();
            
            for (Object[] conta : contasProblematicas) {
                if (conta.length >= 2) {
                    String idConta = conta[0].toString();
                    Long quantidadeDenuncias = (Long) conta[1];
                    
                    if (quantidadeDenuncias >= 10) {
                        Map<String, Object> alerta = new HashMap<>();
                        alerta.put("tipo", "CONTA_ALTA_DENUNCIA");
                        alerta.put("nivel", quantidadeDenuncias >= 15 ? "URGENTE" : "ALTO");
                        alerta.put("titulo", "Conta " + idConta);
                        alerta.put("descricao", quantidadeDenuncias + " denúncias recentes");
                        alerta.put("tempo", "Detectado há 15 min");
                        alerta.put("icone", quantidadeDenuncias >= 15 ? "fire" : "exclamation-triangle");
                        alerta.put("acao", "Investigar");
                        alertas.add(alerta);
                    }
                }
            }
            
            // Adiciona alertas simulados de padrões suspeitos
            if (alertas.size() < 3) {
                alertas.add(Map.of(
                    "tipo", "PADRAO_SUSPEITO",
                    "nivel", "ALTO",
                    "titulo", "Padrão Suspeito Detectado",
                    "descricao", "5 contas com CPF similar criadas hoje",
                    "tempo", "Detectado há 8 min",
                    "icone", "users",
                    "acao", "Analisar"
                ));
            }
            
            // Ordena por urgência
            alertas.sort((a, b) -> {
                String nivelA = (String) a.get("nivel");
                String nivelB = (String) b.get("nivel");
                return nivelB.compareTo(nivelA); // URGENTE > ALTO > MEDIO
            });
            
            return ResponseEntity.ok(alertas.stream().limit(5).collect(Collectors.toList()));
            
        } catch (Exception e) {
            logger.error("❌ Erro ao buscar alertas: {}", e.getMessage(), e);
            return ResponseEntity.ok(new ArrayList<>()); // Retorna lista vazia em caso de erro
        }
    }
    
    /**
     * Retorna dados das denúncias para tabela
     */
    @GetMapping("/denuncias")
    public ResponseEntity<Map<String, Object>> getDenunciasParaTabela(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String filtro,
            @RequestParam(defaultValue = "") String status) {
        
        logger.info("📋 Buscando denúncias para tabela: page={}, size={}, filtro={}", 
                   page, size, filtro);
        
        try {
            List<Map<String, Object>> denuncias = denunciaService.getDenunciasParaTabela(
                page, size, filtro, status);
            
            long totalDenuncias = denunciaService.contarDenuncias(filtro, status);
            int totalPages = (int) Math.ceil((double) totalDenuncias / size);
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("denuncias", denuncias);
            resultado.put("totalElements", totalDenuncias);
            resultado.put("totalPages", totalPages);
            resultado.put("currentPage", page);
            resultado.put("size", size);
            resultado.put("hasNext", page < totalPages - 1);
            resultado.put("hasPrevious", page > 0);
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao buscar denúncias: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                Map.of("error", "Erro ao buscar denúncias", "message", e.getMessage())
            );
        }
    }
    
    /**
     * Retorna dados das contas suspeitas para tabela
     */
    @GetMapping("/contas-suspeitas")
    public ResponseEntity<Map<String, Object>> getContasSuspeitasParaTabela(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String filtro) {
        
        logger.info("🔍 Buscando contas suspeitas para tabela: page={}, size={}", page, size);
        
        try {
            List<Map<String, Object>> contasSuspeitas = new ArrayList<>();
            
            // Busca contas com scores baixos
            List<Object> scoresRiscoAlto = bancoDadosService.listarScoresRiscoAlto()
                .stream()
                .limit((long) size * (page + 1))
                .skip((long) size * page)
                .collect(Collectors.toList());
            
            for (int i = 0; i < scoresRiscoAlto.size(); i++) {
                Map<String, Object> conta = new HashMap<>();
                conta.put("id", "ACC" + String.format("%06d", i + 1 + (page * size)));
                conta.put("cpf", "***." + String.format("%03d", new Random().nextInt(1000)) + ".***-**");
                conta.put("nome", "Cliente " + (i + 1 + (page * size)));
                conta.put("score", 30 + new Random().nextInt(30)); // Score baixo
                conta.put("denuncias", 5 + new Random().nextInt(10));
                conta.put("ultimaAtividade", LocalDateTime.now().minusHours(new Random().nextInt(48)));
                conta.put("status", new Random().nextBoolean() ? "BLOQUEADA" : "MONITORADA");
                conta.put("risco", "ALTO");
                contasSuspeitas.add(conta);
            }
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("contas", contasSuspeitas);
            resultado.put("totalElements", bancoDadosService.listarScoresRiscoAlto().size());
            resultado.put("currentPage", page);
            resultado.put("size", size);
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao buscar contas suspeitas: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                Map.of("error", "Erro ao buscar contas", "message", e.getMessage())
            );
        }
    }
    
    /**
     * Realiza análise de IA para uma conta específica
     */
    @PostMapping("/analise-ia")
    public ResponseEntity<Map<String, Object>> realizarAnaliseIA(@RequestBody Map<String, Object> request) {
        logger.info("🤖 Realizando análise de IA para conta");
        
        try {
            String cpf = (String) request.get("cpf");
            String nomeTitular = (String) request.get("nomeTitular");
            
            // Simula dados para análise
            double valorTotalRecebido = 50000.0 + (Math.random() * 200000);
            int quantidadeRecebimentos = 10 + (int)(Math.random() * 50);
            int quantidadeDenuncias = (int)(Math.random() * 15);
            int tempoDesdeCriacao = 30 + (int)(Math.random() * 365);
            
            Map<String, Object> analise = pixSuspicionPredictor.analisarConta(
                quantidadeDenuncias, 
                15, // tempo entre denúncias
                quantidadeDenuncias / 30.0, // frequência
                quantidadeRecebimentos,
                valorTotalRecebido,
                tempoDesdeCriacao
            );
            
            // Adiciona informações detalhadas para o frontend
            analise.put("cpf", cpf);
            analise.put("nomeTitular", nomeTitular);
            analise.put("valorTotalRecebido", valorTotalRecebido);
            analise.put("quantidadeRecebimentos", quantidadeRecebimentos);
            analise.put("tempoDesdeCriacao", tempoDesdeCriacao);
            analise.put("timestamp", LocalDateTime.now());
            
            // Adiciona recomendações baseadas no resultado
            List<String> recomendacoes = new ArrayList<>();
            boolean contaSuspeita = (Boolean) analise.get("contaSuspeita");
            double confianca = (Double) analise.get("confianca");
            
            if (contaSuspeita && confianca > 0.8) {
                recomendacoes.add("Bloqueio imediato da conta recomendado");
                recomendacoes.add("Investigação detalhada das transações");
                recomendacoes.add("Contato com cliente para verificação");
            } else if (contaSuspeita) {
                recomendacoes.add("Monitoramento intensivo recomendado");
                recomendacoes.add("Análise manual das últimas transações");
            } else {
                recomendacoes.add("Conta dentro dos parâmetros normais");
                recomendacoes.add("Manter monitoramento padrão");
            }
            
            analise.put("recomendacoes", recomendacoes);
            
            return ResponseEntity.ok(analise);
            
        } catch (Exception e) {
            logger.error("❌ Erro na análise de IA: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                Map.of("error", "Erro na análise", "message", e.getMessage())
            );
        }
    }
    
    // =============================================
    // MÉTODOS AUXILIARES
    // =============================================
    
    private double calcularTaxaDeteccaoIA() {
        // Simula taxa de detecção baseada na configuração
        double baseRate = 92.0;
        double variation = Math.random() * 4.0; // Variação de até 4%
        return Math.min(98.0, baseRate + variation);
    }
    
    private double calcularTempoMedioAnalise() {
        // Simula tempo médio em horas
        return 1.0 + (Math.random() * 0.8); // Entre 1.0 e 1.8 horas
    }
    
    private int simularDenunciasPorHora(int hora) {
        // Simula padrão realista: mais denúncias durante horário comercial
        if (hora >= 8 && hora <= 18) {
            return 3 + (int)(Math.random() * 15); // 3-18 denúncias
        } else if (hora >= 19 && hora <= 22) {
            return 1 + (int)(Math.random() * 8); // 1-9 denúncias
        } else {
            return (int)(Math.random() * 4); // 0-3 denúncias
        }
    }
} 