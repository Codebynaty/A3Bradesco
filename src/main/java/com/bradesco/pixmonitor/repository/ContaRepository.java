package com.bradesco.pixmonitor.repository;

import com.bradesco.pixmonitor.model.Conta;
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
public interface ContaRepository extends JpaRepository<Conta, Long> {
    
    /**
     * Busca conta por número e agência
     */
    Optional<Conta> findByNumeroContaAndAgencia(String numeroConta, String agencia);
    
    /**
     * Busca contas por cliente
     */
    List<Conta> findByCliente(Cliente cliente);
    
    /**
     * Busca contas por cliente ID
     */
    List<Conta> findByClienteId(Long clienteId);
    
    /**
     * Busca contas por agência
     */
    List<Conta> findByAgencia(String agencia);
    
    /**
     * Busca contas por tipo
     */
    List<Conta> findByTipoConta(Conta.TipoConta tipoConta);
    
    /**
     * Busca contas por status
     */
    List<Conta> findByStatusConta(String statusConta);
    
    /**
     * Busca contas com saldo maior que valor
     */
    List<Conta> findBySaldoGreaterThan(BigDecimal valor);
    
    /**
     * Busca contas com saldo menor que valor
     */
    List<Conta> findBySaldoLessThan(BigDecimal valor);
    
    /**
     * Verifica se existe conta com número e agência
     */
    boolean existsByNumeroContaAndAgencia(String numeroConta, String agencia);
    
    /**
     * Busca contas com denúncias pendentes - Corrigido
     */
    @Query("SELECT DISTINCT c FROM Conta c " +
           "JOIN Denuncia d ON d.contaDenunciada = c " +
           "WHERE d.statusDenuncia = 'PENDENTE'")
    List<Conta> findContasComDenunciasPendentes();
    
    /**
     * Busca contas com mais transações que o limite
     */
    @Query("SELECT c FROM Conta c " +
           "WHERE (SELECT COUNT(t) FROM TransacaoPix t WHERE t.contaOrigem = c OR t.contaDestino = c) > :limite")
    List<Conta> findContasComMaisTransacoes(@Param("limite") long limite);
    
    /**
     * Busca contas com transações suspeitas
     */
    @Query("SELECT DISTINCT c FROM Conta c " +
           "JOIN c.transacoesEnviadas t " +
           "WHERE t.statusTransacao IN ('SUSPEITA', 'BLOQUEADA')")
    List<Conta> findContasComTransacoesSuspeitas();
    
    /**
     * Busca contas por CPF do cliente
     */
    @Query("SELECT c FROM Conta c " +
           "JOIN c.cliente cli " +
           "WHERE cli.cpf = :cpf")
    List<Conta> findContasByCpfCliente(@Param("cpf") String cpf);
    
    /**
     * Busca contas ativas de um cliente
     */
    @Query("SELECT c FROM Conta c " +
           "WHERE c.cliente.id = :clienteId " +
           "AND c.statusConta = 'ATIVA'")
    List<Conta> findContasAtivasByClienteId(@Param("clienteId") Long clienteId);
    
    /**
     * Estatísticas de contas por tipo
     */
    @Query("SELECT c.tipoConta, COUNT(c) FROM Conta c GROUP BY c.tipoConta")
    List<Object[]> countContasByTipo();
    
    /**
     * Soma total de saldos por agência
     */
    @Query("SELECT c.agencia, SUM(c.saldo) FROM Conta c GROUP BY c.agencia")
    List<Object[]> sumSaldoByAgencia();
    
    /**
     * Busca contas com valor alto movimentado - Removido query problemática e simplificado
     */
    @Query("SELECT DISTINCT c FROM Conta c " +
           "JOIN TransacaoPix t ON c.id = t.contaOrigem.id " +
           "WHERE t.valor > :valorMinimo")
    List<Conta> findContasComMovimentacaoAlta(@Param("valorMinimo") BigDecimal valorMinimo);
    
    /**
     * Busca contas suspeitas
     */
    @Query("SELECT c FROM Conta c " +
           "WHERE c.statusConta IN ('BLOQUEADA', 'SUSPENSA')")
    List<Conta> findContasSuspeitas();
    
    /**
     * Conta transações por conta - Simplificado
     */
    @Query("SELECT c, " +
           "(SELECT COUNT(t1) FROM TransacaoPix t1 WHERE t1.contaOrigem = c) + " +
           "(SELECT COUNT(t2) FROM TransacaoPix t2 WHERE t2.contaDestino = c) " +
           "FROM Conta c " +
           "ORDER BY (" +
           "(SELECT COUNT(t1) FROM TransacaoPix t1 WHERE t1.contaOrigem = c) + " +
           "(SELECT COUNT(t2) FROM TransacaoPix t2 WHERE t2.contaDestino = c)" +
           ") DESC")
    List<Object[]> countTransacoesPorConta();
    
    /**
     * Busca contas por CPF do cliente
     */
    @Query("SELECT c FROM Conta c " +
           "WHERE c.cliente.cpf = :cpf")
    List<Conta> findByClienteCpf(@Param("cpf") String cpf);
    
    /**
     * Busca contas recém-criadas (últimos 7 dias)
     */
    @Query("SELECT c FROM Conta c " +
           "WHERE c.dataAbertura >= :dataLimite " +
           "ORDER BY c.dataAbertura DESC")
    List<Conta> findContasRecentes(@Param("dataLimite") LocalDateTime dataLimite);
    
    /**
     * Busca contas com maior número de denúncias - Simplificado
     */
    @Query("SELECT c, " +
           "(SELECT COUNT(d) FROM Denuncia d WHERE d.contaDenunciada = c) as numDenuncias " +
           "FROM Conta c " +
           "WHERE (SELECT COUNT(d) FROM Denuncia d WHERE d.contaDenunciada = c) > 0 " +
           "ORDER BY (SELECT COUNT(d) FROM Denuncia d WHERE d.contaDenunciada = c) DESC")
    List<Object[]> findContasComMaisDenuncias();
    
    /**
     * Busca valor total transacionado por conta
     */
    @Query("SELECT c, " +
           "COALESCE((SELECT SUM(t1.valor) FROM TransacaoPix t1 WHERE t1.contaOrigem = c), 0) + " +
           "COALESCE((SELECT SUM(t2.valor) FROM TransacaoPix t2 WHERE t2.contaDestino = c), 0) " +
           "FROM Conta c " +
           "ORDER BY (" +
           "COALESCE((SELECT SUM(t1.valor) FROM TransacaoPix t1 WHERE t1.contaOrigem = c), 0) + " +
           "COALESCE((SELECT SUM(t2.valor) FROM TransacaoPix t2 WHERE t2.contaDestino = c), 0)" +
           ") DESC")
    List<Object[]> findContasPorVolumeTransacionado();
} 