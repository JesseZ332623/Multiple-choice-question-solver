const isNumeric = (str) => { return /^\d+$/.test(str); };

// 验证逻辑
const validators = {
    password:  value => value.length >= 8 || '密码至少需要8个字符',
    varify_code: value => 
        (value.length === 8 && isNumeric(value)) || '请输入正确格式的验证码'
};

function showError(elementId, message) 
{
    const el = document.getElementById(elementId);

    el.textContent   = message;
    el.style.display = 'block';

    setTimeout(
        () => {el.textContent = ''; el.style.display = 'none'; },
        3000
    );
}

function showNotify(elementId, message)
{
    const el = document.getElementById(elementId);

    el.textContent = message;
    el.style.color = `#58A6FF`;

    setTimeout(
        () => {el.textContent = ''; el.style.display = 'none'; }, 
        2000
    );
}

async function doObtainVarifyCode()
{
    // 每次调用前，先清空错误和通知的内容。
    document.getElementById('varify_code_error').textContent = '';

    // 从 meta 标签获取 CSRF Token
    const csrfToken  = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    // 从表单中拿到用户名
    let userName  = document.getElementById('user_name_text').textContent.trim();
    const sendBtn = document.getElementById('send_verify_code');

    sendBtn.disabled = true;

    try 
    {
        const response = await fetch(
            `/api/email/send_verify_code_email/${userName}`,
            {
                method: 'POST',
                headers: { [csrfHeader]: csrfToken }
            }
        );

        if (!response.ok) 
        {
            sendBtn.disabled = false;
            throw new Error(
                `验证码发送失败，状态码：${response.status}\n原因：${await response.text()}。`
            );
        }

        showNotify('notify_message', '验证码已经发送至您的邮箱。');
        setTimeout(() => sendBtn.disabled = false, 90000);
    }
    catch (error) 
    {
        showError('varify_code_error', error.message);
    }
}

async function doDeleteAccount() 
{
    const form          = document.getElementById('delete_form');
    const errorElements = document.querySelectorAll('.error-message');

    errorElements.forEach(e => { e.style.display = 'none'; });

    let isValid = true;

    for (const [id, validator] of Object.entries(validators)) 
    {
        const input   = document.getElementById(id);
        const errorEl = document.getElementById(`${id}Error`);
        const result  = validator(input.value.trim(), form);

        if (result !== true) 
        {
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
        password   : document.getElementById('password').value,
        varifyCode : document.getElementById('varify_code').value
    };

    const deleteAccountDataJson = JSON.stringify(deleteAccountData);

    // console.info(deleteAccountDataJson);

    let deleteUserName = document.getElementById('user_name_text').textContent;

    if (!confirm(`用户：${deleteUserName}，这是一次敏感操作，确定要删除账户吗？`)) 
    {
        document.getElementById('cancel')
                .innerText = '很高兴你能够留下来！';

        return;
    }

    try 
    {
        // 从 meta 标签获取 CSRF Token
        const csrfToken  = document.querySelector('meta[name="_csrf"]').content;
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

        alert(`删除账户成功！再会，用户：${deleteUserName}！`);

        // 重置表单
        document.querySelectorAll('input').forEach(input => input.value = '');

        // 跳转至登录页面
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