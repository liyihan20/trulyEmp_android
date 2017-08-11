package cn.com.truly.ic.trulyemp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cn.com.truly.ic.trulyemp.R;
import cn.com.truly.ic.trulyemp.models.ConsumeRecordModel;

/**
 * Created by 110428101 on 2017-08-09.
 * 饭卡消费记录适配器
 */

public class ConsumeRecordAdapter extends RecyclerView.Adapter<ConsumeRecordAdapter.ConsumeViewHolder> {

    private List<ConsumeRecordModel> mRecords;
    private Context mContext;

    public ConsumeRecordAdapter(Context context, List<ConsumeRecordModel> records) {
        mRecords = records;
        mContext = context;
    }

    @Override
    public ConsumeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_consume_record, parent, false);

        return new ConsumeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ConsumeViewHolder holder, int position) {
        ConsumeRecordModel record = mRecords.get(position);
        holder.bindView(record);
    }

    @Override
    public int getItemCount() {
        return mRecords.size();
    }

    static class ConsumeViewHolder extends RecyclerView.ViewHolder {

        private TextView mConsumeTime, mConsumeMoney, mDinnerType, mPlace;

        public ConsumeViewHolder(View itemView) {
            super(itemView);

            mConsumeTime = (TextView) itemView.findViewById(R.id.item_consume_time_tv);
            mConsumeMoney = (TextView) itemView.findViewById(R.id.item_consume_money_tv);
            mDinnerType = (TextView) itemView.findViewById(R.id.item_consume_type_tv);
            mPlace=(TextView)itemView.findViewById(R.id.item_consume_place_tv);
        }

        public void bindView(ConsumeRecordModel m) {
            mConsumeTime.setText(m.getConsumeTime());
            mConsumeMoney.setText(m.getConsumeMoney());
            mDinnerType.setText(m.getDiningType());
            mPlace.setText(m.getPlace());
        }

    }

}
