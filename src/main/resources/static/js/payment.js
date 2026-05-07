
(function () {
  const STYLES = `
    .pay-row{display:grid;grid-template-columns:1fr 1fr;gap:.75rem}
    .pay-row.triple{grid-template-columns:2fr 1fr 1fr}
    .pay-field{position:relative;margin-bottom:.85rem}
    .pay-field-brand{position:absolute;right:12px;top:38px;font-size:1.4rem;pointer-events:none}
    .pay-summary{
      background:linear-gradient(135deg,#FFFBF5,#E0F7F4);
      border:1px solid var(--line);border-radius:14px;padding:1rem;margin-bottom:1.25rem;
    }
    .pay-summary-row{display:flex;justify-content:space-between;align-items:center}
    .pay-summary-label{color:var(--t2);font-size:.85rem}
    .pay-summary-total{font-family:'DM Serif Display',serif;font-size:1.6rem;color:var(--navy)}
    .pay-mode-badge{
      display:inline-flex;align-items:center;gap:.3rem;
      font-size:.65rem;font-weight:700;letter-spacing:.06em;text-transform:uppercase;
      padding:.18rem .55rem;border-radius:999px;
      background:rgba(0,212,170,.12);color:#0c8a73;border:1px solid rgba(0,212,170,.22);
      margin-left:.4rem;vertical-align:middle;
    }
    .pay-mode-badge.demo{background:rgba(255,209,102,.18);color:#9a7300;border-color:rgba(255,209,102,.4)}
    .pay-mode-badge.live{background:rgba(34,197,94,.18);color:#15803d;border-color:rgba(34,197,94,.4)}
    .pay-secure{
      display:flex;gap:.45rem;align-items:flex-start;color:var(--t2);font-size:.72rem;
      padding:.65rem;background:var(--alt);border-radius:10px;margin-top:.9rem;line-height:1.45;
    }
    .pay-secure svg{flex-shrink:0;margin-top:1px}
    .pay-methods{display:flex;gap:.35rem;margin-bottom:1rem}
    .pay-method-pill{
      flex:1;padding:.5rem .6rem;border:1.5px solid var(--line);border-radius:10px;
      background:var(--bg-raise);font-weight:700;font-size:.78rem;cursor:pointer;
      display:flex;align-items:center;justify-content:center;gap:.35rem;
      transition:all .15s var(--ease);
    }
    .pay-method-pill.active{
      border-color:var(--seafoam);color:var(--seafoam);
      box-shadow:inset 0 0 0 1.5px var(--seafoam);
    }
    .pay-method-pill[disabled]{opacity:.55;cursor:not-allowed}
    .pay-err{
      color:var(--coral);font-size:.78rem;margin-top:-.5rem;margin-bottom:.5rem;display:none;
    }
    .pay-err.show{display:block}
    .pay-processing{display:flex;flex-direction:column;align-items:center;padding:2rem 1rem}
    .pay-spinner{
      width:46px;height:46px;border-radius:50%;
      border:4px solid var(--alt);border-top-color:var(--seafoam);
      animation:pay-spin .8s linear infinite;margin-bottom:1rem;
    }
    @keyframes pay-spin { to { transform:rotate(360deg); } }
    .pay-stripe-btn{
      display:flex;align-items:center;justify-content:center;gap:.4rem;
      padding:.75rem;background:#635BFF;color:#fff;border:none;border-radius:10px;
      font-weight:700;font-size:.92rem;cursor:pointer;width:100%;
      transition:filter .15s var(--ease);
    }
    .pay-stripe-btn:hover:not(:disabled){filter:brightness(1.1)}
    .pay-stripe-btn:disabled{opacity:.7;cursor:not-allowed}
    .pay-stripe-info{
      font-size:.75rem;color:var(--t2);text-align:center;margin-top:.7rem;
    }
  `;

  let _modalEl          = null;
  let _paypalCfg        = null;   // { enabled, clientId, mode, currency }
  let _stripeCfg        = null;   // { enabled, publishableKey }
  let _paypalSdkLoaded  = false;
  let _paypalRendered   = false;
  let _currentReserva   = {};

  async function fetchPaypalConfig() {
    try {
      const r = await fetch('/api/paypal/config', { headers: { 'Content-Type': 'application/json' } });
      if (!r.ok) return { enabled: false };
      return await r.json();
    } catch { return { enabled: false }; }
  }

  async function fetchStripeConfig() {
    try {
      const r = await fetch('/api/payments/config', { headers: { 'Content-Type': 'application/json' } });
      if (!r.ok) return { enabled: false };
      return await r.json();
    } catch { return { enabled: false }; }
  }

  function ensureStyles() {
    if (document.getElementById('payStyles')) return;
    const s = document.createElement('style');
    s.id = 'payStyles';
    s.textContent = STYLES;
    document.head.appendChild(s);
  }

  function ensureModal() {
    if (_modalEl) return _modalEl;
    ensureStyles();
    const wrap = document.createElement('div');
    wrap.className = 'dv-modal-backdrop';
    wrap.id = 'payModal';
    wrap.innerHTML = `
      <div class="dv-modal" style="max-width:440px">
        <div class="dv-modal-head">
          <h3 id="payHeadTitle">Pago seguro</h3>
          <button onclick="cerrarModalPago()" aria-label="Cerrar">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M18 6 6 18M6 6l12 12"/></svg>
          </button>
        </div>
        <div class="dv-modal-body" id="payBody"></div>
      </div>
    `;
    document.body.appendChild(wrap);
    _modalEl = wrap;
    wrap.addEventListener('click', e => { if (e.target === wrap) cerrarModalPago(); });
    return wrap;
  }

  function modeBadge() {
    const stripe = _stripeCfg && _stripeCfg.enabled;
    const paypal = _paypalCfg && _paypalCfg.enabled;
    if (!stripe && !paypal) {
      return '<span class="pay-mode-badge demo" title="Pasarela en modo TFG. No se cobra dinero real.">TFG · Demo</span>';
    }
    // Si hay Stripe en sandbox, marcamos sandbox; si hay PayPal en sandbox, también
    const ppMode = paypal ? (_paypalCfg.mode || 'sandbox').toLowerCase() : null;
    const isLive = ppMode === 'live';
    return isLive
      ? '<span class="pay-mode-badge live" title="Pasarela en modo producción.">Live</span>'
      : '<span class="pay-mode-badge" title="Pasarela en modo sandbox (sin cargo real).">Sandbox</span>';
  }

  function renderForm({ total, concepto }) {
    const totalFmt   = Number(total || 0).toFixed(2);
    const stripeOn   = !!(_stripeCfg && _stripeCfg.enabled);
    const paypalOn   = !!(_paypalCfg && _paypalCfg.enabled);
    const paypalMode = paypalOn ? escapeHtml(_paypalCfg.mode || 'sandbox') : '';

    // Bloque "Tarjeta": redirección a Stripe (real) o formulario local (demo)
    const cardBlock = stripeOn ? `
      <div id="payCardBlock">
        <button class="pay-stripe-btn" id="payStripeBtn" type="button">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"><rect x="3" y="10" width="18" height="11" rx="2"/><path d="M7 10V7a5 5 0 0 1 10 0v3"/></svg>
          Pagar ${totalFmt} € con Stripe
        </button>
        <div class="pay-stripe-info">
          Te llevaremos al checkout oficial de Stripe para introducir tu tarjeta de forma segura.
        </div>
      </div>
    ` : `
      <div id="payCardBlock">
        <div class="pay-field">
          <label class="dv-label">Titular de la tarjeta</label>
          <input id="payName" class="dv-input" placeholder="Nombre y apellidos" autocomplete="cc-name">
        </div>
        <div class="pay-field">
          <label class="dv-label">Número de tarjeta</label>
          <input id="payCardNumber" class="dv-input" placeholder="1234 5678 9012 3456" inputmode="numeric" autocomplete="cc-number" maxlength="23">
          <span class="pay-field-brand" id="payBrandIcon" aria-hidden="true">
            <svg viewBox="0 0 24 24" width="22" height="14" fill="none" stroke="#62B6CB" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="6" width="20" height="14" rx="2.5"/><path d="M2 11h20"/></svg>
          </span>
        </div>
        <div class="pay-err" id="payErrNumber">Número de tarjeta no válido</div>
        <div class="pay-row">
          <div class="pay-field">
            <label class="dv-label">Caducidad (MM/AA)</label>
            <input id="payExpiry" class="dv-input" placeholder="MM/AA" inputmode="numeric" autocomplete="cc-exp" maxlength="5">
            <div class="pay-err" id="payErrExpiry">Fecha no válida</div>
          </div>
          <div class="pay-field">
            <label class="dv-label">CVC</label>
            <input id="payCvc" class="dv-input" placeholder="123" inputmode="numeric" autocomplete="cc-csc" maxlength="4">
            <div class="pay-err" id="payErrCvc">3–4 dígitos</div>
          </div>
        </div>
        <button class="dv-btn-pay" id="payConfirmBtn" type="button">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" style="vertical-align:-3px;margin-right:.35rem"><rect x="3" y="10" width="18" height="11" rx="2"/><path d="M7 10V7a5 5 0 0 1 10 0v3"/></svg>
          Pagar ${totalFmt} €
        </button>
      </div>
    `;

    // Pestaña PayPal
    const paypalPill = paypalOn
      ? `<button class="pay-method-pill" data-method="paypal" type="button">
           <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M7.1 21H3.3c-.2 0-.4-.2-.3-.4L5.6 4.3c.2-1 1-1.7 2-1.7h6.7c3.2 0 5.4 1.6 4.6 5.2-.8 3.6-3.4 5.2-6.6 5.2H9.8c-.3 0-.6.2-.6.5L7.9 20.4c0 .3-.3.6-.7.6H7.1z"/></svg>
           PayPal
         </button>`
      : `<button class="pay-method-pill" type="button" disabled
                title="PayPal no está disponible en esta instancia. Usa la opción Tarjeta.">
           <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M7.1 21H3.3c-.2 0-.4-.2-.3-.4L5.6 4.3c.2-1 1-1.7 2-1.7h6.7c3.2 0 5.4 1.6 4.6 5.2-.8 3.6-3.4 5.2-6.6 5.2H9.8c-.3 0-.6.2-.6.5L7.9 20.4c0 .3-.3.6-.7.6H7.1z"/></svg>
           PayPal
         </button>`;

    // Mensaje informativo según escenario
    let secureText;
    if (stripeOn && paypalOn) {
      secureText = `Pasarelas reales: <strong>Stripe</strong> y <strong>PayPal ${paypalMode}</strong>. Tu tarjeta nunca pasa por nuestros servidores.`;
    } else if (stripeOn) {
      secureText = `Pasarela real con <strong>Stripe Checkout</strong>. Tu tarjeta se introduce en el dominio cifrado de Stripe.`;
    } else if (paypalOn) {
      secureText = `Pasarela PayPal real (<strong>${paypalMode}</strong>). Para tarjeta usa la pestaña local: tarjeta de prueba <strong>4242&nbsp;4242&nbsp;4242&nbsp;4242</strong>, fecha futura y cualquier CVC.`;
    } else {
      secureText = `Pasarela en <strong>modo demo TFG</strong>. Puedes usar la tarjeta de prueba <strong>4242&nbsp;4242&nbsp;4242&nbsp;4242</strong>, cualquier fecha futura y cualquier CVC.`;
    }

    return `
      <div class="pay-summary">
        <div style="font-size:.75rem;color:var(--t2);text-transform:uppercase;letter-spacing:.05em;margin-bottom:.35rem">
          ${escapeHtml(concepto || 'Pago')} ${modeBadge()}
        </div>
        <div class="pay-summary-row">
          <span class="pay-summary-label">Total a pagar</span>
          <span class="pay-summary-total">${totalFmt} €</span>
        </div>
      </div>

      <div class="pay-methods">
        <button class="pay-method-pill active" data-method="card" type="button">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="6" width="20" height="14" rx="2"/><path d="M2 11h20"/></svg>
          Tarjeta
        </button>
        ${paypalPill}
      </div>

      ${cardBlock}

      <div id="payPaypalBlock" style="display:none">
        <div id="paypalButtonContainer" style="min-height:70px"></div>
        <div id="paypalStatus" style="font-size:.78rem;color:var(--t2);margin-top:.5rem;text-align:center"></div>
      </div>

      <div class="pay-secure">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
        <span>${secureText}</span>
      </div>
    `;
  }

  function renderProcessing(msg) {
    return `
      <div class="pay-processing">
        <div class="pay-spinner"></div>
        <h4 style="margin:0 0 .4rem;color:var(--navy)">${escapeHtml(msg || 'Procesando pago…')}</h4>
        <p style="margin:0;color:var(--t2);font-size:.85rem">Un momento, estamos confirmando con la pasarela.</p>
      </div>
    `;
  }

  function renderSuccess(total) {
    return `
      <div class="dv-pay-success">
        <div class="dv-pay-success-tick">
          <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-linecap="round" stroke-linejoin="round"><path d="m5 12 5 5L20 7"/></svg>
        </div>
        <h4 style="margin:0 0 .3rem;color:var(--navy);font-family:'DM Serif Display',serif;font-size:1.4rem">¡Pago confirmado!</h4>
        <p style="margin:0;color:var(--t2);font-size:.88rem">Se han cobrado <strong>${Number(total).toFixed(2)} €</strong>. Tu reserva está ahora en estado CONFIRMADA.</p>
        <button class="dv-btn-pay" style="margin-top:1.25rem" onclick="cerrarModalPago()">Listo</button>
      </div>
    `;
  }

  function renderError(msg, canRetry) {
    return `
      <div style="text-align:center;padding:1.5rem 1rem">
        <svg width="42" height="42" viewBox="0 0 24 24" fill="none" stroke="var(--coral)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="display:block;margin:0 auto"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
        <h4 style="margin:.5rem 0;color:var(--coral);font-family:'DM Serif Display',serif">Pago no completado</h4>
        <p style="margin:0;color:var(--t2);font-size:.88rem">${escapeHtml(msg || 'Inténtalo de nuevo en unos segundos.')}</p>
        <div style="display:flex;gap:.5rem;margin-top:1.25rem;justify-content:center">
          ${canRetry
            ? `<button class="dv-btn-pay" onclick="window.__payRetry && window.__payRetry()">Reintentar</button>`
            : ''}
          <button class="dv-btn-pay" style="background:var(--alt);color:var(--t1)" onclick="cerrarModalPago()">Cerrar</button>
        </div>
      </div>
    `;
  }

  function luhn(num) {
    const digits = (num || '').replace(/\D/g, '');
    if (digits.length < 12) return false;
    let sum = 0, alt = false;
    for (let i = digits.length - 1; i >= 0; i--) {
      let n = parseInt(digits.charAt(i), 10);
      if (alt) { n *= 2; if (n > 9) n -= 9; }
      sum += n; alt = !alt;
    }
    return sum % 10 === 0;
  }

  // El indicador visual de marca se mantiene constante (sólo decorativo).
  // No expone el detalle de la BIN al usuario y mantiene la UI limpia.
  function detectBrand() {
    return `<svg viewBox="0 0 24 24" width="22" height="14" fill="none" stroke="#62B6CB" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="6" width="20" height="14" rx="2.5"/><path d="M2 11h20"/></svg>`;
  }

  function formatCardNumber(v) {
    const digits = (v || '').replace(/\D/g, '').slice(0, 19);
    return digits.replace(/(.{4})/g, '$1 ').trim();
  }

  function formatExpiry(v) {
    const d = (v || '').replace(/\D/g, '').slice(0, 4);
    if (d.length <= 2) return d;
    return d.slice(0, 2) + '/' + d.slice(2);
  }

  function validarExpiry(v) {
    const m = /^(\d{2})\/(\d{2})$/.exec(v || '');
    if (!m) return false;
    const mm = parseInt(m[1], 10);
    const yy = parseInt(m[2], 10);
    if (mm < 1 || mm > 12) return false;
    const now = new Date();
    const y = 2000 + yy;
    const endOfMonth = new Date(y, mm, 0, 23, 59, 59);
    return endOfMonth >= now;
  }

  function validarCvc(v) { return /^\d{3,4}$/.test((v || '').trim()); }

  function toggleErr(id, show) {
    const el = document.getElementById(id);
    if (el) el.classList.toggle('show', show);
  }

  function wireForm(total) {
    // Listener de tabs
    document.querySelectorAll('.pay-method-pill').forEach(pill => {
      if (pill.disabled) return;
      pill.addEventListener('click', () => cambiarMetodo(pill.dataset.method, total));
    });

    // Botón Stripe (modo real) o formulario local (modo demo)
    const stripeBtn = document.getElementById('payStripeBtn');
    if (stripeBtn) {
      stripeBtn.addEventListener('click', () => irAStripeCheckout(stripeBtn));
      return;
    }

    // Modo demo: wiring del formulario de tarjeta
    const numEl   = document.getElementById('payCardNumber');
    const expEl   = document.getElementById('payExpiry');
    const cvcEl   = document.getElementById('payCvc');
    const brandEl = document.getElementById('payBrandIcon');
    const btn     = document.getElementById('payConfirmBtn');
    if (!numEl) return;

    numEl.addEventListener('input', () => {
      numEl.value = formatCardNumber(numEl.value);
      brandEl.innerHTML = detectBrand();
      toggleErr('payErrNumber', numEl.value.replace(/\D/g,'').length >= 12 && !luhn(numEl.value));
    });
    expEl.addEventListener('input', () => {
      expEl.value = formatExpiry(expEl.value);
      toggleErr('payErrExpiry', expEl.value.length === 5 && !validarExpiry(expEl.value));
    });
    cvcEl.addEventListener('input', () => {
      cvcEl.value = cvcEl.value.replace(/\D/g,'').slice(0,4);
      toggleErr('payErrCvc', cvcEl.value.length > 0 && !validarCvc(cvcEl.value));
    });
    btn.onclick = () => procesarPagoDemo(total);
  }

  function cambiarMetodo(metodo, total) {
    document.querySelectorAll('.pay-method-pill').forEach(p => p.classList.toggle('active', p.dataset.method === metodo));
    const cardB = document.getElementById('payCardBlock');
    const payB  = document.getElementById('payPaypalBlock');
    if (!cardB || !payB) return;
    if (metodo === 'paypal') {
      cardB.style.display = 'none';
      payB.style.display = 'block';
      renderPaypalButtons(total);
    } else {
      cardB.style.display = 'block';
      payB.style.display = 'none';
    }
  }

  async function irAStripeCheckout(btn) {
    btn.disabled = true;
    const body = document.getElementById('payBody');
    body.innerHTML = renderProcessing('Conectando con Stripe…');
    try {
      const resp = await fetchAPI(`/payments/checkout/${_currentReserva.reservaId}`, { method: 'POST' });
      if (resp && resp.url) {
        window.location.href = resp.url;
        return;
      }
      window.__payRetry = () => reabrirFormulario(_currentReserva.total, _currentReserva.concepto);
      body.innerHTML = renderError(resp && resp.error ? resp.error : 'No pudimos crear la sesión de Stripe.', true);
    } catch (err) {
      window.__payRetry = () => reabrirFormulario(_currentReserva.total, _currentReserva.concepto);
      body.innerHTML = renderError(err.message || 'Error de red contactando con Stripe.', true);
    }
  }

  async function procesarPagoDemo(total) {
    const numero = document.getElementById('payCardNumber').value;
    const exp    = document.getElementById('payExpiry').value;
    const cvc    = document.getElementById('payCvc').value;
    const nombre = document.getElementById('payName').value.trim();

    let ok = true;
    if (!nombre) { showAlert('Indica el titular de la tarjeta', 'error'); ok = false; }
    if (ok && !luhn(numero))           { toggleErr('payErrNumber', true); ok = false; }
    if (ok && !validarExpiry(exp))     { toggleErr('payErrExpiry', true); ok = false; }
    if (ok && !validarCvc(cvc))        { toggleErr('payErrCvc', true);    ok = false; }
    if (!ok) return;

    const btn = document.getElementById('payConfirmBtn');
    if (btn) btn.disabled = true;

    const body = document.getElementById('payBody');
    body.innerHTML = renderProcessing();

    // Pequeña pausa para sensación de "contactando con la pasarela"
    await new Promise(r => setTimeout(r, 900));

    try {
      const resp = await fetchAPI(`/payments/verify/${_currentReserva.reservaId}`, { method: 'POST' });
      if (!resp || (resp.status && resp.status !== 'PAID')) {
        window.__payRetry = () => reabrirFormulario(_currentReserva.total, _currentReserva.concepto);
        body.innerHTML = renderError('La pasarela rechazó el pago. Inténtalo de nuevo.', true);
        return;
      }
      body.innerHTML = renderSuccess(total);
      if (typeof _currentReserva.onSuccess === 'function') {
        setTimeout(() => _currentReserva.onSuccess(resp), 50);
      }
    } catch (err) {
      window.__payRetry = () => reabrirFormulario(_currentReserva.total, _currentReserva.concepto);
      body.innerHTML = renderError(err.message || 'Error de red', true);
    }
  }

  function reabrirFormulario(total, concepto) {
    _paypalRendered = false;
    const body = document.getElementById('payBody');
    if (!body) return;
    body.innerHTML = renderForm({ total, concepto });
    wireForm(total);
  }

  function cargarSdkPayPal(clientId, currency) {
    if (_paypalSdkLoaded && window.paypal) return Promise.resolve(window.paypal);
    return new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = `https://www.paypal.com/sdk/js?client-id=${encodeURIComponent(clientId)}&currency=${encodeURIComponent(currency || 'EUR')}&intent=capture`;
      script.async = true;
      script.onload  = () => { _paypalSdkLoaded = true; resolve(window.paypal); };
      script.onerror = () => reject(new Error('No se pudo cargar el SDK de PayPal'));
      document.head.appendChild(script);
    });
  }

  async function renderPaypalButtons(total) {
    if (!_paypalCfg || !_paypalCfg.enabled) return;
    const container = document.getElementById('paypalButtonContainer');
    const statusEl  = document.getElementById('paypalStatus');
    if (!container) return;
    if (_paypalRendered) return;

    statusEl.textContent = 'Cargando PayPal…';
    try {
      const paypal = await cargarSdkPayPal(_paypalCfg.clientId, _paypalCfg.currency || 'EUR');
      statusEl.textContent = '';
      paypal.Buttons({
        style: { layout: 'vertical', color: 'gold', shape: 'pill', label: 'pay' },
        createOrder: async () => {
          const resp = await fetchAPI(`/paypal/create-order/${_currentReserva.reservaId}`, { method: 'POST' });
          if (!resp || !resp.orderId) throw new Error('No se pudo crear la orden PayPal');
          return resp.orderId;
        },
        onApprove: async (data) => {
          const body = document.getElementById('payBody');
          body.innerHTML = renderProcessing('Capturando pago en PayPal…');
          try {
            const resp = await fetchAPI(
              `/paypal/capture-order/${_currentReserva.reservaId}?orderId=${encodeURIComponent(data.orderID)}`,
              { method: 'POST' });
            if (resp && resp.status && resp.status.toUpperCase() === 'COMPLETED') {
              body.innerHTML = renderSuccess(total);
              if (typeof _currentReserva.onSuccess === 'function') {
                setTimeout(() => _currentReserva.onSuccess(resp), 50);
              }
            } else {
              window.__payRetry = () => reabrirFormulario(_currentReserva.total, _currentReserva.concepto);
              body.innerHTML = renderError('PayPal no completó el pago.', true);
            }
          } catch (err) {
            window.__payRetry = () => reabrirFormulario(_currentReserva.total, _currentReserva.concepto);
            body.innerHTML = renderError(err.message || 'Error capturando la orden PayPal', true);
          }
        },
        onCancel: () => { statusEl.textContent = 'Pago PayPal cancelado.'; },
        onError: (err) => {
          console.error('PayPal error:', err);
          statusEl.textContent = 'Error al comunicar con PayPal. Inténtalo de nuevo.';
        }
      }).render('#paypalButtonContainer');
      _paypalRendered = true;
    } catch (e) {
      statusEl.textContent = e.message || 'No se pudo inicializar PayPal';
    }
  }

  async function autoConfirmarGratis(reservaId, onSuccess) {
    try {
      const resp = await fetchAPI(`/payments/verify/${reservaId}`, { method: 'POST' });
      if (typeof onSuccess === 'function') onSuccess(resp);
      if (typeof showAlert === 'function') {
        showAlert('Reserva gratuita confirmada.', 'success');
      }
    } catch (err) {
      if (typeof showAlert === 'function') {
        showAlert(err.message || 'No se pudo confirmar la reserva gratuita', 'error');
      }
    }
  }

  window.abrirModalPago = async function ({ reservaId, total, concepto, onSuccess }) {
    _currentReserva = { reservaId, total, concepto, onSuccess };
    _paypalRendered = false;

    // Reservas con total 0 → confirmar directamente
    if (Number(total || 0) <= 0) {
      return autoConfirmarGratis(reservaId, onSuccess);
    }

    // Cargar configuración de las pasarelas en paralelo
    if (!_paypalCfg || !_stripeCfg) {
      const [pp, st] = await Promise.all([fetchPaypalConfig(), fetchStripeConfig()]);
      _paypalCfg = pp;
      _stripeCfg = st;
    }

    const wrap = ensureModal();
    document.getElementById('payBody').innerHTML = renderForm({ total, concepto });
    wrap.classList.add('open');
    wireForm(total);
  };

  window.cerrarModalPago = function () {
    if (_modalEl) _modalEl.classList.remove('open');
    window.__payRetry = null;
  };

  function escapeHtml(t) {
    return (t || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
  }
})();
