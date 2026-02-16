// Módulo de autenticación

async function login(usernameOrEmail, password) {
    try {
        const data = await fetchAPI('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ usernameOrEmail, password }),
        });
        
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify(data.usuario));
        
        return { success: true, data };
    } catch (error) {
        return { success: false, message: error.message };
    }
}

async function register(userData) {
    try {
        await fetchAPI('/auth/registro', {
            method: 'POST',
            body: JSON.stringify(userData),
        });
        
        return { success: true };
    } catch (error) {
        return { success: false, message: error.message };
    }
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/pages/login.html';
}

function getCurrentUser() {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
}

function isAuthenticated() {
    return !!localStorage.getItem('token');
}

// Proteger páginas que requieren autenticación
function requireAuth() {
    if (!isAuthenticated()) {
        window.location.href = '/pages/login.html';
    }
}

// Redirigir si ya está autenticado (para login/register)
function redirectIfAuthenticated() {
    if (isAuthenticated()) {
        window.location.href = '/pages/feed.html';
    }
}