package com.bradesco.pixmonitor.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "contas", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"numero_conta", "agencia"}))
public class Conta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
    
    @Column(name = "numero_conta", nullable = false, length = 20)
    private String numeroConta;
    
    @Column(name = "agencia", nullable = false, length = 10)
    private String agencia;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conta", nullable = false)
    private TipoConta tipoConta = TipoConta.CORRENTE;
    
    @Column(name = "saldo")
    private BigDecimal saldo;
    
    @Column(name = "data_abertura")
    private LocalDateTime dataAbertura;
    
    @Column(name = "status_conta")
    private String statusConta = "ATIVA";
    
    @OneToMany(mappedBy = "contaOrigem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransacaoPix> transacoesEnviadas;
    
    @OneToMany(mappedBy = "contaDestino", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransacaoPix> transacoesRecebidas;
    
    @OneToMany(mappedBy = "contaDenunciada", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Denuncia> denuncias;
    
    public enum TipoConta {
        CORRENTE, POUPANCA
    }
    
    // Constructors
    public Conta() {}
    
    public Conta(Cliente cliente, String numeroConta, String agencia, TipoConta tipoConta) {
        this.cliente = cliente;
        this.numeroConta = numeroConta;
        this.agencia = agencia;
        this.tipoConta = tipoConta;
        this.dataAbertura = LocalDateTime.now();
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
    
    public String getNumeroConta() {
        return numeroConta;
    }
    
    public void setNumeroConta(String numeroConta) {
        this.numeroConta = numeroConta;
    }
    
    public String getAgencia() {
        return agencia;
    }
    
    public void setAgencia(String agencia) {
        this.agencia = agencia;
    }
    
    public TipoConta getTipoConta() {
        return tipoConta;
    }
    
    public void setTipoConta(TipoConta tipoConta) {
        this.tipoConta = tipoConta;
    }
    
    public BigDecimal getSaldo() {
        return saldo;
    }
    
    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }
    
    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }
    
    public void setDataAbertura(LocalDateTime dataAbertura) {
        this.dataAbertura = dataAbertura;
    }
    
    public String getStatusConta() {
        return statusConta;
    }
    
    public void setStatusConta(String statusConta) {
        this.statusConta = statusConta;
    }
    
    public List<TransacaoPix> getTransacoesEnviadas() {
        return transacoesEnviadas;
    }
    
    public void setTransacoesEnviadas(List<TransacaoPix> transacoesEnviadas) {
        this.transacoesEnviadas = transacoesEnviadas;
    }
    
    public List<TransacaoPix> getTransacoesRecebidas() {
        return transacoesRecebidas;
    }
    
    public void setTransacoesRecebidas(List<TransacaoPix> transacoesRecebidas) {
        this.transacoesRecebidas = transacoesRecebidas;
    }
    
    public List<Denuncia> getDenuncias() {
        return denuncias;
    }
    
    public void setDenuncias(List<Denuncia> denuncias) {
        this.denuncias = denuncias;
    }
    
    @PrePersist
    protected void onCreate() {
        dataAbertura = LocalDateTime.now();
        if (statusConta == null) {
            statusConta = "ATIVA";
        }
        if (saldo == null) {
            saldo = BigDecimal.ZERO;
        }
    }
    
    // Métodos auxiliares para identificação
    public String getContaCompleta() {
        return String.format("%s-%s", numeroConta, agencia);
    }
    
    @Override
    public String toString() {
        return "Conta{" +
                "id=" + id +
                ", numeroConta='" + numeroConta + '\'' +
                ", agencia='" + agencia + '\'' +
                ", tipoConta=" + tipoConta +
                ", saldo=" + saldo +
                ", statusConta='" + statusConta + '\'' +
                '}';
    }
} 