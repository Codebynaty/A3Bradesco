// =============================================
// BRADESCO PIX MONITOR - JAVASCRIPT PRINCIPAL
// Sistema de Login e Portal Principal
// =============================================

// Estado global da aplicação
const app = {
    isLoaded: false,
    config: {},
    currentTab: 'cliente',
    loading: false,
    systemStatus: {
        api: 'checking',
        database: 'checking',
        ia: 'checking',
        security: 'checking'
    }
};

// =============================================
// INICIALIZAÇÃO
// =============================================

document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Bradesco PIX Monitor - Sistema iniciado');
    initializeApp();
});

async function initializeApp() {
    try {
        showLoading('Inicializando sistema...');
        
        // Configurações iniciais
        setupEventListeners();
        loadDefaultConfig();
        
        // Carrega configurações do servidor
        await loadServerConfig();
        
        // Verifica status do sistema
        await checkSystemStatus();
        
        // Animações e efeitos
        initializeAnimations();
        
        app.isLoaded = true;
        hideLoading();
        
        console.log('✅ Sistema inicializado com sucesso');
        
    } catch (error) {
        console.error('❌ Erro na inicialização:', error);
        hideLoading();
        showNotification('Erro na inicialização do sistema. Usando configuração padrão.', 'warning');
    }
}

// =============================================
// CONFIGURAÇÕES
// =============================================

function loadDefaultConfig() {
    app.config = {
        logoUrl: null, // Usando ícone FontAwesome
        tituloSistema: 'Bradesco PIX Monitor',
        subtituloSistema: 'Sistema de Segurança e Anti-Fraude',
        descricaoSistema: 'Sistema avançado de monitoramento e detecção de fraudes em tempo real',
        corPrimaria: '#cc092f',
        corSecundaria: '#a00724',
        precisaoIA: '99.8%',
        transacoesProtegidas: '2.5M+',
        fraudesBloqueadas: '15.2K',
        tempoResposta: '1.8s'
    };
}

async function loadServerConfig() {
    try {
        const response = await fetch('/api/config/interface');
        if (response.ok) {
            const serverConfig = await response.json();
            app.config = { ...app.config, ...serverConfig };
            updateInterfaceElements();
        }
    } catch (error) {
        console.log('⚠️ Configurações do servidor não disponíveis, usando padrão');
    }
}

function updateInterfaceElements() {
    // Atualiza estatísticas se os elementos existirem
    const elementos = {
        'transacoes-protegidas': app.config.transacoesProtegidas,
        'fraudes-bloqueadas': app.config.fraudesBloqueadas,
        'tempo-resposta': app.config.tempoResposta,
        'precisao-ia': app.config.precisaoIA
    };
    
    Object.entries(elementos).forEach(([id, valor]) => {
        const elemento = document.getElementById(id);
        if (elemento) {
            elemento.textContent = valor;
            animateNumber(elemento);
        }
    });
    
    // Atualiza cores CSS se diferentes do padrão
    if (app.config.corPrimaria !== '#cc092f') {
        document.documentElement.style.setProperty('--bradesco-red', app.config.corPrimaria);
    }
    if (app.config.corSecundaria !== '#a00724') {
        document.documentElement.style.setProperty('--bradesco-dark-red', app.config.corSecundaria);
    }
}

// =============================================
// SISTEMA DE TABS
// =============================================

function switchTab(tabType) {
    // Remove active de todos os botões e conteúdos
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
    
    // Adiciona active no botão clicado
    const clickedBtn = document.querySelector(`[data-tab="${tabType}"]`);
    if (clickedBtn) clickedBtn.classList.add('active');
    
    // Mostra conteúdo correspondente
    const tabContent = document.getElementById(`tab-${tabType}`);
    if (tabContent) {
        tabContent.classList.add('active');
        tabContent.style.animation = 'fadeInUp 0.3s ease';
    }
    
    app.currentTab = tabType;
    console.log(`🔄 Tab alterada para: ${tabType}`);
}

// =============================================
// SISTEMA DE LOGIN
// =============================================

async function loginCliente(event) {
    event.preventDefault();
    
    const cpf = document.getElementById('clienteCpf').value;
    const email = document.getElementById('clienteEmail').value;
    
    if (!cpf || !email) {
        showNotification('Por favor, preencha todos os campos', 'warning');
        return;
    }
    
    if (!isValidCPF(cpf)) {
        showNotification('CPF inválido', 'error');
        return;
    }
    
    if (!isValidEmail(email)) {
        showNotification('E-mail inválido', 'error');
        return;
    }
    
    try {
        showLoading('Validando credenciais...');
        
        // Simula validação (em produção faria chamada à API)
        await simulateAPICall(2000);
        
        showNotification('Login realizado com sucesso!', 'success');
        
        setTimeout(() => {
            window.location.href = 'cliente.html';
        }, 1500);
        
    } catch (error) {
        hideLoading();
        showNotification('Erro no login. Verifique suas credenciais.', 'error');
    }
}

async function loginFuncionario(event) {
    event.preventDefault();
    
    const matricula = document.getElementById('funcionarioMatricula').value;
    const senha = document.getElementById('funcionarioSenha').value;
    
    if (!matricula || !senha) {
        showNotification('Por favor, preencha todos os campos', 'warning');
        return;
    }
    
    try {
        showLoading('Autenticando funcionário...');
        
        // Simula autenticação (em produção faria chamada à API)
        await simulateAPICall(2500);
        
        // Verifica se deve lembrar login
        const rememberMe = document.getElementById('rememberMe').checked;
        if (rememberMe) {
            localStorage.setItem('bradesco_remember_login', 'true');
        }
        
        showNotification('Autenticação realizada com sucesso!', 'success');
        
        setTimeout(() => {
            window.location.href = 'funcionario.html';
        }, 1500);
        
    } catch (error) {
        hideLoading();
        showNotification('Credenciais inválidas. Tente novamente.', 'error');
    }
}

// =============================================
// UTILITÁRIOS DE VALIDAÇÃO
// =============================================

function formatCPF(input) {
    let value = input.value.replace(/\D/g, '');
    value = value.replace(/(\d{3})(\d)/, '$1.$2');
    value = value.replace(/(\d{3})(\d)/, '$1.$2');
    value = value.replace(/(\d{3})(\d{1,2})$/, '$1-$2');
    input.value = value;
}

function isValidCPF(cpf) {
    cpf = cpf.replace(/[^\d]/g, '');
    
    if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) {
        return false;
    }
    
    let sum = 0;
    for (let i = 0; i < 9; i++) {
        sum += parseInt(cpf.charAt(i)) * (10 - i);
    }
    let remainder = 11 - (sum % 11);
    if (remainder === 10 || remainder === 11) remainder = 0;
    if (remainder !== parseInt(cpf.charAt(9))) return false;
    
    sum = 0;
    for (let i = 0; i < 10; i++) {
        sum += parseInt(cpf.charAt(i)) * (11 - i);
    }
    remainder = 11 - (sum % 11);
    if (remainder === 10 || remainder === 11) remainder = 0;
    
    return remainder === parseInt(cpf.charAt(10));
}

function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

function togglePassword(inputId) {
    const input = document.getElementById(inputId);
    const button = input.nextElementSibling;
    const icon = button.querySelector('i');
    
    if (input.type === 'password') {
        input.type = 'text';
        icon.className = 'fas fa-eye-slash';
    } else {
        input.type = 'password';
        icon.className = 'fas fa-eye';
    }
}

// =============================================
// STATUS DO SISTEMA
// =============================================

async function checkSystemStatus() {
    console.log('🔍 Verificando status do sistema...');
    
    const statusChecks = {
        api: checkAPIStatus,
        database: checkDatabaseStatus,
        ia: checkIAStatus,
        security: checkSecurityStatus
    };
    
    // Executa todas as verificações em paralelo
    const results = await Promise.allSettled(
        Object.entries(statusChecks).map(async ([key, checkFn]) => {
            try {
                const result = await checkFn();
                return { key, ...result };
            } catch (error) {
                return { key, status: 'offline', message: 'Erro' };
            }
        })
    );
    
    // Atualiza interface com os resultados
    results.forEach(result => {
        if (result.status === 'fulfilled') {
            const { key, status, message } = result.value;
            updateStatusIndicator(key, status, message);
            app.systemStatus[key] = status;
        }
    });
    
    console.log('✅ Verificação de status concluída');
}

async function checkAPIStatus() {
    try {
        const response = await fetch('/api/config/status', { 
            method: 'GET',
            timeout: 5000 
        });
        return {
            status: response.ok ? 'online' : 'offline',
            message: response.ok ? 'Online' : 'Indisponível'
        };
    } catch {
        return { status: 'offline', message: 'Offline' };
    }
}

async function checkDatabaseStatus() {
    try {
        const response = await fetch('/api/bd/status', { 
            method: 'GET',
            timeout: 5000 
        });
        return {
            status: response.ok ? 'online' : 'warning',
            message: response.ok ? 'Operacional' : 'Parcial'
        };
    } catch {
        return { status: 'warning', message: 'Verificando...' };
    }
}

async function checkIAStatus() {
    // Simula verificação do sistema de IA
    await simulateAPICall(1000);
    return {
        status: 'online',
        message: 'Ativo'
    };
}

async function checkSecurityStatus() {
    // Verifica se HTTPS está ativo e outros aspectos de segurança
    const isSecure = window.location.protocol === 'https:' || window.location.hostname === 'localhost';
    return {
        status: isSecure ? 'online' : 'warning',
        message: isSecure ? 'Protegido' : 'Atenção'
    };
}

function updateStatusIndicator(type, status, message) {
    const indicator = document.getElementById(`${type}-status`);
    if (indicator) {
        indicator.className = `status-indicator ${status}`;
        indicator.textContent = message;
        
        // Adiciona animação de atualização
        indicator.style.animation = 'pulse 0.3s ease';
        setTimeout(() => {
            indicator.style.animation = '';
        }, 300);
    }
}

// =============================================
// ANIMAÇÕES E EFEITOS
// =============================================

function initializeAnimations() {
    // Anima números nas estatísticas
    animateCounters();
    
    // Anima cards com delay
    animateCardsOnLoad();
    
    // Parallax simples no background
    setupParallaxEffect();
}

function animateCounters() {
    const counters = document.querySelectorAll('.stat-content h3');
    counters.forEach(counter => {
        animateNumber(counter);
    });
}

function animateNumber(element) {
    const text = element.textContent;
    const number = parseFloat(text.replace(/[^\d.]/g, ''));
    
    if (isNaN(number)) return;
    
    const suffix = text.replace(/[\d.]/g, '');
    const duration = 2000;
    const steps = 60;
    const increment = number / steps;
    let current = 0;
    
    const timer = setInterval(() => {
        current += increment;
        if (current >= number) {
            element.textContent = text;
            clearInterval(timer);
        } else {
            element.textContent = Math.floor(current) + suffix;
        }
    }, duration / steps);
}

function animateCardsOnLoad() {
    const cards = document.querySelectorAll('.stat-card, .feature-card, .status-item');
    cards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(30px)';
        
        setTimeout(() => {
            card.style.transition = 'all 0.6s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, index * 100);
    });
}

function setupParallaxEffect() {
    const heroPattern = document.querySelector('.hero-pattern');
    if (!heroPattern) return;
    
    window.addEventListener('scroll', () => {
        const scrolled = window.pageYOffset;
        const rate = scrolled * -0.5;
        heroPattern.style.transform = `translate(${rate}px, ${rate * 0.5}px)`;
    });
}

// =============================================
// EVENT LISTENERS
// =============================================

function setupEventListeners() {
    // Event listeners para formulários
    const clienteForm = document.getElementById('clienteForm');
    const funcionarioForm = document.getElementById('funcionarioForm');
    
    if (clienteForm) {
        clienteForm.addEventListener('submit', loginCliente);
    }
    
    if (funcionarioForm) {
        funcionarioForm.addEventListener('submit', loginFuncionario);
    }
    
    // Event listener para formatação de CPF
    const cpfInput = document.getElementById('clienteCpf');
    if (cpfInput) {
        cpfInput.addEventListener('input', (e) => formatCPF(e.target));
    }
    
    // Event listeners para cards de estatísticas
    setupCardHoverEffects();
    
    // Event listener para verificação periódica de status
    setInterval(checkSystemStatus, 60000); // A cada 1 minuto
    
    // Event listener para tecla ESC fechar modais
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            hideLoading();
        }
    });
}

function setupCardHoverEffects() {
    const cards = document.querySelectorAll('.stat-card, .feature-card');
    
    cards.forEach(card => {
        card.addEventListener('mouseenter', () => {
            card.style.transform = 'translateY(-8px) scale(1.02)';
        });
        
        card.addEventListener('mouseleave', () => {
            card.style.transform = 'translateY(0) scale(1)';
        });
    });
}

// =============================================
// SISTEMA DE NOTIFICAÇÕES
// =============================================

function showNotification(message, type = 'info') {
    // Remove notificações anteriores
    const existingNotifications = document.querySelectorAll('.notification');
    existingNotifications.forEach(notif => notif.remove());
    
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.innerHTML = `
        <div class="notification-content">
            <i class="fas fa-${getNotificationIcon(type)}"></i>
            <span>${message}</span>
            <button class="notification-close" onclick="this.parentElement.parentElement.remove()">
                <i class="fas fa-times"></i>
            </button>
        </div>
    `;
    
    // Adiciona estilos inline para a notificação
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 10000;
        min-width: 300px;
        max-width: 400px;
        padding: 1rem;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        animation: slideInRight 0.3s ease;
        border-left: 4px solid ${getNotificationColor(type)};
        background: white;
        color: #333;
    `;
    
    document.body.appendChild(notification);
    
    // Remove automaticamente após 5 segundos
    setTimeout(() => {
        if (notification.parentElement) {
            notification.style.animation = 'slideOutRight 0.3s ease';
            setTimeout(() => notification.remove(), 300);
        }
    }, 5000);
}

function getNotificationIcon(type) {
    const icons = {
        success: 'check-circle',
        error: 'exclamation-circle',
        warning: 'exclamation-triangle',
        info: 'info-circle'
    };
    return icons[type] || 'info-circle';
}

function getNotificationColor(type) {
    const colors = {
        success: '#28a745',
        error: '#dc3545',
        warning: '#ffc107',
        info: '#17a2b8'
    };
    return colors[type] || '#17a2b8';
}

// =============================================
// LOADING E UTILITÁRIOS
// =============================================

function showLoading(message = 'Carregando...') {
    const overlay = document.getElementById('loadingOverlay');
    const loadingText = overlay.querySelector('p');
    
    if (loadingText) loadingText.textContent = message;
    
    overlay.classList.remove('hidden');
    app.loading = true;
}

function hideLoading() {
    const overlay = document.getElementById('loadingOverlay');
    overlay.classList.add('hidden');
    app.loading = false;
}

async function simulateAPICall(delay = 1000) {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            // 90% de sucesso, 10% de erro para simular cenários reais
            if (Math.random() > 0.1) {
                resolve();
            } else {
                reject(new Error('Simulated API error'));
            }
        }, delay);
    });
}

// =============================================
// ANIMAÇÕES CSS DINÂMICAS
// =============================================

// Adiciona animações CSS no head
const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOutRight {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
    
    @keyframes pulse {
        0%, 100% { transform: scale(1); }
        50% { transform: scale(1.05); }
    }
    
    .notification-content {
        display: flex;
        align-items: center;
        gap: 0.75rem;
    }
    
    .notification-close {
        margin-left: auto;
        background: none;
        border: none;
        color: #666;
        cursor: pointer;
        padding: 0.25rem;
        border-radius: 50%;
        transition: all 0.2s ease;
    }
    
    .notification-close:hover {
        background: rgba(0, 0, 0, 0.1);
        color: #333;
    }
`;
document.head.appendChild(style);

// =============================================
// EXPOSIÇÃO GLOBAL DE FUNÇÕES
// =============================================

// Torna funções disponíveis globalmente para uso no HTML
window.switchTab = switchTab;
window.loginCliente = loginCliente;
window.loginFuncionario = loginFuncionario;
window.formatCPF = formatCPF;
window.togglePassword = togglePassword;
window.showNotification = showNotification;

console.log('📋 Sistema JavaScript carregado e pronto!'); 