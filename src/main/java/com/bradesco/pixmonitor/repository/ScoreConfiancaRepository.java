package com.bradesco.pixmonitor.repository;

import com.bradesco.pixmonitor.model.ScoreConfianca;
import com.bradesco.pixmonitor.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreConfiancaRepository extends JpaRepository<ScoreConfianca, Long> {
    
    /**
     * Busca score por cliente
     */
    Optional<ScoreConfianca> findByCliente(Cliente cliente);
    
    /**
     * Busca score por cliente ID
     */
    Optional<ScoreConfianca> findByClienteId(Long clienteId);
    
    /**
     * Busca scores por nível de risco
     */
    List<ScoreConfianca> findByNivelRisco(ScoreConfianca.NivelRisco nivelRisco);
    
    /**
     * Busca scores por status da conta
     */
    List<ScoreConfianca> findByStatusConta(ScoreConfianca.StatusConta statusConta);
    
    /**
     * Busca scores com valor menor que
     */
    List<ScoreConfianca> findByScoreLessThan(int score);
    
    /**
     * Busca scores com valor maior que
     */
    List<ScoreConfianca> findByScoreGreaterThan(int score);
    
    /**
     * Busca scores em faixa de valores
     */
    List<ScoreConfianca> findByScoreBetween(int scoreMinimo, int scoreMaximo);
    
    /**
     * Busca scores com muitas denúncias
     */
    List<ScoreConfianca> findByTotalDenunciasGreaterThan(int limite);
    
    /**
     * Busca scores com muitas transações
     */
    List<ScoreConfianca> findByTotalTransacoesGreaterThan(int limite);
    
    /**
     * Busca scores com valor alto transacionado
     */
    List<ScoreConfianca> findByValorTotalTransacionadoGreaterThan(BigDecimal valor);
    
    /**
     * Busca scores atualizados recentemente
     */
    List<ScoreConfianca> findByUltimaAtualizacaoAfter(LocalDateTime data);
    
    /**
     * Busca scores de risco alto (< 40)
     */
    @Query("SELECT sc FROM ScoreConfianca sc WHERE sc.score < 40 ORDER BY sc.score ASC")
    List<ScoreConfianca> findScoresRiscoAlto();
    
    /**
     * Busca scores de risco médio (40-69)
     */
    @Query("SELECT sc FROM ScoreConfianca sc WHERE sc.score >= 40 AND sc.score < 70 ORDER BY sc.score ASC")
    List<ScoreConfianca> findScoresRiscoMedio();
    
    /**
     * Busca scores de risco baixo (≥ 70)
     */
    @Query("SELECT sc FROM ScoreConfianca sc WHERE sc.score >= 70 ORDER BY sc.score DESC")
    List<ScoreConfianca> findScoresRiscoBaixo();
    
    /**
     * Busca contas bloqueadas
     */
    @Query("SELECT sc FROM ScoreConfianca sc WHERE sc.statusConta = 'BLOQUEADA' ORDER BY sc.ultimaAtualizacao DESC")
    List<ScoreConfianca> findContasBloqueadas();
    
    /**
     * Busca contas monitoradas
     */
    @Query("SELECT sc FROM ScoreConfianca sc WHERE sc.statusConta = 'MONITORADA' ORDER BY sc.score ASC")
    List<ScoreConfianca> findContasMonitoradas();
    
    /**
     * Estatísticas de scores por faixa
     */
    @Query("SELECT " +
           "CASE " +
           "  WHEN sc.score < 40 THEN 'ALTO RISCO' " +
           "  WHEN sc.score < 70 THEN 'MÉDIO RISCO' " +
           "  ELSE 'BAIXO RISCO' " +
           "END, COUNT(sc) " +
           "FROM ScoreConfianca sc " +
           "GROUP BY " +
           "CASE " +
           "  WHEN sc.score < 40 THEN 'ALTO RISCO' " +
           "  WHEN sc.score < 70 THEN 'MÉDIO RISCO' " +
           "  ELSE 'BAIXO RISCO' " +
           "END")
    List<Object[]> getEstatisticasPorFaixaRisco();
    
    /**
     * Score médio por período de atualização
     */
    @Query("SELECT DATE(sc.ultimaAtualizacao), AVG(sc.score) FROM ScoreConfianca sc " +
           "WHERE sc.ultimaAtualizacao >= :dataInicio " +
           "GROUP BY DATE(sc.ultimaAtualizacao) " +
           "ORDER BY DATE(sc.ultimaAtualizacao)")
    List<Object[]> getScoreMedioPorPeriodo(@Param("dataInicio") LocalDateTime dataInicio);
    
    /**
     * Top 10 piores scores
     */
    @Query("SELECT sc FROM ScoreConfianca sc ORDER BY sc.score ASC, sc.totalDenuncias DESC LIMIT 10")
    List<ScoreConfianca> getTop10PioresScores();
    
    /**
     * Top 10 melhores scores
     */
    @Query("SELECT sc FROM ScoreConfianca sc ORDER BY sc.score DESC, sc.totalTransacoes DESC LIMIT 10")
    List<ScoreConfianca> getTop10MelhoresScores();
    
    /**
     * Clientes com score deteriorando
     */
    @Query("SELECT sc FROM ScoreConfianca sc " +
           "WHERE sc.ultimaAtualizacao >= :dataInicio " +
           "AND sc.score < 60 " +
           "ORDER BY sc.ultimaAtualizacao DESC")
    List<ScoreConfianca> findClientesComScoreDeteriorando(@Param("dataInicio") LocalDateTime dataInicio);
    
    /**
     * Busca scores por CPF do cliente
     */
    @Query("SELECT sc FROM ScoreConfianca sc " +
           "JOIN sc.cliente c " +
           "WHERE c.cpf = :cpf")
    Optional<ScoreConfianca> findByClienteCpf(@Param("cpf") String cpf);
    
    /**
     * Relatório completo de risco
     */
    @Query("SELECT sc.statusConta, sc.nivelRisco, COUNT(sc), AVG(sc.score), SUM(sc.totalDenuncias) " +
           "FROM ScoreConfianca sc " +
           "GROUP BY sc.statusConta, sc.nivelRisco " +
           "ORDER BY AVG(sc.score) ASC")
    List<Object[]> getRelatorioCompletoRisco();
    
    /**
     * Busca clientes que precisam de atenção (score baixo, muitas transações)
     */
    @Query("SELECT sc FROM ScoreConfianca sc " +
           "WHERE sc.score < 50 " +
           "AND sc.totalTransacoes > 100 " +
           "AND sc.valorTotalTransacionado > :valorMinimo " +
           "ORDER BY sc.score ASC")
    List<ScoreConfianca> findClientesPrecisandoAtencao(@Param("valorMinimo") BigDecimal valorMinimo);
    
    /**
     * Atualização em lote de scores
     */
    @Query("UPDATE ScoreConfianca sc SET sc.ultimaAtualizacao = CURRENT_TIMESTAMP " +
           "WHERE sc.ultimaAtualizacao < :dataLimite")
    int atualizarScoresAntigos(@Param("dataLimite") LocalDateTime dataLimite);
} 