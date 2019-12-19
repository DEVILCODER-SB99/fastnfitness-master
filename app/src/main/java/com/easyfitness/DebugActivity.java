package com.easyfitness;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;

import android.view.View;

import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;


public class DebugActivity extends AppCompatActivity implements SensorEventListener {

    private static int SMOOTHING_WINDOW_SIZE = 20;

    public static SensorManager mSensorManager;
    public static Sensor mSensorCount, mSensorAcc;
    private float mRawAccelValues[] = new float[3];


    private float mAccelValueHistory[][] = new float[3][SMOOTHING_WINDOW_SIZE];
    private float mRunningAccelTotal[] = new float[3];
    private float mCurAccelAvg[] = new float[3];
    private int mCurReadIndex = 0;

    public static float mStepCounter = 0;
    public static float mStepCounterAndroid = 0;
    public static float mInitialStepCount = 0;

    private double mGraph1LastXValue = 0d;
    private double mGraph2LastXValue = 0d;

    private LineGraphSeries<DataPoint> mSeries1;
    private LineGraphSeries<DataPoint> mSeries2;

    private double lastMag = 0d;
    private double avgMag = 0d;
    private double netMag = 0d;


    private double lastXPoint = 1d;
    double stepThreshold = 1.0d;
    double noiseThreshold = 2.5d;
    private int windowSize = 10;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_more);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorCount = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensorCount, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mSensorAcc, SensorManager.SENSOR_DELAY_UI);



        GraphView graph = (GraphView) this.findViewById(R.id.graph);
        mSeries1 = new LineGraphSeries<>();
        graph.addSeries(mSeries1);
        graph.setTitle("Accelerator Signal");
        graph.getGridLabelRenderer().setVerticalAxisTitle("Signal Value");
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(60);


        GraphView graph2 = (GraphView) this.findViewById(R.id.graph2);
        mSeries2 = new LineGraphSeries<>();
        graph2.setTitle("Smoothed Signal");
        graph2.addSeries(mSeries2);
        graph2.getGridLabelRenderer().setVerticalAxisTitle("Signal Value");
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(60);
    }


    public void onClickBtn(View v)
    {
        Intent i = new Intent(this, MainActivity.class);
        this.startActivity(i);
    }

    @Override
    public void onSensorChanged (SensorEvent e)
    {
        switch (e.sensor.getType()) {
            case Sensor.TYPE_STEP_COUNTER:
                if(mInitialStepCount == 0.0){
                    mInitialStepCount = e.values[0];
                }
                mStepCounterAndroid = e.values[0];
                break;
            case Sensor.TYPE_ACCELEROMETER:
                mRawAccelValues[0] = e.values[0];
                mRawAccelValues[1] = e.values[1];
                mRawAccelValues[2] = e.values[2];

                lastMag = Math.sqrt(Math.pow(mRawAccelValues[0], 2) + Math.pow(mRawAccelValues[1], 2) + Math.pow(mRawAccelValues[2], 2));


                for (int i = 0; i < 3; i++) {
                    mRunningAccelTotal[i] = mRunningAccelTotal[i] - mAccelValueHistory[i][mCurReadIndex];
                    mAccelValueHistory[i][mCurReadIndex] = mRawAccelValues[i];
                    mRunningAccelTotal[i] = mRunningAccelTotal[i] + mAccelValueHistory[i][mCurReadIndex];
                    mCurAccelAvg[i] = mRunningAccelTotal[i] / SMOOTHING_WINDOW_SIZE;
                }
                mCurReadIndex++;
                if(mCurReadIndex >= SMOOTHING_WINDOW_SIZE){
                    mCurReadIndex = 0;
                }

                avgMag = Math.sqrt(Math.pow(mCurAccelAvg[0], 2) + Math.pow(mCurAccelAvg[1], 2) + Math.pow(mCurAccelAvg[2], 2));

                netMag = lastMag - avgMag;


                mGraph1LastXValue += 1d;
                mSeries1.appendData(new DataPoint(mGraph1LastXValue, lastMag), true, 60);

                mGraph2LastXValue += 1d;
                mSeries2.appendData(new DataPoint(mGraph2LastXValue, netMag), true, 60);
        }

        TextView calculatedStep = (TextView) this.findViewById(R.id.tv1);
        TextView androidStep = (TextView) this.findViewById(R.id.tv2);

        peakDetection();

        calculatedStep.setText(new String("Steps Tracked: " + (int)mStepCounter));

        androidStep.setText(new String("Android Steps Tracked: " + (int)(mStepCounterAndroid - mInitialStepCount)));
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void peakDetection(){



        double highestValX = mSeries2.getHighestValueX();

        if(highestValX - lastXPoint < windowSize){
            return;
        }

        Iterator<DataPoint> valuesInWindow = mSeries2.getValues(lastXPoint,highestValX);

        lastXPoint = highestValX;

        double forwardSlope = 0d;
        double downwardSlope = 0d;

        List<DataPoint> dataPointList = new ArrayList<DataPoint>();
        valuesInWindow.forEachRemaining(dataPointList::add);

        for(int i = 0; i<dataPointList.size(); i++){
            if(i == 0) continue;
            else if(i < dataPointList.size() - 1){
                forwardSlope = dataPointList.get(i+1).getY() - dataPointList.get(i).getY();
                downwardSlope = dataPointList.get(i).getY() - dataPointList.get(i - 1).getY();

                if(forwardSlope < 0 && downwardSlope > 0 && dataPointList.get(i).getY() > stepThreshold && dataPointList.get(i).getY() < noiseThreshold){
                    mStepCounter+=1;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
