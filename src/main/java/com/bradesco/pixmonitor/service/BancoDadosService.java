package com.bradesco.pixmonitor.service;

import com.bradesco.pixmonitor.model.*;
import com.bradesco.pixmonitor.repository.*;
import com.bradesco.pixmonitor.config.BradescoPixConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class BancoDadosService {
    
    private static final Logger logger = LoggerFactory.getLogger(BancoDadosService.class);
    
    @Autowired
    private ClienteRepository clienteRepository;
    
    @Autowired
    private ContaRepository contaRepository;
    
    @Autowired
    private TransacaoPixRepository transacaoRepository;
    
    @Autowired
    private DenunciaRepository denunciaRepository;
    
    @Autowired
    private ScoreConfiancaRepository scoreRepository;
    
    @Autowired
    private PixSuspicionPredictor pixSuspicionPredictor;
    
    @Autowired
    private BradescoPixConfig config;
    
    // =============================================
    // OPERAÇÕES DE CLIENTES
    // =============================================
    
    public Cliente criarCliente(String nome, String cpf, String email) {
        logger.info("Criando cliente: nome={}, cpf={}, email={}", nome, cpf, email);
        
        // Valida se CPF e email já existem
        if (clienteRepository.existsByCpf(cpf)) {
            throw new RuntimeException("Cliente com CPF " + cpf + " já existe");
        }
        
        if (clienteRepository.existsByEmail(email)) {
            throw new RuntimeException("Cliente com email " + email + " já existe");
        }
        
        Cliente cliente = new Cliente(nome, cpf, email);
        cliente = clienteRepository.save(cliente);
        
        // Cria score inicial
        ScoreConfianca score = new ScoreConfianca(cliente);
        scoreRepository.save(score);
        
        logger.info("Cliente criado com sucesso: {}", cliente);
        return cliente;
    }
    
    public Optional<Cliente> buscarClientePorCpf(String cpf) {
        return clienteRepository.findByCpf(cpf);
    }
    
    public List<Cliente> listarClientesRiscoAlto() {
        return clienteRepository.findClientesRiscoAlto();
    }
    
    // =============================================
    // OPERAÇÕES DE CONTAS
    // =============================================
    
    public Conta criarConta(Long clienteId, String numeroConta, String agencia, 
                           Conta.TipoConta tipoConta, BigDecimal saldoInicial) {
        logger.info("Criando conta: clienteId={}, conta={}, agencia={}", clienteId, numeroConta, agencia);
        
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado: " + clienteId));
        
        if (contaRepository.existsByNumeroContaAndAgencia(numeroConta, agencia)) {
            throw new RuntimeException("Conta já existe: " + numeroConta + "-" + agencia);
        }
        
        Conta conta = new Conta(cliente, numeroConta, agencia, tipoConta);
        conta.setSaldo(saldoInicial != null ? saldoInicial : BigDecimal.ZERO);
        
        conta = contaRepository.save(conta);
        logger.info("Conta criada com sucesso: {}", conta);
        return conta;
    }
    
    public Optional<Conta> buscarContaPorNumeroEAgencia(String numeroConta, String agencia) {
        return contaRepository.findByNumeroContaAndAgencia(numeroConta, agencia);
    }
    
    public List<Conta> buscarContasPorCliente(Long clienteId) {
        return contaRepository.findByClienteId(clienteId);
    }
    
    // =============================================
    // OPERAÇÕES DE TRANSAÇÕES PIX
    // =============================================
    
    public TransacaoPix realizarTransacaoPix(Long contaOrigemId, Long contaDestinoId, 
                                           BigDecimal valor, String descricao) {
        logger.info("🔄 INICIANDO TRANSAÇÃO PIX: origem={}, destino={}, valor=R${}", 
                   contaOrigemId, contaDestinoId, valor);
        
        Conta contaOrigem = contaRepository.findById(contaOrigemId)
                .orElseThrow(() -> new RuntimeException("Conta origem não encontrada: " + contaOrigemId));
        
        Conta contaDestino = contaRepository.findById(contaDestinoId)
                .orElseThrow(() -> new RuntimeException("Conta destino não encontrada: " + contaDestinoId));
        
        // =============================================
        // ANÁLISE DE SCORE ANTES DA TRANSAÇÃO (CRÍTICO!)
        // =============================================
        
        // Buscar ou criar score da conta origem
        Optional<ScoreConfianca> scoreOrigemOpt = scoreRepository.findByClienteId(contaOrigem.getCliente().getId());
        ScoreConfianca scoreOrigem;
        if (scoreOrigemOpt.isEmpty()) {
            scoreOrigem = new ScoreConfianca(contaOrigem.getCliente());
            scoreOrigem = scoreRepository.save(scoreOrigem);
            logger.info("🆕 Novo score criado para cliente origem: {}", contaOrigem.getCliente().getNomeCompleto());
        } else {
            scoreOrigem = scoreOrigemOpt.get();
        }
        
        // Buscar ou criar score da conta destino
        Optional<ScoreConfianca> scoreDestinoOpt = scoreRepository.findByClienteId(contaDestino.getCliente().getId());
        ScoreConfianca scoreDestino;
        if (scoreDestinoOpt.isEmpty()) {
            scoreDestino = new ScoreConfianca(contaDestino.getCliente());
            scoreDestino = scoreRepository.save(scoreDestino);
            logger.info("🆕 Novo score criado para cliente destino: {}", contaDestino.getCliente().getNomeCompleto());
        } else {
            scoreDestino = scoreDestinoOpt.get();
        }
        
        // ⚠️ IMPORTANTE: QUANTO MENOR O SCORE, MAIOR O RISCO!
        // Score 0-30 = RISCO CRÍTICO (BLOQUEAR)
        // Score 31-50 = RISCO ALTO (SUSPEITA + DENÚNCIA)  
        // Score 51-70 = RISCO MÉDIO (MONITORAMENTO)
        // Score 71-100 = RISCO BAIXO (APROVADO)
        
        final int SCORE_CRITICO = 30;    // ≤ 30 = BLOQUEIO IMEDIATO
        final int SCORE_ALTO = 50;       // ≤ 50 = TRANSAÇÃO SUSPEITA  
        final int SCORE_MEDIO = 70;      // ≤ 70 = MONITORAMENTO ESPECIAL
        
        Integer scoreOrigemValue = scoreOrigem.getScore();
        Integer scoreDestinoValue = scoreDestino.getScore();
        
        logger.info("📊 ANÁLISE DE SCORE:");
        logger.info("   👤 Origem: {} (Score: {}/100) - Risco: {}", 
                   contaOrigem.getCliente().getNomeCompleto(), 
                   scoreOrigemValue, 
                   determinarNivelRisco(scoreOrigemValue));
        logger.info("   👤 Destino: {} (Score: {}/100) - Risco: {}", 
                   contaDestino.getCliente().getNomeCompleto(), 
                   scoreDestinoValue, 
                   determinarNivelRisco(scoreDestinoValue));
        
        TransacaoPix transacao = new TransacaoPix(contaOrigem, contaDestino, valor, descricao);
        
        // =============================================
        // 🚫 VERIFICAÇÃO DE RISCO CRÍTICO (Score ≤ 30)
        // =============================================
        if (scoreOrigemValue <= SCORE_CRITICO || scoreDestinoValue <= SCORE_CRITICO) {
            Integer menorScore = Math.min(scoreOrigemValue, scoreDestinoValue);
            String contaCritica = scoreOrigemValue <= SCORE_CRITICO ? "ORIGEM" : "DESTINO";
            
            logger.error("🚫 TRANSAÇÃO BLOQUEADA - RISCO CRÍTICO!");
            logger.error("   💥 Score crítico detectado: {} (Score: {})", contaCritica, menorScore);
            logger.error("   💰 Valor bloqueado: R$ {}", valor);
            
            transacao.setStatusTransacao(TransacaoPix.StatusTransacao.BLOQUEADA);
            transacao.setScoreConfianca(new BigDecimal(menorScore));
            transacao = transacaoRepository.save(transacao);
            
            // Criar denúncia automática URGENTE
            String motivoDenuncia = String.format(
                "🚨 BLOQUEIO AUTOMÁTICO - RISCO CRÍTICO 🚨\n\n" +
                "Score extremamente baixo detectado na conta %s: %d/100\n" +
                "Transação PIX de R$ %.2f BLOQUEADA automaticamente\n" +
                "Cliente: %s\n" +
                "Conta: %s-%s\n\n" +
                "⚠️ REQUER VERIFICAÇÃO MANUAL IMEDIATA\n" +
                "Sistema de IA classificou como FRAUDE POTENCIAL",
                contaCritica, menorScore, valor,
                scoreOrigemValue <= SCORE_CRITICO ? contaOrigem.getCliente().getNomeCompleto() : contaDestino.getCliente().getNomeCompleto(),
                scoreOrigemValue <= SCORE_CRITICO ? contaOrigem.getAgencia() : contaDestino.getAgencia(),
                scoreOrigemValue <= SCORE_CRITICO ? contaOrigem.getNumeroConta() : contaDestino.getNumeroConta()
            );
            
            Denuncia denunciaAutomatica = criarDenuncia(
                scoreOrigemValue <= SCORE_CRITICO ? contaOrigemId : contaDestinoId, 
                motivoDenuncia, 
                transacao.getId()
            );
            denunciaAutomatica.setTipoDenuncia("SCORE_CRITICO_AUTOMATICO");
            denunciaAutomatica.setPrioridade("URGENTE");
            denunciaRepository.save(denunciaAutomatica);
            
            logger.error("📋 Denúncia automática criada: {}", denunciaAutomatica.getProtocolo());
            
            return transacao;
        }
        
        // =============================================
        // ⚠️ VERIFICAÇÃO DE RISCO ALTO (Score ≤ 50)
        // =============================================
        if (scoreOrigemValue <= SCORE_ALTO || scoreDestinoValue <= SCORE_ALTO) {
            Integer menorScore = Math.min(scoreOrigemValue, scoreDestinoValue);
            
            logger.warn("⚠️ TRANSAÇÃO SUSPEITA - RISCO ALTO!");
            logger.warn("   📉 Score baixo detectado: Origem={}, Destino={}", scoreOrigemValue, scoreDestinoValue);
            logger.warn("   🎯 Menor score: {}/100", menorScore);
            
            transacao.setStatusTransacao(TransacaoPix.StatusTransacao.SUSPEITA);
            transacao.setScoreConfianca(new BigDecimal(menorScore));
            transacao = transacaoRepository.save(transacao);
            
            // Criar denúncia automática para investigação
            String motivoDenuncia = String.format(
                "⚠️ RISCO ALTO DETECTADO - ANÁLISE DE IA ⚠️\n\n" +
                "Score baixo identificado:\n" +
                "• Conta Origem: %s (Score: %d/100)\n" +
                "• Conta Destino: %s (Score: %d/100)\n" +
                "• Valor da Transação: R$ %.2f\n\n" +
                "Transação marcada como SUSPEITA para investigação.\n" +
                "Padrão de risco identificado pelo sistema de IA.\n\n" +
                "🔍 INVESTIGAR IMEDIATAMENTE",
                contaOrigem.getCliente().getNomeCompleto(), scoreOrigemValue,
                contaDestino.getCliente().getNomeCompleto(), scoreDestinoValue,
                valor
            );
            
            Denuncia denunciaAutomatica = criarDenuncia(contaOrigemId, motivoDenuncia, transacao.getId());
            denunciaAutomatica.setTipoDenuncia("ALTO_RISCO_AUTOMATICO");
            denunciaAutomatica.setPrioridade("ALTA");
            denunciaRepository.save(denunciaAutomatica);
            
            logger.warn("📋 Denúncia automática criada: {}", denunciaAutomatica.getProtocolo());
            
            return transacao;
        }
        
        // =============================================
        // 📊 VERIFICAÇÃO DE RISCO MÉDIO (Score ≤ 70)
        // =============================================
        if (scoreOrigemValue <= SCORE_MEDIO || scoreDestinoValue <= SCORE_MEDIO) {
            logger.info("📊 TRANSAÇÃO MONITORADA - RISCO MÉDIO");
            logger.info("   📈 Score médio: Origem={}, Destino={}", scoreOrigemValue, scoreDestinoValue);
            
            // Análise adicional com IA para risco médio
            Map<String, Object> analiseIA = analisarRiscoTransacao(contaOrigem, scoreOrigemValue);
            
            boolean transacaoSuspensa = (Boolean) analiseIA.getOrDefault("transacaoSuspensa", false);
            
            if (transacaoSuspensa) {
                transacao.setStatusTransacao(TransacaoPix.StatusTransacao.SUSPEITA);
                logger.warn("🤖 IA recomendou suspensão da transação");
            } else {
                transacao.setStatusTransacao(TransacaoPix.StatusTransacao.CONCLUIDA);
                logger.info("🤖 IA aprovou a transação com monitoramento");
            }
            
            transacao.setScoreConfianca(new BigDecimal(scoreOrigemValue));
            transacao = transacaoRepository.save(transacao);
            
            logger.info("✅ Transação processada com monitoramento especial. Score: {}, Análise IA: {}", 
                       scoreOrigemValue, analiseIA.get("recomendacao"));
            
            return transacao;
        }
        
        // =============================================
        // ✅ TRANSAÇÃO APROVADA (Score > 70)
        // =============================================
        logger.info("✅ TRANSAÇÃO APROVADA - BAIXO RISCO!");
        logger.info("   📈 Scores excelentes: Origem={}, Destino={}", scoreOrigemValue, scoreDestinoValue);
        
        transacao.setStatusTransacao(TransacaoPix.StatusTransacao.CONCLUIDA);
        transacao.setScoreConfianca(new BigDecimal(scoreOrigemValue));
        transacao = transacaoRepository.save(transacao);
        
        logger.info("✅ Transação PIX realizada com sucesso: ID={}, Valor=R${}, Status={}", 
                   transacao.getId(), valor, transacao.getStatusTransacao());
        
        return transacao;
    }
    
    private Map<String, Object> analisarRiscoTransacao(Conta conta, Integer scoreAtual) {
        // Busca dados históricos da conta
        Long totalDenuncias = (long) denunciaRepository.findByContaDenunciada(conta).size();
        LocalDateTime dataLimite = LocalDateTime.now().minusDays(config.getTransacao().getDiasAnaliseHistorico());
        Long denunciasRecentes = denunciaRepository.findByContaDenunciada(conta).stream()
                .filter(d -> d.getDataDenuncia().isAfter(dataLimite))
                .count();
        
        List<TransacaoPix> transacoesRecentes = transacaoRepository.findByDataTransacaoBetween(
                dataLimite, LocalDateTime.now()).stream()
                .filter(t -> t.getContaOrigem().equals(conta) || t.getContaDestino().equals(conta))
                .toList();
        
        // Calcula métricas para IA
        int quantidadeDenuncias = totalDenuncias.intValue();
        int tempoEntreDenuncias = denunciasRecentes > 0 ? 
            config.getTransacao().getTempoEntreDenunciasSuspeito() : 
            config.getTransacao().getTempoEntreDenunciasSeguro();
        double frequenciaDenuncias = denunciasRecentes.doubleValue() / (double) config.getTransacao().getDiasAnaliseHistorico();
        int quantidadeRecebimentos = transacoesRecentes.size();
        double valorTotalRecebido = transacoesRecentes.stream()
                .mapToDouble(t -> t.getValor().doubleValue())
                .sum();
        int tempoDesdeCriacao = (int) java.time.temporal.ChronoUnit.DAYS.between(
                conta.getDataAbertura().toLocalDate(), LocalDateTime.now().toLocalDate());
        
        // Chama IA para análise
        Map<String, Object> resultado = pixSuspicionPredictor.analisarConta(
                quantidadeDenuncias, tempoEntreDenuncias, frequenciaDenuncias,
                quantidadeRecebimentos, valorTotalRecebido, tempoDesdeCriacao);
        
        // Considera o score atual na decisão final
        Boolean transacaoSuspensa = (Boolean) resultado.get("transacaoSuspensa");
        
        // Se o score é muito baixo, força suspensão independente da IA
        if (scoreAtual < 40) {
            resultado.put("transacaoSuspensa", true);
            resultado.put("motivoSuspensao", "Score muito baixo: " + scoreAtual);
        } else if (scoreAtual < 70 && transacaoSuspensa) {
            // Se score médio e IA detectou problema, mantém suspensão
            resultado.put("transacaoSuspensa", true);
            resultado.put("motivoSuspensao", "Score médio + IA detectou risco");
        }
        
        // Usa o score atual como base se não tiver score calculado pela IA
        if (!resultado.containsKey("scoreSeguranca")) {
            resultado.put("scoreSeguranca", scoreAtual);
        }
        
        return resultado;
    }
    
    public List<TransacaoPix> buscarTransacoesSuspeitas() {
        return transacaoRepository.findTransacoesSuspeitas();
    }
    
    // =============================================
    // OPERAÇÕES DE DENÚNCIAS
    // =============================================
    
    public Denuncia criarDenuncia(Long contaDenunciadaId, String motivo, Long transacaoId) {
        logger.info("📋 Criando denúncia: conta={}, motivo={}", contaDenunciadaId, motivo);
        
        Conta contaDenunciada = contaRepository.findById(contaDenunciadaId)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada: " + contaDenunciadaId));
        
        TransacaoPix transacao = null;
        if (transacaoId != null) {
            transacao = transacaoRepository.findById(transacaoId).orElse(null);
        }
        
        Denuncia denuncia = new Denuncia(transacao, contaDenunciada, motivo);
        denuncia = denunciaRepository.save(denuncia);
        
        // Reduzir score automaticamente quando há denúncia
        atualizarScoreAposDenuncia(contaDenunciada.getCliente().getId());
        
        logger.info("✅ Denúncia criada: {}", denuncia);
        return denuncia;
    }
    
    /**
     * Criar denúncia manual via interface web
     * @param dadosDenuncia Dados da denúncia manual
     * @return Denúncia criada
     */
    public Denuncia criarDenunciaManual(Map<String, Object> dadosDenuncia) {
        try {
            logger.info("📋 Criando denúncia MANUAL via interface web");
            
            // Extrair dados da denúncia
            String tipoTransacao = (String) dadosDenuncia.get("tipoTransacao");
            String contaId = (String) dadosDenuncia.get("contaId");
            String valorStr = (String) dadosDenuncia.get("valorTransacao");
            String motivo = (String) dadosDenuncia.get("motivoDenuncia");
            String evidencias = (String) dadosDenuncia.getOrDefault("evidencias", "");
            String denunciante = (String) dadosDenuncia.getOrDefault("denunciante", "Usuário Anônimo");
            
            // Validar dados obrigatórios
            if (contaId == null || contaId.trim().isEmpty()) {
                throw new IllegalArgumentException("ID da conta é obrigatório");
            }
            if (motivo == null || motivo.trim().isEmpty()) {
                throw new IllegalArgumentException("Motivo da denúncia é obrigatório");
            }
            
            // Buscar conta por ID
            Long contaDenunciadaId;
            try {
                contaDenunciadaId = Long.parseLong(contaId.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ID da conta deve ser um número válido");
            }
            
            Conta contaDenunciada = contaRepository.findById(contaDenunciadaId)
                    .orElseThrow(() -> new RuntimeException("Conta não encontrada: " + contaDenunciadaId));
            
            // Verificar score atual da conta
            Optional<ScoreConfianca> scoreOpt = scoreRepository.findByClienteId(contaDenunciada.getCliente().getId());
            Integer scoreAtual = scoreOpt.map(ScoreConfianca::getScore).orElse(100);
            
            // Criar denúncia com informações detalhadas
            String motivoCompleto = String.format(
                "🚨 DENÚNCIA MANUAL 🚨\n\n" +
                "Denunciante: %s\n" +
                "Tipo de Transação: %s\n" +
                "Valor: %s\n" +
                "Score Atual da Conta: %d/100\n\n" +
                "Motivo:\n%s\n\n" +
                "%s" +
                "Data da Denúncia: %s\n" +
                "Status: PENDENTE ANÁLISE",
                denunciante,
                tipoTransacao != null ? tipoTransacao : "Não especificado",
                valorStr != null ? "R$ " + valorStr : "Não especificado",
                scoreAtual,
                motivo,
                evidencias.isEmpty() ? "" : "Evidências:\n" + evidencias + "\n\n",
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
            );
            
            Denuncia denuncia = new Denuncia(contaDenunciada, motivoCompleto);
            denuncia.setTipoDenuncia("DENUNCIA_MANUAL");
            denuncia.setPrioridade(determinarPrioridadePorScore(scoreAtual));
            denuncia.setFuncionarioResponsavel("Sistema Web");
            denuncia.setEvidencias(evidencias);
            
            denuncia = denunciaRepository.save(denuncia);
            
            // Atualizar score após denúncia manual
            atualizarScoreAposDenuncia(contaDenunciada.getCliente().getId());
            
            logger.info("✅ Denúncia manual criada com sucesso:");
            logger.info("   📋 Protocolo: {}", denuncia.getProtocolo());
            logger.info("   👤 Cliente: {}", contaDenunciada.getCliente().getNomeCompleto());
            logger.info("   📊 Score atual: {}/100", scoreAtual);
            logger.info("   ⚡ Prioridade: {}", denuncia.getPrioridade());
            
            return denuncia;
            
        } catch (Exception e) {
            logger.error("❌ Erro ao criar denúncia manual: {}", e.getMessage());
            throw new RuntimeException("Erro ao processar denúncia: " + e.getMessage());
        }
    }
    
    /**
     * Determinar prioridade baseada no score
     */
    private String determinarPrioridadePorScore(Integer score) {
        if (score <= 30) return "URGENTE";
        if (score <= 50) return "ALTA";
        if (score <= 70) return "MEDIA";
        return "BAIXA";
    }
    
    /**
     * Atualizar score após denúncia (reduz o score)
     */
    private void atualizarScoreAposDenuncia(Long clienteId) {
        try {
            Optional<ScoreConfianca> scoreOpt = scoreRepository.findByClienteId(clienteId);
            ScoreConfianca score;
            
            if (scoreOpt.isEmpty()) {
                // Criar score inicial se não existir
                Cliente cliente = clienteRepository.findById(clienteId)
                        .orElseThrow(() -> new RuntimeException("Cliente não encontrado: " + clienteId));
                score = new ScoreConfianca(cliente);
            } else {
                score = scoreOpt.get();
            }
            
            // Reduzir score por denúncia (quanto mais denúncias, menor o score)
            Integer scoreAtual = score.getScore();
            Integer novoScore = Math.max(0, scoreAtual - config.getScore().getReducaoDenuncia());
            
            score.setScore(novoScore);
            score.setTotalDenuncias(score.getTotalDenuncias() + 1);
            score.setUltimaAtualizacao(java.time.LocalDateTime.now());
            
            scoreRepository.save(score);
            
            logger.info("📊 Score atualizado após denúncia:");
            logger.info("   🔢 Score anterior: {}/100", scoreAtual);
            logger.info("   🔢 Score novo: {}/100", novoScore);
            logger.info("   📉 Redução: -{} pontos", config.getScore().getReducaoDenuncia());
            
        } catch (Exception e) {
            logger.error("❌ Erro ao atualizar score após denúncia: {}", e.getMessage());
        }
    }
    
    public List<Denuncia> listarDenunciasPendentes() {
        return denunciaRepository.findDenunciasPendentes();
    }
    
    public Denuncia atualizarStatusDenuncia(Long denunciaId, Denuncia.StatusDenuncia novoStatus, 
                                           String funcionario, String observacoes) {
        Denuncia denuncia = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new RuntimeException("Denúncia não encontrada: " + denunciaId));
        
        denuncia.setStatusDenuncia(novoStatus);
        denuncia.setFuncionarioResponsavel(funcionario);
        denuncia.setObservacoes(observacoes);
        
        return denunciaRepository.save(denuncia);
    }
    
    // =============================================
    // OPERAÇÕES DE SCORES
    // =============================================
    
    public Optional<ScoreConfianca> buscarScorePorCliente(Long clienteId) {
        return scoreRepository.findByClienteId(clienteId);
    }
    
    public List<ScoreConfianca> listarContasBloqueadas() {
        return scoreRepository.findContasBloqueadas();
    }
    
    public List<ScoreConfianca> listarScoresRiscoAlto() {
        return scoreRepository.findScoresRiscoAlto();
    }
    
    // =============================================
    // RELATÓRIOS E ESTATÍSTICAS
    // =============================================
    
    public Map<String, Object> gerarRelatorioCompleto() {
        List<Object[]> estatisticasClientes = clienteRepository.countClientesByStatus();
        List<Object[]> estatisticasRisco = scoreRepository.getEstatisticasPorFaixaRisco();
        
        return Map.of(
                "estatisticasClientes", estatisticasClientes,
                "estatisticasRisco", estatisticasRisco,
                "dataGeracao", LocalDateTime.now()
        );
    }
    
    public List<Object[]> obterTopContasComMaisDenuncias() {
        return denunciaRepository.findTopContasComMaisDenuncias();
    }
    
    public Optional<Conta> buscarContaPorId(Long contaId) {
        return contaRepository.findById(contaId);
    }
    
    public List<Denuncia> buscarDenunciasPorTransacao(TransacaoPix transacao) {
        return denunciaRepository.findByTransacao(transacao);
    }
    
    public List<TransacaoPix> buscarTransacoesAltoValor(BigDecimal valorMinimo) {
        return transacaoRepository.findByValorGreaterThan(valorMinimo);
    }
    
    // =============================================
    // MÉTODOS DE COMPATIBILIDADE SIMPLIFICADOS
    // =============================================
    
    public List<Denuncia> buscarDenunciasPorIdConta(String idConta) {
        // Busca por número da conta
        List<Conta> contas = contaRepository.findAll().stream()
                .filter(c -> c.getContaCompleta().equals(idConta))
                .toList();
        
        if (!contas.isEmpty()) {
            return denunciaRepository.findByContaDenunciada(contas.get(0));
        }
        return List.of();
    }
    
    public Optional<Denuncia> buscarDenunciaPorId(String idDenuncia) {
        try {
            Long id = Long.parseLong(idDenuncia);
            return denunciaRepository.findById(id);
        } catch (NumberFormatException e) {
            return denunciaRepository.findByProtocolo(idDenuncia);
        }
    }
    
    // =============================================
    // OPERAÇÕES DE MANUTENÇÃO
    // =============================================
    
    @Transactional
    public void recalcularTodosScores() {
        logger.info("Iniciando recálculo de todos os scores...");
        
        List<Cliente> clientes = clienteRepository.findAll();
        
        for (Cliente cliente : clientes) {
            try {
                recalcularScoreCliente(cliente.getId());
            } catch (Exception e) {
                logger.error("Erro ao recalcular score do cliente {}: {}", cliente.getId(), e.getMessage());
            }
        }
        
        logger.info("Recálculo de scores concluído para {} clientes", clientes.size());
    }
    
    private void recalcularScoreCliente(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId).orElse(null);
        if (cliente == null) return;
        
        // Busca ou cria score
        ScoreConfianca score = scoreRepository.findByClienteId(clienteId)
                .orElse(new ScoreConfianca(cliente));
        
        // Recalcula baseado nas denúncias atuais
        List<Denuncia> denuncias = denunciaRepository.findByContaDenunciada(
                contaRepository.findByClienteId(clienteId).isEmpty() ? 
                null : contaRepository.findByClienteId(clienteId).get(0));
        
        // Atualiza totais
        score.setTotalDenuncias(denuncias.size());
        score.setScore(Math.max(0, 100 - (denuncias.size() * 8))); // Reduz 8 pontos por denúncia
        
        scoreRepository.save(score);
    }
    
    // Método auxiliar para determinar nível de risco baseado no score
    private String determinarNivelRisco(Integer score) {
        if (score <= 30) return "🔴 CRÍTICO";
        if (score <= 50) return "🟠 ALTO";
        if (score <= 70) return "🟡 MÉDIO";
        return "🟢 BAIXO";
    }
    
    /**
     * Obtém o protocolo da denúncia automática criada para uma transação
     */
    public String obterProtocoloDenunciaAutomatica(Long transacaoId) {
        try {
            // Buscar denúncia associada à transação
            Optional<TransacaoPix> transacaoOpt = transacaoRepository.findById(transacaoId);
            
            if (transacaoOpt.isPresent()) {
                List<Denuncia> denuncias = denunciaRepository.findByTransacao(transacaoOpt.get());
                
                if (!denuncias.isEmpty()) {
                    String protocolo = denuncias.get(0).getProtocolo();
                    logger.info("📋 Protocolo encontrado para transação {}: {}", transacaoId, protocolo);
                    return protocolo;
                } else {
                    logger.warn("⚠️ Nenhuma denúncia encontrada para transação: {}", transacaoId);
                    return null;
                }
            } else {
                logger.warn("⚠️ Transação não encontrada: {}", transacaoId);
                return null;
            }
            
        } catch (Exception e) {
            logger.error("❌ Erro ao buscar protocolo da denúncia: {}", e.getMessage(), e);
            return null;
        }
    }
} 