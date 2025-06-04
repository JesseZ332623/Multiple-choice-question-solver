/**
 * 清空某用户的所有答题成绩记录。 
 * 
 * @param {HTMLElement} button 触发这个动作的按钮元素
*/
async function truncateRecord(button)
{
    try 
    {
        // 从 meta 标签获取 CSRF Token
        const csrfToken  = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
        var userName     = document.getElementById('user_name_text').textContent;

        button.classList.add('loading');

        if (confirm('这是一个敏感操作，您确定要删除所有练习记录吗？'))
        {
            const response = await fetch(
                `/api/score_record/delete_by_username/${userName}`,
                {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json',
                        [csrfHeader]: csrfToken
                    }
                }
            );

            if (!response.ok) 
            {
                const errorDetail = await response.text().catch(() => null);

                throw new Error(`
                        Http error! status: ${response.status}.
                        Detail: ${errorDetail || 'Unknow'}`
                );
            }
            else 
            {
                alert(
                    await response.text().catch(() => null)
                );

                location.reload();
            }
        }
    }
    catch (error)
    {
        alert(error);
        console.error(error);
    }
    finally { button.classList.remove('loading'); }
}