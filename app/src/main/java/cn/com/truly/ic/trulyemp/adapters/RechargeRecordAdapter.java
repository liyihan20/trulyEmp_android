package cn.com.truly.ic.trulyemp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cn.com.truly.ic.trulyemp.R;
import cn.com.truly.ic.trulyemp.models.RechargeRecordModel;
import cn.com.truly.ic.trulyemp.utils.MyUtils;

/**
 * Created by 110428101 on 2017-08-10.
 * 饭卡充值适配器
 */

public class RechargeRecordAdapter extends RecyclerView.Adapter<RechargeRecordAdapter.RechargeViewHolder> {

    private List<RechargeRecordModel> mRecords;
    private Context mContext;

    public RechargeRecordAdapter(Context context, List<RechargeRecordModel> records) {
        mRecords = records;
        mContext = context;
    }

    @Override
    public RechargeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_recharge_record, parent, false);
        return new RechargeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RechargeViewHolder holder, int position) {
        RechargeRecordModel record = mRecords.get(position);
        holder.bindView(record);
    }

    @Override
    public int getItemCount() {
        return mRecords.size();
    }

    static class RechargeViewHolder extends RecyclerView.ViewHolder {

        private TextView mRechargeSumTv, mBeforeSumTv, mAfterSumTv, mRechargeTimeTv, mPlaceTv;

        public RechargeViewHolder(View itemView) {
            super(itemView);

            mRechargeSumTv = (TextView) itemView.findViewById(R.id.item_recharge_sum_tv);
            mBeforeSumTv = (TextView) itemView.findViewById(R.id.item_recharge_before_tv);
            mAfterSumTv = (TextView) itemView.findViewById(R.id.item_recharge_after_tv);
            mRechargeTimeTv = (TextView) itemView.findViewById(R.id.item_recharge_time_tv);
            mPlaceTv = (TextView) itemView.findViewById(R.id.item_recharge_place_tv);
        }

        public void bindView(RechargeRecordModel m) {
            mRechargeSumTv.setText(m.getRechargeSum());
            mBeforeSumTv.setText(m.getBeforeSum());
            mAfterSumTv.setText(m.getAfterSum());
            mRechargeTimeTv.setText(m.getRechargeTime());
            mPlaceTv.setText(m.getPlace());
        }
    }
}
