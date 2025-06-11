package com.bradesco.pixmonitor.controller;

import com.bradesco.pixmonitor.model.Conta;
import com.bradesco.pixmonitor.model.ScoreConfianca;
import com.bradesco.pixmonitor.model.TransacaoPix;
import com.bradesco.pixmonitor.service.BancoDadosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pix")
@CrossOrigin(origins = "*")
public class PixController {

    private static final Logger logger = LoggerFactory.getLogger(PixController.class);

    @Autowired
    private BancoDadosService bancoDadosService;

    /**
     * Análise de PIX ANTES da transação ser realizada
     * Esta análise verifica o score e retorna se a transação deve ser bloqueada
     * LÓGICA: QUANTO MENOR O SCORE, MAIOR O RISCO!
     */
    @PostMapping("/analisar-pre-transacao")
    public ResponseEntity<Map<String, Object>> analisarPreTransacao(@RequestBody Map<String, Object> dadosTransacao) {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            logger.info("🔍 INICIANDO ANÁLISE PRÉ-TRANSAÇÃO PIX");
            
            // Extrair dados da requisição
            String chavePix = (String) dadosTransacao.get("chavePix");
            String valorStr = (String) dadosTransacao.get("valor");
            String descricao = (String) dadosTransacao.getOrDefault("descricao", "");
            String contaOrigemIdStr = (String) dadosTransacao.get("contaOrigemId");
            
            BigDecimal valor = new BigDecimal(valorStr);
            Long contaOrigemId = Long.parseLong(contaOrigemIdStr);
            
            logger.info("📋 Dados da transação: ChavePix={}, Valor=R${}, ContaOrigem={}", 
                       chavePix, valor, contaOrigemId);
            
            // Buscar conta origem
            Optional<Conta> contaOrigemOpt = bancoDadosService.buscarContaPorId(contaOrigemId);
            if (contaOrigemOpt.isEmpty()) {
                resultado.put("erro", "Conta origem não encontrada");
                return ResponseEntity.badRequest().body(resultado);
            }
            
            Conta contaOrigem = contaOrigemOpt.get();
            
            // =============================================
            // ANÁLISE CRÍTICA DO SCORE (ANTES DA TRANSAÇÃO!)
            // =============================================
            
            // Buscar score da conta origem
            Optional<ScoreConfianca> scoreOpt = bancoDadosService.buscarScorePorCliente(contaOrigem.getCliente().getId());
            
            Integer scoreAtual;
            if (scoreOpt.isEmpty()) {
                // Se não tem score, criar um score inicial médio
                scoreAtual = 70; // Score médio inicial
                logger.info("⚠️ Cliente sem score definido. Usando score inicial: {}", scoreAtual);
            } else {
                scoreAtual = scoreOpt.get().getScore();
            }
            
            // ⚠️ IMPORTANTE: QUANTO MENOR O SCORE, MAIOR O RISCO!
            final int SCORE_CRITICO = 30;    // ≤ 30 = BLOQUEIO IMEDIATO
            final int SCORE_ALTO = 50;       // ≤ 50 = TRANSAÇÃO SUSPEITA  
            final int SCORE_MEDIO = 70;      // ≤ 70 = MONITORAMENTO ESPECIAL
            
            String nivelRisco = determinarNivelRisco(scoreAtual);
            boolean transacaoBloqueada = false;
            boolean transacaoSuspeita = false;
            boolean requerAprovacao = false;
            String motivoBloqueio = "";
            
            logger.info("📊 ANÁLISE DE SCORE DETALHADA:");
            logger.info("   👤 Cliente: {}", contaOrigem.getCliente().getNomeCompleto());
            logger.info("   🔢 Score atual: {}/100", scoreAtual);
            logger.info("   ⚡ Nível de risco: {}", nivelRisco);
            
            // =============================================
            // 🚫 RISCO CRÍTICO (Score ≤ 30) - BLOQUEAR
            // =============================================
            if (scoreAtual <= SCORE_CRITICO) {
                transacaoBloqueada = true;
                motivoBloqueio = String.format(
                    "SCORE CRÍTICO DETECTADO: %d/100 - ALTO RISCO DE FRAUDE", scoreAtual);
                
                logger.error("🚫 TRANSAÇÃO BLOQUEADA - RISCO CRÍTICO!");
                logger.error("   💥 Score crítico: {}/100", scoreAtual);
                logger.error("   💰 Valor bloqueado: R$ {}", valor);
                
                resultado.put("acao", "BLOQUEAR");
                resultado.put("permitirTransacao", false);
                
            // =============================================
            // ⚠️ RISCO ALTO (Score ≤ 50) - MARCAR COMO SUSPEITA
            // =============================================    
            } else if (scoreAtual <= SCORE_ALTO) {
                transacaoSuspeita = true;
                motivoBloqueio = String.format(
                    "SCORE BAIXO DETECTADO: %d/100 - TRANSAÇÃO SUSPEITA", scoreAtual);
                
                logger.warn("⚠️ TRANSAÇÃO SUSPEITA - RISCO ALTO!");
                logger.warn("   📉 Score baixo: {}/100", scoreAtual);
                logger.warn("   🎯 Valor suspeito: R$ {}", valor);
                
                resultado.put("acao", "SUSPENDER");
                resultado.put("permitirTransacao", false);
                
            // =============================================
            // 📊 RISCO MÉDIO (Score ≤ 70) - MONITORAMENTO ESPECIAL
            // =============================================
            } else if (scoreAtual <= SCORE_MEDIO) {
                requerAprovacao = true;
                
                logger.info("📊 TRANSAÇÃO MONITORADA - RISCO MÉDIO");
                logger.info("   📈 Score médio: {}/100", scoreAtual);
                logger.info("   🔍 Requer monitoramento especial");
                
                // Para valores muito altos com score médio, requerer aprovação
                if (valor.compareTo(new BigDecimal("1000")) > 0) {
                    resultado.put("acao", "APROVAR_MANUAL");
                    resultado.put("permitirTransacao", false);
                } else {
                    resultado.put("acao", "MONITORAR");
                    resultado.put("permitirTransacao", true);
                }
                
            // =============================================
            // ✅ RISCO BAIXO (Score > 70) - APROVAR
            // =============================================
            } else {
                logger.info("✅ TRANSAÇÃO APROVADA - BAIXO RISCO!");
                logger.info("   📈 Score excelente: {}/100", scoreAtual);
                
                resultado.put("acao", "APROVAR");
                resultado.put("permitirTransacao", true);
            }
            
            // =============================================
            // MONTAR RESPOSTA DETALHADA
            // =============================================
            
            resultado.put("sucesso", true);
            resultado.put("scoreAtual", scoreAtual);
            resultado.put("nivelRisco", nivelRisco);
            resultado.put("transacaoBloqueada", transacaoBloqueada);
            resultado.put("transacaoSuspeita", transacaoSuspeita);
            resultado.put("requerAprovacao", requerAprovacao);
            resultado.put("motivoBloqueio", motivoBloqueio);
            
            // Informações para a interface
            resultado.put("cliente", Map.of(
                "nome", contaOrigem.getCliente().getNomeCompleto(),
                "cpf", contaOrigem.getCliente().getCpf(),
                "conta", contaOrigem.getContaCompleta()
            ));
            
            resultado.put("transacao", Map.of(
                "valor", valor.toString(),
                "chavePix", chavePix,
                "descricao", descricao
            ));
            
            // Fatores de risco identificados
            resultado.put("fatoresRisco", gerarFatoresRisco(scoreAtual, valor));
            
            // Recomendações
            resultado.put("recomendacoes", gerarRecomendacoes(scoreAtual, valor, transacaoBloqueada));
            
            logger.info("✅ Análise pré-transação concluída:");
            logger.info("   🎯 Ação recomendada: {}", resultado.get("acao"));
            logger.info("   ✔️ Permitir transação: {}", resultado.get("permitirTransacao"));
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            logger.error("❌ Erro na análise pré-transação: {}", e.getMessage(), e);
            
            resultado.put("sucesso", false);
            resultado.put("erro", "Erro interno na análise: " + e.getMessage());
            resultado.put("acao", "BLOQUEAR");
            resultado.put("permitirTransacao", false);
            resultado.put("motivoBloqueio", "Erro técnico - transação bloqueada por segurança");
            
            return ResponseEntity.status(500).body(resultado);
        }
    }
    
    /**
     * Processa a transação PIX (apenas se passou na análise prévia)
     */
    @PostMapping("/processar")
    public ResponseEntity<Map<String, Object>> processarPix(@RequestBody Map<String, Object> dadosTransacao) {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            logger.info("💳 PROCESSANDO TRANSAÇÃO PIX");
            
            Long contaOrigemId = Long.parseLong((String) dadosTransacao.get("contaOrigemId"));
            Long contaDestinoId = Long.parseLong((String) dadosTransacao.get("contaDestinoId"));
            BigDecimal valor = new BigDecimal((String) dadosTransacao.get("valor"));
            String descricao = (String) dadosTransacao.getOrDefault("descricao", "");
            
            // Chamar o serviço que já tem toda a lógica implementada
            TransacaoPix transacao = bancoDadosService.realizarTransacaoPix(
                contaOrigemId, contaDestinoId, valor, descricao);
            
            resultado.put("sucesso", true);
            resultado.put("transacaoId", transacao.getId());
            resultado.put("status", transacao.getStatusTransacao().toString());
            resultado.put("valor", transacao.getValor().toString());
            resultado.put("dataHora", transacao.getDataTransacao());
            resultado.put("score", transacao.getScoreConfianca());
            
            if (transacao.getStatusTransacao() == TransacaoPix.StatusTransacao.BLOQUEADA) {
                resultado.put("motivo", "Transação bloqueada por score de risco crítico");
                
                // Buscar protocolo da denúncia automática criada
                try {
                    String protocoloDenuncia = bancoDadosService.obterProtocoloDenunciaAutomatica(transacao.getId());
                    if (protocoloDenuncia != null) {
                        resultado.put("protocoloDenuncia", protocoloDenuncia);
                        logger.info("📋 Protocolo da denúncia automática: {}", protocoloDenuncia);
                    }
                } catch (Exception e) {
                    logger.warn("⚠️ Não foi possível obter protocolo da denúncia: {}", e.getMessage());
                }
                
            } else if (transacao.getStatusTransacao() == TransacaoPix.StatusTransacao.SUSPEITA) {
                resultado.put("motivo", "Transação marcada como suspeita para análise");
            }
            
            logger.info("💳 Transação processada: ID={}, Status={}", 
                       transacao.getId(), transacao.getStatusTransacao());
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao processar PIX: {}", e.getMessage(), e);
            
            resultado.put("sucesso", false);
            resultado.put("erro", "Erro ao processar transação: " + e.getMessage());
            
            return ResponseEntity.status(500).body(resultado);
        }
    }
    
    // =============================================
    // MÉTODOS AUXILIARES
    // =============================================
    
    private String determinarNivelRisco(Integer score) {
        if (score <= 30) return "🔴 CRÍTICO";
        if (score <= 50) return "🟠 ALTO";
        if (score <= 70) return "🟡 MÉDIO";
        return "🟢 BAIXO";
    }
    
    private java.util.List<String> gerarFatoresRisco(Integer score, BigDecimal valor) {
        java.util.List<String> fatores = new java.util.ArrayList<>();
        
        if (score <= 30) {
            fatores.add("Score extremamente baixo (" + score + "/100)");
            fatores.add("Alto histórico de denúncias");
            fatores.add("Comportamento suspeito detectado");
        } else if (score <= 50) {
            fatores.add("Score baixo (" + score + "/100)");
            fatores.add("Presença de denúncias recentes");
        } else if (score <= 70) {
            fatores.add("Score médio (" + score + "/100)");
            fatores.add("Conta em monitoramento");
        }
        
        if (valor.compareTo(new BigDecimal("1000")) > 0) {
            fatores.add("Valor elevado da transação (R$ " + valor + ")");
        }
        
        if (fatores.isEmpty()) {
            fatores.add("Nenhum fator de risco identificado");
        }
        
        return fatores;
    }
    
    private java.util.List<String> gerarRecomendacoes(Integer score, BigDecimal valor, boolean bloqueada) {
        java.util.List<String> recomendacoes = new java.util.ArrayList<>();
        
        if (bloqueada) {
            recomendacoes.add("🚫 Transação BLOQUEADA por segurança");
            recomendacoes.add("📞 Entre em contato com a central de atendimento");
            recomendacoes.add("🔍 Análise manual necessária");
        } else if (score <= 50) {
            recomendacoes.add("⚠️ Transação em análise pela equipe de segurança");
            recomendacoes.add("⏱️ Processamento pode demorar até 24h");
        } else if (score <= 70) {
            recomendacoes.add("📊 Transação em monitoramento especial");
            recomendacoes.add("✅ Processamento normal esperado");
        } else {
            recomendacoes.add("✅ Transação aprovada - baixo risco");
            recomendacoes.add("⚡ Processamento imediato");
        }
        
        return recomendacoes;
    }
} 