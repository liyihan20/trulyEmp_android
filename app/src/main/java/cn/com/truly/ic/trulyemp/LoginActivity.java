package cn.com.truly.ic.trulyemp;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cn.com.truly.ic.trulyemp.app.TrulyEmpApp;
import cn.com.truly.ic.trulyemp.models.ParamsModel;
import cn.com.truly.ic.trulyemp.models.SimpleResultModel;
import cn.com.truly.ic.trulyemp.models.UpdateModel;
import cn.com.truly.ic.trulyemp.models.UserModel;
import cn.com.truly.ic.trulyemp.utils.MyUtils;
import cn.com.truly.ic.trulyemp.utils.SoapService;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    public static final int REQUEST_CODE_RESET = 1;
    private static final int REQUEST_CODE_REGISTER = 2;
    private static final String DIALOG_ACTIVATE_USER = "dialog_activate_user";
    private ProgressBar mProgressBar;
    private ScrollView mLoginFormView;
    private EditText mUserName;
    private EditText mPassword;
    private CheckBox mRememberMe;
    private Handler mHandler;
    private String mDeviceID;

    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!MyUtils.isConnectingToInternet(getApplicationContext())) {
            MyUtils.showAlertDialog(this, "网络连接检查", "无法连接到网络，请检查系统设置", false, null, null);
            return;
        }

        setContentView(R.layout.activity_login);
        initView();

        sp = getSharedPreferences("trulyEmpLoginUser", MODE_PRIVATE);
        if (sp != null) {
            mUserName.setText(sp.getString("userName", ""));
            mRememberMe.setChecked(sp.getBoolean("rememberMe", false));
            String password = sp.getString("password", "");
            if (!TextUtils.isEmpty(password)) {
                try {
                    String clearPassword = MyUtils.AES.decrypt(password);
                    mPassword.setText(clearPassword);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                final SimpleResultModel result = JSON.parseObject(msg.obj.toString(), SimpleResultModel.class);
                switch (msg.what) {
                    case 1:
                        if (result.isSuc()) {
                            if ("CHANGE_PASSWORD".equals(result.getMsg())) {
                                //需重置密码
                                showProgress(false);
                                Intent intent = ResetPasswordActivity.newIntent(LoginActivity.this,
                                        mUserName.getText().toString());
                                startActivityForResult(intent, REQUEST_CODE_RESET);
                            } else {
                                Toast.makeText(LoginActivity.this, result.getMsg(), Toast.LENGTH_SHORT).show();
                                loginSuccess(JSON.parseObject(result.getExtra(), UserModel.class));
                            }
                        } else {
                            showProgress(false);
                            Toast.makeText(LoginActivity.this, result.getMsg(), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 2:
                        showProgress(false);
                        if (result.isSuc()) {
                            UserEmailAndIdModel ueModel = JSON.parseObject(result.getExtra(), UserEmailAndIdModel.class);
                            FragmentManager fm = getSupportFragmentManager();
                            ActivateUserFragment dialog = ActivateUserFragment.newInstance(
                                    mUserName.getText().toString(),
                                    ueModel.getEmail(),
                                    ueModel.getIdCode()
                            );
                            dialog.show(fm, DIALOG_ACTIVATE_USER);
                        } else {
                            Toast.makeText(LoginActivity.this, result.getMsg(), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 3:
                        showProgress(false); //登陆操作的进度条在这里才取消
                        final UpdateModel uModel = JSON.parseObject(result.getExtra(), UpdateModel.class);
                        if (uModel.getVersionCode() > getVersionCode()) {
                            MyUtils.showAlertDialog(LoginActivity.this,
                                    "版本升级"
                                    , "检测到有新的版本" + (uModel.isForceUpdate() ? "(重大更新，必须升级)" : "") + ",是否前往下载安装?"
                                    , null,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse(uModel.getApkUrl()));
                                            startActivity(intent);
                                            finish();
                                        }
                                    }, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (uModel.isForceUpdate()) {
                                                finish();
                                            } else {
                                                Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                                startActivity(mainIntent);
                                            }
                                        }
                                    });
                        } else {
                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(mainIntent);
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        };

        mDeviceID = getCombinedDeviceId();

//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) !=
//                PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
//        } else {
//            //授权成功
//        }

    }

    //初始化界面
    private void initView() {
        Button loginButton = (Button) findViewById(R.id.login_button);
        mUserName = (EditText) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);
        mProgressBar = (ProgressBar) findViewById(R.id.login_progress);
        mLoginFormView = (ScrollView) findViewById(R.id.login_form);
        mRememberMe = (CheckBox) findViewById(R.id.remember_me);
        TextView registerLink = (TextView) findViewById(R.id.register_link);
        TextView forgetLink = (TextView) findViewById(R.id.forget_link);
        TextView docLink = (TextView) findViewById(R.id.doc_link);
        TextView questionLink = (TextView) findViewById(R.id.question_link);
        TextView faQuestion = (TextView) findViewById(R.id.icon_question);
        TextView faQuestion2 = (TextView) findViewById(R.id.icon_question2);
        TextView faQuestion3 = (TextView) findViewById(R.id.icon_question3);
        TextView faTip = (TextView) findViewById(R.id.icon_tip);
        TextView faUser = (TextView) findViewById(R.id.icon_user);
        TextView faLock = (TextView) findViewById(R.id.icon_lock);

        //设置font字体
        List<TextView> fontViews = MyUtils.createArrayList(faQuestion, faQuestion2, faTip, faUser, faLock, faQuestion3);
        MyUtils.setFont(this, fontViews);

        //密码的输入法回车事件
        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == R.id.login
                        || actionId == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        //登陆按钮事件
        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        //注册链接事件
        registerLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent regIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(regIntent, REQUEST_CODE_REGISTER);
            }
        });

        //禁用/忘记密码连接事件
        forgetLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mUserName.getText().toString())) {
                    Toast.makeText(LoginActivity.this, "请先输入厂牌号", Toast.LENGTH_LONG).show();
                } else {
                    new UserHasEmailThread().start();
                    showProgress(true);
                }
            }
        });

        //操作指引链接事件
        docLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://59.37.42.23/Emp/Content/doc/员工信息查询操作指引V1.0.pdf"));
                startActivity(intent);
            }
        });

        //有问题发送邮件给管理员
        questionLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //i.setType("text/plain"); //use this line for testing in the emulator
                i.setType("message/rfc822"); // use from live device
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"liyihan.ic@truly.com.cn"});
                i.putExtra(Intent.EXTRA_SUBJECT, "信利员工信息查询系统(android)");
                i.putExtra(Intent.EXTRA_TEXT, "系统管理员,你好：");
                startActivity(Intent.createChooser(i, "请选择发送邮件的程序"));
            }
        });

        mRememberMe.requestFocus();
    }


    private class LoginThread extends Thread {

        @Override
        public void run() {

            SoapService soap = new SoapService();
            ParamsModel pm = new ParamsModel();
            pm.setArg1(mUserName.getText().toString());
            pm.setArg2(mPassword.getText().toString());
            pm.setArg3(mDeviceID);
            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                msg.obj = soap.getSoapStringResult("UserLogin", pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class UserHasEmailThread extends Thread {
        @Override
        public void run() {
            SoapService soap = new SoapService();
            ParamsModel pm = new ParamsModel();
            pm.setArg1(mUserName.getText().toString());
            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 2;
                msg.obj = soap.getSoapStringResult("UserHasEmail", pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class CheckUpdateThread extends Thread {
        @Override
        public void run() {
            SoapService soap = new SoapService();
            ParamsModel pm = new ParamsModel();
            pm.setArg1(mUserName.getText().toString());
            pm.setArg2(mDeviceID);
            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 3;
                msg.obj = soap.getSoapStringResult("GetVersionCode", pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    static class UserEmailAndIdModel {
        private String email;
        private String idCode;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getIdCode() {
            return idCode;
        }

        public void setIdCode(String idCode) {
            this.idCode = idCode;
        }
    }

    private void attemptLogin() {

        mUserName.setError(null);
        mUserName.setError(null);

        if (TextUtils.isEmpty(mUserName.getText().toString())) {
            mUserName.setError(getString(R.string.error_invalid_username));
            mUserName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(mPassword.getText().toString()) || mPassword.getText().toString().length() < 6) {
            mPassword.setError(getString(R.string.error_invalid_password));
            mPassword.requestFocus();
            return;
        }

        showProgress(true);
        new LoginThread().start();
    }

    private void loginSuccess(UserModel user) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("userName", mUserName.getText().toString());
        editor.putBoolean("rememberMe", mRememberMe.isChecked());
        editor.putString("loginTime", MyUtils.getDateTime(new Date()));
        if (mRememberMe.isChecked()) {
            try {
                String encryptedPassword = MyUtils.AES.encrypt(mPassword.getText().toString());
                editor.putString("password", encryptedPassword);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            editor.putString("password", "");
        }
        editor.apply();

        final TrulyEmpApp app = (TrulyEmpApp) getApplication();
        app.setUserModel(user);

        JPushInterface.setAlias(this, user.getCardNumber(), new TagAliasCallback() {
            @Override
            public void gotResult(int i, String s, Set<String> set) {
                Log.d("SetAlias", s);
            }
        });

        HashSet<String> tags = new HashSet<>();
        tags.add("V" + getVersionCode());
        JPushInterface.setTags(this, 0, tags);

        //检查版本更新
        new CheckUpdateThread().start();

    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });

            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressBar.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mProgressBar.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case 1:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    //授权成功
//                } else {
//                    mImei = "0";
//
//                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                    builder.setTitle("当前应用缺少手机权限,请去设置界面打开");
//                    builder.setNegativeButton("取消", null);
//                    builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                            intent.setData(Uri.parse("package:" + getPackageName())); // 根据包名打开对应的设置界面
//                            startActivity(intent);
//                        }
//                    });
//                    builder.create().show();
//
//                }
//                break;
//            default:
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_RESET:
                mPassword.setText("");
                Toast.makeText(LoginActivity.this, "密码重置成功，请重新登陆", Toast.LENGTH_LONG).show();
                break;
            case REQUEST_CODE_REGISTER:
                Toast.makeText(LoginActivity.this, "注册成功，请登录系统", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private int getVersionCode() {
        try {
            return getPackageManager().getPackageInfo("cn.com.truly.ic.trulyemp", PackageManager.GET_CONFIGURATIONS).versionCode;
        } catch (PackageManager.NameNotFoundException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    private String getCombinedDeviceId() {
        String secureId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String macAddress = wm.getConnectionInfo().getMacAddress();
        if (secureId == null) secureId = "";
        if (macAddress == null) macAddress = "";
        return MyUtils.stringToMyMD5(secureId + macAddress);
    }

}

