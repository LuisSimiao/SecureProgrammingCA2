SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `mysecureapplication`
--

DROP DATABASE IF EXISTS `bank_system`;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `bank_system` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `bank_system`;
-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `customers` (
  `accountNo` varchar(25) NOT NULL,
  `password` varchar(25) NOT NULL,
  `balance` float NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `users`
--

INSERT INTO `customers` (`accountNo`, `password`, `balance`) VALUES
('test', 'test', '100.23'),
('BOI16589', 'password1', '100.23'),
('BOI23658', 'password2', '2000.36'),
('BOI41235', 'password3', '500.99'),
('BOI87854', 'password4', '123.65'),
('BOI11236', 'password5', '6587.21'),
('BOI65214', 'password6', '5000.00'),
('BOI66985', 'password7', '86.35');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `users`
--
ALTER TABLE `customers`
  ADD UNIQUE KEY `accountNo` (`accountNo`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;