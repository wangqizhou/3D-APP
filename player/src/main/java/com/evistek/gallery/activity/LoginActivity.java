package com.evistek.gallery.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.evistek.gallery.R;
import com.evistek.gallery.model.User;
import com.evistek.gallery.model.UserModel;
import com.evistek.gallery.net.NetWorkService;
import com.evistek.gallery.net.callback.LoginCallback;
import com.evistek.gallery.net.callback.UserCallBack;
import com.evistek.gallery.net.callback.UserNameCallBack;
import com.evistek.gallery.user.ThirdPartyLogin;
import com.evistek.gallery.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends Activity {
    private final static String TENCENT_APP_ID = "1105523360";
    private final static String TENCENT_SCOPE = "all";
    private final static String TAG = "LoginV2Activity";

    @BindView(R.id.EtLogUsername)
    EditText mLogUserNameET;
    @BindView(R.id.EtLogPassword)
    EditText mLogPasswordEt;
    @BindView(R.id.BtLogLogin)
    Button mLogButton;
    @BindView(R.id.BtLogRegister)
    TextView mRegisterBt;
    @BindView(R.id.v2_user_login_backbt)
    ImageView mBack;

    private static ThirdPartyLogin mThirdPartyLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_v2);
        ButterKnife.bind(this);

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initView() {
        initLoginSlidingMenu();
    }

    private void initLoginSlidingMenu() {
        mBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mLogButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!mLogUserNameET.getText().toString().equals("")) {
                    if (mLogPasswordEt.getText().toString().length() > 6) {
                        final String userName = mLogUserNameET.getText().toString();
                        final String password = mLogPasswordEt.getText().toString();
                        NetWorkService.login(userName, password, new LoginCallback() {

                            @Override
                            public void onResult(int code, String msg, User respLogin) {
                                if (code == 200) {
                                    getApplicationContext();
                                    Utils.saveValue(Utils.SHARED_USERNAME, userName);
                                    Utils.saveValue(Utils.SHARED_USERID, respLogin.getId());
                                    Utils.saveValue(Utils.SHARED_REGISTERTIME, respLogin.getRegisterTime().toString());
                                    Utils.saveValue(Utils.SHARED_SOURCE, respLogin.getSource());
                                    E3DApplication.getInstance().getUser().update();
                                    updateDeviceInfo();
                                    Toast.makeText(getApplicationContext(),
                                            getApplicationContext().getResources().getText(R.string.LoginSuccess),
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                } else if (code == 404) {
                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                } else if (code == 401) {
                                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                } else if (Utils.isNetworkAvailable() == false) {
                                    Toast.makeText(getApplicationContext(), R.string.net_not_available, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(),
                                getApplicationContext().getResources().getText(R.string.RegisterPasswordlimit),
                                Toast.LENGTH_SHORT).show();
                        mLogPasswordEt.setText("");
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            getApplicationContext().getResources().getText(R.string.RegisterUsernamelimit),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        mRegisterBt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
//                startActivity(intent);
                AlertDialog mdialog = new  AlertDialog.Builder(LoginActivity.this)
                        .setTitle(R.string.downloadDeletetip)
                        .setMessage(R.string.log_warning)
                        .setPositiveButton(R.string.ok,null)
                        .show();
            }
        });

    }

    public void qqLogin(View view) {
        mThirdPartyLogin = new ThirdPartyLogin(LoginActivity.this, mHandler, TENCENT_APP_ID, TENCENT_SCOPE);
    }

    Handler mqqLoginHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            User user = (User) msg.obj;
            if (user != null) {
                NetWorkService.register(user, new LoginCallback() {
                    @Override
                    public void onResult(int code, String msg, User responUser) {
                        if (code == 200) {
                            Utils.saveValue(Utils.SHARED_USERNAME, responUser.getUsername());
                            Utils.saveValue(Utils.SHARED_NICKNAME, responUser.getNickname());
                            Utils.saveValue(Utils.SHARED_USERID, responUser.getId());
                            Utils.saveValue(Utils.SHARED_REGISTERTIME, responUser.getRegisterTime().toString());
                            Utils.saveValue(Utils.SHARED_SOURCE, responUser.getSource());
                            Utils.saveValue(Utils.SHARED_HEAD_IMGURL, responUser.getFigureUrl());
                            E3DApplication.getInstance().getUser().update();
                            Toast.makeText(getApplicationContext(),
                                    getApplicationContext().getResources().getText(R.string.LoginSuccess),
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
            }
            return true;
        }
    });

    Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 0) {
                final ThirdPartyLogin.User userInfo = mThirdPartyLogin.getUser();
                final User user = new User();
                user.setUsername(userInfo.getOpenId());
                user.setPassword(userInfo.getAccessToken());
                user.setNickname(userInfo.getNickName());
                user.setLocation(userInfo.getLocation());
                user.setSex(userInfo.getSex());
                user.setFigureUrl(userInfo.getHeadImgurl());
                user.setSource(userInfo.getSource());
                user.setPhoneDevice(Utils.getDeviceModel());
                user.setPhoneSystem(Utils.getDeviceSystem());
                NetWorkService.getUserByUserName(userInfo.getOpenId(), new UserNameCallBack() {
                    @Override
                    public void onResult(int code, final UserModel JsonResp) {
                        if (code == 200) {
                            User user = new User(userInfo.getOpenId(),
                            userInfo.getAccessToken(), userInfo.getNickName(),
                            userInfo.getLocation(), userInfo.getSex(),
                            userInfo.getHeadImgurl(), userInfo.getSource());
                            updateUserAndLogin(JsonResp, user);
                        } else {
                            Message msgLogin = mqqLoginHandler.obtainMessage();
                            msgLogin.obj = user;
                            mqqLoginHandler.sendMessage(msgLogin);
                        }
                    }
                });
            }
            return true;
        }

    });

    public void addLoginRecord (int userId) {
        //上传登录记录的信息 1：login ; 0:logout
        NetWorkService.addLoginRecord(userId, 1, new UserCallBack() {

            @Override
            public void onResult(int code, String msg) {
                if (code == 200) {
                    Log.i(TAG, "add loginRecord successfully");
                } else if (code == 400) {
                    Log.i(TAG, "failed to add loginRecord");
                }
            }
        });
    }

    public void updateUserAndLogin(final UserModel JsonResp, final User user) {
        Date register = null;
        try {
            register = new SimpleDateFormat("yyyy-MM-dd HH:ss:mm").parse(JsonResp.getRegisterTime().toString());
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        user.setId(JsonResp.getId());
        user.setRegisterTime(register);
        user.setPhone(JsonResp.getPhone());
        user.setEmail(JsonResp.getEmail());
        // update User
        NetWorkService.updateUserInfo(user, new UserCallBack() {

            @Override
            public void onResult(int code, String msg) {
                if (code == 200) {
                    Utils.saveValue(Utils.SHARED_USERNAME, user.getUsername());
                    Utils.saveValue(Utils.SHARED_NICKNAME, user.getNickname());
                    Utils.saveValue(Utils.SHARED_USERID, user.getId());
                    Utils.saveValue(Utils.SHARED_REGISTERTIME, JsonResp.getRegisterTime().toString());
                    Utils.saveValue(Utils.SHARED_SOURCE, user.getSource());
                    Utils.saveValue(Utils.SHARED_HEAD_IMGURL, user.getFigureUrl());
                    E3DApplication.getInstance().getUser().update();
                    finish();
                }
            }
        });
    }

    private void updateDeviceInfo() {
//        User user = E3DApplication.getInstance().getUser();
//        if (user.isLogin) {
//
//            User userInfo = new User();
//            userInfo.setUserId(user.id);
//            userInfo.setVrDevice(user.vrDevice);
//
//            NetWorkService.updateUserInfo(userInfo, new UserCallBack() {
//                @Override
//                public void onResult(int code, String msg) {
//                    if (code != 200) {
//                        Log.e(TAG, "code: " + code + " msg: " + msg);
//                    }
//                }
//            });
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mThirdPartyLogin.onActivityResultData(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
