package com.bradesco.pixmonitor.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Entity
@Table(name = "contas_suspeitas")
public class ContaSuspeita {
    
    @Id
    @NotBlank(message = "ID da conta é obrigatório")
    @Size(min = 3, max = 50, message = "ID da conta deve ter entre 3 e 50 caracteres")
    private String idConta;
    
    @Min(value = 0, message = "Número de denúncias deve ser maior ou igual a 0")
    private int numeroDenuncias;
    
    @NotNull(message = "Status de congelamento é obrigatório")
    private boolean congelada;
    
    // Atributos para compatibilidade com PixSuspicionPredictor
    @Column(name = "cpf", length = 11)
    private String cpf;
    
    @Column(name = "nome_titular", length = 100)
    private String nomeTitular;
    
    @Column(name = "valor_total_recebido")
    private double valorTotalRecebido;
    
    @Column(name = "quantidade_recebimentos")
    private int quantidadeRecebimentos;
    
    @Column(name = "quantidade_denuncias")
    private int quantidadeDenuncias;
    
    @Column(name = "tempo_desde_criacao")
    private int tempoDesdeCriacao;
    
    @Column(name = "score")
    private int score;
    
    @Column(name = "suspeita")
    private boolean suspeita;

    // Construtores
    public ContaSuspeita() {}
    
    public ContaSuspeita(String idConta) {
        this.idConta = idConta;
        this.numeroDenuncias = 0;
        this.congelada = false;
    }

    // Getters e setters manuais para garantir compilação
    public String getIdConta() {
        return idConta;
    }

    public void setIdConta(String idConta) {
        this.idConta = idConta;
    }

    public int getNumeroDenuncias() {
        return numeroDenuncias;
    }

    public void setNumeroDenuncias(int numeroDenuncias) {
        this.numeroDenuncias = numeroDenuncias;
    }

    public boolean isCongelada() {
        return congelada;
    }

    public void setCongelada(boolean congelada) {
        this.congelada = congelada;
    }

    // Novos getters e setters
    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNomeTitular() {
        return nomeTitular;
    }

    public void setNomeTitular(String nomeTitular) {
        this.nomeTitular = nomeTitular;
    }

    public double getValorTotalRecebido() {
        return valorTotalRecebido;
    }

    public void setValorTotalRecebido(double valorTotalRecebido) {
        this.valorTotalRecebido = valorTotalRecebido;
    }

    public int getQuantidadeRecebimentos() {
        return quantidadeRecebimentos;
    }

    public void setQuantidadeRecebimentos(int quantidadeRecebimentos) {
        this.quantidadeRecebimentos = quantidadeRecebimentos;
    }

    public int getQuantidadeDenuncias() {
        return quantidadeDenuncias;
    }

    public void setQuantidadeDenuncias(int quantidadeDenuncias) {
        this.quantidadeDenuncias = quantidadeDenuncias;
    }

    public int getTempoDesdeCriacao() {
        return tempoDesdeCriacao;
    }

    public void setTempoDesdeCriacao(int tempoDesdeCriacao) {
        this.tempoDesdeCriacao = tempoDesdeCriacao;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isSuspeita() {
        return suspeita;
    }

    public void setSuspeita(boolean suspeita) {
        this.suspeita = suspeita;
    }
} 