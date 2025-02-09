import { Utils } from './main.js';
import { mapState } from './map.js';

class SearchManager {
    constructor() {
        this.elements = {
            searchInput: document.getElementById('countrySearch'),
            searchBtn: document.getElementById('searchBtn'),
            regionFilter: document.getElementById('regionFilter')
        };
        
        this.countries = window.allCountries || [];
        this.init();
    }

    init() {
        if (!this.validateElements()) return;
        this.setupEventListeners();
    }

    validateElements() {
        return Object.values(this.elements).every(element => element !== null);
    }

    setupEventListeners() {
        const { searchInput, searchBtn, regionFilter } = this.elements;

        searchInput.addEventListener('input', 
            Utils.debounce(() => this.handleSearch(), 300)
        );
        
        searchBtn.addEventListener('click', 
            () => this.handleSearch()
        );
        
        regionFilter.addEventListener('change', 
            () => this.handleSearch()
        );
    }

    handleSearch() {
        const { searchInput, regionFilter } = this.elements;
        const query = searchInput.value.toLowerCase().trim();
        const region = regionFilter.value;

        if (!query && !region) {
            this.updateResults(this.countries);
            return;
        }

        const filteredCountries = this.filterCountries(query, region);
        this.updateResults(filteredCountries);
    }

    filterCountries(query, region) {
        return this.countries.filter(country => {
            const matchesQuery = !query || this.matchesSearchQuery(country, query);
            const matchesRegion = !region || country.region === region;
            return matchesQuery && matchesRegion;
        });
    }

    matchesSearchQuery(country, query) {
        return (
            country.countryName.toLowerCase().includes(query) ||
            country.localName?.toLowerCase().includes(query) ||
            country.countryCode.toLowerCase().includes(query)
        );
    }

    updateResults(countries) {
        // results.js의 updateDisplay 함수 호출
        if (typeof window.updateDisplay === 'function') {
            window.updateDisplay(countries);
        }
        
        // 지도 마커 업데이트
        mapState.updateMarkers(countries);
    }
}

// DOM이 로드된 후 SearchManager 초기화
document.addEventListener('DOMContentLoaded', () => {
    window.searchManager = new SearchManager();
});

export { SearchManager };