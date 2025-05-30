// 验证逻辑
const validators = {
    user_name: value => value.length >= 3 || '用户名至少需要3个字符',
    password: value => value.length >= 8 || '密码至少需要8个字符',
    confirm_password: (value, form) =>
        value === form.password.value || '两次输入的密码不一致',
    email: value =>
        /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value) || '请输入有效的邮箱地址'
};

async function doRegister() 
{
    const form = document.getElementById('registerForm');
    const errorElements = document.querySelectorAll('.error-message');
    errorElements.forEach(el => el.style.display = 'none');

    let isValid = true;

    // 实时验证
    for (const [id, validator] of Object.entries(validators)) 
    {
        const input = document.getElementById(id);
        const errorEl = document.getElementById(`${id}Error`);
        const result = validator(input.value.trim(), form);

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

    const submitData = 
    {
        "userName": document.getElementById('user_name').value,
        "password": document.getElementById('password').value,
        "email": document.getElementById('email').value,
        "fullName": document.getElementById('full_name').value,
        "telephoneNumber": document.getElementById('phone_number').value
    };

    const dataJson = JSON.stringify(submitData);

    console.info(dataJson);

    try 
    {
        // 从 meta 标签获取 CSRF Token
        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        const response = await fetch(
            '/api/user_info/register',
            {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: dataJson
            }
        );

        if (!response.ok) 
        {
            const errorData = await response.text();
            throw new Error(errorData || '注册失败');
        }

        alert(`注册成功，欢迎用户：${submitData.userName}！`);

        // 重置表单
        document.querySelectorAll('input')
            .forEach(input => input.value = '');

        window.location = '/user_info/login';

    } 
    catch (error) 
    {
        alert('注册失败：' + error.message);
        console.error(error);

        // 重置表单
        document.querySelectorAll('input')
                .forEach(input => input.value = '');
    }
}