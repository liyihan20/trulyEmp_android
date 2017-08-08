package cn.com.truly.ic.trulyemp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import cn.com.truly.ic.trulyemp.app.TrulyEmpApp;
import cn.com.truly.ic.trulyemp.models.UserModel;

/**
 * Created by 110428101 on 2017-07-12.
 */

public class BaseActivity extends AppCompatActivity {

    protected UserModel userModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        TrulyEmpApp app = (TrulyEmpApp) getApplication();
        userModel = app.getUserModel();

        super.onCreate(savedInstanceState);
    }


}
