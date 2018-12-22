/**
 * ~~~api.js~~~
 * @author Kai Barth
 * @version 1.2 - 19.12.2018
 * 
 * Das Skript beinhaltet die clientseitige Logik, um eine API, fuer die Abfrage
 * der Verknuepfungen zwischen LocationID und ContentID bereitzustellen. 
 * Es uebernimmt den Aufruf zum entsprechenden PHP-Skript auf dem Server via 
 * AJAX und wertet anschließend die empfangenen Daten des Servers wieder aus und
 * stellt diese fuer den Nutzer dar.
 * 
 */
$(document).ready(function () {

//Variablen anlegen
    var id_number = '';
    var id_type = '';
    var req_format = 'json';
    var flag_id_number = false;
    var flag_id_type = false;
    var flag_req_format = true;
    var url = '';
    //Eingabe der ID validieren
    $('#number_id').bind('click keyup', function () {
        id_number = $('#number_id').val();
        if (id_number.match(/^\d{1,5}$/) && id_number != '' && id_number > 0) {
            greenBorder('#number_id');
            flag_id_number = true;
        } else {
            redBorder('#number_id');
            flag_id_number = false;
        }
        ;
    });
    //Eingabe des ausgewaehlten Typs validieren
    $('#selected_id').bind('click change', function () {
        id_type = $('#selected_id').val();
        if (id_type.match(/^location_id$/)) {
            greenBorder('#selected_id');
            flag_id_type = true;
        } else {
            redBorder('#selected_id');
            flag_id_type = false;
        }
        ;
    });
    //Eingabe des ausgewaehlten Formates validieren
    $('input[name=req_format]').bind('click', function () {
        req_format = $('input[name=req_format]:checked').val();
        if (req_format.match(/^json$/)) {
            flag_req_format = true;
        } else {
            flag_req_format = false;
        }
        ;
    });
    //Verhalten des Reset-Buttons festlegen
    $(resetButton).click(function (e) {
        allRedBorders();
        flag_id_number = false;
        flag_id_type = false;
        hideHint();
        hideResult();
    });
    //Verhalten des Submit-Buttons festlegen
    $(submitButton).click(function (e) {

        e.preventDefault();
        lockButton(true);
        //pruefen, ob alle Eingaben erfolgreich validiert worden sind
        if (flag_id_number && flag_id_type && flag_req_format) {
            hideHint();

            //URL zusammenfuegen
            url = '/php/api.php?format=' + req_format + '&id_type=' + id_type + '&id=' + id_number;

            //oeffne neues Fenster zum Abfragen der API
            window.open(url);

            //Ausgabe der abgefragten URL
            $('#result').attr("style", "visibility: visible")
            $('#result').attr("class", "alert alert-warning");
            $('#result').attr("role", "alert");
            $('#result').html('Die abgefragte URL lautet: <br>www.lmulinker.kai-barth.de' + url).slideDown("Slow");
        } else {

            if (!flag_id_number) {
                redBorder('#number_id');
                showHint('ID. <br> Dieser muss zwischen 0 und 99999 liegen!');
            }

            if (!flag_id_type) {
                redBorder('#selected_id');
                showHint('ID-Typ.');
            }

            if (!flag_req_format) {
                redBorder('#req_format');
                showHint('Momentan wird nur JSON unterst&uumlzt!');
            }
        }
        lockButton(false);
    });
});

//Funktion zum einfaerben der Umrandungen als Hinweis fuer den Nutzer
function allRedBorders() {
    $('#number_id').css('border-color', 'red');
    $('#selected_id').css('border-color', 'red');
}

//Funktion zum einfaerben der Umrandungen als Hinweis fuer den Nutzer
function redBorder(element) {
    $(element).css('border-color', 'red');
}

//Funktion zum einfaerben der Umrandungen als Hinweis fuer den Nutzer
function greenBorder(element) {
    $(element).css('border-color', 'green');
}

//Funktion zur Ausgabe des Hinweises
function showHint(element) {
    $('#hint').attr("style", "visibility: visible");
    $('#hint').html('<span>Bitte korrigieren Sie den Wert f&uumlr das Feld ' + element + ' </span>').slideDown("Slow");
    hideResult();
    //unlock the button, otherwise it woud stay locked!
    lockButton(false);
}

//Funktion, um den Hinweis aus der Ansicht zu entfernen
function hideHint() {
    $('#hint').attr("style", "visibility: hidden");
    $('#hint').html('');
}

//Funktion, um die Ergebnisanzeige aus der Ansicht zu entfernen
function hideResult() {
    $('#result').html('');
    $('#result').attr("style", "visibility: hidden");
}

//Funktion, um den Submit-Button zu sperren, damit dieser waehrend des Ladevorgangs nicht erneut betaetigt werden kann
function lockButton(state) {
    $('#submitButton').prop('disabled', state);
}
