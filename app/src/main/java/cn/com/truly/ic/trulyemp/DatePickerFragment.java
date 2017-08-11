package cn.com.truly.ic.trulyemp;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import cn.com.truly.ic.trulyemp.utils.MyUtils;


public class DatePickerFragment extends DialogFragment {

    private static final String ARG_DATE = "date";
    private static final String ARG_MIN_DATE="min_date";
    public static final String EXTRA_DATE = "cn.com.truly.ic.date_picker_fragment_date";

    private DatePicker mDatePicker;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param date    Parameter 1.
     * @param minDate Parameter 2.
     * @return A new instance of fragment DatePickerFragment.
     */
    public static DatePickerFragment newInstance(Date date, long minDate) {
        DatePickerFragment fragment = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);
        args.putLong(ARG_MIN_DATE,minDate);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_date_picker, null);

        Date currentDate = new Date();
        long minDate=MyUtils.addDays(currentDate,-60).getTime();
        if (getArguments() != null) {
            currentDate = (Date) getArguments().getSerializable(ARG_DATE);
            minDate=getArguments().getLong(ARG_MIN_DATE);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        mDatePicker = (DatePicker) v.findViewById(R.id.frg_date_picker);
        mDatePicker.init(year, month, day, null);
        mDatePicker.setMaxDate(new Date().getTime());
        mDatePicker.setMinDate(minDate);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle("请选择日期")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Date date = new GregorianCalendar(
                                mDatePicker.getYear(),
                                mDatePicker.getMonth(),
                                mDatePicker.getDayOfMonth()
                        ).getTime();
                        sendResult(Activity.RESULT_OK, date);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .create();
    }

    private void sendResult(int resultCode, Date date) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATE, date);

        if (getTargetFragment() != null) {
            getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
        } else {
            getActivity().setResult(resultCode, intent);
        }
    }

}
