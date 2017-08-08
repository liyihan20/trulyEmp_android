package cn.com.truly.ic.trulyemp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.util.List;

import cn.com.truly.ic.trulyemp.models.ParamsModel;
import cn.com.truly.ic.trulyemp.models.SimpleResultModel;
import cn.com.truly.ic.trulyemp.utils.MyUtils;
import cn.com.truly.ic.trulyemp.utils.SoapService;

public class RegisterActivity extends AppCompatActivity {

    private EditText mCardNumber, mUserName, mIdNumber, mEmail, mEmailCode, mPhoneNumber;
    private Button mNextStepButton, mSendEmailCodeButton, mFinishRegisterButton;
    private LinearLayout mSecondStepLayout;
    private FirstStepResult firstResult;
    private CountDowntime mTimer;
    private Handler mHandler;
    private String mEmailCodeResult = "", mPhoneNumberResult = "";
    private static final int REQUEST_CODE_SET_PASSWORD = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();
        mTimer = new CountDowntime(60 * 1000, 1000);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                SimpleResultModel result = JSON.parseObject(msg.obj.toString(), SimpleResultModel.class);
                switch (msg.what) {
                    case 1:
                        if (!result.isSuc()) {
                            mNextStepButton.setText(getString(R.string.next_step));
                            mNextStepButton.setEnabled(true);
                        } else {
                            mCardNumber.setEnabled(false);
                            mNextStepButton.setVisibility(View.GONE);
                            mSecondStepLayout.setVisibility(View.VISIBLE);
                            firstResult = JSON.parseObject(result.getExtra(), FirstStepResult.class);
                            if (!TextUtils.isEmpty(firstResult.getEmail())) {
                                mEmail.setText(firstResult.getEmail());
                            } else {
                                mEmail.setText("人事系统没有登记,不能验证邮箱");
                                ((LinearLayout) mSendEmailCodeButton.getParent()).setVisibility(View.GONE);
                            }
                            if (!TextUtils.isEmpty(firstResult.getPhone()) && firstResult.getPhone().length() > 5) {
                                mPhoneNumberResult = firstResult.getPhone();
                                mPhoneNumber.setText(mPhoneNumberResult.substring(0, mPhoneNumberResult.length() - 4));
                            } else {
                                mPhoneNumber.setText("人事系统没有登记");
                                mPhoneNumber.setEnabled(false);
                            }
                        }
                        Toast.makeText(RegisterActivity.this, result.getMsg(), Toast.LENGTH_LONG).show();
                        break;
                    case 2:
                        if (result.isSuc()) {
                            mEmailCodeResult = result.getExtra();
                            mTimer.start();
                        } else {
                            mSendEmailCodeButton.setEnabled(true);
                            mSendEmailCodeButton.setText(getString(R.string.send_email_code));
                        }
                        Toast.makeText(RegisterActivity.this, result.getMsg(), Toast.LENGTH_LONG).show();
                        break;
                    case 3:
                        if (result.isSuc()) {
                            mTimer.cancel();
                            MyUtils.showAlertDialog(RegisterActivity.this, "用户注册", "注册成功！请设置登陆密码", true,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent i = ResetPasswordActivity.newIntent(RegisterActivity.this, mCardNumber.getText().toString());
                                            startActivityForResult(i, REQUEST_CODE_SET_PASSWORD);
                                        }
                                    },null);
                        } else {
                            Toast.makeText(RegisterActivity.this, result.getMsg(), Toast.LENGTH_LONG).show();
                        }
                        break;
                    default:
                }
                super.handleMessage(msg);
            }
        };

    }


    private void initView() {
        TextView mFaCard = (TextView) findViewById(R.id.reg_icon_card);
        TextView mFaUser = (TextView) findViewById(R.id.reg_icon_user);
        TextView mFaIdNumber = (TextView) findViewById(R.id.reg_icon_id_card);
        TextView mFaPhone = (TextView) findViewById(R.id.reg_icon_phone);
        TextView mFaEmail = (TextView) findViewById(R.id.reg_icon_email);
        TextView mFaEmailCode = (TextView) findViewById(R.id.reg_icon_email_code);

        mSecondStepLayout = (LinearLayout) findViewById(R.id.reg_second_step_form);

        mNextStepButton = (Button) findViewById(R.id.reg_next_step_button);
        mSendEmailCodeButton = (Button) findViewById(R.id.reg_send_code_button);
        mFinishRegisterButton = (Button) findViewById(R.id.reg_finish_button);

        mCardNumber = (EditText) findViewById(R.id.reg_card_number);
        mUserName = (EditText) findViewById(R.id.reg_user_name);
        mIdNumber = (EditText) findViewById(R.id.reg_id_number);
        mEmail = (EditText) findViewById(R.id.reg_email);
        mEmailCode = (EditText) findViewById(R.id.reg_email_code);
        mPhoneNumber = (EditText) findViewById(R.id.reg_phone);

        List<TextView> fontViews = MyUtils.createArrayList(mFaCard, mFaUser, mFaIdNumber, mFaPhone,
                mFaEmail, mFaEmailCode);
        MyUtils.setFont(this, fontViews);

        mSecondStepLayout.setVisibility(View.GONE);
        mEmail.setEnabled(false);

        mSendEmailCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mEmail.getText().toString())) {
                    mSendEmailCodeButton.setText("发送中...");
                    mSendEmailCodeButton.setEnabled(false);
                    new SendEmailCodeThread().start();
                }
            }
        });

        mNextStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cardNumber = mCardNumber.getText().toString();
                String userName = mUserName.getText().toString();
                String idNumber = mIdNumber.getText().toString();

                if (TextUtils.isEmpty(cardNumber)) {
                    Toast.makeText(RegisterActivity.this, "厂牌号必须输入", Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(userName)) {
                    Toast.makeText(RegisterActivity.this, "姓名必须输入", Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(idNumber)) {
                    Toast.makeText(RegisterActivity.this, "身份证后六位必须输入", Toast.LENGTH_LONG).show();
                    return;
                }
                if (idNumber.length() != 6) {
                    Toast.makeText(RegisterActivity.this, "身份证必须输入后6位", Toast.LENGTH_LONG).show();
                    return;
                }
                mNextStepButton.setText("验证中...");
                mNextStepButton.setEnabled(false);
                new FirstStepThread().start();
            }
        });

        mFinishRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean okFlag = false;
                String msg = "";
                if (!TextUtils.isEmpty(mEmailCodeResult)) {
                    if (mEmailCodeResult.toLowerCase().equals(mEmailCode.getText().toString().toLowerCase())) {
                        okFlag = true;
                    } else {
                        msg = "邮箱验证码错误";
                    }
                }
                if (!TextUtils.isEmpty(mPhoneNumberResult)) {
                    if (mPhoneNumberResult.equals(mPhoneNumber.getText().toString())) {
                        okFlag = true;
                    } else {
                        msg = "手机号码错误";
                    }
                }
                if (okFlag) {
                    //注册成功
                    new FinishRegisterThread().start();

                } else {
                    Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_SET_PASSWORD:
                setResult(RESULT_OK);
                finish();
                break;
            default:
        }

    }

    private class FirstStepThread extends Thread {
        @Override
        public void run() {
            SoapService soap = new SoapService();
            ParamsModel pm = new ParamsModel();
            pm.setArg1(mCardNumber.getText().toString());
            pm.setArg2(mUserName.getText().toString());
            pm.setArg3(mIdNumber.getText().toString());

            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                msg.obj = soap.getSoapStringResult("RegisterFirst", pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            super.run();
        }
    }

    private class SendEmailCodeThread extends Thread {
        @Override
        public void run() {
            SoapService soap = new SoapService();
            ParamsModel pm = new ParamsModel();
            pm.setArg1(mUserName.getText().toString());
            pm.setArg2(mEmail.getText().toString());

            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 2;
                msg.obj = soap.getSoapStringResult("SendEmailCode", pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            super.run();
        }
    }

    private class FinishRegisterThread extends Thread {
        @Override
        public void run() {
            SoapService soap = new SoapService();
            ParamsModel pm = new ParamsModel();
            pm.setArg1(mCardNumber.getText().toString());

            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 3;
                msg.obj = soap.getSoapStringResult("FinishRegister", pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            super.run();
        }
    }

    static class FirstStepResult {
        private String email;
        private String phone;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    private class CountDowntime extends CountDownTimer {

        public CountDowntime(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            mSendEmailCodeButton.setClickable(false);
            mSendEmailCodeButton.setText(millisUntilFinished / 1000 + "秒");
        }

        @Override
        public void onFinish() {
            mSendEmailCodeButton.setClickable(true);
            mSendEmailCodeButton.setText(getString(R.string.send_email_code));
        }
    }


}
