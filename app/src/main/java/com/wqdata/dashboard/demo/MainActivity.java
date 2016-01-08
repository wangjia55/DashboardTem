package com.wqdata.dashboard.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MeasureDashboardView view = (MeasureDashboardView) findViewById(R.id.measure_view);
        Button button = (Button) findViewById(R.id.button_test);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float value = 32 + new Random().nextInt(12);
                view.setValue(value);
            }
        });

    }
}
