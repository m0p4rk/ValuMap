// Constants
export const CURRENCY_PLACEHOLDERS = {
    'USD': '1000',
    'KRW': '1000000',
    'JPY': '100000',
    'EUR': '1000'
};

export const NUMBER_FORMAT_OPTIONS = {
    USD: { minimumFractionDigits: 2, maximumFractionDigits: 2 },
    KRW: { minimumFractionDigits: 0, maximumFractionDigits: 0 },
    JPY: { minimumFractionDigits: 0, maximumFractionDigits: 0 },
    EUR: { minimumFractionDigits: 2, maximumFractionDigits: 2 }
};

// Utility class
export class Utils {
    static formatNumber(number, currency) {
        return new Intl.NumberFormat(
            'en-US', 
            NUMBER_FORMAT_OPTIONS[currency] || NUMBER_FORMAT_OPTIONS.USD
        ).format(number);
    }

    static formatCurrency(amount) {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
            minimumFractionDigits: 0
        }).format(amount);
    }

    static debounce(func, wait) {
        let timeout;
        return function(...args) {
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(this, args), wait);
        };
    }

    static handleError(error) {
        console.error('An error occurred:', error);
        // 여기에 사용자에게 보여줄 에러 처리 로직 추가 가능
    }
}

// Form Manager class
class FormManager {
    constructor() {
        this.setupFormValidation();
        this.setupCurrencySelector();
    }

    setupFormValidation() {
        const forms = document.querySelectorAll('.needs-validation');
        forms.forEach(form => {
            form.addEventListener('submit', this.handleFormSubmit.bind(this));
        });
    }

    handleFormSubmit(event) {
        const form = event.target;
        if (!form.checkValidity()) {
            event.preventDefault();
            event.stopPropagation();
        }
        form.classList.add('was-validated');
    }

    setupCurrencySelector() {
        const currencySelect = document.getElementById('baseCurrency');
        if (!currencySelect) return;
        
        currencySelect.addEventListener('change', (e) => {
            this.updatePlaceholder(e.target.value);
        });
    }

    updatePlaceholder(currency) {
        const budgetInput = document.getElementById('budget');
        if (!budgetInput) return;
        budgetInput.placeholder = CURRENCY_PLACEHOLDERS[currency] || '1000';
    }
}

// Exchange Rate Manager class
class ExchangeRateManager {
    constructor(updateInterval = 300000) {
        this.updateInterval = updateInterval;
        this.initialize();
    }

    initialize() {
        this.updateExchangeRates();
        setInterval(() => this.updateExchangeRates(), this.updateInterval);
    }

    updateExchangeRates() {
        const timestamp = new Date().toLocaleTimeString('en-US');
        const updateTimeElement = document.getElementById('updateTime');
        if (updateTimeElement) {
            updateTimeElement.textContent = `Last updated: ${timestamp}`;
        }
    }
}

// Initialize application
document.addEventListener('DOMContentLoaded', () => {
    new FormManager();
    new ExchangeRateManager();
});