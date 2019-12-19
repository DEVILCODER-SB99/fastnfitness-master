package com.easyfitness;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.easyfitness.DebugActivity;
import com.easyfitness.R;


public class StepCountActivity extends AppCompatActivity {
    private Handler mHandler;
    private Runnable _timer1;
    private int stepCounter = 0;
    private int lastStep = 0;
    private boolean showedGoalReach = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_count);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        stepCounter = (int) DebugActivity.mStepCounter;
        mHandler = new Handler();
        startRepeatingTask();
    }

    private void updateView(){
        if(DebugActivity.mStepCounter > stepCounter) {
            stepCounter = (int)DebugActivity.mStepCounter;
            if(stepCounter >= 500 && !showedGoalReach){
                showedGoalReach = true;
                Context context = getApplicationContext();
                CharSequence text = "Good Job! You've reached your goal!";
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
            TextView stepCountStr = (TextView) this.findViewById(R.id.maintv1);
            stepCountStr.setText(new String("Step Count: " + stepCounter));
            TextView progressText = (TextView) this.findViewById(R.id.maintv2);
            progressText.setText(new String("Step Goal: " + 500 + ". Progress: " + stepCounter + " / 500"+"\n"+
                "The only place where success comes before work is in the dictionary."+"\n"+
                "Train Insane or remain the same."+"\n"+
                "Success starts with self-discipline"+"\n"+
                "The body achieves what the mind believes."+"\n"+
                "Making excuses burns zero calories per hour."));

            ProgressBar progressBar = (ProgressBar) this.findViewById(R.id.progressBar);
            ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", lastStep, stepCounter);
            animation.setDuration(500);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
            lastStep = stepCounter;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_step_count, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {



        int id = item.getItemId();
        final Context context = this;


        if (id == R.id.action_debug) {
            Intent intent = new Intent(context, DebugActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                updateView();
            } finally {
                mHandler.postDelayed(mStatusChecker, 1);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }
}
