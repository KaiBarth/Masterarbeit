<?php

/**
 * ~~~linker_speichern.php~~~
 * @author Kai Barth
 * @version 1.2 19.12.2018
 * 
 * Serverseitiges Skript zum Speichern einer Verknuepfung zwischen LocationID
 * und ContentID 
 * 
 */
//weitere erforderliche Skripte laden
require_once 'connectDatabase.php';
require_once 'validate.php';

//header auf json setzen
header('Content-Type: application/json; charset=utf-8');

//output Array initialisieren
$output = array();

//auf POST-Request pruefen
if ($_SERVER['REQUEST_METHOD'] == 'POST') {

    //pruefen, ob die entsprechenden Variablen vom Formular gesendet worden sind
    if (isset($_POST["location_id"], $_POST["content_id"])) {

//Eingabe validieren
        $location_id = validateInput($_POST["location_id"]);
        $content_id = validateInput($_POST["content_id"]);

        //Auf leere Variablen pruefen
        if (empty($location_id) OR $location_id == '' OR empty($content_id) OR $content_id == '') {
            $output = json_encode(array('type' => 'error', 'text' => 'Keine leeren Felder erlaubt.'), JSON_UNESCAPED_UNICODE);
            die($output);
        }

        //location_id validieren
        if (!filter_var($location_id, FILTER_VALIDATE_REGEXP, array("options" => array("regexp" => "/^\d{1,5}$/")))) {
            $output = json_encode(array('type' => 'error', 'text' => 'Die LocationID muss eine maximal f&uumlnfstellige Zahl sein.'), JSON_UNESCAPED_UNICODE);
            die($output);
        }

        //content_id validieren
        if (!filter_var($content_id, FILTER_VALIDATE_REGEXP, array("options" => array("regexp" => "/^\d{1,5}$/")))) {
            $output = json_encode(array('type' => 'error', 'text' => 'Die ContentID muss eine maximal f&uumlnfstellige Zahl sein.'), JSON_UNESCAPED_UNICODE);
            die($output);
        }

        //Datenbankverbindung aufbauen
        $mysql = new My_MySQLi("62.108.32.188", "bbjqe_admin", "x5wXv?35", "lmulinker_db");

        //uebermittelten Werte escapen
        $escaped_location_id = $mysql->escape($location_id);
        $escaped_content_id = $mysql->escape($content_id);

        //SQL-Statement vorbereiten
        $sql = 'SELECT * FROM location_content_link WHERE location_id = ' . $escaped_location_id . ' AND content_id = ' . $escaped_content_id . ';';

        //SQL-Statement ausführen
        $result_sql = $mysql->query($sql);

        //Pruefen, ob die Verlinkung bereits besteht
        if ($result_sql->num_rows > 0) {
            $output = json_encode(array('type' => 'error', 'text' => 'Verkn&uuml;pfung breits vorhanden!'), JSON_UNESCAPED_UNICODE);
            die($output);
        } else {
            //Pruefen ob der LocationID bereits eine ContentID zugeordnet ist
            $sql = 'SELECT content_id FROM location_content_link WHERE location_id = ' . $escaped_location_id . ';';

            //SQL-Statement ausführen
            $result_sql = $mysql->query($sql);

            //Pruefen, ob auf diese LocationID bereits verlinkt worden ist
            if ($result_sql->num_rows > 0) {
                $result = $result_sql->fetch_assoc();
                $output = json_encode(array('type' => 'error', 'text' => 'Auf diese LocationID ist bereits eine Verkn&uuml;pfung zu ContentID ' . $result[content_id] . ' gesetzt.'), JSON_UNESCAPED_UNICODE);
                die($output);
            } else {
                //SQL-Statement vorbereiten
                $sql = 'INSERT INTO location_content_link (id, location_id, content_id) VALUES (null, ' . $location_id . ', ' . $content_id . ');';
                
                //Pruefen, ob speichern erfolgreich gewesen ist
                if ($mysql->query($sql)) {
                    $output["type"] = 'success';
                    $output["location_id"] = $escaped_location_id;
                    $output["content_id"] = $escaped_content_id;
                    $output = json_encode($output, JSON_UNESCAPED_UNICODE);
                    die($output);
                } else {
                    //Ausgabe der Fehlermeldung als JSON-Objekt
                    $error = $mysql->errno() . '-' . $mysql->error();
                    $output = json_encode(array('type' => 'error', 'text' => 'Speichern der Verkn&uuml;pfung fehlgeschlagen. Fehler: ' . $error), JSON_UNESCAPED_UNICODE);
                    die($output);
                }
            }
        }
    } else {
        $output = json_encode(array('type' => 'error', 'text' => 'Einige Variablen fehlen'), JSON_UNESCAPED_UNICODE);
        die($output);
    }
} else {
    $output = json_encode(array('type' => 'error', 'text' => 'Request Method muss POST sein!'), JSON_UNESCAPED_UNICODE);
    die($output);
}
?>