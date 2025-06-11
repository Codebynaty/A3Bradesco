// Estado da aplicação
let currentSection = 'dashboard';
let isBalanceVisible = true;
let userCPF = '12345678901'; // CPF simulado do usuário
let securityScore = 11; // Score de segurança inicial (BAIXO = ALTO RISCO)

// Dados simulados
const userData = {
    name: 'João Silva',
    account: '1234',
    agency: '12345-6',
    balance: 2847.53,
    cpf: '12345678901'
};

// Inicialização
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
    updateSecurityScore();
    initializeBinoAssistant();
});

// Inicialização da aplicação
function initializeApp() {
    showSection('dashboard');
    loadUserData();
    setupEventListeners();
}

// Configurar event listeners
function setupEventListeners() {
    // Não há listeners específicos necessários no carregamento inicial
}

// Carrega dados do usuário
function loadUserData() {
    document.getElementById('userName').textContent = userData.name;
    updateBalanceDisplay();
}

// Navegação entre seções
function showSection(section) {
    // Remove active de todas as seções
    document.querySelectorAll('.banking-section').forEach(s => {
        s.classList.remove('active');
    });
    
    // Remove active de todos os nav items
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
    });
    
    // Adiciona active na seção atual
    const currentSectionElement = document.getElementById(section);
    if (currentSectionElement) {
        currentSectionElement.classList.add('active');
    }
    
    // Adiciona active no nav item atual
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach((item, index) => {
        const sections = ['dashboard', 'pix', 'extratos', 'seguranca'];
        if (sections[index] === section) {
            item.classList.add('active');
        }
    });
    
    currentSection = section;
}

// Toggle visualização do saldo
function toggleBalance() {
    isBalanceVisible = !isBalanceVisible;
    updateBalanceDisplay();
}

function updateBalanceDisplay() {
    const balanceElement = document.querySelector('.amount');
    const eyeIcon = document.querySelector('.balance-header i');
    
    if (isBalanceVisible) {
        balanceElement.textContent = userData.balance.toLocaleString('pt-BR', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
        eyeIcon.className = 'fas fa-eye';
    } else {
        balanceElement.textContent = '•••••••';
        eyeIcon.className = 'fas fa-eye-slash';
    }
}

// PIX Functions
function showPixTransfer() {
    showSection('pix');
    document.getElementById('pixTransferForm').style.display = 'block';
}

function hidePixTransfer() {
    document.getElementById('pixTransferForm').style.display = 'none';
}

function showPixReceive() {
    showNotification('Funcionalidade de recebimento PIX será implementada em breve', 'info');
}

function showPixKeys() {
    showNotification('Gerenciamento de chaves PIX será implementado em breve', 'info');
}

function showPixHistory() {
    showNotification('Histórico PIX será implementado em breve', 'info');
}

// Processar PIX com análise de IA PRÉ-TRANSAÇÃO
async function processPix(event) {
    event.preventDefault();
    
    const pixKey = document.getElementById('pixKey').value.trim();
    const amount = parseFloat(document.getElementById('pixAmount').value);
    const description = document.getElementById('pixDescription').value.trim();
    
    if (!pixKey || !amount || amount <= 0) {
        showNotification('Preencha todos os campos obrigatórios', 'warning');
        return;
    }
    
    try {
        showLoading('🔍 Analisando segurança da transação...');
        
        // =============================================
        // BINO: MENSAGEM DE ANÁLISE INICIAL
        // =============================================
        showBinoAnalysisMessage('🔍 Analisando os riscos da sua transação PIX...');
        
        // =============================================
        // ANÁLISE PRÉ-TRANSAÇÃO (NOVO ENDPOINT)
        // =============================================
        
        const dadosAnalise = {
            chavePix: pixKey,
            valor: amount.toString(),
            descricao: description,
            contaOrigemId: "1" // Simular conta do usuário logado
        };
        
        console.log('🔄 Enviando para análise pré-transação:', dadosAnalise);
        
        // BINO: Mensagem durante análise
        setTimeout(() => {
            showBinoAnalysisMessage('🤖 IA verificando padrões de fraude...');
        }, 1500);
        
        const responseAnalise = await fetch('/api/pix/analisar-pre-transacao', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(dadosAnalise)
        });
        
        const resultadoAnalise = await responseAnalise.json();
        
        console.log('📊 Resultado da análise:', resultadoAnalise);
        
        hideLoading();
        
        // =============================================
        // PROCESSAR RESULTADO DA ANÁLISE
        // =============================================
        
        if (!resultadoAnalise.sucesso) {
            console.error('❌ Erro na análise:', resultadoAnalise.erro);
            showNotification('Erro na análise de segurança: ' + resultadoAnalise.erro, 'error');
            return;
        }
        
        const scoreAtual = resultadoAnalise.scoreAtual;
        const nivelRisco = resultadoAnalise.nivelRisco;
        const acao = resultadoAnalise.acao;
        const permitirTransacao = resultadoAnalise.permitirTransacao;
        
        console.log(`📊 Score: ${scoreAtual}/100, Risco: ${nivelRisco}, Ação: ${acao}`);
        
        // =============================================
        // DECISÃO BASEADA NO SCORE (QUANTO MENOR, MAIOR O RISCO)
        // =============================================
        
        if (acao === 'BLOQUEAR' || !permitirTransacao) {
            // TRANSAÇÃO BLOQUEADA - Score muito baixo
            console.log('🚫 TRANSAÇÃO BLOQUEADA - Score crítico:', scoreAtual);
            
            // BINO: Mensagem de bloqueio
            showBinoResultMessage('🚫 Transação BLOQUEADA por alto risco de fraude!', 'blocked');
            
            showBlockedTransactionModal(pixKey, amount, description, {
                isBlocked: true,
                riskScore: scoreAtual,
                riskFactors: resultadoAnalise.fatoresRisco || [
                    `Score crítico: ${scoreAtual}/100`,
                    'Alto risco de fraude detectado',
                    'Transação bloqueada automaticamente'
                ],
                motivoBloqueio: resultadoAnalise.motivoBloqueio,
                recomendacoes: resultadoAnalise.recomendacoes
            });
            
            // Atualizar score local para refletir o risco
            securityScore = scoreAtual;
            updateSecurityScore();
            
            return;
            
        } else if (acao === 'SUSPENDER') {
            // TRANSAÇÃO SUSPEITA - Score baixo
            console.log('⚠️ TRANSAÇÃO SUSPEITA - Score baixo:', scoreAtual);
            
            // BINO: Mensagem de suspensão
            showBinoResultMessage('⚠️ Transação SUSPENSA para análise manual!', 'warning');
            
            showSuspiciousTransactionModal(pixKey, amount, description, {
                riskScore: scoreAtual,
                motivoSuspensao: resultadoAnalise.motivoBloqueio,
                fatoresRisco: resultadoAnalise.fatoresRisco,
                recomendacoes: resultadoAnalise.recomendacoes
            });
            
            return;
            
        } else if (acao === 'APROVAR_MANUAL') {
            // REQUER APROVAÇÃO MANUAL - Score médio + valor alto
            console.log('📋 REQUER APROVAÇÃO MANUAL - Score médio + valor alto:', scoreAtual);
            
            // BINO: Mensagem de aprovação manual
            showBinoResultMessage('📋 Requer aprovação manual por valor alto!', 'warning');
            
            showManualApprovalModal(pixKey, amount, description, {
                riskScore: scoreAtual,
                fatoresRisco: resultadoAnalise.fatoresRisco,
                recomendacoes: resultadoAnalise.recomendacoes
            });
            
            return;
            
        } else if (acao === 'MONITORAR') {
            // MONITORAMENTO ESPECIAL - Score médio
            console.log('📊 MONITORAMENTO ESPECIAL - Score médio:', scoreAtual);
            
            // BINO: Mensagem de monitoramento
            showBinoResultMessage('📊 Transação aprovada com monitoramento especial!', 'success');
            
            showLoading('📊 Processando com monitoramento especial...');
            
            // Processar com delay para simular análise adicional
            await delay(3000);
            
        } else {
            // APROVADO - Score alto (baixo risco)
            console.log('✅ TRANSAÇÃO APROVADA - Score alto:', scoreAtual);
            
            // BINO: Mensagem de aprovação
            showBinoResultMessage('✅ Transação APROVADA! Tudo seguro!', 'success');
            
            showLoading('✅ Processando transação aprovada...');
        }
        
        // =============================================
        // PROCESSAR TRANSAÇÃO (se chegou até aqui)
        // =============================================
        
        console.log('💳 Iniciando processamento real da transação no banco...');
        
        // Extrair CPF da chave PIX para encontrar conta destino
        const cpfDestino = extractCPFFromKey(pixKey);
        const contaDestinoId = extractAccountIdFromCPF(cpfDestino);
        
        const dadosProcessamento = {
            contaOrigemId: "1", // Conta do usuário logado
            contaDestinoId: contaDestinoId.toString(),
            valor: amount.toString(),
            descricao: description || 'Transferência PIX'
        };
        
        console.log('📤 Enviando para processamento:', dadosProcessamento);
        
        try {
            const responseProcessamento = await fetch('/api/pix/processar', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(dadosProcessamento)
            });
            
            const resultadoProcessamento = await responseProcessamento.json();
            console.log('✅ Resultado do processamento:', resultadoProcessamento);
        
        hideLoading();
        
            if (resultadoProcessamento.sucesso) {
                console.log('💾 Transação salva no banco! ID:', resultadoProcessamento.transacaoId);
                
                // Verificar se a transação foi bloqueada
                if (resultadoProcessamento.status === 'BLOQUEADA') {
                    console.log('🚫 Transação BLOQUEADA por risco crítico');
                    console.log('📊 Score real do backend:', resultadoProcessamento.score);
                    
                    // BINO: Mensagem final de bloqueio
                    showBinoResultMessage('🛡️ Sua conta foi protegida! Transação bloqueada automaticamente.', 'blocked');
                    
                    // Usar score real retornado pelo backend (não o score da análise prévia)
                    const scoreRealDestino = resultadoProcessamento.score || 1;
                    
                    // Mostrar modal de transação bloqueada com dados corretos do backend
                    showBlockedTransactionModal(cpfDestino, amount, description, {
                        riskScore: scoreRealDestino, // ✅ Score correto do backend
                        riskFactors: [
                            `Score crítico detectado na conta destino: ${scoreRealDestino}/100`,
                            `Transação de R$ ${amount.toFixed(2)} bloqueada automaticamente`,
                            'Sistema de IA classificou como FRAUDE POTENCIAL',
                            'Denúncia automática criada para verificação manual',
                            `Protocolo: ${resultadoProcessamento.protocoloDenuncia || 'N/A'}`
                        ],
                        isBlocked: true,
                        transacaoId: resultadoProcessamento.transacaoId,
                        protocoloDenuncia: resultadoProcessamento.protocoloDenuncia || 'Verificar logs do sistema',
                        valorBloqueado: amount,
                        dataHora: new Date(resultadoProcessamento.dataHora).toLocaleString('pt-BR')
                    });
                    
                } else {
                    // Transação aprovada e processada com sucesso
                    console.log('✅ Transação APROVADA e processada com sucesso');
                    
                    // BINO: Mensagem final de sucesso
                    showBinoResultMessage('🎉 PIX realizado com sucesso! Tudo certo na sua transação!', 'success');
                    
                    showPixSuccessModal(resultadoProcessamento.transacaoId, {
            tipo: 'PIX_PROCESSADO',
                        prioridade: 'NORMAL',
            scoreSeguranca: scoreAtual,
            nivelRisco: nivelRisco,
            acao: acao,
                        status: resultadoProcessamento.status,
                        valor: amount,
                        dataHora: new Date(resultadoProcessamento.dataHora).toLocaleString('pt-BR'),
                        chavePix: pixKey,
                        info: {
                            mensagem: `Transação processada com sucesso no banco - ID: ${resultadoProcessamento.transacaoId}`,
                            protocolo_acesso: `Status: ${resultadoProcessamento.status}`,
                            tempo_analise: 'Processamento em tempo real',
                            contato: 'Transação registrada no sistema Bradesco'
                        }
                    });
                }
                
            } else {
                throw new Error(resultadoProcessamento.erro || 'Erro no processamento');
            }
            
        } catch (errorProcessamento) {
            console.error('❌ Erro no processamento da transação:', errorProcessamento);
            hideLoading();
            
            // Mesmo com erro, mostrar modal de sucesso pois a análise passou
            // mas alertar que pode haver delay no processamento
            showPixSuccessModal(generateTransactionId(), {
                tipo: 'PIX_PENDENTE',
                prioridade: 'ALTA',
                scoreSeguranca: scoreAtual,
                nivelRisco: nivelRisco,
                acao: acao,
                valor: amount,
                chavePix: pixKey,
            dataHora: new Date().toLocaleString('pt-BR'),
            info: {
                    mensagem: 'Transação aprovada - processamento pode ter delay técnico',
                    protocolo_acesso: `Análise realizada com sucesso`,
                    tempo_analise: 'Processamento assíncrono',
                    contato: 'Entre em contato se não receber confirmação em 10 minutos'
            }
        });
                 }
        
        // Atualizar score local
        securityScore = scoreAtual;
        updateSecurityScore();
        
        hidePixTransfer();
        
    } catch (error) {
        console.error('❌ Erro ao processar PIX:', error);
        hideLoading();
        showNotification('Erro ao processar transação: ' + error.message, 'error');
    }
}

// Extrai CPF da chave PIX (simula diferentes tipos de chaves)
function extractCPFFromKey(pixKey) {
    // Remove formatação se for CPF
    if (pixKey.match(/^\d{3}\.\d{3}\.\d{3}-\d{2}$/)) {
        return pixKey.replace(/[.-]/g, '');
    }
    
    // Se for CPF sem formatação
    if (pixKey.match(/^\d{11}$/)) {
        return pixKey;
    }
    
    // Para outros tipos de chave, simula um CPF de risco alto
    return '49650556474'; // CPF que resultará em bloqueio
}

// Análise de segurança por IA
async function analyzeTransactionSecurity(cpf, amount, description) {
    // Simula chamada para a API de análise de IA
    await delay(2000); // Simula processamento
    
    try {
        const response = await fetch('/api/predict-suspicion', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                quantidadeDenuncias: Math.floor(Math.random() * 5) + 3, // 3-7
                tempoEntreDenuncias: Math.floor(Math.random() * 48) + 1, // 1-48 horas
                frequenciaDenuncias: Math.random() * 0.5 + 0.3, // 0.3-0.8
                quantidadeRecebimentos: Math.floor(Math.random() * 20) + 10, // 10-29
                valorTotalRecebido: amount * (Math.random() * 10 + 5), // 5-15x o valor
                tempoDesdeCriacao: Math.floor(Math.random() * 30) + 1 // 1-30 dias
            })
        });
        
        if (response.ok) {
            const result = await response.json();
            // Se a API retorna true para conta suspeita, queremos score baixo (alto risco)
            const riskScore = result.contaSuspeita ? 11 : 85;
            return {
                isBlocked: result.contaSuspeita,
                riskScore: riskScore,
                riskFactors: result.contaSuspeita ? [
                    'Conta criada recentemente',
                    'Score de risco elevado',
                    'Frequência de denúncias',
                    'Quantidade recebimentos'
                ] : []
            };
        }
    } catch (error) {
        console.log('Usando análise simulada devido a erro na API');
    }
    
    // Análise simulada local
    const riskFactors = [];
    let riskScore = 100; // Começa com score alto (baixo risco)
    
    // Fatores de risco baseados no CPF - REDUZEM o score (aumentam o risco)
    if (cpf === '49650556474') {
        riskScore = 11; // Score muito baixo = Alto risco
        riskFactors.push('Conta criada recentemente');
        riskFactors.push('Score de risco elevado');
        riskFactors.push('Frequência de denúncias');
        riskFactors.push('Quantidade recebimentos');
    } else if (cpf.startsWith('123') || cpf.startsWith('111')) {
        riskScore = 25; // Score baixo = Médio risco
        riskFactors.push('Padrão suspeito no CPF');
        riskFactors.push('Histórico de transações irregulares');
    } else {
        riskScore = Math.floor(Math.random() * 30) + 70; // 70-99 (baixo risco)
    }
    
    // Fatores baseados no valor - REDUZEM o score
    if (amount > 1000) {
        riskScore -= 15;
        riskFactors.push('Valor elevado da transação');
    }
    
    // Fatores baseados na descrição - REDUZEM o score
    if (description && description.toLowerCase().includes('teste')) {
        riskScore -= 20;
        riskFactors.push('Descrição suspeita');
    }
    
    // Garante que o score não seja menor que 1
    riskScore = Math.max(riskScore, 1);
    
    return {
        isBlocked: riskScore < 30, // Score baixo = Alto risco = Bloquear
        riskScore: riskScore,
        riskFactors: riskFactors
    };
}

// Mostra modal de transação bloqueada
function showBlockedTransactionModal(cpf, amount, description, analysis) {
    const modal = document.getElementById('blockedModal');
    
    // Atualiza dados da transação
    document.getElementById('blockedCpf').textContent = formatCPF(cpf);
    document.getElementById('blockedAmount').textContent = `R$ ${amount.toFixed(2).replace('.', ',')}`;
    document.getElementById('blockedDescription').textContent = description || 'Transferência PIX';
    
    // Atualiza score de segurança
    const scoreElements = modal.querySelectorAll('.score-number');
    scoreElements.forEach(el => {
        el.textContent = `${analysis.riskScore}/100`;
    });
    
    // Atualiza fatores de risco
    const riskList = modal.querySelector('.risk-factors ul');
    riskList.innerHTML = '';
    analysis.riskFactors.forEach(factor => {
        const li = document.createElement('li');
        li.innerHTML = `<i class="fas fa-exclamation-circle"></i> ${factor}`;
        riskList.appendChild(li);
    });
    
    // Adicionar informações extras se transação foi processada mas bloqueada
    if (analysis.transacaoId) {
        const existingInfo = modal.querySelector('.transaction-info');
        if (existingInfo) {
            existingInfo.remove();
        }
        
        const infoDiv = document.createElement('div');
        infoDiv.className = 'transaction-info';
        infoDiv.style.cssText = `
            background: #fff3cd;
            border: 1px solid #ffeaa7;
            border-radius: 8px;
            padding: 1rem;
            margin: 1rem 0;
            color: #856404;
        `;
        
        infoDiv.innerHTML = `
            <h5>📋 Informações da Transação</h5>
            <p><strong>ID da Transação:</strong> ${analysis.transacaoId}</p>
            <p><strong>Status:</strong> <span style="color: #d73527; font-weight: bold;">BLOQUEADA</span></p>
            <p><strong>Score Real do Sistema:</strong> <span style="color: #d73527; font-weight: bold;">${analysis.riskScore}/100</span></p>
            <p><strong>Valor:</strong> R$ ${analysis.valorBloqueado ? analysis.valorBloqueado.toFixed(2).replace('.', ',') : '0,00'}</p>
            <p><strong>Data/Hora:</strong> ${analysis.dataHora || new Date().toLocaleString('pt-BR')}</p>
            ${analysis.protocoloDenuncia ? `<p><strong>Protocolo de Denúncia:</strong> <span style="color: #007bff; font-weight: bold;">${analysis.protocoloDenuncia}</span></p>` : ''}
            <p><small>⚠️ Esta transação foi registrada no sistema mas bloqueada automaticamente devido ao score crítico detectado (≤30 pontos).</small></p>
        `;
        
        // Inserir após os dados da transação
        const transactionData = modal.querySelector('.blocked-transaction');
        if (transactionData) {
            transactionData.parentNode.insertBefore(infoDiv, transactionData.nextSibling);
        }
    }
    
    modal.style.display = 'flex';
}

function closeBlockedModal() {
    document.getElementById('blockedModal').style.display = 'none';
}

async function reportSuspiciousTransaction() {
    closeBlockedModal();
    
    // Carregar tipos de golpes antes de abrir o modal
    await carregarTiposGolpes();
    
    document.getElementById('reportModal').style.display = 'flex';
}

function closeReportModal() {
    document.getElementById('reportModal').style.display = 'none';
}

// Submeter denúncia com tipo de golpe
async function submitReport(event) {
    event.preventDefault();
    
    const description = document.getElementById('reportDescription').value.trim();
    const tipoGolpe = document.getElementById('tipoGolpeSelecionado').value;
    const contactMethods = Array.from(document.querySelectorAll('input[name="contactMethod"]:checked'))
        .map(cb => cb.value);
    
    // Validações
    if (!tipoGolpe) {
        showNotification('⚠️ Selecione o tipo de golpe', 'warning');
        return;
    }
    
    if (!description || description.length < 20) {
        showNotification('⚠️ A descrição deve ter pelo menos 20 caracteres', 'warning');
        return;
    }
    
    try {
        showLoading('📋 Enviando denúncia...');
        
        // Obter dados do modal bloqueado para a denúncia
        const cpfElement = document.getElementById('blockedCpf');
        const amountElement = document.getElementById('blockedAmount');
        const descriptionElement = document.getElementById('blockedDescription');
        
        // Buscar informações do tipo de golpe selecionado
        const tipoSelecionado = tiposGolpesData.tipos.find(t => t.codigo === tipoGolpe);
        
        const denunciaData = {
            tipoTransacao: 'PIX',
            tipoGolpe: tipoGolpe,
            contaId: extractAccountIdFromCPF(cpfElement ? cpfElement.textContent : ''),
            valorTransacao: amountElement ? amountElement.textContent.replace('R$ ', '').replace(',', '.') : '0',
            motivoDenuncia: description,
            evidencias: contactMethods.join(', '),
            denunciante: 'Cliente via Interface Web',
            canaisContato: contactMethods,
            detalhesTransacao: {
                chavePix: cpfElement ? cpfElement.textContent : '',
                valorFormatado: amountElement ? amountElement.textContent : 'R$ 0,00',
                descricaoTransacao: descriptionElement ? descriptionElement.textContent : ''
            }
        };
        
        console.log('🔄 Enviando denúncia com tipo de golpe:', denunciaData);
        console.log('📊 Tipo selecionado:', tipoSelecionado);
        
        // Enviar para a API de denúncias manuais
        const response = await fetch('/api/denuncias/manual', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(denunciaData)
        });
        
        const result = await response.json();
        
        if (response.ok && result.success) {
            console.log('✅ Denúncia criada com sucesso:', result);
            
            closeReportModal();
            
            // Incluir informações do tipo de golpe no modal de sucesso
            const detalhesCompletos = {
                tipo: result.tipo || 'DENUNCIA_GOLPE',
                prioridade: result.prioridade || 'MEDIA',
                dataHora: result.dataHora,
                tipoGolpe: result.tipoGolpe || tipoSelecionado,
                info: result.info || {
                    mensagem: 'Denúncia registrada com sucesso',
                    protocolo_acesso: 'Use o protocolo para acompanhamento',
                    tempo_analise: 'Será analisada em breve',
                    contato: 'Entre em contato se necessário'
                }
            };
            
            showReportSuccessModal(result.protocolo, detalhesCompletos);
            
            showNotification(`✅ Denúncia de ${tipoSelecionado ? tipoSelecionado.nome : 'golpe'} registrada!`, 'success');
            
            // Atualizar score de segurança (reduzir após denúncia)
            updateSecurityScoreAfterReport();
            
        } else {
            console.error('❌ Erro na resposta da API:', result);
            throw new Error(result.error || 'Erro desconhecido na API');
        }
        
    } catch (error) {
        console.error('❌ Erro ao enviar denúncia:', error);
        
        // Tentar salvar denúncia localmente como fallback
        try {
            const protocoloLocal = generateProtocolNumber();
            console.log('🔄 Salvando denúncia localmente:', protocoloLocal);
            
            closeReportModal();
            
            const tipoSelecionado = tiposGolpesData.tipos.find(t => t.codigo === tipoGolpe);
            
            showReportSuccessModal(protocoloLocal, {
                tipo: 'DENUNCIA_LOCAL',
                prioridade: 'MEDIA',
                dataHora: new Date().toLocaleString('pt-BR'),
                tipoGolpe: tipoSelecionado,
                info: {
                    mensagem: `Denúncia de ${tipoSelecionado ? tipoSelecionado.nome : 'golpe'} salva localmente.`,
                    protocolo_acesso: 'Use o protocolo ' + protocoloLocal + ' para referência.',
                    tempo_analise: 'Aguarde a sincronização com o servidor.',
                    contato: 'Tente novamente mais tarde ou entre em contato com o suporte.'
                }
            });
            
            showNotification('⚠️ Denúncia salva localmente. Sincronização pendente.', 'warning');
            
        } catch (fallbackError) {
            console.error('❌ Erro no fallback:', fallbackError);
            showNotification('❌ Erro ao processar denúncia: ' + error.message, 'error');
        }
        
    } finally {
        hideLoading();
    }
}

// Função para extrair ID da conta a partir do CPF (mapeamento real dos dados do banco)
function extractAccountIdFromCPF(cpf) {
    if (!cpf || cpf === '***.***.***-**') {
        return '2'; // Maria Oliveira (conta padrão para testes)
    }
    
    // Remover formatação do CPF
    const cleanCpf = cpf.replace(/[^0-9]/g, '');
    
    // Mapeamento real baseado nos dados do banco MySQL
    const cpfToAccountMap = {
        '12345678901': '1', // João Silva Santos
        '23456789012': '2', // Maria Oliveira Costa
        '34567890123': '3', // Pedro Rodrigues Lima  
        '45678901234': '4', // Ana Carolina Mendes
        '56789012345': '5', // Carlos Eduardo Ferreira
        '67890123456': '6', // Fernanda Alves Pereira (se existir)
        '78901234567': '7', // Conta adicional (se existir)
        '89012345678': '8'  // Conta adicional (se existir)
    };
    
    // Retornar o ID da conta correspondente ao CPF
    const contaId = cpfToAccountMap[cleanCpf];
    if (contaId) {
        console.log(`✅ CPF ${cleanCpf} mapeado para conta ID: ${contaId}`);
        return contaId;
    }
    
    // Se não encontrar o CPF, usar conta padrão (Maria Oliveira)
    console.log(`⚠️ CPF ${cleanCpf} não encontrado, usando conta padrão ID: 2`);
    return '2';
}

// Atualizar score após denúncia
function updateSecurityScoreAfterReport() {
    // Reduzir score quando há denúncia (simula o comportamento do backend)
    securityScore = Math.max(0, securityScore - 8); // Redução padrão de 8 pontos
    updateSecurityScore();
    
    console.log('📊 Score atualizado após denúncia:', securityScore);
}

// Modal específico para PIX
function showPixSuccessModal(transactionId, detalhes = null) {
    console.log('🔄 showPixSuccessModal chamada com:', { transactionId, detalhes });
    
    const modal = document.getElementById('pixSuccessModal');
    if (!modal) {
        console.error('❌ Modal pixSuccessModal não encontrado!');
        return;
    }
    
    // Atualizar elementos do modal PIX
    const titleElement = document.getElementById('pixSuccessTitle');
    const messageElement = document.getElementById('pixSuccessMessage');
    const protocolElement = document.getElementById('pixProtocolNumber');
    const subMessageElement = document.getElementById('pixSuccessSubMessage');
    
    if (titleElement) {
        titleElement.textContent = detalhes && detalhes.tipo === 'PIX_PENDENTE' 
            ? 'PIX Aprovado - Processamento Pendente'
            : 'PIX Realizado com Sucesso!';
    }
    
    if (messageElement) {
        messageElement.textContent = detalhes && detalhes.tipo === 'PIX_PENDENTE'
            ? 'Sua transferência foi aprovada, mas pode haver delay no processamento.'
            : 'Sua transferência foi processada com sucesso.';
    }
    
    if (protocolElement) {
        protocolElement.textContent = transactionId;
    }
    
    if (subMessageElement) {
        subMessageElement.textContent = detalhes && detalhes.tipo === 'PIX_PENDENTE'
            ? 'Entre em contato se não receber confirmação em 10 minutos.'
            : 'A transferência foi registrada no sistema.';
    }
    
    // Adicionar detalhes se fornecidos
    if (detalhes && typeof detalhes === 'object') {
        const modalContent = modal.querySelector('.modal-content');
        if (!modalContent) {
            console.error('❌ .modal-content não encontrado no modal PIX!');
            modal.style.display = 'flex';
            return;
        }
        
        // Remover detalhes anteriores se existirem
        const existingDetails = modal.querySelector('.pix-details');
        if (existingDetails) {
            existingDetails.remove();
        }
        
        // Criar seção de detalhes PIX
        const detailsDiv = document.createElement('div');
        detailsDiv.className = 'pix-details';
        
        // Dados da transação PIX
        const valor = detalhes.valor ? `R$ ${parseFloat(detalhes.valor).toFixed(2).replace('.', ',')}` : 'N/A';
        const chavePix = detalhes.chavePix || 'N/A';
        const status = detalhes.status || 'PROCESSADA';
        const dataHora = detalhes.dataHora || new Date().toLocaleString('pt-BR');
        
        detailsDiv.innerHTML = `
            <div class="details-section" style="background: #f8f9fa; padding: 1rem; border-radius: 8px; margin: 1rem 0;">
                <h4>💰 Detalhes da Transação</h4>
                <div class="detail-item" style="margin: 0.5rem 0;">
                    <strong>Tipo:</strong> PIX_PROCESSADO
                </div>
                <div class="detail-item" style="margin: 0.5rem 0;">
                    <strong>Valor:</strong> ${valor}
                </div>
                <div class="detail-item" style="margin: 0.5rem 0;">
                    <strong>Chave PIX:</strong> ${chavePix}
                </div>
                <div class="detail-item" style="margin: 0.5rem 0;">
                    <strong>Status:</strong> <span style="color: #28a745; font-weight: bold;">${status}</span>
                </div>
                <div class="detail-item" style="margin: 0.5rem 0;">
                    <strong>Data/Hora:</strong> ${dataHora}
                </div>
                ${detalhes.info && typeof detalhes.info === 'object' ? `
                    <div class="info-section" style="margin-top: 1rem; padding-top: 1rem; border-top: 1px solid #dee2e6;">
                        <h5>ℹ️ Informações Importantes</h5>
                        <p style="margin: 0.5rem 0;">${detalhes.info.mensagem || ''}</p>
                        <p style="margin: 0.5rem 0;">${detalhes.info.protocolo_acesso || ''}</p>
                        <p style="margin: 0.5rem 0;"><strong>Tempo de Análise:</strong> ${detalhes.info.tempo_analise || 'N/A'}</p>
                        <p style="margin: 0.5rem 0;"><strong>Contato:</strong> ${detalhes.info.contato || 'N/A'}</p>
                    </div>
                ` : ''}
            </div>
        `;
        
        // Inserir antes das ações do modal
        try {
            const modalActions = modalContent.querySelector('.modal-actions');
            if (modalActions && modalContent.contains(modalActions)) {
                console.log('🔄 Inserindo detalhes PIX antes das ações do modal');
                modalContent.insertBefore(detailsDiv, modalActions);
            } else {
                console.log('⚠️ Modal actions não encontrado, inserindo no final');
                modalContent.appendChild(detailsDiv);
            }
        } catch (error) {
            console.error('❌ Erro ao inserir elemento PIX, usando appendChild como fallback:', error);
            try {
                modalContent.appendChild(detailsDiv);
            } catch (appendError) {
                console.error('❌ Erro crítico ao adicionar elemento PIX:', appendError);
            }
        }
    }
    
    modal.style.display = 'flex';
}

// Modal específico para denúncias com informações do tipo de golpe
function showReportSuccessModal(protocolNumber, detalhes = null) {
    console.log('🔄 showReportSuccessModal chamada com:', { protocolNumber, detalhes });
    
    const modal = document.getElementById('reportSuccessModal');
    if (!modal) {
        console.error('❌ Modal reportSuccessModal não encontrado!');
        return;
    }
    
    const protocolElement = document.getElementById('reportProtocolNumber');
    if (protocolElement) {
        protocolElement.textContent = protocolNumber;
    }
    
    // Atualizar título se tipoGolpe estiver presente
    if (detalhes && detalhes.tipoGolpe) {
        const titleElement = modal.querySelector('h2');
        if (titleElement) {
            titleElement.innerHTML = `${detalhes.tipoGolpe.emoji || '📋'} Denúncia Registrada`;
        }
    }
    
    // Adicionar detalhes se fornecidos
    if (detalhes && typeof detalhes === 'object') {
        const modalContent = modal.querySelector('.modal-content');
        if (!modalContent) {
            console.error('❌ .modal-content não encontrado no modal de denúncia!');
            modal.style.display = 'flex';
            return;
        }
        
        // Remover detalhes anteriores se existirem
        const existingDetails = modal.querySelector('.denuncia-details');
        if (existingDetails) {
            existingDetails.remove();
        }
        
        // Criar seção de detalhes da denúncia
        const detailsDiv = document.createElement('div');
        detailsDiv.className = 'denuncia-details';
        
        const tipo = detalhes.tipo || 'Não especificado';
        const prioridade = detalhes.prioridade || 'Normal';
        const prioridadeClass = (detalhes.prioridade && typeof detalhes.prioridade === 'string') 
            ? detalhes.prioridade.toLowerCase() 
            : 'normal';
        const dataHora = detalhes.dataHora || new Date().toLocaleString('pt-BR');
        
        // Informações do tipo de golpe se disponível
        const tipoGolpeInfo = detalhes.tipoGolpe ? `
            <div class="tipo-golpe-info" style="
                background: linear-gradient(135deg, ${detalhes.tipoGolpe.corSeveridade || '#007bff'}15, ${detalhes.tipoGolpe.corSeveridade || '#007bff'}05);
                border-left: 4px solid ${detalhes.tipoGolpe.corSeveridade || '#007bff'};
                padding: 1rem;
                border-radius: 8px;
                margin-bottom: 1rem;
            ">
                <h4 style="margin: 0 0 0.5rem 0; color: ${detalhes.tipoGolpe.corSeveridade || '#007bff'};">
                    ${detalhes.tipoGolpe.emoji || '⚠️'} ${detalhes.tipoGolpe.nome || 'Tipo de Golpe'}
                </h4>
                <p style="margin: 0; font-size: 0.9rem; color: #666;">
                    ${detalhes.tipoGolpe.descricao || 'Tipo de golpe selecionado na denúncia'}
                </p>
                <div style="margin-top: 0.5rem; font-size: 0.8rem;">
                    <span style="background: ${detalhes.tipoGolpe.corSeveridade || '#007bff'}; color: white; padding: 0.2rem 0.5rem; border-radius: 12px; margin-right: 0.5rem;">
                        ${detalhes.tipoGolpe.severidade || 'Média'}
                    </span>
                    <span style="color: #666;">
                        📂 ${detalhes.tipoGolpe.categoria || 'Categoria'}
                    </span>
                </div>
            </div>
        ` : '';
        
        detailsDiv.innerHTML = `
            ${tipoGolpeInfo}
            <div class="details-section" style="background: #f8f9fa; padding: 1rem; border-radius: 8px; margin: 1rem 0;">
                <h4>📋 Detalhes da Denúncia</h4>
                <div class="detail-item" style="margin: 0.5rem 0;">
                    <strong>Tipo:</strong> ${tipo}
                </div>
                <div class="detail-item" style="margin: 0.5rem 0;">
                    <strong>Prioridade:</strong> <span class="priority-${prioridadeClass}" style="
                        background: ${getPriorityColor(prioridade)};
                        color: white;
                        padding: 0.2rem 0.5rem;
                        border-radius: 12px;
                        font-size: 0.8rem;
                    ">${prioridade}</span>
                </div>
                <div class="detail-item" style="margin: 0.5rem 0;">
                    <strong>Data/Hora:</strong> ${dataHora}
                </div>
                ${detalhes.info && typeof detalhes.info === 'object' ? `
                    <div class="info-section" style="margin-top: 1rem; padding-top: 1rem; border-top: 1px solid #dee2e6;">
                        <h5>ℹ️ Informações Importantes</h5>
                        <p style="margin: 0.5rem 0;">${detalhes.info.mensagem || ''}</p>
                        <p style="margin: 0.5rem 0;">${detalhes.info.protocolo_acesso || ''}</p>
                        <p style="margin: 0.5rem 0;"><strong>Tempo de Análise:</strong> ${detalhes.info.tempo_analise || 'N/A'}</p>
                        <p style="margin: 0.5rem 0;"><strong>Contato:</strong> ${detalhes.info.contato || 'N/A'}</p>
                    </div>
                ` : ''}
            </div>
        `;
        
        // Inserir antes das ações do modal
        try {
            const modalActions = modalContent.querySelector('.modal-actions');
            if (modalActions && modalContent.contains(modalActions)) {
                console.log('🔄 Inserindo detalhes da denúncia antes das ações do modal');
                modalContent.insertBefore(detailsDiv, modalActions);
            } else {
                console.log('⚠️ Modal actions não encontrado, inserindo no final');
                modalContent.appendChild(detailsDiv);
            }
        } catch (error) {
            console.error('❌ Erro ao inserir elemento denúncia, usando appendChild como fallback:', error);
            try {
                modalContent.appendChild(detailsDiv);
            } catch (appendError) {
                console.error('❌ Erro crítico ao adicionar elemento denúncia:', appendError);
            }
        }
    }
    
    modal.style.display = 'flex';
}

// Função auxiliar para cores de prioridade
function getPriorityColor(prioridade) {
    const priority = (prioridade || 'MEDIA').toUpperCase();
    switch (priority) {
        case 'URGENTE': return '#dc3545';
        case 'ALTA': return '#fd7e14';
        case 'MEDIA': return '#ffc107';
        case 'BAIXA': return '#28a745';
        default: return '#6c757d';
    }
}

// Fechar modal PIX
function closePixSuccessModal() {
    const modal = document.getElementById('pixSuccessModal');
    if (modal) {
        modal.style.display = 'none';
        
        // Limpar detalhes anteriores
        const existingDetails = modal.querySelector('.pix-details');
        if (existingDetails) {
            existingDetails.remove();
        }
    }
}

// Fechar modal de denúncia
function closeReportSuccessModal() {
    const modal = document.getElementById('reportSuccessModal');
    if (modal) {
        modal.style.display = 'none';
        
        // Limpar detalhes anteriores
        const existingDetails = modal.querySelector('.denuncia-details');
        if (existingDetails) {
            existingDetails.remove();
        }
    }
}

// Atualiza score de segurança
function updateSecurityScore() {
    const scoreElement = document.getElementById('securityScore').querySelector('.score-number');
    const statusElement = document.getElementById('scoreStatus');
    
    if (scoreElement) {
        scoreElement.textContent = securityScore;
    }
    
    if (statusElement) {
        // Nova lógica: Score baixo = Alto risco
        if (securityScore < 30) {
            statusElement.textContent = 'ALTO RISCO';
            statusElement.className = 'score-status high-risk';
        } else if (securityScore < 60) {
            statusElement.textContent = 'MÉDIO RISCO';
            statusElement.className = 'score-status medium-risk';
        } else {
            statusElement.textContent = 'BAIXO RISCO';
            statusElement.className = 'score-status low-risk';
        }
    }
    
    // Atualiza a descrição baseada no novo score
    const scoreDescription = document.querySelector('.score-description');
    if (scoreDescription) {
        if (securityScore < 30) {
            scoreDescription.textContent = 'Sua conta apresenta fatores de alto risco. Recomendamos atenção redobrada e verificação de todas as transações.';
        } else if (securityScore < 60) {
            scoreDescription.textContent = 'Sua conta apresenta alguns fatores de risco. Mantenha-se vigilante com suas transações PIX.';
        } else {
            scoreDescription.textContent = 'Sua conta apresenta baixo risco. Continue seguindo as boas práticas de segurança.';
        }
    }
}

// Logout
function logout() {
    if (confirm('Tem certeza que deseja sair?')) {
        window.location.href = 'index.html';
    }
}

// Funções auxiliares
function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

function generateTransactionId() {
    return 'TXN' + Date.now().toString().slice(-8);
}

function generateProtocolNumber() {
    return 'D' + String(Math.floor(Math.random() * 9999) + 1).padStart(4, '0');
}

function formatCPF(cpf) {
    if (!cpf) return '***.***.***-**';
    
    // Se já está formatado, retorna como está
    if (cpf.includes('.')) return cpf;
    
    // Formata CPF
    return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
}

function showLoading(message = 'Processando...') {
    // Cria overlay de loading
    const loadingOverlay = document.createElement('div');
    loadingOverlay.id = 'loadingOverlay';
    loadingOverlay.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 3000;
        color: white;
        font-size: 1.2rem;
        font-weight: 600;
    `;
    
    loadingOverlay.innerHTML = `
        <div style="text-align: center;">
            <i class="fas fa-spinner fa-spin" style="font-size: 2rem; margin-bottom: 1rem;"></i>
            <div>${message}</div>
        </div>
    `;
    
    document.body.appendChild(loadingOverlay);
}

function hideLoading() {
    const loadingOverlay = document.getElementById('loadingOverlay');
    if (loadingOverlay) {
        loadingOverlay.remove();
    }
}

function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.style.cssText = `
        position: fixed;
        top: 90px;
        right: 20px;
        padding: 1rem 1.5rem;
        background: ${type === 'success' ? 'var(--success)' : 
                     type === 'error' ? 'var(--danger)' : 
                     type === 'warning' ? 'var(--warning)' : 'var(--info)'};
        color: white;
        border-radius: 10px;
        z-index: 3000;
        box-shadow: 0 8px 25px rgba(0,0,0,0.2);
        transition: all 0.3s ease;
        display: flex;
        align-items: center;
        gap: 0.5rem;
        max-width: 400px;
        transform: translateX(100%);
        opacity: 0;
    `;
    
    const icon = type === 'success' ? 'check-circle' : 
                 type === 'error' ? 'exclamation-circle' : 
                 type === 'warning' ? 'exclamation-triangle' : 'info-circle';
    
    notification.innerHTML = `
        <i class="fas fa-${icon}"></i>
        <span>${message}</span>
    `;
    
    document.body.appendChild(notification);
    
    // Animação de entrada
    setTimeout(() => {
        notification.style.transform = 'translateX(0)';
        notification.style.opacity = '1';
    }, 100);
    
    // Remover após 4 segundos
    setTimeout(() => {
        notification.style.opacity = '0';
        notification.style.transform = 'translateX(100%)';
        setTimeout(() => notification.remove(), 300);
    }, 4000);
}

// Estilos adicionais para elementos criados dinamicamente
const additionalStyles = `
    .score-status.medium-risk {
        background: rgba(255, 193, 7, 0.1);
        color: var(--warning);
    }
    
    .score-status.low-risk {
        background: rgba(40, 167, 69, 0.1);
        color: var(--success);
    }
    
    #loadingOverlay {
        animation: fadeIn 0.3s ease;
    }
    
    .notification {
        animation: slideInRight 0.3s ease;
    }
    
    @keyframes slideInRight {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
`;

// Injetar estilos adicionais
const styleSheet = document.createElement('style');
styleSheet.textContent = additionalStyles;
document.head.appendChild(styleSheet);

// Mostra modal de transação suspeita (score baixo mas não crítico)
function showSuspiciousTransactionModal(pixKey, amount, description, analysis) {
    console.log('⚠️ Exibindo modal de transação suspeita');
    
    // Reutilizar o modal bloqueado mas com texto diferente
    const modal = document.getElementById('blockedModal');
    
    // Atualizar título
    const title = modal.querySelector('h2');
    if (title) {
        title.innerHTML = '⚠️ Transação Suspeita';
        title.style.color = '#f39c12'; // Cor laranja para suspeito
    }
    
    // Atualizar dados da transação
    document.getElementById('blockedCpf').textContent = formatCPF(pixKey);
    document.getElementById('blockedAmount').textContent = `R$ ${amount.toFixed(2).replace('.', ',')}`;
    document.getElementById('blockedDescription').textContent = description || 'Transferência PIX';
    
    // Atualizar score de segurança
    const scoreElements = modal.querySelectorAll('.score-number');
    scoreElements.forEach(el => {
        el.textContent = `${analysis.riskScore}/100`;
        el.style.color = '#f39c12'; // Cor laranja
    });
    
    // Atualizar fatores de risco
    const riskList = modal.querySelector('.risk-factors ul');
    riskList.innerHTML = '';
    analysis.fatoresRisco.forEach(factor => {
        const li = document.createElement('li');
        li.innerHTML = `<i class="fas fa-exclamation-triangle" style="color: #f39c12;"></i> ${factor}`;
        riskList.appendChild(li);
    });
    
    // Adicionar informações específicas de suspensão
    const suspensionInfo = document.createElement('div');
    suspensionInfo.className = 'suspension-info';
    suspensionInfo.style.cssText = `
        margin-top: 1rem;
        padding: 1rem;
        background: rgba(243, 156, 18, 0.1);
        border-left: 4px solid #f39c12;
        border-radius: 5px;
    `;
    suspensionInfo.innerHTML = `
        <h4 style="color: #f39c12; margin: 0 0 0.5rem 0;">📋 Análise Necessária</h4>
        <p style="margin: 0; font-size: 0.9rem;">
            ${analysis.motivoSuspensao || 'Esta transação será analisada pela equipe de segurança.'}
        </p>
        <p style="margin: 0.5rem 0 0 0; font-size: 0.8rem; color: #666;">
            ⏱️ Tempo estimado: 2-24 horas • 📞 Você será notificado do resultado
        </p>
    `;
    
    // Inserir informações no modal
    const modalContent = modal.querySelector('.modal-content');
    const existingSuspension = modal.querySelector('.suspension-info');
    if (existingSuspension) {
        existingSuspension.remove();
    }
    
    // Inserir com verificação de segurança
    const modalActions = modal.querySelector('.modal-actions');
    if (modalActions && modalContent.contains(modalActions)) {
        modalContent.insertBefore(suspensionInfo, modalActions);
    } else {
        // Se não encontrar modal-actions, adicionar no final
        modalContent.appendChild(suspensionInfo);
    }
    
    modal.style.display = 'flex';
}

// Mostra modal de aprovação manual necessária
function showManualApprovalModal(pixKey, amount, description, analysis) {
    console.log('📋 Exibindo modal de aprovação manual');
    
    // Criar modal específico se não existir
    let modal = document.getElementById('manualApprovalModal');
    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'manualApprovalModal';
        modal.className = 'modal';
        modal.style.display = 'none';
        
        modal.innerHTML = `
            <div class="modal-content" style="max-width: 500px;">
                <h2 style="color: #3498db; margin-bottom: 1rem;">
                    📋 Aprovação Manual Necessária
                </h2>
                
                <div class="approval-info" style="
                    padding: 1rem;
                    background: rgba(52, 152, 219, 0.1);
                    border-left: 4px solid #3498db;
                    border-radius: 5px;
                    margin-bottom: 1rem;
                ">
                    <h4 style="color: #3498db; margin: 0 0 0.5rem 0;">🔍 Análise Detalhada</h4>
                    <p style="margin: 0; font-size: 0.9rem;">
                        Esta transação requer aprovação manual devido ao valor e score de segurança.
                    </p>
                </div>
                
                <div class="transaction-details">
                    <h4>📋 Detalhes da Transação</h4>
                    <div class="detail-row">
                        <span>💰 Valor:</span>
                        <span id="approvalAmount"></span>
                    </div>
                    <div class="detail-row">
                        <span>🔑 Chave PIX:</span>
                        <span id="approvalPixKey"></span>
                    </div>
                    <div class="detail-row">
                        <span>📊 Score Atual:</span>
                        <span id="approvalScore"></span>
                    </div>
                </div>
                
                <div class="risk-factors" style="margin: 1rem 0;">
                    <h4>⚠️ Fatores Identificados</h4>
                    <ul id="approvalFactors"></ul>
                </div>
                
                <div class="approval-options" style="
                    display: flex;
                    gap: 1rem;
                    margin-top: 1.5rem;
                ">
                    <button class="btn btn-success" onclick="requestManualApproval()" style="flex: 1;">
                        📨 Solicitar Aprovação
                    </button>
                    <button class="btn btn-secondary" onclick="closeManualApprovalModal()" style="flex: 1;">
                        ❌ Cancelar
                    </button>
                </div>
            </div>
        `;
        
        document.body.appendChild(modal);
    }
    
    // Preencher dados
    document.getElementById('approvalAmount').textContent = `R$ ${amount.toFixed(2).replace('.', ',')}`;
    document.getElementById('approvalPixKey').textContent = pixKey;
    document.getElementById('approvalScore').textContent = `${analysis.riskScore}/100`;
    
    // Preencher fatores de risco
    const factorsList = document.getElementById('approvalFactors');
    factorsList.innerHTML = '';
    analysis.fatoresRisco.forEach(factor => {
        const li = document.createElement('li');
        li.innerHTML = `<i class="fas fa-info-circle" style="color: #3498db;"></i> ${factor}`;
        factorsList.appendChild(li);
    });
    
    modal.style.display = 'flex';
}

function closeManualApprovalModal() {
    const modal = document.getElementById('manualApprovalModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

async function requestManualApproval() {
    try {
        showLoading('📨 Enviando solicitação de aprovação...');
        
        // Simular envio da solicitação
        await delay(2000);
        
        hideLoading();
        closeManualApprovalModal();
        
        showNotification('✅ Solicitação enviada! Você será notificado em até 2 horas.', 'success');
        
    } catch (error) {
        hideLoading();
        showNotification('❌ Erro ao enviar solicitação: ' + error.message, 'error');
    }
}

// =============================================
// SISTEMA AVANÇADO DE DENÚNCIAS COM TIPOS DE GOLPES
// =============================================

// Estado global para tipos de golpes
let tiposGolpesData = {
    tipos: [],
    categorias: {},
    carregado: false
};

// Carregar tipos de golpes do backend
async function carregarTiposGolpes() {
    if (tiposGolpesData.carregado) {
        return tiposGolpesData;
    }
    
    try {
        console.log('🔄 Carregando tipos de golpes...');
        
        const response = await fetch('/api/tipos-golpes/por-categoria', {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error(`Erro HTTP: ${response.status}`);
        }
        
        const data = await response.json();
        
        if (data.success) {
            tiposGolpesData.categorias = data.categorias;
            tiposGolpesData.tipos = [];
            
            // Flatten tipos para acesso direto
            Object.values(data.categorias).forEach(categoria => {
                tiposGolpesData.tipos.push(...categoria.tipos);
            });
            
            tiposGolpesData.carregado = true;
            
            console.log('✅ Tipos de golpes carregados:', {
                totalCategorias: Object.keys(tiposGolpesData.categorias).length,
                totalTipos: tiposGolpesData.tipos.length
            });
            
            // Renderizar seletor no modal
            renderizarSeletorGolpes();
            
        } else {
            throw new Error(data.error || 'Erro desconhecido ao carregar tipos');
        }
        
    } catch (error) {
        console.error('❌ Erro ao carregar tipos de golpes:', error);
        
        // Fallback com tipos básicos
        tiposGolpesData = {
            tipos: [
                { codigo: 'OUT001', nome: 'Outros', categoria: 'Outros', emoji: '❓', severidade: 'Média' }
            ],
            categorias: {
                OUTROS: {
                    nome: 'Outros',
                    emoji: '❓',
                    tipos: [{ codigo: 'OUT001', nome: 'Outros', categoria: 'Outros', emoji: '❓', severidade: 'Média' }]
                }
            },
            carregado: true
        };
        
        renderizarSeletorGolpes();
        showNotification('⚠️ Usando tipos de golpe padrão', 'warning');
    }
    
    return tiposGolpesData;
}

// Renderizar seletor de tipos de golpes no modal com animações
function renderizarSeletorGolpes() {
    const container = document.getElementById('tipoGolpeContainer');
    if (!container) {
        console.warn('⚠️ Container tipoGolpeContainer não encontrado');
        return;
    }
    
    // Animação de saída
    container.style.opacity = '0';
    container.style.transform = 'translateY(20px)';
    
    setTimeout(() => {
        container.innerHTML = '';
        
        // Criar estrutura do seletor moderno
        const seletorHTML = `
            <div class="tipo-golpe-selector" style="opacity: 0; transform: translateY(20px); transition: all 0.6s ease;">
                <label for="tipoGolpe" class="form-label">
                    <i class="fas fa-shield-alt" style="margin-right: 0.5rem; color: var(--bradesco-red);"></i>
                    Tipo de Golpe <span class="required">*</span>
                </label>
                
                <!-- Barra de busca com filtro integrado -->
                <div class="search-filter-container" style="
                    display: flex;
                    gap: 0.75rem;
                    margin-bottom: 1.5rem;
                    align-items: stretch;
                ">
                    <!-- Campo de busca -->
                    <div class="search-container" style="flex: 1;">
                        <input type="text" 
                               id="buscaGolpe" 
                               placeholder="🔍 Digite para buscar o tipo de golpe..." 
                               class="form-control">
                        <i class="fas fa-search search-icon"></i>
                    </div>
                    
                    <!-- Dropdown de categoria compacto -->
                    <div class="filter-dropdown" style="position: relative;">
                        <button type="button" id="filterButton" class="filter-btn" style="
                            padding: 1rem;
                            border: 2px solid transparent;
                            background: linear-gradient(white, white) padding-box,
                                        linear-gradient(135deg, #e9ecef, #f8f9fa) border-box;
                            border-radius: 16px;
                            cursor: pointer;
                            transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
                            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
                            display: flex;
                            align-items: center;
                            gap: 0.5rem;
                            font-weight: 600;
                            min-width: 120px;
                            justify-content: center;
                        ">
                            <span id="filterIcon">🔍</span>
                            <span id="filterText">Todas</span>
                            <i class="fas fa-chevron-down" style="font-size: 0.8rem; transition: transform 0.3s ease;" id="filterArrow"></i>
                        </button>
                        
                        <!-- Menu dropdown -->
                        <div id="filterMenu" class="filter-menu" style="
                            position: absolute;
                            top: calc(100% + 0.5rem);
                            left: 0;
                            right: 0;
                            background: linear-gradient(135deg, #ffffff, #f8f9fa);
                            border-radius: 16px;
                            box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
                            border: 2px solid rgba(204, 9, 47, 0.1);
                            z-index: 1000;
                            opacity: 0;
                            transform: translateY(-10px);
                            pointer-events: none;
                            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                            backdrop-filter: blur(20px);
                            max-height: 300px;
                            overflow-y: auto;
                        ">
                            <div class="filter-option active" data-categoria="TODAS" style="
                                padding: 0.75rem 1rem;
                                cursor: pointer;
                                transition: all 0.2s ease;
                                font-weight: 600;
                                display: flex;
                                align-items: center;
                                gap: 0.75rem;
                                border-bottom: 1px solid rgba(0, 0, 0, 0.05);
                            ">
                                <span style="font-size: 1.1rem;">🔍</span>
                                <span>Todas as categorias</span>
                                <span class="option-count" style="
                                    margin-left: auto;
                                    background: var(--bradesco-red);
                                    color: white;
                                    padding: 0.2rem 0.5rem;
                                    border-radius: 12px;
                                    font-size: 0.75rem;
                                    font-weight: 700;
                                ">${tiposGolpesData.tipos.length}</span>
                            </div>
                            ${Object.entries(tiposGolpesData.categorias).map(([key, categoria]) => `
                                <div class="filter-option" data-categoria="${key}" style="
                                    padding: 0.75rem 1rem;
                                    cursor: pointer;
                                    transition: all 0.2s ease;
                                    font-weight: 500;
                                    display: flex;
                                    align-items: center;
                                    gap: 0.75rem;
                                    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
                                ">
                                    <span style="font-size: 1.1rem;">${categoria.emoji}</span>
                                    <span>${categoria.nome}</span>
                                    <span class="option-count" style="
                                        margin-left: auto;
                                        background: #6c757d;
                                        color: white;
                                        padding: 0.2rem 0.5rem;
                                        border-radius: 12px;
                                        font-size: 0.75rem;
                                        font-weight: 700;
                                    ">${categoria.tipos.length}</span>
                                </div>
                            `).join('')}
                        </div>
                    </div>
                </div>
                
                <!-- Contador de resultados compacto -->
                <div id="contadorResultados" class="contador-resultados" style="
                    text-align: center; 
                    margin-bottom: 1rem; 
                    padding: 0.5rem 1rem;
                    font-size: 0.85rem; 
                    opacity: 0;
                    transition: opacity 0.3s ease;
                ">
                    <span id="textoContador">${tiposGolpesData.tipos.length} tipos disponíveis</span>
                </div>
                
                <!-- Lista de tipos modernizada -->
                <div class="tipos-lista" id="tiposLista">
                    ${renderizarTiposGolpe('TODAS')}
                </div>
                
                <!-- Tipo selecionado -->
                <input type="hidden" id="tipoGolpeSelecionado" name="tipoGolpe" value="">
                
                <!-- Informações do tipo selecionado -->
                <div id="infoTipoSelecionado" class="tipo-info" style="
                    display: none; 
                    margin-top: 1.5rem;
                    opacity: 0;
                    transform: translateY(10px);
                    transition: all 0.4s ease;
                ">
                    <div class="alert alert-info" style="
                        background: linear-gradient(135deg, rgba(0, 123, 255, 0.1), rgba(0, 123, 255, 0.05));
                        border: 2px solid rgba(0, 123, 255, 0.2);
                        border-radius: 16px;
                        padding: 1.5rem;
                        box-shadow: 0 8px 25px rgba(0, 123, 255, 0.1);
                    ">
                        <div style="display: flex; align-items: center; margin-bottom: 1rem;">
                            <div style="
                                width: 50px; 
                                height: 50px; 
                                background: linear-gradient(135deg, var(--bradesco-red), var(--bradesco-dark-red));
                                border-radius: 50%;
                                display: flex;
                                align-items: center;
                                justify-content: center;
                                margin-right: 1rem;
                                font-size: 1.5rem;
                            " id="emojiGolpeSelecionado">⚠️</div>
                            <div>
                                <strong id="nomeGolpeSelecionado" style="font-size: 1.2rem; color: #0056b3;"></strong>
                                <div id="badgeSeveridadeSelecionado" style="margin-top: 0.5rem;"></div>
                            </div>
                        </div>
                        <p id="descricaoGolpeSelecionado" style="margin: 0; font-size: 0.95rem; line-height: 1.5;"></p>
                        <div id="metadataGolpeSelecionado" style="margin-top: 1rem; padding-top: 1rem; border-top: 1px solid rgba(0, 123, 255, 0.2); font-size: 0.85rem;"></div>
                    </div>
                </div>
            </div>
        `;
        
        container.innerHTML = seletorHTML;
        
        // Animação de entrada
        setTimeout(() => {
            container.style.opacity = '1';
            container.style.transform = 'translateY(0)';
            
            const selector = container.querySelector('.tipo-golpe-selector');
            if (selector) {
                selector.style.opacity = '1';
                selector.style.transform = 'translateY(0)';
            }
            
            // Mostrar contador após animação
            setTimeout(() => {
                const contador = document.getElementById('contadorResultados');
                if (contador) {
                    contador.style.opacity = '1';
                }
            }, 300);
            
        }, 100);
        
        // Configurar event listeners
        configurarEventListenersGolpes();
        
        // Inicializar contador após carregar
        setTimeout(() => {
            atualizarContadorResultados();
        }, 100);
        
    }, 150);
}

// Renderizar tipos de golpe filtrados
function renderizarTiposGolpe(categoriaFiltro = 'TODAS', termoBusca = '') {
    let tiposFiltrados = tiposGolpesData.tipos;
    
    // Filtrar por categoria
    if (categoriaFiltro !== 'TODAS') {
        const categoria = tiposGolpesData.categorias[categoriaFiltro];
        if (categoria) {
            tiposFiltrados = categoria.tipos;
        }
    }
    
    // Filtrar por busca
    if (termoBusca.trim()) {
        const termo = termoBusca.toLowerCase();
        tiposFiltrados = tiposFiltrados.filter(tipo => 
            tipo.nome.toLowerCase().includes(termo) ||
            tipo.descricao.toLowerCase().includes(termo) ||
            tipo.categoria.toLowerCase().includes(termo)
        );
    }
    
    if (tiposFiltrados.length === 0) {
        return `
            <div class="no-results" style="text-align: center; padding: 2rem; color: #666;">
                <i class="fas fa-search" style="font-size: 2rem; color: #ccc; margin-bottom: 1rem;"></i>
                <p>Nenhum tipo de golpe encontrado</p>
                <small>Tente ajustar os filtros ou termo de busca</small>
            </div>
        `;
    }
    
    return tiposFiltrados.map(tipo => `
        <div class="tipo-item" data-codigo="${tipo.codigo}" data-categoria="${tipo.categoriaKey || 'OUTROS'}" 
             style="border: 1px solid #eee; border-radius: 8px; padding: 1rem; margin-bottom: 0.5rem; cursor: pointer; transition: all 0.2s;">
            <div class="tipo-header" style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 0.5rem;">
                <div style="display: flex; align-items: center; gap: 0.5rem;">
                    <span class="tipo-emoji" style="font-size: 1.2rem;">${tipo.emoji}</span>
                    <span class="tipo-nome" style="font-weight: 600; color: #333;">${tipo.nome}</span>
                </div>
                <span class="tipo-severidade" style="background-color: ${tipo.corSeveridade || '#ffc107'}; color: white; padding: 0.2rem 0.5rem; border-radius: 12px; font-size: 0.8rem;">
                    ${tipo.severidade}
                </span>
            </div>
            <div class="tipo-descricao" style="font-size: 0.9rem; color: #666; margin-bottom: 0.5rem;">${tipo.descricao}</div>
            <div class="tipo-metadata" style="font-size: 0.8rem; color: #888;">
                <span class="metadata-item">📂 ${tipo.categoria}</span>
            </div>
        </div>
    `).join('');
}

// Configurar event listeners para o seletor de golpes
function configurarEventListenersGolpes() {
    // Dropdown de filtro compacto
    const filterButton = document.getElementById('filterButton');
    const filterMenu = document.getElementById('filterMenu');
    const filterArrow = document.getElementById('filterArrow');
    
    if (filterButton && filterMenu) {
        // Toggle dropdown
        filterButton.addEventListener('click', function(e) {
            e.stopPropagation();
            const isOpen = filterMenu.style.opacity === '1';
            
            if (isOpen) {
                // Fechar
                filterMenu.style.opacity = '0';
                filterMenu.style.transform = 'translateY(-10px)';
                filterMenu.style.pointerEvents = 'none';
                filterArrow.style.transform = 'rotate(0deg)';
            } else {
                // Abrir
                filterMenu.style.opacity = '1';
                filterMenu.style.transform = 'translateY(0)';
                filterMenu.style.pointerEvents = 'auto';
                filterArrow.style.transform = 'rotate(180deg)';
            }
        });
        
        // Fechar dropdown ao clicar fora
        document.addEventListener('click', function() {
            filterMenu.style.opacity = '0';
            filterMenu.style.transform = 'translateY(-10px)';
            filterMenu.style.pointerEvents = 'none';
            filterArrow.style.transform = 'rotate(0deg)';
        });
        
        // Prevenir fechamento ao clicar no menu
        filterMenu.addEventListener('click', function(e) {
            e.stopPropagation();
        });
    }
    
    // Opções do filtro
    document.querySelectorAll('.filter-option').forEach(option => {
        option.addEventListener('click', function() {
            const categoria = this.dataset.categoria;
            const termoBusca = document.getElementById('buscaGolpe')?.value || '';
            
            // Atualizar opção ativa
            document.querySelectorAll('.filter-option').forEach(opt => {
                opt.classList.remove('active');
                opt.style.background = '';
                opt.querySelector('.option-count').style.background = '#6c757d';
            });
            
            this.classList.add('active');
            this.style.background = 'linear-gradient(135deg, rgba(204, 9, 47, 0.1), rgba(204, 9, 47, 0.05))';
            this.querySelector('.option-count').style.background = 'var(--bradesco-red)';
            
            // Atualizar botão
            const filterIcon = document.getElementById('filterIcon');
            const filterText = document.getElementById('filterText');
            
            if (categoria === 'TODAS') {
                filterIcon.textContent = '🔍';
                filterText.textContent = 'Todas';
            } else {
                const categoriaData = tiposGolpesData.categorias[categoria];
                if (categoriaData) {
                    filterIcon.textContent = categoriaData.emoji;
                    filterText.textContent = categoriaData.nome;
                }
            }
            
            // Fechar dropdown
            filterMenu.style.opacity = '0';
            filterMenu.style.transform = 'translateY(-10px)';
            filterMenu.style.pointerEvents = 'none';
            filterArrow.style.transform = 'rotate(0deg)';
            
            // Filtrar tipos com animação
            const listaContainer = document.getElementById('tiposLista');
            if (listaContainer) {
                // Animação de saída
                listaContainer.style.opacity = '0.3';
                listaContainer.style.transform = 'translateY(10px)';
                
                setTimeout(() => {
                    listaContainer.innerHTML = renderizarTiposGolpe(categoria, termoBusca);
                    configurarEventListenersTipos();
                    
                    // Animação de entrada
                    listaContainer.style.opacity = '1';
                    listaContainer.style.transform = 'translateY(0)';
                    
                    // Atualizar contador
                    atualizarContadorResultados();
                }, 150);
            }
            
            // Feedback haptic
            if (navigator.vibrate) {
                navigator.vibrate(30);
            }
        });
        
        // Hover effects
        option.addEventListener('mouseenter', function() {
            if (!this.classList.contains('active')) {
                this.style.background = 'rgba(204, 9, 47, 0.05)';
            }
        });
        
        option.addEventListener('mouseleave', function() {
            if (!this.classList.contains('active')) {
                this.style.background = '';
            }
        });
    });
    
    // Campo de busca com debounce e animações
    const campoBusca = document.getElementById('buscaGolpe');
    if (campoBusca) {
        let timeoutId;
        
        campoBusca.addEventListener('input', function() {
            const termoBusca = this.value;
            const categoriaAtiva = document.querySelector('.filter-option.active')?.dataset.categoria || 'TODAS';
            
            // Debounce para melhor performance
            clearTimeout(timeoutId);
            timeoutId = setTimeout(() => {
                const listaContainer = document.getElementById('tiposLista');
                if (listaContainer) {
                    // Animação de saída
                    listaContainer.style.opacity = '0.5';
                    listaContainer.style.transform = 'translateY(10px)';
                    
                    setTimeout(() => {
                        listaContainer.innerHTML = renderizarTiposGolpe(categoriaAtiva, termoBusca);
                        configurarEventListenersTipos();
                        
                        // Animação de entrada
                        listaContainer.style.opacity = '1';
                        listaContainer.style.transform = 'translateY(0)';
                        
                        // Atualizar contador
                        atualizarContadorResultados();
                    }, 150);
                }
            }, 300);
        });
        
        // Efeitos visuais no campo de busca
        campoBusca.addEventListener('keydown', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                // Feedback visual
                this.style.transform = 'scale(0.98)';
                setTimeout(() => {
                    this.style.transform = '';
                }, 100);
            }
        });
    }
    
    // Configurar seleção de tipos
    configurarEventListenersTipos();
}

// Configurar event listeners para seleção de tipos com animações
function configurarEventListenersTipos() {
    document.querySelectorAll('.tipo-item').forEach((item, index) => {
        // Animação de entrada escalonada
        item.style.animationDelay = `${index * 50}ms`;
        item.style.animation = 'slideInFromBottom 0.4s ease forwards';
        
        item.addEventListener('click', function() {
            // Feedback haptic (se disponível)
            if (navigator.vibrate) {
                navigator.vibrate(30);
            }
            
            // Remover seleção anterior com animação
            document.querySelectorAll('.tipo-item').forEach(i => {
                i.classList.remove('selected');
                i.style.transform = '';
            });
            
            // Adicionar seleção atual com animação
            this.classList.add('selected');
            
            // Animação de seleção
            this.style.transform = 'scale(1.02)';
            setTimeout(() => {
                this.style.transform = '';
            }, 200);
            
            // Scroll suave para o item selecionado
            this.scrollIntoView({
                behavior: 'smooth',
                block: 'nearest'
            });
            
            // Atualizar campo hidden
            const codigo = this.dataset.codigo;
            const campoHidden = document.getElementById('tipoGolpeSelecionado');
            if (campoHidden) {
                campoHidden.value = codigo;
            }
            
            // Buscar dados do tipo selecionado
            const tipoSelecionado = tiposGolpesData.tipos.find(t => t.codigo === codigo);
            if (tipoSelecionado) {
                mostrarInfoTipoSelecionadoModerno(tipoSelecionado);
            }
            
            // Atualizar contador de resultados
            atualizarContadorResultados();
        });
        
        // Hover effects modernos (não aplicar no touch)
        if (!('ontouchstart' in window)) {
            item.addEventListener('mouseenter', function() {
                if (!this.classList.contains('selected')) {
                    this.style.transform = 'translateY(-2px)';
                }
            });
            
            item.addEventListener('mouseleave', function() {
                if (!this.classList.contains('selected')) {
                    this.style.transform = '';
                }
            });
        }
    });
}

// Função moderna para mostrar informações do tipo selecionado
function mostrarInfoTipoSelecionadoModerno(tipo) {
    const infoContainer = document.getElementById('infoTipoSelecionado');
    const nomeElement = document.getElementById('nomeGolpeSelecionado');
    const descricaoElement = document.getElementById('descricaoGolpeSelecionado');
    const metadataElement = document.getElementById('metadataGolpeSelecionado');
    const emojiElement = document.getElementById('emojiGolpeSelecionado');
    const badgeElement = document.getElementById('badgeSeveridadeSelecionado');
    
    if (infoContainer && nomeElement && descricaoElement && metadataElement) {
        // Animação de saída
        infoContainer.style.opacity = '0';
        infoContainer.style.transform = 'translateY(10px)';
        
        setTimeout(() => {
            // Atualizar conteúdo
            nomeElement.textContent = tipo.nome;
            descricaoElement.textContent = tipo.descricao;
            
            // Atualizar emoji
            if (emojiElement) {
                const categoria = tiposGolpesData.categorias[tipo.categoriaKey];
                emojiElement.textContent = categoria ? categoria.emoji : tipo.emoji || '⚠️';
            }
            
            // Atualizar badge de severidade
            if (badgeElement) {
                const severidadeClass = tipo.severidade.toLowerCase().replace('í', 'i').replace('é', 'e');
                badgeElement.innerHTML = `
                    <span class="tipo-severidade ${severidadeClass}">
                        ${tipo.severidade.toUpperCase()}
                    </span>
                `;
            }
            
            // Metadata atualizada e moderna
            metadataElement.innerHTML = `
                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem;">
                    <div>
                        <i class="fas fa-folder" style="color: var(--bradesco-red); margin-right: 0.5rem;"></i>
                        <strong>Categoria:</strong> ${tipo.categoria}
                    </div>
                    <div>
                        <i class="fas fa-clock" style="color: var(--bradesco-red); margin-right: 0.5rem;"></i>
                        <strong>Tempo de Análise:</strong> ${obterTempoAnalise(tipo.severidade)}
                    </div>
                    <div>
                        <i class="fas fa-chart-line" style="color: var(--bradesco-red); margin-right: 0.5rem;"></i>
                        <strong>Prioridade:</strong> ${obterPrioridadePorSeveridade(tipo.severidade)}
                    </div>
                </div>
            `;
            
            // Animação de entrada
            infoContainer.style.display = 'block';
            setTimeout(() => {
                infoContainer.style.opacity = '1';
                infoContainer.style.transform = 'translateY(0)';
            }, 50);
            
        }, 200);
    }
}

// Função auxiliar para obter tempo de análise
function obterTempoAnalise(severidade) {
    const mapa = {
        'CRÍTICA': '2 horas',
        'ALTA': '8 horas', 
        'MÉDIA': '24 horas',
        'BAIXA': '48 horas'
    };
    return mapa[severidade] || '24 horas';
}

// Função auxiliar para obter prioridade
function obterPrioridadePorSeveridade(severidade) {
    const mapa = {
        'CRÍTICA': 'URGENTE',
        'ALTA': 'ALTA', 
        'MÉDIA': 'MÉDIA',
        'BAIXA': 'BAIXA'
    };
    return mapa[severidade] || 'MÉDIA';
}

// Atualizar contador de resultados compacto
function atualizarContadorResultados() {
    const contador = document.getElementById('contadorResultados');
    const textoContador = document.getElementById('textoContador');
    
    if (contador && textoContador) {
        const tiposVisiveis = document.querySelectorAll('.tipo-item').length;
        const tipoSelecionado = document.querySelector('.tipo-item.selected');
        const categoriaAtiva = document.querySelector('.filter-option.active');
        
        if (tipoSelecionado) {
            const nomeGolpe = tipoSelecionado.querySelector('.tipo-nome').textContent;
            textoContador.innerHTML = `
                <i class="fas fa-check-circle" style="color: #28a745; margin-right: 0.5rem;"></i>
                <strong>Selecionado:</strong> ${nomeGolpe}
            `;
            contador.style.background = 'linear-gradient(135deg, rgba(40, 167, 69, 0.15), rgba(40, 167, 69, 0.05))';
            contador.style.borderColor = 'rgba(40, 167, 69, 0.4)';
            contador.style.color = '#1e7e34';
        } else {
            const filterText = document.getElementById('filterText')?.textContent || 'Todas';
            const filterIcon = document.getElementById('filterIcon')?.textContent || '🔍';
            
            textoContador.innerHTML = `
                <span style="display: flex; align-items: center; justify-content: center; gap: 0.5rem;">
                    <span style="font-size: 1rem;">${filterIcon}</span>
                    <span><strong>${tiposVisiveis}</strong> ${tiposVisiveis === 1 ? 'tipo' : 'tipos'} ${filterText !== 'Todas' ? `em ${filterText}` : 'disponíveis'}</span>
                </span>
            `;
            contador.style.background = 'linear-gradient(135deg, rgba(204, 9, 47, 0.1), rgba(204, 9, 47, 0.05))';
            contador.style.borderColor = 'rgba(204, 9, 47, 0.2)';
            contador.style.color = 'var(--bradesco-dark)';
        }
        
        // Animação pulsante para feedback visual
        contador.classList.add('pulse-animation');
        setTimeout(() => {
            contador.classList.remove('pulse-animation');
        }, 600);
    }
}

// Mostrar informações do tipo selecionado
function mostrarInfoTipoSelecionado(tipo) {
    const infoContainer = document.getElementById('infoTipoSelecionado');
    const nomeElement = document.getElementById('nomeGolpeSelecionado');
    const descricaoElement = document.getElementById('descricaoGolpeSelecionado');
    const metadataElement = document.getElementById('metadataGolpeSelecionado');
    
    if (infoContainer && nomeElement && descricaoElement && metadataElement) {
        nomeElement.textContent = `${tipo.emoji} ${tipo.nome}`;
        descricaoElement.textContent = tipo.descricao;
        metadataElement.innerHTML = `
            <strong>Categoria:</strong> ${tipo.categoria} • 
            <strong>Severidade:</strong> <span style="color: ${tipo.corSeveridade || '#ffc107'};">${tipo.severidade}</span>
        `;
        
        infoContainer.style.display = 'block';
    }
}

// =============================================
// BINO ASSISTENTE - FUNCIONALIDADE REVISADA
// =============================================

function initializeBinoAssistant() {
    console.log('🔥 Inicializando Bino Assistente...');
    
    const binoAssistant = document.getElementById('binoAssistant');
    const speechBubble = document.querySelector('.bino-speech-bubble');
    const binoImage = document.querySelector('.bino-image');
    
    if (!binoAssistant) {
        console.error('❌ Elemento binoAssistant não encontrado');
        return;
    }
    if (!speechBubble) {
        console.error('❌ Elemento speechBubble não encontrado');
        return;
    }
    if (!binoImage) {
        console.error('❌ Elemento binoImage não encontrado');
        return;
    }
    
    console.log('✅ Todos os elementos do Bino encontrados');
    
    // Garantir que o Bino está visível
    binoAssistant.style.display = 'flex';
    binoAssistant.style.opacity = '1';
    
    // Mostrar balão imediatamente
    speechBubble.style.display = 'block';
    speechBubble.style.opacity = '1';
    
    console.log('✅ Bino inicializado com sucesso');
    
    // Auto esconder o balão após 6 segundos
    setTimeout(() => {
        hideSpeechBubble();
    }, 6000);
    
    // Reaparecer periodicamente
    setInterval(() => {
        showRandomMessage();
    }, 20000);
    
    // Configurar clique
    binoImage.addEventListener('click', handleBinoClick);
    
    // Configurar hover
    binoImage.addEventListener('mouseenter', handleBinoHover);
    binoImage.addEventListener('mouseleave', handleBinoLeave);
    
    function showRandomMessage() {
        const messages = [
            "🛡️ Oi! Sou o Bino, seu assistente de segurança!",
            "🔒 Sempre verifique os dados antes de fazer PIX!",
            "⚠️ Desconfie de ofertas muito boas para ser verdade!",
            "📱 O Bradesco nunca pede dados por telefone!",
            "🚨 Clique em mim para dicas de segurança!",
            "💪 Juntos mantemos sua conta protegida!"
        ];
        
        const randomMessage = messages[Math.floor(Math.random() * messages.length)];
        showSpeechBubble(randomMessage);
        
        // Adicionar efeito pulse
        binoImage.classList.add('pulse');
        setTimeout(() => {
            binoImage.classList.remove('pulse');
        }, 2000);
        
        // Auto esconder após 5 segundos
        setTimeout(() => {
            hideSpeechBubble();
        }, 5000);
    }
    
    function handleBinoClick() {
        console.log('🖱️ Bino clicado');
        
        const tips = [
            "🔐 Nunca compartilhe sua senha ou token!",
            "🎯 Sempre confirme os dados do destinatário!",
            "📱 Desconfie de links suspeitos no WhatsApp!",
            "☎️ O Santander nunca pede dados por telefone!",
            "🚨 Denuncie tentativas de golpe imediatamente!",
            "✅ Mantenha seu app sempre atualizado!",
            "🔍 Verifique o valor antes de confirmar!",
            "💡 Na dúvida, procure uma agência!",
            "🛡️ Use sempre redes Wi-Fi seguras!",
            "👀 Cubra o teclado ao digitar a senha!"
        ];
        
        const randomTip = tips[Math.floor(Math.random() * tips.length)];
        showSpeechBubble(randomTip, true);
        
        // Efeito visual
        binoImage.style.transform = 'scale(1.15)';
        setTimeout(() => {
            binoImage.style.transform = 'scale(1)';
        }, 200);
        
        // Auto esconder após 4 segundos
        setTimeout(() => {
            hideSpeechBubble();
        }, 4000);
    }
    
    function handleBinoHover() {
        if (speechBubble.style.display === 'none' || speechBubble.style.opacity === '0') {
            showSpeechBubble("👋 Clique em mim para dicas de segurança!");
            
            setTimeout(() => {
                hideSpeechBubble();
            }, 3000);
        }
    }
    
    function handleBinoLeave() {
        // Apenas limpa o timeout se houver um
    }
    
    function showSpeechBubble(message, highlight = false) {
        speechBubble.querySelector('p').textContent = message;
        speechBubble.style.display = 'block';
        speechBubble.style.opacity = '1';
        speechBubble.style.transform = 'translateY(0)';
        
        if (highlight) {
            speechBubble.classList.add('highlight');
            setTimeout(() => {
                speechBubble.classList.remove('highlight');
            }, 4000);
        }
        
        console.log('💬 Balão mostrado:', message);
    }
    
    function hideSpeechBubble() {
        speechBubble.style.opacity = '0';
        speechBubble.style.transform = 'translateY(-10px)';
        
        setTimeout(() => {
            speechBubble.style.display = 'none';
        }, 300);
        
        console.log('💬 Balão escondido');
    }
}

// Fim da funcionalidade do Bino Revisada

// =============================================
// BINO: MENSAGENS INTELIGENTES PARA ANÁLISE PIX
// =============================================

/**
 * Mostra mensagem do Bino durante análise PIX
 * @param {string} message - Mensagem a ser exibida
 */
function showBinoAnalysisMessage(message) {
    console.log('🤖 Bino Análise:', message);
    
    const speechBubble = document.querySelector('.bino-speech-bubble');
    const binoImage = document.querySelector('.bino-image');
    
    if (!speechBubble || !binoImage) {
        console.warn('⚠️ Elementos do Bino não encontrados');
        return;
    }
    
    // Mostrar mensagem com efeito especial
    speechBubble.querySelector('p').textContent = message;
    speechBubble.style.display = 'block';
    speechBubble.style.opacity = '1';
    speechBubble.style.transform = 'translateY(0)';
    speechBubble.style.background = 'linear-gradient(135deg, #4dabf7, #1971c2)';
    speechBubble.style.color = 'white';
    speechBubble.style.border = '2px solid #1971c2';
    
    // Efeito de análise no Bino
    binoImage.style.animation = 'binoPulseAnalysis 1.5s ease-in-out infinite';
    binoImage.style.borderColor = '#1971c2';
    binoImage.style.boxShadow = '0 0 20px rgba(25, 113, 194, 0.6)';
    
    // Adicionar classe de análise ao balão
    speechBubble.classList.add('analysis');
}

/**
 * Mostra mensagem do Bino com resultado da análise
 * @param {string} message - Mensagem a ser exibida
 * @param {string} type - Tipo: 'success', 'warning', 'blocked'
 */
function showBinoResultMessage(message, type = 'success') {
    console.log('🎯 Bino Resultado:', message, type);
    
    const speechBubble = document.querySelector('.bino-speech-bubble');
    const binoImage = document.querySelector('.bino-image');
    
    if (!speechBubble || !binoImage) {
        console.warn('⚠️ Elementos do Bino não encontrados');
        return;
    }
    
    // Configurar estilo baseado no tipo
    let bgColor, borderColor, pulseColor;
    
    switch (type) {
        case 'success':
            bgColor = 'linear-gradient(135deg, #51cf66, #2b8a3e)';
            borderColor = '#2b8a3e';
            pulseColor = 'rgba(43, 138, 62, 0.6)';
            break;
        case 'warning':
            bgColor = 'linear-gradient(135deg, #ffd43b, #fab005)';
            borderColor = '#fab005';
            pulseColor = 'rgba(250, 176, 5, 0.6)';
            break;
        case 'blocked':
            bgColor = 'linear-gradient(135deg, #ff6b6b, #e03131)';
            borderColor = '#e03131';
            pulseColor = 'rgba(224, 49, 49, 0.6)';
            break;
        default:
            bgColor = 'linear-gradient(135deg, #4dabf7, #1971c2)';
            borderColor = '#1971c2';
            pulseColor = 'rgba(25, 113, 194, 0.6)';
    }
    
    // Aplicar estilo na mensagem
    speechBubble.querySelector('p').textContent = message;
    speechBubble.style.display = 'block';
    speechBubble.style.opacity = '1';
    speechBubble.style.transform = 'translateY(0)';
    speechBubble.style.background = bgColor;
    speechBubble.style.color = 'white';
    speechBubble.style.border = `2px solid ${borderColor}`;
    speechBubble.style.fontWeight = 'bold';
    speechBubble.style.fontSize = '0.95rem';
    
    // Efeito visual no Bino
    binoImage.style.borderColor = borderColor;
    binoImage.style.boxShadow = `0 0 25px ${pulseColor}`;
    
    // Adicionar classe CSS apropriada ao balão
    speechBubble.classList.remove('analysis', 'success', 'blocked', 'warning');
    speechBubble.classList.add(type);
    
    // Animação especial baseada no tipo
    if (type === 'success') {
        binoImage.style.animation = 'binoSuccessPulse 2s ease-in-out';
        // Efeito de "celebração"
        setTimeout(() => {
            binoImage.style.transform = 'scale(1.1) rotate(5deg)';
            setTimeout(() => {
                binoImage.style.transform = 'scale(1) rotate(0deg)';
            }, 200);
        }, 500);
    } else if (type === 'blocked') {
        binoImage.style.animation = 'binoBlockedShake 1s ease-in-out';
        // Efeito de "proteção"
        binoImage.style.filter = 'brightness(1.2)';
        setTimeout(() => {
            binoImage.style.filter = 'brightness(1)';
        }, 2000);
    } else if (type === 'warning') {
        binoImage.style.animation = 'binoWarningBlink 1.5s ease-in-out';
    }
    
    // Auto-esconder após 8 segundos
    setTimeout(() => {
        hideBinoMessage();
    }, 8000);
}

/**
 * Esconde mensagens do Bino e restaura estado normal
 */
function hideBinoMessage() {
    const speechBubble = document.querySelector('.bino-speech-bubble');
    const binoImage = document.querySelector('.bino-image');
    
    if (!speechBubble || !binoImage) return;
    
    // Esconder balão
    speechBubble.style.opacity = '0';
    speechBubble.style.transform = 'translateY(-10px)';
    
    setTimeout(() => {
        speechBubble.style.display = 'none';
        
        // Restaurar estilo normal do balão
        speechBubble.style.background = 'linear-gradient(135deg, #fff, #f8f9fa)';
        speechBubble.style.color = 'var(--bradesco-dark)';
        speechBubble.style.border = '2px solid rgba(204, 9, 47, 0.2)';
        speechBubble.style.fontWeight = 'normal';
        speechBubble.style.fontSize = '0.9rem';
        
        // Remover classes de animação
        speechBubble.classList.remove('analysis', 'success', 'blocked', 'warning');
        
        // Restaurar estilo normal do Bino
        binoImage.style.animation = 'binoPulseGlow 3s ease-in-out infinite';
        binoImage.style.borderColor = '#CC092F';
        binoImage.style.boxShadow = '0 4px 15px rgba(204, 9, 47, 0.3)';
        binoImage.style.transform = 'scale(1) rotate(0deg)';
        binoImage.style.filter = 'brightness(1)';
    }, 300);
}

// Fim das Mensagens Inteligentes do Bino