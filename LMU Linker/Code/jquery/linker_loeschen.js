/**
 * ~~~linker_loeschen.js~~~
 * @author Kai Barth
 * @version 1.2 - 19.12.2018
 * 
 * Das Skript beinhaltet die clientseitige Logik, um Verknuepfungen zwischen
 * LocationID und ContentID zu loeschen. Es uebernimmt den Aufruf zum 
 * entsprechenden PHP-Skript auf dem Server via AJAX, wertet anschließend 
 * die empfangenen Daten des Servers wieder aus und stellt diese fuer den Nutzer 
 * dar.
 * 
 */
$(document).ready(function () {

    //Variablen anlegen
    var location_id = '';
    var content_id = '';
    var flag_location_id = false;
    var flag_content_id = false;

    //Eingabe der LocationID validieren
    $('#location_id').bind('click keyup', function () {
        location_id = $('#location_id').val();
        if (location_id.match(/^\d{1,5}$/) && location_id != '' && location_id > 0) {
            greenBorder('#location_id');
            flag_location_id = true;
        } else {
            redBorder('#location_id');
            flag_location_id = false;
        }
        ;
    });
    
    //Eingabe der ContentID validieren
    $('#content_id').bind('click keyup', function () {
        content_id = $('#content_id').val();
        if (content_id.match(/^\d{1,5}$/) && content_id != '' && content_id > 0) {
            greenBorder('#content_id');
            flag_content_id = true;
        } else {
            redBorder('#content_id');
            flag_content_id = false;
        }
        ;
    });
    
    //Verhalten des Reset-Buttons festlegen
    $(resetButton).click(function (e) {
        allRedBorders();
        flag_location_id = false;
        flag_content_id = false;
        hideHint();
        hideResult();
    });
    
    //Verhalten des Submit-Buttons festlegen
    $(submitButton).click(function (e) {
        e.preventDefault();
        lockButton(true);
        
        //pruefen, ob alle Eingaben erfolgreich validiert worden sind
        if (flag_location_id && flag_content_id) {
            hideHint();
            
            //AJAX Request an den Server schicken
            $.ajax({
                type: 'POST',
                url: 'php/linker_loeschen.php',
                dataType: 'json',
                data: {location_id: location_id,
                    content_id: content_id},
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
                    } else if (data.type == 'success') {
                        $('#result').attr("class", "alert alert-success");
                        $('#result').attr("role", "alert");
                        $('#result').html('L&ouml;schen der Verkn&uuml;pfung zwischen LocationID ' + data.location_id + ' und ContentID ' + data.content_id + ' erfolgreich!').slideDown("Slow");
                    }
                }
            });
        } else {

            if (!flag_content_id) {
                showHint('ContentID. <br> Dieser muss zwischen 0 und 99999 liegen!');
                redBorder('#content_id');
            }

            if (!flag_location_id) {
                showHint('LocationID. <br> Dieser muss zwischen 0 und 99999 liegen!');
                redBorder('#location_id');
            }
        }
    });
});

//Funktion zum einfaerben der Umrandungen als Hinweis fuer den Nutzer
function allRedBorders() {
    $('#content_id').css('border-color', 'red');
    $('#location_id').css('border-color', 'red');
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
