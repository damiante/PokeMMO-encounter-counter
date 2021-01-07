
package com.example.pokemmoencountercounter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;
    public static final String STORAGE_FILENAME = "counterdata.txt";
    public static final String COUNTER_TITLE_PREFS_KEY = "Counter Title";
    public static final String COUNT_BY_AMOUNT_KEY = "count_by_amount";

    private TextView counterText;
    private TextView counterTitle;
    private Button buttonInc;
    private Button buttonDec;
    private Button buttonRst;
    private int countByAmount;


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
                writeCounterValueToInternalStorage();
            }
        });

        buttonDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrementCounter();
                writeCounterValueToInternalStorage();
            }
        });

        buttonRst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCounterText(0);
                writeCounterValueToInternalStorage();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m){
        getMenuInflater().inflate(R.menu.main_activity_settings_menu, m);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // launch settings activity
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private void askPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }

    public void onClick(View v) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            writeCounterValueToInternalStorage();
            writeCounterTitleToInternalStorage();
            startService(new Intent(MainActivity.this, FloatingViewService.class));
            finish();
        } else if (Settings.canDrawOverlays(this)) {
            writeCounterValueToInternalStorage();
            writeCounterTitleToInternalStorage();
            startService(new Intent(MainActivity.this, FloatingViewService.class));
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
        readCounterTitleFromInternalStorage();

        buttonInc = findViewById(R.id.button_inc);
        buttonDec = findViewById(R.id.button_dec);
        buttonRst = findViewById(R.id.button_rst);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        countByAmount = Integer.parseInt(prefs.getString(COUNT_BY_AMOUNT_KEY, "1"));

    }

    private void initCounter(){
        try{
            setCounterText(readCounterValueFromInternalStorage());
        } catch (NumberFormatException e){
            e.printStackTrace();
            setCounterText(0);
        } catch (IOException e){
            e.printStackTrace();
            setCounterText(0);
        }
    }

    private void incrementCounter(){
        setCounterText(getCounterText() + countByAmount);
    }

    private void decrementCounter(){
        int counterVal = getCounterText();
        if ((counterVal - countByAmount) > 0) {
            setCounterText(counterVal - countByAmount);
        } else {
            setCounterText(0);
        }
    }

    private void setCounterText(int value){
        counterText.setText(Integer.toString(value));
    }

    private int getCounterText(){
        return Integer.parseInt(counterText.getText().toString());
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
            outputStreamWriter.write(counterText.getText().toString());
            outputStreamWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readCounterTitleFromInternalStorage(){
        SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
        counterTitle.setText(prefs.getString(COUNTER_TITLE_PREFS_KEY, "Encounter Count"));
    }

    private void writeCounterTitleToInternalStorage(){
        SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(COUNTER_TITLE_PREFS_KEY, counterTitle.getText().toString());
        editor.apply();
    }

    public void onDestroy() {
        super.onDestroy();
        writeCounterValueToInternalStorage();
        writeCounterTitleToInternalStorage();
    }

    public void onPause() {
        super.onPause();
        writeCounterValueToInternalStorage();
        writeCounterTitleToInternalStorage();
    }

    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        countByAmount = Integer.parseInt(prefs.getString(COUNT_BY_AMOUNT_KEY, "1"));
    }
}