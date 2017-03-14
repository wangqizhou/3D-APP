package com.evistek.gallery.net;

import com.evistek.gallery.activity.E3DApplication;

public class Config {
    // configure for update server setting.
    /*
     * The sample of version.xml, you can refer to /src/version.xml file. it
     * should includes 4 items, 'version', 'name', 'url' & 'info' we prefer use
     * the version.xml on network, not local
     */
    public static final String NET_SERVER_IP = "192.168.1.111";
    public static final String NET_SERVER_ADDRESS = "http://" + NET_SERVER_IP + ":8080/" + "version/version.xml";

    public static final int NET_CONNECT_TIMEOUT = 10000;
    public static final int NET_READ_TIMEOUT = 20000;

    // NetWork
    public static final String HOST = "http://" + NET_SERVER_IP + ":8080/EvistekMediaServerDev";
    public static final String URL_LIST = HOST + "/list";
    public static final String URL_LIST_BY_USER = HOST + "/listByUser";
    public static final String URL_DEVICE = HOST + "/device";
    public static final String URL_REGISTER = HOST + "/register";
    public static final String URL_REGISTER_CHALLENGE = HOST + "/register/challenge";
    public static final String URL_LOGIN = HOST + "/login";
    public static final String URL_LOGIN_CHALLENGE = HOST + "/login/challenge";
    public static final String URL_GET_CONTENT_COMMENT = HOST + "/getContentComment";
    public static final String URL_GET_USER_COMMENT = HOST + "/getUserComment";
    public static final String URL_SEND_COMMENT = HOST + "/sendComment";
    public static final String URL_UPLOAD = HOST + "/upLoad";
    public static final String URL_DOWNLOAD = HOST + "/download";
    public static final String URL_LIST_RECOMMEND = HOST + "/listVideoRecommend";
    public static final String URL_LIST_USERNAME = HOST + "/listUserName";
    public static final String URL_LIST_CONTENT = HOST + "/listContents";
    public static final String URL_USERNAME = HOST + "/getUserByName";
    public static final String URL_UPDATEUSER = HOST + "/updateUserInfo";
    public static final String URL_VRLIST = HOST + "/categoryVRList";
    public static final String URL_EXCEPTVRLIST = HOST + "/categoryExceptVRList";
    public static final String URL_GET_APPLICATION = HOST + "/getApp";
    public static final String URL_APP_DOWNLOAD = HOST + "/appDownload";
    public static final String URL_ADD_PLAY_RECORD = HOST + "/addPlayRecord";
    public static final String URL_UPDATE_DURATION = HOST + "/updatePlayDuration";
    public static final String URL_GET_PLAY_RECORD = HOST + "/getPlayRecordsByUserId";
    public static final String URL_ADD_FAVORITE = HOST + "/addFavorite";
    public static final String URL_GET_FAVORITE = HOST + "/getFavoritesByUserId";
    public static final String URL_DELETE_PLAY_RECORD = HOST + "/deletePlayRecordBatch";
    public static final String URL_DELETE_FAVORITES = HOST + "/deleteFavoriteBatch";
    public static final String URL_DELETE_FAVORITE = HOST + "/deleteFavoriteInContent";
    public static final String URL_ADD_LOGIN_RECORD = HOST + "/addLoginRecord";
    public static final String URL_GET_VIDEO_BY_ID = HOST + "/getVideoById";
    public static final String URL_GET_ALL_ADS = HOST + "/getAllAds";

    public static final String APP = "http://" + NET_SERVER_IP + ":8080" + "/api/v2";
    public static final String APP_VIDEOS = APP + "/videos";
    public static final String APP_IMAGES = APP + "/images";
    public static final String APP_CATEGORIES = APP + "/categories";
    public static final String APP_REGISTER = APP + "/users";
    public static final String APP_LOGIN = APP + "/users";
    public static final String APP_PRODUCT = APP + "/products";
    public static final String APP_DEVICE = APP + "/devices";
    public static final String APP_PLAYRECORD = APP + "/play_records";
    public static final String APP_FAVORITE = APP + "/favorites";

    //cache setting
    public static final String DISK_CACHE_PATH = E3DApplication.getInstance().getExternalCacheDir().getAbsolutePath() + "/diskCache/";
    public static final int MEMORY_CACHE_SIZE = 1024 * 1024 * 24;   //24M
    public static final int DISK_CACHE_SIZE = 1024 * 1024 * 200;    //200M

    //paging
    public static final int PAGE_SIZE = 40;
    public static final int FIRST_PAGE = 1;
    public static final int CHARACTER_LIMIT = 5;

    //local image
    public static final int IMG_MIN_WIDTH = 8;
    public static final int IMG_MIN_HIGHT = 8;
}
