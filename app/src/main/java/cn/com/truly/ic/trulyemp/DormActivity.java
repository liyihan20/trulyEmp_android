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

import org.w3c.dom.Text;

import java.lang.ref.WeakReference;

import cn.com.truly.ic.trulyemp.models.DormInfoModel;
import cn.com.truly.ic.trulyemp.models.ParamsModel;
import cn.com.truly.ic.trulyemp.models.SimpleResultModel;
import cn.com.truly.ic.trulyemp.utils.MyUtils;
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

        mHandler = new MyHandler(this);

        new GetDormLivingStatusThread().start();
    }

    private static class MyHandler extends Handler {
        private final WeakReference<DormActivity> mDormActivityWeakReference;

        private MyHandler(DormActivity activity) {
            mDormActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            DormActivity target = mDormActivityWeakReference.get();
            if (target == null) return;
            SimpleResultModel result = JSON.parseObject(msg.obj.toString(), SimpleResultModel.class);
            switch (msg.what) {
                case 1:
                    DormInfoModel info = JSON.parseObject(result.getExtra(), DormInfoModel.class);
                    target.mDormStatusTv.setText(info.getLivingStatus());
                    target.mInDateTv.setText(info.getInDate());
                    target.mAreaNameTv.setText(info.getAreaName());
                    target.mDormNumberTv.setText(info.getDormNumber());

                    PagerAdapter pagerAdapter = new PagerAdapter(
                            target.getSupportFragmentManager(),
                            info.getFeeMonths().split(","),
                            target.userModel.getSalaryNumber()
                    );
                    target.mViewPager.setAdapter(pagerAdapter);
                    target.mTabLayout.setupWithViewPager(target.mViewPager);

                    break;
            }
            super.handleMessage(msg);
        }

    }

    private void initView() {
        mDormNumberTv = (TextView) findViewById(R.id.dorm_dorm_number_tv);
        mDormStatusTv = (TextView) findViewById(R.id.dorm_living_status_tv);
        mInDateTv = (TextView) findViewById(R.id.dorm_living_date_tv);
        mAreaNameTv = (TextView) findViewById(R.id.dorm_area_number_tv);

        mTabLayout = (TabLayout) findViewById(R.id.dorm_fee_tab);
        mViewPager = (ViewPager) findViewById(R.id.dorm_fee_pager);

        TextView iconHome=(TextView)findViewById(R.id.dorm_icon_living_status);
        TextView iconFee=(TextView)findViewById(R.id.dorm_icon_fee);

        MyUtils.setFont(this,MyUtils.createArrayList(iconHome,iconFee));
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

    private static class PagerAdapter extends FragmentPagerAdapter {

        private String mSalaryNumber;
        private String[] mTitles;

        private PagerAdapter(FragmentManager fm, String[] titles, String salaryNumber) {
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
