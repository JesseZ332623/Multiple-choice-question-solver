var TOTAL_QUESTION_AMOUNT = Number(document.getElementById("total_questions").textContent.split('：')[1]);
var TotalCorrect = 0;
var TotalMistake = 0;

function checkAnswer(button) 
{
    // 通过按钮元素向上查找父容器
    const container = button.closest('.question-container');

    // 获取用户选择的单选值
    const selected = container.querySelector('input[type="radio"]:checked')?.value;

    // 获取正确答案（假设answer字段存储的是单个字母如"A"）
    const correctAnswer = container.dataset.correctAnswer;

    // 获取反馈显示区域
    const feedback = container.querySelector('.feedback');

    if (!selected) {
        feedback.innerHTML = '<span class="incorrect">请先选择答案！</span>';
        return;
    }

    // 验证答案（不区分大小写）
    const isCorrect = selected.toUpperCase() === correctAnswer.toUpperCase();

    if (isCorrect) 
    {
        updateCorrectTimes(container.dataset.questionId);

        feedback.innerHTML = '<span class="correct">✅ 回答正确！</span>';

        container.querySelectorAll('input[type="radio"]').forEach(radio => {
            radio.disabled = true;
        });

        button.disabled = true;

        ++TotalCorrect;
    }
    else {
        feedback.innerHTML = `<span class="incorrect">❌ 回答错误！正确答案：${correctAnswer}</span>`;

        container.querySelectorAll('input[type="radio"]').forEach(radio => {
            radio.disabled = true;
        });

        button.disabled = true;
        ++TotalMistake;
    }
}

function getCurrentISOTimeStrWithoutZone() {
    return new Date().toISOString().split('.')[0].replace('Z', ' ');
}

function totalSubmit(button) {
    var noAnswer = TOTAL_QUESTION_AMOUNT - (TotalCorrect + TotalMistake);
    var mistakeRate = (TotalMistake + noAnswer) / TOTAL_QUESTION_AMOUNT * 100;

    const practiseScoreDOM = document.getElementById('practise_score');

    const recordJson =
    {
        "userName": document.getElementById('user_name_text').textContent.trim(),
        "submitDate": getCurrentISOTimeStrWithoutZone(),
        "correctCount": TotalCorrect,
        "errorCount": TotalMistake,
        "noAnswerCount": noAnswer,
        "mistakeRate": mistakeRate.toFixed(2)
    };

    addNewScoreRecord(recordJson, button);      // 将成绩数据写入数据表

    setTimeout(
        () => {
            button.disabled = false;
            window.location = '../score_record/current_score_settlement';
        }, 2500
    );
}

async function updateCorrectTimes(questionId) {
    try {
        // 从 meta 标签获取 CSRF Token
        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        const questionPosData = {
            "userName": document.getElementById('user_name_text').textContent.trim(),
            "questionId": questionId
        };

        const questionPosDataJson = JSON.stringify(questionPosData);

        console.info(questionPosDataJson);

        const response = await fetch(
            `/api/redis/correct_times_plus_one`,
            {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: questionPosDataJson
            }
        );

        if (!response.ok) {
            throw new Error(
                `Http error! statues: ${response.status}
                    Error Detail: ${await response.text().catch(() => null)}`
            );
        }

        console.log(
            `Fetch api/correct_times_plus_one/${questionId} success, 
				response: ${await response.text().catch(() => null)}`
        );
    }
    catch (error) {
        alert(error);
        console.error(error);
    }
}

async function addNewScoreRecord(recordJson, button) {
    try {
        // 从 meta 标签获取 CSRF Token
        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        const response = await fetch(
            "/api/score_record/add_one_new_score_record",
            {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify(recordJson)
            }
        );

        if (!response.ok) {
            const errorDetail = await response.text().catch(() => null);
            throw new Error(
                `Http error! statues: ${response.status}. Detail: ${errorDetail?.message || 'Unknown'}`
            );
        }

        const responseText = await response.text().catch(() => null);

        const practiseScoreDOM = document.getElementById('practise_score');

        practiseScoreDOM.innerHTML
            = `<span>✅ [${getCurrentISOTimeStrWithoutZone()}]: 成绩数据已提交，正在跳转至结算页面。<span>`;

        button.disabled = true;

        console.log(`${responseText}`);
    }
    catch (error) {
        console.error(error);
    }
}