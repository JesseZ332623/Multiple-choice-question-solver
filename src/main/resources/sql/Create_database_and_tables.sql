USE exam_question;

CREATE TABLE questions (
	id 		 INT  PRIMARY KEY AUTO_INCREMENT,
    content  TEXT NOT NULL,
    answer   ENUM('A', 'B', 'C', 'D') NOT NULL,
    correct_times INT NOT NULL DEFAULT 0
) ENGINE=InnoDB;

CREATE TABLE options(
	question_id INT,
    option_key  ENUM('A', 'B', 'C', 'D'),
    content     TEXT NOT NULL,
    PRIMARY KEY (question_id, option_key),
    FOREIGN KEY (question_id) REFERENCES questions(id)
) ENGINE=InnoDB;

CREATE TABLE score_record (
  submit_date     DATETIME NOT NULL,
  correct_count   INT      NOT NULL,
  error_count     INT      NOT NULL,
  no_answer_count INT      NOT NULL,
  mistake_rate    DOUBLE   NOT NULL,
  PRIMARY KEY (submit_date)
) ENGINE=InnoDB;
