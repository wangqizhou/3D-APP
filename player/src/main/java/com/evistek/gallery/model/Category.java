package com.evistek.gallery.model;

import java.io.Serializable;

public class Category implements Serializable{

    private static final long serialVersionUID = 7531154184743238657L;
    private int id;
    private String name;
    private String type;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * 无参构造
     */
    public Category() {
    }

    /**
     * 有参构造
     */

    public Category(String name, String type) {
        super();
        this.name = name;
        this.type = type;
    }

    /**
     * 重载构造
     */

    public Category(int id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }
}