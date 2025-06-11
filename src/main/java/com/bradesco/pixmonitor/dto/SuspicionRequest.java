package com.bradesco.pixmonitor.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class SuspicionRequest {
    
    @NotNull(message = "Quantidade de denúncias é obrigatória")
    @Min(value = 0, message = "Quantidade de denúncias deve ser maior ou igual a 0")
    private int quantidadeDenuncias;
    
    @NotNull(message = "Tempo entre denúncias é obrigatório")
    @Min(value = 0, message = "Tempo entre denúncias deve ser maior ou igual a 0")
    private int tempoEntreDenuncias;
    
    @NotNull(message = "Frequência de denúncias é obrigatória")
    @DecimalMin(value = "0.0", message = "Frequência de denúncias deve ser maior ou igual a 0")
    private double frequenciaDenuncias;
    
    @NotNull(message = "Quantidade de recebimentos é obrigatória")
    @Min(value = 0, message = "Quantidade de recebimentos deve ser maior ou igual a 0")
    private int quantidadeRecebimentos;
    
    @NotNull(message = "Valor total recebido é obrigatório")
    @DecimalMin(value = "0.0", message = "Valor total recebido deve ser maior ou igual a 0")
    private double valorTotalRecebido;
    
    @NotNull(message = "Tempo desde criação é obrigatório")
    @Min(value = 0, message = "Tempo desde criação deve ser maior ou igual a 0")
    private int tempoDesdeCriacao;

    // Getters e Setters manuais para garantir compilação
    public int getQuantidadeDenuncias() {
        return quantidadeDenuncias;
    }

    public void setQuantidadeDenuncias(int quantidadeDenuncias) {
        this.quantidadeDenuncias = quantidadeDenuncias;
    }

    public int getTempoEntreDenuncias() {
        return tempoEntreDenuncias;
    }

    public void setTempoEntreDenuncias(int tempoEntreDenuncias) {
        this.tempoEntreDenuncias = tempoEntreDenuncias;
    }

    public double getFrequenciaDenuncias() {
        return frequenciaDenuncias;
    }

    public void setFrequenciaDenuncias(double frequenciaDenuncias) {
        this.frequenciaDenuncias = frequenciaDenuncias;
    }

    public int getQuantidadeRecebimentos() {
        return quantidadeRecebimentos;
    }

    public void setQuantidadeRecebimentos(int quantidadeRecebimentos) {
        this.quantidadeRecebimentos = quantidadeRecebimentos;
    }

    public double getValorTotalRecebido() {
        return valorTotalRecebido;
    }

    public void setValorTotalRecebido(double valorTotalRecebido) {
        this.valorTotalRecebido = valorTotalRecebido;
    }

    public int getTempoDesdeCriacao() {
        return tempoDesdeCriacao;
    }

    public void setTempoDesdeCriacao(int tempoDesdeCriacao) {
        this.tempoDesdeCriacao = tempoDesdeCriacao;
    }
} 