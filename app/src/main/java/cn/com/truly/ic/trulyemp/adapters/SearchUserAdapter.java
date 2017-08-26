package cn.com.truly.ic.trulyemp.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cn.com.truly.ic.trulyemp.R;
import cn.com.truly.ic.trulyemp.models.SimpleSearchUserModel;

/**
 * Created by 110428101 on 2017-08-15.
 * 用户搜索适配器
 */

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.SearchUserViewHolder> {

    private List<SimpleSearchUserModel> mRecords;
    private Context mContext;

    private CallBacks mCallBacks;

    public SearchUserAdapter(Context context, List<SimpleSearchUserModel> records) {
        mRecords = records;
        mContext = context;
        mCallBacks = (CallBacks) context;
    }

    @Override
    public SearchUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_search_user, parent, false);

        return new SearchUserViewHolder(mCallBacks, v);
    }

    @Override
    public void onBindViewHolder(SearchUserViewHolder holder, int position) {
        SimpleSearchUserModel record = mRecords.get(position);
        holder.bindView(record);
    }

    @Override
    public int getItemCount() {
        return mRecords.size();
    }

    static class SearchUserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mUserName, mUserSex, mUserDep, mUserCardNumber, mUserStatus;
        private CallBacks mBacks;
        private String mCardNumber;

        public SearchUserViewHolder(CallBacks callBacks, View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mUserName = (TextView) itemView.findViewById(R.id.item_user_name);
            mUserSex = (TextView) itemView.findViewById(R.id.item_user_sex);
            mUserDep = (TextView) itemView.findViewById(R.id.item_user_dep_name);
            mUserCardNumber = (TextView) itemView.findViewById(R.id.item_user_card_number);
            mUserStatus = (TextView) itemView.findViewById(R.id.item_user_status);

            mBacks = callBacks;
        }

        public void bindView(SimpleSearchUserModel m) {
            mUserName.setText(m.getUserName());
            mUserSex.setText(m.getSex());
            mUserDep.setText(m.getShortDepName());
            mUserCardNumber.setText(m.getCardNumber());
            mUserStatus.setText(m.getUserStatus());

            mCardNumber=m.getCardNumber();
        }

        @Override
        public void onClick(View v) {
            mBacks.onUserSelected(mCardNumber);
        }
    }

    public interface CallBacks {
        void onUserSelected(String cardNumber);
    }


}
