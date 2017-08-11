package cn.com.truly.ic.trulyemp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.beardedhen.androidbootstrap.BootstrapCircleThumbnail;

import java.util.Date;

import cn.com.truly.ic.trulyemp.models.ParamsModel;
import cn.com.truly.ic.trulyemp.models.SimpleResultModel;
import cn.com.truly.ic.trulyemp.utils.MyUtils;
import cn.com.truly.ic.trulyemp.utils.SoapService;

public class MainActivity extends BaseActivity {

    private BootstrapCircleThumbnail mPortraitThumbnail;
    private ScrollView mScrollView;
    private static final String UPDATE_INFO_DIALOG = "update_info_dialog";

    private Handler mHandler;
    private long mClickedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                SimpleResultModel result;
                switch (msg.what) {
                    case 1:
                        if (mPortraitThumbnail != null) {
                            mPortraitThumbnail.setImageBitmap((Bitmap) msg.obj);
                        }
                        break;
                    case 2:
                        result = JSON.parseObject(msg.obj.toString(), SimpleResultModel.class);
                        if (!result.isSuc()) {
                            Toast.makeText(MainActivity.this, result.getMsg(), Toast.LENGTH_LONG).show();
                        } else {
                            Intent dormIntent = new Intent(MainActivity.this, DormActivity.class);
                            startActivity(dormIntent);
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };

        new GetImageThread().start();
        validateEmailAndPhone();
    }

    private void initView() {
        mScrollView = (ScrollView) findViewById(R.id.main_scroll_view);
        mPortraitThumbnail = (BootstrapCircleThumbnail) findViewById(R.id.main_portrait_thumbnail);

        RelativeLayout dormLayout=(RelativeLayout)findViewById(R.id.main_dorm_layout);
        RelativeLayout dinnerLayout=(RelativeLayout)findViewById(R.id.main_dinner_layout);
        TextView userNameTextView = (TextView) findViewById(R.id.main_user_name);
        TextView iconDorm = (TextView) findViewById(R.id.icon_main_dorm);
        TextView iconRight1 = (TextView) findViewById(R.id.icon_main_right1);
        TextView iconDinnerCard = (TextView) findViewById(R.id.icon_main_dinner_card);
        TextView iconRight2 = (TextView) findViewById(R.id.icon_main_right2);

        MyUtils.setFont(this, MyUtils.createArrayList(iconDorm, iconRight1, iconDinnerCard, iconRight2));
        userNameTextView.setText(userModel.getUserName());

        mPortraitThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserInfo();
            }
        });

        dormLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UserIsInDormThread().start();
            }
        });

        dinnerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(DinnerActivity.newIntent(MainActivity.this));
            }
        });

    }

    private void updateUserInfo() {
        UpdateInfoFragment fragment = UpdateInfoFragment.newInstance(
                userModel.getUserId(),
                userModel.getMd5Password(),
                userModel.getPhoneNumber(),
                userModel.getShortPhoneNumber(),
                userModel.getEmail()
        );
        fragment.show(getSupportFragmentManager(), UPDATE_INFO_DIALOG);
    }

    private void validateEmailAndPhone() {
        String email = userModel.getEmail();
        String phone = userModel.getPhoneNumber();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(phone)) {
            return;
        }

        String msg = "";
        if (TextUtils.isEmpty(email) && TextUtils.isEmpty(phone)) {
            msg = "检测到你的邮箱地址和手机号没有设置，请更新个人信息";
        } else if (TextUtils.isEmpty(email)) {
            msg = "检测到你的邮箱地址未设置，不限信利邮箱，可以是QQ或163等邮箱，请更新个人信息";
        } else if (TextUtils.isEmpty(phone)) {
            msg = "检测到你的手机号码未设置，请更新个人信息";
        }

        Snackbar.make(mScrollView, msg, BaseTransientBottomBar.LENGTH_INDEFINITE)
                .setAction("更新信息", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateUserInfo();
                    }
                }).show();

    }

    private class GetImageThread extends Thread {
        @Override
        public void run() {
            String cardNo = userModel.getCardNumber();
            String url = "http://59.37.42.23/Emp/Home/GetEmpPortrait?card_no=" + cardNo;
            Message msg = mHandler.obtainMessage();
            msg.what = 1;
            msg.obj = MyUtils.getHttpBitmap(url);
            mHandler.sendMessage(msg);
            super.run();
        }
    }

    private class UserIsInDormThread extends Thread {
        @Override
        public void run() {
            SoapService soap = new SoapService();
            ParamsModel pm = new ParamsModel();
            pm.setArg1(userModel.getSalaryNumber());

            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 2;
                msg.obj = soap.getSoapStringResult("IsUserInDorm", pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            super.run();
        }
    }

    @Override
    public void onBackPressed() {
        long currentTime = new Date().getTime();
        if (currentTime - mClickedTime < 1000) {
            finish();
        } else {
            mClickedTime = currentTime;
            Toast.makeText(this, "连续按两次返回键登出系统", Toast.LENGTH_SHORT).show();
        }
    }

}
