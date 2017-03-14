package com.evistek.gallery.model;

import java.io.Serializable;

/**
 * Created by evis on 2016/8/17.
 */
public class LocalVideo implements Serializable {
    private static final long serialVersionUID = 7639315018319524648L;
    public long id;
    public String path;
    public String title;
    public String displayName;
    public long size;
    public long date;
    public long duration;
    public int width;
    public int height;
    public String thumbnail;
}
