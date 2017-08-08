package cn.com.truly.ic.trulyemp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;

import cn.com.truly.ic.trulyemp.models.DormInfoModel;
import cn.com.truly.ic.trulyemp.models.ParamsModel;
import cn.com.truly.ic.trulyemp.models.SimpleResultModel;
import cn.com.truly.ic.trulyemp.utils.SoapService;

public class DormActivity extends BaseActivity {

    private TextView mDormStatusTv, mAreaNameTv, mDormNumberTv, mInDateTv;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dorm);

        initView();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                SimpleResultModel result = JSON.parseObject(msg.obj.toString(), SimpleResultModel.class);
                switch (msg.what) {
                    case 1:
                        DormInfoModel info = JSON.parseObject(result.getExtra(), DormInfoModel.class);
                        mDormStatusTv.setText(info.getLivingStatus());
                        mInDateTv.setText(info.getInDate());
                        mAreaNameTv.setText(info.getAreaName());
                        mDormNumberTv.setText(info.getDormNumber());

                        PagerAdapter pagerAdapter = new PagerAdapter(
                                getSupportFragmentManager(),
                                info.getFeeMonths().split(","),
                                userModel.getSalaryNumber()
                        );
                        mViewPager.setAdapter(pagerAdapter);
                        mTabLayout.setupWithViewPager(mViewPager);

                        break;
                }
                super.handleMessage(msg);
            }
        };

        new GetDormLivingStatusThread().start();
    }

    private void initView() {
        mDormNumberTv = (TextView) findViewById(R.id.dorm_dorm_number_tv);
        mDormStatusTv = (TextView) findViewById(R.id.dorm_living_status_tv);
        mInDateTv = (TextView) findViewById(R.id.dorm_living_date_tv);
        mAreaNameTv = (TextView) findViewById(R.id.dorm_area_number_tv);

        mTabLayout = (TabLayout) findViewById(R.id.dorm_fee_tab);
        mViewPager = (ViewPager) findViewById(R.id.dorm_fee_pager);
    }

    private class GetDormLivingStatusThread extends Thread {
        @Override
        public void run() {
            SoapService soap = new SoapService();
            ParamsModel pm = new ParamsModel();
            pm.setArg1(userModel.getCardNumber());

            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                msg.obj = soap.getSoapStringResult("GetDormInfo", pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            super.run();
        }
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        private String mSalaryNumber;
        private String[] mTitles;

        public PagerAdapter(FragmentManager fm, String[] titles, String salaryNumber) {
            super(fm);
            this.mTitles = titles;
            this.mSalaryNumber = salaryNumber;
        }

        @Override
        public Fragment getItem(int position) {
            return DormFeeFragment.newInstance(mSalaryNumber, mTitles[position]);
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }
    }

}
