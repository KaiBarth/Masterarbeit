<?php

/**
 * ~~~linker_anzeigen.php~~~
 * @author Kai Barth
 * @version 1.2 19.12.2018
 * 
 * Serverseitiges Skript zur Ausgabe bestehender Verknuepfungen zwischen 
 * LocationID und ContentID
 * 
 */
//weitere erforderliche Skripte laden
require_once 'connectDatabase.php';
require_once 'validate.php';

//header auf json setzen
header('Content-Type: application/json; charset=utf-8');

//initialisiere Output-Array
$output = array();

//auf POST-Request pruefen
if ($_SERVER['REQUEST_METHOD'] == 'POST') {

    //pruefen, ob die entsprechenden Variablen vom Formular gesendet worden sind
    if (isset($_POST["id_type"], $_POST["id_number"])) {

        //Eingabe validieren
        $id_type = validateInput($_POST["id_type"]);
        $id_number = validateInput($_POST["id_number"]);

        //Auf leere Variablen pruefen
        if (empty($id_type) OR $id_type == '' OR empty($id_number) OR $id_number == '') {
            $output = json_encode(array('type' => 'error', 'text' => 'Keine leeren Felder erlaubt.'), JSON_UNESCAPED_UNICODE);
            die($output);
        }

        //id_type validieren
        if (!filter_var($id_type, FILTER_VALIDATE_REGEXP, array("options" => array("regexp" => "/^(location_id$)|(content_id$)|(all_id$)/")))) {
            $output = json_encode(array('type' => 'error', 'text' => 'Der ID-Type muss gew&aumlhlt werden.'), JSON_UNESCAPED_UNICODE);
            die($output);
        }

        //id_number validieren
        if (!filter_var($id_number, FILTER_VALIDATE_REGEXP, array("options" => array("regexp" => "/^\d{1,5}$/")))) {
            $output = json_encode(array('type' => 'error', 'text' => 'Die ID muss eine maximal f&uumlnfstellige Zahl sein.'), JSON_UNESCAPED_UNICODE);
            die($output);
        }

        //Datenbankverbindung aufbauen
        $mysql = new My_MySQLi("62.108.32.188", "bbjqe_admin", "x5wXv?35", "lmulinker_db");

        //uebermittelten Werte escapen
        $escaped_id_type = $mysql->escape($id_type);
        $escaped_id_number = $mysql->escape($id_number);

        //Abzufragenden ID-Typ speichern
        if ($escaped_id_type == 'location_id') {
            $requested_id_type = 'content_id';
        } elseif ($escaped_id_type == 'content_id') {
            $requested_id_type = 'location_id';
        } else {
            $requested_id_type = 'all_id';
        }

        //SQL-Statement vorbereiten
        if ($requested_id_type == 'content_id' || $requested_id_type == 'location_id') {
            $sql = 'SELECT ' . $requested_id_type . ' FROM location_content_link WHERE ' . $escaped_id_type . '=' . $escaped_id_number . ';';
        } else {
            $sql = 'SELECT * FROM location_content_link;';
        }

        //SQL-Statement ausführen
        $result_sql = $mysql->query($sql);
        
        //Pruefen, ob eine Verknuepfung besteht
        if ($result_sql->num_rows === 0) {
            $output = json_encode(array('type' => 'norows', 'text' => 'Zu dieser ID sind keine Eintr&aumlge vorhanden.'), JSON_UNESCAPED_UNICODE);
            die($output);
        } else {
            //Speicher das Ergebnis in einem Array
            while ($resultObj = $result_sql->fetch_assoc()) {
                $result[] = $resultObj;
            }
            
            //Ausgabe vorbereiten
            $output['type'] = 'success';
            
            //LocationID ist angefragt worden
            if ($escaped_id_type == 'location_id') {
                $output['id_type'] = 'LocationID';
            } elseif ($escaped_id_type == 'location_id') {
                $output['id_type'] = 'ContentID';
            } else {
                $output['id_type'] = 'allen Verkn&uumlpfungen';
            }

            //ContentID ist angefragt worden
            if ($escaped_id_type == 'content_id') {
                $output['requested_id_type'] = 'LocationID';
            } elseif ($escaped_id_type == 'location_id') {
                $output['requested_id_type'] = 'ContentID';
            } else {
                $output['requested_id_type'] = 'all';
            }

            //Alle Verknuepfungen sollen ausgegeben werden
            if ($requested_id_type == 'all_id') {
                $output['id'] = '';
            } else {
                $output['id'] = $escaped_id_number;
            }
            
            //Anzahl der Verknuepfungen 
            $output['anzahl'] = count($result);

            //iteriere durch die Treffermenge
            foreach ($result as $row) {
                if ($requested_id_type == 'all_id') {
                    $output['result'][] = $row;
                } else {
                    $output['result'][] = $row[$requested_id_type];
                }
            }

            $output = json_encode($output, JSON_UNESCAPED_UNICODE);
            die($output);
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