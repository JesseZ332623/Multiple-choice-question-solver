// 优化后的处理函数
async function handleClear() 
{
    if (confirm('确定要清除所有题目的答对次数吗？此操作不可恢复！')) 
    {
        // 添加加载状态
        const btn     = document.getElementById('clearButton');
        btn.disabled  = true;
        btn.innerHTML = '<i class="fas fa-spinner fa-pulse"></i> 处理中...';

        try 
        {
            // 从 meta 标签获取 CSRF Token
            const csrfToken  = document.querySelector('meta[name="_csrf"]').content;
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

            let userName = document.getElementById('user_name_text').textContent;

            const response = await fetch(
                `/api/redis/clean_correct_times/${userName}`,
                {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        [csrfHeader]: csrfToken
                    }
                }
            );

            if (!response.ok) 
            {
                throw new Error(
                    `清除失败！状态码：${response.status}, 原因：${await response.text()}`
                );
            }

            alert('已将所有问题的答对次数设置为 0！');
        }
        catch (error)
        {
            console.error(error);
            alert(error.message);
            
            btn.disabled = false;
            btn.innerHTML = '<i class="fas fa-eraser"></i> 清除所有答对次数';
        }
        finally 
        {
            btn.disabled = false;
            btn.innerHTML = '<i class="fas fa-eraser"></i> 清除所有答对次数';
        }
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