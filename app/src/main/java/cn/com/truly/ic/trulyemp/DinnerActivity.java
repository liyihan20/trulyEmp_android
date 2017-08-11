package cn.com.truly.ic.trulyemp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.SwitchCompat;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.lang.ref.WeakReference;

import cn.com.truly.ic.trulyemp.models.DinnerBindingModel;
import cn.com.truly.ic.trulyemp.models.DinnerInfoModels;
import cn.com.truly.ic.trulyemp.models.ParamsModel;
import cn.com.truly.ic.trulyemp.models.SimpleResultModel;
import cn.com.truly.ic.trulyemp.utils.MyUtils;
import cn.com.truly.ic.trulyemp.utils.SoapService;

public class DinnerActivity extends BaseActivity {

    private TextView mUserName, mCardNumber, mDCardStatus, mRemainSum, mLastConsumeTime, mDCardAction;
    private SwitchCompat mSwitchCompat;
    private SeekBar mSeekBar;
    private TextView mLimitValue;
    private EditText mPayPassword;
    private TextView mIconEye;
    private Button mSaveButton;

    private Handler mHandler;
    private String mActionCode;


    public static Intent newIntent(Context context) {
        return new Intent(context, DinnerActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dinner);

        initView();

        mHandler = new MyHandler(this);

        new GetDormInfoThread().start();
        new GetBindingInfoThread().start();
    }

    private static class MyHandler extends Handler {
        private final WeakReference<DinnerActivity> mActivity;

        private MyHandler(DinnerActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            DinnerActivity target = mActivity.get();
            if (target == null) return;
            SimpleResultModel result = JSON.parseObject(msg.obj.toString(), SimpleResultModel.class);
            switch (msg.what) {
                case 1:
                    DinnerInfoModels info = JSON.parseObject(result.getExtra(), DinnerInfoModels.class);
                    target.mUserName.setText(target.userModel.getUserName());
                    target.mCardNumber.setText(target.userModel.getCardNumber());
                    target.mDCardStatus.setText(info.getCardStatus());
                    target.mRemainSum.setText(info.getSumRemain());
                    target.mLastConsumeTime.setText(info.getLastConsumeTime());
                    target.updateCardAction();
                    break;
                case 2:
                    target.mDCardStatus.setText(result.getExtra());
                    target.updateCardAction();
                    Toast.makeText(target, result.getMsg(), Toast.LENGTH_LONG).show();
                    break;
                case 3:
                    if (result.isSuc()) {
                        DinnerBindingModel binding = JSON.parseObject(result.getExtra(), DinnerBindingModel.class);
                        target.mSwitchCompat.setChecked("1".equals(binding.getStatus()));
                        target.mPayPassword.setText(binding.getPayPassword());
                        target.mSeekBar.setProgress(Integer.parseInt(binding.getLimit()));
                    }
                    mActivity.get().setBindingEnable();
                    break;
                case 4:
                    Toast.makeText(target, result.getMsg(), Toast.LENGTH_LONG).show();
                    target.mSaveButton.setEnabled(true);
                    break;
            }

            super.handleMessage(msg);
        }
    }

    private void initView() {
        mUserName = (TextView) findViewById(R.id.dinner_user_name_tv);
        mCardNumber = (TextView) findViewById(R.id.dinner_card_number_tv);
        mDCardStatus = (TextView) findViewById(R.id.dinner_status_tv);
        mRemainSum = (TextView) findViewById(R.id.dinner_remain_sum_tv);
        mLastConsumeTime = (TextView) findViewById(R.id.dinner_last_consume_time_tv);
        mDCardAction = (TextView) findViewById(R.id.dinner_card_action_tv);

        mSwitchCompat = (SwitchCompat) findViewById(R.id.dinner_consume_switch);
        mSeekBar = (SeekBar) findViewById(R.id.dinner_limit_seek_bar);
        mPayPassword = (EditText) findViewById(R.id.dinner_pay_password);
        mLimitValue = (TextView) findViewById(R.id.dinner_limit_value_tv);

        RelativeLayout consumeRecordLt = (RelativeLayout) findViewById(R.id.dinner_consume_record_layout);
        RelativeLayout chargeRecordLt = (RelativeLayout) findViewById(R.id.dinner_charge_record_layout);

        mIconEye = (TextView) findViewById(R.id.dinner_icon_eye);
        TextView iconInfo1 = (TextView) findViewById(R.id.dinner_icon_info1);
        TextView iconInfo2 = (TextView) findViewById(R.id.dinner_icon_info2);
        TextView iconInfo3 = (TextView) findViewById(R.id.dinner_icon_info3);
        TextView iconList = (TextView) findViewById(R.id.dinner_icon_list);
        TextView iconBinding = (TextView) findViewById(R.id.dinner_icon_binding);
        TextView iconSearch = (TextView) findViewById(R.id.dinner_icon_search);
        TextView iconMinus = (TextView) findViewById(R.id.dinner_icon_minus);
        TextView iconRight1 = (TextView) findViewById(R.id.dinner_icon_right1);
        TextView iconPlus = (TextView) findViewById(R.id.dinner_icon_plus);
        TextView iconRight2 = (TextView) findViewById(R.id.dinner_icon_right2);
        TextView iconInfo4 = (TextView) findViewById(R.id.dinner_icon_info4);
        TextView iconInfo5 = (TextView) findViewById(R.id.dinner_icon_info5);

        mSaveButton = (Button) findViewById(R.id.dinner_save_binding_bt);

        MyUtils.setFont(this, MyUtils.createArrayList(mIconEye, iconInfo1, iconInfo2,
                iconInfo3, iconList, iconBinding, iconSearch, iconMinus, iconRight1,
                iconPlus, iconRight2, iconInfo4, iconInfo5));

        mIconEye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIconEye.getText().toString().equals(getString(R.string.fa_eye))) {
                    mIconEye.setText(getString(R.string.fa_eye_slash));
                    mPayPassword.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                } else {
                    mIconEye.setText(getString(R.string.fa_eye));
                    mPayPassword.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            }
        });
        mDCardAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String alertMessage;
                if ("申请挂失？".equals(mDCardAction.getText().toString())) {
                    alertMessage = "确认要申请挂失你的饭卡吗？挂失后此饭卡将不能再刷机消费。";
                    mActionCode = "2";
                } else if ("解除挂失？".equals(mDCardAction.getText().toString())) {
                    alertMessage = "确认要解除挂失你的饭卡吗？解除挂失后此饭卡可以继续刷机消费。";
                    mActionCode = "1";
                } else {
                    return;
                }
                MyUtils.showAlertDialog(DinnerActivity.this, "操作确认", alertMessage, null,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mDCardAction.setText("处理中...");
                                new LockAndUnlock().start();
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
            }
        });

        mSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        mLimitValue.setText(String.format(getString(R.string.many_yuan), progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );

        mSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setBindingEnable();
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(userModel.getPhoneNumber())) {
                    Toast.makeText(DinnerActivity.this, "保存设定需要先绑定手机长号，请在主界面点击头像设置手机长号", Toast.LENGTH_LONG).show();
                    return;
                }
                int limit = mSeekBar.getProgress();
                if (limit > 100 || limit < 0) {
                    Toast.makeText(DinnerActivity.this, "免密限额必须介于0与100之间", Toast.LENGTH_LONG).show();
                    return;
                }
                mSaveButton.setEnabled(false);
                new SaveBindingInfoThread().start();
            }
        });

        consumeRecordLt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = SearchByDateActivity.newIntent(DinnerActivity.this,
                        getString(R.string.consume_record_check), SearchByDateFragment.Which.CONSUME_RECORD);
                startActivity(intent);
            }
        });

        chargeRecordLt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = SearchByDateActivity.newIntent(DinnerActivity.this,
                        getString(R.string.recharge_record_check), SearchByDateFragment.Which.RECHARGE_RECORD);
                startActivity(intent);
            }
        });

        mDCardAction.requestFocus();
    }


    //显示、隐藏支付密码和消费限额
    private void setBindingEnable() {
        if (mSwitchCompat.isChecked()) {
            mPayPassword.setEnabled(true);
            mSeekBar.setEnabled(true);
        } else {
            mPayPassword.setEnabled(false);
            mSeekBar.setEnabled(false);
        }
    }

    //更新挂失状态
    private void updateCardAction() {
        if ("正常".equals(mDCardStatus.getText().toString())) {
            mDCardAction.setText("申请挂失？");
        } else if ("挂失".equals(mDCardStatus.getText().toString())) {
            mDCardAction.setText("解除挂失？");
        }
    }

    // 获取饭卡基本信息
    private class GetDormInfoThread extends Thread {
        @Override
        public void run() {
            SoapService soap = new SoapService();
            ParamsModel pm = new ParamsModel();
            pm.setArg1(userModel.getCardNumber());

            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                msg.obj = soap.getSoapStringResult("GetDinnerInfo", pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            super.run();
        }
    }

    //挂失/解挂失
    private class LockAndUnlock extends Thread {
        @Override
        public void run() {
            SoapService soap = new SoapService();
            ParamsModel pm = new ParamsModel();
            pm.setArg1(userModel.getCardNumber());
            pm.setArg2(mActionCode);

            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 2;
                msg.obj = soap.getSoapStringResult("LockOrUnLockDinnerCard", pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            super.run();
        }
    }

    //获取绑卡信息
    private class GetBindingInfoThread extends Thread {
        @Override
        public void run() {
            SoapService soap = new SoapService();
            ParamsModel pm = new ParamsModel();
            pm.setArg1(userModel.getCardNumber());

            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 3;
                msg.obj = soap.getSoapStringResult("GetDinnerBindingInfo", pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            super.run();
        }
    }

    //保存绑卡信息
    private class SaveBindingInfoThread extends Thread {
        @Override
        public void run() {
            SoapService soap = new SoapService();
            ParamsModel pm = new ParamsModel();
            pm.setArg1(userModel.getCardNumber());
            pm.setArg2(mSwitchCompat.isChecked() ? "1" : "0");
            pm.setArg3(mPayPassword.getText().toString());
            pm.setArg4(mSeekBar.getProgress() + "");

            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 4;
                msg.obj = soap.getSoapStringResult("SaveDinnerBindingInfo", pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            super.run();
        }
    }

}
