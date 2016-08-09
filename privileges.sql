-- MySQL dump 10.16  Distrib 10.1.14-MariaDB, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: openmrs
-- ------------------------------------------------------
-- Server version	5.6.31

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `role_privilege`
--
-- WHERE:  role='Radiology: Referring physician'

LOCK TABLES `role_privilege` WRITE;
/*!40000 ALTER TABLE `role_privilege` DISABLE KEYS */;
INSERT INTO `role_privilege` VALUES ('Radiology: Referring physician','Add Encounters'),('Radiology: Referring physician','Add Observations'),('Radiology: Referring physician','Add Orders'),('Radiology: Referring physician','Add Radiology Orders'),('Radiology: Referring physician','Add Radiology Studies'),('Radiology: Referring physician','Add Relationships'),('Radiology: Referring physician','Add Visits'),('Radiology: Referring physician','Delete Observations'),('Radiology: Referring physician','Delete Radiology Orders'),('Radiology: Referring physician','Delete Relationships'),('Radiology: Referring physician','Edit Encounters'),('Radiology: Referring physician','Edit Observations'),('Radiology: Referring physician','Edit Orders'),('Radiology: Referring physician','Edit Radiology Studies'),('Radiology: Referring physician','Edit Relationships'),('Radiology: Referring physician','Edit Visits'),('Radiology: Referring physician','Form Entry'),('Radiology: Referring physician','Get Care Settings'),('Radiology: Referring physician','Get Concepts'),('Radiology: Referring physician','Get Encounter Roles'),('Radiology: Referring physician','Get Encounters'),('Radiology: Referring physician','Get Forms'),('Radiology: Referring physician','Get Observations'),('Radiology: Referring physician','Get Orders'),('Radiology: Referring physician','Get Patients'),('Radiology: Referring physician','Get People'),('Radiology: Referring physician','Get Providers'),('Radiology: Referring physician','Get Radiology Orders'),('Radiology: Referring physician','Get Radiology Studies'),('Radiology: Referring physician','Get Users'),('Radiology: Referring physician','Get Visit Attribute Types'),('Radiology: Referring physician','Get Visit Types'),('Radiology: Referring physician','Get Visits'),('Radiology: Referring physician','Manage Visit Types'),('Radiology: Referring physician','Patient Dashboard - View Demographics Section'),('Radiology: Referring physician','Patient Dashboard - View Encounters Section'),('Radiology: Referring physician','Patient Dashboard - View Forms Section'),('Radiology: Referring physician','Patient Dashboard - View Overview Section'),('Radiology: Referring physician','Patient Dashboard - View Patient Summary'),('Radiology: Referring physician','Patient Dashboard - View Radiology Section'),('Radiology: Referring physician','Patient Dashboard - View Regimen Section'),('Radiology: Referring physician','View Encounters'),('Radiology: Referring physician','View Forms'),('Radiology: Referring physician','View Navigation Menu'),('Radiology: Referring physician','View Observations'),('Radiology: Referring physician','View Orders'),('Radiology: Referring physician','View Patients');
/*!40000 ALTER TABLE `role_privilege` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-08-09 13:32:04
