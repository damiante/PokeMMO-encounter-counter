package com.example.pokemmoencountercounter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FloatingViewService extends Service implements View.OnClickListener {

    private static final int MOVEMENT_THRESHOLD = 10;


    private WindowManager mWindowManager;
    private View mFloatingView;
    private View collapsedView;
//    private View expandedView;

    private TextView counterText;
    private int counterVal;


    public FloatingViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        //getting the widget layout from xml using layout inflater
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);

        //setting the layout parameters
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);


        //getting windows services and adding the floating view to it
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);


        //getting the collapsed and expanded view from the floating view
        collapsedView = mFloatingView.findViewById(R.id.layoutCollapsed);
        //expandedView = mFloatingView.findViewById(R.id.layoutExpanded);

        //intialise counter
        counterText = mFloatingView.findViewById(R.id.floating_counter_text);
        initCounter();

        //adding click listener to close button and expanded view
        mFloatingView.findViewById(R.id.buttonClose).setOnClickListener(this);
        mFloatingView.findViewById(R.id.buttonMinus).setOnClickListener(this);
        //expandedView.setOnClickListener(this);

        //adding an touchlistener to make drag movement of the floating widget
        mFloatingView.findViewById(R.id.relativeLayoutParent).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_UP:
                        //when the drag is ended switching the state of the widget
                        if (
                                ((int) (event.getRawX() - initialTouchX) < MOVEMENT_THRESHOLD) &&
                                ((int) (event.getRawY() - initialTouchY) < MOVEMENT_THRESHOLD)
                        ){
                            incrementCounter();
                        }
                        /*else {
                            collapsedView.setVisibility(View.GONE);
                            expandedView.setVisibility(View.VISIBLE);
                        }*/
                        return true;


                    case MotionEvent.ACTION_MOVE:
                        //this code is helping the widget to move around the screen with fingers
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mFloatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        writeCounterValueToInternalStorage();
        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.layoutExpanded:
//                //switching views
//                collapsedView.setVisibility(View.VISIBLE);
//                expandedView.setVisibility(View.GONE);
//                break;
            case R.id.buttonMinus:
                decrementCounter();
                break;

            case R.id.buttonClose:
                //closing the widget
                stopSelf();
                break;
        }
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

    private int readCounterValueFromInternalStorage() throws NumberFormatException, IOException {
        FileInputStream fstream = openFileInput(MainActivity.STORAGE_FILENAME);
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
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(MainActivity.STORAGE_FILENAME, Context.MODE_PRIVATE));
            outputStreamWriter.write(Integer.toString(counterVal));
            outputStreamWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
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
}