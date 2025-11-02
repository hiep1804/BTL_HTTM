CREATE DATABASE  IF NOT EXISTS `game_othello` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `game_othello`;
-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: game_othello
-- ------------------------------------------------------
-- Server version	8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `game`
--

DROP TABLE IF EXISTS `game`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `game` (
  `id` int NOT NULL AUTO_INCREMENT,
  `end_time` datetime(6) DEFAULT NULL,
  `score_black` int NOT NULL,
  `score_white` int NOT NULL,
  `start_time` datetime(6) NOT NULL,
  `player_black_id` int NOT NULL,
  `player_white_id` int NOT NULL,
  `player_winner_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKj2xy74duvst9wn9gtxufp8viq` (`player_winner_id`),
  KEY `FKaeyi3olruucgr4iwvcv2hbrho` (`player_white_id`),
  KEY `FKlsxaoyculx2lxod44lsrbxppe` (`player_black_id`),
  CONSTRAINT `FKaeyi3olruucgr4iwvcv2hbrho` FOREIGN KEY (`player_white_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKj2xy74duvst9wn9gtxufp8viq` FOREIGN KEY (`player_winner_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKlsxaoyculx2lxod44lsrbxppe` FOREIGN KEY (`player_black_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `game`
--

LOCK TABLES `game` WRITE;
/*!40000 ALTER TABLE `game` DISABLE KEYS */;
INSERT INTO `game` VALUES (1,NULL,0,0,'2025-10-25 08:22:14.558560',2,1,NULL),(2,NULL,0,0,'2025-10-25 09:00:04.426148',1,2,NULL),(3,NULL,0,0,'2025-10-27 19:00:45.869176',1,2,NULL),(4,NULL,0,0,'2025-10-27 19:05:27.906543',2,1,NULL),(5,NULL,0,0,'2025-11-02 16:12:47.666120',1,2,NULL),(6,NULL,0,0,'2025-11-02 16:16:18.493904',1,2,NULL),(7,'2025-11-02 20:51:37.807388',0,0,'2025-11-02 20:51:29.305587',1,2,1),(8,'2025-11-02 21:02:37.066418',0,0,'2025-11-02 21:02:32.593472',1,2,1),(9,'2025-11-02 21:03:32.775804',0,0,'2025-11-02 21:03:28.069473',1,2,1),(10,'2025-11-02 21:14:05.155569',0,0,'2025-11-02 21:13:41.444739',2,1,1),(11,'2025-11-02 21:15:19.951732',0,0,'2025-11-02 21:15:14.929840',1,2,1),(12,'2025-11-02 21:16:28.510087',0,0,'2025-11-02 21:16:21.041611',2,1,2);
/*!40000 ALTER TABLE `game` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `move`
--

DROP TABLE IF EXISTS `move`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `move` (
  `id` int NOT NULL AUTO_INCREMENT,
  `col` int NOT NULL,
  `row_index` int NOT NULL,
  `game_id` int NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6khbbqe4c9jmywgvi9nehpkd1` (`game_id`),
  KEY `FKd35qi0icfit7h232153myuym5` (`user_id`),
  CONSTRAINT `FK6khbbqe4c9jmywgvi9nehpkd1` FOREIGN KEY (`game_id`) REFERENCES `game` (`id`),
  CONSTRAINT `FKd35qi0icfit7h232153myuym5` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `move`
--

LOCK TABLES `move` WRITE;
/*!40000 ALTER TABLE `move` DISABLE KEYS */;
INSERT INTO `move` VALUES (1,3,2,2,1),(2,2,2,2,2),(3,2,3,2,1),(4,2,4,2,2),(5,1,5,2,1),(6,3,2,3,1),(7,2,2,3,2),(8,2,3,3,1),(9,2,4,3,2),(10,3,2,5,1),(11,3,2,6,1),(12,2,2,6,2),(13,5,4,6,1),(14,3,5,6,2),(15,3,2,10,2),(16,2,2,10,1),(17,3,2,12,2),(18,2,2,12,1);
/*!40000 ALTER TABLE `move` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `elo_rating` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email_UNIQUE` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'alice','passAlice123','alice@example.com',4),(2,'bob','passBob456','bob@example.com',2),(3,'charlie','passCharlie789','charlie@example.com',0),(4,'david','passDavid321','david@example.com',0),(5,'eve','passEve654','eve@example.com',0);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-02 23:19:22
