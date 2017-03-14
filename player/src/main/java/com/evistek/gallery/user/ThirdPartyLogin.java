package com.evistek.gallery.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

public class ThirdPartyLogin {
    private UserInfo mUserInfo;
    private Context mContext;
    private Handler mHandler;
    private Tencent mTencent;
    private IUiListener mLoginListener;
    private User mUser;

    public ThirdPartyLogin(Context context, Handler handler, String appID, String tencentScope) {
        mContext = context;
        mHandler = handler;
        mTencent = Tencent.createInstance(appID, mContext);
        if (!mTencent.isSessionValid()) {
            mLoginListener = new BaseUiListener() {
                @Override
                protected void doComplete(JSONObject values) {
                    Log.d("SDKQQAgentPref", "AuthorSwitch_SDK:" + SystemClock.elapsedRealtime());
                    initOpenidAndToken(values);
                    updateUserInfo();
                }
            };
            mTencent.login((Activity) mContext, tencentScope, mLoginListener);
        }
        mTencent.logout(mContext);
    }

    public User getUser() {
        return mUser;
    }

    public void onActivityResultData(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_LOGIN || requestCode == Constants.REQUEST_APPBAR) {
            mTencent.onActivityResultData(requestCode, resultCode, data, mLoginListener);
        }
    }

    private void updateUserInfo() {
        if (mTencent != null && mTencent.isSessionValid()) {
            IUiListener listener = new IUiListener() {

                @Override
                public void onError(UiError e) {

                }

                @Override
                public void onComplete(Object response) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = 0;
                    mHandler.sendMessage(msg);
                    final JSONObject json = (JSONObject) response;
                    mUser = new User();
                    mUser.setOpenId(mTencent.getOpenId());
                    mUser.setAccessToken(mTencent.getAccessToken());
                    mUser.setSource("qq");
                    try {
                        mUser.setHeadImgurl(json.getString("figureurl_qq_2"));
                        mUser.setNickName(json.getString("nickname"));
                        mUser.setSex(json.getString("gender"));
                        mUser.setLocation(json.getString("province") + " " + json.getString("city"));
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancel() {

                }
            };
            mUserInfo = new UserInfo(mContext, mTencent.getQQToken());
            mUserInfo.getUserInfo(listener);
        }
    }

    private void initOpenidAndToken(JSONObject jsonObject) {
        try {
            String token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
            String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
            String openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires) && !TextUtils.isEmpty(openId)) {
                mTencent.setAccessToken(token, expires);
                mTencent.setOpenId(openId);
            }
        } catch (Exception e) {
        }
    }

    private class BaseUiListener implements IUiListener {

        @Override
        public void onComplete(Object response) {
            JSONObject jsonResponse = (JSONObject) response;
            if (null == response || (null != jsonResponse && jsonResponse.length() == 0)) {
                return;
            }
            doComplete((JSONObject) response);
        }

        protected void doComplete(JSONObject values) {

        }

        @Override
        public void onError(UiError e) {

        }

        @Override
        public void onCancel() {

        }
    }

    public class User {

        private String openId;

        private String accessToken;

        private String nickName;

        private String location;

        private String sex;

        private String headImgurl;

        private String source;

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getOpenId() {
            return openId;
        }

        public void setOpenId(String openId) {
            this.openId = openId;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
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

        public String getHeadImgurl() {
            return headImgurl;
        }

        public void setHeadImgurl(String headImgurl) {
            this.headImgurl = headImgurl;
        }

    }
}