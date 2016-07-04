// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.czechaddress.parser;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.czechaddress.DatabaseLoadException;
import org.openstreetmap.josm.plugins.czechaddress.addressdatabase.Database;

/**
 * General superclass for any parser capable of filling the database.
 *
 * @author Radomír Černoch, raodmir.cernoch@gmail.com
 * @see Database
 */
public abstract class DatabaseParser {

    /** Directory, where the parser can store cache data. */
    protected String storageDir;

    /**
     * Sets the storage directory.
     */
    public void setStorageDir(String storageDir) {
        this.storageDir = storageDir;
    }

    /** Target Database, which should be filled by the parser. */
    protected Database target = null;

    /**
     * Sets the target database, which should be filled by the parser.
     */
    public void setTargetDatabase(Database targetDatabase) {
        target = targetDatabase;
    }

    /**
     * The ultimate method, which starts filling the database.
     *
     * @throws DatabaseLoadException if anything goes wrong...
     */
    public void fillDatabase() throws DatabaseLoadException {

        assert target != null;

        if (!(new File(getDatabasePath())).exists()) {
            Main.info("CzechAddress: Soubor s databází českých adres na disku nenalazen. Pokusím se jej stáhnout.");
            downloadDatabase();
        }

        parseDatabase();
    }

    /**
     * The internal method, which does the actual parsing.
     *
     * @throws DatabaseLoadException should be thrown if anything gets wrong...
     */
    protected abstract void parseDatabase() throws DatabaseLoadException;

    /**
     * Returns a URL, where the database can be dowloaded from. This method
     * is used in the {@code dowloadDatabase()} method.
     */
    protected abstract String getDatabaseUrl();

    /**
     * Returns a path, where the database can be stored on local disk.
     */
    protected abstract String getDatabasePath();

    /**
     * Opens the database and provides an input stream to its content.
     *
     * @throws DatabaseLoadException should be thrown when the input cannot be opened.
     */
    protected abstract InputStream getDatabaseStream() throws DatabaseLoadException;

    /**
     * Downloads the database from remote URL.
     *
     * The URL is provided by {@code getDatabaseUrl()} method.
     *
     * @throws DatabaseLoadException if any error occurs during the download.
     */
    protected void downloadDatabase() throws DatabaseLoadException {

        try {

            URL url = new URL(getDatabaseUrl());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            //Set timeout for 30 seconds.
            con.setReadTimeout(30000);
            con.connect();

            Main.info("CzechAddress: Stahuji: " + url.toString());
            Main.info("CzechAddress: Server vrátil: " + con.getResponseMessage());

            // Check the status error code from server
            if (con.getResponseCode() != 200)
                throw new DatabaseLoadException(
                        "Požadavek na server selhal, číslo chyby: " + String.valueOf(con.getResponseCode()));

            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(getDatabasePath()))) {
                int total = 0, count;
                byte[] buffer = new byte[1024*512];
                while ((count = con.getInputStream().read(buffer)) >= 0) {
                    bos.write(buffer, 0, count);
                    total += count;
                    Main.info("CzechAddress: MVČR database: " + String.valueOf(total/1024) + " kb downloaded.");
                }
            }

            // Look for a detailed error message from the server
            if (con.getHeaderField("Error") != null) {
                String er = con.getHeaderField("Error");
                throw new DatabaseLoadException("Server vrátil chybu: " + er);
            }
            con.disconnect();

        } catch (IOException ioexp) {
            throw new DatabaseLoadException("Chyba při načítání databáze", ioexp);
        }
    }
}
