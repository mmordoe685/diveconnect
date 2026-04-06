// ============================================================
// api.js — Cliente HTTP + utilidades globales de DiveConnect
// ============================================================

const API_BASE_URL = '/api';

/**
 * Realiza una petición autenticada a la API.
 * Gestiona:
 *   - Token JWT en cabecera Authorization
 *   - 401 → limpia sesión y redirige a login
 *   - 403 → limpia sesión y redirige a login (token expirado / sin permisos)
 *   - 204 → devuelve null (sin cuerpo)
 *   - Cuerpos vacíos o no-JSON → no explota, lanza Error descriptivo
 */
async function fetchAPI(endpoint, options = {}) {
    const token = localStorage.getItem('token');

    const headers = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = 'Bearer ' + token;

    const config = {
        ...options,
        headers: { ...headers, ...(options.headers || {}) }
    };

    let response;
    try {
        response = await fetch(API_BASE_URL + endpoint, config);
    } catch (networkError) {
        throw new Error('No se pudo conectar con el servidor. Comprueba tu conexión.');
    }

    // Token inválido / expirado
    if (response.status === 401) {
        _limpiarSesionYRedirigir();
        return null;
    }

    // Acceso denegado (también puede indicar token expirado cuando el
    // servidor devuelve 403 en lugar de 401 para rutas sin entrypoint)
    if (response.status === 403) {
        _limpiarSesionYRedirigir();
        return null;
    }

    // Sin contenido
    if (response.status === 204) return null;

    // Intentar parsear JSON de forma segura
    const contentType = response.headers.get('content-type') || '';
    let data = null;

    if (contentType.includes('application/json')) {
        try {
            data = await response.json();
        } catch {
            // Cuerpo marcado como JSON pero vacío o malformado
            if (!response.ok) {
                throw new Error('Error ' + response.status + ' del servidor.');
            }
            return null;
        }
    } else if (!response.ok) {
        // Respuesta de error sin cuerpo JSON (p.ej. 500 de proxy)
        throw new Error('Error ' + response.status + ' del servidor.');
    }

    if (!response.ok) {
        throw new Error((data && data.message) || 'Error ' + response.status);
    }

    return data;
}

// ── Sesión ────────────────────────────────────────────────────
function _limpiarSesionYRedirigir() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    // Solo redirigir si no estamos ya en la página de login
    if (!window.location.pathname.includes('/pages/login')) {
        window.location.href = '/pages/login.html';
    }
}

function getCurrentUser() {
    const raw = localStorage.getItem('user');
    try { return raw ? JSON.parse(raw) : null; }
    catch { return null; }
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

// ── Alertas flotantes ─────────────────────────────────────────
function showAlert(message, type) {
    type = type || 'info';

    const prev = document.getElementById('__dc_alert__');
    if (prev) prev.remove();

    const div = document.createElement('div');
    div.id = '__dc_alert__';

    const palette = {
        success: { bg: '#DCFCE7', color: '#166534', border: '#86EFAC' },
        error:   { bg: '#FEE2E2', color: '#991B1B', border: '#FCA5A5' },
        info:    { bg: '#DBEAFE', color: '#1E40AF', border: '#93C5FD' }
    };
    const c = palette[type] || palette.info;

    div.style.cssText =
        'position:fixed;top:72px;right:16px;z-index:99999;' +
        'padding:12px 20px;border-radius:10px;font-size:14px;font-weight:600;' +
        'max-width:380px;box-shadow:0 4px 20px rgba(0,0,0,0.15);' +
        'background:' + c.bg + ';color:' + c.color + ';border:1px solid ' + c.border + ';' +
        'animation:slideIn .25s ease';

    // Animación de entrada
    if (!document.getElementById('__dc_alert_style__')) {
        const style = document.createElement('style');
        style.id = '__dc_alert_style__';
        style.textContent = '@keyframes slideIn{from{opacity:0;transform:translateY(-8px)}to{opacity:1;transform:translateY(0)}}';
        document.head.appendChild(style);
    }

    div.textContent = message;
    document.body.appendChild(div);
    setTimeout(() => { if (div.parentNode) div.remove(); }, 4500);
}

// ── Formato de fechas ─────────────────────────────────────────
function formatFecha(fechaStr) {
    if (!fechaStr) return '';
    const d    = new Date(fechaStr);
    if (isNaN(d.getTime())) return '';
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

// ── Escape HTML (anti-XSS) ────────────────────────────────────
function escapeHtml(text) {
    if (text === null || text === undefined) return '';
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}
