/**
 * nav.js — DiveConnect role-aware navigation (Instagram-style)
 *
 * Renderiza:
 *  - Topbar minimalista con logo y menú de usuario (y campanita de notis)
 *  - Dock inferior fijo tipo app: Feed / Buscar / Crear / Mapa / Perfil
 *
 * Inclúyelo tras api.js y llama a initNav() en DOMContentLoaded.
 */

/**
 * Pestañas del dock inferior según tipo de usuario.
 * Cada entrada: { href, label, icon (SVG path), role? }
 * El botón "Crear" abre un modal (no navega).
 */
const DOCK_TABS_COMUN = [
  { key: 'feed',    href: '/pages/feed.html',     label: 'Inicio' },
  { key: 'search',  href: '/pages/buscar.html',   label: 'Explorar' },
  { key: 'create',  href: '#',                    label: 'Crear',   action: 'create' },
  { key: 'mapa',    href: '/pages/mapa.html',     label: 'Mapa' },
  { key: 'perfil',  href: '/pages/Perfil.html',   label: 'Perfil' },
];

const DOCK_TABS_EMPRESA = [
  { key: 'feed',    href: '/pages/feed.html',                  label: 'Inicio' },
  { key: 'mapa',    href: '/pages/mapa.html',                  label: 'Mapa' },
  { key: 'create',  href: '#',                                 label: 'Crear',  action: 'create' },
  { key: 'panel',   href: '/pages/empresa/dashboard.html',     label: 'Panel' },
  { key: 'perfil',  href: '/pages/Perfil.html',                label: 'Perfil' },
];

const DOCK_TABS_ADMIN = [
  { key: 'admin',   href: '/pages/admin/dashboard.html',       label: 'Panel' },
  { key: 'users',   href: '/pages/admin/usuarios.html',        label: 'Usuarios' },
  { key: 'feed',    href: '/pages/feed.html',                  label: 'Feed' },
  { key: 'mapa',    href: '/pages/mapa.html',                  label: 'Mapa' },
  { key: 'perfil',  href: '/pages/Perfil.html',                label: 'Perfil' },
];

/** SVGs por clave de pestaña — trazados con tema submarinismo. */
const ICONS = {
  feed: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 11.5 12 4l9 7.5"/><path d="M5 10v10h5v-6h4v6h5V10"/></svg>`,
  search: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/></svg>`,
  create: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="5"/><path d="M12 8v8M8 12h8"/></svg>`,
  mapa: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M9 4 3 6v14l6-2 6 2 6-2V4l-6 2Z"/><path d="M9 4v14M15 6v14"/></svg>`,
  perfil: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="8" r="4"/><path d="M4 21c0-4.4 3.6-8 8-8s8 3.6 8 8"/></svg>`,
  panel: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="9"/><rect x="14" y="3" width="7" height="5"/><rect x="14" y="12" width="7" height="9"/><rect x="3" y="16" width="7" height="5"/></svg>`,
  users: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="8" r="4"/><path d="M2 21c0-3.9 3.1-7 7-7s7 3.1 7 7"/><circle cx="17" cy="7" r="3"/><path d="M15 14h.5c3 0 5.5 2.5 5.5 5.5V21"/></svg>`,
  admin: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2 4 5v6c0 5 3.6 9.4 8 11 4.4-1.6 8-6 8-11V5Z"/><path d="m9 12 2 2 4-4"/></svg>`,
};

/** SVG decorativo de regulador/burbujas para el logo. */
const LOGO_SVG = `
  <svg viewBox="0 0 28 28" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <path d="M3 8c.7.6 1.4 1.1 2.8 1.1 2.8 0 2.8-2.2 5.6-2.2 2.9 0 2.7 2.2 5.6 2.2 2.8 0 2.8-2.2 5.6-2.2 1.4 0 2.1.5 2.8 1.1"/>
    <path d="M3 14c.7.6 1.4 1.1 2.8 1.1 2.8 0 2.8-2.2 5.6-2.2 2.9 0 2.7 2.2 5.6 2.2 2.8 0 2.8-2.2 5.6-2.2 1.4 0 2.1.5 2.8 1.1"/>
    <path d="M3 20c.7.6 1.4 1.1 2.8 1.1 2.8 0 2.8-2.2 5.6-2.2 2.9 0 2.7 2.2 5.6 2.2 2.8 0 2.8-2.2 5.6-2.2 1.4 0 2.1.5 2.8 1.1"/>
  </svg>`;

function getDockTabs(role) {
  if (role === 'ADMINISTRADOR')   return DOCK_TABS_ADMIN;
  if (role === 'USUARIO_EMPRESA') return DOCK_TABS_EMPRESA;
  return DOCK_TABS_COMUN;
}

function initNav() {
  const topbar = document.getElementById('mainNavbar');
  if (!topbar) return;

  const userStr = localStorage.getItem('user');
  const token   = localStorage.getItem('token');

  // No autenticado → navbar público con login/register
  if (!token || !userStr) {
    _renderPublicNav(topbar);
    return;
  }

  let user;
  try { user = JSON.parse(userStr); } catch { _renderPublicNav(topbar); return; }

  const role = user.tipoUsuario || 'USUARIO_COMUN';
  const tabs = getDockTabs(role);
  const activeKey = _guessActiveTab();

  // ── Topbar minimalista (solo logo + notificaciones + logout)
  topbar.innerHTML = `
    <div class="topbar-inner">
      <a href="/pages/feed.html" class="topbar-logo">
        ${LOGO_SVG}
        <span class="topbar-logo-text">Dive<span>Connect</span></span>
      </a>
      <div class="topbar-actions">
        <button class="topbar-icon-btn" aria-label="Notificaciones" title="Notificaciones">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8a6 6 0 1 0-12 0c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.7 21a2 2 0 0 1-3.4 0"/></svg>
        </button>
        <button class="topbar-icon-btn" aria-label="Salir" title="Cerrar sesión" onclick="logout()">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="m16 17 5-5-5-5"/><path d="M21 12H9"/></svg>
        </button>
      </div>
    </div>
  `;

  // ── Dock inferior fijo
  const existingDock = document.getElementById('mobileDock');
  if (existingDock) existingDock.remove();

  const dock = document.createElement('nav');
  dock.id = 'mobileDock';
  dock.className = 'dock';
  dock.innerHTML = tabs.map(t => {
    const isCreate = t.action === 'create';
    const active = !isCreate && activeKey === t.key;
    const iconHtml = ICONS[t.key] || ICONS.feed;
    if (isCreate) {
      return `
        <button class="dock-item dock-create" onclick="openCreateSheet()" aria-label="Crear">
          <span class="dock-create-circle">${iconHtml}</span>
          <span class="dock-label">${t.label}</span>
        </button>`;
    }
    return `
      <a class="dock-item${active ? ' active' : ''}" href="${t.href}" aria-label="${t.label}">
        <span class="dock-ico">${iconHtml}</span>
        <span class="dock-label">${t.label}</span>
      </a>`;
  }).join('');
  document.body.appendChild(dock);

  // Padding inferior para que el contenido no quede oculto
  document.body.classList.add('has-dock');

  // Inyectar hoja de creación si aún no está
  if (!document.getElementById('createSheet')) _injectCreateSheet();
}

function _guessActiveTab() {
  const p = window.location.pathname.toLowerCase();
  if (p.endsWith('feed.html')) return 'feed';
  if (p.endsWith('mapa.html')) return 'mapa';
  if (p.endsWith('perfil.html')) return 'perfil';
  if (p.endsWith('buscar.html')) return 'search';
  if (p.includes('/admin/')) return 'admin';
  if (p.includes('/empresa/')) return 'panel';
  return '';
}

function _renderPublicNav(navbar) {
  navbar.innerHTML = `
    <div class="topbar-inner">
      <a href="/index.html" class="topbar-logo">
        ${LOGO_SVG}
        <span class="topbar-logo-text">Dive<span>Connect</span></span>
      </a>
      <nav class="topbar-public-menu">
        <a href="/index.html" class="nav-link">Inicio</a>
        <a href="/pages/login.html" class="btn btn-sm btn-primary">Iniciar sesión</a>
        <a href="/pages/register.html" class="btn btn-sm btn-outline">Registrarse</a>
      </nav>
    </div>
  `;
}

/** Hoja inferior "Crear" que lanza el flujo de publicación o historia. */
function _injectCreateSheet() {
  const sheet = document.createElement('div');
  sheet.id = 'createSheet';
  sheet.className = 'create-sheet-backdrop';
  sheet.innerHTML = `
    <div class="create-sheet" role="dialog" aria-modal="true" aria-label="Crear contenido">
      <div class="create-sheet-handle"></div>
      <h3>¿Qué quieres compartir?</h3>
      <p class="create-sheet-sub">Comparte tu última inmersión con la comunidad</p>

      <button class="create-sheet-option" onclick="_createChoice('publicacion')">
        <span class="create-sheet-icon" style="background:linear-gradient(135deg,#00D4AA,#5B5EA6)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="3"/><circle cx="9" cy="9" r="2"/><path d="m21 15-5-5L5 21"/></svg>
        </span>
        <div>
          <strong>Publicación</strong>
          <small>Foto o video de tu inmersión (queda en tu perfil)</small>
        </div>
      </button>

      <button class="create-sheet-option" onclick="_createChoice('historia')">
        <span class="create-sheet-icon" style="background:linear-gradient(135deg,#F5A623,#FF6B6B)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M12 3v3M12 18v3M3 12h3M18 12h3"/></svg>
        </span>
        <div>
          <strong>Historia</strong>
          <small>Desaparece a las 24h — ideal para un reel del día</small>
        </div>
      </button>

      <button class="create-sheet-close" onclick="closeCreateSheet()">Cancelar</button>
    </div>
  `;
  document.body.appendChild(sheet);
  sheet.addEventListener('click', (e) => {
    if (e.target === sheet) closeCreateSheet();
  });
}

function openCreateSheet() {
  const s = document.getElementById('createSheet');
  if (s) s.classList.add('open');
}

function closeCreateSheet() {
  const s = document.getElementById('createSheet');
  if (s) s.classList.remove('open');
}

function _createChoice(type) {
  closeCreateSheet();
  // El feed y otras páginas pueden sobrescribir openCreatePublicacion/openCreateHistoria.
  if (type === 'publicacion') {
    if (typeof window.openCreatePublicacion === 'function') {
      window.openCreatePublicacion();
    } else {
      window.location.href = '/pages/feed.html?create=publicacion';
    }
  } else if (type === 'historia') {
    if (typeof window.openCreateHistoria === 'function') {
      window.openCreateHistoria();
    } else {
      window.location.href = '/pages/feed.html?create=historia';
    }
  }
}

function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  window.location.href = '/pages/login.html';
}

/** Guard: redirect to login if not authenticated */
function requireAuth() {
  if (!localStorage.getItem('token')) {
    window.location.href = '/pages/login.html';
    return false;
  }
  return true;
}

/** Guard: redirect if role doesn't match */
function requireRole(...roles) {
  if (!requireAuth()) return false;
  try {
    const user = JSON.parse(localStorage.getItem('user'));
    if (!roles.includes(user.tipoUsuario)) {
      window.location.href = '/pages/feed.html';
      return false;
    }
  } catch {
    window.location.href = '/pages/login.html';
    return false;
  }
  return true;
}
