getScoreSattlement();

async function getScoreSattlement() 
{
    try 
    {
        const response = await fetch(
            `/api/score_record/score_settlement/
            ${document.getElementById('user_name_text').textContent.trim()}`
        );

        if (!response.ok) 
        {
            // 获取响应体中携带的错误信息
            const errorDetail = await response.text().catch(() => null);

            throw new Error(
                `${errorDetail || 'Unknown'}`
            );
        }

        const responseJson
            = response.json()
                .then((data) => {
                    // 从 responseJson 中提取四个键值对存于对象中。
                    const { correctCount, errorCount, noAnswerCount, mistakeRate } = data;

                    document.getElementById('correct_count').innerText          = `${correctCount} 题`;
                    document.getElementById('error_count').innerText            = `${errorCount} 题`;
                    document.getElementById('no_answer_count').innerText        = `${noAnswerCount} 题`;
                    document.getElementById('mistake_rate').innerText           = `${mistakeRate} %`;
                    document.getElementById('combinded_mistake_rate').innerText = `${mistakeRate} %`;
                }).catch((error) => { console.error(error) });
    }
    catch (error) 
    {
        const errorMessage = document.getElementById('error_message');

        errorMessage.classList.add('show');
        errorMessage.innerHTML = `<span>${error.message}</span>`;

        setTimeout(() => {
            document.getElementById('error_message').classList.remove('show');
        }, 3500);

        console.error(error);
    }
}