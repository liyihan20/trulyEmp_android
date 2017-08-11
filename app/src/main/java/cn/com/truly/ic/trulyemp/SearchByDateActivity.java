package cn.com.truly.ic.trulyemp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by 110428101 on 2017-08-09.
 */

public class SearchByDateActivity extends SingleFragmentActivity {

    private static final String KEY_TITLE = "cn.com.truly.ic.trulyemp.searchByDate.title";
    private static final String KEY_DO_WHICH = "cn.com.truly.ic.trulyemp.searchByDate.do_which";

    @Override
    protected Fragment createFragment() {
        String title = getIntent().getStringExtra(KEY_TITLE);
        SearchByDateFragment.Which doWhich=(SearchByDateFragment.Which)getIntent().getSerializableExtra(KEY_DO_WHICH);

        setTitle(title); //设置标题

        return SearchByDateFragment.newInstance(doWhich,userModel.getCardNumber()); //设置要做什么
    }

    public static Intent newIntent(Context context, String title, SearchByDateFragment.Which doWhich) {
        Intent intent = new Intent(context, SearchByDateActivity.class);
        intent.putExtra(KEY_TITLE, title);
        intent.putExtra(KEY_DO_WHICH,doWhich);
        return intent;
    }

}
