CREATE TABLE `users` (
  `user_id` 		  BIGINT 	  NOT NULL AUTO_INCREMENT,
  `user_name` 		  VARCHAR(255) NOT NULL,
  `password` 		  VARCHAR(128) NOT NULL,
  `full_name` 		  VARCHAR(128) NOT NULL,
  `telephone_number`  VARCHAR(16)  NOT NULL,
  `email` 			  VARCHAR(64)  NOT NULL,
  `register_datetime` DATETIME     NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `user_id_UNIQUE` (`user_id`),
  UNIQUE KEY `user_name_UNIQUE` (`user_name`),
  UNIQUE KEY `full_name_UNIQUE` (`full_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user_roles` (
  `id` 			BIGINT 		NOT NULL,
  `role_name` 	VARCHAR(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user_role_relation` (
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `fk_role` (`role_id`),
  CONSTRAINT `fk_role` FOREIGN KEY (`role_id`) REFERENCES `user_roles` (`id`),
  CONSTRAINT `fk_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


  


