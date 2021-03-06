package cn.com.truly.ic.trulyemp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.beardedhen.androidbootstrap.BootstrapCircleThumbnail;

import java.lang.ref.WeakReference;
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

    private LinearLayout mAdminLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        mHandler = new MyHandler(this);

        new GetImageThread().start();
        new GetUserPowerThread().start();
        validateEmailAndPhone();
    }

    private static class MyHandler extends Handler {

        private final WeakReference<MainActivity> mReference;

        private MyHandler(MainActivity activity) {
            mReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity target = mReference.get();
            if (target == null) return;

            SimpleResultModel result;
            switch (msg.what) {
                case 1:
                    if (target.mPortraitThumbnail != null) {
                        target.mPortraitThumbnail.setImageBitmap((Bitmap) msg.obj);
                    }
                    break;
                case 2:
                    result = JSON.parseObject(msg.obj.toString(), SimpleResultModel.class);
                    if (!result.isSuc()) {
                        Toast.makeText(target, result.getMsg(), Toast.LENGTH_LONG).show();
                    } else {
                        Intent dormIntent = new Intent(target, DormActivity.class);
                        target.startActivity(dormIntent);
                    }
                    break;
                case 3:
                    result = JSON.parseObject(msg.obj.toString(), SimpleResultModel.class);
                    if (result.isSuc()) {
                        if (result.getExtra().contains("AdminIndex")) {
                            target.mAdminLayout.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                case 4:
                    result = JSON.parseObject(msg.obj.toString(), SimpleResultModel.class);
                    if(!result.isSuc()){
                        Toast.makeText(target,result.getMsg(),Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(TextUtils.isEmpty(target.userModel.getBankCardNumber())){
                        Toast.makeText(target,result.getMsg(),Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(!target.userModel.getBankCardNumber().equals(result.getExtra())){
                        Toast.makeText(target,"检测到你的工资银行卡号已变动，请点击头像更新你的银行卡号后再查询",Toast.LENGTH_LONG).show();
                        return;
                    }
                    Intent intent = SalaryActivity.newIntent(target);
                    target.startActivity(intent);
                    break;
            }
            super.handleMessage(msg);
        }
    }

    private void initView() {
        mScrollView = (ScrollView) findViewById(R.id.main_scroll_view);
        mPortraitThumbnail = (BootstrapCircleThumbnail) findViewById(R.id.main_portrait_thumbnail);
        mAdminLayout = (LinearLayout) findViewById(R.id.main_admin_linear_layout);

        RelativeLayout dormLayout = (RelativeLayout) findViewById(R.id.main_dorm_layout);
        RelativeLayout dinnerLayout = (RelativeLayout) findViewById(R.id.main_dinner_layout);
        RelativeLayout adminRelativeLayout = (RelativeLayout) findViewById(R.id.main_admin_relative_layout);
        RelativeLayout salaryLayout = (RelativeLayout) findViewById(R.id.main_salary_layout);

        TextView userNameTextView = (TextView) findViewById(R.id.main_user_name);
        TextView iconDorm = (TextView) findViewById(R.id.icon_main_dorm);
        TextView iconRight1 = (TextView) findViewById(R.id.icon_main_right1);
        TextView iconDinnerCard = (TextView) findViewById(R.id.icon_main_dinner_card);
        TextView iconRight2 = (TextView) findViewById(R.id.icon_main_right2);
        TextView iconAdmin = (TextView) findViewById(R.id.icon_main_admin);
        TextView iconRight3 = (TextView) findViewById(R.id.icon_main_right3);
        TextView iconSalary = (TextView) findViewById(R.id.icon_main_salary);
        TextView iconRight4 = (TextView) findViewById(R.id.icon_main_right4);

        MyUtils.setFont(this, MyUtils.createArrayList(iconDorm, iconRight1, iconDinnerCard,
                iconRight2, iconAdmin, iconRight3, iconSalary, iconRight4));

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

        salaryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //邮箱和手机号码齐全才能进入
                if (TextUtils.isEmpty(userModel.getPhoneNumber())) {
                    Toast.makeText(MainActivity.this, "进入查询之前请先登记你的手机号码，点击头像即可设置", Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(userModel.getEmail())) {
                    Toast.makeText(MainActivity.this, "进入查询之前请先登记你的邮箱(不限信利邮箱，qq或163等都可以)，点击头像即可设置", Toast.LENGTH_LONG).show();
                    return;
                }
                //获取工资银行卡号后6位
                new GetSalaryBankCardThread().start();
            }
        });

        adminRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = AdminActivity.newIntent(MainActivity.this);
                startActivity(intent);
            }
        });

    }

    private void updateUserInfo() {
        UpdateInfoFragment fragment = UpdateInfoFragment.newInstance(
                userModel.getUserId(),
                userModel.getMd5Password(),
                userModel.getPhoneNumber(),
                userModel.getShortPhoneNumber(),
                userModel.getEmail(),
                userModel.getBankCardNumber()
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
            SoapService soap = new SoapService(userModel.getUserId());
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

    private class GetUserPowerThread extends Thread {
        @Override
        public void run() {
            SoapService soap = new SoapService(userModel.getUserId());
            ParamsModel pm = new ParamsModel();

            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 3;
                msg.obj = soap.getSoapStringResult("GetUserPower", pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            super.run();
        }
    }

    private class GetSalaryBankCardThread extends Thread {
        @Override
        public void run() {
            SoapService soap = new SoapService(userModel.getUserId());
            ParamsModel pm = new ParamsModel();
            pm.setArg1(userModel.getSalaryNumber());

            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 4;
                msg.obj = soap.getSoapStringResult("GetSalaryBankCard", pm);
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
