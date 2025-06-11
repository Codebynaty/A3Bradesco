package com.bradesco.pixmonitor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Configuration
@ConfigurationProperties(prefix = "bradesco.pix")
@Validated
public class BradescoPixConfig {
    
    // =============================================
    // CONFIGURAÇÕES DE SCORE
    // =============================================
    
    private Score score = new Score();
    
    public static class Score {
        @Min(0) @Max(100)
        private int inicial = 100;
        
        @Min(1) @Max(50)
        private int reducaoDenuncia = 8;
        
        @Min(0) @Max(100)
        private int limiteRiscoAlto = 40;
        
        @Min(0) @Max(100)
        private int limiteRiscoMedio = 70;
        
        @Min(1) @Max(50)
        private int penalizacaoFrequencia = 15;
        
        @Min(1) @Max(50)
        private int penalizacaoTempo = 2;
        
        @Min(1) @Max(100)
        private int penalizacaoContaNova = 20;
        
        @Min(1) @Max(100)
        private int penalizacaoValorAlto = 25;
        
        @Min(1) @Max(100)
        private int penalizacaoPadrao = 20;
        
        // Getters e Setters
        public int getInicial() { return inicial; }
        public void setInicial(int inicial) { this.inicial = inicial; }
        
        public int getReducaoDenuncia() { return reducaoDenuncia; }
        public void setReducaoDenuncia(int reducaoDenuncia) { this.reducaoDenuncia = reducaoDenuncia; }
        
        public int getLimiteRiscoAlto() { return limiteRiscoAlto; }
        public void setLimiteRiscoAlto(int limiteRiscoAlto) { this.limiteRiscoAlto = limiteRiscoAlto; }
        
        public int getLimiteRiscoMedio() { return limiteRiscoMedio; }
        public void setLimiteRiscoMedio(int limiteRiscoMedio) { this.limiteRiscoMedio = limiteRiscoMedio; }
        
        public int getPenalizacaoFrequencia() { return penalizacaoFrequencia; }
        public void setPenalizacaoFrequencia(int penalizacaoFrequencia) { this.penalizacaoFrequencia = penalizacaoFrequencia; }
        
        public int getPenalizacaoTempo() { return penalizacaoTempo; }
        public void setPenalizacaoTempo(int penalizacaoTempo) { this.penalizacaoTempo = penalizacaoTempo; }
        
        public int getPenalizacaoContaNova() { return penalizacaoContaNova; }
        public void setPenalizacaoContaNova(int penalizacaoContaNova) { this.penalizacaoContaNova = penalizacaoContaNova; }
        
        public int getPenalizacaoValorAlto() { return penalizacaoValorAlto; }
        public void setPenalizacaoValorAlto(int penalizacaoValorAlto) { this.penalizacaoValorAlto = penalizacaoValorAlto; }
        
        public int getPenalizacaoPadrao() { return penalizacaoPadrao; }
        public void setPenalizacaoPadrao(int penalizacaoPadrao) { this.penalizacaoPadrao = penalizacaoPadrao; }
    }
    
    // =============================================
    // CONFIGURAÇÕES DE TRANSAÇÃO
    // =============================================
    
    private Transacao transacao = new Transacao();
    
    public static class Transacao {
        @DecimalMin("100.00")
        private BigDecimal valorAlto = new BigDecimal("10000.00");
        
        @Min(1) @Max(365)
        private int diasAnaliseHistorico = 30;
        
        @Min(1) @Max(180)
        private int tempoEntreDenunciasSeguro = 180;
        
        @Min(1) @Max(90)
        private int tempoEntreDenunciasSuspeito = 15;
        
        @DecimalMin("0.1") @DecimalMax("5.0")
        private double limiteFrequenciaDenunciaAlta = 1.0;
        
        @DecimalMin("0.1") @DecimalMax("5.0")
        private double limiteFrequenciaDenunciaMedia = 0.5;
        
        @Min(1) @Max(365)
        private int diasContaNova = 60;
        
        @Min(10) @Max(1000)
        private int limiteRecebimentosContaNova = 100;
        
        @DecimalMin("1000.00")
        private BigDecimal valorLimiteContaNova = new BigDecimal("100000.00");
        
        @Min(1) @Max(365)
        private int diasContaNovaValorAlto = 90;
        
        @Min(1) @Max(50)
        private int limiteDenunciasPadrao = 5;
        
        @DecimalMin("0.1") @DecimalMax("1.0")
        private double limiteFrequenciaPadrao = 0.3;
        
        // Getters e Setters
        public BigDecimal getValorAlto() { return valorAlto; }
        public void setValorAlto(BigDecimal valorAlto) { this.valorAlto = valorAlto; }
        
        public int getDiasAnaliseHistorico() { return diasAnaliseHistorico; }
        public void setDiasAnaliseHistorico(int diasAnaliseHistorico) { this.diasAnaliseHistorico = diasAnaliseHistorico; }
        
        public int getTempoEntreDenunciasSeguro() { return tempoEntreDenunciasSeguro; }
        public void setTempoEntreDenunciasSeguro(int tempoEntreDenunciasSeguro) { this.tempoEntreDenunciasSeguro = tempoEntreDenunciasSeguro; }
        
        public int getTempoEntreDenunciasSuspeito() { return tempoEntreDenunciasSuspeito; }
        public void setTempoEntreDenunciasSuspeito(int tempoEntreDenunciasSuspeito) { this.tempoEntreDenunciasSuspeito = tempoEntreDenunciasSuspeito; }
        
        public double getLimiteFrequenciaDenunciaAlta() { return limiteFrequenciaDenunciaAlta; }
        public void setLimiteFrequenciaDenunciaAlta(double limiteFrequenciaDenunciaAlta) { this.limiteFrequenciaDenunciaAlta = limiteFrequenciaDenunciaAlta; }
        
        public double getLimiteFrequenciaDenunciaMedia() { return limiteFrequenciaDenunciaMedia; }
        public void setLimiteFrequenciaDenunciaMedia(double limiteFrequenciaDenunciaMedia) { this.limiteFrequenciaDenunciaMedia = limiteFrequenciaDenunciaMedia; }
        
        public int getDiasContaNova() { return diasContaNova; }
        public void setDiasContaNova(int diasContaNova) { this.diasContaNova = diasContaNova; }
        
        public int getLimiteRecebimentosContaNova() { return limiteRecebimentosContaNova; }
        public void setLimiteRecebimentosContaNova(int limiteRecebimentosContaNova) { this.limiteRecebimentosContaNova = limiteRecebimentosContaNova; }
        
        public BigDecimal getValorLimiteContaNova() { return valorLimiteContaNova; }
        public void setValorLimiteContaNova(BigDecimal valorLimiteContaNova) { this.valorLimiteContaNova = valorLimiteContaNova; }
        
        public int getDiasContaNovaValorAlto() { return diasContaNovaValorAlto; }
        public void setDiasContaNovaValorAlto(int diasContaNovaValorAlto) { this.diasContaNovaValorAlto = diasContaNovaValorAlto; }
        
        public int getLimiteDenunciasPadrao() { return limiteDenunciasPadrao; }
        public void setLimiteDenunciasPadrao(int limiteDenunciasPadrao) { this.limiteDenunciasPadrao = limiteDenunciasPadrao; }
        
        public double getLimiteFrequenciaPadrao() { return limiteFrequenciaPadrao; }
        public void setLimiteFrequenciaPadrao(double limiteFrequenciaPadrao) { this.limiteFrequenciaPadrao = limiteFrequenciaPadrao; }
    }
    
    // =============================================
    // CONFIGURAÇÕES DE IA
    // =============================================
    
    private Ia ia = new Ia();
    
    public static class Ia {
        @NotBlank
        private String modelo = "J48";
        
        @NotBlank
        private String arquivoTreinamento = "suspect_accounts.arff";
        
        @DecimalMin("0.01") @DecimalMax("0.99")
        private double limiteConfianca = 0.7;
        
        // Getters e Setters
        public String getModelo() { return modelo; }
        public void setModelo(String modelo) { this.modelo = modelo; }
        
        public String getArquivoTreinamento() { return arquivoTreinamento; }
        public void setArquivoTreinamento(String arquivoTreinamento) { this.arquivoTreinamento = arquivoTreinamento; }
        
        public double getLimiteConfianca() { return limiteConfianca; }
        public void setLimiteConfianca(double limiteConfianca) { this.limiteConfianca = limiteConfianca; }
    }
    
    // =============================================
    // CONFIGURAÇÕES DE SEGURANÇA
    // =============================================
    
    private Seguranca seguranca = new Seguranca();
    
    public static class Seguranca {
        @Min(1) @Max(100)
        private int tentativasMaximas = 3;
        
        @Min(1) @Max(3600)
        private int tempoBloqueioBloqueio = 900; // 15 minutos
        
        @NotBlank
        private String algoritmoHash = "SHA-256";
        
        @Min(60) @Max(7200)
        private int tempoSessao = 1800; // 30 minutos
        
        // Getters e Setters
        public int getTentativasMaximas() { return tentativasMaximas; }
        public void setTentativasMaximas(int tentativasMaximas) { this.tentativasMaximas = tentativasMaximas; }
        
        public int getTempoBloqueioBloqueio() { return tempoBloqueioBloqueio; }
        public void setTempoBloqueioBloqueio(int tempoBloqueioBloqueio) { this.tempoBloqueioBloqueio = tempoBloqueioBloqueio; }
        
        public String getAlgoritmoHash() { return algoritmoHash; }
        public void setAlgoritmoHash(String algoritmoHash) { this.algoritmoHash = algoritmoHash; }
        
        public int getTempoSessao() { return tempoSessao; }
        public void setTempoSessao(int tempoSessao) { this.tempoSessao = tempoSessao; }
    }
    
    // =============================================
    // CONFIGURAÇÕES DE INTERFACE
    // =============================================
    
    private Interface interfaceConfig = new Interface();
    
    public static class Interface {
        @NotBlank
        private String logoUrl = "https://logodownload.org/wp-content/uploads/2018/09/bradesco-logo-0.png";
        
        @NotBlank
        private String tituloSistema = "PIX Monitor";
        
        @NotBlank
        private String subtituloSistema = "Sistema de Monitoramento Anti-Fraude";
        
        @NotBlank
        private String descricaoSistema = "Sistema inteligente de detecção e prevenção de fraudes com tecnologia de IA";
        
        @DecimalMin("90.0") @DecimalMax("100.0")
        private double precisaoSistema = 99.8;
        
        @NotBlank
        private String monitoramento = "24/7";
        
        @NotBlank
        private String transacoesSeguras = "1M+";
        
        @NotBlank
        private String corPrimaria = "#cc092f";
        
        @NotBlank
        private String corSecundaria = "#a00724";
        
        // Getters e Setters
        public String getLogoUrl() { return logoUrl; }
        public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
        
        public String getTituloSistema() { return tituloSistema; }
        public void setTituloSistema(String tituloSistema) { this.tituloSistema = tituloSistema; }
        
        public String getSubtituloSistema() { return subtituloSistema; }
        public void setSubtituloSistema(String subtituloSistema) { this.subtituloSistema = subtituloSistema; }
        
        public String getDescricaoSistema() { return descricaoSistema; }
        public void setDescricaoSistema(String descricaoSistema) { this.descricaoSistema = descricaoSistema; }
        
        public double getPrecisaoSistema() { return precisaoSistema; }
        public void setPrecisaoSistema(double precisaoSistema) { this.precisaoSistema = precisaoSistema; }
        
        public String getMonitoramento() { return monitoramento; }
        public void setMonitoramento(String monitoramento) { this.monitoramento = monitoramento; }
        
        public String getTransacoesSeguras() { return transacoesSeguras; }
        public void setTransacoesSeguras(String transacoesSeguras) { this.transacoesSeguras = transacoesSeguras; }
        
        public String getCorPrimaria() { return corPrimaria; }
        public void setCorPrimaria(String corPrimaria) { this.corPrimaria = corPrimaria; }
        
        public String getCorSecundaria() { return corSecundaria; }
        public void setCorSecundaria(String corSecundaria) { this.corSecundaria = corSecundaria; }
    }
    
    // =============================================
    // GETTERS PRINCIPAIS
    // =============================================
    
    public Score getScore() { return score; }
    public void setScore(Score score) { this.score = score; }
    
    public Transacao getTransacao() { return transacao; }
    public void setTransacao(Transacao transacao) { this.transacao = transacao; }
    
    public Ia getIa() { return ia; }
    public void setIa(Ia ia) { this.ia = ia; }
    
    public Seguranca getSeguranca() { return seguranca; }
    public void setSeguranca(Seguranca seguranca) { this.seguranca = seguranca; }
    
    public Interface getInterface() { return interfaceConfig; }
    public void setInterface(Interface interfaceConfig) { this.interfaceConfig = interfaceConfig; }
} 