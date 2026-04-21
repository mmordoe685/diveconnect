// ============================================================
// publicaciones.js — Módulo de publicaciones y comentarios
// ============================================================

async function getFeed(page = 0, size = 10) {
    return await fetchAPI(`/publicaciones/feed?page=${page}&size=${size}`);
}

async function createPublicacion(data) {
    return await fetchAPI('/publicaciones', { method: 'POST', body: JSON.stringify(data) });
}

async function likePublicacion(id) {
    return await fetchAPI(`/publicaciones/${id}/like`, { method: 'POST' });
}

async function unlikePublicacion(id) {
    return await fetchAPI(`/publicaciones/${id}/like`, { method: 'DELETE' });
}

async function deletePublicacion(id) {
    return await fetchAPI(`/publicaciones/${id}`, { method: 'DELETE' });
}

async function getComentarios(publicacionId) {
    return await fetchAPI(`/publicaciones/${publicacionId}/comentarios`);
}

async function createComentario(publicacionId, contenido) {
    return await fetchAPI(`/publicaciones/${publicacionId}/comentarios`, {
        method: 'POST',
        body: JSON.stringify({ contenido })
    });
}

// ── Renderizado ───────────────────────────────────────────

function renderPublicacion(p) {
    const me     = getCurrentUser();
    const esMia  = me && me.id === p.usuarioId;
    const inicial = (p.usuarioUsername || '?').charAt(0).toUpperCase();

    const detallesHtml = p.lugarInmersion ? `
        <div class="publicacion-detalles">
            <div class="detalle-item"><strong>${escapeHtml(p.lugarInmersion)}</strong></div>
            ${p.profundidadMaxima ? `<div class="detalle-item">Profundidad: ${p.profundidadMaxima}m</div>` : ''}
            ${p.temperaturaAgua   ? `<div class="detalle-item">Temperatura: ${p.temperaturaAgua}°C</div>` : ''}
            ${p.visibilidad       ? `<div class="detalle-item">Visibilidad: ${p.visibilidad}m</div>` : ''}
            ${p.especiesVistas    ? `<div class="detalle-item">Especies: ${escapeHtml(p.especiesVistas)}</div>` : ''}
        </div>` : '';

    return `
    <div class="publicacion" id="pub-${p.id}">
        <div class="publicacion-header">
            <div class="publicacion-avatar">${inicial}</div>
            <div class="publicacion-user">
                <div class="publicacion-username">${escapeHtml(p.usuarioUsername)}</div>
                <div class="publicacion-fecha">${formatFecha(p.fechaPublicacion)}</div>
            </div>
            ${esMia ? `<button class="btn btn-danger btn-sm" onclick="handleDeletePublicacion(${p.id})">Eliminar</button>` : ''}
        </div>
        <div class="publicacion-contenido">${escapeHtml(p.contenido)}</div>
        ${p.imagenUrl ? `<img src="${escapeHtml(p.imagenUrl)}" alt="imagen" class="publicacion-imagen">` : ''}
        ${detallesHtml}
        <div class="publicacion-actions">
            <button class="action-btn ${p.likedByCurrentUser ? 'liked' : ''}"
                    id="btn-like-${p.id}"
                    onclick="handleLike(${p.id}, ${p.likedByCurrentUser})">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
                <span id="likes-count-${p.id}">${p.numeroLikes}</span>
            </button>
            <button class="action-btn" onclick="toggleComentarios(${p.id})">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
                <span id="coms-count-${p.id}">${p.numeroComentarios}</span>
            </button>
        </div>
        <div class="comentarios-section hidden" id="comentarios-${p.id}">
            <div class="comentario-form">
                <textarea class="form-input" id="comentario-input-${p.id}"
                    placeholder="Escribe un comentario..." rows="2"></textarea>
                <button class="btn btn-primary btn-sm mt-1"
                    onclick="handleComentario(${p.id})">Comentar</button>
            </div>
            <div id="comentarios-list-${p.id}"></div>
        </div>
    </div>`;
}

function renderComentarios(publicacionId, lista) {
    const listDiv = document.getElementById(`comentarios-list-${publicacionId}`);
    if (!listDiv) return;
    if (!lista || lista.length === 0) {
        listDiv.innerHTML = '<p style="color:#9e9e9e;font-size:0.85rem;padding:0.5rem">Sin comentarios aún.</p>';
        return;
    }
    listDiv.innerHTML = lista.map(c => `
        <div class="comentario">
            <div class="comentario-header">
                <span class="comentario-username">${escapeHtml(c.usuarioUsername)}</span>
                <span class="comentario-fecha">${formatFecha(c.fechaComentario)}</span>
            </div>
            <div class="comentario-contenido">${escapeHtml(c.contenido)}</div>
        </div>`
    ).join('');
}

// ── Handlers (sin dependencias de páginas concretas) ──────

async function handleLike(publicacionId, yaLiked) {
    try {
        yaLiked ? await unlikePublicacion(publicacionId) : await likePublicacion(publicacionId);

        const btn   = document.getElementById(`btn-like-${publicacionId}`);
        const count = document.getElementById(`likes-count-${publicacionId}`);
        if (btn && count) {
            const actual = parseInt(count.textContent) || 0;
            count.textContent = yaLiked ? Math.max(0, actual - 1) : actual + 1;
            btn.classList.toggle('liked', !yaLiked);
            btn.setAttribute('onclick', `handleLike(${publicacionId}, ${!yaLiked})`);
        }
    } catch (err) {
        showAlert(err.message || 'Error al dar like', 'error');
    }
}

async function handleDeletePublicacion(publicacionId) {
    if (!confirm('¿Eliminar esta publicación?')) return;
    try {
        await deletePublicacion(publicacionId);
        document.getElementById(`pub-${publicacionId}`)?.remove();
        showAlert('Publicación eliminada', 'success');
    } catch (err) {
        showAlert(err.message || 'Error al eliminar', 'error');
    }
}

async function toggleComentarios(publicacionId) {
    const section = document.getElementById(`comentarios-${publicacionId}`);
    if (!section) return;
    // Soporta dos estilos: 'hidden' (páginas legacy) y 'open' (feed Instagram)
    const usaOpen = section.classList.contains('ig-comment-area');
    if (usaOpen) {
        section.classList.toggle('open');
        if (section.classList.contains('open')) {
            try { renderComentarios(publicacionId, await getComentarios(publicacionId)); }
            catch { showAlert('Error al cargar comentarios', 'error'); }
        }
    } else {
        section.classList.toggle('hidden');
        if (!section.classList.contains('hidden')) {
            try { renderComentarios(publicacionId, await getComentarios(publicacionId)); }
            catch { showAlert('Error al cargar comentarios', 'error'); }
        }
    }
}

async function handleComentario(publicacionId) {
    const input = document.getElementById(`comentario-input-${publicacionId}`);
    const contenido = input?.value.trim();
    if (!contenido) return;
    try {
        await createComentario(publicacionId, contenido);
        input.value = '';
        renderComentarios(publicacionId, await getComentarios(publicacionId));
        const span = document.getElementById(`coms-count-${publicacionId}`);
        if (span) span.textContent = parseInt(span.textContent || '0') + 1;
        showAlert('Comentario añadido', 'success');
    } catch (err) {
        showAlert(err.message || 'Error al comentar', 'error');
    }
}