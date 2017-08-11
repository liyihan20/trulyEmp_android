package cn.com.truly.ic.trulyemp;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.com.truly.ic.trulyemp.adapters.ConsumeRecordAdapter;
import cn.com.truly.ic.trulyemp.adapters.RechargeRecordAdapter;
import cn.com.truly.ic.trulyemp.models.ConsumeRecordModel;
import cn.com.truly.ic.trulyemp.models.ParamsModel;
import cn.com.truly.ic.trulyemp.models.RechargeRecordModel;
import cn.com.truly.ic.trulyemp.models.SimpleResultModel;
import cn.com.truly.ic.trulyemp.utils.MyUtils;
import cn.com.truly.ic.trulyemp.utils.SoapService;


public class SearchByDateFragment extends Fragment {

    private static final String ARG_DO_WHICH = "do_which";
    private static final String ARG_CARD_NUMBER = "card_number";

    private static final String TAG_DIALOG_FROM_DATE = "from_date";
    private static final String TAG_DIALOG_TO_DATE = "to_date";

    private static final int REQUEST_FROM_DATE = 1;
    private static final int REQUEST_TO_DATE = 2;

    private Which mDoWhich;
    private String mCardNumber;

    private TextView mFromDateTv, mToDateTv;
    private Button mSearchBt;
    private RecyclerView mRecyclerView;

    private Date mFromDate, mToDate;
    private Handler mHandler;

    private String mUrlMethod;
    private long minDate;

    public SearchByDateFragment() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), 0, 0, 0);
        mFromDate = MyUtils.addDays(calendar.getTime(), -7);
        mToDate = calendar.getTime();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param doWhich Parameter 1.
     * @return A new instance of fragment SearchByDateFragment.
     */
    public static SearchByDateFragment newInstance(Which doWhich, String cardNumber) {
        SearchByDateFragment fragment = new SearchByDateFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DO_WHICH, doWhich);
        args.putString(ARG_CARD_NUMBER, cardNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDoWhich = (Which) getArguments().getSerializable(ARG_DO_WHICH);
            mCardNumber = getArguments().getString(ARG_CARD_NUMBER);
        }
        SetParamsByDoWhich();

        mHandler = new MyHandler(this);
    }

    private static class MyHandler extends Handler {
        private final WeakReference<SearchByDateFragment> mReference;

        private MyHandler(SearchByDateFragment fragment) {
            mReference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            SearchByDateFragment target = mReference.get();
            if (target == null) return;
            SimpleResultModel result = JSON.parseObject(msg.obj.toString(), SimpleResultModel.class);
            switch (msg.what) {
                case 1:
                    if (result.isSuc()) {
                        target.bindData(result.getExtra());
                    }
                    if (!TextUtils.isEmpty(result.getMsg())) {
                        Toast.makeText(target.getActivity(), result.getMsg(), Toast.LENGTH_LONG).show();
                    }

                    target.mSearchBt.setText(target.getString(R.string.begin_search));
                    target.mSearchBt.setEnabled(true);
                    break;
            }

            super.handleMessage(msg);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_search_by_date, container, false);
        initView(v);
        return v;
    }

    private void initView(View v) {
        mFromDateTv = (TextView) v.findViewById(R.id.frg_search_from_date_tv);
        mToDateTv = (TextView) v.findViewById(R.id.frg_search_to_date_tv);
        mSearchBt = (Button) v.findViewById(R.id.frg_search_bt);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.frg_search_recycle_view);
        LinearLayout fromDateLayout = (LinearLayout) v.findViewById(R.id.frg_search_from_date_layout);
        LinearLayout toDateLayout = (LinearLayout) v.findViewById(R.id.frg_search_to_date_layout);

        TextView iconDate1 = (TextView) v.findViewById(R.id.frg_search_icon_date1);
        TextView iconDate2 = (TextView) v.findViewById(R.id.frg_search_icon_date2);
        TextView iconRight1 = (TextView) v.findViewById(R.id.frg_search_icon_right1);
        TextView iconRight2 = (TextView) v.findViewById(R.id.frg_search_icon_right2);

        MyUtils.setFont(getActivity(), MyUtils.createArrayList(iconDate1, iconDate2, iconRight1, iconRight2));

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity())); //设置适配器布局类型

        fromDateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment dialog = DatePickerFragment.newInstance(mFromDate, minDate);
                dialog.setTargetFragment(SearchByDateFragment.this, REQUEST_FROM_DATE);
                dialog.show(getFragmentManager(), TAG_DIALOG_FROM_DATE);
            }
        });

        toDateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment dialog = DatePickerFragment.newInstance(mToDate, minDate);
                dialog.setTargetFragment(SearchByDateFragment.this, REQUEST_TO_DATE);
                dialog.show(getFragmentManager(), TAG_DIALOG_TO_DATE);
            }
        });

        mSearchBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mToDate.before(mFromDate)) {
                    Toast.makeText(getActivity(), "结束日期不能早于开始日期", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!TextUtils.isEmpty(mUrlMethod)) {
                    mSearchBt.setText(getString(R.string.checking));
                    mSearchBt.setEnabled(false);
                    new GetRecordThread(mUrlMethod).start();
                }
            }
        });

        setDate();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_FROM_DATE:
                mFromDate = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
                setDate();
                break;
            case REQUEST_TO_DATE:
                mToDate = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
                setDate();
                break;
        }
    }

    private void setDate() {
        mFromDateTv.setText(MyUtils.getDateStr(mFromDate));
        mToDateTv.setText(MyUtils.getDateStr(mToDate));
    }

    public enum Which {
        CONSUME_RECORD,
        RECHARGE_RECORD
    }


    /**
     * 设置获取数据的方法、选择日期最少值
     */
    private void SetParamsByDoWhich() {
        switch (mDoWhich) {
            case CONSUME_RECORD:
                //消费记录
                mUrlMethod = "GetConsumeRecords";
                minDate = MyUtils.addDays(new Date(), -30).getTime();
                break;
            case RECHARGE_RECORD:
                //充值记录
                mUrlMethod = "GetReChargeRecords";
                minDate = MyUtils.addDays(new Date(), -30 * 6).getTime();
                break;
        }
    }

    private void bindData(String jsonResult) {
        RecyclerView.Adapter adapter;

        switch (mDoWhich) {
            case CONSUME_RECORD:
                //消费记录
                List<ConsumeRecordModel> cRecords = JSON.parseArray(jsonResult, ConsumeRecordModel.class);
                adapter = new ConsumeRecordAdapter(getActivity(), cRecords);
                mRecyclerView.setAdapter(adapter);
                break;
            case RECHARGE_RECORD:
                //充值记录
                List<RechargeRecordModel> rRecords = JSON.parseArray(jsonResult, RechargeRecordModel.class);
                adapter = new RechargeRecordAdapter(getActivity(), rRecords);
                mRecyclerView.setAdapter(adapter);
                break;
        }

    }

    private class GetRecordThread extends Thread {

        private String mMethod;

        private GetRecordThread(String method) {
            mMethod = method;
        }

        @Override
        public void run() {
            SoapService soap = new SoapService();
            ParamsModel pm = new ParamsModel();
            pm.setArg1(mCardNumber);
            pm.setArg2(MyUtils.getDateStr(mFromDate));
            pm.setArg3(MyUtils.getDateStr(mToDate));

            try {
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                msg.obj = soap.getSoapStringResult(mMethod, pm);
                mHandler.sendMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            super.run();

        }
    }

}
