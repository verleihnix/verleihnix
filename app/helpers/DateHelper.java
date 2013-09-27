package helpers;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: chrisch
 * Date: 17.04.13
 * Time: 20:04
 * Eine Utility-Klasse mit Hilfsmethoden für Datumsangaben.
 */
public final class DateHelper {

    /**
     * Konstruktor private weil Utility Klasse
     */
     private DateHelper() {};

    /**
     * Methode um das zu komplizierte toString von Date zu
     * einem lesbareren format zu konvertieren
     * Die Methode ist außerdem null-sicher!
     * @param ein datumsobjekt der klasse java.util.Date
     * @return ein als String einfach formatiertes Datum
     */
    public static String toDD_MM_YYYY_String(Date datum)  {
        if (datum == null) return "";
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
        return dateFormatter.format(datum);
    }
}
