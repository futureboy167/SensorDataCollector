package com.example.sensordatacollector;


import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity2 extends WearableActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope, magnetometer;

    private Button btRecord, btNext;
    private Spinner spinnerAct, spinnerRep;

    private String[] actions, rep;
    private String action, repeat;
    private String ax, ay, az, gx, gy, gz, mx, my, mz;
    private String filePath, fileName;
    private String subjectID;
    private FileWriter writer;

    private boolean record = false;
    private boolean flagA = false;
    private boolean flagG = false;

    public static final String EXTRA_SUBJECT_ID = "SUBJECT_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent myIntent = getIntent();
        subjectID = myIntent.getStringExtra("SUBJECT_ID");
        Log.d(TAG, subjectID);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, 10000);
            Log.d(TAG, "onCreate: Registered accelerometer listener");
        }

        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, 10000);
            Log.d(TAG, "onCreate: Registered gyroscope listener");
        }

        spinnerAct = findViewById(R.id.spinner);
        populateSpinnerAct();
        spinnerRep = findViewById(R.id.spinner2);
        populateSpinnerRep();

        btRecord = findViewById(R.id.btRecord);
        btRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dataCollector(arg0);
            }
        });

        btNext = findViewById(R.id.btNext);
        btNext.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               nextActivity(v);
           }
        });

        filePath = Environment.getExternalStorageDirectory() + "/SensorData/" + subjectID + "/1";
        File file = new File(Environment.getExternalStorageDirectory()
                + "/SensorData/" + subjectID + "/1");

        if (!file.exists())
            file.mkdirs();
        if (file.listFiles().length == 0)
            btNext.setEnabled(false);

        setAmbientEnabled();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            flagA = true;
            ax = String.format("%.4f", event.values[0]);
            ay = String.format("%.4f", event.values[1]);
            az = String.format("%.4f", event.values[2]);
        }

        if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            flagG = true;
            gx = String.format("%.4f", event.values[0]);
            gy = String.format("%.4f", event.values[1]);
            gz = String.format("%.4f", event.values[2]);
        }

        if (flagA && flagG) {
//            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String date = String.valueOf(System.currentTimeMillis());
            date += "," + ax + "," + ay + "," + az + "," + gx + "," + gy + "," + gz + "\n";

            if (!date.isEmpty() && record)
                dataReader(date);

            flagA = false;
            flagG = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void dataCollector(View arg0) {
        String buttonText = btRecord.getText().toString();

        if (buttonText.equals("Start")) {
            Toast.makeText(getApplicationContext(), "Recording",
                    Toast.LENGTH_SHORT).show();

            action = spinnerAct.getSelectedItem().toString().trim().replaceAll(" ", "");
            repeat = spinnerRep.getSelectedItem().toString().trim();

            Date date = new Date(System.currentTimeMillis());
            String timeMilli = "" + date.getTime();

            fileName = timeMilli + "_" + subjectID + "_" + action + "_" + repeat + ".csv";
            record = true;

            try {
                writer = new FileWriter(new File(filePath, fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
            btRecord.setText("Stop");
            btNext.setEnabled(false);
        }

        else {
            Toast.makeText(getApplicationContext(), "Saving",
                    Toast.LENGTH_SHORT).show();
            record = false;
            btRecord.setText("Start");
            btNext.setEnabled(true);
        }
    }

    public void nextActivity(View v) {
        Intent intent = new Intent(this, MainActivity3.class);

        intent.putExtra(EXTRA_SUBJECT_ID, subjectID);
        startActivity(intent);
    }

    private void dataReader(String data) {
        try {
            writer.write(data);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void populateSpinnerRep() {
        rep = new String[]{"5", "8", "10", "11", "12", "13", "14", "15", "18", "20"};
        ArrayAdapter freqAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, rep);
        freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerRep.setAdapter(freqAdapter);
    }

    private void populateSpinnerAct() {

        actions = new String[]{"Badminton serving", "Bouncing", "Boxing", "Brushing hair", "Brushing teeth",
                "Clapping", "Crunch", "Cutting", "Drumming", "Dusting", "Eating soup", "Grating",
                "Grooming", "Hanging laundry", "Ironing", "Jump shot", "Jumping jack", "Lunge", "Nailing", "Packing",
                "Painting", "Peeling", "Picking up", "Picking up food with chopsticks", "Playing Cajon",
                "Playing Guitar", "Playing Piano", "Pouring", "Pull up", "Push up", "Put dishes into cupboards",
                "Putting clothes in the washing machine", "Reading newspaper", "Rolling flour", "Sawing",
                "Scooping", "Skipping", "Slicing", "Spreading", "Squat", "Stirring", "Sweeping", "Swinging",
                "Typing", "Vacuuming", "Walking", "Washing dishes", "Waving", "Wrenching", "Writing"};
        ArrayAdapter actAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, actions);
        actAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerAct.setAdapter(actAdapter);
    }
}