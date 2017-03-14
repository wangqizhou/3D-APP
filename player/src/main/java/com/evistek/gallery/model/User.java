package com.evistek.gallery.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户对象
 */
public class User implements Serializable{

    private static final long serialVersionUID = -4627185082611952306L;
    private int id;
    private String username;
    private String password;
    private String phone;
    private String email;
    private String nickname;
    private Date registerTime;
    private String location;
    private String sex;
    private String figureUrl;
    private String source;
    private String phoneDevice;
    private String phoneSystem;
    private String vrDevice;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Date getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(Date registerTime) {
        this.registerTime = registerTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getFigureUrl() {
        return figureUrl;
    }

    public void setFigureUrl(String figureUrl) {
        this.figureUrl = figureUrl;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPhoneDevice() {
        return phoneDevice;
    }

    public void setPhoneDevice(String phoneDevice) {
        this.phoneDevice = phoneDevice;
    }

    public String getPhoneSystem() {
        return phoneSystem;
    }

    public void setPhoneSystem(String phoneSystem) {
        this.phoneSystem = phoneSystem;
    }

    public String getVrDevice() {
        return vrDevice;
    }

    public void setVrDevice(String vrDevice) {
        this.vrDevice = vrDevice;
    }

    /**
     * 无参构造
     */
    public User() {
    }

    public User(String username, String password, String phone, String email) {
        super();
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.email = email;
    }

    /**
     * 构造重载
     */

    public User(String username, String password, String phone, String email, String location, String sex) {
        super();
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.location = location;
        this.sex = sex;
    }

    public User(String username, String password, String phone, String email, String nickname, Date registerTime
            , String location, String sex, String figureUrl, String source, String phoneDevice, String phoneSystem, String vrDevice) {
        super();
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.nickname = nickname;
        this.registerTime = registerTime;
        this.location = location;
        this.sex = sex;
        this.figureUrl = figureUrl;
        this.source = source;
        this.phoneDevice = phoneDevice;
        this.phoneSystem = phoneSystem;
        this.vrDevice = vrDevice;
    }

    public User(int id, String password, String nickname, String location, String sex, String figureUrl, String source) {
        this.id = id;
        this.password = password;
        this.nickname = nickname;
        this.location = location;
        this.sex = sex;
        this.figureUrl = figureUrl;
        this.source = source;
    }

    public User(String username, String password, String nickname, String location, String sex, String figureUrl, String source) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.location = location;
        this.sex = sex;
        this.figureUrl = figureUrl;
        this.source = source;
    }

    public User(int id, String username, String password, String phone, String email, String nickname, Date registerTime
            , String location, String sex, String figureUrl, String source, String phoneDevice, String phoneSystem, String vrDevice) {
        super();
        this.id = id;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.nickname = nickname;
        this.registerTime = registerTime;
        this.location = location;
        this.sex = sex;
        this.figureUrl = figureUrl;
        this.source = source;
        this.phoneDevice = phoneDevice;
        this.phoneSystem = phoneSystem;
        this.vrDevice = vrDevice;
    }
}