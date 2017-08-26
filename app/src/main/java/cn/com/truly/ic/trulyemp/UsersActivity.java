package cn.com.truly.ic.trulyemp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.lang.ref.WeakReference;
import java.util.List;

import cn.com.truly.ic.trulyemp.adapters.SearchUserAdapter;
import cn.com.truly.ic.trulyemp.models.ParamsModel;
import cn.com.truly.ic.trulyemp.models.SimpleResultModel;
import cn.com.truly.ic.trulyemp.models.SimpleSearchUserModel;
import cn.com.truly.ic.trulyemp.utils.SoapService;

public class UsersActivity extends BaseActivity implements SearchUserAdapter.CallBacks {

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;

    private Handler mHandler;

    public static Intent newIntent(Context context) {
        return new Intent(context, UsersActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mRecyclerView = (RecyclerView) findViewById(R.id.users_recycle_view);
        mProgressBar=(ProgressBar)findViewById(R.id.user_progress_bar);

        mHandler = new MyHandler(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        new GetUserCountThread().start();

    }

    @Override
    public void onUserSelected(String cardNumber) {
        Intent intent=UserDetailActivity.newIntent(this,cardNumber);
        startActivity(intent);
    }

    private static class MyHandler extends Handler {
        private final WeakReference<UsersActivity> mReference;

        private MyHandler(UsersActivity activity) {
            mReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            UsersActivity target = mReference.get();
            if (target == null) return;

            SimpleResultModel result = JSON.parseObject(msg.obj.toString(), SimpleResultModel.class);
            switch (msg.what) {
                case 1:
                    if (result.isSuc()) {
                        Snackbar.make(target.mRecyclerView, result.getMsg(), BaseTransientBottomBar.LENGTH_LONG).show();
                    }
                    break;
                case 2:
                    target.mProgressBar.setVisibility(View.GONE);
                    if(result.isSuc()){
                        List<SimpleSearchUserModel> userModels= JSON.parseArray(result.getExtra(),SimpleSearchUserModel.class);
                        SearchUserAdapter userAdapter=new SearchUserAdapter(target,userModels);
                        target.mRecyclerView.setAdapter(userAdapter);
                    }
                    Toast.makeText(target,result.getMsg(),Toast.LENGTH_LONG).show();
                    break;
            }

            super.handleMessage(msg);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_users, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.menu_user_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        //searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mProgressBar.setVisibility(View.VISIBLE);
                new SearchUsersThread(query).start();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    private class GetUserCountThread extends Thread {
        @Override
        public void run() {
            SoapService soap = new SoapService(userModel.getUserId());
            ParamsModel pm = new ParamsModel();

            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                msg.obj = soap.getSoapStringResult("GetUserCount", pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            super.run();
        }
    }

    private class SearchUsersThread extends Thread{
        private String mSearchStr;

        private SearchUsersThread(String searchStr){
            mSearchStr=searchStr;
        }

        @Override
        public void run() {
            SoapService soap = new SoapService(userModel.getUserId());
            ParamsModel pm = new ParamsModel();
            pm.setArg1(mSearchStr);

            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 2;
                msg.obj = soap.getSoapStringResult("SearchUsers", pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            super.run();
        }
    }


}
