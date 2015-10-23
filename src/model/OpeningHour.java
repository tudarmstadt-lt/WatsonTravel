package model;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OpeningHour {

    Date openFrom;
    Date openUntil;

    public OpeningHour() {

    }

    public OpeningHour(Date openFrom, Date openUntil) {
        this.openFrom = openFrom;
        this.openUntil = openUntil;
    }

    public boolean isInOpeningHour(Date date) {
        return !(openFrom == null || openUntil == null) && (openFrom.compareTo(date) <= 0 && openUntil.compareTo(date) >= 0);
    }

    @Override
    public String toString() {
        DateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        return dateFormat.format(openFrom) + " - " + dateFormat.format(openUntil);
    }
}
