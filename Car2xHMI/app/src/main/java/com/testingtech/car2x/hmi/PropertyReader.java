package com.testingtech.car2x.hmi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * TODO: Comments Property Reader class
 */
public final class PropertyReader {

    private static Properties properties;

    /**
     * @param property key, which should be looked up.
     * @return Property value, if property key is in property list
     */
    public static String readProperty(String property) {
        return properties.getProperty(property);
    }

    public static void loadPropertyFile() {
        try {
//            File path = Globals.mainActivity.getFilesDir();
            File path = Globals.mainActivity.getExternalFilesDir(null);
            File file = new File(path, "config.properties");
            properties = new Properties();
            final InputStream inputStream = new FileInputStream(file);
            properties.load(inputStream);
        } catch (IOException ioe) {
            ioe.printStackTrace(Logger.writer);
        }
    }

    public static void loadPropertyFile(String path) {
        try {
            properties = new Properties();
            properties.load(new FileInputStream(path));
        } catch (IOException ioe) {
            ioe.printStackTrace(Logger.writer);
        }
    }

}