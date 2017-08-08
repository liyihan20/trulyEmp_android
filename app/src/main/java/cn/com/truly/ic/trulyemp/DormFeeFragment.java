package cn.com.truly.ic.trulyemp;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import cn.com.truly.ic.trulyemp.models.DormFeeModel;
import cn.com.truly.ic.trulyemp.models.ParamsModel;
import cn.com.truly.ic.trulyemp.models.SimpleResultModel;
import cn.com.truly.ic.trulyemp.utils.SoapService;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DormFeeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DormFeeFragment extends Fragment {

    private static final String TAG = "DormFeeFragment";
    private static final String ARG_SALARY_NUMBER = "salaryNumber";
    private static final String ARG_YEAR_MONTH = "yearMonth";

    private String mSalaryNumber;
    private String mYearMonth;

    private ProgressBar mProgressBar;
    private RelativeLayout mRelativeLayout;

    private TextView mYearMonthTv, mDormNumberTv, mRentTv, mManagementTv, mReparTv,
            mEleTv, mWaterTv, mFineTv, mOtherTv, mCommentTv, mTotalTv,mNoFeeTip;

    private Handler mHandler;

    public DormFeeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param salaryNumber Parameter 1.
     * @param yearMonth    Parameter 2.
     * @return A new instance of fragment DormFeeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DormFeeFragment newInstance(String salaryNumber, String yearMonth) {
        DormFeeFragment fragment = new DormFeeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SALARY_NUMBER, salaryNumber);
        args.putString(ARG_YEAR_MONTH, yearMonth);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSalaryNumber = getArguments().getString(ARG_SALARY_NUMBER);
            mYearMonth = getArguments().getString(ARG_YEAR_MONTH);
        }

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        SimpleResultModel result = JSON.parseObject(msg.obj.toString(), SimpleResultModel.class);
                        if (!result.isSuc()) {
                            mNoFeeTip.setVisibility(View.VISIBLE);
                        } else {
                            setDormFee(JSON.parseObject(result.getExtra(), DormFeeModel.class));
                            mRelativeLayout.setVisibility(View.VISIBLE);
                        }
                        mProgressBar.setVisibility(View.GONE);
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_dorm_fee, container, false);
        initView(v);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        new GetDormFeeThread().start();
    }

    private void initView(View v) {

        mProgressBar = (ProgressBar) v.findViewById(R.id.frg_dorm_progress_bar);
        mRelativeLayout = (RelativeLayout) v.findViewById(R.id.frg_dorm_fee_layout);
        mNoFeeTip=(TextView)v.findViewById(R.id.frg_dorm_no_fee_tip);

        mYearMonthTv = (TextView) v.findViewById(R.id.frg_dorm_fee_year_month);
        mDormNumberTv = (TextView) v.findViewById(R.id.frg_dorm_fee_dorm_number);
        mRentTv = (TextView) v.findViewById(R.id.frg_dorm_fee_rent);
        mManagementTv = (TextView) v.findViewById(R.id.frg_dorm_fee_management);
        mEleTv = (TextView) v.findViewById(R.id.frg_dorm_fee_ele);
        mWaterTv = (TextView) v.findViewById(R.id.frg_dorm_fee_water);
        mReparTv = (TextView) v.findViewById(R.id.frg_dorm_fee_repair);
        mFineTv = (TextView) v.findViewById(R.id.frg_dorm_fee_fine);
        mOtherTv = (TextView) v.findViewById(R.id.frg_dorm_fee_other);
        mCommentTv = (TextView) v.findViewById(R.id.frg_dorm_fee_comment);
        mTotalTv = (TextView) v.findViewById(R.id.frg_dorm_fee_total);

        mRelativeLayout.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);

        mYearMonthTv.setText(mYearMonth);
    }

    private void setDormFee(DormFeeModel m) {
        mYearMonthTv.setText(m.getYearMonth());
        mDormNumberTv.setText(m.getDormNumber());
        mRentTv.setText(m.getRent());
        mManagementTv.setText(m.getManagement());
        mEleTv.setText(m.getElec());
        mWaterTv.setText(m.getWater());
        mReparTv.setText(m.getRepair());
        mFineTv.setText(m.getFine());
        mOtherTv.setText(m.getOthers());
        mCommentTv.setText(m.getComment());
        mTotalTv.setText(m.getTotal());
    }

    private class GetDormFeeThread extends Thread {
        @Override
        public void run() {
            SoapService soap = new SoapService();
            ParamsModel pm = new ParamsModel();
            pm.setArg1(mSalaryNumber);
            pm.setArg2(mYearMonth);

            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                msg.obj = soap.getSoapStringResult("GetDormFee", pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            super.run();
        }
    }

}
