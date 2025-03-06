
function updateLocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function(position) {
            const lat = position.coords.latitude;
            const lon = position.coords.longitude;

            // 只有当位置发生变化时才更新
            if (lat !== currentLocation.latitude || lon !== currentLocation.longitude) {
                currentLocation.latitude = lat;
                currentLocation.longitude = lon;
                sendLocationToBackend(lat, lon);
            }
        });
    }
}

function sendLocationToBackend(latitude, longitude) {
    fetch(baseUrl + '/api/vendor-analysis', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            latitude: latitude,
            longitude: longitude
        })
    }).then(response => {
        if (response.ok) {
            console.log('位置信息已更新');
        }
    }).catch(error => {
        console.error('更新位置信息失败:', error);
    });
}