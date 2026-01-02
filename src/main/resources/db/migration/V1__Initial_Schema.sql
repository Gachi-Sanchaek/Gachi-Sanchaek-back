SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `organization`;
CREATE TABLE `organization` (
                                `latitude` double NOT NULL,
                                `longitude` double NOT NULL,
                                `created_at` datetime(6) NOT NULL,
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `kakao_place_id` bigint NOT NULL,
                                `address` varchar(255) NOT NULL,
                                `name` varchar(255) NOT NULL,
                                `phone` varchar(255) DEFAULT NULL,
                                `qr_code_payload` varchar(255) DEFAULT NULL,
                                `category` enum('SHELTER','WELFARE') NOT NULL,
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `UKd6ptq07s0tiw3poly2la862g8` (`kakao_place_id`),
                                UNIQUE KEY `UK19c8owyadhdm08xfshax9kf6j` (`qr_code_payload`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `point_logs`;
CREATE TABLE `point_logs` (
                              `amount` bigint NOT NULL,
                              `created_at` datetime(6) NOT NULL,
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `user_id` bigint DEFAULT NULL,
                              `location` varchar(255) DEFAULT NULL,
                              `type` enum('DOG','NORMAL','PLOGGING','SENIOR') NOT NULL,
                              PRIMARY KEY (`id`),
                              KEY `FK15n4gica2qwsebf21gp07vw9n` (`user_id`),
                              CONSTRAINT `FK15n4gica2qwsebf21gp07vw9n` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100331 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `rankings`;
CREATE TABLE `rankings` (
                            `rank_period` int NOT NULL,
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `point` bigint NOT NULL,
                            `updated_at` datetime(6) NOT NULL,
                            `user_id` bigint DEFAULT NULL,
                            PRIMARY KEY (`id`),
                            KEY `FKcup4ei1jmensgunlbncpb5rnv` (`user_id`),
                            KEY `idx_ranking_period_point` (`rank_period`,`point` DESC),
                            KEY `idx_rankings_covering` (`rank_period`,`point` DESC,`updated_at`,`user_id`),
                            CONSTRAINT `FKcup4ei1jmensgunlbncpb5rnv` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100331 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `stamps`;
CREATE TABLE `stamps` (
                          `created_at` datetime(6) NOT NULL,
                          `id` bigint NOT NULL,
                          `price` bigint NOT NULL,
                          `image_url` varchar(255) NOT NULL,
                          `name` varchar(255) NOT NULL,
                          PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
                         `is_deleted` bit(1) NOT NULL,
                         `created_at` datetime(6) DEFAULT NULL,
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `kakao_id` bigint NOT NULL,
                         `total_points` bigint NOT NULL,
                         `walking_count` bigint NOT NULL,
                         `gender` varchar(255) NOT NULL,
                         `nickname` varchar(255) NOT NULL,
                         `profile_image_url` varchar(255) NOT NULL,
                         `role` varchar(255) NOT NULL,
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100332 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `walk_location`;
CREATE TABLE `walk_location` (
                                 `latitude` double DEFAULT NULL,
                                 `longitude` double DEFAULT NULL,
                                 `id` bigint NOT NULL,
                                 `walk_id` bigint DEFAULT NULL,
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `walk_location_seq`;
CREATE TABLE `walk_location_seq` (
                                     `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `walk_recommendation`;
CREATE TABLE `walk_recommendation` (
                                       `planned_minutes` int NOT NULL,
                                       `created_at` datetime(6) NOT NULL,
                                       `id` bigint NOT NULL AUTO_INCREMENT,
                                       `organization_id` bigint DEFAULT NULL,
                                       `user_id` bigint NOT NULL,
                                       `description` text NOT NULL,
                                       `recommendation_group_id` varchar(255) NOT NULL,
                                       `way_points` text NOT NULL,
                                       PRIMARY KEY (`id`),
                                       KEY `FKebu5dfndv8te3xn6vjfq7mfmv` (`organization_id`),
                                       KEY `FKt5g20nh79xlkpy8nq6wab7fqq` (`user_id`),
                                       CONSTRAINT `FKebu5dfndv8te3xn6vjfq7mfmv` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`),
                                       CONSTRAINT `FKt5g20nh79xlkpy8nq6wab7fqq` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `user_organization`;
CREATE TABLE `user_organization` (
                                     `id` bigint NOT NULL AUTO_INCREMENT,
                                     `organization_id` bigint NOT NULL,
                                     `user_id` bigint NOT NULL,
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `UKkipx8j1oqh945qbaopdf0jua2` (`user_id`,`organization_id`),
                                     KEY `FKfdnaj8emi62iffmg6w6ykjxf4` (`organization_id`),
                                     CONSTRAINT `FK19kj4cb4e3gnlw2nga0a4b1xy` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
                                     CONSTRAINT `FKfdnaj8emi62iffmg6w6ykjxf4` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `walk_record`;
CREATE TABLE `walk_record` (
                               `plogging_verified` bit(1) DEFAULT NULL,
                               `point` int DEFAULT NULL,
                               `total_distance` double DEFAULT NULL,
                               `total_time` int DEFAULT NULL,
                               `created_at` datetime(6) DEFAULT NULL,
                               `end_time` datetime(6) DEFAULT NULL,
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `start_time` datetime(6) NOT NULL,
                               `user_id` bigint NOT NULL,
                               `walk_recommendation_id` bigint DEFAULT NULL,
                               `qr_token` varchar(255) DEFAULT NULL,
                               `qr_stage` enum('FIRST_SCANNED','UNVERIFIED','VERIFIED') DEFAULT NULL,
                               `status` enum('FINISHED','ONGOING','WAITING') NOT NULL,
                               `verification_method` enum('AI','NONE','QR') NOT NULL,
                               `walk_type` enum('DOG','NORMAL','PLOGGING','SENIOR') NOT NULL,
                               PRIMARY KEY (`id`),
                               KEY `FKinb15fqm7nonu6nvxas3ntjao` (`user_id`),
                               CONSTRAINT `FKinb15fqm7nonu6nvxas3ntjao` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100332 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;
