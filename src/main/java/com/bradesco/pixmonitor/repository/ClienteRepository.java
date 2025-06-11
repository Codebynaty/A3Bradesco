package com.bradesco.pixmonitor.repository;

import com.bradesco.pixmonitor.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    /**
     * Busca cliente por CPF
     */
    Optional<Cliente> findByCpf(String cpf);
    
    /**
     * Busca cliente por email
     */
    Optional<Cliente> findByEmail(String email);
    
    /**
     * Busca clientes por nome (busca parcial)
     */
    List<Cliente> findByNomeContainingIgnoreCase(String nome);
    
    /**
     * Busca clientes por status
     */
    List<Cliente> findByStatus(String status);
    
    /**
     * Verifica se existe cliente com CPF
     */
    boolean existsByCpf(String cpf);
    
    /**
     * Verifica se existe cliente com email
     */
    boolean existsByEmail(String email);
    
    /**
     * Busca clientes com scores de risco alto
     */
    @Query("SELECT c FROM Cliente c " +
           "WHERE c.id IN (SELECT sc.cliente.id FROM ScoreConfianca sc WHERE sc.score < 40)")
    List<Cliente> findClientesRiscoAlto();
    
    /**
     * Busca clientes com contas bloqueadas
     */
    @Query("SELECT c FROM Cliente c " +
           "WHERE c.id IN (SELECT sc.cliente.id FROM ScoreConfianca sc WHERE sc.statusConta = 'BLOQUEADA')")
    List<Cliente> findClientesComContasBloqueadas();
    
    /**
     * Busca clientes com mais denúncias que o limite
     */
    @Query("SELECT c FROM Cliente c " +
           "WHERE c.id IN (SELECT sc.cliente.id FROM ScoreConfianca sc WHERE sc.totalDenuncias > :limite)")
    List<Cliente> findClientesComMaisDenuncias(@Param("limite") int limite);
    
    /**
     * Busca clientes ativos com score abaixo do limite
     */
    @Query("SELECT c FROM Cliente c " +
           "WHERE c.status = 'ATIVO' " +
           "AND c.id IN (SELECT sc.cliente.id FROM ScoreConfianca sc WHERE sc.score < :scoreMinimo)")
    List<Cliente> findClientesAtivosComScoreBaixo(@Param("scoreMinimo") int scoreMinimo);
    
    /**
     * Estatísticas de clientes por status
     */
    @Query("SELECT c.status, COUNT(c) FROM Cliente c GROUP BY c.status")
    List<Object[]> countClientesByStatus();
    
    /**
     * Busca clientes com transações recentes - Removido para H2 compatibilidade
     */
    // Query temporariamente removida - problemas de compatibilidade com H2
    // @Query("SELECT DISTINCT c FROM Cliente c " +
    //        "JOIN c.contas conta " +
    //        "JOIN conta.transacoesEnviadas t " +
    //        "WHERE t.dataTransacao >= :dataLimite")
    // List<Cliente> findClientesComTransacoesRecentes(@Param("dataLimite") LocalDateTime dataLimite);
    
    /**
     * Busca clientes por score baixo
     */
    @Query("SELECT c FROM Cliente c " +
           "WHERE c.id IN (SELECT sc.cliente.id FROM ScoreConfianca sc WHERE sc.score < :scoreMaximo ORDER BY sc.score ASC)")
    List<Cliente> findClientesComScoreBaixo(@Param("scoreMaximo") Integer scoreMaximo);
} 