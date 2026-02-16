// Módulo de publicaciones

async function getFeed(page = 0, size = 10) {
    return await fetchAPI(`/publicaciones/feed?page=${page}&size=${size}`);
}

async function createPublicacion(data) {
    return await fetchAPI('/publicaciones', {
        method: 'POST',
        body: JSON.stringify(data),
    });
}

async function likePublicacion(id) {
    return await fetchAPI(`/publicaciones/${id}/like`, {
        method: 'POST',
    });
}

async function unlikePublicacion(id) {
    return await fetchAPI(`/publicaciones/${id}/like`, {
        method: 'DELETE',
    });
}

async function deletePublicacion(id) {
    return await fetchAPI(`/publicaciones/${id}`, {
        method: 'DELETE',
    });
}

async function getComentarios(publicacionId) {
    return await fetchAPI(`/publicaciones/${publicacionId}/comentarios`);
}

async function createComentario(publicacionId, contenido) {
    return await fetchAPI(`/publicaciones/${publicacionId}/comentarios`, {
        method: 'POST',
        body: JSON.stringify({ contenido }),
    });
}

// Renderizar una publicación
function renderPublicacion(publicacion) {
    const user = getCurrentUser();
    const esPropia = user && user.id === publicacion.usuarioId;
    
    return `
        <div class="publicacion" data-id="${publicacion.id}">
            <div class="publicacion-header">
                <div class="publicacion-avatar">
                    ${publicacion.usuarioUsername.charAt(0).toUpperCase()}
                </div>
                <div class="publicacion-user">
                    <div class="publicacion-username">${publicacion.usuarioUsername}</div>
                    <div class="publicacion-fecha">${formatFecha(publicacion.fechaPublicacion)}</div>
                </div>
                ${esPropia ? `
                    <button class="btn-danger btn-sm" onclick="handleDeletePublicacion(${publicacion.id})">
                        Eliminar
                    </button>
                ` : ''}
            </div>
            
            <div class="publicacion-contenido">${publicacion.contenido}</div>
            
            ${publicacion.imagenUrl ? `
                <img src="${publicacion.imagenUrl}" alt="Imagen" class="publicacion-imagen">
            ` : ''}
            
            ${publicacion.lugarInmersion ? `
                <div class="publicacion-detalles">
                    <div class="detalle-item">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                            <circle cx="12" cy="10" r="3"></circle>
                        </svg>
                        <strong>${publicacion.lugarInmersion}</strong>
                    </div>
                    ${publicacion.profundidadMaxima ? `
                        <div class="detalle-item">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <circle cx="12" cy="12" r="10"></circle>
                                <path d="M8 14s1.5 2 4 2 4-2 4-2"></path>
                                <line x1="9" y1="9" x2="9.01" y2="9"></line>
                                <line x1="15" y1="9" x2="15.01" y2="9"></line>
                            </svg>
                            Profundidad: ${publicacion.profundidadMaxima}m
                        </div>
                    ` : ''}
                    ${publicacion.temperaturaAgua ? `
                        <div class="detalle-item">
                            Temperatura: ${publicacion.temperaturaAgua}°C
                        </div>
                    ` : ''}
                    ${publicacion.especiesVistas ? `
                        <div class="detalle-item">
                            <strong>Especies:</strong> ${publicacion.especiesVistas}
                        </div>
                    ` : ''}
                </div>
            ` : ''}
            
            <div class="publicacion-actions">
                <button class="action-btn ${publicacion.likedByCurrentUser ? 'liked' : ''}" 
                        onclick="handleLike(${publicacion.id}, ${publicacion.likedByCurrentUser})">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
                    </svg>
                    <span class="like-count">${publicacion.numeroLikes}</span>
                </button>
                
                <button class="action-btn" onclick="toggleComentarios(${publicacion.id})">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
                    </svg>
                    ${publicacion.numeroComentarios} comentarios
                </button>
            </div>
            
            <div class="comentarios-section hidden" id="comentarios-${publicacion.id}">
                <div class="comentario-form">
                    <textarea class="form-input" placeholder="Escribe un comentario..." id="comentario-input-${publicacion.id}" rows="2"></textarea>
                    <button class="btn-primary btn-sm mt-1" onclick="handleComentario(${publicacion.id})">Comentar</button>
                </div>
                <div id="comentarios-list-${publicacion.id}"></div>
            </div>
        </div>
    `;
}

// Formatear fecha
function formatFecha(fecha) {
    const date = new Date(fecha);
    const now = new Date();
    const diff = now - date;
    const seconds = Math.floor(diff / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);
    
    if (days > 7) {
        return date.toLocaleDateString('es-ES', { day: 'numeric', month: 'long' });
    } else if (days > 0) {
        return `Hace ${days} día${days > 1 ? 's' : ''}`;
    } else if (hours > 0) {
        return `Hace ${hours} hora${hours > 1 ? 's' : ''}`;
    } else if (minutes > 0) {
        return `Hace ${minutes} minuto${minutes > 1 ? 's' : ''}`;
    } else {
        return 'Hace un momento';
    }
}

// Handlers de eventos
async function handleLike(publicacionId, isLiked) {
    try {
        if (isLiked) {
            await unlikePublicacion(publicacionId);
        } else {
            await likePublicacion(publicacionId);
        }
        // Recargar publicaciones
        loadFeed();
    } catch (error) {
        showAlert('Error al dar like', 'error');
    }
}

async function handleDeletePublicacion(publicacionId) {
    if (confirm('¿Estás seguro de eliminar esta publicación?')) {
        try {
            await deletePublicacion(publicacionId);
            showAlert('Publicación eliminada', 'success');
            loadFeed();
        } catch (error) {
            showAlert('Error al eliminar', 'error');
        }
    }
}

async function toggleComentarios(publicacionId) {
    const comentariosDiv = document.getElementById(`comentarios-${publicacionId}`);
    comentariosDiv.classList.toggle('hidden');
    
    if (!comentariosDiv.classList.contains('hidden')) {
        // Cargar comentarios
        try {
            const comentarios = await getComentarios(publicacionId);
            renderComentarios(publicacionId, comentarios);
        } catch (error) {
            showAlert('Error al cargar comentarios', 'error');
        }
    }
}

async function handleComentario(publicacionId) {
    const input = document.getElementById(`comentario-input-${publicacionId}`);
    const contenido = input.value.trim();
    
    if (!contenido) return;
    
    try {
        await createComentario(publicacionId, contenido);
        input.value = '';
        // Recargar comentarios
        const comentarios = await getComentarios(publicacionId);
        renderComentarios(publicacionId, comentarios);
        showAlert('Comentario añadido', 'success');
    } catch (error) {
        showAlert('Error al comentar', 'error');
    }
}

function renderComentarios(publicacionId, comentarios) {
    const listDiv = document.getElementById(`comentarios-list-${publicacionId}`);
    
    if (comentarios.length === 0) {
        listDiv.innerHTML = '<p class="text-center">No hay comentarios aún</p>';
        return;
    }
    
    listDiv.innerHTML = comentarios.map(c => `
        <div class="comentario">
            <div class="comentario-header">
                <span class="comentario-username">${c.usuarioUsername}</span>
                <span class="comentario-fecha">${formatFecha(c.fechaComentario)}</span>
            </div>
            <div class="comentario-contenido">${c.contenido}</div>
        </div>
    `).join('');
}