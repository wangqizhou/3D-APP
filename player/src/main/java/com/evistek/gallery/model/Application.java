package com.evistek.gallery.model;

import java.io.Serializable;
import java.util.Date;

public class Application implements Serializable {
    private Integer id;
    private String name;
    private String iconUrl;
    private String url;
    private String intro;
    private String version;
    private Date updateDate;
    private int downloadCount;
    private int rating;
    private String author;
    private int size;
    private String preview1Url;
    private String preview2Url;
    private String preview3Url;

    public Integer getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getIconUrl() {
        return iconUrl;
    }
    public String getUrl() {
        return url;
    }
    public String getIntro() {
        return intro;
    }
    public String getVersion() {
        return version;
    }
    public Date getUpdateDate() {
        return updateDate;
    }
    public int getDownloadCount() {
        return downloadCount;
    }
    public int getRating() {
        return rating;
    }
    public String getAuthor() {
        return author;
    }
    public int getSize() {
        return size;
    }
    public String getPreview1Url() {
        return preview1Url;
    }
    public String getPreview2Url() {
        return preview2Url;
    }
    public String getPreview3Url() {
        return preview3Url;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public void setIntro(String intro) {
        this.intro = intro;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }
    public void setRating(int rating) {
        this.rating = rating;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public void setSize(int size) {
        this.size = size;
    }
    public void setPreview1Url(String preview1Url) {
        this.preview1Url = preview1Url;
    }
    public void setPreview2Url(String preview2Url) {
        this.preview2Url = preview2Url;
    }
    public void setPreview3Url(String preview3Url) {
        this.preview3Url = preview3Url;
    }
}
