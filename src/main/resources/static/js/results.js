import { Utils } from './main.js';
import { mapState, COUNTRY_COORDINATES } from './map.js';

class ResultsManager {
    constructor() {
        this.allCountries = [];
        this.userInput = {
            budget: parseFloat(window.currentUserInput?.budget || 0),
            duration: parseInt(window.currentUserInput?.duration || 0),
            travelStyle: window.currentUserInput?.travelStyle || 'Standard'
        };
        this.selectedCountry = null;
        this.mapInitialized = false;
        this.init();
    }

    init() {
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => {
                this.createInitialLayout();
                this.loadCountries();
                this.setupSearch();
            });
        } else {
            this.createInitialLayout();
            this.loadCountries();
            this.setupSearch();
        }
    }

    createInitialLayout() {
        const grid = document.getElementById('countryGrid');
        if (!grid) return;

        // Remove existing content
        grid.innerHTML = '';

        // Add budget info section
        grid.appendChild(this.createBudgetSection());

        // Add map section
        grid.appendChild(this.createMapSection());

        // Initialize map
        setTimeout(() => {
            if (!this.mapInitialized) {
                mapState.init();
                this.mapInitialized = true;
            }
        }, 500);
    }

    async loadCountries() {
        try {
            const response = await fetch('/api/v1/valumap/countries', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(this.userInput)
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const data = await response.json();
            this.allCountries = data;
            this.updateDisplay(data);
            mapState.updateMarkers(data);
        } catch (error) {
            console.error('Failed to load countries:', error);
        }
    }

    updateDisplay(countries) {
        const grid = document.getElementById('countryGrid');
        if (!grid) return;

        // Update budget info section
        const existingBudgetInfo = grid.querySelector('.budget-info');
        if (existingBudgetInfo) {
            existingBudgetInfo.remove();
        }
        grid.insertBefore(this.createBudgetSection(), grid.firstChild);

        // Update map markers
        if (this.mapInitialized) {
            mapState.updateMarkers(countries);
        }

        // Remove existing recommendations section
        const existingRecommendations = grid.querySelector('.recommendations-section');
        if (existingRecommendations) {
            existingRecommendations.remove();
        }

        // Show nearby destinations when single country is selected or displayed
        if (countries.length === 1 || this.selectedCountry) {
            const targetCountry = countries.length === 1 ? countries[0] : this.selectedCountry;
            const recommendations = this.findNearbyCountries(targetCountry);
            
            if (recommendations && recommendations.length > 0) {
                const recommendationsSection = this.createRecommendationsSection(recommendations);
                grid.appendChild(recommendationsSection);
            }
        }
    }

    createMapSection() {
        const section = document.createElement('div');
        section.id = 'mapContainer';
        section.className = 'card shadow-sm mb-4';
        section.innerHTML = `
            <div class="card-body p-0">
                <div id="map" style="height: 500px; width: 100%;"></div>
            </div>
        `;
        return section;
    }

    createBudgetSection() {
        const section = document.createElement('div');
        section.className = 'budget-info mb-4';
        section.innerHTML = `
            <div class="row align-items-center">
                <div class="col-md-8">
                    <h4 class="mb-3">Travel Budget Information</h4>
                    <div class="d-flex gap-4">
                        <div>
                            <p class="mb-2"><i class="bi bi-wallet2"></i> Total Budget</p>
                            <h5>${Utils.formatCurrency(this.userInput.budget)}</h5>
                        </div>
                        <div>
                            <p class="mb-2"><i class="bi bi-person-walking"></i> Travel Style</p>
                            <h5>${this.userInput.travelStyle}</h5>
                        </div>
                    </div>
                </div>
            </div>
        `;
        return section;
    }

    createRecommendationsSection(recommendations) {
        const section = document.createElement('div');
        section.className = 'recommendations-section mb-4';
        section.innerHTML = `
            <div class="card shadow-sm">
                <div class="card-body">
                    <h4 class="mb-3">💡 Nearby Destinations</h4>
                    <div class="row g-4">
                        ${recommendations.map(country => this.createRecommendedCard(country)).join('')}
                    </div>
                </div>
            </div>
        `;
        return section;
    }

    createRecommendedCard(country) {
        const valueScore = country.valueScore || 0;
        const distance = Math.round(country.distance || 0);
        
        return `
            <div class="col-md-4">
                <div class="card h-100 border-0 shadow-sm">
                    <div class="card-body">
                        <div class="d-flex align-items-center mb-3">
                            <img src="https://flagcdn.com/w40/${country.countryCode.toLowerCase()}.png" 
                                 alt="${country.countryCode}" 
                                 class="me-2 rounded">
                            <div>
                                <h5 class="mb-0">${country.countryName}</h5>
                                <small class="text-muted">${country.localName}</small>
                            </div>
                        </div>
                        <div class="badge mb-3" 
                             style="background-color: ${this.getColorByScore(valueScore)}">
                            Value Score ${valueScore.toFixed(1)}
                        </div>
                        <div class="d-flex justify-content-between mb-2">
                            <span>Daily Cost</span>
                            <strong>${Utils.formatCurrency(country.dailyCost)}</strong>
                        </div>
                        <div class="d-flex justify-content-between mb-2">
                            <span>Possible Stay</span>
                            <strong>${country.possibleDays} days</strong>
                        </div>
                        <div class="d-flex justify-content-between">
                            <span>Distance</span>
                            <strong>${distance}km</strong>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    setupSearch() {
        const searchInput = document.getElementById('countrySearch');
        if (!searchInput) return;

        searchInput.addEventListener('input', Utils.debounce((event) => {
            const query = event.target.value.toLowerCase().trim();
            
            if (!query) {
                this.selectedCountry = null;  // Reset selected country when query is empty
                this.updateDisplay(this.allCountries);
                mapState.updateMarkers(this.allCountries);
                return;
            }

            const matches = this.allCountries.filter(country => 
                country.countryName.toLowerCase().includes(query) ||
                country.localName?.toLowerCase().includes(query) ||
                country.countryCode.toLowerCase().includes(query)
            );

            if (matches.length > 0) {
                if (matches.length === 1) {
                    this.selectedCountry = matches[0];
                    const coords = COUNTRY_COORDINATES[matches[0].countryCode];
                    if (coords) {
                        mapState.map.setView(coords, 6, {
                            animate: true,
                            duration: 1
                        });
                        mapState.selectCountry(matches[0]);
                    }
                } else {
                    this.selectedCountry = null;
                }
                this.updateDisplay(matches);
            }
        }, 300));
    }

    findNearbyCountries(targetCountry) {
        if (!targetCountry || !COUNTRY_COORDINATES[targetCountry.countryCode]) {
            console.log('Invalid target country:', targetCountry); // Debug
            return [];
        }

        const targetCoords = COUNTRY_COORDINATES[targetCountry.countryCode];
        console.log('Target coordinates:', targetCoords); // Debug

        return this.allCountries
            .filter(country => country.countryCode !== targetCountry.countryCode)
            .map(country => {
                const coords = COUNTRY_COORDINATES[country.countryCode];
                if (!coords) return null;

                const distance = this.calculateDistance(coords, targetCoords);
                return {
                    ...country,
                    distance: distance
                };
            })
            .filter(country => country !== null)
            .sort((a, b) => a.distance - b.distance)
            .slice(0, 3);
    }

    calculateDistance(coords1, coords2) {
        const R = 6371; // Earth's radius (km)
        const lat1 = coords1[0] * Math.PI / 180;
        const lat2 = coords2[0] * Math.PI / 180;
        const dLat = (coords2[0] - coords1[0]) * Math.PI / 180;
        const dLon = (coords2[1] - coords1[1]) * Math.PI / 180;

        const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                 Math.cos(lat1) * Math.cos(lat2) *
                 Math.sin(dLon/2) * Math.sin(dLon/2);
        
        return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    getColorByScore(score) {
        if (!score) return '#F44336';  // Red
        if (score >= 90) return '#4CAF50';  // Green
        if (score >= 80) return '#8BC34A';  // Light green
        if (score >= 70) return '#CDDC39';  // Lime
        if (score >= 60) return '#FFEB3B';  // Yellow
        if (score >= 50) return '#FFC107';  // Orange
        return '#F44336';  // Red
    }
}

// Create singleton instance
const resultsManager = new ResultsManager();

// Expose globally
window.resultsManager = resultsManager;

export { resultsManager };