-- 查询每个选择题的 id、题目、正确答案，答对次数及其所有的选项，
-- 选项和内容通过 JSON_OBJECTAGG() 整合到一个 JSON 之中
SELECT
    questions.id             AS QuestionID,
    questions.content        AS QuestionContent,
    questions.answer         AS CorrectAnswer,
    questions.correct_times  AS CorrectTimes,
    JSON_OBJECTAGG(options.option_key, options.content) AS OptionsJSON
FROM questions
INNER JOIN options ON questions.id = options.question_id
GROUP BY questions.id;

-- 查询所有题目的 id、正确选项、答案内容。
SELECT
    questions.id        AS QuestionID,
    questions.content   AS QuestionContent,
    questions.answer    AS CorrectAnswer,
    options.content     AS CorrectOptionContent
FROM questions
INNER JOIN options ON options.question_id = questions.answer
GROUP BY questions.id;