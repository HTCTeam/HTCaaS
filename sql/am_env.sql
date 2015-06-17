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
-- Table structure for table `am_env`
--

DROP TABLE IF EXISTS `am_env`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `am_env` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '',
  `auto` bit(1) NOT NULL DEFAULT b'0',
  `service_Infra_id` set('1','2','3','4','5','6','7','8','9','10') NOT NULL DEFAULT '',
  `agentScalingMetric_id` tinyint(2) unsigned NOT NULL DEFAULT '5',
  `ceSelectionMetric_id` tinyint(2) unsigned NOT NULL DEFAULT '1',
  `addAgentNO` int(5) unsigned NOT NULL DEFAULT '0',
  `statusMonitoringHP` tinyint(2) unsigned NOT NULL DEFAULT '1',
  `thresholdMaxAgent` tinyint(2) unsigned NOT NULL DEFAULT '100',
  `thresholdMinAgent` tinyint(2) unsigned NOT NULL DEFAULT '10',
  `numAgentRunning` int(5) unsigned NOT NULL DEFAULT '1',
  `numAgentSubmitFailure` int(5) unsigned NOT NULL DEFAULT '0',
  `minAgentNO` int(5) unsigned NOT NULL DEFAULT '0',
  `resourceAP` smallint(5) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `agentScalingMetric_id` (`agentScalingMetric_id`),
  KEY `serviceInfraMetric_id` (`service_Infra_id`),
  KEY `CESelectionMetric_id` (`ceSelectionMetric_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='Agent Manager 관리 설정 테이블';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `am_env`
--

LOCK TABLES `am_env` WRITE;
/*!40000 ALTER TABLE `am_env` DISABLE KEYS */;
/*!40000 ALTER TABLE `am_env` ENABLE KEYS */;
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
