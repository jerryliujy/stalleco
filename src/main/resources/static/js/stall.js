let timer = null;
let currentSession = null;
let startTime = null;

window.onload = function() {
    console.log('页面加载完成');
    checkLogin();
    console.log('页面加载完成');
    document.getElementById('startStallBtn').addEventListener('click', startStall);
    document.getElementById('endStallBtn').addEventListener('click', showEndStallSection);
    document.getElementById('cleanlinessPhoto').addEventListener('change', handlePhotoUpload);
    document.getElementById('submitEndStall').addEventListener('click', endStall);
    console.log('页面加载完成');
}

function startTimer() {
    startTime = new Date();
    timer = setInterval(updateTimer, 1000);
}

function updateTimer() {
    const now = new Date();
    const diff = now - startTime;
    const hours = Math.floor(diff / 3600000);
    const minutes = Math.floor((diff % 3600000) / 60000);
    const seconds = Math.floor((diff % 60000) / 1000);
    
    document.getElementById('timerDisplay').textContent = 
        `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
}

function startStall() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function(position) {
            const lat = position.coords.latitude;
            const lon = position.coords.longitude;
            
            const currentUser = getCurrentUser();
            fetch(baseUrl + '/stall/start', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    vendorId: currentUser.id,
                    latitude: lat,
                    longitude: lon
                })
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('开始摆摊失败');
                }
                return response.json();
            })
            .then(data => {
                currentSession = data;
                document.getElementById('startPage').style.display = 'none';
                document.getElementById('timerPage').style.display = 'block';
                startTimer();
            })
            .catch(error => {
                alert('开始摆摊失败：' + error.message);
            });
        }, function(error) {
            alert('获取位置信息失败：' + error.message);
        });
    } else {
        alert('您的浏览器不支持地理定位');
    }
}

function showEndStallSection() {
    document.getElementById('timerPage').style.display = 'none';
    document.getElementById('endStallPage').style.display = 'block';
}

function handlePhotoUpload(event) {
    const file = event.target.files[0];
    if (file) {
        document.getElementById('submitEndStall').disabled = false;
        
        // 预览照片
        const reader = new FileReader();
        reader.onload = function(e) {
            const preview = document.getElementById('photoPreview');
            preview.innerHTML = `<img src="${e.target.result}" style="max-width: 300px;">`;
        };
        reader.readAsDataURL(file);
    }
}