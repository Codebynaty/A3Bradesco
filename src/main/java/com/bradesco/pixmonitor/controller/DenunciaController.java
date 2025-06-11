package com.bradesco.pixmonitor.controller;

import com.bradesco.pixmonitor.model.ContaSuspeita;
import com.bradesco.pixmonitor.model.Denuncia;
import com.bradesco.pixmonitor.service.ContaSuspeitaService;
import com.bradesco.pixmonitor.service.DenunciaService;
import com.bradesco.pixmonitor.service.BancoDadosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/denuncias")
@CrossOrigin(origins = "*")
public class DenunciaController {

    private static final Logger logger = LoggerFactory.getLogger(DenunciaController.class);

    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private BancoDadosService bancoDadosService;

    private final DenunciaService denunciaService;
    private final ContaSuspeitaService contaSuspeitaService;

    public DenunciaController(DenunciaService denunciaService, 
                             ContaSuspeitaService contaSuspeitaService) {
        this.denunciaService = denunciaService;
        this.contaSuspeitaService = contaSuspeitaService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDenuncias(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim,
            @RequestParam(required = false) String busca) {
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> denuncias = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection()) {
            StringBuilder queryBuilder = new StringBuilder("""
                SELECT d.*, c.nome as cliente_nome, c.cpf as cliente_cpf 
                FROM denuncias d 
                LEFT JOIN clientes c ON d.cliente_id = c.id 
                WHERE 1=1
                """);
            
            List<Object> params = new ArrayList<>();
            
            if (status != null && !status.isEmpty()) {
                queryBuilder.append(" AND d.status = ?");
                params.add(status);
            }
            
            if (dataInicio != null && !dataInicio.isEmpty()) {
                queryBuilder.append(" AND DATE(d.data_criacao) >= ?");
                params.add(dataInicio);
            }
            
            if (dataFim != null && !dataFim.isEmpty()) {
                queryBuilder.append(" AND DATE(d.data_criacao) <= ?");
                params.add(dataFim);
            }
            
            if (busca != null && !busca.isEmpty()) {
                queryBuilder.append(" AND (d.protocolo LIKE ? OR c.nome LIKE ? OR c.cpf LIKE ?)");
                String searchPattern = "%" + busca + "%";
                params.add(searchPattern);
                params.add(searchPattern);
                params.add(searchPattern);
            }
            
            queryBuilder.append(" ORDER BY d.data_criacao DESC LIMIT ? OFFSET ?");
            params.add(size);
            params.add(page * size);
            
            try (PreparedStatement ps = conn.prepareStatement(queryBuilder.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> denuncia = new HashMap<>();
                        denuncia.put("id", rs.getLong("id"));
                        denuncia.put("protocolo", rs.getString("protocolo"));
                        denuncia.put("tipo_denuncia", rs.getString("tipo_denuncia"));
                        denuncia.put("descricao", rs.getString("descricao"));
                        denuncia.put("evidencias", rs.getString("evidencias"));
                        denuncia.put("status", rs.getString("status"));
                        denuncia.put("prioridade", rs.getString("prioridade"));
                        denuncia.put("funcionario_responsavel", rs.getString("funcionario_responsavel"));
                        denuncia.put("data_criacao", rs.getTimestamp("data_criacao"));
                        denuncia.put("data_resolucao", rs.getTimestamp("data_resolucao"));
                        denuncia.put("observacoes", rs.getString("observacoes"));
                        denuncia.put("cliente_nome", rs.getString("cliente_nome"));
                        denuncia.put("cliente_cpf", rs.getString("cliente_cpf"));
                        denuncias.add(denuncia);
                    }
                }
            }
            
            // Contar total
            String countQuery = "SELECT COUNT(*) FROM denuncias d LEFT JOIN clientes c ON d.cliente_id = c.id";
            try (PreparedStatement ps = conn.prepareStatement(countQuery);
                 ResultSet rs = ps.executeQuery()) {
                result.put("total", rs.next() ? rs.getInt(1) : 0);
            }
            
            result.put("denuncias", denuncias);
            result.put("page", page);
            result.put("size", size);
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("denuncias", new ArrayList<>());
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDenuncia(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection conn = dataSource.getConnection()) {
            String query = """
                SELECT d.*, c.nome as cliente_nome, c.cpf as cliente_cpf, c.telefone,
                       t.valor as transacao_valor, t.descricao as transacao_descricao
                FROM denuncias d 
                LEFT JOIN clientes c ON d.cliente_id = c.id 
                LEFT JOIN transacoes_pix t ON d.transacao_id = t.id
                WHERE d.id = ?
                """;
            
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setLong(1, id);
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        result.put("id", rs.getLong("id"));
                        result.put("protocolo", rs.getString("protocolo"));
                        result.put("tipo_denuncia", rs.getString("tipo_denuncia"));
                        result.put("descricao", rs.getString("descricao"));
                        result.put("evidencias", rs.getString("evidencias"));
                        result.put("status", rs.getString("status"));
                        result.put("prioridade", rs.getString("prioridade"));
                        result.put("funcionario_responsavel", rs.getString("funcionario_responsavel"));
                        result.put("data_criacao", rs.getTimestamp("data_criacao"));
                        result.put("data_resolucao", rs.getTimestamp("data_resolucao"));
                        result.put("observacoes", rs.getString("observacoes"));
                        result.put("cliente_nome", rs.getString("cliente_nome"));
                        result.put("cliente_cpf", rs.getString("cliente_cpf"));
                        result.put("cliente_telefone", rs.getString("telefone"));
                        result.put("transacao_valor", rs.getBigDecimal("transacao_valor"));
                        result.put("transacao_descricao", rs.getString("transacao_descricao"));
                    } else {
                        result.put("error", "Denúncia não encontrada");
                    }
                }
            }
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createDenuncia(@RequestBody Map<String, Object> denunciaData) {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection conn = dataSource.getConnection()) {
            String protocolo = "DEN-" + System.currentTimeMillis();
            
            String insertQuery = """
                INSERT INTO denuncias (protocolo, cliente_id, transacao_id, tipo_denuncia, descricao, 
                                     evidencias, status, prioridade, observacoes) 
                VALUES (?, ?, ?, ?, ?, ?, 'PENDENTE', ?, ?)
                """;
            
            try (PreparedStatement ps = conn.prepareStatement(insertQuery)) {
                ps.setString(1, protocolo);
                ps.setObject(2, denunciaData.get("cliente_id"));
                ps.setObject(3, denunciaData.get("transacao_id"));
                ps.setString(4, (String) denunciaData.get("tipo_denuncia"));
                ps.setString(5, (String) denunciaData.get("descricao"));
                ps.setString(6, (String) denunciaData.getOrDefault("evidencias", ""));
                ps.setString(7, (String) denunciaData.getOrDefault("prioridade", "MEDIA"));
                ps.setString(8, (String) denunciaData.getOrDefault("observacoes", ""));
                
                int affected = ps.executeUpdate();
                
                if (affected > 0) {
                    result.put("success", true);
                    result.put("protocolo", protocolo);
                    result.put("message", "Denúncia criada com sucesso");
                } else {
                    result.put("success", false);
                    result.put("message", "Erro ao criar denúncia");
                }
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateDenuncia(@PathVariable Long id, @RequestBody Map<String, Object> denunciaData) {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection conn = dataSource.getConnection()) {
            String updateQuery = """
                UPDATE denuncias SET 
                    status = ?, 
                    funcionario_responsavel = ?, 
                    observacoes = ?,
                    data_resolucao = ?
                WHERE id = ?
                """;
            
            try (PreparedStatement ps = conn.prepareStatement(updateQuery)) {
                ps.setString(1, (String) denunciaData.get("status"));
                ps.setString(2, (String) denunciaData.get("funcionario_responsavel"));
                ps.setString(3, (String) denunciaData.get("observacoes"));
                
                if ("RESOLVIDA".equals(denunciaData.get("status")) || "ARQUIVADA".equals(denunciaData.get("status"))) {
                    ps.setObject(4, LocalDateTime.now());
                } else {
                    ps.setObject(4, null);
                }
                
                ps.setLong(5, id);
                
                int affected = ps.executeUpdate();
                
                result.put("success", affected > 0);
                result.put("message", affected > 0 ? "Denúncia atualizada com sucesso" : "Denúncia não encontrada");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDenunciasStats() {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection conn = dataSource.getConnection()) {
            // Stats por status
            String statusQuery = "SELECT status, COUNT(*) as total FROM denuncias GROUP BY status";
            Map<String, Integer> statusStats = new HashMap<>();
            
            try (PreparedStatement ps = conn.prepareStatement(statusQuery);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    statusStats.put(rs.getString("status"), rs.getInt("total"));
                }
            }
            
            // Stats por prioridade
            String prioridadeQuery = "SELECT prioridade, COUNT(*) as total FROM denuncias GROUP BY prioridade";
            Map<String, Integer> prioridadeStats = new HashMap<>();
            
            try (PreparedStatement ps = conn.prepareStatement(prioridadeQuery);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    prioridadeStats.put(rs.getString("prioridade"), rs.getInt("total"));
                }
            }
            
            // Stats por tipo
            String tipoQuery = "SELECT tipo_denuncia, COUNT(*) as total FROM denuncias GROUP BY tipo_denuncia ORDER BY total DESC";
            List<Map<String, Object>> tipoStats = new ArrayList<>();
            
            try (PreparedStatement ps = conn.prepareStatement(tipoQuery);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("tipo", rs.getString("tipo_denuncia"));
                    item.put("total", rs.getInt("total"));
                    tipoStats.add(item);
                }
            }
            
            result.put("status", statusStats);
            result.put("prioridade", prioridadeStats);
            result.put("tipos", tipoStats);
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/contas-suspeitas")
    public ResponseEntity<List<ContaSuspeita>> listarContasSuspeitas() {
        return ResponseEntity.ok(denunciaService.listarContasSuspeitas());
    }

    @PutMapping("/contas-suspeitas/{idConta}/descongelar")
    public ResponseEntity<Map<String, String>> descongelarConta(@PathVariable String idConta) {
        contaSuspeitaService.descongelarConta(idConta);
        return ResponseEntity.ok(Map.of("message", "Conta descongelada com sucesso", "idConta", idConta));
    }

    /**
     * Criar denúncia manual via interface web com tipo de golpe
     */
    @PostMapping("/manual")
    public ResponseEntity<Map<String, Object>> criarDenunciaManual(@RequestBody Map<String, Object> denunciaData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.info("📋 Recebida solicitação de denúncia manual via interface web");
            logger.info("📋 Dados recebidos: {}", denunciaData);
            
            // Validar dados obrigatórios
            String tipoGolpeCodigo = (String) denunciaData.get("tipoGolpe");
            String descricao = (String) denunciaData.get("motivoDenuncia");
            
            if (tipoGolpeCodigo == null || tipoGolpeCodigo.trim().isEmpty()) {
                throw new IllegalArgumentException("Tipo de golpe é obrigatório");
            }
            
            if (descricao == null || descricao.trim().length() < 20) {
                throw new IllegalArgumentException("Descrição deve ter pelo menos 20 caracteres");
            }
            
            // Buscar tipo de golpe
            com.bradesco.pixmonitor.model.TipoGolpe tipoGolpe = 
                com.bradesco.pixmonitor.model.TipoGolpe.porCodigo(tipoGolpeCodigo);
            
            if (tipoGolpe == null) {
                throw new IllegalArgumentException("Tipo de golpe inválido: " + tipoGolpeCodigo);
            }
            
            // Enriquecer dados da denúncia com informações do tipo de golpe
            Map<String, Object> dadosEnriquecidos = new HashMap<>(denunciaData);
            dadosEnriquecidos.put("tipo_denuncia", tipoGolpe.getNome());
            dadosEnriquecidos.put("categoria_golpe", tipoGolpe.getCategoria().getNome());
            dadosEnriquecidos.put("severidade_golpe", tipoGolpe.getSeveridade().getNome());
            dadosEnriquecidos.put("codigo_golpe", tipoGolpe.getCodigo());
            
            // Determinar prioridade baseada na severidade do golpe
            String prioridade = determinarPrioridade(tipoGolpe.getSeveridade());
            dadosEnriquecidos.put("prioridade", prioridade);
            
            // Adicionar informações de contexto
            dadosEnriquecidos.put("origem", "INTERFACE_CLIENTE");
            dadosEnriquecidos.put("descricao_enriquecida", 
                String.format("[%s - %s] %s", 
                    tipoGolpe.getEmoji(), 
                    tipoGolpe.getNome(), 
                    descricao));
            
            logger.info("📋 Dados enriquecidos: {}", dadosEnriquecidos);
            
            // Usar o método do serviço para criar denúncia
            Denuncia denuncia = bancoDadosService.criarDenunciaManual(dadosEnriquecidos);
            
            result.put("success", true);
            result.put("message", "✅ Denúncia criada com sucesso!");
            result.put("protocolo", denuncia.getProtocolo());
            result.put("id", denuncia.getId());
            result.put("tipo", denuncia.getTipoDenuncia());
            result.put("prioridade", denuncia.getPrioridade());
            result.put("status", denuncia.getStatusDenuncia().toString());
            result.put("dataHora", denuncia.getDataDenuncia().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            
            // Informações específicas do tipo de golpe
            result.put("tipoGolpe", Map.of(
                "codigo", tipoGolpe.getCodigo(),
                "nome", tipoGolpe.getNome(),
                "categoria", tipoGolpe.getCategoria().getNome(),
                "severidade", tipoGolpe.getSeveridade().getNome(),
                "emoji", tipoGolpe.getEmoji()
            ));
            
            // Informações adicionais para a interface
            String tempoAnalise = determinarTempoAnalise(tipoGolpe.getSeveridade());
            result.put("info", Map.of(
                "mensagem", String.format("Denúncia de %s registrada com prioridade %s.", 
                    tipoGolpe.getNome(), prioridade),
                "protocolo_acesso", "Use o protocolo " + denuncia.getProtocolo() + " para acompanhar o status.",
                "tempo_analise", tempoAnalise,
                "contato", determinarContatoUrgencia(tipoGolpe.getSeveridade())
            ));
            
            logger.info("✅ Denúncia manual criada com sucesso: protocolo={}, tipo={}", 
                denuncia.getProtocolo(), tipoGolpe.getNome());
            
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Dados inválidos na denúncia manual: {}", e.getMessage());
            result.put("success", false);
            result.put("error", "Dados inválidos: " + e.getMessage());
            result.put("code", "INVALID_DATA");
            return ResponseEntity.badRequest().body(result);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao processar denúncia manual: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "Erro interno do servidor: " + e.getMessage());
            result.put("code", "INTERNAL_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * Determina a prioridade baseada na severidade do golpe
     */
    private String determinarPrioridade(com.bradesco.pixmonitor.model.TipoGolpe.SeveridadeGolpe severidade) {
        return switch (severidade) {
            case CRITICA -> "URGENTE";
            case ALTA -> "ALTA";
            case MEDIA -> "MEDIA";
            case BAIXA -> "BAIXA";
        };
    }

    /**
     * Determina o tempo de análise baseado na severidade
     */
    private String determinarTempoAnalise(com.bradesco.pixmonitor.model.TipoGolpe.SeveridadeGolpe severidade) {
        return switch (severidade) {
            case CRITICA -> "Análise imediata - até 2 horas";
            case ALTA -> "Análise prioritária - até 8 horas";
            case MEDIA -> "Análise padrão - até 24 horas";
            case BAIXA -> "Análise normal - até 48 horas";
        };
    }

    /**
     * Determina informações de contato baseadas na severidade
     */
    private String determinarContatoUrgencia(com.bradesco.pixmonitor.model.TipoGolpe.SeveridadeGolpe severidade) {
        return switch (severidade) {
            case CRITICA -> "URGÊNCIA: Entre em contato imediatamente com nossa central de segurança 0800-xxx-xxxx";
            case ALTA -> "Em caso de urgência, ligue para 0800-xxx-xxxx ou acesse nosso chat 24h";
            case MEDIA -> "Acompanhe o status pelo protocolo ou entre em contato se necessário";
            case BAIXA -> "Sua denúncia será analisada no prazo normal. Obrigado pela colaboração!";
        };
    }
} 