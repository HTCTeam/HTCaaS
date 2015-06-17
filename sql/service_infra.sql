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
-- Table structure for table `service_infra`
--

DROP TABLE IF EXISTS `service_infra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `service_infra` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '',
  `service_Code_id` int(11) unsigned DEFAULT NULL,
  `priority` smallint(5) unsigned DEFAULT NULL COMMENT '서비스 인프라 우선순위, 큰 값이 높은 우선순위를 가짐',
  `available` bit(1) NOT NULL DEFAULT b'0',
  `runningAgentHP` smallint(5) unsigned NOT NULL DEFAULT '5',
  `submittedAgentHP` smallint(5) unsigned NOT NULL DEFAULT '5',
  `newAgentHP` smallint(5) unsigned NOT NULL DEFAULT '5',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `service_Code_id` (`service_Code_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='서비스 인프라 관리 테이블';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_infra`
--

LOCK TABLES `service_infra` WRITE;
/*!40000 ALTER TABLE `service_infra` DISABLE KEYS */;
INSERT INTO `service_infra` VALUES (1,'local',1,2,'',5,5,5),(2,'vo.france-asia.org',2,5,'',15,60,20),(3,'biomed',2,4,'',15,60,20),(4,'PLSI',3,7,'',6,6,6),(5,'4TH',3,6,'\0',6,6,6),(6,'Amazon EC2',4,3,'\0',5,5,5),(7,'pbs',5,7,'',5,5,5),(8,'condor',3,8,'',5,5,5),(9,'Openstack',4,2,'',5,5,5);
/*!40000 ALTER TABLE `service_infra` ENABLE KEYS */;
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
