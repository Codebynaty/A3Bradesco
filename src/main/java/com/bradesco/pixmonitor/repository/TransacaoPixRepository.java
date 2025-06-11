package com.bradesco.pixmonitor.repository;

import com.bradesco.pixmonitor.model.TransacaoPix;
import com.bradesco.pixmonitor.model.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransacaoPixRepository extends JpaRepository<TransacaoPix, Long> {
    
    /**
     * Busca transações por conta origem
     */
    List<TransacaoPix> findByContaOrigem(Conta contaOrigem);
    
    /**
     * Busca transações por conta destino
     */
    List<TransacaoPix> findByContaDestino(Conta contaDestino);
    
    /**
     * Busca transações por status
     */
    List<TransacaoPix> findByStatusTransacao(TransacaoPix.StatusTransacao status);
    
    /**
     * Busca transações em período - Corrigido para usar query explicita
     */
    @Query("SELECT t FROM TransacaoPix t WHERE t.dataTransacao BETWEEN :inicio AND :fim ORDER BY t.dataTransacao DESC")
    List<TransacaoPix> findByDataTransacaoBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
    
    /**
     * Busca transações com valor maior que
     */
    List<TransacaoPix> findByValorGreaterThan(BigDecimal valor);
    
    /**
     * Busca transações com valor menor que
     */
    List<TransacaoPix> findByValorLessThan(BigDecimal valor);
    
    /**
     * Busca transações com score baixo
     */
    List<TransacaoPix> findByScoreRiscoLessThan(BigDecimal score);
    
    /**
     * Busca transações suspeitas
     */
    @Query("SELECT t FROM TransacaoPix t " +
           "WHERE t.statusTransacao IN ('SUSPEITA', 'BLOQUEADA') " +
           "ORDER BY t.dataTransacao DESC")
    List<TransacaoPix> findTransacoesSuspeitas();
    
    /**
     * Busca transações de uma conta (origem ou destino)
     */
    @Query("SELECT t FROM TransacaoPix t " +
           "WHERE t.contaOrigem = :conta OR t.contaDestino = :conta " +
           "ORDER BY t.dataTransacao DESC")
    List<TransacaoPix> findByContaOrigemOrContaDestino(@Param("conta") Conta conta);
    
    /**
     * Busca transações recentes (últimas 24h)
     */
    @Query("SELECT t FROM TransacaoPix t " +
           "WHERE t.dataTransacao >= :dataLimite " +
           "ORDER BY t.dataTransacao DESC")
    List<TransacaoPix> findTransacoesRecentes(@Param("dataLimite") LocalDateTime dataLimite);
    
    /**
     * Busca transações de alto valor
     */
    @Query("SELECT t FROM TransacaoPix t " +
           "WHERE t.valor >= :valorMinimo " +
           "ORDER BY t.valor DESC")
    List<TransacaoPix> findTransacoesAltoValor(@Param("valorMinimo") BigDecimal valorMinimo);
    
    /**
     * Conta transações por conta origem
     */
    long countByContaOrigem(Conta conta);
    
    /**
     * Conta transações por conta destino
     */
    long countByContaDestino(Conta conta);
    
    /**
     * Soma valores transacionados por conta origem
     */
    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM TransacaoPix t " +
           "WHERE t.contaOrigem = :conta")
    BigDecimal sumValorByContaOrigem(@Param("conta") Conta conta);
    
    /**
     * Busca transações entre duas datas específicas
     */
    @Query("SELECT t FROM TransacaoPix t " +
           "WHERE t.dataTransacao BETWEEN :inicio AND :fim " +
           "ORDER BY t.dataTransacao DESC")
    List<TransacaoPix> findTransacoesPorPeriodo(@Param("inicio") LocalDateTime inicio, 
                                               @Param("fim") LocalDateTime fim);
    
    /**
     * Busca transações com score de risco acima de um valor
     */
    @Query("SELECT t FROM TransacaoPix t " +
           "WHERE t.scoreRisco >= :scoreMinimo " +
           "ORDER BY t.scoreRisco DESC")
    List<TransacaoPix> findByScoreRiscoGreaterThanEqual(@Param("scoreMinimo") BigDecimal scoreMinimo);
    
    /**
     * Busca transações por tipo
     */
    @Query("SELECT t FROM TransacaoPix t " +
           "WHERE UPPER(t.descricao) LIKE UPPER(CONCAT('%', :tipo, '%')) " +
           "ORDER BY t.dataTransacao DESC")
    List<TransacaoPix> findByTipoTransacao(@Param("tipo") String tipo);
}