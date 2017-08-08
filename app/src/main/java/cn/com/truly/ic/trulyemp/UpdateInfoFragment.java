package cn.com.truly.ic.trulyemp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.util.regex.Pattern;

import cn.com.truly.ic.trulyemp.app.TrulyEmpApp;
import cn.com.truly.ic.trulyemp.models.ParamsModel;
import cn.com.truly.ic.trulyemp.models.SimpleResultModel;
import cn.com.truly.ic.trulyemp.models.UserModel;
import cn.com.truly.ic.trulyemp.utils.MyUtils;
import cn.com.truly.ic.trulyemp.utils.SoapService;

/**
 * Created by 110428101 on 2017-08-02.
 */

public class UpdateInfoFragment extends DialogFragment {
    private static final String ARG_USER_ID = "arg_user_id";
    private static final String ARG_MD5_PASSWORD = "arg_md5_password";
    private static final String ARG_PHONE = "arg_phone";
    private static final String ARG_SHORT_PHONE = "arg_short_phone";
    private static final String ARG_EMAIL = "arg_email";

    private int mUserId;
    private String mMd5Password, mPhone, mShortPhone, mEmail;
    private LinearLayout mPasswordLinearLayout;
    private RelativeLayout mDetailRelativeLayout;
    private EditText mPaswordEt, mEmailEt, mPhoneEt, mShortPhoneEt, mNewPasswordEt, mConfirmPasswordEt;

    private Handler mHandler;

    public static UpdateInfoFragment newInstance(int userId, String md5Password, String phone, String shortPhone, String email) {
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, userId);
        args.putString(ARG_MD5_PASSWORD, md5Password);
        args.putString(ARG_PHONE, phone);
        args.putString(ARG_SHORT_PHONE, shortPhone);
        args.putString(ARG_EMAIL, email);

        UpdateInfoFragment fragment = new UpdateInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mUserId = getArguments().getInt(ARG_USER_ID);
        mMd5Password = getArguments().getString(ARG_MD5_PASSWORD);
        mPhone = getArguments().getString(ARG_PHONE);
        mShortPhone = getArguments().getString(ARG_SHORT_PHONE);
        mEmail = getArguments().getString(ARG_EMAIL);

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_update_info, null);

        initView(v);

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle("更新个人信息")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, null)
                .create();

        return alertDialog;
    }

    public void initView(View v) {
        mPasswordLinearLayout = (LinearLayout) v.findViewById(R.id.dialog_info_password_layout);
        mDetailRelativeLayout = (RelativeLayout) v.findViewById(R.id.dialog_info_detail_layout);
        mPaswordEt = (EditText) v.findViewById(R.id.dialog_info_password_edit_text);
        mEmailEt = (EditText) v.findViewById(R.id.dialog_info_email_edit_text);
        mPhoneEt = (EditText) v.findViewById(R.id.dialog_info_phone_edit_text);
        mShortPhoneEt = (EditText) v.findViewById(R.id.dialog_info_short_phone_edit_text);
        mNewPasswordEt = (EditText) v.findViewById(R.id.dialog_info_new_password_edit_text);
        mConfirmPasswordEt = (EditText) v.findViewById(R.id.dialog_info_confirm_password_edit_text);

        TextView iconPasswordTv = (TextView) v.findViewById(R.id.dialog_info_password_icon);
        TextView iconNewPasswordTv = (TextView) v.findViewById(R.id.dialog_info_new_password_icon);
        TextView iconConfirmPasswordTv = (TextView) v.findViewById(R.id.dialog_info_confirm_password_icon);
        TextView iconEmailTv = (TextView) v.findViewById(R.id.dialog_info_email_icon);
        TextView iconPhoneTv = (TextView) v.findViewById(R.id.dialog_info_phone_icon);
        TextView iconShortPhoneTv = (TextView) v.findViewById(R.id.dialog_info_short_phone_icon);

        MyUtils.setFont(getActivity(), MyUtils.createArrayList(iconPasswordTv, iconNewPasswordTv,
                iconConfirmPasswordTv, iconEmailTv, iconPhoneTv, iconShortPhoneTv));

    }

    @Override
    public void onResume() {
        super.onResume();

        final AlertDialog dialog = (AlertDialog) getDialog();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDetailRelativeLayout.getVisibility() == View.GONE) {
                    //验证密码
                    if (TextUtils.isEmpty(mPaswordEt.getText().toString())) {
                        mPaswordEt.setError("密码不能为空");
                    } else if (!mMd5Password.equals(MyUtils.stringToMyMD5(mPaswordEt.getText().toString()))) {
                        mPaswordEt.setError("密码不正确");
                    } else {
                        Toast.makeText(getActivity(), "密码验证通过，请设置个人信息", Toast.LENGTH_LONG).show();
                        mPasswordLinearLayout.setVisibility(View.GONE);
                        mDetailRelativeLayout.setVisibility(View.VISIBLE);
                        mEmailEt.setText(mEmail);
                        mPhoneEt.setText(mPhone);
                        mShortPhoneEt.setText(mShortPhone);
                    }
                } else {
                    //设置个人信息
                    String phoneRegex = "^\\d{11}$";
                    String emailRegex = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";

                    if (!TextUtils.isEmpty(mEmailEt.getText().toString()) &&
                            !Pattern.matches(emailRegex, mEmailEt.getText().toString())) {
                        mEmailEt.setError("邮箱地址不合法");
                        mEmailEt.requestFocus();
                    } else if (!TextUtils.isEmpty(mPhoneEt.getText().toString()) &&
                            !Pattern.matches(phoneRegex, mPhoneEt.getText().toString())) {
                        mPhoneEt.setError("手机长号必须是11位数字");
                        mPhoneEt.requestFocus();
                    } else if (!mNewPasswordEt.getText().toString().equals(mConfirmPasswordEt.getText().toString())) {
                        mConfirmPasswordEt.requestFocus();
                        mConfirmPasswordEt.setError("确认密码与新密码不一致");
                    } else {
                        new UpdateInfoThread().start();
                    }

                }
            }
        });

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        SimpleResultModel result = JSON.parseObject(msg.obj.toString(), SimpleResultModel.class);
                        if (result.isSuc()) {
                            //更新app缓存的userInfo
                            TrulyEmpApp app = (TrulyEmpApp) getActivity().getApplication();
                            UserModel userModel = app.getUserModel();
                            if (!TextUtils.isEmpty(mEmailEt.getText().toString())) {
                                userModel.setEmail(mEmailEt.getText().toString());
                            }
                            if (!TextUtils.isEmpty(mPhoneEt.getText().toString())) {
                                userModel.setPhoneNumber(mPhoneEt.getText().toString());
                            }
                            if (!TextUtils.isEmpty(mShortPhoneEt.getText().toString())) {
                                userModel.setShortPhoneNumber(mShortPhoneEt.getText().toString());
                            }
                            if (!TextUtils.isEmpty(mNewPasswordEt.getText().toString())) {
                                userModel.setMd5Password(MyUtils.stringToMyMD5(mNewPasswordEt.getText().toString()));
                            }

                            dialog.dismiss();
                        }
                        Toast.makeText(getActivity(), result.getMsg(), Toast.LENGTH_LONG).show();
                        break;
                }

                super.handleMessage(msg);
            }
        };
    }

    private class UpdateInfoThread extends Thread {
        @Override
        public void run() {
            SoapService soap = new SoapService(mUserId);
            ParamsModel params = new ParamsModel();
            params.setArg1(mEmailEt.getText().toString());
            params.setArg2(mPhoneEt.getText().toString());
            params.setArg3(mShortPhoneEt.getText().toString());
            params.setArg4(mNewPasswordEt.getText().toString());

            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                msg.obj = soap.getSoapStringResult("UpdateUserInfo", params);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            super.run();
        }
    }

}
