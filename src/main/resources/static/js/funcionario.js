// =============================================
// BRADESCO PIX MONITOR - PORTAL DO FUNCIONÁRIO
// JavaScript Moderno com Integração Backend
// =============================================

// Estado global da aplicação
const app = {
    currentSection: 'dashboard',
    isLoading: false,
    isSidebarCollapsed: false,
    user: {
        name: 'João Silva',
        role: 'Analista Senior',
        permissions: ['read', 'write', 'admin']
    },
    config: {
        apiBaseUrl: '/api',
        refreshInterval: 30000, // 30 segundos
        itemsPerPage: 10
    },
    data: {
        metrics: {},
        denuncias: [],
        contas: [],
        notifications: []
    },
    charts: {}
};

// =============================================
// INICIALIZAÇÃO
// =============================================

document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Portal do Funcionário - Inicializando...');
    initializeApp();
});

async function initializeApp() {
    try {
        showLoading('Inicializando sistema...');
        
        // Setup inicial
        setupEventListeners();
        await loadSystemConfig();
        await loadInitialData();
        
        // Inicializar componentes
        initializeCharts();
        initializeNavigation();
        initializeNotifications();
        
        // Carrega seção inicial
        showSection('dashboard');
        
        // Auto-refresh periódico
        setupAutoRefresh();
        
        hideLoading();
        console.log('✅ Sistema inicializado com sucesso');
        
    } catch (error) {
        console.error('❌ Erro na inicialização:', error);
        hideLoading();
        showNotification('Erro na inicialização do sistema', 'error');
    }
}

// =============================================
// CONFIGURAÇÃO E DADOS INICIAIS
// =============================================

async function loadSystemConfig() {
    try {
        const response = await fetch(`${app.config.apiBaseUrl}/config/interface`);
        if (response.ok) {
            const config = await response.json();
            app.config = { ...app.config, ...config };
            updateInterfaceElements();
        }
    } catch (error) {
        console.warn('Configurações do servidor não disponíveis');
    }
}

async function loadInitialData() {
    showLoading('Carregando dados...');
    
    try {
        // Carrega dados em paralelo
        const [metricsData, systemStatus] = await Promise.all([
            loadMetrics(),
            checkSystemStatus(),
        ]);
        
        app.data.metrics = metricsData;
        updateMetricsDisplay();
        
    } catch (error) {
        console.error('Erro ao carregar dados iniciais:', error);
    }
}

function updateInterfaceElements() {
    // Atualiza elementos da interface com configurações do servidor
    const elements = {
        'systemStatus': 'Sistema Online'
    };
    
    Object.entries(elements).forEach(([id, value]) => {
        const element = document.getElementById(id);
        if (element) element.textContent = value;
    });
}

// =============================================
// NAVEGAÇÃO E UI
// =============================================

function setupEventListeners() {
    // Event listeners para navegação
    document.querySelectorAll('.nav-item').forEach(item => {
        item.addEventListener('click', handleNavigation);
    });
    
    // Event listeners para busca global
    const globalSearch = document.getElementById('globalSearch');
    if (globalSearch) {
        globalSearch.addEventListener('input', debounce(handleGlobalSearch, 300));
    }
    
    // Event listeners para modal
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') closeModal();
    });
    
    // Event listeners para sidebar
    setupSidebarEvents();
}

function handleNavigation(event) {
    event.preventDefault();
    const section = event.currentTarget.dataset.section;
    if (section) {
        showSection(section);
        updateActiveNavigation(section);
    }
}

function showSection(sectionName) {
    // Oculta todas as seções
    document.querySelectorAll('.content-section').forEach(section => {
        section.classList.remove('active');
    });
    
    // Mostra a seção selecionada
    const targetSection = document.getElementById(`${sectionName}-section`);
    if (targetSection) {
        targetSection.classList.add('active');
        app.currentSection = sectionName;
        
        // Carrega dados específicos da seção
        loadSectionData(sectionName);
        
        console.log(`Seção ativa: ${sectionName}`);
    }
}

function updateActiveNavigation(sectionName) {
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
    });
    
    const activeItem = document.querySelector(`[data-section="${sectionName}"]`);
    if (activeItem) activeItem.classList.add('active');
}

async function loadSectionData(sectionName) {
    switch (sectionName) {
        case 'dashboard':
            await loadDashboardData();
            break;
        case 'denuncias':
            await loadDenunciasData();
            break;
        case 'contas':
            await loadContasData();
            break;
        case 'ia':
            await loadIAData();
            break;
        default:
            console.log(`Seção ${sectionName} não requer carregamento de dados`);
    }
}

// =============================================
// DASHBOARD
// =============================================

async function loadDashboardData() {
    try {
        showLoading('Atualizando dashboard...');
        
        await Promise.all([
            loadMetrics(),
            loadRecentAlerts(),
            loadRecentActivities(),
            updateCharts()
        ]);
        
        hideLoading();
    } catch (error) {
        console.error('Erro ao carregar dashboard:', error);
        hideLoading();
    }
}

async function loadMetrics() {
    try {
        const response = await fetch(`${app.config.apiBaseUrl}/config/metrics`);
        if (response.ok) {
            const metrics = await response.json();
            app.data.metrics = metrics;
            updateMetricsDisplay();
            return metrics;
        }
    } catch (error) {
        console.error('Erro ao carregar métricas:', error);
        // Dados de fallback
        return {
            denunciasHoje: 23,
            contasBloqueadas: 12,
            taxaDeteccao: 99.8,
            tempoAnalise: '1.2s'
        };
    }
}

function updateMetricsDisplay() {
    const metrics = app.data.metrics;
    
    const metricElements = {
        'denunciasHoje': metrics.denunciasHoje || 0,
        'contasBloqueadas': metrics.contasBloqueadas || 0,
        'taxaDeteccao': `${metrics.taxaDeteccao || 99.8}%`,
        'tempoAnalise': metrics.tempoAnalise || '1.2s'
    };
    
    Object.entries(metricElements).forEach(([id, value]) => {
        const element = document.getElementById(id);
        if (element) {
            animateNumber(element, value);
        }
    });
}

async function loadRecentAlerts() {
    const alertsList = document.getElementById('alertsList');
    if (!alertsList) return;
    
    // Simula carregamento de alertas
    const alerts = [
        {
            id: 1,
            type: 'URGENTE',
            message: 'Conta com 5 transações suspeitas detectadas',
            time: '5 min atrás',
            account: '12345-6'
        },
        {
            id: 2,
            type: 'ALTO',
            message: 'Padrão anômalo detectado pela IA',
            time: '12 min atrás',
            account: '67890-1'
        },
        {
            id: 3,
            type: 'MÉDIO',
            message: 'Transação acima do limite usual',
            time: '25 min atrás',
            account: '11111-2'
        }
    ];
    
    alertsList.innerHTML = alerts.map(alert => `
        <div class="alert-item ${alert.type.toLowerCase()}">
            <div class="alert-icon">
                <i class="fas fa-exclamation-triangle"></i>
            </div>
            <div class="alert-content">
                <strong>${alert.message}</strong>
                <small>Conta: ${alert.account} • ${alert.time}</small>
            </div>
            <button class="btn-small" onclick="viewAlert(${alert.id})">
                Ver
            </button>
        </div>
    `).join('');
}

async function loadRecentActivities() {
    const activitiesList = document.getElementById('activitiesList');
    if (!activitiesList) return;
    
    // Simula atividades recentes
    const activities = [
        {
            action: 'Conta bloqueada',
            user: 'Maria Santos',
            time: '2 min atrás',
            icon: 'fa-ban'
        },
        {
            action: 'Denúncia analisada',
            user: 'João Silva',
            time: '8 min atrás',
            icon: 'fa-check'
        },
        {
            action: 'Modelo IA atualizado',
            user: 'Sistema',
            time: '15 min atrás',
            icon: 'fa-brain'
        }
    ];
    
    activitiesList.innerHTML = activities.map(activity => `
        <div class="activity-item">
            <div class="activity-icon">
                <i class="fas ${activity.icon}"></i>
            </div>
            <div class="activity-content">
                <strong>${activity.action}</strong>
                <small>por ${activity.user} • ${activity.time}</small>
            </div>
        </div>
    `).join('');
}

// =============================================
// SISTEMA DE NOTIFICAÇÕES
// =============================================

function initializeNotifications() {
    // Carrega notificações iniciais
    loadNotifications();
    
    // Setup para marcar como lidas
    const markAllRead = document.querySelector('.mark-all-read');
    if (markAllRead) {
        markAllRead.addEventListener('click', markAllNotificationsRead);
    }
}

function toggleNotifications() {
    const dropdown = document.getElementById('notificationsDropdown');
    if (dropdown) {
        dropdown.classList.toggle('show');
    }
}

function toggleUserMenu() {
    const dropdown = document.getElementById('userDropdown');
    if (dropdown) {
        dropdown.classList.toggle('show');
    }
}

async function loadNotifications() {
    // Simula carregamento de notificações
    const notifications = [
        {
            id: 1,
            title: 'Nova denúncia registrada',
            message: 'Protocolo DEN-2024-001 criado',
            time: '5 min',
            read: false
        },
        {
            id: 2,
            title: 'Sistema IA atualizado',
            message: 'Novo modelo disponível',
            time: '1h',
            read: false
        },
        {
            id: 3,
            title: 'Relatório mensal pronto',
            message: 'Disponível para download',
            time: '2h',
            read: true
        }
    ];
    
    const notificationsList = document.getElementById('notificationsList');
    if (notificationsList) {
        notificationsList.innerHTML = notifications.map(notif => `
            <div class="notification-item ${notif.read ? 'read' : 'unread'}">
                <div class="notification-content">
                    <h5>${notif.title}</h5>
                    <p>${notif.message}</p>
                    <small>${notif.time} atrás</small>
                </div>
            </div>
        `).join('');
    }
    
    // Atualiza badge
    const unreadCount = notifications.filter(n => !n.read).length;
    const badge = document.getElementById('notificationBadge');
    if (badge) {
        badge.textContent = unreadCount;
        badge.style.display = unreadCount > 0 ? 'flex' : 'none';
    }
}

function showNotification(message, type = 'info') {
    const container = document.getElementById('notificationContainer');
    if (!container) return;
    
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.innerHTML = `
        <div class="notification-content">
            <i class="fas fa-${getNotificationIcon(type)}"></i>
            <span>${message}</span>
            <button onclick="this.parentElement.parentElement.remove()">
                <i class="fas fa-times"></i>
            </button>
        </div>
    `;
    
    // Estilo inline para a notificação
    notification.style.cssText = `
        background: white;
        padding: 1rem;
        margin-bottom: 0.5rem;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        border-left: 4px solid ${getNotificationColor(type)};
        animation: slideInRight 0.3s ease;
    `;
    
    container.appendChild(notification);
    
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
// GRÁFICOS E CHARTS
// =============================================

function initializeCharts() {
    // Inicializa gráficos pequenos das métricas
    initializeMetricCharts();
    
    // Inicializa gráficos principais
    initializeDashboardCharts();
}

function initializeMetricCharts() {
    const chartIds = ['denunciasChart', 'bloqueiosChart', 'iaChart', 'tempoChart'];
    
    chartIds.forEach(chartId => {
        const canvas = document.getElementById(chartId);
        if (canvas) {
            const ctx = canvas.getContext('2d');
            
            // Dados de exemplo para mini gráficos
            const data = generateMiniChartData();
            
            app.charts[chartId] = new Chart(ctx, {
                type: 'line',
                data: data,
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false }
                    },
                    scales: {
                        x: { display: false },
                        y: { display: false }
                    },
                    elements: {
                        point: { radius: 0 },
                        line: { borderWidth: 2 }
                    }
                }
            });
        }
    });
}

function initializeDashboardCharts() {
    // Gráfico de denúncias por período
    const denunciasCanvas = document.getElementById('denunciasPeriodoChart');
    if (denunciasCanvas) {
        const ctx = denunciasCanvas.getContext('2d');
        app.charts.denunciasPeriodo = new Chart(ctx, {
            type: 'line',
            data: {
                labels: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00'],
                datasets: [{
                    label: 'Denúncias',
                    data: [2, 1, 5, 8, 12, 6],
                    borderColor: '#cc092f',
                    backgroundColor: 'rgba(204, 9, 47, 0.1)',
                    tension: 0.4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                }
            }
        });
    }
    
    // Gráfico de tipos de fraude
    const fraudeCanvas = document.getElementById('tiposFraudeChart');
    if (fraudeCanvas) {
        const ctx = fraudeCanvas.getContext('2d');
        app.charts.tiposFraude = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Phishing', 'Conta Falsa', 'Transferência Ilícita', 'Outros'],
                datasets: [{
                    data: [45, 25, 20, 10],
                    backgroundColor: [
                        '#cc092f',
                        '#ffc107',
                        '#17a2b8',
                        '#6c757d'
                    ]
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false
            }
        });
    }
}

function generateMiniChartData() {
    const data = [];
    for (let i = 0; i < 10; i++) {
        data.push(Math.floor(Math.random() * 100));
    }
    
    return {
        labels: Array.from({length: 10}, (_, i) => i),
        datasets: [{
            data: data,
            borderColor: '#cc092f',
            backgroundColor: 'rgba(204, 9, 47, 0.1)'
        }]
    };
}

async function updateCharts() {
    // Atualiza gráficos com dados reais
    Object.values(app.charts).forEach(chart => {
        if (chart && chart.update) {
            chart.update();
        }
    });
}

// =============================================
// DENÚNCIAS
// =============================================

async function loadDenunciasData() {
    try {
        showLoading('Carregando denúncias...');
        
        // Simula carregamento de denúncias
        const denuncias = [
            {
                id: 1,
                protocolo: 'DEN-2024-001',
                data: '2024-06-02',
                cliente: 'João Silva Santos',
                motivo: 'Transação não autorizada',
                status: 'PENDENTE',
                funcionario: '-'
            },
            {
                id: 2,
                protocolo: 'DEN-2024-002',
                data: '2024-06-02',
                cliente: 'Maria Costa',
                motivo: 'Possível fraude',
                status: 'EM_ANALISE',
                funcionario: 'Ana Silva'
            }
        ];
        
        app.data.denuncias = denuncias;
        updateDenunciasTable();
        
        hideLoading();
    } catch (error) {
        console.error('Erro ao carregar denúncias:', error);
        hideLoading();
    }
}

function updateDenunciasTable() {
    const tbody = document.getElementById('denunciasTableBody');
    if (!tbody) return;
    
    tbody.innerHTML = app.data.denuncias.map(denuncia => `
        <tr>
            <td>${denuncia.protocolo}</td>
            <td>${formatDate(denuncia.data)}</td>
            <td>${denuncia.cliente}</td>
            <td>${denuncia.motivo}</td>
            <td><span class="status-badge status-${denuncia.status.toLowerCase()}">${denuncia.status}</span></td>
            <td>${denuncia.funcionario}</td>
            <td>
                <div class="action-buttons">
                    <button class="btn-action btn-view" onclick="viewDenuncia(${denuncia.id})" title="Visualizar">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="btn-action btn-edit" onclick="editDenuncia(${denuncia.id})" title="Editar">
                        <i class="fas fa-edit"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
    
    // Atualiza informações de paginação
    updatePaginationInfo('denuncias', app.data.denuncias.length);
}

// =============================================
// ANÁLISE IA
// =============================================

async function analyzeAccount(event) {
    event.preventDefault();
    
    const cpf = document.getElementById('iaCpf').value;
    if (!cpf || !isValidCPF(cpf)) {
        showNotification('CPF inválido', 'error');
        return;
    }
    
    try {
        showLoading('Analisando conta com IA...');
        
        // Simula análise de IA
        await simulateDelay(3000);
        
        const result = {
            cpf: cpf,
            score: 75,
            risco: 'MÉDIO',
            confianca: 89,
            recomendacoes: [
                'Monitorar transações futuras',
                'Verificar documentos adicionais',
                'Aplicar limite temporário'
            ]
        };
        
        displayIAResult(result);
        hideLoading();
        
    } catch (error) {
        console.error('Erro na análise IA:', error);
        hideLoading();
        showNotification('Erro na análise. Tente novamente.', 'error');
    }
}

function displayIAResult(result) {
    const resultSection = document.getElementById('iaResultSection');
    const resultContent = document.getElementById('iaResultContent');
    
    if (resultSection && resultContent) {
        resultContent.innerHTML = `
            <div class="ia-result">
                <div class="result-header">
                    <h4>Análise para CPF: ${result.cpf}</h4>
                </div>
                
                <div class="result-metrics">
                    <div class="metric-item">
                        <label>Score de Segurança:</label>
                        <span class="score-value score-${getScoreClass(result.score)}">${result.score}/100</span>
                    </div>
                    <div class="metric-item">
                        <label>Nível de Risco:</label>
                        <span class="risk-badge risk-${result.risco.toLowerCase()}">${result.risco}</span>
                    </div>
                    <div class="metric-item">
                        <label>Confiança da IA:</label>
                        <span>${result.confianca}%</span>
                    </div>
                </div>
                
                <div class="result-recommendations">
                    <h5>Recomendações:</h5>
                    <ul>
                        ${result.recomendacoes.map(rec => `<li>${rec}</li>`).join('')}
                    </ul>
                </div>
                
                <div class="result-actions">
                    <button class="btn-warning" onclick="monitorAccount('${result.cpf}')">
                        <i class="fas fa-eye"></i> Monitorar Conta
                    </button>
                    <button class="btn-danger" onclick="blockAccount('${result.cpf}')">
                        <i class="fas fa-ban"></i> Bloquear Conta
                    </button>
                </div>
            </div>
        `;
        
        resultSection.style.display = 'block';
    }
}

// =============================================
// UTILITÁRIOS
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
    if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) return false;
    
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

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('pt-BR');
}

function getScoreClass(score) {
    if (score >= 80) return 'good';
    if (score >= 60) return 'medium';
    return 'bad';
}

function animateNumber(element, targetValue) {
    const current = parseFloat(element.textContent) || 0;
    const target = parseFloat(targetValue.toString().replace(/[^\d.]/g, '')) || 0;
    const suffix = targetValue.toString().replace(/[\d.]/g, '');
    
    if (current === target) return;
    
    const duration = 1000;
    const steps = 30;
    const increment = (target - current) / steps;
    let currentStep = 0;
    
    const timer = setInterval(() => {
        currentStep++;
        const newValue = current + (increment * currentStep);
        
        if (currentStep >= steps) {
            element.textContent = targetValue;
            clearInterval(timer);
        } else {
            element.textContent = Math.floor(newValue) + suffix;
        }
    }, duration / steps);
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

function simulateDelay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

// =============================================
// SISTEMA DE STATUS
// =============================================

async function checkSystemStatus() {
    try {
        const response = await fetch(`${app.config.apiBaseUrl}/config/status`);
        if (response.ok) {
            const status = await response.json();
            updateSystemStatus(status);
            return status;
        }
    } catch (error) {
        console.warn('Status do sistema não disponível');
        return { status: 'unknown' };
    }
}

function updateSystemStatus(status) {
    const statusElement = document.getElementById('systemStatus');
    if (statusElement) {
        const isOnline = status.status === 'online';
        statusElement.innerHTML = `
            <i class="fas fa-circle ${isOnline ? 'text-success' : 'text-danger'}"></i>
            <span>${isOnline ? 'Sistema Online' : 'Sistema Offline'}</span>
        `;
    }
}

// =============================================
// LOADING E MODAIS
// =============================================

function showLoading(message = 'Carregando...') {
    const overlay = document.getElementById('loadingOverlay');
    const messageElement = document.getElementById('loadingMessage');
    
    if (messageElement) messageElement.textContent = message;
    if (overlay) overlay.classList.remove('hidden');
    
    app.isLoading = true;
}

function hideLoading() {
    const overlay = document.getElementById('loadingOverlay');
    if (overlay) overlay.classList.add('hidden');
    
    app.isLoading = false;
}

function openModal(title, content, footer = '') {
    const overlay = document.getElementById('modalOverlay');
    const titleElement = document.getElementById('modalTitle');
    const bodyElement = document.getElementById('modalBody');
    const footerElement = document.getElementById('modalFooter');
    
    if (titleElement) titleElement.textContent = title;
    if (bodyElement) bodyElement.innerHTML = content;
    if (footerElement) footerElement.innerHTML = footer;
    if (overlay) overlay.classList.remove('hidden');
}

function closeModal() {
    const overlay = document.getElementById('modalOverlay');
    if (overlay) overlay.classList.add('hidden');
}

// =============================================
// AUTO-REFRESH E SIDEBAR
// =============================================

function setupAutoRefresh() {
    setInterval(async () => {
        if (!app.isLoading && app.currentSection === 'dashboard') {
            try {
                await loadMetrics();
                await updateCharts();
            } catch (error) {
                console.warn('Erro no auto-refresh:', error);
            }
        }
    }, app.config.refreshInterval);
}

function setupSidebarEvents() {
    // Fechar dropdowns ao clicar fora
    document.addEventListener('click', (e) => {
        if (!e.target.closest('.notifications-bell')) {
            const notificationsDropdown = document.getElementById('notificationsDropdown');
            if (notificationsDropdown) notificationsDropdown.classList.remove('show');
        }
        
        if (!e.target.closest('.user-menu')) {
            const userDropdown = document.getElementById('userDropdown');
            if (userDropdown) userDropdown.classList.remove('show');
        }
    });
}

function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    if (sidebar) {
        sidebar.classList.toggle('collapsed');
        app.isSidebarCollapsed = !app.isSidebarCollapsed;
    }
}

// =============================================
// FUNÇÕES ESPECÍFICAS
// =============================================

function refreshDashboard() {
    loadDashboardData();
    showNotification('Dashboard atualizado', 'success');
}

function openQuickAction() {
    openModal(
        'Ação Rápida',
        `<div class="quick-actions">
            <button class="btn-primary" onclick="novaDenuncia()">Nova Denúncia</button>
            <button class="btn-warning" onclick="openModal('Bloquear Conta', 'Formulário de bloqueio...')">Bloquear Conta</button>
            <button class="btn-secondary" onclick="generateReport()">Gerar Relatório</button>
        </div>`,
        '<button class="btn-secondary" onclick="closeModal()">Fechar</button>'
    );
}

function novaDenuncia() {
    showNotification('Função em desenvolvimento', 'info');
}

function updatePaginationInfo(type, total) {
    const rangeElement = document.getElementById(`${type}Range`);
    const totalElement = document.getElementById(`${type}Total`);
    
    if (rangeElement) rangeElement.textContent = `1-${Math.min(app.config.itemsPerPage, total)}`;
    if (totalElement) totalElement.textContent = total;
}

// =============================================
// EXPOSIÇÃO GLOBAL
// =============================================

// Torna funções disponíveis globalmente
window.showSection = showSection;
window.toggleNotifications = toggleNotifications;
window.toggleUserMenu = toggleUserMenu;
window.toggleSidebar = toggleSidebar;
window.refreshDashboard = refreshDashboard;
window.openQuickAction = openQuickAction;
window.analyzeAccount = analyzeAccount;
window.formatCPF = formatCPF;
window.closeModal = closeModal;
window.showNotification = showNotification;

console.log('📋 Portal do Funcionário - JavaScript carregado!'); 