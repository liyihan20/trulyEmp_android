package cn.com.truly.ic.trulyemp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;

import cn.com.truly.ic.trulyemp.models.ParamsModel;
import cn.com.truly.ic.trulyemp.models.SalaryInfoModel;
import cn.com.truly.ic.trulyemp.models.SimpleResultModel;
import cn.com.truly.ic.trulyemp.utils.MyUtils;
import cn.com.truly.ic.trulyemp.utils.SoapService;

public class SalaryActivity extends BaseActivity {

    private TextView mSalaryNoTv, mBasicSalaryTv, mSalaryTypeTv, mLastSalaryMonthTv;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ProgressBar mProgressBar;

    private Handler mHandler;

    public static Intent newIntent(Context context) {
        return new Intent(context, SalaryActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary);

        initView();

        mHandler = new MyHandler(this);

        new GetSalaryInfoThread().start();
        new GetSalaryMonthsThread().start();
    }

    private void initView() {

        mTabLayout = (TabLayout) findViewById(R.id.salary_detail_tab);
        mViewPager = (ViewPager) findViewById(R.id.salary_detail_pager);

        mSalaryNoTv = (TextView) findViewById(R.id.salary_no_tv);
        mBasicSalaryTv = (TextView) findViewById(R.id.salary_basic_salary_tv);
        mSalaryTypeTv = (TextView) findViewById(R.id.salary_type_tv);
        mLastSalaryMonthTv = (TextView) findViewById(R.id.salary_last_month_tv);

        mProgressBar=(ProgressBar)findViewById(R.id.salary_pb);

        TextView iconInfo = (TextView) findViewById(R.id.salary_icon_info);
        TextView iconDetail = (TextView) findViewById(R.id.salary_icon_detail);

        MyUtils.setFont(this, MyUtils.createArrayList(iconInfo, iconDetail));
    }

    private static class MyHandler extends Handler {
        private final WeakReference<SalaryActivity> mReference;

        private MyHandler(SalaryActivity activity) {
            mReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            SalaryActivity target = mReference.get();
            if (target == null) return;

            SimpleResultModel result = JSON.parseObject(msg.obj.toString(), SimpleResultModel.class);
            switch (msg.what) {
                case 1:
                    SalaryInfoModel m = JSON.parseObject(result.getExtra(), SalaryInfoModel.class);
                    target.mSalaryNoTv.setText(target.userModel.getSalaryNumber());
                    target.mSalaryTypeTv.setText(m.getSalaryType());
                    target.mBasicSalaryTv.setText(new DecimalFormat("###,###.0##").format(m.getBasicSalary()));
                    target.mLastSalaryMonthTv.setText(m.getLastSalaryDate());

                    break;
                case 2:
                    target.mProgressBar.setVisibility(View.GONE);
                    String[] months = result.getExtra().split(",");
                    ViewPagerAdapter adapter = new ViewPagerAdapter(
                            target.getSupportFragmentManager(),
                            months,
                            target.userModel.getSalaryNumber()
                    );
                    target.mViewPager.setAdapter(adapter);
                    target.mTabLayout.setupWithViewPager(target.mViewPager);
                    break;
            }

            super.handleMessage(msg);
        }
    }

    private class GetSalaryInfoThread extends Thread {
        @Override
        public void run() {
            SoapService soap = new SoapService(userModel.getUserId());
            ParamsModel pm = new ParamsModel();
            pm.setArg1(userModel.getSalaryNumber());

            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                msg.obj = soap.getSoapStringResult("GetSalaryInfo", pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            super.run();
        }
    }

    private class GetSalaryMonthsThread extends Thread {
        @Override
        public void run() {
            SoapService soap = new SoapService(userModel.getUserId());
            ParamsModel pm = new ParamsModel();
            pm.setArg1(userModel.getSalaryNumber());

            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 2;
                msg.obj = soap.getSoapStringResult("GetSalaryMonths", pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            super.run();
        }
    }

    private static class ViewPagerAdapter extends FragmentPagerAdapter {

        private String[] mTitles;
        private String mSalaryNo;

        public ViewPagerAdapter(FragmentManager fm, String[] titles, String salaryNo) {
            super(fm);

            mTitles = titles;
            mSalaryNo = salaryNo;
        }

        @Override
        public Fragment getItem(int position) {
            return SalaryDetailFragment.newInstance(mSalaryNo, mTitles[position]);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }
    }

}
