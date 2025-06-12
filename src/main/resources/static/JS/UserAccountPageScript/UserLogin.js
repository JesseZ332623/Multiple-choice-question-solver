const isNumeric = (str) => { return /^\d+$/.test(str); };

// 增强版验证逻辑
const validators = {
    user_name:   value => value.length >= 3 || '用户名至少需要3个字符',
    password:    value => value.length >= 8 || '密码至少需要8个字符',
    verify_code: value => (value.length === 8 && isNumeric(value)) || '请输入正确格式的验证码'
};

function showError(elementId, message) 
{
    const el = document.getElementById(elementId);
    el.textContent = message;
    el.style.display = 'block';

    setTimeout(
        () => {el.textContent = ''; el.style.display = 'none'; },
        3000
    );
}

function showNotify(message) 
{
    const el = document.getElementById('notifyMessage');
    el.textContent = message;
    el.style.color = `#58A6FF`;

    setTimeout(
        () => {el.textContent = ''; el.style.display = 'none'; },
        3000
    );
}

async function doObtainVarifyCode() 
{
    // 每次调用前，先清空错误和通知的内容。
    document.getElementById('verify_codeError').textContent = '';
    document.getElementById('notifyMessage').textContent    = '';

    // 从 meta 标签获取 CSRF Token
    const csrfToken  = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    // 从表单中拿到用户名
    let userName  = document.getElementById('user_name').value.trim();
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

const USER_ROLE_SELECT = {
    ROLE_NOT_SELECT : -1,
    ADMIN_USER      :  0,
    ORDINARY_USER   :  1
};
/**
 * 检查用户的角色，有三种情况：
 * 
 * @returns ROLE_NOT_SELECT 用户没有选择角色
 *          ADMIN_USER      管理员
 *          ORDINARY_USER   普通用户
*/
function checkUserRole() 
{
    const roleRadios = document.getElementsByName('role');

    if (
        roleRadios.item(0).checked === false &&
        roleRadios.item(1).checked === false
    ) {
        showError('role_select_error', '请选择你的身份！');
        return USER_ROLE_SELECT.ROLE_NOT_SELECT; 
    }

    for (const radio of roleRadios) 
    {
        if (radio.checked && radio.id === 'ordinary_user_radio') {
            return USER_ROLE_SELECT.ORDINARY_USER;
        }
        else { return USER_ROLE_SELECT.ADMIN_USER; }
    }
}

async function doLogin()
{
    const form = document.getElementById('loginForm');
    const errorElements = document.querySelectorAll('.error-message');

    errorElements.forEach(e => { e.style.display = 'none'; });

    let isValid = true;

    for (const [id, validator] of Object.entries(validators)) 
    {
        const input   = document.getElementById(id);
        const errorEl = document.getElementById(`${id}_error`);

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

    const userRole = checkUserRole();

    if (!isValid || userRole === USER_ROLE_SELECT.ROLE_NOT_SELECT) { 
        return; 
    }

    const loginData = {
        userName:   document.getElementById('user_name').value,
        password:   document.getElementById('password').value,
        verifyCode: document.getElementById('verify_code').value
    };

    const loginDataJson = JSON.stringify(loginData);

    try 
    {
        // 从 meta 标签获取 CSRF Token
        const csrfToken  = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
        
        // 根据前端所判断的不同类型，来决定调用哪个 API 进行登录操作
        if (userRole === USER_ROLE_SELECT.ORDINARY_USER)
        {
            const response = await fetch(
                '/api/user_info/login',
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        [csrfHeader]: csrfToken
                    },
                    body: loginDataJson
                }
            );

            if (!response.ok) 
            {
                const errorData = await response.text();
                throw new Error(errorData || '登录失败');
            }

            alert(`登录成功，欢迎普通用户：${loginData.userName}！`);

            // 重置表单
            document.querySelectorAll('input').forEach(input => input.value = '');

            window.location.href = '/user_info/user_front_page';
        }
        else if (userRole === USER_ROLE_SELECT.ADMIN_USER) 
        {
            // 调用管理员登录 API
            const response = await fetch(
                '/api/admin/login',
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        [csrfHeader]: csrfToken
                    },
                    body: loginDataJson
                }
            );

            if (!response.ok) 
            {
                const errorData = await response.text();
                throw new Error(errorData || '登录失败');
            }

            alert(`登录成功，欢迎管理员：${loginData.userName}！`);

            // 重置表单
            document.querySelectorAll('input').forEach(input => input.value = '');

            window.location.href = '/admin/all_users';
        }
        else 
        {
            /* 
                此处前面的判断逻辑已经防住了无任何选择的情况，
                所以这里什么都不用做。
            */
        }
    }
    catch (error) 
    {
        console.error(error);
        alert('登录失败：' + error.message);

        // 重置表单
        document.querySelectorAll('input').forEach(input => input.value = '');

        // 看起来验证码的按钮需要重新打开？
        document.getElementById('send_verify_code').disabled = false;
    }
}