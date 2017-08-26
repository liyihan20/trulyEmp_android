package cn.com.truly.ic.trulyemp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.lang.ref.WeakReference;
import java.util.List;

import cn.com.truly.ic.trulyemp.adapters.KeyValueAdapter;
import cn.com.truly.ic.trulyemp.models.KVModel;
import cn.com.truly.ic.trulyemp.models.ParamsModel;
import cn.com.truly.ic.trulyemp.models.SimpleResultModel;
import cn.com.truly.ic.trulyemp.utils.MyUtils;
import cn.com.truly.ic.trulyemp.utils.SoapService;

public class UserDetailActivity extends BaseActivity {

    private static final String EXTRA_CARD_NUMBER = "cn.com.truly.emp.user.card_number";

    private String mCardNumber;

    private ImageView mPortrait;
    private Button mResetBt, mActivateBt;
    private RecyclerView mRecyclerView;

    private Handler mHandler;

    public static Intent newIntent(Context context, String cardNumber) {
        Intent intent = new Intent(context, UserDetailActivity.class);
        intent.putExtra(EXTRA_CARD_NUMBER, cardNumber);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        mCardNumber = getIntent().getStringExtra(EXTRA_CARD_NUMBER);

        mRecyclerView = (RecyclerView) findViewById(R.id.user_detail_recycle_view);
        mPortrait = (ImageView) findViewById(R.id.user_detail_portrait);
        mResetBt = (Button) findViewById(R.id.user_detail_reset_bt);
        mActivateBt = (Button) findViewById(R.id.user_detail_activate_bt);

        mResetBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResetBt.setText(getString(R.string.dealing));
                mResetBt.setEnabled(false);
                new UserDetailThread(3,"AdminResetPassword").start();
            }
        });

        mActivateBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivateBt.setText(getString(R.string.dealing));
                mActivateBt.setEnabled(false);
                new UserDetailThread(4,"AdminActivatePassword").start();
            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mHandler = new MyHandler(this);

        new GetImageThread().start();
        getUserDetailInfo();

    }

    private void getUserDetailInfo(){
        new UserDetailThread(2, "GetUserDetail").start();
    }

    private static class MyHandler extends Handler {
        private final WeakReference<UserDetailActivity> mReference;

        private MyHandler(UserDetailActivity activity) {
            mReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            UserDetailActivity target = mReference.get();
            if (target == null) return;

            SimpleResultModel result;
            switch (msg.what) {
                case 1:
                    if (target.mPortrait != null) {
                        target.mPortrait.setImageBitmap((Bitmap) msg.obj);
                    }
                    break;
                case 2:
                    result = JSON.parseObject(msg.obj.toString(), SimpleResultModel.class);
                    List<KVModel> list = JSON.parseArray(result.getExtra(), KVModel.class);

                    KeyValueAdapter adapter = new KeyValueAdapter(target, list);
                    target.mRecyclerView.setAdapter(adapter);
                    break;
                case 3:
                    target.mResetBt.setText(target.getString(R.string.reset_password));
                    target.mResetBt.setEnabled(true);
                    result = JSON.parseObject(msg.obj.toString(), SimpleResultModel.class);

                    Toast.makeText(target,result.getMsg(),Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    target.mActivateBt.setText(target.getString(R.string.activate_user));
                    target.mActivateBt.setEnabled(true);
                    result = JSON.parseObject(msg.obj.toString(), SimpleResultModel.class);

                    if(result.isSuc()){
                        target.getUserDetailInfo();
                    }
                    Toast.makeText(target,result.getMsg(),Toast.LENGTH_SHORT).show();
                    break;
            }

            super.handleMessage(msg);
        }
    }

    private class GetImageThread extends Thread {
        @Override
        public void run() {
            String url = "http://59.37.42.23/Emp/Home/GetEmpPortrait?card_no=" + mCardNumber;
            Message msg = mHandler.obtainMessage();
            msg.what = 1;
            msg.obj = MyUtils.getHttpBitmap(url);
            mHandler.sendMessage(msg);
            super.run();
        }
    }

    private class UserDetailThread extends Thread {
        private int mWhat;
        private String mMethod;

        private UserDetailThread(int what, String method) {
            mWhat = what;
            mMethod = method;
        }

        @Override
        public void run() {
            SoapService soap = new SoapService(userModel.getUserId());
            ParamsModel pm = new ParamsModel();
            pm.setArg1(mCardNumber);

            try {
                Message msg = mHandler.obtainMessage();
                msg.what = mWhat;
                msg.obj = soap.getSoapStringResult(mMethod, pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            super.run();
        }
    }


}
