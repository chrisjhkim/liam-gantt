// Liam Gantt Chart - Main Application JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Initialize application
    initializeApp();
});

/**
 * Initialize the application
 */
function initializeApp() {
    // Initialize tooltips
    initializeTooltips();
    
    // Initialize form validation
    initializeFormValidation();
    
    // Initialize auto-hide alerts
    initializeAutoHideAlerts();
    
    // Initialize loading states
    initializeLoadingStates();
    
    console.log('Liam Gantt Chart Application initialized');
}

/**
 * Initialize Bootstrap tooltips
 */
function initializeTooltips() {
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    const tooltipList = tooltipTriggerList.map(function(tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

/**
 * Initialize form validation
 */
function initializeFormValidation() {
    // Bootstrap validation
    const forms = document.querySelectorAll('.needs-validation');
    Array.prototype.slice.call(forms).forEach(function(form) {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        });
    });

    // Custom validation for date ranges
    const startDateInputs = document.querySelectorAll('input[name="startDate"], input[id="startDate"]');
    const endDateInputs = document.querySelectorAll('input[name="endDate"], input[id="endDate"]');
    
    function validateDateRange() {
        startDateInputs.forEach((startInput, index) => {
            const endInput = endDateInputs[index];
            if (startInput && endInput) {
                const startDate = new Date(startInput.value);
                const endDate = new Date(endInput.value);
                
                if (startDate && endDate && startDate > endDate) {
                    endInput.setCustomValidity('종료일은 시작일보다 늦어야 합니다.');
                    endInput.classList.add('is-invalid');
                } else {
                    endInput.setCustomValidity('');
                    endInput.classList.remove('is-invalid');
                }
            }
        });
    }
    
    startDateInputs.forEach(input => input.addEventListener('change', validateDateRange));
    endDateInputs.forEach(input => input.addEventListener('change', validateDateRange));
}

/**
 * Initialize auto-hide alerts
 */
function initializeAutoHideAlerts() {
    const alerts = document.querySelectorAll('.alert:not(.alert-permanent)');
    alerts.forEach(alert => {
        setTimeout(() => {
            if (alert && alert.parentNode) {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            }
        }, 5000); // Hide after 5 seconds
    });
}

/**
 * Initialize loading states for forms and buttons
 */
function initializeLoadingStates() {
    // Add loading state to form submissions
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function(event) {
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn && form.checkValidity()) {
                showLoadingState(submitBtn);
            }
        });
    });
    
    // Add loading state to AJAX buttons
    const ajaxButtons = document.querySelectorAll('[data-ajax="true"]');
    ajaxButtons.forEach(button => {
        button.addEventListener('click', function() {
            showLoadingState(this);
        });
    });
}

/**
 * Show loading state on button
 */
function showLoadingState(button) {
    const originalText = button.innerHTML;
    const originalDisabled = button.disabled;
    
    button.disabled = true;
    button.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>처리 중...';
    
    // Store original state for potential restoration
    button.setAttribute('data-original-text', originalText);
    button.setAttribute('data-original-disabled', originalDisabled);
}

/**
 * Hide loading state on button
 */
function hideLoadingState(button) {
    const originalText = button.getAttribute('data-original-text');
    const originalDisabled = button.getAttribute('data-original-disabled') === 'true';
    
    if (originalText) {
        button.innerHTML = originalText;
        button.disabled = originalDisabled;
        button.removeAttribute('data-original-text');
        button.removeAttribute('data-original-disabled');
    }
}

/**
 * Show toast notification
 */
function showToast(message, type = 'info', duration = 3000) {
    const toastContainer = getOrCreateToastContainer();
    
    const toastId = 'toast-' + Date.now();
    const toastHtml = `
        <div id="${toastId}" class="toast align-items-center text-white bg-${type} border-0" role="alert">
            <div class="d-flex">
                <div class="toast-body">
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        </div>
    `;
    
    toastContainer.insertAdjacentHTML('beforeend', toastHtml);
    
    const toastElement = document.getElementById(toastId);
    const toast = new bootstrap.Toast(toastElement, { delay: duration });
    toast.show();
    
    // Remove toast element after hiding
    toastElement.addEventListener('hidden.bs.toast', function() {
        toastElement.remove();
    });
}

/**
 * Get or create toast container
 */
function getOrCreateToastContainer() {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container position-fixed top-0 end-0 p-3';
        container.style.zIndex = '1080';
        document.body.appendChild(container);
    }
    return container;
}

/**
 * Confirm dialog helper
 */
function confirmAction(message, callback) {
    if (confirm(message)) {
        callback();
    }
}

/**
 * AJAX helper functions
 */
const Ajax = {
    get: function(url, options = {}) {
        return this.request('GET', url, null, options);
    },
    
    post: function(url, data, options = {}) {
        return this.request('POST', url, data, options);
    },
    
    put: function(url, data, options = {}) {
        return this.request('PUT', url, data, options);
    },
    
    delete: function(url, options = {}) {
        return this.request('DELETE', url, null, options);
    },
    
    request: function(method, url, data, options = {}) {
        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'same-origin'
        };
        
        const config = Object.assign({}, defaultOptions, options, {
            method: method
        });
        
        if (data) {
            config.body = JSON.stringify(data);
        }
        
        return fetch(url, config)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}: ${response.statusText}`);
                }
                return response.json();
            })
            .catch(error => {
                console.error('AJAX Error:', error);
                showToast('서버 오류가 발생했습니다.', 'danger');
                throw error;
            });
    }
};

/**
 * Format date helper
 */
function formatDate(date, format = 'YYYY-MM-DD') {
    if (!date) return '';
    
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');
    const seconds = String(d.getSeconds()).padStart(2, '0');
    
    return format
        .replace('YYYY', year)
        .replace('MM', month)
        .replace('DD', day)
        .replace('HH', hours)
        .replace('mm', minutes)
        .replace('ss', seconds);
}

/**
 * Debounce helper
 */
function debounce(func, wait, immediate) {
    let timeout;
    return function executedFunction() {
        const context = this;
        const args = arguments;
        const later = function() {
            timeout = null;
            if (!immediate) func.apply(context, args);
        };
        const callNow = immediate && !timeout;
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
        if (callNow) func.apply(context, args);
    };
}

/**
 * Progress bar animation
 */
function animateProgressBar(element, targetPercentage) {
    if (!element) return;
    
    const duration = 1000; // 1 second
    const startPercentage = 0;
    const startTime = performance.now();
    
    function updateProgress(currentTime) {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);
        const currentPercentage = startPercentage + (targetPercentage - startPercentage) * progress;
        
        element.style.width = currentPercentage + '%';
        element.setAttribute('aria-valuenow', Math.round(currentPercentage));
        
        if (progress < 1) {
            requestAnimationFrame(updateProgress);
        }
    }
    
    requestAnimationFrame(updateProgress);
}

/**
 * Export functions to global scope
 */
window.LiamGantt = {
    showToast,
    confirmAction,
    Ajax,
    formatDate,
    debounce,
    animateProgressBar,
    showLoadingState,
    hideLoadingState
};