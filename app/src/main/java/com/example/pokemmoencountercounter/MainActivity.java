
package com.example.pokemmoencountercounter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;
    public static final String STORAGE_FILENAME = "counterdata.txt";

    private TextView counterText;
    private int counterVal;
    private TextView counterTitle;
    private Button buttonInc;
    private Button buttonDec;
    private Button buttonRst;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            askPermission();
        }

        findViewById(R.id.buttonCreateWidget).setOnClickListener(this);

        buttonInc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementCounter();
            }
        });

        buttonDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrementCounter();
            }
        });

        buttonRst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zeroCounter();
            }
        });
    }


    private void askPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }

    public void onClick(View v) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startService(new Intent(MainActivity.this, FloatingViewService.class));
            writeCounterValueToInternalStorage();
            finish();
        } else if (Settings.canDrawOverlays(this)) {
            startService(new Intent(MainActivity.this, FloatingViewService.class));
            writeCounterValueToInternalStorage();
            finish();
        } else {
            askPermission();
            Toast.makeText(this, "You need System Alert Window Permission to do this", Toast.LENGTH_SHORT).show();
        }
    }

    private void initUI() {
        counterText = findViewById(R.id.counter_text);
        initCounter();
        counterTitle = findViewById(R.id.counter_title);

        buttonInc = findViewById(R.id.button_inc);
        buttonDec = findViewById(R.id.button_dec);
        buttonRst = findViewById(R.id.button_rst);

    }

    private void initCounter(){
        try{
            counterVal = readCounterValueFromInternalStorage();
        } catch (NumberFormatException e){
            e.printStackTrace();
            counterVal = 0;
        } catch (IOException e){
            e.printStackTrace();
            counterVal = 0;
        }
        counterText.setText(counterVal + "");
    }

    private void zeroCounter(){
        counterVal = 0;
        counterText.setText(counterVal + "");
    }

    private void incrementCounter(){
        counterVal++;
        counterText.setText(counterVal + "");
    }

    private void decrementCounter(){
        if (counterVal > 0) {
            counterVal--;
            counterText.setText(counterVal + "");
        }
    }

    private int readCounterValueFromInternalStorage() throws NumberFormatException, IOException {
        FileInputStream fstream = openFileInput(STORAGE_FILENAME);
        StringBuffer sbuffer = new StringBuffer();
        int i;
        while ((i = fstream.read())!= -1){
            sbuffer.append((char)i);
        }
        fstream.close();
        try {
            return Integer.parseInt(String.valueOf(sbuffer));
        } catch (Exception e) {
            e.printStackTrace();
            throw new NumberFormatException("Could not read number from file");
        }
    }

    private void writeCounterValueToInternalStorage(){
        Context context = getApplicationContext();

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(STORAGE_FILENAME, Context.MODE_PRIVATE));
            outputStreamWriter.write(Integer.toString(counterVal));
            outputStreamWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        writeCounterValueToInternalStorage();
    }
}