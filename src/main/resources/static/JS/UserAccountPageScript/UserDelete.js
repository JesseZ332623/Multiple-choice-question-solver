// 验证逻辑
const validators = {
    user_name: value => value.length >= 3 || '用户名至少需要3个字符',
    password: value => value.length >= 8 || '密码至少需要8个字符'
};

async function doDeleteAccount() 
{
    const form = document.getElementById('delete_form');
    const errorElements = document.querySelectorAll('.error-message');

    errorElements.forEach(e => { e.style.display = 'none'; });

    let isValid = true;

    for (const [id, validator] of Object.entries(validators)) {
        const input = document.getElementById(id);
        const errorEl = document.getElementById(`${id}Error`);
        const result = validator(input.value.trim(), form);

        if (result !== true) {
            input.classList.add('invalid');
            errorEl.textContent = result;
            errorEl.style.display = 'block';
            isValid = false;
        }
        else {
            input.classList.remove('invalid');
        }
    }

    if (!isValid) return;

    const deleteAccountData = {
        userName: document.getElementById('user_name').value,
        password: document.getElementById('password').value
    };

    const deleteAccountDataJson = JSON.stringify(deleteAccountData);

    console.info(deleteAccountDataJson);

    if (!confirm(`用户：${deleteAccountData.userName}，这是一次敏感操作，确定要删除账户吗？`)) {
        const cancelDisplay
            = document.getElementById('cancel');

        cancelDisplay.innerText = '很高兴你能够留下来！';

        return;
    }

    try 
    {
        // 从 meta 标签获取 CSRF Token
        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        const response = await fetch(
            '/api/user_info/delete',
            {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: deleteAccountDataJson
            }
        );

        if (!response.ok) {
            const errorData = await response.text();
            throw new Error(errorData || '无法删除账户')
        }

        alert(`删除账户成功！再会，用户：${deleteAccountData.userName}！`);

        // 重置表单
        document.querySelectorAll('input')
            .forEach(input => input.value = '');

        window.location = '/user_info/login';

    }
    catch (error) 
    {
        alert('登录失败：' + error.message);
        console.error(error);

        // 重置表单
        document.querySelectorAll('input')
            .forEach(input => input.value = '');
    }
}