/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.testingtech.car2x.hmi.properties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 *
 * http://crunchify.com/java-properties-file-how-to-read-config-properties-values-in-java/
 * @author Rob
 * TODO: Comments Property Reader class
 * @
 */
public final class PropertyReader {
  private static final String propertyFilePath = "config/";
  private static final String propertyFile = "config.properties";
    
/**
 * 
 * @param property key, which should be looked up.
 * @return Property value, if property key is in property list. 
 *         If the key is not found there, null is returned.
 * @throws java.io.IOException, if the properties file is not found.
 *         Make sure, that the file is in the path resources/config/config.properties.
 */
  public static final String readProperty(final String property) {  
    try {
      final Properties tempProp = loadPropertyFile();
      final String propertyValue = tempProp.getProperty(property);

      return propertyValue;
      
    } catch (IOException e) {
      // return null, if properties file is not found
      e.printStackTrace();
    }
    
    return null;
  }
  
  
  private static final Properties loadPropertyFile() throws IOException {
    final Properties properties = new Properties();
    final InputStream inputStream = PropertyReader.class.getClassLoader().
                                               getResourceAsStream(propertyFilePath + propertyFile);
      
    if (inputStream != null) {
      properties.load(inputStream);
    } else {
      throw new FileNotFoundException("Property file not found. Expected path: resources/" 
                                                                + propertyFilePath + propertyFile);
    }
      
    return properties;
  }

}
