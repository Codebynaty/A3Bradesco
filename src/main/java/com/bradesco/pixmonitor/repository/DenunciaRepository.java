package com.bradesco.pixmonitor.repository;

import com.bradesco.pixmonitor.model.Denuncia;
import com.bradesco.pixmonitor.model.Conta;
import com.bradesco.pixmonitor.model.TransacaoPix;
import com.bradesco.pixmonitor.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {
    
    /**
     * Busca denúncia por protocolo
     */
    Optional<Denuncia> findByProtocolo(String protocolo);
    
    /**
     * Busca denúncias por conta denunciada
     */
    List<Denuncia> findByContaDenunciada(Conta conta);
    
    /**
     * Busca denúncias por transação
     */
    List<Denuncia> findByTransacao(TransacaoPix transacao);
    
    /**
     * Busca denúncias por status
     */
    List<Denuncia> findByStatusDenuncia(Denuncia.StatusDenuncia status);
    
    /**
     * Busca denúncias por período
     */
    List<Denuncia> findByDataDenunciaBetween(LocalDateTime inicio, LocalDateTime fim);
    
    /**
     * Conta denúncias por período - MÉTODO FALTANTE
     */
    long countByDataDenunciaBetween(LocalDateTime inicio, LocalDateTime fim);
    
    /**
     * Busca denúncias por status ordenadas por data - MÉTODO FALTANTE
     */
    List<Denuncia> findByStatusDenunciaOrderByDataDenunciaDesc(Denuncia.StatusDenuncia status);
    
    /**
     * Busca todas as denúncias ordenadas por data - MÉTODO FALTANTE
     */
    List<Denuncia> findAllByOrderByDataDenunciaDesc();
    
    /**
     * Conta denúncias por status - MÉTODO FALTANTE
     */
    long countByStatusDenuncia(Denuncia.StatusDenuncia status);
    
    /**
     * Busca denúncias por funcionário responsável
     */
    List<Denuncia> findByFuncionarioResponsavel(String funcionario);
    
    /**
     * Verifica se existe denúncia com protocolo
     */
    boolean existsByProtocolo(String protocolo);
    
    /**
     * Busca denúncias pendentes
     */
    @Query("SELECT d FROM Denuncia d WHERE d.statusDenuncia = 'PENDENTE' ORDER BY d.dataDenuncia DESC")
    List<Denuncia> findDenunciasPendentes();
    
    /**
     * Busca denúncias em análise
     */
    @Query("SELECT d FROM Denuncia d WHERE d.statusDenuncia = 'EM_ANALISE' ORDER BY d.dataDenuncia DESC")
    List<Denuncia> findDenunciasEmAnalise();
    
    /**
     * Conta denúncias por conta
     */
    @Query("SELECT d.contaDenunciada, COUNT(d) FROM Denuncia d " +
           "GROUP BY d.contaDenunciada " +
           "ORDER BY COUNT(d) DESC")
    List<Object[]> countDenunciasPorConta();
    
    /**
     * Busca denúncias recentes (últimos 7 dias)
     */
    @Query("SELECT d FROM Denuncia d " +
           "WHERE d.dataDenuncia >= :dataLimite " +
           "ORDER BY d.dataDenuncia DESC")
    List<Denuncia> findDenunciasRecentes(@Param("dataLimite") LocalDateTime dataLimite);
    
    /**
     * Busca denúncias por prioridade
     */
    List<Denuncia> findByPrioridadeOrderByDataDenunciaDesc(String prioridade);
    
    /**
     * Busca denúncias por tipo
     */
    List<Denuncia> findByTipoDenunciaOrderByDataDenunciaDesc(String tipoDenuncia);
    
    /**
     * Busca denúncias sem funcionário responsável
     */
    @Query("SELECT d FROM Denuncia d " +
           "WHERE d.funcionarioResponsavel IS NULL " +
           "AND d.statusDenuncia = 'PENDENTE' " +
           "ORDER BY d.prioridade, d.dataDenuncia")
    List<Denuncia> findDenunciasSemResponsavel();
    
    /**
     * Busca estatísticas de denúncias por status
     */
    @Query("SELECT d.statusDenuncia, COUNT(d) FROM Denuncia d " +
           "GROUP BY d.statusDenuncia")
    List<Object[]> getEstatisticasPorStatus();
    
    /**
     * Busca denúncias por cliente
     */
    @Query("SELECT d FROM Denuncia d " +
           "WHERE d.cliente = :cliente " +
           "ORDER BY d.dataDenuncia DESC")
    List<Denuncia> findByCliente(@Param("cliente") Cliente cliente);
    
    /**
     * Busca top contas com mais denúncias
     */
    @Query("SELECT d.contaDenunciada, COUNT(d) as total FROM Denuncia d " +
           "GROUP BY d.contaDenunciada " +
           "ORDER BY COUNT(d) DESC")
    List<Object[]> findTopContasComMaisDenuncias();
    
    /**
     * Busca contas com denúncias pendentes - Versão simplificada para H2
     */
    @Query("SELECT DISTINCT c FROM Conta c " +
           "WHERE c.id IN (SELECT d.contaDenunciada.id FROM Denuncia d WHERE d.statusDenuncia = 'PENDENTE')")
    List<Conta> findContasComDenunciasPendentes();
} 