// ============================================================
// auth.js — Módulo de autenticación
// ============================================================

async function login(usernameOrEmail, password) {
    const data = await fetchAPI('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ usernameOrEmail, password })
    });
    if (data) {
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify(data.usuario));
    }
    return data;
}

async function registrarUsuario(userData) {
    return await fetchAPI('/auth/registro', {
        method: 'POST',
        body: JSON.stringify(userData)
    });
}

function logout() {
    localStorage.clear();
    window.location.href = '/pages/login.html';
}