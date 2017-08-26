package cn.com.truly.ic.trulyemp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.com.truly.ic.trulyemp.utils.MyUtils;

/**
 * Created by 110428101 on 2017-08-14.
 */

public class AdminActivity extends BaseActivity {

    public static Intent newIntent(Context context){
        return new Intent(context,AdminActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_admin);
        TextView iconUser=(TextView)findViewById(R.id.icon_admin_user);
        RelativeLayout userLayout=(RelativeLayout)findViewById(R.id.admin_user_layout);

        MyUtils.setFont(this,MyUtils.createArrayList(iconUser));

        userLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(UsersActivity.newIntent(AdminActivity.this));
            }
        });
    }
}
