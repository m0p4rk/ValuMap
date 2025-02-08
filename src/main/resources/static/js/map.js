document.addEventListener('DOMContentLoaded', function() {
    // 지도 초기화
    const map = L.map('map').setView([30, 0], 2);
    
    // OpenStreetMap 타일 레이어 추가
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors'
    }).addTo(map);

    // GeoJSON 스타일 함수
    function getCountryStyle(feature) {
        const countryCode = feature.properties.iso_a2;
        const countryValue = countryValues.find(cv => cv.countryCode === countryCode);
        
        if (!countryValue) {
            return {
                fillColor: '#gray',
                weight: 1,
                opacity: 1,
                color: 'white',
                fillOpacity: 0.3
            };
        }

        return {
            fillColor: countryValue.colorCode,
            weight: 1,
            opacity: 1,
            color: 'white',
            fillOpacity: 0.7
        };
    }

    // 팝업 내용 생성 함수
    function createPopupContent(countryValue) {
        return `
            <div class="country-popup">
                <h5>${countryValue.countryName}</h5>
                <p>
                    <strong>가성비 점수:</strong> ${countryValue.valueScore}점<br>
                    <strong>일일 비용:</strong> ${new Intl.NumberFormat('ko-KR').format(countryValue.dailyCost)} KRW<br>
                    <strong>여행 가능 일수:</strong> ${countryValue.possibleDays}일
                </p>
            </div>
        `;
    }

    // GeoJSON 데이터 로드 및 지도에 표시
    fetch('/js/data/world.geo.json')
        .then(response => response.json())
        .then(data => {
            L.geoJSON(data, {
                style: getCountryStyle,
                onEachFeature: function(feature, layer) {
                    const countryCode = feature.properties.iso_a2;
                    const countryValue = countryValues.find(cv => cv.countryCode === countryCode);
                    
                    if (countryValue) {
                        layer.bindPopup(createPopupContent(countryValue));
                        
                        layer.on({
                            mouseover: function(e) {
                                const layer = e.target;
                                layer.setStyle({
                                    weight: 2,
                                    fillOpacity: 0.9
                                });
                                layer.bringToFront();
                            },
                            mouseout: function(e) {
                                const layer = e.target;
                                layer.setStyle({
                                    weight: 1,
                                    fillOpacity: 0.7
                                });
                            },
                            click: function(e) {
                                map.fitBounds(e.target.getBounds());
                            }
                        });
                    }
                }
            }).addTo(map);
        })
        .catch(error => console.error('Error loading GeoJSON:', error));

    // 범례 추가
    const legend = L.control({position: 'bottomright'});
    legend.onAdd = function(map) {
        const div = L.DomUtil.create('div', 'info legend');
        const grades = [0, 20, 40, 60, 80];
        const colors = ['#FF0000', '#FFA500', '#FFFF00', '#90EE90', '#00FF00'];
        
        div.innerHTML = '<h6>가성비 점수</h6>';
        
        for (let i = 0; i < grades.length; i++) {
            div.innerHTML +=
                '<i style="background:' + colors[i] + '"></i> ' +
                grades[i] + (grades[i + 1] ? '&ndash;' + grades[i + 1] + '<br>' : '+');
        }
        
        return div;
    };
    legend.addTo(map);
});
