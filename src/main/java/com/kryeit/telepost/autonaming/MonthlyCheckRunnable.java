package com.kryeit.telepost.autonaming;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;

public class MonthlyCheckRunnable implements Runnable {
    private static final String FILE_PATH = "lastCheckedMonth.txt";
    private int lastCheckedMonth;

    public MonthlyCheckRunnable() {
        lastCheckedMonth = readLastCheckedMonthFromFile();
    }

    @Override
    public void run() {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);

        if (currentMonth != lastCheckedMonth) {

            // Month changed, run automatic naming and run /setworldspawn command
            AutonamingUtils.autonamePost();

            lastCheckedMonth = currentMonth;
            writeLastCheckedMonthToFile(currentMonth);
        }
    }

    private int readLastCheckedMonthFromFile() {
        Path path = Paths.get(FILE_PATH);
        try {
            if (!Files.exists(path)) {
                // If the file doesn't exist, create it with the current month
                int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
                writeLastCheckedMonthToFile(currentMonth);
                return currentMonth;
            } else {
                // If the file exists, read the last checked month
                String content = new String(Files.readAllBytes(path));
                return Integer.parseInt(content.trim());
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            // In case of an error, return the current month and write it to the file
            int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
            writeLastCheckedMonthToFile(currentMonth);
            return currentMonth;
        }
    }

    private void writeLastCheckedMonthToFile(int month) {
        try {
            Files.write(Paths.get(FILE_PATH), String.valueOf(month).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
