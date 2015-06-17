-- MySQL dump 10.13  Distrib 5.5.9, for Linux (x86_64)
--
-- Host: localhost    Database: htcaas_portal
-- ------------------------------------------------------
-- Server version	5.5.9-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `attach`
--

DROP TABLE IF EXISTS `attach`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `attach` (
  `ATC_NO` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '번호',
  `BOARD_TYPE` bigint(20) unsigned NOT NULL COMMENT '게시판 타입',
  `BOARD_NO` bigint(20) unsigned NOT NULL COMMENT '게시판 번호',
  `REALFILENAME` varchar(255) NOT NULL COMMENT '실제파일이름',
  `FILENAME` varchar(255) NOT NULL COMMENT '임의파일이름',
  `PATH` varchar(2000) NOT NULL COMMENT '파일경로',
  `FDATE` datetime NOT NULL COMMENT '등록일',
  `FUSERID` bigint(20) NOT NULL COMMENT '등록자',
  PRIMARY KEY (`ATC_NO`),
  KEY `fk_ntc_no` (`BOARD_NO`)
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8 COMMENT='첨부파일';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `attach`
--

LOCK TABLES `attach` WRITE;
/*!40000 ALTER TABLE `attach` DISABLE KEYS */;
INSERT INTO `attach` VALUES (9,1,7,'관리자페이지 회의 내용.hwp','bX2mRX73Amozl4J.hwp','2013/09/','2013-09-10 18:43:50',18),(10,1,7,'사용자페이지 회의 내용.hwp','Erawck1TUeSPSyA.hwp','2013/09/','2013-09-10 18:43:50',18),(22,3,3,'Tulips.jpg','9C7B7Ng0RxbIVD3.jpg','2013/09/','2013-09-12 09:54:01',18),(23,3,1,'Hydrangeas.jpg','3zRFJo3Q9D8bwCT.jpg','2013/09/','2013-09-12 09:54:12',18),(38,1,8,'Tulips.jpg','MNe5hwZFT5JGWWb.jpg','2013/11/','2013-11-04 16:52:19',18),(39,1,8,'Hydrangeas.jpg','OCgYfI7eaNjdJbW.jpg','2013/11/','2013-11-04 16:52:19',18),(40,1,8,'Jellyfish.jpg','87CGKNbMuImQSDs.jpg','2013/11/','2013-11-04 16:52:19',18),(41,1,9,'Chrysanthemum.jpg','AmHyAJU8Ht2lmCl.jpg','2013/11/','2013-11-04 16:52:38',18),(42,1,9,'Desert.jpg','ysZxeGc3vd12Ks8.jpg','2013/11/','2013-11-04 16:52:38',18),(43,1,9,'Hydrangeas.jpg','1UNAaOyeV6yoAvA.jpg','2013/11/','2013-11-04 16:52:38',18),(48,3,5,'Penguins.jpg','LmH5KpCbzJLmvOD.jpg','2013/11/','2013-11-04 19:30:27',18),(49,1,6,'HTCaaS강의1.pdf','Cb5ZK3XjKxjZBO1.pdf','2013/11/','2013-11-20 14:04:44',18);
/*!40000 ALTER TABLE `attach` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-06-19 17:27:32
