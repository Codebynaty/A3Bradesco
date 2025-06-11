package com.bradesco.pixmonitor.service;

import com.bradesco.pixmonitor.model.*;
import com.bradesco.pixmonitor.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class DadosTesteService implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DadosTesteService.class);

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private ScoreConfiancaRepository scoreRepository;

    @Autowired
    private DenunciaRepository denunciaRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("🗃️ Iniciando criação de dados de teste...");
        
        try {
            // Verificar se já existem dados
            if (clienteRepository.count() > 0) {
                logger.info("📊 Dados já existem. Pulando criação.");
                return;
            }

            criarDadosTeste();
            logger.info("✅ Dados de teste criados com sucesso!");
            
        } catch (Exception e) {
            logger.error("❌ Erro ao criar dados de teste: {}", e.getMessage(), e);
        }
    }

    private void criarDadosTeste() {
        // =============================================
        // CLIENTE 1 - RISCO CRÍTICO (Score ≤ 30)
        // =============================================
        Cliente clienteRiscoCritico = new Cliente(
            "João Silva Suspeito", 
            "49650556474", 
            "joao.suspeito@email.com"
        );
        clienteRiscoCritico = clienteRepository.save(clienteRiscoCritico);

        Conta contaRiscoCritico = new Conta(
            clienteRiscoCritico, 
            "0001", 
            "12345-6", 
            Conta.TipoConta.CORRENTE
        );
        contaRiscoCritico.setSaldo(new BigDecimal("1500.00"));
        contaRiscoCritico = contaRepository.save(contaRiscoCritico);

        // Score crítico (15/100)
        ScoreConfianca scoreCritico = new ScoreConfianca(clienteRiscoCritico);
        scoreCritico.setScore(15); // RISCO CRÍTICO
        scoreCritico.setTotalDenuncias(8);
        scoreCritico.setNivelRisco(ScoreConfianca.NivelRisco.ALTO);
        scoreCritico.setStatusConta(ScoreConfianca.StatusConta.BLOQUEADA);
        scoreRepository.save(scoreCritico);

        // Criar denúncias para justificar o score baixo
        for (int i = 1; i <= 3; i++) {
            Denuncia denuncia = new Denuncia(
                contaRiscoCritico,
                "Denúncia automática " + i + " - Comportamento suspeito detectado pela IA"
            );
            denuncia.setTipoDenuncia("COMPORTAMENTO_SUSPEITO");
            denuncia.setPrioridade("URGENTE");
            denunciaRepository.save(denuncia);
        }

        // =============================================
        // CLIENTE 2 - RISCO ALTO (Score ≤ 50)
        // =============================================
        Cliente clienteRiscoAlto = new Cliente(
            "Maria Santos", 
            "12345678901", 
            "maria.santos@email.com"
        );
        clienteRiscoAlto = clienteRepository.save(clienteRiscoAlto);

        Conta contaRiscoAlto = new Conta(
            clienteRiscoAlto, 
            "0002", 
            "12345-6", 
            Conta.TipoConta.CORRENTE
        );
        contaRiscoAlto.setSaldo(new BigDecimal("2500.00"));
        contaRiscoAlto = contaRepository.save(contaRiscoAlto);

        // Score alto risco (40/100)
        ScoreConfianca scoreAlto = new ScoreConfianca(clienteRiscoAlto);
        scoreAlto.setScore(40); // RISCO ALTO
        scoreAlto.setTotalDenuncias(3);
        scoreAlto.setNivelRisco(ScoreConfianca.NivelRisco.ALTO);
        scoreAlto.setStatusConta(ScoreConfianca.StatusConta.MONITORADA);
        scoreRepository.save(scoreAlto);

        // Criar uma denúncia
        Denuncia denunciaAlto = new Denuncia(
            contaRiscoAlto,
            "Transação suspeita identificada - Valor elevado em horário atípico"
        );
        denunciaAlto.setTipoDenuncia("TRANSACAO_SUSPEITA");
        denunciaAlto.setPrioridade("ALTA");
        denunciaRepository.save(denunciaAlto);

        // =============================================
        // CLIENTE 3 - RISCO MÉDIO (Score ≤ 70)
        // =============================================
        Cliente clienteRiscoMedio = new Cliente(
            "Carlos Oliveira", 
            "98765432100", 
            "carlos.oliveira@email.com"
        );
        clienteRiscoMedio = clienteRepository.save(clienteRiscoMedio);

        Conta contaRiscoMedio = new Conta(
            clienteRiscoMedio, 
            "0003", 
            "12345-6", 
            Conta.TipoConta.CORRENTE
        );
        contaRiscoMedio.setSaldo(new BigDecimal("5000.00"));
        contaRiscoMedio = contaRepository.save(contaRiscoMedio);

        // Score médio risco (65/100)
        ScoreConfianca scoreMedio = new ScoreConfianca(clienteRiscoMedio);
        scoreMedio.setScore(65); // RISCO MÉDIO
        scoreMedio.setTotalDenuncias(1);
        scoreMedio.setNivelRisco(ScoreConfianca.NivelRisco.MEDIO);
        scoreMedio.setStatusConta(ScoreConfianca.StatusConta.MONITORADA);
        scoreRepository.save(scoreMedio);

        // =============================================
        // CLIENTE 4 - RISCO BAIXO (Score > 70)
        // =============================================
        Cliente clienteRiscoBaixo = new Cliente(
            "Ana Paula Confiável", 
            "11122233344", 
            "ana.confiavel@email.com"
        );
        clienteRiscoBaixo = clienteRepository.save(clienteRiscoBaixo);

        Conta contaRiscoBaixo = new Conta(
            clienteRiscoBaixo, 
            "0004", 
            "12345-6", 
            Conta.TipoConta.CORRENTE
        );
        contaRiscoBaixo.setSaldo(new BigDecimal("10000.00"));
        contaRiscoBaixo = contaRepository.save(contaRiscoBaixo);

        // Score baixo risco (85/100)
        ScoreConfianca scoreBaixo = new ScoreConfianca(clienteRiscoBaixo);
        scoreBaixo.setScore(85); // RISCO BAIXO
        scoreBaixo.setTotalDenuncias(0);
        scoreBaixo.setNivelRisco(ScoreConfianca.NivelRisco.BAIXO);
        scoreBaixo.setStatusConta(ScoreConfianca.StatusConta.NORMAL);
        scoreRepository.save(scoreBaixo);

        // =============================================
        // CLIENTE PRINCIPAL (para interface)
        // =============================================
        Cliente clientePrincipal = new Cliente(
            "Usuário Teste", 
            "12345678901", 
            "usuario@teste.com"
        );
        clientePrincipal = clienteRepository.save(clientePrincipal);

        Conta contaPrincipal = new Conta(
            clientePrincipal, 
            "1234", 
            "12345-6", 
            Conta.TipoConta.CORRENTE
        );
        contaPrincipal.setSaldo(new BigDecimal("2847.53"));
        contaPrincipal = contaRepository.save(contaPrincipal);

        // Score inicial do usuário da interface (11 - baixo para demonstrar)
        ScoreConfianca scorePrincipal = new ScoreConfianca(clientePrincipal);
        scorePrincipal.setScore(11); // Score baixo inicial para demonstração
        scorePrincipal.setTotalDenuncias(0);
        scorePrincipal.setNivelRisco(ScoreConfianca.NivelRisco.ALTO);
        scorePrincipal.setStatusConta(ScoreConfianca.StatusConta.NORMAL);
        scoreRepository.save(scorePrincipal);

        logger.info("📊 DADOS DE TESTE CRIADOS:");
        logger.info("   🔴 Cliente Risco Crítico: {} (Score: {})", clienteRiscoCritico.getNomeCompleto(), 15);
        logger.info("   🟠 Cliente Risco Alto: {} (Score: {})", clienteRiscoAlto.getNomeCompleto(), 40);
        logger.info("   🟡 Cliente Risco Médio: {} (Score: {})", clienteRiscoMedio.getNomeCompleto(), 65);
        logger.info("   🟢 Cliente Risco Baixo: {} (Score: {})", clienteRiscoBaixo.getNomeCompleto(), 85);
        logger.info("   👤 Cliente Principal (Interface): {} (Score: {})", clientePrincipal.getNomeCompleto(), 11);
    }
} 