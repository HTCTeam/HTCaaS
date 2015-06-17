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
-- Table structure for table `server_env`
--

DROP TABLE IF EXISTS `server_env`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `server_env` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '',
  `value` varchar(255) NOT NULL DEFAULT '',
  `content` longtext NOT NULL,
  `comment` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='서버 환경 설정 테이블';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `server_env`
--

LOCK TABLES `server_env` WRITE;
/*!40000 ALTER TABLE `server_env` DISABLE KEYS */;
INSERT INTO `server_env` VALUES (1,'HTCaaS.conf','','DBManager.Address=http://diamond.kisti.re.kr:9000/Database\nUDManager.Address=http://pearl.kisti.re.kr:9001/UserDataManager\nJobManager.Address=http://diamond.kisti.re.kr:9001/JobManager\nFTP.Address=pearl.kisti.re.kr\nMonitoring.Address=http://diamond.kisti.re.kr:9002/Monitoring\n\nActiveMQ.url=tcp://wisdom.kisti.re.kr:61616\nActiveMQ.Broker.JMXServiceURL=service:jmx:rmi:///jndi/rmi://wisdom.kisti.re.kr:2011/jmxrmi\nActiveMQ.Broker.ObjectName=my-broker:BrokerName=localhost,Type=Broker\nActiveMQ.SubJobQueue=JobQueue.ActiveMQ\nActiveMQ.MetaJobQueue=MetaJobQueue.ActiveMQ\n\nAgent.Heartbeat.Period=1\nSCAgent.Heartbeat.Period=1\nRunningAgent.Heartbeat.Period=6\nSubmittedAgent.Heartbeat.Period=20\nNewAgent.Heartbeat.Period=5\nStatusMonitoring.Heartbeat.Period=1\n\n\ndb.driver = com.mysql.jdbc.Driver\ndb.url = jdbc:mysql://amga.kisti.re.kr/htc\ndb.user = shlee\ndb.password = dltpgns\ndbpool.initialSize = 10\ndbpool.maxSize = 100','HTCaaS 서버 설정'),(2,'runAgent.sh','','#!/bin/sh\r\n\r\nusage=\"Usage: $0 agentId\"\r\n\r\nif [ $# -ne 1 ]; then\r\n  echo $usage >&2\r\n  exit 1\r\nfi\r\n\r\nagentId=$1\r\n\r\nexport ANT_HOME=$PWD/agent/apache-ant-1.8.1\r\nexport PATH=$ANT_HOME/bin:$PATH\r\n\r\nwget http://diamond.kisti.re.kr/htc_storage/agent.zip\r\nunzip -q agent.zip\r\n\r\ncd $PWD/agent\r\n\r\nchmod -R +x $ANT_HOME/bin\r\n\r\nls -al $ANT_HOME/bin\r\n\r\nant -f run.xml -DagentId=$agentId\r\n','일반 Agent 실행 스크립트'),(3,'runAgentPLSI.sh','','#!/bin/sh\n\nusage=\"Usage: $0 agentId\"\n\nif [ $# -ne 1 ]; then\n  echo $usage >&2\n  exit 1\nfi\n\nagentId=$1\nmkdir workspace\ncd workspace\nmkdir $1\ncd $1\n\nexport ANT_HOME=$PWD/agent/apache-ant-1.8.1\nexport PATH=$ANT_HOME/bin:$PATH\n\nwget http://diamond.kisti.re.kr/htc_storage/agent.zip\nunzip -q agent.zip\n\ncd $PWD/agent\n\nchmod -R +x $ANT_HOME/bin\n\nant -f run.xml -DagentId=$agentId\n','PLSI Agent 실행 스크립트'),(5,'initialdir','/htcaas/','',''),(6,'CLI_Version','20141016','','CLI 최신버전'),(7,'GUI_Version','20140902','','GUI 최신버전'),(8,'notice','','========HTCaaS Notice-========\r\nThe features of new version 130816\r\n1. Added the 2 new commands : htcaas-resource-info, version\r\n2. Edited the command htcaas-job-status : show the last 5 information and added the new option -a\r\n3. Changed the receive timeout of htcaas web service api to infinite\r\n4. Added the version information.\r\nPS-If you want to submit your many and long-running jobs not simple one, please let us know in advance.\r\n					- HTCaaS Team - \r\n======================================================','');
/*!40000 ALTER TABLE `server_env` ENABLE KEYS */;
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
