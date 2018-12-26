-- phpMyAdmin SQL Dump
-- version 4.8.4
-- https://www.phpmyadmin.net/
--
-- Host: 62.108.32.188:3306
-- Erstellungszeit: 26. Dez 2018 um 10:30
-- Server-Version: 5.6.42
-- PHP-Version: 7.1.14

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Datenbank: `lmulinker_db`
--

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `location_content_link`
--

CREATE TABLE `location_content_link` (
  `id` int(10) NOT NULL,
  `location_id` int(10) NOT NULL,
  `content_id` int(10) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Daten für Tabelle `location_content_link`
--

INSERT INTO `location_content_link` (`id`, `location_id`, `content_id`) VALUES
(1, 1, 1),
(2, 2, 1),
(3, 3, 2),
(4, 4, 2),
(5, 5, 2),
(6, 11, 1),
(7, 21, 2),
(8, 41, 3),
(9, 51, 4);

--
-- Indizes der exportierten Tabellen
--

--
-- Indizes für die Tabelle `location_content_link`
--
ALTER TABLE `location_content_link`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT für exportierte Tabellen
--

--
-- AUTO_INCREMENT für Tabelle `location_content_link`
--
ALTER TABLE `location_content_link`
  MODIFY `id` int(10) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
