package com.bradesco.pixmonitor.controller;

import com.bradesco.pixmonitor.model.*;
import com.bradesco.pixmonitor.service.BancoDadosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/bd")
@CrossOrigin(origins = "*")
public class BancoDadosController {
    
    @Autowired
    private BancoDadosService bancoDadosService;
    
    // =============================================
    // ENDPOINTS DE CLIENTES
    // =============================================
    
    @PostMapping("/clientes")
    public ResponseEntity<?> criarCliente(@RequestBody Map<String, String> dados) {
        try {
            Cliente cliente = bancoDadosService.criarCliente(
                dados.get("nome"),
                dados.get("cpf"),
                dados.get("email")
            );
            return ResponseEntity.ok(cliente);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @GetMapping("/clientes/cpf/{cpf}")
    public ResponseEntity<?> buscarClientePorCpf(@PathVariable String cpf) {
        Optional<Cliente> cliente = bancoDadosService.buscarClientePorCpf(cpf);
        if (cliente.isPresent()) {
            return ResponseEntity.ok(cliente.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/clientes/risco-alto")
    public ResponseEntity<List<Cliente>> listarClientesRiscoAlto() {
        return ResponseEntity.ok(bancoDadosService.listarClientesRiscoAlto());
    }
    
    // =============================================
    // ENDPOINTS DE CONTAS
    // =============================================
    
    @PostMapping("/contas")
    public ResponseEntity<?> criarConta(@RequestBody Map<String, Object> dados) {
        try {
            Conta conta = bancoDadosService.criarConta(
                Long.parseLong(dados.get("clienteId").toString()),
                dados.get("numeroConta").toString(),
                dados.get("agencia").toString(),
                Conta.TipoConta.valueOf(dados.get("tipoConta").toString().toUpperCase()),
                dados.get("saldoInicial") != null ? 
                    new BigDecimal(dados.get("saldoInicial").toString()) : BigDecimal.ZERO
            );
            return ResponseEntity.ok(conta);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @GetMapping("/contas/{numeroConta}/{agencia}")
    public ResponseEntity<?> buscarContaPorNumeroEAgencia(@PathVariable String numeroConta, 
                                                         @PathVariable String agencia) {
        Optional<Conta> conta = bancoDadosService.buscarContaPorNumeroEAgencia(numeroConta, agencia);
        if (conta.isPresent()) {
            return ResponseEntity.ok(conta.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/contas/cliente/{clienteId}")
    public ResponseEntity<List<Conta>> buscarContasPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(bancoDadosService.buscarContasPorCliente(clienteId));
    }
    
    // =============================================
    // ENDPOINTS DE TRANSAÇÕES PIX
    // =============================================
    
    @PostMapping("/transacoes")
    public ResponseEntity<?> realizarTransacaoPix(@RequestBody Map<String, Object> dados) {
        try {
            TransacaoPix transacao = bancoDadosService.realizarTransacaoPix(
                Long.parseLong(dados.get("contaOrigemId").toString()),
                Long.parseLong(dados.get("contaDestinoId").toString()),
                new BigDecimal(dados.get("valor").toString()),
                dados.get("descricao").toString()
            );
            
            return ResponseEntity.ok(Map.of(
                "transacao", transacao,
                "status", transacao.getStatusTransacao(),
                "score", transacao.getScoreConfianca(),
                "aprovada", !transacao.isBloqueada()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @GetMapping("/transacoes/suspeitas")
    public ResponseEntity<List<TransacaoPix>> buscarTransacoesSuspeitas() {
        return ResponseEntity.ok(bancoDadosService.buscarTransacoesSuspeitas());
    }
    
    @GetMapping("/transacoes/alto-valor")
    public ResponseEntity<List<TransacaoPix>> buscarTransacoesAltoValor(
            @RequestParam(defaultValue = "10000") BigDecimal valorMinimo) {
        return ResponseEntity.ok(bancoDadosService.buscarTransacoesAltoValor(valorMinimo));
    }
    
    // =============================================
    // ENDPOINTS DE DENÚNCIAS
    // =============================================
    
    @PostMapping("/denuncias")
    public ResponseEntity<?> criarDenuncia(@RequestBody Map<String, Object> dados) {
        try {
            Denuncia denuncia = bancoDadosService.criarDenuncia(
                Long.parseLong(dados.get("contaDenunciadaId").toString()),
                dados.get("motivo").toString(),
                dados.get("transacaoId") != null ? 
                    Long.parseLong(dados.get("transacaoId").toString()) : null
            );
            return ResponseEntity.ok(denuncia);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @GetMapping("/denuncias/pendentes")
    public ResponseEntity<List<Denuncia>> listarDenunciasPendentes() {
        return ResponseEntity.ok(bancoDadosService.listarDenunciasPendentes());
    }
    
    @PutMapping("/denuncias/{denunciaId}/status")
    public ResponseEntity<?> atualizarStatusDenuncia(@PathVariable Long denunciaId, 
                                                    @RequestBody Map<String, String> dados) {
        try {
            Denuncia denuncia = bancoDadosService.atualizarStatusDenuncia(
                denunciaId,
                Denuncia.StatusDenuncia.valueOf(dados.get("status").toUpperCase()),
                dados.get("funcionario"),
                dados.get("observacoes")
            );
            return ResponseEntity.ok(denuncia);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    // =============================================
    // ENDPOINTS DE SCORES
    // =============================================
    
    @GetMapping("/scores/cliente/{clienteId}")
    public ResponseEntity<?> buscarScorePorCliente(@PathVariable Long clienteId) {
        Optional<ScoreConfianca> score = bancoDadosService.buscarScorePorCliente(clienteId);
        if (score.isPresent()) {
            return ResponseEntity.ok(score.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/scores/bloqueadas")
    public ResponseEntity<List<ScoreConfianca>> listarContasBloqueadas() {
        return ResponseEntity.ok(bancoDadosService.listarContasBloqueadas());
    }
    
    @GetMapping("/scores/risco-alto")
    public ResponseEntity<List<ScoreConfianca>> listarScoresRiscoAlto() {
        return ResponseEntity.ok(bancoDadosService.listarScoresRiscoAlto());
    }
    
    // =============================================
    // ENDPOINTS DE RELATÓRIOS
    // =============================================
    
    @GetMapping("/relatorios/completo")
    public ResponseEntity<Map<String, Object>> gerarRelatorioCompleto() {
        return ResponseEntity.ok(bancoDadosService.gerarRelatorioCompleto());
    }
    
    @GetMapping("/relatorios/top-denuncias")
    public ResponseEntity<List<Object[]>> obterTopContasComMaisDenuncias() {
        return ResponseEntity.ok(bancoDadosService.obterTopContasComMaisDenuncias());
    }
    
    // =============================================
    // ENDPOINTS DE OPERAÇÕES DE SISTEMA
    // =============================================
    
    @PostMapping("/sistema/recalcular-scores")
    public ResponseEntity<Map<String, String>> recalcularTodosScores() {
        try {
            bancoDadosService.recalcularTodosScores();
            return ResponseEntity.ok(Map.of("status", "Scores recalculados com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", e.getMessage()));
        }
    }
    
    // =============================================
    // ENDPOINTS DE COMPATIBILIDADE
    // =============================================
    
    @GetMapping("/denuncias/conta/{idConta}")
    public ResponseEntity<List<Denuncia>> buscarDenunciasPorIdConta(@PathVariable String idConta) {
        return ResponseEntity.ok(bancoDadosService.buscarDenunciasPorIdConta(idConta));
    }
    
    @GetMapping("/denuncias/{idDenuncia}")
    public ResponseEntity<?> buscarDenunciaPorId(@PathVariable String idDenuncia) {
        Optional<Denuncia> denuncia = bancoDadosService.buscarDenunciaPorId(idDenuncia);
        if (denuncia.isPresent()) {
            return ResponseEntity.ok(denuncia.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // =============================================
    // ENDPOINT DE TESTE DA BASE DE DADOS
    // =============================================
    
    @PostMapping("/teste/popular-dados")
    public ResponseEntity<Map<String, Object>> popularDadosTeste() {
        try {
            // Cria clientes de teste
            Cliente cliente1 = bancoDadosService.criarCliente("João Silva", "12345678901", "joao@teste.com");
            Cliente cliente2 = bancoDadosService.criarCliente("Maria Santos", "98765432100", "maria@teste.com");
            
            // Cria contas de teste
            Conta conta1 = bancoDadosService.criarConta(cliente1.getId(), "123456-7", "1234", 
                    Conta.TipoConta.CORRENTE, new BigDecimal("5000.00"));
            Conta conta2 = bancoDadosService.criarConta(cliente2.getId(), "654321-8", "1235", 
                    Conta.TipoConta.CORRENTE, new BigDecimal("3000.00"));
            
            // Realiza transação de teste
            TransacaoPix transacao = bancoDadosService.realizarTransacaoPix(
                    conta1.getId(), conta2.getId(), new BigDecimal("500.00"), "Teste PIX");
            
            // Cria denúncia de teste
            Denuncia denuncia = bancoDadosService.criarDenuncia(
                    conta2.getId(), "Transação suspeita - teste", transacao.getId());
            
            return ResponseEntity.ok(Map.of(
                "status", "Dados de teste criados com sucesso",
                "clientes", List.of(cliente1, cliente2),
                "contas", List.of(conta1, conta2),
                "transacao", transacao,
                "denuncia", denuncia
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> verificarStatus() {
        Map<String, Object> relatorio = bancoDadosService.gerarRelatorioCompleto();
        return ResponseEntity.ok(Map.of(
            "status", "Base de dados operacional",
            "timestamp", System.currentTimeMillis(),
            "resumo", relatorio
        ));
    }
    
    // =============================================
    // ENDPOINT PARA TESTE DA ANÁLISE DE SCORE
    // =============================================
    
    @PostMapping("/transacoes/teste-score")
    public ResponseEntity<?> testarAnaliseScore(@RequestBody Map<String, Object> dados) {
        try {
            Long contaOrigemId = Long.parseLong(dados.get("contaOrigemId").toString());
            Long contaDestinoId = Long.parseLong(dados.get("contaDestinoId").toString());
            BigDecimal valor = new BigDecimal(dados.get("valor").toString());
            String descricao = dados.get("descricao").toString();
            
            // Buscar scores antes da transação
            Conta contaOrigem = bancoDadosService.buscarContaPorId(contaOrigemId)
                    .orElseThrow(() -> new RuntimeException("Conta origem não encontrada"));
            Conta contaDestino = bancoDadosService.buscarContaPorId(contaDestinoId)
                    .orElseThrow(() -> new RuntimeException("Conta destino não encontrada"));
            
            Optional<ScoreConfianca> scoreOrigemOpt = bancoDadosService.buscarScorePorCliente(contaOrigem.getCliente().getId());
            Optional<ScoreConfianca> scoreDestinoOpt = bancoDadosService.buscarScorePorCliente(contaDestino.getCliente().getId());
            
            ScoreConfianca scoreOrigem = scoreOrigemOpt.orElse(new ScoreConfianca(contaOrigem.getCliente()));
            ScoreConfianca scoreDestino = scoreDestinoOpt.orElse(new ScoreConfianca(contaDestino.getCliente()));
            
            // Realizar a transação com análise de score
            TransacaoPix transacao = bancoDadosService.realizarTransacaoPix(
                    contaOrigemId, contaDestinoId, valor, descricao);
            
            // Verificar se foi criada denúncia automática
            List<Denuncia> denunciasAutomaticas = bancoDadosService.buscarDenunciasPorTransacao(transacao);
            
            return ResponseEntity.ok(Map.of(
                "transacao", Map.of(
                    "id", transacao.getId(),
                    "valor", transacao.getValor(),
                    "status", transacao.getStatusTransacao(),
                    "scoreTransacao", transacao.getScoreConfianca(),
                    "dataTransacao", transacao.getDataTransacao()
                ),
                "analise", Map.of(
                    "scoreOrigem", scoreOrigem.getScore(),
                    "scoreDestino", scoreDestino.getScore(),
                    "clienteOrigem", contaOrigem.getCliente().getNomeCompleto(),
                    "clienteDestino", contaDestino.getCliente().getNomeCompleto(),
                    "risco", determinarNivelRisco(scoreOrigem.getScore()),
                    "aprovada", transacao.getStatusTransacao() == TransacaoPix.StatusTransacao.CONCLUIDA,
                    "bloqueada", transacao.getStatusTransacao() == TransacaoPix.StatusTransacao.BLOQUEADA,
                    "suspeita", transacao.getStatusTransacao() == TransacaoPix.StatusTransacao.SUSPEITA
                ),
                "denunciasAutomaticas", denunciasAutomaticas.stream()
                    .map(d -> Map.of(
                        "protocolo", d.getProtocolo(),
                        "tipo", d.getTipoDenuncia(),
                        "prioridade", d.getPrioridade(),
                        "motivo", d.getMotivo(),
                        "status", d.getStatusDenuncia()
                    )).toList(),
                "recomendacao", gerarRecomendacao(scoreOrigem.getScore(), transacao.getStatusTransacao())
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "erro", e.getMessage(),
                "detalhe", "Erro ao processar transação com análise de score"
            ));
        }
    }
    
    private String determinarNivelRisco(Integer score) {
        if (score < 30) return "CRÍTICO";
        if (score < 50) return "ALTO";
        if (score < 70) return "MÉDIO";
        return "BAIXO";
    }
    
    private String gerarRecomendacao(Integer score, TransacaoPix.StatusTransacao status) {
        switch (status) {
            case BLOQUEADA:
                return "Transação bloqueada por score crítico (" + score + "). Contate o cliente para verificação manual.";
            case SUSPEITA:
                return "Transação marcada como suspeita devido ao score baixo (" + score + "). Monitoramento especial ativado.";
            case CONCLUIDA:
                if (score < 70) {
                    return "Transação aprovada com monitoramento especial. Score: " + score + ". Acompanhar próximas transações.";
                } else {
                    return "Transação aprovada. Cliente com bom score (" + score + "). Operação normal.";
                }
            default:
                return "Status não reconhecido para análise.";
        }
    }
} 