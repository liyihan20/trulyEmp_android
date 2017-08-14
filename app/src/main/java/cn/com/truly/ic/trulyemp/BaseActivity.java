package cn.com.truly.ic.trulyemp;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import cn.com.truly.ic.trulyemp.app.TrulyEmpApp;
import cn.com.truly.ic.trulyemp.models.UserModel;

/**
 * Created by 110428101 on 2017-07-12.
 */

public class BaseActivity extends AppCompatActivity {

    protected UserModel userModel;
    private static final String SAVE_USER_MODEL="user_model";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        TrulyEmpApp app = (TrulyEmpApp) getApplication();
        if(app!=null && app.getUserModel()!=null) {
            userModel = app.getUserModel();
        }else if(savedInstanceState!=null){
            userModel=(UserModel)savedInstanceState.getSerializable(SAVE_USER_MODEL);
        }else{
            startActivity(new Intent(this,LoginActivity.class));
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putSerializable(SAVE_USER_MODEL,userModel);
        super.onSaveInstanceState(outState, outPersistentState);
    }
}
