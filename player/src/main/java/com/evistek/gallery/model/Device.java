package com.evistek.gallery.model;

import java.util.Date;

public class Device {
    private int id;
    private String model;
    private String system;
    private String location;
    private String client;
    private String clientVersion;
    private Date accessTime;
    private String imei;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public Date getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(Date accessTime) {
        this.accessTime = accessTime;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    /**
     * 无参构造
     */
    public Device() {
    }

    public Device(int id, String model, String system, String location
            , String client, String clientVersion, Date accessTime, String imei) {
        super();
        this.id = id;
        this.model = model;
        this.system = system;
        this.location = location;
        this.client = client;
        this.clientVersion = clientVersion;
        this.accessTime = accessTime;
        this.imei = imei;
    }

    /**
     * 有参构造
     */


    @Override
    public String toString() {
        return "Device [id=" +id+", model=" +model+ ",system=" +system+ ",location=" +location+ ",client=" +client+
                ",clientVersion=" +clientVersion+",accessTime=" +accessTime+",imei=" +imei+"]";
    }
}