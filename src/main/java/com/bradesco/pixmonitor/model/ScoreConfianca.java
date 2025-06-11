package com.bradesco.pixmonitor.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "scores_confianca")
public class ScoreConfianca {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false, unique = true)
    private Cliente cliente;
    
    @Column(name = "score", nullable = false)
    private Integer score = 100;
    
    @Column(name = "total_denuncias")
    private Integer totalDenuncias = 0;
    
    @Column(name = "total_transacoes")
    private Integer totalTransacoes = 0;
    
    @Column(name = "valor_total_transacionado")
    private BigDecimal valorTotalTransacionado;
    
    @Column(name = "ultima_atualizacao")
    private LocalDateTime ultimaAtualizacao;
    
    // Campo calculado automaticamente no banco
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_risco", insertable = false, updatable = false)
    private NivelRisco nivelRisco;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status_conta")
    private StatusConta statusConta = StatusConta.NORMAL;
    
    public enum NivelRisco {
        BAIXO, MEDIO, ALTO
    }
    
    public enum StatusConta {
        NORMAL, MONITORADA, BLOQUEADA
    }
    
    // Constructors
    public ScoreConfianca() {}
    
    public ScoreConfianca(Cliente cliente) {
        this.cliente = cliente;
        this.ultimaAtualizacao = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Cliente getCliente() {
        return cliente;
    }
    
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
    
    public Integer getScore() {
        return score;
    }
    
    public void setScore(Integer score) {
        this.score = score;
        // Atualiza status da conta baseado no score
        atualizarStatusConta();
    }
    
    public Integer getTotalDenuncias() {
        return totalDenuncias;
    }
    
    public void setTotalDenuncias(Integer totalDenuncias) {
        this.totalDenuncias = totalDenuncias;
    }
    
    public Integer getTotalTransacoes() {
        return totalTransacoes;
    }
    
    public void setTotalTransacoes(Integer totalTransacoes) {
        this.totalTransacoes = totalTransacoes;
    }
    
    public BigDecimal getValorTotalTransacionado() {
        return valorTotalTransacionado;
    }
    
    public void setValorTotalTransacionado(BigDecimal valorTotalTransacionado) {
        this.valorTotalTransacionado = valorTotalTransacionado;
    }
    
    public LocalDateTime getUltimaAtualizacao() {
        return ultimaAtualizacao;
    }
    
    public void setUltimaAtualizacao(LocalDateTime ultimaAtualizacao) {
        this.ultimaAtualizacao = ultimaAtualizacao;
    }
    
    public NivelRisco getNivelRisco() {
        return nivelRisco;
    }
    
    public void setNivelRisco(NivelRisco nivelRisco) {
        this.nivelRisco = nivelRisco;
    }
    
    public StatusConta getStatusConta() {
        return statusConta;
    }
    
    public void setStatusConta(StatusConta statusConta) {
        this.statusConta = statusConta;
    }
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        ultimaAtualizacao = LocalDateTime.now();
        if (score == null) {
            score = 100;
        }
        if (totalDenuncias == null) {
            totalDenuncias = 0;
        }
        if (totalTransacoes == null) {
            totalTransacoes = 0;
        }
        if (valorTotalTransacionado == null) {
            valorTotalTransacionado = BigDecimal.ZERO;
        }
        atualizarStatusConta();
    }
    
    // Métodos auxiliares
    private void atualizarStatusConta() {
        if (score < 40) {
            statusConta = StatusConta.BLOQUEADA;
        } else if (score < 70) {
            statusConta = StatusConta.MONITORADA;
        } else {
            statusConta = StatusConta.NORMAL;
        }
    }
    
    public NivelRisco calcularNivelRisco() {
        if (score >= 70) {
            return NivelRisco.BAIXO;
        } else if (score >= 40) {
            return NivelRisco.MEDIO;
        } else {
            return NivelRisco.ALTO;
        }
    }
    
    public boolean isContaBloqueada() {
        return statusConta == StatusConta.BLOQUEADA;
    }
    
    public boolean isContaMonitorada() {
        return statusConta == StatusConta.MONITORADA;
    }
    
    public boolean isRiscoAlto() {
        return score < 40;
    }
    
    public boolean isRiscoMedio() {
        return score >= 40 && score < 70;
    }
    
    public boolean isRiscoBaixo() {
        return score >= 70;
    }
    
    // Métodos de atualização
    public void adicionarDenuncia() {
        totalDenuncias++;
        score = Math.max(0, score - 8); // Cada denúncia reduz 8 pontos
        atualizarStatusConta();
        ultimaAtualizacao = LocalDateTime.now();
    }
    
    public void adicionarTransacao(BigDecimal valor) {
        totalTransacoes++;
        valorTotalTransacionado = valorTotalTransacionado.add(valor);
        ultimaAtualizacao = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "ScoreConfianca{" +
                "id=" + id +
                ", score=" + score +
                ", totalDenuncias=" + totalDenuncias +
                ", totalTransacoes=" + totalTransacoes +
                ", statusConta=" + statusConta +
                ", ultimaAtualizacao=" + ultimaAtualizacao +
                '}';
    }
} 