package com.evistek.gallery.model;

import java.io.Serializable;

public class LocalImage implements Serializable{
    private static final long serialVersionUID = 6410562558210044978L;
    public long id;
    public String path;
    public String title;
    public String name;
    public long size;
    public int width;
    public int height;
    public long date;
    public String thumbnail;
}
