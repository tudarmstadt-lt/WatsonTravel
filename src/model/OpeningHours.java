package model;


import java.util.Calendar;

public class OpeningHours {

    private OpeningHour monday;
    private OpeningHour tuesday;
    private OpeningHour wednesday;
    private OpeningHour thursday;
    private OpeningHour friday;
    private OpeningHour saturday;
    private OpeningHour sunday;

    public OpeningHour getMonday() {
        return monday;
    }

    public void setMonday(OpeningHour monday) {
        this.monday = monday;
    }

    public OpeningHour getTuesday() {
        return tuesday;
    }

    public void setTuesday(OpeningHour tuesday) {
        this.tuesday = tuesday;
    }

    public OpeningHour getWednesday() {
        return wednesday;
    }

    public void setWednesday(OpeningHour wednesday) {
        this.wednesday = wednesday;
    }

    public OpeningHour getThursday() {
        return thursday;
    }

    public void setThursday(OpeningHour thursday) {
        this.thursday = thursday;
    }

    public OpeningHour getFriday() {
        return friday;
    }

    public void setFriday(OpeningHour friday) {
        this.friday = friday;
    }

    public OpeningHour getSaturday() {
        return saturday;
    }

    public void setSaturday(OpeningHour saturday) {
        this.saturday = saturday;
    }

    public OpeningHour getSunday() {
        return sunday;
    }

    public void setSunday(OpeningHour sunday) {
        this.sunday = sunday;
    }

    public OpeningHour getCurrentOpeningHour() {
        Calendar calendar = Calendar.getInstance();
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY: return getMonday();
            case Calendar.TUESDAY: return getTuesday();
            case Calendar.WEDNESDAY: return getWednesday();
            case Calendar.THURSDAY: return getThursday();
            case Calendar.FRIDAY: return getFriday();
            case Calendar.SATURDAY: return getSaturday();
            case Calendar.SUNDAY: return getSunday();
        }
        return null;
    }
}
