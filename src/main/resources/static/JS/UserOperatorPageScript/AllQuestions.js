// 优化后的处理函数
function handleClear() 
{
    if (confirm('确定要清除所有题目的答对次数吗？此操作不可恢复！')) 
    {
        // 添加加载状态
        const btn = document.getElementById('clearButton');
        btn.disabled = true;
        btn.innerHTML = '<i class="fas fa-spinner fa-pulse"></i> 处理中...';

        // 从 meta 标签获取 CSRF Token
        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        let userName = document.getElementById('user_name').textContent;

        // 使用更现代的fetch API
        fetch(`/api/redis/clean_correct_times/${userName}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }
        })
            .then(response => {
                if (response.ok) {
                    location.reload(); // 简单重载页面
                } else {
                    throw new Error('清除失败');
                }
            })
            .catch(error => {
                alert(error.message);
                btn.disabled = false;
                btn.innerHTML = '<i class="fas fa-eraser"></i> 清除所有答对次数';
            });
    }
}

// 滚动性能优化
let lastScroll = 0;
window.addEventListener('scroll', () => {
    const now = Date.now();
    if (now - lastScroll > 100) {
        // 限制滚动事件处理频率
        lastScroll = now;
        // 添加自定义滚动处理（如果需要）
    }
}, { passive: true });