/**
 * payment.js — Modal de pago con tarjeta estilo Stripe/PayPal.
 *
 * Expone:
 *   abrirModalPago({ reservaId, total, concepto, onSuccess })
 *
 * Flujo (modo demo del TFG):
 *   1) Pide datos de tarjeta (número, caducidad, CVC, titular).
 *   2) Valida con Luhn, formato MM/AA y CVC 3–4 dígitos.
 *   3) Muestra estado "Procesando" durante ~2s para simular la pasarela.
 *   4) Llama al backend /api/payments/verify/{reservaId} (marca PAID en modo demo).
 *   5) Muestra un tick de confirmación y llama onSuccess().
 *
 * No se transmite ninguna tarjeta real al servidor: los datos se quedan en el
 * cliente y sólo se confirma la reserva. Esto es apto para un demo de TFG.
 */

(function () {
  // Hoja de estilos específica del modal (complementa style.css)
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
    .pay-secure{
      display:flex;gap:.45rem;align-items:center;color:var(--t2);font-size:.72rem;
      padding:.65rem;background:var(--alt);border-radius:10px;margin-top:.9rem;
    }
    .pay-secure svg{flex-shrink:0}
    .pay-methods{
      display:flex;gap:.35rem;margin-bottom:1rem;
    }
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
    .pay-err{
      color:var(--coral);font-size:.78rem;margin-top:-.5rem;margin-bottom:.5rem;
      display:none;
    }
    .pay-err.show{display:block}
    .pay-processing{
      display:flex;flex-direction:column;align-items:center;padding:2rem 1rem;
    }
    .pay-spinner{
      width:46px;height:46px;border-radius:50%;
      border:4px solid var(--alt);border-top-color:var(--seafoam);
      animation:pay-spin .8s linear infinite;margin-bottom:1rem;
    }
    @keyframes pay-spin { to { transform:rotate(360deg); } }
  `;

  let _modalEl = null;
  let _currentResolve = null;
  let _paypalCfg = null;            // { enabled, clientId, mode, currency }
  let _paypalSdkLoaded = false;     // true una vez inyectado el <script>
  let _paypalRendered = false;      // true si ya se llamó a paypal.Buttons().render

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
          <h3>Pago seguro</h3>
          <button onclick="cerrarModalPago()" aria-label="Cerrar">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M18 6 6 18M6 6l12 12"/></svg>
          </button>
        </div>
        <div class="dv-modal-body" id="payBody">
          <!-- Se pinta en abrirModalPago -->
        </div>
      </div>
    `;
    document.body.appendChild(wrap);
    _modalEl = wrap;
    wrap.addEventListener('click', e => { if (e.target === wrap) cerrarModalPago(); });
    return wrap;
  }

  function renderForm({ total, concepto }) {
    const totalFmt = Number(total || 0).toFixed(2);
    return `
      <div class="pay-summary">
        <div style="font-size:.75rem;color:var(--t2);text-transform:uppercase;letter-spacing:.05em;margin-bottom:.35rem">
          ${escapeHtml(concepto || 'Pago')}
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
        ${_paypalCfg && _paypalCfg.enabled
          ? `<button class="pay-method-pill" data-method="paypal" type="button">
               <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M7.1 21H3.3c-.2 0-.4-.2-.3-.4L5.6 4.3c.2-1 1-1.7 2-1.7h6.7c3.2 0 5.4 1.6 4.6 5.2-.8 3.6-3.4 5.2-6.6 5.2H9.8c-.3 0-.6.2-.6.5L7.9 20.4c0 .3-.3.6-.7.6H7.1z"/></svg>
               PayPal
             </button>`
          : `<button class="pay-method-pill" title="Configura PAYPAL_CLIENT_ID para habilitar PayPal real" disabled style="opacity:.55;cursor:not-allowed">
               <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M7.1 21H3.3c-.2 0-.4-.2-.3-.4L5.6 4.3c.2-1 1-1.7 2-1.7h6.7c3.2 0 5.4 1.6 4.6 5.2-.8 3.6-3.4 5.2-6.6 5.2H9.8c-.3 0-.6.2-.6.5L7.9 20.4c0 .3-.3.6-.7.6H7.1z"/></svg>
               PayPal
             </button>`}
      </div>

      <!-- Bloque tarjeta -->
      <div id="payCardBlock">
        <div class="pay-field">
          <label class="dv-label">Titular de la tarjeta</label>
          <input id="payName" class="dv-input" placeholder="Nombre y apellidos" autocomplete="cc-name" value="">
        </div>

        <div class="pay-field">
          <label class="dv-label">Número de tarjeta</label>
          <input id="payCardNumber" class="dv-input" placeholder="1234 5678 9012 3456" inputmode="numeric" autocomplete="cc-number" maxlength="23">
          <span class="pay-field-brand" id="payBrandIcon">💳</span>
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

      <!-- Bloque PayPal -->
      <div id="payPaypalBlock" style="display:none">
        <div id="paypalButtonContainer" style="min-height:70px"></div>
        <div id="paypalStatus" style="font-size:.78rem;color:var(--t2);margin-top:.5rem;text-align:center"></div>
      </div>

      <div class="pay-secure">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
          ${_paypalCfg && _paypalCfg.enabled
            ? `Pagos reales con <strong>PayPal ${escapeHtml(_paypalCfg.mode || 'sandbox')}</strong>. Alternativamente usa la tarjeta de prueba
               <strong>4242&nbsp;4242&nbsp;4242&nbsp;4242</strong>, fecha futura y cualquier CVC.`
            : `Conexión segura simulada para este TFG. Puedes usar la tarjeta de prueba
               <strong>4242&nbsp;4242&nbsp;4242&nbsp;4242</strong>, cualquier fecha futura y cualquier CVC.`}
      </div>
    `;
  }

  function renderProcessing() {
    return `
      <div class="pay-processing">
        <div class="pay-spinner"></div>
        <h4 style="margin:0 0 .4rem;color:var(--navy)">Procesando pago…</h4>
        <p style="margin:0;color:var(--t2);font-size:.85rem">Estamos contactando con tu banco.</p>
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

  function renderError(msg) {
    return `
      <div style="text-align:center;padding:1.5rem 1rem">
        <div style="font-size:2rem">❌</div>
        <h4 style="margin:.5rem 0;color:var(--coral);font-family:'DM Serif Display',serif">Pago rechazado</h4>
        <p style="margin:0;color:var(--t2);font-size:.88rem">${escapeHtml(msg || 'Inténtalo de nuevo.')}</p>
        <button class="dv-btn-pay" style="margin-top:1.25rem" onclick="cerrarModalPago()">Cerrar</button>
      </div>
    `;
  }

  // ── Algoritmo de Luhn para validar número de tarjeta
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

  function detectBrand(num) {
    const n = (num || '').replace(/\D/g, '');
    if (/^4/.test(n)) return { name: 'Visa', glyph: '💳' };
    if (/^(5[1-5]|2[2-7])/.test(n)) return { name: 'Mastercard', glyph: '💳' };
    if (/^3[47]/.test(n)) return { name: 'Amex', glyph: '💳' };
    if (/^6(?:011|5)/.test(n)) return { name: 'Discover', glyph: '💳' };
    return { name: '', glyph: '💳' };
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

  function wireForm(total) {
    const numEl = document.getElementById('payCardNumber');
    const expEl = document.getElementById('payExpiry');
    const cvcEl = document.getElementById('payCvc');
    const brandEl = document.getElementById('payBrandIcon');
    const btn = document.getElementById('payConfirmBtn');

    numEl.addEventListener('input', () => {
      numEl.value = formatCardNumber(numEl.value);
      const brand = detectBrand(numEl.value);
      brandEl.textContent = brand.glyph;
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

    btn.onclick = () => procesarPago(total);

    // Wire cambio de método (pestañas Tarjeta / PayPal)
    document.querySelectorAll('.pay-method-pill').forEach(pill => {
      if (pill.disabled) return;
      pill.addEventListener('click', () => cambiarMetodo(pill.dataset.method, total));
    });
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

  // ── PayPal SDK (dinámico) ─────────────────────────────────────
  function cargarSdkPayPal(clientId, currency) {
    if (_paypalSdkLoaded && window.paypal) return Promise.resolve(window.paypal);
    return new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = `https://www.paypal.com/sdk/js?client-id=${encodeURIComponent(clientId)}&currency=${encodeURIComponent(currency || 'EUR')}&intent=capture`;
      script.async = true;
      script.onload = () => { _paypalSdkLoaded = true; resolve(window.paypal); };
      script.onerror = () => reject(new Error('No se pudo cargar el SDK de PayPal'));
      document.head.appendChild(script);
    });
  }

  async function renderPaypalButtons(total) {
    if (!_paypalCfg || !_paypalCfg.enabled) return;
    const container = document.getElementById('paypalButtonContainer');
    const statusEl  = document.getElementById('paypalStatus');
    if (!container) return;
    if (_paypalRendered) return; // sólo una vez por apertura del modal

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
          body.innerHTML = renderProcessing();
          try {
            const resp = await fetchAPI(`/paypal/capture-order/${_currentReserva.reservaId}?orderId=${encodeURIComponent(data.orderID)}`, {
              method: 'POST'
            });
            if (resp && resp.status && resp.status.toUpperCase() === 'COMPLETED') {
              body.innerHTML = renderSuccess(total);
              if (typeof _currentReserva.onSuccess === 'function') {
                setTimeout(() => _currentReserva.onSuccess(resp), 50);
              }
            } else {
              body.innerHTML = renderError('PayPal no completó el pago.');
            }
          } catch (err) {
            body.innerHTML = renderError(err.message || 'Error al capturar la orden');
          }
        },
        onCancel: () => { statusEl.textContent = 'Pago cancelado'; },
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

  function toggleErr(id, show) {
    const el = document.getElementById(id);
    if (el) el.classList.toggle('show', show);
  }

  async function procesarPago(total) {
    // Validación final
    const numero = document.getElementById('payCardNumber').value;
    const exp = document.getElementById('payExpiry').value;
    const cvc = document.getElementById('payCvc').value;
    const nombre = document.getElementById('payName').value.trim();

    let ok = true;
    if (!nombre) { alert('Indica el titular de la tarjeta'); ok = false; }
    if (ok && !luhn(numero)) { toggleErr('payErrNumber', true); ok = false; }
    if (ok && !validarExpiry(exp)) { toggleErr('payErrExpiry', true); ok = false; }
    if (ok && !validarCvc(cvc)) { toggleErr('payErrCvc', true); ok = false; }
    if (!ok) return;

    const body = document.getElementById('payBody');
    body.innerHTML = renderProcessing();

    // Retardo "simulado" para sensación de pasarela real
    await new Promise(r => setTimeout(r, 1800));

    try {
      // Llama al backend; en modo demo esto marca PAID + CONFIRMADA.
      // En modo real con Stripe configurado, retrieveSession validará y marcará.
      const resp = await fetchAPI(`/payments/verify/${_currentReserva.reservaId}`, { method: 'POST' });
      if (!resp || (resp.status && resp.status !== 'PAID')) {
        body.innerHTML = renderError('No se pudo confirmar el pago.');
        return;
      }
      body.innerHTML = renderSuccess(total);
      if (typeof _currentReserva.onSuccess === 'function') {
        setTimeout(() => _currentReserva.onSuccess(resp), 50);
      }
    } catch (err) {
      body.innerHTML = renderError(err.message || 'Error de red');
    }
  }

  let _currentReserva = {};

  async function fetchPaypalConfig() {
    try {
      const r = await fetch('/api/paypal/config', { headers: { 'Content-Type': 'application/json' } });
      if (!r.ok) return { enabled: false };
      return await r.json();
    } catch { return { enabled: false }; }
  }

  window.abrirModalPago = async function ({ reservaId, total, concepto, onSuccess }) {
    _currentReserva = { reservaId, total, concepto, onSuccess };
    _paypalRendered = false;
    const wrap = ensureModal();
    // Renderiza inmediatamente con lo que sepamos (posiblemente null → sólo tarjeta).
    // Luego refresca si PayPal se activa en el servidor.
    if (!_paypalCfg) _paypalCfg = await fetchPaypalConfig();
    document.getElementById('payBody').innerHTML = renderForm({ total, concepto });
    wrap.classList.add('open');
    wireForm(total);
  };

  window.cerrarModalPago = function () {
    if (_modalEl) _modalEl.classList.remove('open');
  };

  function escapeHtml(t) {
    return (t || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
  }
})();
