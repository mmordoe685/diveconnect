/**
 * nav.js — DiveConnect role-aware navigation
 * Renders the correct navbar links depending on the user's TipoUsuario.
 * Include AFTER api.js. Call initNav() in DOMContentLoaded.
 */

const NAV_LINKS = {
  USUARIO_COMUN: [
    { href: '/pages/feed.html',        label: '🌊 Feed' },
    { href: '/pages/Inmersiones.html', label: '🤿 Inmersiones' },
    { href: '/pages/centros.html',     label: '🏫 Centros' },
    { href: '/pages/reservas.html',    label: '📋 Mis Reservas' },
    { href: '/pages/Perfil.html',      label: '👤 Mi Perfil' },
  ],
  USUARIO_EMPRESA: [
    { href: '/pages/feed.html',                    label: '🌊 Feed' },
    { href: '/pages/empresa/dashboard.html',       label: '📊 Mi Panel' },
    { href: '/pages/empresa/mi-centro.html',       label: '🏫 Mi Centro' },
    { href: '/pages/empresa/mis-inmersiones.html', label: '🤿 Inmersiones' },
    { href: '/pages/empresa/gestionar-reservas.html', label: '📋 Reservas' },
    { href: '/pages/Perfil.html',                  label: '👤 Mi Perfil' },
  ],
  ADMINISTRADOR: [
    { href: '/pages/admin/dashboard.html',   label: '📊 Panel Admin' },
    { href: '/pages/admin/usuarios.html',    label: '👥 Usuarios' },
    { href: '/pages/admin/centros.html',     label: '🏫 Centros' },
    { href: '/pages/admin/inmersiones.html', label: '🤿 Inmersiones' },
    { href: '/pages/admin/reservas.html',    label: '📋 Reservas' },
    { href: '/pages/feed.html',              label: '🌊 Feed' },
  ],
};

function initNav() {
  const navbar = document.getElementById('mainNavbar');
  if (!navbar) return;

  const userStr = localStorage.getItem('user');
  const token   = localStorage.getItem('token');

  // Not logged in — show minimal nav
  if (!token || !userStr) {
    _renderPublicNav(navbar);
    return;
  }

  let user;
  try { user = JSON.parse(userStr); } catch { _renderPublicNav(navbar); return; }

  const role  = user.tipoUsuario || 'USUARIO_COMUN';
  const links = NAV_LINKS[role] || NAV_LINKS.USUARIO_COMUN;

  navbar.innerHTML = `
    <div class="navbar-container">
      <div class="navbar-logo">
        <span>🌊</span><span>DiveConnect</span>
      </div>
      <nav class="navbar-menu" id="navMenu">
        ${links.map(l => `<a href="${l.href}" class="nav-link${_isActive(l.href) ? ' active' : ''}">${l.label}</a>`).join('')}
      </nav>
      <div class="navbar-actions">
        <span class="badge ${_roleBadge(role)}">${_roleLabel(role)}</span>
        <span id="navUsername" class="nav-username">${user.username || ''}</span>
        <button class="btn btn-sm btn-outline" onclick="logout()">Salir</button>
      </div>
    </div>
  `;
}

function _isActive(href) {
  return window.location.pathname === href ||
         window.location.pathname.endsWith(href.split('/').pop());
}

function _roleBadge(role) {
  return role === 'ADMINISTRADOR' ? 'badge-admin'
       : role === 'USUARIO_EMPRESA' ? 'badge-empresa'
       : 'badge-comun';
}

function _roleLabel(role) {
  return role === 'ADMINISTRADOR' ? 'Admin'
       : role === 'USUARIO_EMPRESA' ? 'Empresa'
       : 'Buceador';
}

function _renderPublicNav(navbar) {
  navbar.innerHTML = `
    <div class="navbar-container">
      <div class="navbar-logo">
        <span>🌊</span><span>DiveConnect</span>
      </div>
      <nav class="navbar-menu">
        <a href="/index.html" class="nav-link">Inicio</a>
        <a href="/pages/Inmersiones.html" class="nav-link">Inmersiones</a>
        <a href="/pages/centros.html" class="nav-link">Centros</a>
      </nav>
      <div class="navbar-actions">
        <a href="/pages/login.html" class="btn btn-sm btn-primary">Iniciar sesión</a>
        <a href="/pages/register.html" class="btn btn-sm btn-outline">Registrarse</a>
      </div>
    </div>
  `;
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
      alert('No tienes permiso para acceder a esta página.');
      window.location.href = '/pages/feed.html';
      return false;
    }
  } catch {
    window.location.href = '/pages/login.html';
    return false;
  }
  return true;
}
