package cn.com.truly.ic.trulyemp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.lang.ref.WeakReference;

import cn.com.truly.ic.trulyemp.models.ParamsModel;
import cn.com.truly.ic.trulyemp.models.SimpleResultModel;
import cn.com.truly.ic.trulyemp.utils.SoapService;

public class ResetPasswordActivity extends AppCompatActivity {

    private String mCardNumber;
    private static final String EXTRA_CARDNUMBER = "cn.com.truly.ic.trulyemp.card_number";
    private EditText mNewPassword;
    private EditText mNewPasswordAgain;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mCardNumber = getIntent().getStringExtra(EXTRA_CARDNUMBER);

        mNewPassword = (EditText) findViewById(R.id.new_pasword);
        mNewPasswordAgain = (EditText) findViewById(R.id.new_pasword_again);
        Button mConfirmButton = (Button) findViewById(R.id.confirm_change);

        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = mNewPassword.getText().toString();
                String passwordAgain = mNewPasswordAgain.getText().toString();

                if (password.length() < 6) {
                    Toast.makeText(ResetPasswordActivity.this, getString(R.string.error_invalid_password),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                if (!password.equals(passwordAgain)) {
                    Toast.makeText(ResetPasswordActivity.this, getString(R.string.passwords_not_same),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                new ResetThread().start();
            }
        });

        mHandler = new MyHandler(this);

    }

    private static class MyHandler extends Handler {
        private final WeakReference<ResetPasswordActivity> mReference;

        private MyHandler(ResetPasswordActivity activity) {
            mReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ResetPasswordActivity target = mReference.get();
            if (target == null) return;

            switch (msg.what) {
                case 1:
                    SimpleResultModel jsonResult = JSON.parseObject(msg.obj.toString(),
                            SimpleResultModel.class);
                    if (jsonResult.isSuc()) {
                        target.setResult(RESULT_OK);
                        target.finish();
                    } else {
                        Toast.makeText(target, jsonResult.getMsg(),
                                Toast.LENGTH_LONG).show();
                    }
                    break;
                default:

            }
            super.handleMessage(msg);
        }

    }

    private class ResetThread extends Thread {
        @Override
        public void run() {
            SoapService soap = new SoapService();
            ParamsModel pm = new ParamsModel();
            pm.setArg1(mCardNumber);
            pm.setArg2(mNewPassword.getText().toString());
            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                msg.obj = soap.getSoapStringResult("ResetPassword", pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }


    public static Intent newIntent(Context context, String cardNumber) {
        Intent i = new Intent(context, ResetPasswordActivity.class);
        i.putExtra(EXTRA_CARDNUMBER, cardNumber);
        return i;
    }

}
