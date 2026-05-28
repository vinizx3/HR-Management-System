package com.myapp.rh.timeclock.entity;

public class WorkTimeCalculator {

    private static final int WORKDAY_MINUTES = 480;
    private static final int TOLERANCE_MINUTES = 10;

    private WorkTimeCalculator() {}

    public static int calculateOvertime(long minutesWorked) {
        if (minutesWorked > WORKDAY_MINUTES + TOLERANCE_MINUTES) {
            return (int) (minutesWorked - WORKDAY_MINUTES);
        }
        return 0;
    }

    public static String formatMinutes(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        return hours + "h " + mins + "min";
    }
}

