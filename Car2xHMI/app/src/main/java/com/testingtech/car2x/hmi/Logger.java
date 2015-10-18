package com.testingtech.car2x.hmi;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {

    public static PrintWriter writer;
    private static Logger instance;

    public static Logger getInstance() {
        if (Logger.instance == null)
            Logger.instance = new Logger();
        return Logger.instance;
    }

    private Logger() {
        initLogFile();
    }

    public void initLogFile() {
        File path = Globals.mainActivity.getExternalFilesDir(null);
//        File path = Globals.mainActivity.getFilesDir();
        File file = new File(path, "Log.txt");
        try {
            Logger.writer = new PrintWriter(file);
        } catch (IOException ioe) {
            Logger.writer = new PrintWriter(System.err);
            ioe.printStackTrace(Logger.writer);
        }
        writeLog("LOGGER: Started");
    }

    public static void writeLog(String msg) {
        writer.println(msg);
        writer.flush();
    }
}
