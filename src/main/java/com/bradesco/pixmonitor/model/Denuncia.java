package com.bradesco.pixmonitor.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "denuncias")
public class Denuncia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transacao_id")
    private TransacaoPix transacao;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_denunciada_id", nullable = false)
    private Conta contaDenunciada;
    
    @NotBlank(message = "Motivo é obrigatório")
    @Size(min = 10, max = 1000, message = "Motivo deve ter entre 10 e 1000 caracteres")
    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String motivo;
    
    @Column(name = "data_denuncia")
    private LocalDateTime dataDenuncia;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status_denuncia")
    private StatusDenuncia statusDenuncia = StatusDenuncia.PENDENTE;
    
    @Column(name = "funcionario_responsavel")
    private String funcionarioResponsavel;
    
    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;
    
    @Column(name = "protocolo", unique = true, length = 50)
    private String protocolo;
    
    @Column(name = "tipo_denuncia")
    private String tipoDenuncia;
    
    @Column(name = "evidencias", columnDefinition = "TEXT")
    private String evidencias;
    
    @Column(name = "prioridade")
    private String prioridade = "MEDIA";
    
    // Campo para relacionamento com Cliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
    
    // Campos legados para compatibilidade
    @Transient
    private String idDenuncia;
    
    @Transient
    private String idConta;
    
    @Transient
    private LocalDateTime dataHora;
    
    @Transient
    private String descricao;
    
    public enum StatusDenuncia {
        PENDENTE, EM_ANALISE, RESOLVIDA, ARQUIVADA
    }
    
    // Constructors
    public Denuncia() {}
    
    public Denuncia(Conta contaDenunciada, String motivo) {
        this.contaDenunciada = contaDenunciada;
        this.motivo = motivo;
        this.dataDenuncia = LocalDateTime.now();
        this.protocolo = gerarProtocolo();
    }
    
    public Denuncia(TransacaoPix transacao, Conta contaDenunciada, String motivo) {
        this.transacao = transacao;
        this.contaDenunciada = contaDenunciada;
        this.motivo = motivo;
        this.dataDenuncia = LocalDateTime.now();
        this.protocolo = gerarProtocolo();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public TransacaoPix getTransacao() {
        return transacao;
    }
    
    public void setTransacao(TransacaoPix transacao) {
        this.transacao = transacao;
    }
    
    public Conta getContaDenunciada() {
        return contaDenunciada;
    }
    
    public void setContaDenunciada(Conta contaDenunciada) {
        this.contaDenunciada = contaDenunciada;
    }
    
    public Cliente getCliente() {
        return cliente;
    }
    
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
    
    public String getMotivo() {
        return motivo;
    }
    
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
    
    public LocalDateTime getDataDenuncia() {
        return dataDenuncia;
    }
    
    public void setDataDenuncia(LocalDateTime dataDenuncia) {
        this.dataDenuncia = dataDenuncia;
    }
    
    public StatusDenuncia getStatusDenuncia() {
        return statusDenuncia;
    }
    
    public void setStatusDenuncia(StatusDenuncia statusDenuncia) {
        this.statusDenuncia = statusDenuncia;
    }
    
    public String getFuncionarioResponsavel() {
        return funcionarioResponsavel;
    }
    
    public void setFuncionarioResponsavel(String funcionarioResponsavel) {
        this.funcionarioResponsavel = funcionarioResponsavel;
    }
    
    public String getObservacoes() {
        return observacoes;
    }
    
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
    
    public String getProtocolo() {
        return protocolo;
    }
    
    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }
    
    public String getTipoDenuncia() {
        return tipoDenuncia;
    }
    
    public void setTipoDenuncia(String tipoDenuncia) {
        this.tipoDenuncia = tipoDenuncia;
    }
    
    public String getEvidencias() {
        return evidencias;
    }
    
    public void setEvidencias(String evidencias) {
        this.evidencias = evidencias;
    }
    
    public String getPrioridade() {
        return prioridade;
    }
    
    public void setPrioridade(String prioridade) {
        this.prioridade = prioridade;
    }
    
    // Métodos legados para compatibilidade
    public String getIdDenuncia() {
        return protocolo != null ? protocolo : String.valueOf(id);
    }
    
    public void setIdDenuncia(String idDenuncia) {
        this.idDenuncia = idDenuncia;
        if (protocolo == null) {
            this.protocolo = idDenuncia;
        }
    }
    
    public String getIdConta() {
        return contaDenunciada != null ? contaDenunciada.getContaCompleta() : null;
    }
    
    public void setIdConta(String idConta) {
        this.idConta = idConta;
    }
    
    public LocalDateTime getDataHora() {
        return dataDenuncia;
    }
    
    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
        this.dataDenuncia = dataHora;
    }
    
    public String getDescricao() {
        return motivo;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
        this.motivo = descricao;
    }
    
    @PrePersist
    protected void onCreate() {
        dataDenuncia = LocalDateTime.now();
        if (protocolo == null) {
            protocolo = gerarProtocolo();
        }
        if (statusDenuncia == null) {
            statusDenuncia = StatusDenuncia.PENDENTE;
        }
    }
    
    // Métodos auxiliares
    private String gerarProtocolo() {
        return String.format("PROT-%d-%s", 
                System.currentTimeMillis() % 100000,
                UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }
    
    public boolean isPendente() {
        return statusDenuncia == StatusDenuncia.PENDENTE;
    }
    
    public boolean isEmAnalise() {
        return statusDenuncia == StatusDenuncia.EM_ANALISE;
    }
    
    public boolean isResolvida() {
        return statusDenuncia == StatusDenuncia.RESOLVIDA;
    }
    
    public boolean isArquivada() {
        return statusDenuncia == StatusDenuncia.ARQUIVADA;
    }
    
    @Override
    public String toString() {
        return "Denuncia{" +
                "id=" + id +
                ", protocolo='" + protocolo + '\'' +
                ", motivo='" + motivo + '\'' +
                ", dataDenuncia=" + dataDenuncia +
                ", statusDenuncia=" + statusDenuncia +
                '}';
    }
} 