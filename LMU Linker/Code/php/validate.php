<?php

/**
 * ~~~validate.php~~~
 * @author Kai Barth
 * @version 1.2 - 19.12.2018
 * 
 * PHP-Funktion, die von weiteren PHP-Skripten, die die Logiken enthalten, ein-
 * gebunden wird. Dient zur allgemeinen serverseitigen Validierung der Nutzer-
 * eingaben.
 * 
 */

/**
 * validateInput Funktion zur serverseitigen Validierung der Nutzereingaben
 * 
 * @param String $data enthaelt die zu validierenden Nutzereingaben
 * @return String $data enthaelt die validierten Nutzeringaben
 */
function validateInput($data) {
    $data = filter_var(trim($data), FILTER_SANITIZE_STRING);
    $data = stripcslashes($data);
    $data = htmlspecialchars($data);

    return $data;
}

?>