package cn.com.truly.ic.trulyemp;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.alibaba.fastjson.JSON;

import java.lang.ref.WeakReference;
import java.util.List;

import cn.com.truly.ic.trulyemp.adapters.KeyValueAdapter;
import cn.com.truly.ic.trulyemp.models.KVModel;
import cn.com.truly.ic.trulyemp.models.ParamsModel;
import cn.com.truly.ic.trulyemp.models.SimpleResultModel;
import cn.com.truly.ic.trulyemp.utils.SoapService;


public class SalaryDetailFragment extends Fragment {
    private static final String ARG_SALARY_NO = "salary_no";
    private static final String ARG_MONTH = "month";

    private String mSalaryNo;
    private String mMonth;

    private RecyclerView mSummaryRv, mMoreDetailRv;
    private Button mCheckMoreBt;
    private ProgressBar mProgressBar;

    private Handler mHandler;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param salaryNo Parameter 1.
     * @param month    Parameter 2.
     * @return A new instance of fragment SalaryDetailFragment.
     */
    public static SalaryDetailFragment newInstance(String salaryNo, String month) {
        SalaryDetailFragment fragment = new SalaryDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SALARY_NO, salaryNo);
        args.putString(ARG_MONTH, month);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSalaryNo = getArguments().getString(ARG_SALARY_NO);
            mMonth = getArguments().getString(ARG_MONTH);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_salary_detail, container, false);

        mSummaryRv = (RecyclerView) v.findViewById(R.id.frg_salary_detail_summary_rv);
        mMoreDetailRv = (RecyclerView) v.findViewById(R.id.frg_salary_detail_more_rv);
        mCheckMoreBt = (Button) v.findViewById(R.id.frg_salary_detail_check_more_bt);
        mProgressBar=(ProgressBar)v.findViewById(R.id.frg_salary_detail_pb);

        mSummaryRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMoreDetailRv.setLayoutManager(new LinearLayoutManager(getActivity()));

        mHandler = new MyHandler(this);
        new GetSalarySummaryThread().start();

        mCheckMoreBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                mCheckMoreBt.setVisibility(View.GONE);

                new GetSalaryDetailThread().start();
            }
        });

        return v;
    }

    private static class MyHandler extends Handler {
        private final WeakReference<SalaryDetailFragment> mReference;

        private MyHandler(SalaryDetailFragment fragment) {
            mReference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            SalaryDetailFragment target = mReference.get();
            if (target == null) return;

            SimpleResultModel result= JSON.parseObject(msg.obj.toString(),SimpleResultModel.class);
            switch (msg.what) {
                case 1:
                    target.mProgressBar.setVisibility(View.GONE);
                    target.mCheckMoreBt.setVisibility(View.VISIBLE);

                    List<KVModel> list= JSON.parseArray(result.getExtra(),KVModel.class);
                    KeyValueAdapter adapter=new KeyValueAdapter(target.getActivity(),list,R.string.fa_chevron_circle_right);
                    target.mSummaryRv.setAdapter(adapter);
                    break;

                case 2:
                    target.mProgressBar.setVisibility(View.GONE);

                    List<KVModel> dList=JSON.parseArray(result.getExtra(),KVModel.class);
                    KeyValueAdapter dAdapter=new KeyValueAdapter(target.getActivity(),dList,R.string.fa_chevron_circle_right);
                    target.mMoreDetailRv.setAdapter(dAdapter);
                    break;
            }

            super.handleMessage(msg);
        }
    }

    private class GetSalarySummaryThread extends Thread {
        @Override
        public void run() {
            SoapService soap = new SoapService();
            ParamsModel pm = new ParamsModel();
            pm.setArg1(mSalaryNo);
            pm.setArg2(mMonth);

            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                msg.obj = soap.getSoapStringResult("GetSalarySummary", pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            super.run();
        }
    }

    private class GetSalaryDetailThread extends Thread {
        @Override
        public void run() {
            SoapService soap = new SoapService();
            ParamsModel pm = new ParamsModel();
            pm.setArg1(mSalaryNo);
            pm.setArg2(mMonth);

            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 2;
                msg.obj = soap.getSoapStringResult("GetSalaryDetail", pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            super.run();
        }
    }

}
