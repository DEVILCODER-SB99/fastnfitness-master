package com.easyfitness;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.easyfitness.utils.AlarmReceiver;
import com.easyfitness.utils.UnitConverter;
import com.github.lzyzsd.circleprogress.DonutProgress;

import java.text.DecimalFormat;

import gr.antoniom.chronometer.Chronometer;
import gr.antoniom.chronometer.Chronometer.OnChronometerTickListener;

public class CountdownDialogbox extends Dialog implements
    View.OnClickListener {

    public Activity activity;
    public Dialog d;
    public Button exit;
    public Chronometer chrono;
    public OnDismissListener onDismissChrono = dialog -> unregisterAlarm(getContext(), 100101);

    private DonutProgress progressCircle;
    private int lNbSerie = 0;
    private float lTotalSession = 0;
    private float lTotalMachine = 0;
    private int iRestTime = 60;
    private OnChronometerTickListener onChronometerTick = new OnChronometerTickListener() {

        boolean bFirst = true;

        @Override
        public void onChronometerTick(Chronometer chronometer) {



            int secElapsed = (int) (chrono.getTimeElapsed() / 1000);

            progressCircle.setProgress(iRestTime + secElapsed);
/*
            if (secElapsed >= -2) {
                if (!bFirst) {
                    activity.getApplicationContext();
                    Vibrator v = (Vibrator) activity.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {

                        v.vibrate(500);
                    }
                } else {
                    bFirst = false;
                }
            }
*/
            if (secElapsed >= 0) {
                chrono.stop();
                dismiss();
            }
        }
    };

    public CountdownDialogbox(Activity a, int pRestTime) {
        super(a);
        this.activity = a;
        iRestTime = pRestTime;
    }

    public static void registerAlarm(Context context, int uniqueId, long triggerAlarmAt) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, uniqueId, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAlarmAt, pendingIntent);
        }
    }

    public static void unregisterAlarm(Context context, int uniqueId) {
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, uniqueId, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getContext().getResources().getString(R.string.ChronometerLabel));
        setContentView(R.layout.dialog_rest);
        this.setCanceledOnTouchOutside(true);

        exit = findViewById(R.id.btn_exit);

        chrono = findViewById(R.id.chronoValue);
        TextView nbSeries = findViewById(R.id.idNbSeries);
        TextView totalSession = findViewById(R.id.idTotalSession);
        TextView totalMachine = findViewById(R.id.idTotalWeightMachine);

        progressCircle = findViewById(R.id.donut_progress);

        progressCircle.setMax(iRestTime);

        exit.setOnClickListener(this);


/*
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(c.getBaseContext());
        iRestTime = Integer.valueOf(SP.getString("prefRestTimeValue", "60"));
*/


        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getContext());
        int defaultUnit = Integer.valueOf(SP.getString("defaultUnit", "0"));

        DecimalFormat numberFormat = new DecimalFormat("#.##");

        if (defaultUnit == UnitConverter.UNIT_KG) {
            totalMachine.setText(numberFormat.format(lTotalMachine) + " " + this.getContext().getResources().getText(R.string.KgUnitLabel));
            totalSession.setText(numberFormat.format(lTotalSession) + " " + this.getContext().getResources().getText(R.string.KgUnitLabel));
        } else if (defaultUnit == UnitConverter.UNIT_LBS) {
            totalMachine.setText(numberFormat.format(UnitConverter.KgtoLbs(lTotalMachine)) + " " + this.getContext().getResources().getText(R.string.LbsUnitLabel));
            totalSession.setText(numberFormat.format(UnitConverter.KgtoLbs(lTotalSession)) + " " + this.getContext().getResources().getText(R.string.LbsUnitLabel));
        }

        nbSeries.setText(Integer.toString(lNbSerie));

        chrono.setOnChronometerTickListener(onChronometerTick);
        chrono.setBase(SystemClock.elapsedRealtime() + (iRestTime + 1) * 1000);
        chrono.setPrecision(false);
        chrono.start();

        setOnDismissListener(onDismissChrono);

        registerAlarm(getContext(), 100101, SystemClock.elapsedRealtime() + (iRestTime - 2) * 1000);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_exit) {
            chrono.stop();
            chrono.setText("00:00");
            dismiss();
        }
    }

    public void setTotalWeightSession(float pTotalWeight) {
        lTotalSession = pTotalWeight;
    }

    public void setTotalWeightMachine(float pTotalWeight) {
        lTotalMachine = pTotalWeight;
    }

    public void setNbSeries(int pNbSeries) {
        lNbSerie = pNbSeries;
    }

}
