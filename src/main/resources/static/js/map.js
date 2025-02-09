// Constants
const COUNTRY_COORDINATES = {
    'KR': [37.5665, 126.9780], 'JP': [35.6762, 139.6503],
    'CN': [35.8617, 104.1954], 'HK': [22.3193, 114.1694], 
    'TW': [23.6978, 120.9605], 'SG': [1.3521, 103.8198],
    'TH': [13.7563, 100.5018], 'VN': [14.0583, 108.2772],
    'MY': [4.2105, 101.9758], 'ID': [-0.7893, 113.9213],
    'PH': [12.8797, 121.7740], 'GB': [55.3781, -3.4360],
    'FR': [46.2276, 2.2137], 'DE': [51.1657, 10.4515],
    'IT': [41.8719, 12.5674], 'ES': [40.4637, -3.7492],
    'PT': [39.3999, -8.2245], 'GR': [39.0742, 21.8243],
    'AU': [-25.2744, 133.7751], 'NZ': [-40.9006, 174.8860]
};

const MAP_CONFIG = {
    DEFAULT_VIEW: [30, 0],
    DEFAULT_ZOOM: 2,
    TILE_LAYER: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
    CIRCLE_BASE_RADIUS: 200000,
    CIRCLE_SCORE_MULTIPLIER: 3000
};

class MapManager {
    constructor() {
        this.map = null;
        this.markers = [];
        this.isInitialized = false;
    }

    init() {
        try {
            const mapElement = document.getElementById('map');
            if (!mapElement) return;

            if (this.map) {
                this.map.remove();
            }

            this.map = L.map('map', {
                center: MAP_CONFIG.DEFAULT_VIEW,
                zoom: MAP_CONFIG.DEFAULT_ZOOM,
                zoomControl: true
            });

            L.tileLayer(MAP_CONFIG.TILE_LAYER, {
                attribution: '© OpenStreetMap contributors'
            }).addTo(this.map);

            this.isInitialized = true;
            console.log('Map initialized successfully');
        } catch (error) {
            console.error('Map initialization error:', error);
        }
    }

    updateMarkers(countries) {
        if (!this.map || !this.isInitialized) {
            console.log('Map not ready, retrying...');
            setTimeout(() => this.updateMarkers(countries), 100);
            return;
        }
        
        this.clearMarkers();
        countries.forEach(country => this.addMarker(country));
    }

    addMarker(country) {
        const coords = COUNTRY_COORDINATES[country.countryCode];
        if (!coords || !this.map) return;

        try {
            const circle = this.createCircleMarker(coords, country);
            
            const popupContent = this.createPopupContent(country);
            const popup = L.popup({
                closeButton: true,
                closeOnClick: false,
                autoClose: false,
                className: 'country-popup-container'
            });
            
            popup.setContent(popupContent);
            circle.bindPopup(popup);
            
            // Click event
            circle.on('click', () => {
                circle.openPopup();
                this.selectCountry(country);
                if (window.resultsManager) {
                    window.resultsManager.selectedCountry = country;
                    window.resultsManager.updateDisplay([country]);
                }
            });

            // Mouse events
            let mouseoverTimeout;
            
            circle.on('mouseover', () => {
                clearTimeout(mouseoverTimeout);
                circle.openPopup();
            });

            circle.on('mouseout', () => {
                mouseoverTimeout = setTimeout(() => {
                    // Only close popup if country is not selected
                    if (!window.resultsManager?.selectedCountry || 
                        window.resultsManager.selectedCountry.countryCode !== country.countryCode) {
                        circle.closePopup();
                    }
                }, 100); // Small delay to prevent flickering when moving between marker and popup
            });

            this.markers.push(circle);
        } catch (error) {
            console.error(`Error adding marker for ${country.countryName}:`, error);
        }
    }

    createCircleMarker(coords, country) {
        const valueScore = country.valueScore || 0;
        return L.circle(coords, {
            radius: MAP_CONFIG.CIRCLE_BASE_RADIUS + (valueScore * MAP_CONFIG.CIRCLE_SCORE_MULTIPLIER),
            color: this.getColorByScore(valueScore),
            fillColor: this.getColorByScore(valueScore),
            fillOpacity: 0.6,
            weight: 1,
            interactive: true
        }).addTo(this.map);
    }

    createPopupContent(country) {
        const valueScore = country.valueScore || 0;
        const dailyCost = country.dailyCost || 0;
        
        return `
            <div class="country-popup text-center" style="min-width: 200px; padding: 10px;">
                <img src="https://flagcdn.com/w40/${country.countryCode.toLowerCase()}.png" 
                     alt="${country.countryCode}" 
                     style="width: 40px; height: auto; margin-bottom: 8px;">
                <h6 style="margin: 0 0 8px 0; font-size: 16px; font-weight: bold;">${country.countryName}</h6>
                <p style="margin: 0 0 8px 0; color: #666;">${country.localName || ''}</p>
                <div style="background-color: ${this.getColorByScore(valueScore)}; 
                            color: white; 
                            padding: 4px 8px; 
                            border-radius: 4px; 
                            margin-bottom: 8px; 
                            display: inline-block;">
                    Value Score ${valueScore.toFixed(1)}
                </div>
                <p style="margin: 0; font-weight: 500;">Daily Cost: $${dailyCost.toLocaleString()}</p>
            </div>
        `;
    }

    selectCountry(country) {
        if (!this.map || !this.isInitialized) return;

        const coords = COUNTRY_COORDINATES[country.countryCode];
        if (!coords) return;

        this.map.setView(coords, 6, { animate: true });
        this.markers.forEach(marker => {
            if (marker.getLatLng().lat === coords[0] && marker.getLatLng().lng === coords[1]) {
                marker.openPopup();
            }
        });
    }

    clearMarkers() {
        this.markers.forEach(marker => {
            if (marker && this.map) {
                marker.remove();
            }
        });
        this.markers = [];
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

const mapState = new MapManager();

export { mapState, COUNTRY_COORDINATES };