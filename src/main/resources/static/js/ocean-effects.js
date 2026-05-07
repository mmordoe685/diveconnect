
(function () {
  const PREFERS_REDUCED = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  function injectBubbles() {
    if (PREFERS_REDUCED) return;
    if (document.getElementById('oceanBubbles')) return;

    const container = document.createElement('div');
    container.id = 'oceanBubbles';
    container.setAttribute('aria-hidden', 'true');

    // Cantidad relativa al ancho de viewport
    const bubbleCount = Math.min(28, Math.max(14, Math.floor(window.innerWidth / 60)));

    const fragments = document.createDocumentFragment();
    for (let i = 0; i < bubbleCount; i++) {
      const b = document.createElement('span');
      b.className = 'ocean-bubble';
      const size = Math.round(6 + Math.random() * 18);   // 6-24 px
      const left = Math.random() * 100;                  // 0-100 vw
      const delay = Math.random() * 14;                  // 0-14 s offset
      const duration = 12 + Math.random() * 14;          // 12-26 s
      const drift = Math.round((Math.random() - .5) * 80); // ±40 px
      const opacity = .35 + Math.random() * .45;         // .35-.8
      b.style.width  = size + 'px';
      b.style.height = size + 'px';
      b.style.left   = left + 'vw';
      b.style.animationDelay    = delay + 's';
      b.style.animationDuration = duration + 's';
      b.style.setProperty('--bubble-drift',   drift + 'px');
      b.style.setProperty('--bubble-opacity', opacity);
      fragments.appendChild(b);
    }
    container.appendChild(fragments);
    document.body.appendChild(container);
  }

  let _revealObserver = null;
  function setupReveal() {
    if (PREFERS_REDUCED) {
      document.querySelectorAll('[data-reveal]').forEach(e => e.classList.add('in-view'));
      return;
    }
    if (!('IntersectionObserver' in window)) {
      document.querySelectorAll('[data-reveal]').forEach(e => e.classList.add('in-view'));
      return;
    }

    _revealObserver = new IntersectionObserver((entries, o) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const delay = parseInt(entry.target.dataset.delay || '0', 10);
          if (delay > 0) {
            setTimeout(() => entry.target.classList.add('in-view'), delay);
          } else {
            entry.target.classList.add('in-view');
          }
          o.unobserve(entry.target);
        }
      });
    }, { threshold: .08, rootMargin: '0px 0px -20px 0px' });

    observeAllReveal();
  }

  function observeAllReveal() {
    if (!_revealObserver) return;
    document.querySelectorAll('[data-reveal]:not(.in-view)').forEach(el => {
      _revealObserver.observe(el);
    });
  }

  function autoTagCards() {
    const selectors = [
      '.post-card', '.reserva-card', '.notif-item',
      '.inmersion-card', '.dive-card',
      '.user-card', '.empresa-card', '.centro-card',
      '.search-card', '.results-card',
      '.stat-cell', '.empty-state'
    ];
    document.querySelectorAll(selectors.join(',')).forEach((el, i) => {
      if (!el.hasAttribute('data-reveal')) {
        el.setAttribute('data-reveal', '');
        if (i < 8) el.dataset.delay = String(Math.min(i * 60, 400));
      }
    });
  }

  function setupTilt() {
    if (PREFERS_REDUCED) return;
    const targets = document.querySelectorAll('.inmersion-card, .empresa-card, .results-card, .dive-card');
    targets.forEach(card => {
      let rect, raf = null;
      const onMove = (e) => {
        rect = rect || card.getBoundingClientRect();
        const x = (e.clientX - rect.left) / rect.width  - .5;
        const y = (e.clientY - rect.top)  / rect.height - .5;
        if (raf) cancelAnimationFrame(raf);
        raf = requestAnimationFrame(() => {
          card.style.transform = `translateY(-4px) rotateX(${(-y * 4).toFixed(2)}deg) rotateY(${(x * 4).toFixed(2)}deg)`;
        });
      };
      const reset = () => {
        rect = null;
        if (raf) cancelAnimationFrame(raf);
        card.style.transform = '';
      };
      card.addEventListener('mousemove', onMove);
      card.addEventListener('mouseleave', reset);
      card.style.transformStyle = 'preserve-3d';
      card.style.willChange = 'transform';
    });
  }

  function init() {
    // Skip auth pages (login / register) — son hero pages enteras
    if (document.body.classList.contains('auth-page-body')) return;
    if (document.querySelector('.auth-page')) return;

    injectBubbles();
    autoTagCards();
    setupReveal();
    setupTilt();

    let debounceId;
    new MutationObserver(() => {
      clearTimeout(debounceId);
      debounceId = setTimeout(() => {
        autoTagCards();
        observeAllReveal();
        setupTilt();
      }, 180);
    }).observe(document.body, { childList: true, subtree: true });

    // Salvavidas: si por algún motivo la animación no dispara, mostrar todo a los 3s.
    setTimeout(() => {
      document.querySelectorAll('[data-reveal]:not(.in-view)').forEach(el => {
        const r = el.getBoundingClientRect();
        if (r.top < window.innerHeight * 1.2) el.classList.add('in-view');
      });
    }, 3000);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
