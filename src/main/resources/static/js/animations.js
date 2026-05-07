
(function () {
  'use strict';

  // -- Scroll Reveal --
  var REVEAL_DEFAULTS = {
    threshold: 0.12,
    rootMargin: '0px 0px -80px 0px',
  };

  function initReveals() {
    var els = document.querySelectorAll('[data-reveal]');
    if (!els.length) return;

    els.forEach(function(el) {
      var dir = el.getAttribute('data-reveal') || 'up';
      el.style.opacity = '0';
      el.style.willChange = 'transform, opacity';
      el.style.transition = 'none';
      switch (dir) {
        case 'left':  el.style.transform = 'translateX(-40px)'; break;
        case 'right': el.style.transform = 'translateX(40px)'; break;
        case 'scale': el.style.transform = 'scale(0.92)'; break;
        default:      el.style.transform = 'translateY(32px)'; break;
      }
    });

    document.body.offsetHeight;
    els.forEach(function(el) {
      var delay = parseInt(el.getAttribute('data-delay') || '0', 10);
      el.style.transition =
        'opacity 0.85s cubic-bezier(0.16,1,0.3,1) ' + delay + 'ms, ' +
        'transform 0.85s cubic-bezier(0.16,1,0.3,1) ' + delay + 'ms';
    });

    var observer = new IntersectionObserver(function(entries) {
      entries.forEach(function(entry) {
        if (!entry.isIntersecting) return;
        var el = entry.target;
        el.style.opacity = '1';
        el.style.transform = 'translate(0) scale(1)';
        observer.unobserve(el);
      });
    }, REVEAL_DEFAULTS);

    els.forEach(function(el) { observer.observe(el); });
  }

  // -- Stagger children --
  function initStagger() {
    document.querySelectorAll('[data-stagger]').forEach(function(parent) {
      var gap = parseInt(parent.getAttribute('data-stagger') || '80', 10);
      Array.from(parent.children).forEach(function(child, i) {
        if (!child.hasAttribute('data-reveal')) {
          child.setAttribute('data-reveal', 'up');
        }
        child.setAttribute('data-delay', String(i * gap));
      });
    });
  }

  // -- Animated Counters --
  function initCounters() {
    var counters = document.querySelectorAll('[data-count-to]');
    if (!counters.length) return;

    var observer = new IntersectionObserver(function(entries) {
      entries.forEach(function(entry) {
        if (!entry.isIntersecting) return;
        observer.unobserve(entry.target);
        animateCounter(entry.target);
      });
    }, { threshold: 0.4 });

    counters.forEach(function(el) { observer.observe(el); });
  }

  function animateCounter(el) {
    var raw = el.getAttribute('data-count-to');
    var suffix = el.getAttribute('data-count-suffix') || '';
    var prefix = el.getAttribute('data-count-prefix') || '';
    var target = parseFloat(raw.replace(/[^0-9.]/g, ''));
    var decimals = (raw.indexOf('.') !== -1) ? raw.split('.')[1].length : 0;
    var duration = 2200;
    var start = performance.now();

    function easeOutExpo(t) {
      return t === 1 ? 1 : 1 - Math.pow(2, -10 * t);
    }

    function tick(now) {
      var elapsed = now - start;
      var progress = Math.min(elapsed / duration, 1);
      var value = target * easeOutExpo(progress);
      el.textContent = prefix + value.toFixed(decimals).replace(/\B(?=(\d{3})+(?!\d))/g, '.') + suffix;
      if (progress < 1) requestAnimationFrame(tick);
    }

    requestAnimationFrame(tick);
  }

  // -- Hero Parallax --
  function initParallax() {
    var hero = document.querySelector('.landing-container');
    if (!hero) return;

    var layers = hero.querySelectorAll('[data-parallax]');
    if (!layers.length) return;

    var ticking = false;
    window.addEventListener('scroll', function() {
      if (ticking) return;
      ticking = true;
      requestAnimationFrame(function() {
        var scrollY = window.scrollY;
        var heroH = hero.offsetHeight;
        if (scrollY < heroH * 1.2) {
          layers.forEach(function(layer) {
            var speed = parseFloat(layer.getAttribute('data-parallax') || '0.3');
            layer.style.transform = 'translateY(' + (scrollY * speed) + 'px)';
          });
        }
        ticking = false;
      });
    }, { passive: true });
  }

  // -- Smooth navbar background on scroll --
  function initNavScroll() {
    var nav = document.querySelector('.navbar');
    if (!nav) return;

    var last = 0;
    window.addEventListener('scroll', function() {
      var y = window.scrollY;
      if (y > 20 && last <= 20) {
        nav.classList.add('navbar--scrolled');
      } else if (y <= 20 && last > 20) {
        nav.classList.remove('navbar--scrolled');
      }
      last = y;
    }, { passive: true });
  }

  // -- Magnetic buttons (subtle pull toward cursor) --
  function initMagnetic() {
    document.querySelectorAll('.btn-magnetic').forEach(function(btn) {
      btn.addEventListener('mousemove', function(e) {
        var rect = btn.getBoundingClientRect();
        var x = e.clientX - rect.left - rect.width / 2;
        var y = e.clientY - rect.top - rect.height / 2;
        btn.style.transform = 'translate(' + (x * 0.15) + 'px, ' + (y * 0.15) + 'px)';
      });
      btn.addEventListener('mouseleave', function() {
        btn.style.transform = '';
        btn.style.transition = 'transform 0.5s cubic-bezier(0.16,1,0.3,1)';
        setTimeout(function() { btn.style.transition = ''; }, 500);
      });
    });
  }

  // -- Smooth scroll for anchor links --
  function initSmoothScroll() {
    document.querySelectorAll('a[href^="#"]').forEach(function(a) {
      a.addEventListener('click', function(e) {
        var target = document.querySelector(a.getAttribute('href'));
        if (target) {
          e.preventDefault();
          target.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
      });
    });
  }

  // -- Button click ripple --
  function initRipple() {
    document.addEventListener('click', function(e) {
      var btn = e.target.closest('.btn');
      if (!btn) return;

      var rect = btn.getBoundingClientRect();
      var size = Math.max(rect.width, rect.height) * 2;
      var ripple = document.createElement('span');
      ripple.className = 'ripple';
      ripple.style.width = ripple.style.height = size + 'px';
      ripple.style.left = (e.clientX - rect.left - size / 2) + 'px';
      ripple.style.top = (e.clientY - rect.top - size / 2) + 'px';
      btn.appendChild(ripple);
      ripple.addEventListener('animationend', function() { ripple.remove(); });
    });
  }

  // -- Scroll progress bar --
  function initScrollProgress() {
    var bar = document.createElement('div');
    bar.style.cssText =
      'position:fixed;top:0;left:0;height:2px;z-index:10001;' +
      'background:linear-gradient(90deg,#00D4AA,#5B5EA6,#FF6B6B);' +
      'width:0%;transition:width 0.1s linear;pointer-events:none;';
    document.body.appendChild(bar);

    window.addEventListener('scroll', function() {
      var h = document.documentElement.scrollHeight - window.innerHeight;
      if (h > 0) {
        bar.style.width = ((window.scrollY / h) * 100) + '%';
      }
    }, { passive: true });
  }

  // -- Hover glow on cards --
  function initCardGlow() {
    document.querySelectorAll('.feature-card, .step-card, .stat-landing-card, .testimonial-card, .voice-card, .voice-featured').forEach(function(card) {
      card.addEventListener('mousemove', function(e) {
        var rect = card.getBoundingClientRect();
        var x = e.clientX - rect.left;
        var y = e.clientY - rect.top;
        card.style.setProperty('--mouse-x', x + 'px');
        card.style.setProperty('--mouse-y', y + 'px');
      });
    });
  }

  // -- Boot --
  function init() {
    initStagger();
    initReveals();
    initCounters();
    initParallax();
    initNavScroll();
    initMagnetic();
    initSmoothScroll();
    initRipple();
    initScrollProgress();
    initCardGlow();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
