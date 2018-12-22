/**
 * ~~~linker_anzeigen.js~~~
 * @author Kai Barth
 * @version 1.2 - 19.12.2018
 * 
 * Das Skript beinhaltet die clientseitige Logik, um Verknuepfungen zwischen
 * LocationID und ContentID anzuzeigen. Es uebernimmt den Aufruf zum 
 * entsprechenden PHP-Skript auf dem Server via AJAX, wertet anschließend 
 * dieempfangenen Daten des Servers wieder aus und stellt diese fuer den Nutzer 
 * dar.
 * 
 */
$(document).ready(function () {

    //Variablen anlegen
    var id_number = '';
    var id_type = '';
    var flag_id_number = false;
    var flag_id_type = false;
    
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
    
    //Eingabe der ausgewaehlten ID validieren
    $('#selected_id').bind('click change', function () {
        id_type = $('#selected_id').val();
        if (id_type.match(/^location_id$/) || id_type.match(/^content_id$/)) {
            greenBorder('#selected_id');
            flag_id_type = true;
        } else if (id_type.match(/^all_id$/)) {
            flag_id_type = true;
            flag_id_number = true;
            greenBorder('#selected_id');
            greenBorder('#number_id');
            id_number = 99999;
        } else {
            redBorder('#selected_id');
            flag_id_type = false;
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
        if (flag_id_number && flag_id_type) {
            
            //AJAX Request an den Server schicken
            $.ajax({
                type: 'POST',
                url: 'php/linker_anzeigen.php',
                dataType: 'json',
                data: {id_type: id_type,
                    id_number: id_number},
                complete: function () {
                    lockButton(false);
                },
                error: function () {
                    alert("Something went wrong call the dev ;)");
                },
                success: function (data) {
                    hideHint();
                    $('#result').attr("style", "visibility: visible");
                    if (data.type == 'error') {
                        $('#result').attr("class", "alert alert-danger");
                        $('#result').attr("role", "alert");
                        $('#result').html(data.text).slideDown("Slow");
                    } else if (data.type == 'norows') {
                        $('#result').attr("class", "alert alert-warning");
                        $('#result').attr("role", "alert");
                        $('#result').html(data.text).slideDown("Slow");
                    } else if (data.type == 'success') {
                        counter = data.anzahl;
                        $('#result').attr("class", "alert alert-success");
                        $('#result').attr("role", "alert");
                        $('#result').html('Ergebnis der Abfrage zu ' + data.id_type + ' ' + data.id + ':<br>').slideDown("Slow");
                        $('#result').append('Gesamtanzahl an Verkn&uumlpfungen: ' + data.anzahl + '<br>________________________________________<br>');
                        if (data.requested_id_type == 'all') {
                            for (i = 0; i < data.anzahl; i++) {
                                $('#result').append('LocationID: ' + data.result[i].location_id + ' --> ContentID: ' + data.result[i].content_id + '<br>');
                            }
                        } else {
                            for (i = 0; i < data.anzahl; i++) {
                                $('#result').append(data.requested_id_type + ' ' + data.result[i] + '<br>');
                            }
                        }
                    }

                }
            });
        } else {

            if (!flag_id_number) {
                redBorder('#number_id');
                showHint('ID. <br> Dieser muss zwischen 0 und 99999 liegen!');
            }

            if (!flag_id_type) {
                redBorder('#selected_id');
                showHint('ID-Typ.');
            }
        }
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
