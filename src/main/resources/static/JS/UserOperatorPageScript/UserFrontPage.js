async function doLogout() 
{
    try 
    {
        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        const response = await fetch('/api/user_info/logout', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            credentials: 'include'
        });

        if (!response.ok) {
            const errorText = await response.text().catch(() => "请求失败");
            throw new Error(`错误: ${errorText}`);
        }

        alert(await response.text().catch(() => "UNKOWN"));

        window.location.href = "/user_info/login";
    } catch (error) 
    {
        alert(error.message);
        console.error("退出登录错误:", error);
    }
}