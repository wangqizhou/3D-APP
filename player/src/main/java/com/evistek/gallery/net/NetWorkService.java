package com.evistek.gallery.net;

import android.util.Log;

import com.evistek.gallery.activity.E3DApplication;
import com.evistek.gallery.model.Category;
import com.evistek.gallery.model.Device;
import com.evistek.gallery.model.Favorite;
import com.evistek.gallery.model.FavoriteModel;
import com.evistek.gallery.model.Image;
import com.evistek.gallery.model.LoginInfo;
import com.evistek.gallery.model.PlayRecord;
import com.evistek.gallery.model.PlayRecordModel;
import com.evistek.gallery.model.Product;
import com.evistek.gallery.model.User;
import com.evistek.gallery.model.UserModel;
import com.evistek.gallery.model.Video;
import com.evistek.gallery.net.callback.AdsCallback;
import com.evistek.gallery.net.callback.ApplicationCallback;
import com.evistek.gallery.net.callback.CategoryCallback;
import com.evistek.gallery.net.callback.ContentCallBack;
import com.evistek.gallery.net.callback.ContentCommentCallback;
import com.evistek.gallery.net.callback.DeviceCallback;
import com.evistek.gallery.net.callback.FavoriteCallback;
import com.evistek.gallery.net.callback.FavoriteListCallback;
import com.evistek.gallery.net.callback.ImageCallback;
import com.evistek.gallery.net.callback.LoginCallback;
import com.evistek.gallery.net.callback.PlayRecordCallback;
import com.evistek.gallery.net.callback.PlayRecordListCallback;
import com.evistek.gallery.net.callback.RegisterCallback;
import com.evistek.gallery.net.callback.SendCommentCallback;
import com.evistek.gallery.net.callback.UserCallBack;
import com.evistek.gallery.net.callback.UserCommentCallback;
import com.evistek.gallery.net.callback.UserNameCallBack;
import com.evistek.gallery.net.callback.UserNameListCallBack;
import com.evistek.gallery.net.callback.VideoCallback;
import com.evistek.gallery.net.json.JsonReqApplication;
import com.evistek.gallery.net.json.JsonReqContent;
import com.evistek.gallery.net.json.JsonReqContentComment;
import com.evistek.gallery.net.json.JsonReqDownload;
import com.evistek.gallery.net.json.JsonReqList;
import com.evistek.gallery.net.json.JsonReqSendComment;
import com.evistek.gallery.net.json.JsonReqUserComment;
import com.evistek.gallery.net.json.JsonReqUserName;
import com.evistek.gallery.net.json.JsonReqUserResource;
import com.evistek.gallery.net.json.JsonRespApplication;
import com.evistek.gallery.net.json.JsonRespContent;
import com.evistek.gallery.net.json.JsonRespContentComment;
import com.evistek.gallery.net.json.JsonRespUserName;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;

public class NetWorkService {
    //The Dateformat of Gson is the same with the server.
    private static Gson mGson = new GsonBuilder().setDateFormat("MMM dd, yyyy hh:mm:ss aa").create();
    private static final String TAG = "NetWorkService";
    public static final MediaType MEDIA_TYPE_JSON
            = MediaType.parse("application/json; charset=utf-8");
    public static OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
            .connectTimeout(500, TimeUnit.MILLISECONDS)
            .readTimeout(500, TimeUnit.MILLISECONDS)
            .writeTimeout(500, TimeUnit.MILLISECONDS)
            .build();

    /**
     * 用户注册
     *
     * @param user
     * @param callback
     */
    public static void register(final User user, final LoginCallback callback) {
        String url = Config.APP_REGISTER;
        List<String> pathList = new ArrayList<>();
        pathList.add("register");
        OkHttpWrapper.getInstance().postAsync(url, null, pathList, user, new OkHttpWrapper.RequestCallBack<String>() {
                    @Override
                    public void onResponse(int code, String response) {
                            if (code == 200) {
                                String challenge = mGson.fromJson(response, String.class);
                                Log.e(TAG, "challenge: " + challenge);

                                registerChallenge(user, challenge, new LoginCallback() {
                                    @Override
                                    public void onResult(int code, String msg, User respLogin) {
                                        if (code == 200) {
                                            callback.onResult(code, "注册成功！", respLogin);
                                        }
                                    }
                                });
                            } else {
                                callback.onResult(code, user.getUsername() + " 已存在.", null);
                                Log.e(TAG, "error code: " + code);
                            }
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        Log.e(TAG, "login error: " + code + " msg: " + msg);
                    }
                });
    }

    /**
     * 用户登录
     *
     * @param userName
     * @param passWord
     * @param callback
     */
    public static void login(final String userName, final String passWord, final LoginCallback callback) {
        String url = Config.APP_LOGIN;
        List<String> pathList = new ArrayList<>();
        pathList.add("login");
        pathList.add(userName);
        OkHttpWrapper.getInstance().getAsync(url, null, pathList,new OkHttpWrapper.RequestCallBack<String>() {
                    @Override
                    public void onResponse(int code, String response) {
                        if (code == 200) {
                            String mChallenge = mGson.fromJson(response, String.class);
                            loginChalleng(userName, passWord, mChallenge,
                                    new LoginCallback() {
                                        @Override
                                        public void onResult(int code, String msg, User respLogin) {
                                            if (code == 200) {
                                                callback.onResult(code, "登陆成功.", respLogin);
                                            } else if (code == 401) {
                                                callback.onResult(code, "用户名和密码不匹配.", null);
                                            } else {
                                                callback.onResult(code, "please check  your net.", null);
                                            }
                                        }
                                    });

                        } else if (code == 404) {
                            callback.onResult(code, "您输入的用户名不存在.", null);
                        }
                    }

                    @Override
                    public void onFailure(int code, String msg) {

                    }
                });
    }

    /**
     * 获取某个类型的Category
     *
     * @param contentType
     * @param callback
     */
    public static void getCateGoryList(String contentType, final CategoryCallback callback) {
        String url = null;
        if (contentType.equals("all")){
            url = Config.APP_CATEGORIES;
        } else {
            url = Config.APP_CATEGORIES + "/type/" + contentType;
        }
        OkHttpWrapper.getInstance().getAsync(url, null, null, new OkHttpWrapper.RequestCallBack<List<Category>>() {
                    @Override
                    public void onResponse(int code, List<Category> response) {
//                        Log.e("ZWX", "onResponse2: "
//                                + " pageNo: " + response.getPageNo()
//                                + " pageSize: " + response.getPageSize()
//                                + " resultSize: " + response.getResults().size());
                        callback.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 获取某个类型的和VR相关的category
     */
    public static void getVRCategoryList(String contentType, final CategoryCallback callback) {
        OkHttpWrapper.getInstance().postAsync(Config.URL_VRLIST, null, null, "", new OkHttpWrapper.RequestCallBack<List<Category>>() {
                    @Override
                    public void onResponse(int code, List<Category> response) {
                        callback.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 获取某个类型的和VR无关的category
     */
    public static void getCategoryListExceptVR(String contentType, final CategoryCallback callback) {
        OkHttpWrapper.getInstance().postAsync(Config.URL_EXCEPTVRLIST, null, null, "", new OkHttpWrapper.RequestCallBack<List<Category>>() {
                    @Override
                    public void onResponse(int code, List<Category> response) {
                        callback.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 获取 Image List
     *
     * @param pageNo
     * @param pageSize
     * @param categoryId
     * @param callback
     */
    public static void getImageList(int pageNo, int pageSize, int categoryId, final ImageCallback callback) {
        String url = Config.APP_IMAGES;
        List<String> pathList = new ArrayList<>();
        pathList.add("category_id");
        pathList.add(String.valueOf(categoryId));
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("page", String.valueOf(pageNo));
        parameterMap.put("page_size", String.valueOf(pageSize));
        OkHttpWrapper.getInstance().getAsync(url, parameterMap, pathList, new OkHttpWrapper.RequestCallBack<List<Image>>() {
                    @Override
                    public void onResponse(int code, List<Image> response) {
                        callback.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 获取Recommend video List
     *
     * @param pageNo
     * @param pageSize
     */
    public static void getRecommendVideoList(int pageNo, int pageSize, final VideoCallback callback) {
        JsonReqList reqJson = new JsonReqList();
        reqJson.setName("recommendVideoList");
        reqJson.setPageNo(pageNo);
        reqJson.setPageSize(pageSize);
        OkHttpWrapper.getInstance().postAsync(Config.URL_LIST_RECOMMEND, null, null, "", new OkHttpWrapper.RequestCallBack<List<Video>>() {
                    @Override
                    public void onResponse(int code, List<Video> response) {
                        callback.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 获取userName List
     */
    public static void getUserNameList(ArrayList<Integer> userIdList, final UserNameListCallBack callback) {
        JsonReqUserName reqJson = new JsonReqUserName();
        reqJson.setName("userNameList");
        reqJson.setUserIdList(userIdList);
        OkHttpWrapper.getInstance().postAsync(Config.URL_LIST_USERNAME, null, null, "", new OkHttpWrapper.RequestCallBack<JsonRespUserName>() {
                    @Override
                    public void onResponse(int code, JsonRespUserName response) {
                        callback.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    public static void getUserByUserName(String userName, final UserNameCallBack callback) {
        String url = Config.APP_LOGIN;
        List<String> pathList = new ArrayList<>();
        pathList.add("name");
        pathList.add(userName);
        OkHttpWrapper.getInstance().getAsync(url, null, pathList, new OkHttpWrapper.RequestCallBack<UserModel>() {
                    @Override
                    public void onResponse(int code, UserModel response) {
                        callback.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    public static void updateUserInfo(User userInfo, final UserCallBack callback) {

        OkHttpWrapper.getInstance().putAsync(Config.APP_LOGIN, userInfo, new OkHttpWrapper.RequestCallBack<String>() {
                    @Override
                    public void onResponse(int code, String response) {
                        callback.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
    * 添加用户登录信息
     * * */
    public static void addLoginRecord (int userId,int status, final UserCallBack callback) {
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setUserId(userId);
        loginInfo.setStatus(status);
        OkHttpWrapper.getInstance().postAsync(Config.URL_ADD_LOGIN_RECORD, null, null, "", new OkHttpWrapper.RequestCallBack<String>() {
                    @Override
                    public void onResponse(int code, String response) {
                        callback.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    public static void getContentList(ArrayList<Integer> contentIdList, final ContentCallBack callback) {
        JsonReqContent reqJson = new JsonReqContent();
        reqJson.setName("contentList");
        reqJson.setContentIdList(contentIdList);
        OkHttpWrapper.getInstance().postAsync(Config.URL_LIST_CONTENT, null, null, "", new OkHttpWrapper.RequestCallBack<JsonRespContent>() {
                    @Override
                    public void onResponse(int code, JsonRespContent response) {
                        callback.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    public static void getVideoById(int id, final VideoCallback callback) {

        String url = Config.APP_VIDEOS;
        List<String> pathList = new ArrayList<>();
        pathList.add("id");
        pathList.add(String.valueOf(id));
        OkHttpWrapper.getInstance().getAsync(url, null, pathList, new OkHttpWrapper.RequestCallBack<Video>() {
                    @Override
                    public void onResponse(int code, Video response) {
                        //Log.e("ZWX", "getVideoById onResponse: " + response.getCode());
                        if (response != null) {
                            ArrayList<Video> videoArrayList = new ArrayList<Video>();
                            videoArrayList.add(response);
                            callback.onResult(code, videoArrayList);
                        }

                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        Log.e("ZWX", "getVideoById onFailure: " + code);
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 获取 Video List
     *
     * @param pageNo
     * @param pageSize
     * @param categoryId
     * @param callback
     */
    public static void getVideoList(int pageNo, int pageSize, int categoryId, final VideoCallback callback) {
        String url = Config.APP_VIDEOS;
        List<String> pathList = new ArrayList<>();
        pathList.add("category_id");
        pathList.add(String.valueOf(categoryId));
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("page", String.valueOf(pageNo));
        parameterMap.put("page_size", String.valueOf(pageSize));
        OkHttpWrapper.getInstance().getAsync(url, parameterMap, pathList, new OkHttpWrapper.RequestCallBack<List<Video>>() {
                    @Override
                    public void onResponse(int code, List<Video> response) {
                        callback.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    public static void getApplicationList(int pageNo, int pageSize, final ApplicationCallback callback) {
        JsonReqApplication jsonReq = new JsonReqApplication();
        jsonReq.setName("getApplication");
        jsonReq.setPageNo(pageNo);
        jsonReq.setPageSize(pageSize);
        jsonReq.setVersion("1.0");
        OkHttpWrapper.getInstance().postAsync(Config.URL_GET_APPLICATION, null, null, "", new OkHttpWrapper.RequestCallBack<JsonRespApplication>() {
                    @Override
                    public void onResponse(int code, JsonRespApplication response) {
                        callback.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 添加播放历史
     * @param playRecord
     * @param callback
     */
    public static void addPlayRecord(PlayRecord playRecord, final PlayRecordCallback callback) {
        String url = Config.APP_PLAYRECORD;
        OkHttpWrapper.getInstance().postAsync(url, null, null, playRecord, new OkHttpWrapper.RequestCallBack<PlayRecordModel>() {
                    @Override
                    public void onResponse(int code, PlayRecordModel response) {
                        callback.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 更新播放记录的播放时长
     *
     * @param
     */
    public static void updatePlayRecordDuration(PlayRecord playRecord, final UserCallBack callback) {
        String url = Config.APP_PLAYRECORD;
        OkHttpWrapper.getInstance().putAsync(url, playRecord, new OkHttpWrapper.RequestCallBack<String>() {
                    @Override
                    public void onResponse(int code, String response) {
                        callback.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 根据用户ID获取播放记录
     *
     */
    public static void getPlayRecordsByUserId(int userId, int page, int page_size, final PlayRecordListCallback callback) {

        String client =  E3DApplication.getInstance().getPackageName();
        String url = Config.APP_PLAYRECORD;
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("user_id", String.valueOf(userId));
        parameterMap.put("client", client);
        parameterMap.put("page", String.valueOf(page));
        parameterMap.put("page_size", String.valueOf(page_size));
        OkHttpWrapper.getInstance().getAsync(url, parameterMap, null, new OkHttpWrapper.RequestCallBack<List<PlayRecordModel>>() {
                    @Override
                    public void onResponse(int code, List<PlayRecordModel> response) {
                        callback.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                }
        );
    }

    /**
     *  删除播放记录
     * @param mList
     * @param callBack
     */
    public static void deletePlayRecords(List<PlayRecord> mList, final UserCallBack callBack){
        String url = Config.APP_PLAYRECORD;
        OkHttpWrapper.getInstance().deleteAsync(url, null, mList, new OkHttpWrapper.RequestCallBack<String>() {
                    @Override
                    public void onResponse(int code, String response) {
                        callBack.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callBack.onResult(code, null);
                    }
                }
        );
    }

    public static void addFavorite(Favorite favorite, final FavoriteCallback callBack) {

        String url = Config.APP_FAVORITE;
        OkHttpWrapper.getInstance().postAsync(url, null, null, favorite, new OkHttpWrapper.RequestCallBack<FavoriteModel>() {
                    @Override
                    public void onResponse(int code, FavoriteModel response) {
                        callBack.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callBack.onResult(code, null);
                    }
                }
        );
    }

    /**
     * 根据用户ID获取收藏记录
     *
     * @param userId
     * @param callback
     */
    public static void getFavoriteByUserId(int userId, int page, int page_size, final FavoriteListCallback callback) {
        String url = Config.APP_FAVORITE;
        List<String> pathList = new ArrayList<>();
        pathList.add("user_id");
        pathList.add(String.valueOf(userId));
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("page", String.valueOf(page));
        parameterMap.put("page_size", String.valueOf(page_size));
        OkHttpWrapper.getInstance().getAsync(url, parameterMap, pathList, new OkHttpWrapper.RequestCallBack<List<FavoriteModel>>() {
                    @Override
                    public void onResponse(int code, List<FavoriteModel> response) {
                        callback.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    public static void deleteFavorite(int favoriteid, final UserCallBack callBack) {

        String url = Config.APP_FAVORITE +"/id/" + favoriteid;
        List<String> pathList = new ArrayList<>();
        pathList.add("id");
        pathList.add(String.valueOf(favoriteid));
        OkHttpWrapper.getInstance().deleteAsync (url, pathList, "",new OkHttpWrapper.RequestCallBack<String>() {
                    @Override
                    public void onResponse(int code, String response) {
                        callBack.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callBack.onResult(code, null);
                    }
                }
        );
    }

    /**
     * 删除收藏记录
     * @param mList
     * @param callBack
     */
    public static void deleteFavorite(List<Favorite> mList, final UserCallBack callBack){

        String url = Config.APP_FAVORITE;
        List<String> pathList = new ArrayList<>();
        OkHttpWrapper.getInstance().deleteAsync(url, pathList, mList, new OkHttpWrapper.RequestCallBack<String>() {
                    @Override
                    public void onResponse(int code, String response) {
                        callBack.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callBack.onResult(code, null);
                    }
                }
        );
    }

    /**
     * 上传设备统计信息
     *
     * @param DeviceModel
     * @param System
     * @param Location
     * @param callback
     */
    public static void uploadDeviceInfo(String DeviceModel, String System, String Location,
                                        String client, String clientVersion, Date accessTime, String imei, final DeviceCallback callback) {
        Device device = new Device();
        device.setModel(DeviceModel);
        device.setSystem(System);
        device.setLocation(Location);
        device.setClient(client);
        device.setClientVersion(clientVersion);
        device.setAccessTime(accessTime);
        device.setImei(imei);

        String url = Config.APP_DEVICE;

        OkHttpWrapper.getInstance().putAsync(url, device, new OkHttpWrapper.RequestCallBack<String>() {
                    @Override
                    public void onResponse(int code, String response) {
                        callback.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                }
        );
    }

    /**
     * 获取一个内容的评论
     *
     * @param pageNo
     * @param pageSize
     * @param Id
     * @param Url
     * @param callback
     */
    public static void getContentComment(int pageNo, int pageSize, int Id, String Url,
                                         final ContentCommentCallback callback) {
        JsonReqContentComment reqJson = new JsonReqContentComment();
        reqJson.setName("getContentComment");
        reqJson.setPageNo(pageNo);
        reqJson.setPageSize(pageSize);
        reqJson.setId(Id);
        reqJson.setUrl(Url);
        reqJson.setVersion("1.0");
        OkHttpWrapper.getInstance().postAsync(Config.URL_GET_CONTENT_COMMENT, null, null, "", new OkHttpWrapper.RequestCallBack<JsonRespContentComment>() {

                    @Override
                    public void onResponse(int code, JsonRespContentComment response) {
                        callback.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 获取一个用户的评论
     *
     * @param pageNo
     * @param pageSize
     * @param UserId
     * @param callback
     */
    public static void getUserComment(int pageNo, int pageSize, int UserId, final UserCommentCallback callback) {
        JsonReqUserComment reqJson = new JsonReqUserComment();
        reqJson.setName("getUserComment");
        reqJson.setPageNo(pageNo);
        reqJson.setPageSize(pageSize);
        reqJson.setUserId(UserId);
        reqJson.setVersion("1.0");
        OkHttpWrapper.getInstance().postAsync(Config.URL_GET_USER_COMMENT, null, null, "", new OkHttpWrapper.RequestCallBack<JsonRespContentComment>() {

                    @Override
                    public void onResponse(int code, JsonRespContentComment response) {
                        callback.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 获取一个用户上传的图片
     *
     * @param pageNo
     * @param pageSize
     * @param userId
     * @param callback
     */
    public static void getUserImage(int pageNo, int pageSize, int userId, final ImageCallback callback) {
        JsonReqUserResource reqJson = new JsonReqUserResource();
        reqJson.setName("listByUser");
        reqJson.setPageNo(pageNo);
        reqJson.setPageSize(pageSize);
        reqJson.setUserId(userId);
        reqJson.setType("image");
        reqJson.setVersion("1.0");
        OkHttpWrapper.getInstance().postAsync(Config.URL_LIST_BY_USER, null, null, "", new OkHttpWrapper.RequestCallBack<List<Image>>() {

                    @Override
                    public void onResponse(int code, List<Image> response) {
                        callback.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 获取一个用户上传的视频
     *
     * @param pageNo
     * @param pageSize
     * @param userId
     * @param callback
     */
    public static void getUserVideo(int pageNo, int pageSize, int userId, final VideoCallback callback) {
        JsonReqUserResource reqJson = new JsonReqUserResource();
        reqJson.setName("listByUser");
        reqJson.setPageNo(pageNo);
        reqJson.setPageSize(pageSize);
        reqJson.setUserId(userId);
        reqJson.setType("video");
        reqJson.setVersion("1.0");
        OkHttpWrapper.getInstance().postAsync(Config.URL_LIST_BY_USER, null, null, "", new OkHttpWrapper.RequestCallBack<List<Video>>() {

                    @Override
                    public void onResponse(int code, List<Video> response) {
                        callback.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    /**
     * 发表评论
     *
     * @param userId
     * @param contentId
     * @param comment
     * @param callback
     */
    public static void sendComment(int userId, int contentId, String comment, final SendCommentCallback callback) {
        JsonReqSendComment reqJson = new JsonReqSendComment();
        reqJson.setName("sendComment");
        reqJson.setUserId(userId);
        reqJson.setContentId(contentId);
        reqJson.setComment(comment);
        reqJson.setVersion("1.0");
        OkHttpWrapper.getInstance().postAsync(Config.URL_SEND_COMMENT, null, null, "", new OkHttpWrapper.RequestCallBack<String>() {

                    @Override
                    public void onResponse(int code, String response) {
                        callback.onResult(code, response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, null);
                    }
                });
    }

    private static int loginChalleng(String userName, String password, String challenge,
                                     final LoginCallback callback) {
        AuthServiceImpl authService = new AuthServiceImpl();

        // 使用密码对challenge字符串进行加密
        SecretKey key = authService.generateKey(password);
        String encryptedChallenge = authService.encryptToBase64String(challenge, key);
        String url = Config.APP_LOGIN;
        List<String> pathList = new ArrayList<>();
        pathList.add("login");
        pathList.add(userName);
        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("challenge", encryptedChallenge);
        OkHttpWrapper.getInstance().postAsync(url, parameterMap, pathList, "", new OkHttpWrapper.RequestCallBack<User>() {

                    @Override
                    public void onResponse(int code, User response) {
                        callback.onResult(code, "", response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        callback.onResult(code, msg, null);
                    }
                });

        return 0;
    }

    private static int registerChallenge(User user, String challenge, final LoginCallback callback) {
        AuthServiceImpl authService = new AuthServiceImpl();
        // 使用challenge字符串对密码进行加密
        SecretKey key = authService.generateKey(challenge);
        String encryptedPassword = authService.encryptToBase64String(user.getPassword(), key);
        Log.e(TAG, encryptedPassword + " :encryptedPassword");
        user.setPassword(encryptedPassword);

        String url = Config.APP_REGISTER;
        List<String> pathList = new ArrayList<>();
        pathList.add("register");
        pathList.add("challenge");
        OkHttpWrapper.getInstance().postAsync(url, null, pathList, user, new OkHttpWrapper.RequestCallBack<User>() {
                    @Override
                    public void onResponse(int code, User response) {
                        callback.onResult(code, "注册成功！" ,response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        Log.e("ZWX", "onFailure code: " + code + " msg: " + msg);
                    }
                });

        return 0;
    }

    public static void register(final String userName, final String access_token, final String nickName, final String location, final String sex,
                                final String headImgurl, final String source, final RegisterCallback callback) {
        final User user = new User();
        user.setUsername(userName);
        String url = Config.APP_REGISTER;
        List<String> pathList = new ArrayList<>();
        pathList.add("register");
        OkHttpWrapper.getInstance().postAsync(url, null, pathList, user, new OkHttpWrapper.RequestCallBack<String>() {
                    @Override
                    public void onResponse(int code, String response) {
                        if (code == 200) {
                            callback.onResult(code, "娉ㄥ唽鎴愬姛.");
                        } else {
                            callback.onResult(code, userName + " 宸插瓨鍦?");
                            Log.e(TAG, "error code: " + code);
                        }
                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        Log.e(TAG, "login error, code: " + code + " msg: " + msg);
                        callback.onResult(code,msg);
                    }
                });
    }

    public static void registerByThirdParty(String userName, String access_token, String nickName, String location, String sex,
                                            String headImgurl, String source, final LoginCallback callback) {
        final User user = new User();
        user.setUsername(userName);
        user.setPassword(access_token);
        user.setNickname(nickName);
        user.setLocation(location);
        user.setSex(sex);
        user.setFigureUrl(headImgurl);
        user.setSource(source);

        String url = Config.APP_REGISTER;
        List<String> pathList = new ArrayList<>();
        pathList.add("register");
        OkHttpWrapper.getInstance().postAsync(url, null, pathList, user, new OkHttpWrapper.RequestCallBack<User>() {
                    @Override
                    public void onResponse(int code, User response) {
                        callback.onResult(code, "success", response);
                    }

                    @Override
                    public void onFailure(int code, String msg) {

                    }
                });
    }

    public static void updateDownloadCount(final String url, int id) {
        JsonReqDownload jsonReqDownload = new JsonReqDownload();
        jsonReqDownload.setName("download");
        jsonReqDownload.setContentId(id);
        jsonReqDownload.setUrl(url);
        OkHttpWrapper.getInstance().postAsync(Config.URL_APP_DOWNLOAD, null, null, "", new OkHttpWrapper.RequestCallBack<String>() {
                    @Override
                    public void onResponse(int code, String response) {

                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        Log.e(TAG, "Failed to update download count, URL: " + url + " errorCode: " + code
                                + " errorInfo: " + msg);
                    }
                });
    }

    public static void updatePlayCount(final String url) {
        JsonReqDownload jsonReqDownload = new JsonReqDownload();
        jsonReqDownload.setName("download");
        jsonReqDownload.setContentId(0);
        jsonReqDownload.setUrl(url);
        OkHttpWrapper.getInstance().postAsync(Config.URL_DOWNLOAD, null, null, "", new OkHttpWrapper.RequestCallBack<String>() {
                    @Override
                    public void onResponse(int code, String response) {

                    }

                    @Override
                    public void onFailure(int code, String msg) {
                        Log.e(TAG, "Failed to update download count, URL: " + url +
                                " errorCode: " + code +
                                " errorInfo: " + msg);
                    }
                });
    }

    public static void getAllAds(final String url, final AdsCallback callback) {
        OkHttpWrapper.getInstance().getAsync(url, null, null, new OkHttpWrapper.RequestCallBack<List<Product>>() {
            @Override
            public void onResponse(int code, List<Product> response) {
                callback.onResult(code, response);
            }

            @Override
            public void onFailure(int code, String msg) {
                callback.onResult(code, null);
            }
        });
    }

}
