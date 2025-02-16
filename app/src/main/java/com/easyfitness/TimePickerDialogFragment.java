package com.easyfitness;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.ikovac.timepickerwithseconds.MyTimePickerDialog;

//@SuppressLint("ValidFragment")
public class TimePickerDialogFragment extends DialogFragment {

    private MyTimePickerDialog.OnTimeSetListener onTimeSetListener;
    private int Hours=0;
    private int Minutes=0;
    private int Seconds=0;

    static public TimePickerDialogFragment newInstance(MyTimePickerDialog.OnTimeSetListener onTimeSetListener, int hour, int min, int sec) {
        TimePickerDialogFragment pickerFragment = new TimePickerDialogFragment();
        pickerFragment.setOnTimeSetListener(onTimeSetListener);


        Bundle bundle = new Bundle();
        bundle.putInt("HOUR", hour);
        bundle.putInt("MINUTE", min);
        bundle.putInt("SECOND", sec);


        pickerFragment.setArguments(bundle);
        return pickerFragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        int hour = bundle.getInt("HOUR");
        int min = bundle.getInt("MINUTE");
        int sec = bundle.getInt("SECOND");

        return new MyTimePickerDialog(getActivity(), onTimeSetListener, hour, min, sec, true);
    }

    static public void setTime(int hour, int min, int sec) {

    }

    private void setOnTimeSetListener(MyTimePickerDialog.OnTimeSetListener listener) {
        this.onTimeSetListener = listener;
    }
}
