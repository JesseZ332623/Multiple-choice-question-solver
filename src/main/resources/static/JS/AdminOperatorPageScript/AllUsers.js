async function adminLogout() 
{
    try 
    {
        const csrfToken  = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        const response = await fetch(
            '/api/admin/logout',
            {
                method: 'POST',
                headers: {
                    'Content-Type' : 'application/json',
                    [csrfHeader]: csrfToken
                }
            }
        );

        if (!response.ok) {
            throw new Error(await response.text());
        }

        alert(await response.text());

        window.location.href = '/user_info/login';
    }
    catch (error)
    {
        console.error(error);
        alert(error.message);
    }
}


const USER_ROLES  = { "roleId": 2, "roleName": "ROLE_USER" };
const ADMIN_ROLES = { "roleId": 1, "roleName": "ROLE_ADMIN" };

function createUserRolesJson(newRolesText) 
{
    let newRoles;

    if (newRolesText === "ROLE_USER") {
        newRoles = [USER_ROLES];
    }
    else if (newRolesText === "ROLE_ADMIN") {
        newRoles = [ADMIN_ROLES];
    }
    else if (
        newRolesText === "ROLE_USER ROLE_ADMIN" ||
        newRolesText === "ROLE_ADMIN ROLE_USER"
    ) {
        newRoles = [USER_ROLES, ADMIN_ROLES];
    }
    else {
        newRoles = [USER_ROLES];
    }

    return newRoles;
}

/*
    进行头像修改。
*/
async function doAvatarModify(userName, index) 
{
    // 文件大小不得超过 8 MB
    const MAX_FILE_SIZE = 8 * 1024 * 1024;

    // 支持的文件类型数组
    const ALLOWED_FILE_TYPES = ['image/png', 'image/jpeg', 'image/webp'];

    const avatarModifyButton = document.getElementById(`cell_${index}`);

    avatarModifyButton.addEventListener('change',
        async (event) => {
            const file = event.target.files[0];  // 获取上传的第一个文件？
            const statusElement = document.getElementById('upload-status');

            if (!file) { return; }

            // 检查文件格式
            if (!ALLOWED_FILE_TYPES.includes(file.type)) {
                statusElement.textContent = '仅支持 PNG/JPED/WEBP 格式的图片。';
                statusElement.style.color = '#F47067';

                return;
            }

            // 检查文件大小
            if (file.size >= MAX_FILE_SIZE) {
                statusElement.textContent = `文件大小应小于 ${MAX_FILE_SIZE / 1024 / 1024} MB!`;
                statusElement.style.color = '#F47067';

                return;
            }

            try {
                statusElement.textContent = '头像上传中';
                statusElement.style.color = 'inherit';

                const fileByteArray = new Uint8Array(await file.arrayBuffer());

                const csrfToken  = document.querySelector('meta[name="_csrf"]').content;
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

                // 发送请求
                const response
                    = await fetch(`/api/admin/set_user_overview_avatar/${userName}`,
                        {
                            method: 'POST',
                            headers:
                            {
                                'Content-Type': 'application/octet-stream',
                                [csrfHeader]: csrfToken
                            },
                            body: fileByteArray
                        }
                    );

                if (!response.ok) {
                    throw new Error(await response.text());
                }

                statusElement.textContent = '头像更新成功！';
                statusElement.style.color = '#57ab5a';

                // 1.5 秒后清除状态
                setTimeout(
                    () => { statusElement.textContent = ''; }, 1500
                );
            }
            catch (error) {
                console.error('Upload failed:', error);
                statusElement.textContent = `上传失败: ${error.message}`;
                statusElement.style.color = '#f47067';
            }
        }
    );
}

const OLDUSER_DATA = 
{
    "oldUserName": null,
    "oldPassword": null,
    "oldFullName": null,
    "oldTelephoneNumber": null,
    "oldEmail": null,
    "oldRoles": null
};

async function doModify(button) 
{
    button.addEventListener(
        "click",
        async function () {
            // 向上查找离 button 最近的父级 tr 标签
            const tableRowElement = button.closest('tr');

            // 再从该 tr 标签开始向下查找所有的子级 th 和 td 标签，返回一个 NodeList
            const cells = tableRowElement.querySelectorAll('td');

            let count = parseInt(this.dataset.clockCount) || 0;
            ++count;
            this.dataset.clockCount = count;

            switch (count) {
                case 1:
                    const oldCellTexts = Array.from(cells).map((cell) => cell.textContent);

                    OLDUSER_DATA.oldUserName = oldCellTexts[0];
                    OLDUSER_DATA.oldPassword = null;
                    OLDUSER_DATA.oldFullName = oldCellTexts[3];
                    OLDUSER_DATA.oldTelephoneNumber = oldCellTexts[4];
                    OLDUSER_DATA.oldEmail = oldCellTexts[5];
                    OLDUSER_DATA.oldRoles = oldCellTexts[7];

                    cells.forEach((cell, index) => {
                        switch (index) {
                            case 0: // 变换成用户名输入框
                                cell.innerHTML
                                    = `<input type="text" id="cell_${index}" placeholder=${cell.textContent}></input>`;
                                break;

                            case 1:
                                cell.innerHTML  // 变换成头像修改的操作按钮
                                    = `<input type="file" id="cell_${index}"
                                              accept="image/png, image/jpeg, image/webp"
                                              onclick="doAvatarModify('${OLDUSER_DATA.oldUserName}', ${index})" hidden>
                                       <button class="gh-button" type="button" 
                                               onclick="document.getElementById('cell_${index}').click()">
                                            <span class="button-text">📌 更换头像</span>
                                       </button>
                                       <div id="upload-status"></div>`;

                                break;

                            case 6: // 注册日期（不可修改）
                                cell.innerHTML
                                    = `<input type="text" 
                                            id="cell_${index}"
                                            placeholder="${cell.textContent} (READ-ONLY)" readonly>
                                          </input>`;
                                break;

                            case 7: // 变换成用户角色选项栏
                                cell.innerHTML
                                    = `<select id="cell_${index}">
                                                <option value="ROLE_USER">普通用户</option>
                                                <option value="ROLE_ADMIN">管理员</option>
                                                <option value="ROLE_ADMIN ROLE_USER">管理员 + 普通用户</option>
                                            </select>`;

                            case 8:
                                break;

                            default:
                                cell.innerHTML
                                    = `<input type="text" id="cell_${index}" placeholder=${cell.textContent}></input>`;
                                break;
                        }
                    });

                    button.innerText = "提交修改";

                    break;

                case 2:
                    const submitData =
                    {
                        "oldUserName": OLDUSER_DATA.oldUserName,
                        "newUserName": document.getElementById('cell_0').value || OLDUSER_DATA.oldUserName,
                        // 如果新密码没填这一字段就为空，后端会进行相应的处理
                        "newPassword": document.getElementById('cell_2').value || OLDUSER_DATA.oldPassword,
                        "newFullName": document.getElementById('cell_3').value || OLDUSER_DATA.oldFullName,
                        "newTelephoneNumber": document.getElementById('cell_4').value || OLDUSER_DATA.oldTelephoneNumber,
                        "newEmail": document.getElementById('cell_5').value || OLDUSER_DATA.oldEmail,
                        "newRoles": createUserRolesJson(document.getElementById('cell_7').value) || OLDUSER_DATA.oldRoles
                    };

                    const submitDataJson = JSON.stringify(submitData);

                    console.info(submitDataJson);

                    try {
                        // 从 meta 标签获取 CSRF Token
                        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
                        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
                        // fetch 操作....
                        const response = await fetch(
                            '/api/admin/modify_user',
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
                            const errorDetail = await response.text().catch(() => null);

                            throw new Error(
                                `Http error! statues: ${response.status}. 
                                    Detail: ${errorDetail || 'Unknown'}`
                            );
                        }
                        else {
                            const successText = await response.text().catch(() => null);
                            alert(successText);
                        }

                    }
                    catch (error) {
                        console.error(error);
                        alert(error);
                    }

                    location.reload();
                    this.dataset.clockCount = 0;

                    break;
            }
        }
    );
}

async function doDelete(button) 
{
    // 向上查找离 button 最近的父级 tr 标签
    const tableRowElement = button.closest('tr');

    const cells = tableRowElement.querySelectorAll('td');

    let deleteUserName;

    cells.forEach((cell, index) => {
        switch (index) {
            case 0:
                deleteUserName = cell.textContent;
                break;

            default:
                break;
        }
    });

    console.info(deleteUserName);

    if (confirm(`确定删除用户: ${deleteUserName} 吗？`)) {
        // 从 meta 标签获取 CSRF Token
        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        try {
            const response = await fetch(
                `/api/admin/delete/${deleteUserName}`,
                {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json',
                        [csrfHeader]: csrfToken
                    },
                    body: deleteUserName
                }
            );

            if (!response.ok) {
                const errorDetail = await response.text().catch(() => null);

                throw new Error(
                    `Http error! statues: ${response.status}.
                                    Detail: ${errorDetail || 'Unknown'}`
                );
            }
            else {
                const successDetail = await response.text().catch(() => null);
                alert(successDetail);
                location.reload();
            }

        }
        catch (error) {
            console.error(error);
            alert(error);
        }
    }
}

async function doAddNewOne(button) 
{
    button.addEventListener(
        "click",
        async function () {
            const addNewUserRowElement
                = document.getElementById('add_new_user_table_row');

            let count = parseInt(this.dataset.clockCount) || 0;
            ++count;
            this.dataset.clockCount = count;

            switch (count) {
                case 1:
                    addNewUserRowElement.innerHTML
                        = `
                                <td data-label="ID">
                                    <input type="text" placeholder="用户 ID（自动生成）" readonly></input>
                                </td>

                                <td data-label="用户名">
                                    <input type="text" id="new_username" placeholder="新用户名"></input>
                                </td>

                                <td data-label="头像">
                                    <img src="/api/admin/get_default_avatar" alt="默认头像" 
                                    style="width: 25px; height: 25px; border-radius: 50%;"></img>
                                </td>

                                <td data-label="密码">
                                    <input type="password" id="new_password" placeholder="密码"></input>
                                </td>

                                <td data-label="全名">
                                    <input type="text" id="new_fullname" placeholder="用户全名"></input>
                                </td>

                                <td data-label="电话号码">
                                    <input type="tel" id="new_telephone" placeholder="电话号码（+86）"></input>
                                </td>

                                <td data-label="邮箱">
                                    <input type="email" id="new_email" placeholder="邮箱"></input>
                                </td>
                                
                                <td data-label="注册日期">
                                    <input type="text" id="new_register_date" placeholder="注册时间（自动生成）" readonly></input>
                                </td>

                                <td data-label="角色"> 
                                    <select id="new_roles">
                                        <option value="ROLE_USER">普通用户</option>
                                        <option value="ROLE_ADMIN">管理员</option>
                                        <option value="ROLE_ADMIN ROLE_USER">管理员 + 普通用户</option>
                                    </select>
                                </td>
                            `;

                    button.innerText = '➕ 创建新用户';

                    break;

                case 2:
                    const submitData = {
                        userName: document.getElementById('new_username').value,
                        password: document.getElementById('new_password').value,
                        fullName: document.getElementById('new_fullname').value,
                        telephoneNumber: document.getElementById('new_telephone').value,
                        email: document.getElementById('new_email').value,
                        roles: createUserRolesJson(document.getElementById('new_roles').value)
                    };

                    const submitDataJson = JSON.stringify(submitData);

                    console.info(submitDataJson);

                    try {
                        // 从 meta 标签获取 CSRF Token
                        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
                        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

                        const response = await fetch(
                            '/api/admin/add_new_user',
                            {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/json',
                                    [csrfHeader]: csrfToken
                                },
                                body: submitDataJson
                            }
                        );

                        if (!response.ok) {
                            const errorDetail = await response.text().catch(() => null);

                            throw new Error(
                                `Http error! statues: ${response.status}. 
                                    Detail: ${errorDetail || 'Unknown'}`
                            );
                        }
                        else {
                            const successDetail = await response.text().catch(() => null);
                            alert(successDetail);
                            location.reload();
                            this.dataset.clockCount = 0;
                        }
                    }
                    catch (error) {
                        console.error(error);
                        alert(error)
                    }
                    break;

                default:
                    break;
            }
        }
    );
}

async function doDeleteAll(button) 
{
    button.innerText = '处理中...';
    button.disabled = true;

    try {
        if (confirm('这是一个敏感操作，确定要删除所有用户吗？')) {
            // 从 meta 标签获取 CSRF Token
            const csrfToken = document.querySelector('meta[name="_csrf"]').content;
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

            const response = await fetch(
                '/api/admin/delete/all',
                {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json',
                        [csrfHeader]: csrfToken
                    }
                }
            );

            if (!response.ok) {
                throw new Error(
                    `Http error: ${response.status}, 
                        Detail: ${await response.text().catch(() => null) || 'Unknow'}`
                );
            }
            else {
                alert(`${await response.text().catch(() => null)}`);
            }
        }
    }
    catch (error) {
        alert(error);
        console.error(error);
    }
    finally {
        button.innerText = 'ALL 🗑️';
        button.disabled = false;
        location.reload();
    }
}

async function doDeleteByRange(button) 
{
    const beginInput = document.getElementById('delete_begin_id');
    const endInput = document.getElementById('delete_end_id');

    if (beginInput.value <= 0 || endInput.value <= 0) {
        alert('id 范围不得小于 0，请重新输入。');
        beginInput.value = '';
        endInput.value = '';

        return;
    }

    button.innerText = '处理中...';
    button.disabled = true;

    try {
        // 从 meta 标签获取 CSRF Token
        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        const response = await fetch(
            `/api/admin/delete/range/${beginInput.value}_${endInput.value}`,
            {
                method: 'DELETE',
                headers: {
                    [csrfHeader]: csrfToken
                }
            }
        );

        if (!response.ok) {
            throw new Error(
                `Http error: ${response.status}, 
                    Detail: ${await response.text().catch(() => null) || 'Unknow'}`
            );
        }
        else {
            alert(`${await response.text().catch(() => null)}`);
        }
    }
    catch (error) {
        alert(error);
        console.error(error);
    }
    finally {
        button.innerText = '（Range 🗑️）批量删除';
        button.disabled = true;
        location.reload();
    }
}