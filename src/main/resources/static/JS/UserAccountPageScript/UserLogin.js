// 增强版验证逻辑
const validators = {
    user_name: value => value.length >= 3 || '用户名至少需要3个字符',
    password: value => value.length >= 8 || '密码至少需要8个字符',
    verify_code: value => value.length >= 0 || '验证码不得为空'
};

function showError(elementId, message) 
{
    const el = document.getElementById(elementId);
    el.textContent = message;
    el.style.display = 'block';
}

function showNotify(message) 
{
    const el = document.getElementById('notifyMessage');
    el.textContent = message;
    el.style.color = `#58A6FF`;
}

async function doObtainVarifyCode() 
{
    // 每次调用前，先清空错误和通知的内容。
    document.getElementById('verify_codeError').textContent = '';
    document.getElementById('notifyMessage').textContent = '';

    // 从 meta 标签获取 CSRF Token
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    // 从表单中拿到用户名
    let userName = document.getElementById('user_name').value.trim();
    const sendBtn = document.getElementById('send_verify_code');

    if (!userName) {
        showError('user_nameError', "请先输入用户名!");
        return;
    }

    sendBtn.disabled = true;

    try {
        const response = await fetch(
            `/api/email/send_verify_code_email/${userName}`,
            {
                method: 'POST',
                headers: { [csrfHeader]: csrfToken }
            }
        );

        if (!response.ok) {
            sendBtn.disabled = false;
            throw new Error(
                `验证码发送失败，状态码：${response.status}\n原因：${await response.text()}。`
            );
        }

        showNotify('验证码已经发送至您的邮箱。');
        setTimeout(() => sendBtn.disabled = false, 90000);
    }
    catch (error) {
        showError('verify_codeError', error.message);
    }
}

async function doLogin() 
{
    const form = document.getElementById('loginForm');
    const errorElements = document.querySelectorAll('.error-message');

    errorElements.forEach(e => { e.style.display = 'none'; });

    let isValid = true;

    for (const [id, validator] of Object.entries(validators)) {
        const input = document.getElementById(id);
        const errorEl = document.getElementById(`${id}Error`);

        if (!input.value) {
            input.classList.add('invalid');
            errorEl.textContent = result;
            errorEl.style.display = 'block';
            isValid = false;
        }

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

    if (!isValid) { return; }

    const loginData =
    {
        userName: document.getElementById('user_name').value,
        password: document.getElementById('password').value,
        verifyCode: document.getElementById('verify_code').value
    };

    const loginDataJson = JSON.stringify(loginData);

    console.info(loginDataJson);

    try {
        // 从 meta 标签获取 CSRF Token
        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        const response = await fetch(
            '/api/user_info/login',
            {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: loginDataJson
                //credentials: 'include'
            }
        );

        if (!response.ok) {
            const errorData = await response.text();
            throw new Error(errorData || '登录失败');
        }

        alert(`登录成功，欢迎用户：${loginData.userName}！`);

        // 重置表单
        document.querySelectorAll('input')
            .forEach(input => input.value = '');

        window.location.href = '/user_info/user_front_page';
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