package com.example.memorymastermind;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Common {
    public static String convertTimestampToDateTime(long timestampMillis) {
        Date date = new Date(timestampMillis);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

        return dateFormat.format(date);
    }
}
