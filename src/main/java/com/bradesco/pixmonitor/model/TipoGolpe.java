package com.bradesco.pixmonitor.model;

/**
 * Enum que define os tipos de golpes mais comuns no PIX
 * Cada tipo possui código, nome, descrição, categoria e severidade
 */
public enum TipoGolpe {
    
    // === GOLPES DE RELACIONAMENTO ===
    FALSO_SEQUESTRO("SEQ001", "Falso Sequestro", 
        "Criminoso se passa por sequestrador e exige transferência PIX como 'resgate'", 
        CategoriaGolpe.RELACIONAMENTO, SeveridadeGolpe.CRITICA),
    
    GOLPE_DO_PARENTE("PAR001", "Golpe do Parente", 
        "Criminoso se passa por familiar em emergência pedindo dinheiro via PIX", 
        CategoriaGolpe.RELACIONAMENTO, SeveridadeGolpe.ALTA),
    
    GOLPE_DO_NAMORADO_VIRTUAL("NAM001", "Namorado Virtual", 
        "Relacionamento falso criado para solicitar transferências PIX", 
        CategoriaGolpe.RELACIONAMENTO, SeveridadeGolpe.MEDIA),
    
    CHANTAGEEM_INTIMA("CHA001", "Chantagem Íntima", 
        "Chantagem com conteúdo íntimo exigindo pagamento via PIX", 
        CategoriaGolpe.RELACIONAMENTO, SeveridadeGolpe.CRITICA),
    
    // === GOLPES FINANCEIROS ===
    INVESTIMENTO_FALSO("INV001", "Investimento Falso", 
        "Promessa de investimento com altos retornos usando PIX como meio de pagamento", 
        CategoriaGolpe.FINANCEIRO, SeveridadeGolpe.ALTA),
    
    CASA_DE_APOSTAS("APO001", "Casa de Apostas Falsa", 
        "Plataforma de apostas fraudulenta que não permite saques", 
        CategoriaGolpe.FINANCEIRO, SeveridadeGolpe.ALTA),
    
    EMPRESTIMO_FALSO("EMP001", "Empréstimo Falso", 
        "Oferta de empréstimo que exige PIX como 'taxa' ou 'garantia'", 
        CategoriaGolpe.FINANCEIRO, SeveridadeGolpe.MEDIA),
    
    CARTAO_CREDITO_FALSO("CAR001", "Cartão de Crédito Falso", 
        "Oferta de cartão que exige PIX como taxa de adesão", 
        CategoriaGolpe.FINANCEIRO, SeveridadeGolpe.MEDIA),
    
    CONSORCIO_FALSO("CON001", "Consórcio Falso", 
        "Oferta de consórcio contemplado mediante PIX", 
        CategoriaGolpe.FINANCEIRO, SeveridadeGolpe.MEDIA),
    
    // === GOLPES DE IDENTIDADE ===
    PESSOA_FAMOSA("FAM001", "Pessoa Famosa", 
        "Criminoso se passa por celebridade ou influencer", 
        CategoriaGolpe.IDENTIDADE, SeveridadeGolpe.MEDIA),
    
    FUNCIONARIO_BANCO("BAN001", "Falso Funcionário do Banco", 
        "Criminoso se passa por funcionário do banco solicitando PIX", 
        CategoriaGolpe.IDENTIDADE, SeveridadeGolpe.ALTA),
    
    FUNCIONARIO_GOVERNO("GOV001", "Falso Funcionário do Governo", 
        "Criminoso se passa por funcionário público exigindo pagamento", 
        CategoriaGolpe.IDENTIDADE, SeveridadeGolpe.ALTA),
    
    ADVOGADO_FALSO("ADV001", "Advogado Falso", 
        "Criminoso se passa por advogado cobrando honorários via PIX", 
        CategoriaGolpe.IDENTIDADE, SeveridadeGolpe.MEDIA),
    
    // === GOLPES DE COMPRA E VENDA ===
    VENDA_FALSA("VEN001", "Venda Falsa", 
        "Produto ou serviço que não existe, pagamento via PIX", 
        CategoriaGolpe.COMERCIAL, SeveridadeGolpe.MEDIA),
    
    ENTREGA_FALSA("ENT001", "Entrega Falsa", 
        "Falso entregador cobrando taxa extra via PIX", 
        CategoriaGolpe.COMERCIAL, SeveridadeGolpe.BAIXA),
    
    MARKETPLACE_FALSO("MAR001", "Marketplace Falso", 
        "Loja online fraudulenta que aceita apenas PIX", 
        CategoriaGolpe.COMERCIAL, SeveridadeGolpe.MEDIA),
    
    ALUGUEL_FALSO("ALU001", "Aluguel Falso", 
        "Oferta de imóvel para locação mediante depósito PIX", 
        CategoriaGolpe.COMERCIAL, SeveridadeGolpe.MEDIA),
    
    // === GOLPES TECNOLÓGICOS ===
    PHISHING_PIX("PHI001", "Phishing PIX", 
        "Link falso que captura dados bancários para PIX", 
        CategoriaGolpe.TECNOLOGICO, SeveridadeGolpe.ALTA),
    
    CLONAGEM_WHATSAPP("WHT001", "Clonagem WhatsApp", 
        "WhatsApp clonado usado para solicitar PIX a contatos", 
        CategoriaGolpe.TECNOLOGICO, SeveridadeGolpe.ALTA),
    
    APP_FALSO("APP001", "App Falso", 
        "Aplicativo fraudulento que solicita transferências PIX", 
        CategoriaGolpe.TECNOLOGICO, SeveridadeGolpe.ALTA),
    
    QR_CODE_FALSO("QRC001", "QR Code Malicioso", 
        "QR Code que direciona para transferência PIX fraudulenta", 
        CategoriaGolpe.TECNOLOGICO, SeveridadeGolpe.MEDIA),
    
    // === GOLPES DE SORTEIO E PRÊMIO ===
    SORTEIO_FALSO("SOR001", "Sorteio Falso", 
        "Falso sorteio que exige PIX como 'taxa' para receber prêmio", 
        CategoriaGolpe.PREMIO, SeveridadeGolpe.MEDIA),
    
    PREMIO_FALSO("PRE001", "Prêmio Falso", 
        "Notificação de prêmio que exige PIX para liberação", 
        CategoriaGolpe.PREMIO, SeveridadeGolpe.MEDIA),
    
    RIFA_FALSA("RIF001", "Rifa Falsa", 
        "Rifa inexistente com pagamento apenas via PIX", 
        CategoriaGolpe.PREMIO, SeveridadeGolpe.BAIXA),
    
    // === GOLPES ESPECÍFICOS ===
    AUXILIO_EMERGENCIAL("AUX001", "Auxílio Emergencial Falso", 
        "Oferta de auxílio governamental mediante PIX", 
        CategoriaGolpe.GOVERNO, SeveridadeGolpe.ALTA),
    
    MULTA_FALSA("MUL001", "Multa Falsa", 
        "Cobrança de multa inexistente via PIX", 
        CategoriaGolpe.GOVERNO, SeveridadeGolpe.MEDIA),
    
    DOACAO_FALSA("DOA001", "Doação Falsa", 
        "Solicitação de doação para causa inexistente", 
        CategoriaGolpe.SOCIAL, SeveridadeGolpe.BAIXA),
    
    // === OUTROS ===
    OUTROS("OUT001", "Outros", 
        "Tipo de golpe não categorizado nas opções anteriores", 
        CategoriaGolpe.OUTROS, SeveridadeGolpe.MEDIA);

    private final String codigo;
    private final String nome;
    private final String descricao;
    private final CategoriaGolpe categoria;
    private final SeveridadeGolpe severidade;

    TipoGolpe(String codigo, String nome, String descricao, 
              CategoriaGolpe categoria, SeveridadeGolpe severidade) {
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
        this.severidade = severidade;
    }

    // Getters
    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public CategoriaGolpe getCategoria() { return categoria; }
    public SeveridadeGolpe getSeveridade() { return severidade; }

    /**
     * Retorna o emoji correspondente ao tipo de golpe
     */
    public String getEmoji() {
        return switch (this.categoria) {
            case RELACIONAMENTO -> "💔";
            case FINANCEIRO -> "💰";
            case IDENTIDADE -> "🎭";
            case COMERCIAL -> "🛍️";
            case TECNOLOGICO -> "💻";
            case PREMIO -> "🎁";
            case GOVERNO -> "🏛️";
            case SOCIAL -> "🤝";
            case OUTROS -> "❓";
        };
    }

    /**
     * Retorna a cor CSS correspondente à severidade
     */
    public String getCorSeveridade() {
        return switch (this.severidade) {
            case CRITICA -> "#dc3545"; // Vermelho
            case ALTA -> "#fd7e14";     // Laranja
            case MEDIA -> "#ffc107";    // Amarelo
            case BAIXA -> "#28a745";    // Verde
        };
    }

    /**
     * Busca tipo de golpe por código
     */
    public static TipoGolpe porCodigo(String codigo) {
        for (TipoGolpe tipo : values()) {
            if (tipo.getCodigo().equals(codigo)) {
                return tipo;
            }
        }
        return OUTROS;
    }

    /**
     * Enum para categorias de golpes
     */
    public enum CategoriaGolpe {
        RELACIONAMENTO("Relacionamento"),
        FINANCEIRO("Financeiro"),
        IDENTIDADE("Identidade"),
        COMERCIAL("Comercial"),
        TECNOLOGICO("Tecnológico"),
        PREMIO("Prêmio/Sorteio"),
        GOVERNO("Governo"),
        SOCIAL("Social"),
        OUTROS("Outros");

        private final String nome;

        CategoriaGolpe(String nome) {
            this.nome = nome;
        }

        public String getNome() { return nome; }
    }

    /**
     * Enum para severidade dos golpes
     */
    public enum SeveridadeGolpe {
        CRITICA("Crítica", 4),
        ALTA("Alta", 3),
        MEDIA("Média", 2),
        BAIXA("Baixa", 1);

        private final String nome;
        private final int nivel;

        SeveridadeGolpe(String nome, int nivel) {
            this.nome = nome;
            this.nivel = nivel;
        }

        public String getNome() { return nome; }
        public int getNivel() { return nivel; }
    }
} 