package cn.com.truly.ic.trulyemp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import cn.com.truly.ic.trulyemp.models.ParamsModel;
import cn.com.truly.ic.trulyemp.models.SimpleResultModel;
import cn.com.truly.ic.trulyemp.utils.SoapService;

/**
 * Created by 110428101 on 2017-07-25.
 */

public class ActivateUserFragment extends DialogFragment {
    private static final String ARG_CARD_NUMBER = "card_number";
    private static final String ARG_EMAIL_ADDR = "email_addr";
    private static final String ARG_ID_CODE = "id_code";
    private String mCardNumber, mEmailAddr, mIdCode, mEmailCode = "";
    private static final String TAG = "ActivateUserFragment";

    private Button mActivateEmailCodeButton;
    private CheckBox mResetOnlyCheckBox, mActivateOnlyCheckBox;
    private EditText mActivateEmailEditText, mActivateEmailCodeEditText, mActivateIdCodeEditText;

    private Handler mHandler;
    private CountDownTimer mTimer;
    private Context mContext;

    public static ActivateUserFragment newInstance(String cardNumber, String emailAddr, String idCode) {
        Bundle args = new Bundle();
        args.putString(ARG_CARD_NUMBER, cardNumber);
        args.putString(ARG_EMAIL_ADDR, emailAddr);
        args.putString(ARG_ID_CODE, idCode);

        ActivateUserFragment fragment = new ActivateUserFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mCardNumber = getArguments().getString(ARG_CARD_NUMBER);
        mEmailAddr = getArguments().getString(ARG_EMAIL_ADDR);
        mIdCode = getArguments().getString(ARG_ID_CODE);

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_activate_user, null);
        mActivateEmailCodeButton = (Button) v.findViewById(R.id.activate_send_email_code_button);
        mResetOnlyCheckBox = (CheckBox) v.findViewById(R.id.reset_only_checkbox);
        mActivateOnlyCheckBox = (CheckBox) v.findViewById(R.id.activate_only_checkbox);
        mActivateEmailEditText = (EditText) v.findViewById(R.id.activate_email_edit_text);
        mActivateEmailCodeEditText = (EditText) v.findViewById(R.id.activate_email_code_edit_text);
        mActivateIdCodeEditText = (EditText) v.findViewById(R.id.activate_id_code_edit_text);

        Log.v(TAG, mCardNumber + ":" + mEmailAddr);

        mActivateEmailEditText.setText(mEmailAddr);
        mActivateEmailEditText.setEnabled(false);
        mActivateEmailCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivateEmailCodeButton.setText("发送中...");
                mActivateEmailCodeButton.setClickable(false);
                new SendEmailCodeThread().start();
            }
        });


        mTimer = new CountDowntime(60 * 1000, 1000);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                SimpleResultModel result = JSON.parseObject(msg.obj.toString(), SimpleResultModel.class);
                switch (msg.what) {
                    case 1:
                        if (result.isSuc()) {
                            mEmailCode = result.getExtra();
                            mTimer.start();
                        } else {
                            mActivateEmailCodeButton.setText(R.string.send_email_code);
                            mActivateEmailCodeButton.setClickable(true);
                        }
                        Toast.makeText(mContext, result.getMsg(), Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        break;
                }
                super.handleMessage(msg);
            }
        };

        AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setView(v)
                .setTitle("解禁 & 重置密码")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .create();

        return alertDialog;
    }

    @Override
    public void onPause() {
        super.onPause();
        mTimer.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();

        final AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            Button pButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            pButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String msg;
                    boolean isReset = mResetOnlyCheckBox.isChecked();
                    boolean isActivated = mActivateOnlyCheckBox.isChecked();
                    if (!mIdCode.equals(mActivateIdCodeEditText.getText().toString())) {
                        msg = "身份证号码后6位不正确";
                    } else if (mEmailCode.equals("") ||
                            !mEmailCode.toLowerCase().equals(mActivateEmailCodeEditText.getText().toString().toLowerCase())) {
                        msg = "邮箱验证码不正确";
                    } else if (!isReset && !isActivated) {
                        msg = "用户解禁与重置密码必须至少勾选一项";
                    } else {
                        if (isReset && isActivated) {
                            msg = "验证成功，用户已解禁，请重新设置密码";
                        } else if (isReset) {
                            msg = "验证成功，请重新设置密码";
                        } else {
                            msg = "验证成功，用户已解禁，请重新登陆";
                        }

                        if (isActivated) {
                            new ActivateUserThread().start();
                        }

                        if (isReset) {
                            Intent intent = ResetPasswordActivity.newIntent(getActivity(), mCardNumber);
                            getActivity().startActivityForResult(intent, LoginActivity.REQUEST_CODE_RESET);
                        }
                        mTimer.cancel();
                        dialog.dismiss();

                    }
                    if (mContext != null) {
                        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private class SendEmailCodeThread extends Thread {
        @Override
        public void run() {
            SoapService soap = new SoapService();
            ParamsModel pm = new ParamsModel();
            pm.setArg1(mCardNumber);
            pm.setArg2(mEmailAddr);

            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                msg.obj = soap.getSoapStringResult("SendEmailCode", pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            super.run();
        }
    }

    private class ActivateUserThread extends Thread {
        @Override
        public void run() {
            SoapService soap = new SoapService();
            ParamsModel pm = new ParamsModel();
            pm.setArg1(mCardNumber);

            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 2;
                msg.obj = soap.getSoapStringResult("ActivateUser", pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            super.run();
        }
    }

    private class CountDowntime extends CountDownTimer {

        public CountDowntime(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            mActivateEmailCodeButton.setClickable(false);
            mActivateEmailCodeButton.setText(millisUntilFinished / 1000 + "秒");
        }

        @Override
        public void onFinish() {
            mActivateEmailCodeButton.setClickable(true);
            mActivateEmailCodeButton.setText(getString(R.string.send_email_code));
        }
    }


}
