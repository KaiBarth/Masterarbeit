<?php

/**
 * ~~~api.php~~~
 * @author Kai Barth
 * @version 1.2 19.12.2018
 * 
 * Serverseitiges Skript fuer die Bereitstellung einer API zum Abfragen der
 * Verknuepfung zwischen LocationID und ContentID 
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
if ($_SERVER['REQUEST_METHOD'] == 'GET') {
    
    //pruefen, ob die entsprechenden Variablen vom Formular gesendet worden sind
    if (isset($_GET["format"], $_GET["id_type"], $_GET["id"])) {
        
//Eingabe validieren
        $format = validateInput($_GET["format"]);
        $id_type = validateInput($_GET["id_type"]);
        $id = validateInput($_GET["id"]);
        $retrieve_id_type = 'content_id';

        //Auf leere Variablen pruefen
        if (empty($id_type) OR $id_type == '' OR empty($id) OR $id == '' OR empty($format) OR $format == '') {
            http_response_code(404);
            $output = json_encode(array('type' => 'error', 'text' => 'Keine leeren Felder erlaubt.'), JSON_UNESCAPED_UNICODE);
            die($output);
        }
        
        //Format validieren (momentan wird nur JSON unterstuetzt)
        if (!filter_var($format, FILTER_VALIDATE_REGEXP, array("options" => array("regexp" => "/^(json$)/")))) {
            http_response_code(404);
            $output = json_encode(array('type' => 'error', 'text' => 'Derzeit kann nur JSON abgefragt werden'), JSON_UNESCAPED_UNICODE);
            die($output);
        }

        //id_type validieren
        if (!filter_var($id_type, FILTER_VALIDATE_REGEXP, array("options" => array("regexp" => "/^(location_id$)/")))) {
            http_response_code(404);
            $output = json_encode(array('type' => 'error', 'text' => 'Derzeit kann nur die LocationID abgfragt werden.'), JSON_UNESCAPED_UNICODE);
            die($output);
        }

        //id_number validieren
        if (!filter_var($id, FILTER_VALIDATE_REGEXP, array("options" => array("regexp" => "/^\d{1,5}$/")))) {
            http_response_code(404);
            $output = json_encode(array('type' => 'error', 'text' => 'Die ID muss eine maximal fuenfstellige Zahl sein.'), JSON_UNESCAPED_UNICODE);
            die($output);
        }

        //Datenbankverbindung aufbauen
        $mysql = new My_MySQLi("62.108.32.188", "bbjqe_admin", "x5wXv?35", "lmulinker_db");

        //uebermittelten Werte escapen
        $escaped_id_type = $mysql->escape($id_type);
        $escaped_format = $mysql->escape($format);
        $escaped_id = $mysql->escape($id);

        //SQL-Statement vorbereiten
        $sql = 'SELECT ' . $retrieve_id_type . '  FROM location_content_link WHERE ' . $escaped_id_type . '=' . $escaped_id . ';';
        
        //SQL-Statement ausführen
        $result_sql = $mysql->query($sql);

        //Pruefen, ob eine Verknuepfung zu der angefragten LocationID besteht
        if ($result_sql->num_rows === 0) {
            http_response_code(200);
            $output = json_encode(array('type' => 'norows', 'text' => 'Zu dieser ID sind keine Eintraege vorhanden.'), JSON_UNESCAPED_UNICODE);
            die($output);
        } else {
            //Speicher das Ergebnis in einem Array
            while ($resultObj = $result_sql->fetch_assoc()) {
                $result[] = $resultObj;
            }
            //Vorbereiten der Ausgabe
            $output['type'] = 'success';
            $output['requested_id_type'] = $escaped_id_type;
            $output['requested_id'] = $escaped_id;
            $output['linked_id_type'] = $retrieve_id_type;
            $output['sum'] = count($result);                        
            //iteriere durch die Treffermenge
            foreach ($result as $row) {
                $output['linked_id'] = $row[$retrieve_id_type];
            }
            http_response_code(200);
            $output = json_encode($output, JSON_UNESCAPED_UNICODE);
            die($output);
        }
    } else {
        http_response_code(404);
        $output = json_encode(array('type' => 'error', 'text' => 'Einige Variablen fehlen'), JSON_UNESCAPED_UNICODE);
        die($output);
    }
} else {
    http_response_code(404);
    $output = json_encode(array('type' => 'error', 'text' => 'Request Method muss GET sein!'), JSON_UNESCAPED_UNICODE);
    die($output);
}
?>