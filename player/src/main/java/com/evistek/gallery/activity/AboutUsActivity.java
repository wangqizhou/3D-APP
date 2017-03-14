package com.evistek.gallery.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.evistek.gallery.R;
import com.evistek.gallery.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AboutUsActivity extends Activity {

    @BindView(R.id.v2_user_aboutus_backbt)
    ImageView mBack;
    @BindView(R.id.tv_version)
    TextView mTVversion;

    private String version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        ButterKnife.bind(this);

        version = Utils.getVersion(getApplicationContext());
        mTVversion.setText(version);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
