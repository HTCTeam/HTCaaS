-- MySQL dump 10.13  Distrib 5.1.73, for redhat-linux-gnu (x86_64)
--
-- Host: localhost    Database: htcaas_server
-- ------------------------------------------------------
-- Server version	5.1.73
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping routines for database 'htcaas_server'
--
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50020 DEFINER=`chiron`@`%`*/ /*!50003 FUNCTION `GET_INFRA_NAME`(strIds VARCHAR(50)) RETURNS varchar(50) CHARSET utf8
    SQL SECURITY INVOKER
BEGIN
        DECLARE startIdx INT;
        DECLARE endIdx INT;
        DECLARE returnStr VARCHAR(50);

        SET startIdx = 1;
        SET endIdx = 0;
        SET returnStr = '';
        
        IF LOCATE(',', strIds) = 0 AND  LENGTH(strIds) > 0 THEN
            SET returnStr = (SELECT NAME FROM SERVICE_INFRA WHERE ID = strIds);
        ELSE
            WHILE LOCATE(',', strIds) != 0 DO
                SET endIdx = LOCATE(',', strIds);
                SET returnStr = CONCAT(returnStr, (SELECT NAME FROM SERVICE_INFRA WHERE ID = SUBSTRING(strIds, startIdx, (endIdx-startIdx))));
                SET returnStr = CONCAT(returnStr, ', ');
                SET strIds = SUBSTRING(strIds, endIdx+1);
            
                IF (LOCATE(',', strIds) = 0) THEN 
                    SET returnStr = CONCAT(returnStr, (SELECT NAME FROM SERVICE_INFRA WHERE ID = strIds));
                    SET returnStr = CONCAT(returnStr, ' ');
                END IF;

            END WHILE;
        END IF;

    RETURN returnStr;
    
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-04-16 12:02:14
