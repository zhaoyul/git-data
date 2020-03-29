CREATE TABLE `demo_user` (
  `id` varchar(40) NOT NULL,
  `first_name` varchar(30) DEFAULT NULL,
  `last_name` varchar(30) DEFAULT NULL,
  `email` varchar(30) DEFAULT NULL,
  `admin` tinyint(1) DEFAULT NULL,
  `last_login` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_active` tinyint(1) DEFAULT NULL,
  `pass` varchar(300) DEFAULT NULL,
  `remark` json DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
--;;
INSERT INTO `demo_user` (`id`, `first_name`, `last_name`, `email`, `admin`, `last_login`, `is_active`, `pass`) VALUES
	('0', 'demo', 'hc', 'demo@redcreation.net', NULL, '2019-09-12 15:19:34', NULL, NULL),
	('1', 'dirk', 'sun', 'dirk@redcreation.net', 1, '2019-09-12 15:19:53', 1, '123456'),
	('2', 'marvin', 'ma', 'marvin@redcreation.net', 1, '2019-09-12 15:19:50', 1, '123456'),
	('3', 'tom', 'yu', 'tom@redcreation.net', 1, '2019-09-12 15:19:48', 1, '123456'),
	('4', 'tony', 'yang', 'tony@redcreation.net', 1, '2019-09-12 15:19:45', 1, '123456');
