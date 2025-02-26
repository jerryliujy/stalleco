// 后端 API 地址（根据实际部署修改）
const baseUrl = 'http://localhost:8080';

// 从 localStorage 中获取当前登录的用户
function getCurrentUser() {
  const user = localStorage.getItem('currentUser');
  return user ? JSON.parse(user) : null;
}

// 检查登录状态（未登录则跳转到登录页面）
function checkLogin() {
  const user = getCurrentUser();
  if (!user) {
    window.location.href = 'login.html';
  }
}

// 登出处理
function logoutUser() {
  localStorage.removeItem('currentUser');
  window.location.href = 'login.html';
}

// 登录接口调用
function loginUser(username, password) {
  fetch(baseUrl + '/vendors/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ username, password })
  })
  .then(response => {
    if (response.ok) return response.json();
    else throw new Error('登录失败');
  })
  .then(data => {
    // 登录成功，将用户信息保存在 localStorage 中
    localStorage.setItem('currentUser', JSON.stringify(data));
    window.location.href = 'home.html';
  })
  .catch(error => {
    document.getElementById('loginMessage').innerText = error.message;
  });
}

// 注册接口调用（仅传递用户名和密码）
function registerUser(username, password) {
  fetch(baseUrl + '/vendors/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ username, password })
  })
  .then(response => {
    if (response.ok) return response.json();
    else throw new Error('注册失败：用户名可能已存在');
  })
  .then(data => {
    alert('注册成功，请登录');
    window.location.href = 'login.html';
  })
  .catch(error => {
    document.getElementById('registerMessage').innerText = error.message;
  });
}

// 更新个人摊位信息，通过 PUT 接口更新
function updateProfile(vendorId, updatedVendor) {
  fetch(baseUrl + '/vendors/' + vendorId, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(updatedVendor)
  })
  .then(response => {
    if (response.ok) return response.json();
    else throw new Error('更新失败');
  })
  .then(data => {
    // 更新成功后，更新本地存储中的用户信息
    let currentUser = getCurrentUser();
    currentUser = { ...currentUser, ...data };
    localStorage.setItem('currentUser', JSON.stringify(currentUser));
    document.getElementById('profileMessage').innerText = "更新成功";
  })
  .catch(error => {
    document.getElementById('profileMessage').innerText = error.message;
  });
}

// 社区模式：创建帖子
function createPost(content, imageUrls, vendorId) {
  const postData = {
    content: content,
    imageUrls: imageUrls,
    vendor: { id: vendorId }
  };
  fetch(baseUrl + '/posts', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(postData)
  })
  .then(response => {
    if(response.ok) return response.json();
    else throw new Error('发帖失败');
  })
  .then(data => {
    document.getElementById('postMessage').innerText = "帖子已发布";
    document.getElementById('postContent').value = "";
    document.getElementById('postImageUrls').value = "";
    loadPosts(); // 刷新帖子列表
  })
  .catch(error => {
    document.getElementById('postMessage').innerText = error.message;
  });
}

// 加载所有帖子，并显示在页面中
function loadPosts() {
  fetch(baseUrl + '/posts')
    .then(response => response.json())
    .then(data => {
      const postsList = document.getElementById('postsList');
      postsList.innerHTML = '';
      data.forEach(post => {
        const postDiv = document.createElement('div');
        postDiv.className = 'post';
        postDiv.innerHTML = `<p><strong>${post.vendor.stallName || post.vendor.username}</strong>:</p>
                             <p>${post.content}</p>`;
        
        // 评论区域
        const commentSection = document.createElement('div');
        commentSection.className = 'comment-section';
        commentSection.innerHTML = `<h4>评论</h4>`;
        
        // 显示已有评论
        if(post.comments && post.comments.length > 0) {
          post.comments.forEach(comment => {
            const commentDiv = document.createElement('div');
            commentDiv.className = 'comment';
            commentDiv.innerHTML = `<p><strong>${comment.vendor.stallName || comment.vendor.username}</strong>: ${comment.content}</p>`;
            commentSection.appendChild(commentDiv);
          });
        }
        
        // 添加评论表单
        const commentForm = document.createElement('form');
        commentForm.innerHTML = `<input type="text" placeholder="添加评论" required>
                                 <button type="submit">评论</button>`;
        commentForm.addEventListener('submit', function(e) {
          e.preventDefault();
          const commentContent = this.querySelector('input').value;
          const currentUser = getCurrentUser();
          addComment(post.id, commentContent, currentUser.id, commentSection);
          this.querySelector('input').value = "";
        });
        commentSection.appendChild(commentForm);
        postDiv.appendChild(commentSection);
        postsList.appendChild(postDiv);
      });
    })
    .catch(error => {
      document.getElementById('postsList').innerText = '加载帖子失败: ' + error.message;
    });
}

// 社区模式：添加评论
function addComment(postId, content, vendorId, commentSection) {
  const commentData = {
    content: content,
    vendor: { id: vendorId }
  };
  fetch(baseUrl + '/posts/' + postId + '/comments', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(commentData)
  })
  .then(response => {
    if(response.ok) return response.json();
    else throw new Error('评论失败');
  })
  .then(data => {
    const commentDiv = document.createElement('div');
    commentDiv.className = 'comment';
    commentDiv.innerHTML = `<p><strong>${data.vendor.stallName || data.vendor.username}</strong>: ${data.content}</p>`;
    commentSection.insertBefore(commentDiv, commentSection.lastElementChild);
  })
  .catch(error => {
    alert(error.message);
  });
}