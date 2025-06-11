package com.bradesco.pixmonitor.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "transacoes_pix")
public class TransacaoPix {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_origem_id", nullable = false)
    private Conta contaOrigem;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_destino_id", nullable = false)
    private Conta contaDestino;
    
    @Column(name = "valor", nullable = false)
    private BigDecimal valor;
    
    @Column(name = "data_transacao")
    private LocalDateTime dataTransacao;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusTransacao statusTransacao = StatusTransacao.CONCLUIDA;
    
    @Column(name = "chave_pix_origem")
    private String chavePixOrigem;
    
    @Column(name = "chave_pix_destino")
    private String chavePixDestino;
    
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;
    
    @Column(name = "score_risco")
    private BigDecimal scoreRisco;
    
    @OneToMany(mappedBy = "transacao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Denuncia> denuncias;
    
    public enum StatusTransacao {
        CONCLUIDA, SUSPEITA, NEGADA, BLOQUEADA
    }
    
    // Constructors
    public TransacaoPix() {}
    
    public TransacaoPix(Conta contaOrigem, Conta contaDestino, BigDecimal valor, String descricao) {
        this.contaOrigem = contaOrigem;
        this.contaDestino = contaDestino;
        this.valor = valor;
        this.descricao = descricao;
        this.dataTransacao = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Conta getContaOrigem() {
        return contaOrigem;
    }
    
    public void setContaOrigem(Conta contaOrigem) {
        this.contaOrigem = contaOrigem;
    }
    
    public Conta getContaDestino() {
        return contaDestino;
    }
    
    public void setContaDestino(Conta contaDestino) {
        this.contaDestino = contaDestino;
    }
    
    public BigDecimal getValor() {
        return valor;
    }
    
    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
    
    public LocalDateTime getDataTransacao() {
        return dataTransacao;
    }
    
    public void setDataTransacao(LocalDateTime dataTransacao) {
        this.dataTransacao = dataTransacao;
    }
    
    // Método para compatibilidade com código existente
    public LocalDateTime getDataHora() {
        return dataTransacao;
    }
    
    public void setDataHora(LocalDateTime dataHora) {
        this.dataTransacao = dataHora;
    }
    
    public StatusTransacao getStatusTransacao() {
        return statusTransacao;
    }
    
    public void setStatusTransacao(StatusTransacao statusTransacao) {
        this.statusTransacao = statusTransacao;
    }
    
    public String getChavePixOrigem() {
        return chavePixOrigem;
    }
    
    public void setChavePixOrigem(String chavePixOrigem) {
        this.chavePixOrigem = chavePixOrigem;
    }
    
    public String getChavePixDestino() {
        return chavePixDestino;
    }
    
    public void setChavePixDestino(String chavePixDestino) {
        this.chavePixDestino = chavePixDestino;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public BigDecimal getScoreRisco() {
        return scoreRisco;
    }
    
    public void setScoreRisco(BigDecimal scoreRisco) {
        this.scoreRisco = scoreRisco;
    }
    
    // Método para compatibilidade
    public BigDecimal getScoreConfianca() {
        return scoreRisco;
    }
    
    public void setScoreConfianca(BigDecimal scoreConfianca) {
        this.scoreRisco = scoreConfianca;
    }
    
    public List<Denuncia> getDenuncias() {
        return denuncias;
    }
    
    public void setDenuncias(List<Denuncia> denuncias) {
        this.denuncias = denuncias;
    }
    
    @PrePersist
    protected void onCreate() {
        dataTransacao = LocalDateTime.now();
        if (statusTransacao == null) {
            statusTransacao = StatusTransacao.CONCLUIDA;
        }
        if (scoreRisco == null) {
            scoreRisco = BigDecimal.valueOf(100);
        }
    }
    
    // Métodos auxiliares
    public boolean isSuspeita() {
        return statusTransacao == StatusTransacao.SUSPEITA || statusTransacao == StatusTransacao.BLOQUEADA;
    }
    
    public boolean isBloqueada() {
        return statusTransacao == StatusTransacao.BLOQUEADA;
    }
    
    @Override
    public String toString() {
        return "TransacaoPix{" +
                "id=" + id +
                ", valor=" + valor +
                ", dataTransacao=" + dataTransacao +
                ", statusTransacao=" + statusTransacao +
                ", scoreRisco=" + scoreRisco +
                '}';
    }
} 