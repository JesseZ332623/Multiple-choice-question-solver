CREATE DATABASE exam_question;

USE exam_question;

-- 新建问题表
CREATE TABLE questions (
	id 		 INT  PRIMARY KEY AUTO_INCREMENT,
    content  TEXT NOT NULL,
    answer   ENUM('A', 'B', 'C', 'D') NOT NULL,
    correct_times INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 新建选项表
CREATE TABLE options(
	question_id INT,
    option_key  ENUM('A', 'B', 'C', 'D'),
    content     TEXT NOT NULL,
    PRIMARY KEY (question_id, option_key),
    FOREIGN KEY (question_id) REFERENCES questions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;;

-- 新建答题成绩记录表
CREATE TABLE score_record (
  submit_date     DATETIME NOT NULL,
  correct_count   INT      NOT NULL,
  error_count     INT      NOT NULL,
  no_answer_count INT      NOT NULL,
  mistake_rate    DOUBLE   NOT NULL,
  PRIMARY KEY (submit_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
