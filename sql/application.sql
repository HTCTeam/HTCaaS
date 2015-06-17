-- MySQL dump 10.13  Distrib 5.1.73, for redhat-linux-gnu (x86_64)
--
-- Host: localhost    Database: htcaas_server
-- ------------------------------------------------------
-- Server version	5.1.73

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
-- Table structure for table `application`
--

DROP TABLE IF EXISTS `application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `application` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '응용 소프트웨어 번호',
  `name` varchar(255) NOT NULL DEFAULT '' COMMENT '응용 소프트웨어 명',
  `comment` varchar(2000) DEFAULT NULL COMMENT '비고',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '우선순위',
  `useyn` tinyint(1) NOT NULL DEFAULT '1' COMMENT '사용유무',
  `fdate` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '등록일',
  `fuserid` varchar(255) NOT NULL COMMENT '등록자',
  `ldate` datetime NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '최종수정일',
  `luserid` varchar(255) NOT NULL DEFAULT '' COMMENT '최종수정자',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='어플리케이션';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `application`
--

LOCK TABLES `application` WRITE;
/*!40000 ALTER TABLE `application` DISABLE KEYS */;
INSERT INTO `application` VALUES (1,'general',NULL,0,1,'2013-08-30 16:07:35','0','2013-08-30 16:07:35','0'),(2,'optimization',NULL,0,1,'2013-08-30 16:07:52','0','2013-08-30 16:07:52','0'),(3,'autodock3',NULL,0,1,'2013-08-30 16:08:03','0','2013-08-30 16:08:03','0'),(4,'madgraph','',0,1,'2013-08-30 16:08:13','0','2013-10-14 11:50:51','19'),(5,'threekaonomega',NULL,0,1,'2013-08-30 16:08:27','0','2013-08-30 16:08:27','0'),(6,'pythia',NULL,0,1,'2013-09-02 16:08:11','0','2013-09-02 16:08:11','0'),(7,'autodock_vina',NULL,0,1,'2013-09-30 16:07:35','18','2013-09-30 16:07:35','18'),(8,'BioKnowledge Viewer',NULL,0,1,'2013-10-24 16:07:35','18','2013-10-24 16:07:35','18'),(9,'mg5_amc','',0,1,'2014-07-14 18:20:41','38','2014-07-14 18:20:41','38');
/*!40000 ALTER TABLE `application` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-04-16 12:02:14
