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
        let   userName   = document.getElementById('user_name_text').textContent;

        button.classList.add('loading');

        if (confirm('这是一个敏感操作，您确定要删除您的所有练习记录吗？'))
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

var LAST_PAGE = Number.parseInt(
                document.getElementById('page_count').textContent
            );

function validatePage(page)
{
    if (Number.isNaN(page)) 
    {
        alert('不是一个有效的页码！');
        return false;
    }

    if (page > LAST_PAGE) {
        alert(`最多 ${LAST_PAGE} 页，不能再多了！`);
        return false;
    }

    if (page <= 0) {
        alert('有比 1 还小的页码吗？');
        return false;
    }

    return true;
}

// 跳转至指定的页
function toDesignatedPage(page)
{
    if (validatePage(page)) 
    {
        window.location.href 
            = `/score_record/paginated_score_record?page=${page}`;
    }
}

function toNextPage()
{
    let nextPage
            = Number.parseInt(
                document.getElementById('current_page').textContent
            ) + 1;
    
    toDesignatedPage(nextPage);
}

function toPreviousPage()
{
    let previousPage
            = Number.parseInt(
                document.getElementById('current_page').textContent
            ) - 1;

    toDesignatedPage(previousPage);
}

function toFirstPage() {
    toDesignatedPage(1);
}

function toLastPage()
{
    let lastPage = Number.parseInt(
                document.getElementById('page_count').textContent
            );

    toDesignatedPage(lastPage);
}

function jumpToDesignatedPage() 
{
    toDesignatedPage(
        Number.parseInt(
            document.getElementById('jump_input').value
        )
    );
}