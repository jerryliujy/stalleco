
document.getElementById('analyzeButton').addEventListener('click', function() {
    const vendorId = document.getElementById('vendorSelect').value;
    const imageFile = document.getElementById('imageUpload').files[0];

    if (vendorId && imageFile) {
        analyzeVendor(vendorId, imageFile)
            .then(result => {
                document.getElementById('analysisResult').textContent = result;
            })
            .catch(error => {
                console.error('分析失败:', error);
                document.getElementById('analysisResult').textContent = '分析失败，请重试';
            });
    } else {
        alert('请选择摊主和上传图片');
    }
});

// 示例：添加一个本地分析按钮的点击事件处理器
document.getElementById('analyzeLocalButton').addEventListener('click', function() {
    const vendorId = document.getElementById('vendorSelect').value;
    const imagePath = document.getElementById('imagePath').value;

    if (vendorId && imagePath) {
        analyzeVendorLocal(vendorId, imagePath)
            .then(result => {
                document.getElementById('analysisResult').textContent = result;
            })
            .catch(error => {
                console.error('分析失败:', error);
                document.getElementById('analysisResult').textContent = '分析失败，请重试';
            });
    } else {
        alert('请选择摊主和输入图片路径');
    }
});

function analyzeVendor(vendorId, imageFile) {
    // 创建FormData对象
    const formData = new FormData();
    formData.append('vendorId', vendorId);
    formData.append('image', imageFile);

    // 添加位置信息
    if (currentLocation.latitude && currentLocation.longitude) {
        formData.append('latitude', currentLocation.latitude);
        formData.append('longitude', currentLocation.longitude);
    }

    // 发送请求
    return fetch(baseUrl + '/api/vendor-analysis/analyse', {
        method: 'POST',
        body: formData
    })
        .then(response => response.text());
}

// 添加一个本地分析函数
function analyzeVendorLocal(vendorId, imagePath) {
    // 创建URL参数
    let url = new URL(baseUrl + '/api/vendor-analysis/analyse-local');
    let params = new URLSearchParams();
    params.append('vendorId', vendorId);
    params.append('path', imagePath);

    // 添加位置信息
    if (currentLocation.latitude && currentLocation.longitude) {
        params.append('latitude', currentLocation.latitude);
        params.append('longitude', currentLocation.longitude);
    }

    url.search = params.toString();

    // 发送请求
    return fetch(url, {
        method: 'POST'
    })
        .then(response => response.text());
}

