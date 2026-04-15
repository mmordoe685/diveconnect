// ================================================================
// animations.js — Scroll reveals, counters, parallax, cursor glow
// Inspired by Linear, Stripe, Vercel motion patterns
// ================================================================

(function () {
  'use strict';

  // ── Scroll Reveal ─────────────────────────────────────────────
  // Elements with [data-reveal] fade/slide in when they enter viewport.
  // Attributes:
  //   data-reveal            → basic fade-up
  //   data-reveal="left"     → slide from left
  //   data-reveal="right"    → slide from right
  //   data-reveal="scale"    → scale up
  //   data-delay="200"       → ms delay
  //   data-stagger           → parent: children animate sequentially

  const REVEAL_DEFAULTS = {
    threshold: 0.15,
    rootMargin: '0px 0px -60px 0px',
  };

  function initReveals() {
    const els = document.querySelectorAll('[data-reveal]');
    if (!els.length) return;

    // Set initial hidden state via inline styles (avoids FOUC flash)
    els.forEach((el) => {
      const dir = el.getAttribute('data-reveal') || 'up';
      el.style.opacity = '0';
      el.style.willChange = 'transform, opacity';
      el.style.transition = 'none'; // prevent flash
      switch (dir) {
        case 'left':  el.style.transform = 'translateX(-32px)'; break;
        case 'right': el.style.transform = 'translateX(32px)'; break;
        case 'scale': el.style.transform = 'scale(0.94)'; break;
        default:      el.style.transform = 'translateY(28px)'; break;
      }
    });

    // Force reflow then enable transitions
    document.body.offsetHeight; // eslint-disable-line no-unused-expressions
    els.forEach((el) => {
      const delay = parseInt(el.getAttribute('data-delay') || '0', 10);
      el.style.transition =
        `opacity 0.7s cubic-bezier(0.16,1,0.3,1) ${delay}ms, ` +
        `transform 0.7s cubic-bezier(0.16,1,0.3,1) ${delay}ms`;
    });

    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (!entry.isIntersecting) return;
        const el = entry.target;
        el.style.opacity = '1';
        el.style.transform = 'translate(0) scale(1)';
        observer.unobserve(el);
      });
    }, REVEAL_DEFAULTS);

    els.forEach((el) => observer.observe(el));
  }

  // ── Stagger children ──────────────────────────────────────────
  function initStagger() {
    document.querySelectorAll('[data-stagger]').forEach((parent) => {
      const gap = parseInt(parent.getAttribute('data-stagger') || '80', 10);
      Array.from(parent.children).forEach((child, i) => {
        if (!child.hasAttribute('data-reveal')) {
          child.setAttribute('data-reveal', 'up');
        }
        child.setAttribute('data-delay', String(i * gap));
      });
    });
  }

  // ── Animated Counters ─────────────────────────────────────────
  // Elements with [data-count-to="2400"] will count from 0 to 2400
  function initCounters() {
    const counters = document.querySelectorAll('[data-count-to]');
    if (!counters.length) return;

    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (!entry.isIntersecting) return;
        const el = entry.target;
        observer.unobserve(el);
        animateCounter(el);
      });
    }, { threshold: 0.4 });

    counters.forEach((el) => observer.observe(el));
  }

  function animateCounter(el) {
    const raw = el.getAttribute('data-count-to');
    const suffix = el.getAttribute('data-count-suffix') || '';
    const prefix = el.getAttribute('data-count-prefix') || '';
    const target = parseFloat(raw.replace(/[^0-9.]/g, ''));
    const decimals = (raw.includes('.')) ? raw.split('.')[1].length : 0;
    const duration = 1800;
    const start = performance.now();

    function easeOutExpo(t) {
      return t === 1 ? 1 : 1 - Math.pow(2, -10 * t);
    }

    function tick(now) {
      const elapsed = now - start;
      const progress = Math.min(elapsed / duration, 1);
      const value = target * easeOutExpo(progress);
      el.textContent = prefix + value.toFixed(decimals).replace(/\B(?=(\d{3})+(?!\d))/g, '.') + suffix;
      if (progress < 1) requestAnimationFrame(tick);
    }

    requestAnimationFrame(tick);
  }

  // ── Hero Parallax ─────────────────────────────────────────────
  function initParallax() {
    const hero = document.querySelector('.landing-container');
    if (!hero) return;

    const layers = hero.querySelectorAll('[data-parallax]');
    if (!layers.length) return;

    let ticking = false;
    window.addEventListener('scroll', () => {
      if (ticking) return;
      ticking = true;
      requestAnimationFrame(() => {
        const scrollY = window.scrollY;
        const heroH = hero.offsetHeight;
        if (scrollY < heroH * 1.2) {
          layers.forEach((layer) => {
            const speed = parseFloat(layer.getAttribute('data-parallax') || '0.3');
            layer.style.transform = `translateY(${scrollY * speed}px)`;
          });
        }
        ticking = false;
      });
    }, { passive: true });
  }

  // ── Cursor glow (subtle spotlight that follows mouse) ─────────
  function initCursorGlow() {
    const glow = document.querySelector('.cursor-glow');
    if (!glow) return;

    let mx = 0, my = 0, cx = 0, cy = 0;
    document.addEventListener('mousemove', (e) => {
      mx = e.clientX;
      my = e.clientY;
    }, { passive: true });

    function lerp(a, b, t) { return a + (b - a) * t; }

    function tick() {
      cx = lerp(cx, mx, 0.08);
      cy = lerp(cy, my, 0.08);
      glow.style.transform = `translate(${cx - 300}px, ${cy - 300}px)`;
      requestAnimationFrame(tick);
    }
    requestAnimationFrame(tick);
  }

  // ── Smooth navbar background on scroll ────────────────────────
  function initNavScroll() {
    const nav = document.querySelector('.navbar');
    if (!nav) return;

    let last = 0;
    window.addEventListener('scroll', () => {
      const y = window.scrollY;
      if (y > 20 && last <= 20) {
        nav.classList.add('navbar--scrolled');
      } else if (y <= 20 && last > 20) {
        nav.classList.remove('navbar--scrolled');
      }
      last = y;
    }, { passive: true });
  }

  // ── Magnetic buttons (subtle pull toward cursor) ──────────────
  function initMagnetic() {
    document.querySelectorAll('.btn-magnetic').forEach((btn) => {
      btn.addEventListener('mousemove', (e) => {
        const rect = btn.getBoundingClientRect();
        const x = e.clientX - rect.left - rect.width / 2;
        const y = e.clientY - rect.top - rect.height / 2;
        btn.style.transform = `translate(${x * 0.15}px, ${y * 0.15}px)`;
      });
      btn.addEventListener('mouseleave', () => {
        btn.style.transform = '';
        btn.style.transition = 'transform 0.4s cubic-bezier(0.16,1,0.3,1)';
        setTimeout(() => { btn.style.transition = ''; }, 400);
      });
    });
  }

  // ── Tilt cards (3D perspective on hover) ──────────────────────
  function initTilt() {
    document.querySelectorAll('[data-tilt]').forEach((card) => {
      card.addEventListener('mousemove', (e) => {
        const rect = card.getBoundingClientRect();
        const x = (e.clientX - rect.left) / rect.width - 0.5;
        const y = (e.clientY - rect.top) / rect.height - 0.5;
        card.style.transform =
          `perspective(600px) rotateY(${x * 6}deg) rotateX(${-y * 6}deg) scale(1.01)`;
      });
      card.addEventListener('mouseleave', () => {
        card.style.transform = '';
        card.style.transition = 'transform 0.5s cubic-bezier(0.16,1,0.3,1)';
        setTimeout(() => { card.style.transition = ''; }, 500);
      });
    });
  }

  // ── Smooth scroll for anchor links ────────────────────────────
  function initSmoothScroll() {
    document.querySelectorAll('a[href^="#"]').forEach((a) => {
      a.addEventListener('click', (e) => {
        const target = document.querySelector(a.getAttribute('href'));
        if (target) {
          e.preventDefault();
          target.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
      });
    });
  }

  // ── Noise texture overlay (adds depth like Linear/Raycast) ───
  function initNoiseOverlay() {
    if (document.querySelector('.noise-overlay')) return;
    const canvas = document.createElement('canvas');
    canvas.width = 200;
    canvas.height = 200;
    const ctx = canvas.getContext('2d');
    const imageData = ctx.createImageData(200, 200);
    for (let i = 0; i < imageData.data.length; i += 4) {
      const v = Math.random() * 255;
      imageData.data[i] = v;
      imageData.data[i + 1] = v;
      imageData.data[i + 2] = v;
      imageData.data[i + 3] = 12; // very subtle
    }
    ctx.putImageData(imageData, 0, 0);

    const overlay = document.createElement('div');
    overlay.className = 'noise-overlay';
    overlay.style.cssText =
      'position:fixed;inset:0;pointer-events:none;z-index:9999;' +
      'background-image:url(' + canvas.toDataURL() + ');' +
      'background-repeat:repeat;opacity:0.4;mix-blend-mode:overlay;';
    document.body.appendChild(overlay);
  }

  // ── Boot ──────────────────────────────────────────────────────
  function init() {
    initStagger();
    initReveals();
    initCounters();
    initParallax();
    initCursorGlow();
    initNavScroll();
    initMagnetic();
    initTilt();
    initSmoothScroll();
    initNoiseOverlay();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
