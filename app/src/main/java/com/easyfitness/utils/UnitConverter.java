package com.easyfitness.utils;

public class UnitConverter {

    public static final int UNIT_KG = 0;
    public static final int UNIT_LBS = 1;

    public UnitConverter() {
    }

   
    static public float weightConverter(float pWeight, int pUnitIn, int pUnitOut) {
        switch (pUnitIn) {
            case UNIT_KG:
                switch (pUnitOut) {
                    case UNIT_LBS:
                        return KgtoLbs(pWeight);
                    case UNIT_KG:
                    default:
                        return pWeight;
                }
            case UNIT_LBS:
                switch (pUnitOut) {
                    case UNIT_KG:
                        return LbstoKg(pWeight);
                    case UNIT_LBS:
                    default:
                        return pWeight;
                }
            default:
                return pWeight;
        }
    }


    static public float KgtoLbs(float pKg) {
        return pKg / (float) 0.45359237;
    }


    static public float LbstoKg(float pLbs) {
        return pLbs * (float) 0.45359237;
    }


    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";


        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (hours > 0) {
            finalTimerString = hours + ":";
        }


        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;


        return finalTimerString;
    }


    public int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);


        percentage = (((double) currentSeconds) / totalSeconds) * 100;


        return percentage.intValue();
    }

    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = totalDuration / 1000;
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);


        return currentDuration * 1000;
    }
}
