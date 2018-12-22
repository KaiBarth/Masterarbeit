package com.example.kai.philologicum;

import java.util.HashMap;

/**
 * Class LocationMap represents the internal way to save the link between keywords and locations
 * It contains the initial assignments between them. Edit this basement of data to train the
 * system, in order to recognize different locations.
 * Up to know the app only uses these fixed links. In the future source code should be implemented
 * for a flexible use. Maybe this class must be changed than.
 */

public class LocationMap {

    //HasMap for saving the links
    //Keys are the string values which could be recognized by the app
    //Values are the locationIDs which are passed back
    private HashMap<String, Integer> locationMap = new HashMap<String, Integer>();

    /**
     * Method buildLocationMap contains all initial values for the system. If it is not necessary
     * to use the system in a flexible way, this method can be used to train the system.
     * By using a flexible way for managing the links between keywords and locations it may be
     * possible to change the source code.
     *
     * @return true if finished successfully, false if not
     */
    public boolean buildLocationMap () {

        locationMap.clear();

        try {
            //locationID's for testscenario panel 1 front
            locationMap.put("Miss Brooke had", 11);
            locationMap.put("that kind of beauty which", 11);
            locationMap.put("seems to be thrown", 11);
            locationMap.put("into relief by poor dress.", 11);
            locationMap.put("Who's", 11);
            locationMap.put("there?", 11);
            locationMap.put("Hohe Herren", 11);
            locationMap.put("von der Akademie!", 11);
            locationMap.put("ÄNTLIGEN STOD", 11);
            locationMap.put("PRÄSTEN I", 11);
            locationMap.put("PREDIKSTOLEN.", 11);
            locationMap.put("Il faut, autant qu’on peut, obliger tout le monde:", 11);
            locationMap.put("On a souvent besoin d’un plus petit que soi.", 11);
            locationMap.put("Lydia brukade", 11);
            locationMap.put("bada ensam", 11);

            //locationID's for testscenario panel 1 back
            locationMap.put("Hemos perdido", 12);
            locationMap.put("aun este", 12);
            locationMap.put("crepúsculo.", 12);
            locationMap.put("De o săptămînă …", 12);
            locationMap.put("De o săptămînă nu mai am linişte,", 12);
            locationMap.put("fir-ar să fie!", 12);
            locationMap.put("Veliká doba", 12);
            locationMap.put("žádá velké l idi.", 12);
            locationMap.put("Nay answer me:", 12);
            locationMap.put("Stand & vnfold your selfe.", 12);
            locationMap.put("Erschrecken Sie nicht, meine Freundin,", 12);
            locationMap.put("anstatt der Handschrift von Ihrer Sternheim", 12);
            locationMap.put("eine gedruckte Copey zu erhalten,", 12);
            locationMap.put("welche Ihnen auf einmal die ganze Verräterei", 12);
            locationMap.put("entdeckt, die ich an Ihnen begangen habe.", 12);
            locationMap.put("Umana cosa è", 12);
            locationMap.put("aver compassione", 12);
            locationMap.put("degli aff litti", 12);
            locationMap.put("Egy kissé mindjárt", 12);
            locationMap.put("hátra is hoköltem tolük,", 12);
            locationMap.put("természetesen.", 12);

            ////locationID's for testscenario panel 2 front
            locationMap.put("GALLIA EST OMNIS DIVISA IN PARTES TRES,", 21);
            locationMap.put("QUARUM UNAM INCOLUNT BELGAE,", 21);
            locationMap.put("ALIAM AQUITANI, TERTIAM QUI IPSORUM LINGUA CELTAE,", 21);
            locationMap.put("NOSTRA GALLI APPELANTUR.", 21);
            locationMap.put("Мой дядя самых честных правил,", 21);
            locationMap.put("Когда не в шутку занемог,", 21);
            locationMap.put("Он уважать себя заставил.", 21);
            locationMap.put("И лучше выдумать не мог.", 21);
            locationMap.put("Legalább vállfát", 21);
            locationMap.put("ne kelljen keresgélniük.", 21);

            ////locationID's for testscenario panel 2 back
            locationMap.put("Ἄνδρα μοι ἔννεπε, Μοῦσα, πολύτροπον, ὃς μάλα πολλὰ", 22);
            locationMap.put("πλάγχϑη, ἐπεὶ Τροίης ἱερὸν πτολίεϑρον ἔπερσε·", 22);
            locationMap.put("πολλῶν δ' ἀνϑρώπων ἴδεν ἄστεα καὶ νόον ἔγνω,", 22);
            locationMap.put("πολλὰ δ' ὅ γ' ἐν πόντῳ πάϑεν ἄλγεα ὃν κατὰ ϑυμόν,", 22);
            locationMap.put("ἀρνύμενος ἥν τε ψυχὴν καὶ νόστον ἑταίρων.", 22);
            locationMap.put("Mieleni minun tekevi,", 22);
            locationMap.put("Aivoni ajattelevi,", 22);
            locationMap.put("Lähteäni laulamahan,", 22);
            locationMap.put("Saa’ani sanelemahan,", 22);
            locationMap.put("Sukuvirttä suoltamahan,", 22);
            locationMap.put("Lajivirttä laulamahan;", 22);

            ////locationID's for testscenario panel 3 front
            locationMap.put("En un lugar de la Mancha,", 31);
            locationMap.put("de cuyo nombre no quiero acordarme ...", 31);
            locationMap.put("Hljóðs bið ek allar", 31);
            locationMap.put("helgar kindir,", 31);
            locationMap.put("meiri ok minni", 31);
            locationMap.put("mögu Heimdallar;", 31);
            locationMap.put("viltu, at ek, Valföðr!", 31);
            locationMap.put("vel framtelja", 31);
            locationMap.put("forn spjöll fíra,", 31);
            locationMap.put("þau er fremst um man.", 31);
            locationMap.put("Ποικιλόθρον᾽ ὰθάνατ᾽ ᾽Αφροδιτα,", 31);
            locationMap.put("παῖ Δίοσ, δολόπλοκε, λίσσομαί σε", 31);
            locationMap.put("μή μ᾽ ἄσαισι μήτ᾽ ὀνίαισι δάμνα,", 31);
            locationMap.put("πότνια, θῦμον.", 31);

            ////locationID's for testscenario panel 3 back
            locationMap.put("It was a queer. sultry summer,", 32);
            locationMap.put("the summer they electrocuted", 32);
            locationMap.put("the Rosenbergs, and I didn’t know", 32);
            locationMap.put("Sein Blick ist vom Vorübergehn der Stäbe", 32);
            locationMap.put("what I was doing in New York.", 32);
            locationMap.put("so müd geworden, daß er nichts mehr hält.", 32);

            ////locationId for testscenario panel 4 front
            locationMap.put("PHIL 01.123", 41);
            locationMap.put("PHIL 01 123", 41);
            locationMap.put("01 123", 41);

            ////locationId for testscecario mufu
            locationMap.put("Mufu", 51);
            locationMap.put("mufu", 51);
            locationMap.put("Mufu 1", 51);
            locationMap.put("mufu 1", 51);

            return true;
        } catch (Exception e) {
            //TODO Exception Handler
            return false;
        }
    }

    /**
     * Method find LocationId contains the logic for looking up if a recognized string from
     * LMU Spot is a keyword linked to a location.
     *
     * @param keyword String recognized by the LMU Spot tab
     * @return null if no matched was found, otherwise return the retrieved locationId
     */
    public Integer findLocationId (String keyword) {

        if (locationMap.isEmpty()) {
            return null;
        }

        if (locationMap.containsKey(keyword)) {
            return locationMap.get(keyword);
        } else {
            return null;
        }

    }

    //TODO Method for Updating the location map
}
