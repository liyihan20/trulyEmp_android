package cn.com.truly.ic.trulyemp.adapters;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cn.com.truly.ic.trulyemp.R;
import cn.com.truly.ic.trulyemp.models.KVModel;
import cn.com.truly.ic.trulyemp.models.SimpleSearchUserModel;
import cn.com.truly.ic.trulyemp.utils.MyUtils;

/**
 * Created by 110428101 on 2017-08-15.
 * key-value适配器
 */

public class KeyValueAdapter extends RecyclerView.Adapter<KeyValueAdapter.KeyValueViewHolder> {

    private List<KVModel> mRecords;
    private Context mContext;
    private
    @StringRes
    int mIconRes;

    public KeyValueAdapter(Context context, List<KVModel> records) {
        mRecords = records;
        mContext = context;
        mIconRes = R.string.fa_caret_right;
    }

    public KeyValueAdapter(Context context, List<KVModel> records, @StringRes int iconRes) {
        mRecords = records;
        mContext = context;
        mIconRes = iconRes;
    }

    @Override
    public KeyValueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_key_value, parent, false);

        return new KeyValueViewHolder(mContext, v);
    }

    @Override
    public void onBindViewHolder(KeyValueViewHolder holder, int position) {
        KVModel record = mRecords.get(position);
        holder.bindView(record, mIconRes);
    }

    @Override
    public int getItemCount() {
        return mRecords.size();
    }

    static class KeyValueViewHolder extends RecyclerView.ViewHolder {

        private Context mContext;
        private TextView mIcon;
        private TextView mKey;
        private TextView mValue;

        private KeyValueViewHolder(Context context, View itemView) {
            super(itemView);

            mContext = context;
            mIcon = (TextView) itemView.findViewById(R.id.item_key_value_icon);
            mKey = (TextView) itemView.findViewById(R.id.item_key_value_key);
            mValue = (TextView) itemView.findViewById(R.id.item_key_value_value);
        }

        private void bindView(KVModel m, @StringRes int iconRes) {
            mIcon.setText(mContext.getString(iconRes));
            MyUtils.setFont(mContext, MyUtils.createArrayList(mIcon));
            mKey.setText(m.getKey());
            mValue.setText(m.getValue());
        }


    }


}
