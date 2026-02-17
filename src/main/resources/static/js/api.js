// ============================================================
// api.js — Cliente HTTP + utilidades globales de DiveConnect
// ============================================================

const API_BASE_URL = '/api';

async function fetchAPI(endpoint, options = {}) {
    const token = localStorage.getItem('token');

    const defaultHeaders = { 'Content-Type': 'application/json' };
    if (token) defaultHeaders['Authorization'] = 'Bearer ' + token;

    const config = {
        ...options,
        headers: { ...defaultHeaders, ...(options.headers || {}) }
    };

    const response = await fetch(API_BASE_URL + endpoint, config);

    // Token inválido o expirado → login
    if (response.status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/pages/login.html';
        return null;
    }

    // Sin contenido
    if (response.status === 204) return null;

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || 'Error ' + response.status);
    }

    return data;
}

// ── Autenticación ─────────────────────────────────────────
function getCurrentUser() {
    const raw = localStorage.getItem('user');
    return raw ? JSON.parse(raw) : null;
}

function isAuthenticated() {
    return !!localStorage.getItem('token');
}

function requireAuth() {
    if (!isAuthenticated()) window.location.href = '/pages/login.html';
}

function redirectIfAuthenticated() {
    if (isAuthenticated()) window.location.href = '/pages/feed.html';
}

// ── Alertas (funciona en CUALQUIER página, sin .container) ─
function showAlert(message, type) {
    type = type || 'info';

    const prev = document.getElementById('__dc_alert__');
    if (prev) prev.remove();

    const div = document.createElement('div');
    div.id = '__dc_alert__';

    const colors = {
        success: { bg: '#e8f5e9', color: '#2e7d32', border: '#a5d6a7' },
        error:   { bg: '#ffebee', color: '#c62828', border: '#ef9a9a' },
        info:    { bg: '#e3f2fd', color: '#1565c0', border: '#90caf9' }
    };
    const c = colors[type] || colors.info;

    div.style.cssText =
        'position:fixed;top:72px;right:16px;z-index:99999;' +
        'padding:12px 18px;border-radius:8px;font-size:14px;font-weight:500;' +
        'max-width:360px;box-shadow:0 4px 16px rgba(0,0,0,0.15);' +
        'background:' + c.bg + ';color:' + c.color + ';border:1px solid ' + c.border;

    div.textContent = message;
    document.body.appendChild(div);
    setTimeout(() => { if (div.parentNode) div.remove(); }, 4500);
}

// ── Formato de fechas ─────────────────────────────────────
function formatFecha(fechaStr) {
    if (!fechaStr) return '';
    const d    = new Date(fechaStr);
    const diff = Date.now() - d.getTime();
    const min  = Math.floor(diff / 60000);
    const h    = Math.floor(min / 60);
    const days = Math.floor(h / 24);
    if (days > 7)  return d.toLocaleDateString('es-ES', { day: 'numeric', month: 'short' });
    if (days > 0)  return 'Hace ' + days + (days === 1 ? ' día' : ' días');
    if (h > 0)     return 'Hace ' + h + (h === 1 ? ' hora' : ' horas');
    if (min > 0)   return 'Hace ' + min + ' min';
    return 'Ahora mismo';
}

// ── Escapar HTML (anti-XSS) ───────────────────────────────
function escapeHtml(text) {
    if (!text) return '';
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}