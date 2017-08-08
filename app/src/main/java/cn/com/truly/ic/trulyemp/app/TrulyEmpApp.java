package cn.com.truly.ic.trulyemp.app;


import android.app.Application;
import android.util.Log;

import com.beardedhen.androidbootstrap.TypefaceProvider;

import cn.com.truly.ic.trulyemp.models.UserModel;
import cn.jpush.android.api.JPushInterface;

/**
 * Created by 110428101 on 2017-07-05.
 */

public class TrulyEmpApp extends Application {

    private static final String TAG = "TrulyEmpApp";
    private UserModel mUserModel;

    public UserModel getUserModel() {
        return mUserModel;
    }

    public void setUserModel(UserModel userModel) {
        mUserModel = userModel;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        TypefaceProvider.registerDefaultIconSets();

        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
    }
}



