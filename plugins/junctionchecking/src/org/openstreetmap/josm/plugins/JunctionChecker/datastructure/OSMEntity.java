// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.JunctionChecker.datastructure;

import java.util.HashMap;

/**
 * Basisklasse für die 3 OSM-Objekte Node, Way und Relation
 * @author  joerg
 */
public class OSMEntity {

    //TODO Idee: alle Wertestandardmäßig auf -1 setzen, so muß
    //man bei der Umwandlung nicht auf null-Werte prüfen
    /**
     * @uml.property  name="id"
     */
    private long id;
    /**
     * @uml.property  name="visible"
     */
    private boolean visible;
    /**
     * @uml.property  name="timestamp"
     */
    private String timestamp;
    /**
     * @uml.property  name="user"
     */
    private String user;
    /**
     * @uml.property  name="uid"
     */
    private int uid;
    /**
     * @uml.property  name="changeset"
     */
    private int changeset;
    /**
     * @uml.property  name="hashmap"
     */
    private HashMap<String, String> hashmap = new HashMap<>();
    /**
     * @uml.property  name="version"
     */
    private int version;

    public void setversion(int version) {
        this.version = version;
    }

    /**
     * @uml.property  name="version"
     */
    public int getVersion() {
        return version;
    }

    /**
     * @uml.property  name="id"
     */
    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @uml.property  name="visible"
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @uml.property  name="visible"
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * @uml.property  name="timestamp"
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * @uml.property  name="timestamp"
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @uml.property  name="user"
     */
    public String getUser() {
        return user;
    }

    /**
     * @uml.property  name="user"
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @uml.property  name="uid"
     */
    public int getUid() {
        return uid;
    }

    /**
     * @uml.property  name="uid"
     */
    public void setUid(int uid) {
        this.uid = uid;
    }

    /**
     * @uml.property  name="changeset"
     */
    public int getChangeset() {
        return changeset;
    }

    /**
     * @uml.property  name="changeset"
     */
    public void setChangeset(int changeset) {
        this.changeset = changeset;
    }

    /**
     * @uml.property  name="hashmap"
     */
    public HashMap<String, String> getHashmap() {
        return hashmap;
    }

    /**
     * @uml.property  name="hashmap"
     */
    public void setHashmap(HashMap<String, String> hashmap) {
        this.hashmap = hashmap;
    }

    public void setKeyValue(String key, String value) {
        hashmap.put(key, value);
    }

    public String getValue(String key) {
        return hashmap.get(key);
    }

    public boolean hasKey(String key) {
        return hashmap.containsKey(key);
    }

    /**
     * prüft, ob der übergebene String als Wert existiert
     */
    public boolean hasValue(String value) {
        return hashmap.containsValue(value);
    }

    protected String valuestoString() {
        return ("ID: " + id + "\n" + "User: " + user + "\n");
    }
}
