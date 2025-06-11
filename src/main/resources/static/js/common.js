// Formatação de valores monetários
export function formatarValor(valor) {
    if (!valor) return 'R$ 0,00';
    return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
    }).format(valor);
}

// Formatação de data e hora
export function formatarDataHora(dataHora) {
    if (!dataHora) return '';
    return new Date(dataHora).toLocaleString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Exibição de mensagens de sucesso
export function mostrarMensagemSucesso(mensagem) {
    const successMessage = document.getElementById('success-message');
    if (successMessage) {
        successMessage.textContent = mensagem;
        successMessage.style.display = 'block';
        setTimeout(() => {
            successMessage.style.display = 'none';
        }, 3000);
    }
}

// Exibição de mensagens de erro
export function mostrarMensagemErro(mensagem) {
    const errorMessage = document.getElementById('error-message');
    if (errorMessage) {
        errorMessage.textContent = mensagem;
        errorMessage.style.display = 'block';
        setTimeout(() => {
            errorMessage.style.display = 'none';
        }, 3000);
    }
}

// Validação de ID de conta
export function validarIdConta(idConta) {
    const regex = /^\d{6,9}$/;
    return regex.test(idConta);
}

// Validação de valor monetário
export function validarValor(valor) {
    if (!valor) return false;
    const valorNumerico = parseFloat(valor.replace(/[^\d,-]/g, '').replace(',', '.'));
    return !isNaN(valorNumerico) && valorNumerico > 0;
}

// Função para fazer requisições à API
export async function fazerRequisicao(endpoint, metodo = 'GET', dados = null) {
    try {
        const options = {
            method: metodo,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        };

        if (dados) {
            options.body = JSON.stringify(dados);
        }

        const response = await fetch(`/api/${endpoint}`, options);
        
        if (!response.ok) {
            throw new Error(`Erro na requisição: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('Erro na requisição:', error);
        throw error;
    }
}

// Verificação de autenticação
export function verificarAutenticacao() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/login.html';
    }
}

// Função de logout
export function logout() {
    localStorage.removeItem('token');
    window.location.href = '/index.html';
}

// Gerenciamento de modais
export function inicializarModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        window.addEventListener('click', (event) => {
            if (event.target === modal) {
                fecharModal(modalId);
            }
        });
    }
}

export function mostrarModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'block';
    }
}

export function fecharModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
    }
}

// Formatação de números
export function formatarNumeroDenuncias(numero) {
    return new Intl.NumberFormat('pt-BR').format(numero);
}

// Formatação de status de conta
export function formatarStatusConta(status) {
    return status === 'active' ? 'Ativa' : 'Bloqueada';
}

// Obtenção de classe CSS para status
export function obterClasseStatus(status) {
    return status === 'active' ? 'active' : 'blocked';
}

// Função de debounce para otimização de performance
export function debounce(func, wait) {
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

// Função de throttle para otimização de performance
export function throttle(func, limit) {
    let inThrottle;
    return function executedFunction(...args) {
        if (!inThrottle) {
            func(...args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
} 