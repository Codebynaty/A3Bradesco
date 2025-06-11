package com.bradesco.pixmonitor.service;

import com.bradesco.pixmonitor.model.ContaSuspeita;
import com.bradesco.pixmonitor.repository.ContaSuspeitaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ContaSuspeitaService {

    private static final Logger logger = LoggerFactory.getLogger(ContaSuspeitaService.class);
    private static final int LIMITE_DENUNCIAS_CONGELAMENTO = 3;

    private final ContaSuspeitaRepository contaSuspeitaRepository;

    public ContaSuspeitaService(ContaSuspeitaRepository contaSuspeitaRepository) {
        this.contaSuspeitaRepository = contaSuspeitaRepository;
    }

    @Transactional(readOnly = true)
    public List<ContaSuspeita> listarTodasContas() {
        logger.debug("Listando todas as contas suspeitas");
        return contaSuspeitaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ContaSuspeita> buscarPorId(String idConta) {
        logger.debug("Buscando conta suspeita por ID: {}", idConta);
        return contaSuspeitaRepository.findById(idConta);
    }

    @Transactional
    public ContaSuspeita criarOuAtualizar(String idConta) {
        Optional<ContaSuspeita> contaExistente = contaSuspeitaRepository.findById(idConta);
        
        if (contaExistente.isPresent()) {
            ContaSuspeita conta = contaExistente.get();
            conta.setNumeroDenuncias(conta.getNumeroDenuncias() + 1);
            
            if (conta.getNumeroDenuncias() >= LIMITE_DENUNCIAS_CONGELAMENTO && !conta.isCongelada()) {
                conta.setCongelada(true);
                logger.warn("Conta {} foi congelada automaticamente após atingir {} denúncias!", 
                           idConta, LIMITE_DENUNCIAS_CONGELAMENTO);
            }
            
            return contaSuspeitaRepository.save(conta);
        } else {
            ContaSuspeita novaConta = new ContaSuspeita();
            novaConta.setIdConta(idConta);
            novaConta.setNumeroDenuncias(1);
            novaConta.setCongelada(false);
            
            logger.info("Nova conta suspeita criada: {}", idConta);
            return contaSuspeitaRepository.save(novaConta);
        }
    }

    @Transactional
    public void descongelarConta(String idConta) {
        Optional<ContaSuspeita> conta = contaSuspeitaRepository.findById(idConta);
        
        if (conta.isPresent()) {
            ContaSuspeita contaSuspeita = conta.get();
            contaSuspeita.setCongelada(false);
            contaSuspeitaRepository.save(contaSuspeita);
            logger.info("Conta {} foi descongelada manualmente", idConta);
        } else {
            logger.warn("Tentativa de descongelar conta inexistente: {}", idConta);
            throw new IllegalArgumentException("Conta não encontrada: " + idConta);
        }
    }
} 