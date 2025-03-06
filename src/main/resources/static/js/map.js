let map = null;
let markers = [];
let currentLocation = {
    latitude: null,
    longitude: null
};

window.onload = function() {
    checkLogin();
    initMap();
    loadActiveVendors();
    
    const user = getCurrentUser();
    document.getElementById('welcomeUser').innerText = user.username;
    document.getElementById('logoutLink').addEventListener('click', function(e) {
        e.preventDefault();
        logoutUser();
    });
};

function initMap() {
    map = new AMap.Map('map', {
        zoom: 15
    });
    
    // 获取当前位置并设置为地图中心
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function(position) {
            const lat = position.coords.latitude;
            const lon = position.coords.longitude;
            map.setCenter([lon, lat]);
        });
    }
}

function updateLocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function(position) {
            const lat = position.coords.latitude;
            const lon = position.coords.longitude;

            // 只有当位置发生变化时才更新
            if (lat !== currentLocation.latitude || lon !== currentLocation.longitude) {
                currentLocation.latitude = lat;
                currentLocation.longitude = lon;
            }
        });
    }
}

function loadActiveVendors() {
    fetch(baseUrl + '/vendors/active')
        .then(response => response.json())
        .then(vendors => {
            clearMarkers();
            vendors.forEach(vendor => {
                if (vendor.latitude && vendor.longitude) {
                    addVendorMarker(vendor);
                }
            });
        });
}

function addVendorMarker(vendor) {
    const marker = new AMap.Marker({
        position: [vendor.longitude, vendor.latitude],
        title: vendor.stallName || vendor.username
    });
    
    // 添加信息窗体
    const info = new AMap.InfoWindow({
        content: `
            <div>
                <h4>${vendor.stallName || vendor.username}</h4>
                <p>${vendor.description || '暂无描述'}</p>
                <p>摆摊时长：${calculateDuration(vendor.startTime)}</p>
            </div>
        `,
        offset: new AMap.Pixel(0, -30)
    });
    
    marker.on('click', () => {
        info.open(map, marker.getPosition());
    });
    
    marker.setMap(map);
    markers.push(marker);
}

function clearMarkers() {
    markers.forEach(marker => marker.setMap(null));
    markers = [];
}

function calculateDuration(startTime) {
    const start = new Date(startTime);
    const now = new Date();
    const diff = now - start;
    const hours = Math.floor(diff / 3600000);
    const minutes = Math.floor((diff % 3600000) / 60000);
    return `${hours}小时${minutes}分钟`;
}

// 定期刷新摊贩位置
setInterval(loadActiveVendors, 30000); // 每30秒刷新一次
setInterval(updateLocation, 60000);