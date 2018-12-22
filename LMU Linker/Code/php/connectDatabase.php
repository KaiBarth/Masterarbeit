<?php

/**
 * ~~~connectDatabase.php~~~
 * @author Kai Barth
 * @version 1.3 - 19.12.2018
 * 
 * PHP-Skript zur Verfuegungstellung einer Klasse, die den Aufbau zur 
 * Datenbank verwaltet und SQL-Statements ausfuehrt.
 * Darueber hinaus beinhaltet diese Funktionen, um Fehlerberichte zu erstellen
 * sowie eine Funktion, um uebergebene Werte zu escapen, damit SQL-Injections
 * unterbunden werden.
 *
 */
class My_MySQLi {

    //private Variablen 
    private $_host = null;
    private $_username = null;
    private $_password = null;
    private $_database = null;
    private $_charset = null;
    private $_port = 3306;
    private $_isConnected = false;
    private $_connection = null;

    /**
     * Konstruktor
     */
    public function __construct($host, $username, $password, $database, $charset = "UTF8") {
        $this->setHost($host);
        $this->setUsername($username);
        $this->setPassword($password);
        $this->setDatabase($database);
        $this->setCharset($charset);
    }

    /**
     * connect Funktion zum Aufbaue einer Verbindung zur Datenbank
     * 
     * @return
     */
    protected function connect() {
        //Pruefe, ob die Verbindung bereits besteht
        if (!is_null($this->_connection)) {
            return;
        }

        //initialisiere mysqli
        $this->_connection = mysqli_init();

        //beuge Fehler vor
        $this->_isConnected = @mysqli_real_connect($this->_connection, $this->_host, $this->_username, $this->_password, $this->_database, $this->_port);

        //Werfe einen Fehler, wenn keine Verbindung moeglich ist
        if (!$this->_isConnected) {
            $output = json_encode(array('type' => 'error', 'text' => 'No connection to internal database!'));
            die($output);
        }

        //Zeichensatz fuer die Verbindung nutzen
        mysqli_set_charset($this->_connection, $this->_charset);
    }

    /**
     * query Funktion zum Senden von Queries an die Datenbank
     * @param String $query
     * @return Gebe Boolean zurueck, wenn erfolgreich
     */
    public function query($query) {
        $this->connect();

        return mysqli_query($this->_connection, $query);
    }

    /**
     * errno Funktion, zur Rueckgabe der Fehlernummer, wenn die Query 
     * fehlschlaegt
     * 
     * @return String enthaelt die Fehlernummer
     */
    public function errno() {
        $this->connect();

        return mysqli_errno($this->_connection);
    }

    /**
     * error Funktion zur Rueckgabe einer Fehlermeldung
     * 
     * @return String enthaelt die Fehlernachricht
     */
    public function error() {
        $this->connect();

        return mysqli_error($this->_connection);
    }

    /**
     * escape Funktion zur Bereitstellung von escapten Werten, um SQL-Injections
     * vorzubeugen
     * 
     * @param String $string enthaelt den unescapten Wert
     * @return String enthaelt den escapten Wert
     */
    public function escape($string) {
        $this->connect();

        return mysqli_real_escape_string($this->_connection, $string);
    }

    /**
     * prepare Funktion zur Vorbereitung von SQL-Statements
     * 
     * @param String $sql enthaelt das vorzubereitende SQL-Statement
     * @return mysqli_stmt Statement,dass das vorbereitete statment enthaelt
     */
    public function prepare($sql) {
        $this->connect();

        return mysqli_prepare($this->_connection, $sql);
    }

    /**
     * setter fuer den Host-Wert
     * @param String $host enthaelt den zu setzenden Wert
     */
    public function setHost($host) {
        $this->_host = $host;
    }

    /**
     * setter fuer den Benutzernamen
     * @param type $username enthaelt den zu setzenden Wert
     */
    public function setUsername($username) {
        $this->_username = $username;
    }

    /**
     * setter fuer das Passwort
     * @param type $password enthaelt den zu setzenden Wert
     */
    public function setPassword($password) {
        $this->_password = $password;
    }

    /**
     * setter fuer die Datenbank
     * @param type $database enthaelt den zu setzenden Wert
     */
    public function setDatabase($database) {
        $this->_database = $database;
    }

    /**
     * setter fuer den Zeichensatz
     * @param type $charset enthaelt den zu setzenden Wert
     */
    Public function setCharset($charset) {
        $this->_charset = $charset;
    }

}
