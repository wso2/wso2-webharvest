package org.webharvest.utils;

import org.webharvest.exception.*;

import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Properties;
import java.io.File;
import java.sql.*;
import java.util.logging.Logger;

/**
 * Class loading utility - used for loading JDBC driver classes and plugin classes.
 */
public class ClassLoaderUtil {

    private static class DriverShim implements Driver {
        private Driver driver;

        DriverShim(Driver d) {
            this.driver = d;
        }
        public boolean acceptsURL(String u) throws SQLException {
            return this.driver.acceptsURL(u);
        }
        public Connection connect(String u, Properties p) throws SQLException {
            return this.driver.connect(u, p);
        }
        public int getMajorVersion() {
            return this.driver.getMajorVersion();
        }
        public int getMinorVersion() {
            return this.driver.getMinorVersion();
        }
        public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
            return this.driver.getPropertyInfo(u, p);
        }
        public boolean jdbcCompliant() {
            return this.driver.jdbcCompliant();
        }
        public Logger getParentLogger() {
            return null;
        }
    }

    // class loader that insludes all JAR libraries in the working folder of the application. 
    private static URLClassLoader rootClassLoader = null;

    /**
     * Lists all JARs in the working folder (folder of WebHarvest executable)
     */
    private static void defineRootLoader() {
        java.util.List urls = new ArrayList();
        String rootDirPath = new File("").getAbsolutePath();

        try {
            urls.add(new File("").toURI().toURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // add all JAR files from the root folder to the class path
        File[] entries = new File(rootDirPath).listFiles();
        if (entries != null) {
            for (int f = 0; f < entries.length; f++) {
                File entry = entries[f];
                if ( entry != null && !entry.isDirectory() && entry.getName().toLowerCase().endsWith(".jar") ) {
                   try {
                       String jarAbsolutePath = entry.getAbsolutePath();
                       urls.add( new URL("jar:file:/" + jarAbsolutePath.replace('\\', '/') + "!/") );
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        URL urlsArray[] = new URL[urls.size()];
        for (int i = 0; i < urls.size(); i++) {
            urlsArray[i] = (URL) urls.get(i);
        }

        rootClassLoader = new URLClassLoader(urlsArray);
    }

    public static void registerJDBCDriver(String driverClassName)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException {
        if (rootClassLoader == null) {
            defineRootLoader();
        }
        
        Driver driver = (Driver)Class.forName(driverClassName, true, rootClassLoader).newInstance();
        DriverManager.registerDriver(new DriverShim(driver));
    }

    public static Class getPluginClass(String fullClassName) throws PluginException {
        try {
            return Class.forName(fullClassName);
        } catch (ClassNotFoundException e) {
            throw new PluginException("Error finding plugin class \"" + fullClassName + "\": " + e.getMessage(), e);
        } catch (NoClassDefFoundError e) {
            throw new PluginException("Error finding plugin class \"" + fullClassName + "\": " + e.getMessage(), e);
        }
    }

}