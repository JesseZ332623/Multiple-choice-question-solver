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

        const responseJson = await response.json();

        document.getElementById('correct_count').innerText 
                                    = `${responseJson.correctCount} 题`;
        document.getElementById('error_count').innerText
                                    = `${responseJson.errorCount} 题`;
        document.getElementById('no_answer_count').innerText
                                    = `${responseJson.noAnswerCount} 题`;
        document.getElementById('mistake_rate').innerText           
                                    = `${responseJson.mistakeRate} %`;
        document.getElementById('combinded_mistake_rate').innerText
                                    = `${responseJson.mistakeRate} %`;
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