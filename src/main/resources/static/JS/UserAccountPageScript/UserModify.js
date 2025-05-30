const validators =
{
    old_user_name: value => value.length >= 3 || '用户名至少需要3个字符',
    new_user_name: value => value.length >= 3 || '用户名至少需要3个字符',
    old_password: value => value.length >= 8 || '密码至少需要8个字符',
    new_password: value => value.length >= 8 || '密码至少需要8个字符',
    confirm_password: (value, form) =>
        value === document.getElementById('new_password').value ||
        '两次输入的密码不一致',
    email: value =>
        /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value) || '请输入有效的邮箱地址'
};


async function doModify() 
{
    const form = document.getElementById('modifyForm');
    const errorElements = document.querySelectorAll('.error-message');
    errorElements.forEach(el => el.style.display = 'none');

    let isValid = true;

    // 实时验证
    for (const [id, validator] of Object.entries(validators)) 
    {
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

    const submitData =
    {
        userLoginDTO: {
            userName: document.getElementById('old_user_name').value,
            password: document.getElementById('old_password').value
        },
        userMidifyInfoDTO: {
            newUserName: document.getElementById('new_user_name').value,
            newPassword: document.getElementById('new_password').value,
            newFullName: document.getElementById('full_name').value,
            newTelephoneNumber: document.getElementById('phone_number').value,
            newEmail: document.getElementById('email').value
        }
    };

    const submitDataJson = JSON.stringify(submitData);

    console.info(submitDataJson);

    try {
        // 从 meta 标签获取 CSRF Token
        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        const response = await fetch(
            '/api/user_info/modify',
            {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: submitDataJson
            }
        );

        if (!response.ok) {
            const errorData = await response.text();
            throw new Error(errorData || '修改失败');
        }

        alert(`修改成功，欢迎用户：${submitData.userMidifyInfoDTO.newUserName}，请重新进行登录！`);

        // 重置表单
        document.querySelectorAll('input')
            .forEach(input => input.value = '');

        window.location = '/user_info/login';
    }
    catch (error) {
        alert('注册失败：' + error.message);
        console.error(error);

        // 重置表单
        document.querySelectorAll('input')
            .forEach(input => input.value = '');
    }
}