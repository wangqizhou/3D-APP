package com.evistek.gallery.user;

import android.util.Log;

import com.evistek.gallery.model.Favorite;
import com.evistek.gallery.model.FavoriteModel;
import com.evistek.gallery.net.Config;
import com.evistek.gallery.net.NetWorkService;
import com.evistek.gallery.net.callback.FavoriteCallback;
import com.evistek.gallery.net.callback.FavoriteListCallback;
import com.evistek.gallery.net.json.JsonRespFavorite;
import com.evistek.gallery.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by evis on 2016/8/17.
 */
public class User {
    public static final String SOURCE_QQ = "qq";
    public String name;
    public String nickname;
    public String location;
    public String registerTime;
    public int id;
    public int type;
    public String source;
    public String headImgUrl;
    public boolean isLogin;
    public List<FavoriteModel> favorites = new ArrayList<>();

    public User() {
        update();
    }

    public void update() {
        name = Utils.getValue(Utils.SHARED_USERNAME, null);
        isLogin = (name == null) ? false : true;
        nickname = Utils.getValue(Utils.SHARED_NICKNAME, null);
        location = Utils.getValue(Utils.SHARED_LOCATION, null);
        registerTime = Utils.getValue(Utils.SHARED_REGISTERTIME, null);
        id = Utils.getValue(Utils.SHARED_USERID, 0);
        type = Utils.getValue(Utils.SHARED_USERTYPE, 0);
        source = Utils.getValue(Utils.SHARED_SOURCE, null);
        headImgUrl = Utils.getValue(Utils.SHARED_HEAD_IMGURL, null);
        getFavorite();
    }

    private void getFavorite() {
        if (isLogin) {
            NetWorkService.getFavoriteByUserId(id, Config.FIRST_PAGE, Config.PAGE_SIZE, new FavoriteListCallback() {
                @Override
                public void onResult(int code, List<FavoriteModel> jsonResp) {
                    if (code == 200) {
                        favorites = jsonResp;
                    } else if (code == 400) {
                        favorites.clear();
                    }
                }
            });
        }
    }
}
