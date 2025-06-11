package com.bradesco.pixmonitor.service;

import com.bradesco.pixmonitor.model.ContaSuspeita;
import com.bradesco.pixmonitor.model.Denuncia;
import com.bradesco.pixmonitor.repository.DenunciaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.time.LocalDateTime;

@Service
public class DenunciaService {

    private static final Logger logger = LoggerFactory.getLogger(DenunciaService.class);

    private final DenunciaRepository denunciaRepository;
    private final ContaSuspeitaService contaSuspeitaService;

    public DenunciaService(DenunciaRepository denunciaRepository, 
                          ContaSuspeitaService contaSuspeitaService) {
        this.denunciaRepository = denunciaRepository;
        this.contaSuspeitaService = contaSuspeitaService;
    }

    @Transactional
    public Denuncia registrarDenuncia(Denuncia denuncia) {
        logger.info("Registrando denúncia para a conta: {}", denuncia.getIdConta());
        
        // Atualiza ou cria a conta suspeita
        ContaSuspeita conta = contaSuspeitaService.criarOuAtualizar(denuncia.getIdConta());
        
        // Salva a denúncia
        Denuncia denunciaSalva = denunciaRepository.save(denuncia);
        logger.info("Denúncia registrada com sucesso. ID: {}", denunciaSalva.getIdDenuncia());
        
        return denunciaSalva;
    }

    @Transactional(readOnly = true)
    public List<ContaSuspeita> listarContasSuspeitas() {
        logger.debug("Listando todas as contas suspeitas");
        return contaSuspeitaService.listarTodasContas();
    }

    @Transactional(readOnly = true)
    public List<Denuncia> listarTodasDenuncias() {
        logger.debug("Listando todas as denúncias");
        return denunciaRepository.findAll();
    }

    /**
     * Conta denúncias do dia atual
     */
    public long contarDenunciasHoje() {
        LocalDateTime inicioHoje = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime fimHoje = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        
        return denunciaRepository.countByDataDenunciaBetween(inicioHoje, fimHoje);
    }
    
    /**
     * Retorna dados de denúncias por hora para gráficos
     */
    public List<Map<String, Object>> getDenunciasPorHora() {
        List<Map<String, Object>> dadosHorarios = new ArrayList<>();
        LocalDateTime agora = LocalDateTime.now();
        
        for (int i = 23; i >= 0; i--) {
            LocalDateTime horaInicio = agora.minusHours(i).withMinute(0).withSecond(0);
            LocalDateTime horaFim = agora.minusHours(i).withMinute(59).withSecond(59);
            
            long quantidade = denunciaRepository.countByDataDenunciaBetween(horaInicio, horaFim);
            
            Map<String, Object> dadoHora = new HashMap<>();
            dadoHora.put("hora", horaInicio.getHour());
            dadoHora.put("quantidade", quantidade);
            dadosHorarios.add(dadoHora);
        }
        
        return dadosHorarios;
    }
    
    /**
     * Retorna denúncias formatadas para tabela com paginação
     */
    public List<Map<String, Object>> getDenunciasParaTabela(int page, int size, String filtro, String status) {
        List<Denuncia> denuncias;
        
        if (status != null && !status.isEmpty()) {
            Denuncia.StatusDenuncia statusEnum = Denuncia.StatusDenuncia.valueOf(status.toUpperCase());
            denuncias = denunciaRepository.findByStatusDenunciaOrderByDataDenunciaDesc(statusEnum);
        } else {
            denuncias = denunciaRepository.findAllByOrderByDataDenunciaDesc();
        }
        
        // Aplica filtro se fornecido
        if (filtro != null && !filtro.isEmpty()) {
            final String filtroLower = filtro.toLowerCase();
            denuncias = denuncias.stream()
                .filter(d -> d.getMotivo().toLowerCase().contains(filtroLower) ||
                           (d.getContaDenunciada() != null && 
                            d.getContaDenunciada().getNumeroConta().contains(filtroLower)))
                .collect(Collectors.toList());
        }
        
        // Aplica paginação
        int start = page * size;
        int end = Math.min(start + size, denuncias.size());
        
        if (start >= denuncias.size()) {
            return new ArrayList<>();
        }
        
        return denuncias.subList(start, end).stream()
            .map(this::mapDenunciaParaTabela)
            .collect(Collectors.toList());
    }
    
    /**
     * Conta total de denúncias com filtros
     */
    public long contarDenuncias(String filtro, String status) {
        if (status != null && !status.isEmpty()) {
            Denuncia.StatusDenuncia statusEnum = Denuncia.StatusDenuncia.valueOf(status.toUpperCase());
            return denunciaRepository.countByStatusDenuncia(statusEnum);
        } else {
            return denunciaRepository.count();
        }
    }
    
    private Map<String, Object> mapDenunciaParaTabela(Denuncia denuncia) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", denuncia.getId());
        map.put("protocolo", denuncia.getProtocolo());
        map.put("dataHora", denuncia.getDataDenuncia());
        map.put("contaDenunciada", denuncia.getContaDenunciada() != null ? 
               denuncia.getContaDenunciada().getContaCompleta() : "N/A");
        map.put("motivo", denuncia.getMotivo());
        map.put("status", denuncia.getStatusDenuncia().toString());
        map.put("funcionario", denuncia.getFuncionarioResponsavel() != null ? 
               denuncia.getFuncionarioResponsavel() : "Não atribuído");
        map.put("prioridade", calcularPrioridade(denuncia));
        return map;
    }
    
    private String calcularPrioridade(Denuncia denuncia) {
        // Lógica simples para calcular prioridade
        if (denuncia.getMotivo().toLowerCase().contains("urgente") || 
            denuncia.getMotivo().toLowerCase().contains("fraude")) {
            return "ALTA";
        } else if (denuncia.getMotivo().toLowerCase().contains("suspeita")) {
            return "MÉDIA";
        }
        return "BAIXA";
    }
} 