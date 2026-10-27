'use strict';

// Keep simple client-side helpers here; server-side validation remains authoritative.
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('form[data-confirm]').forEach((form) => {
    form.addEventListener('submit', (event) => {
      if (form.dataset.confirm && !window.confirm(form.dataset.confirm)) event.preventDefault();
    });
  });
});
