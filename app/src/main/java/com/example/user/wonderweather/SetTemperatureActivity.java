package com.example.user.wonderweather;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SetTemperatureActivity extends AppCompatActivity {
    EditText maxTempET, minTempET;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_temperature);

        maxTempET = (EditText)findViewById(R.id.maxTempId);
        minTempET = (EditText)findViewById(R.id.minTempId);

        sharedPreferences = getSharedPreferences("TempData", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        String maxTemp_str= sharedPreferences.getString("MaxTemp","");
        String minTemp_str = sharedPreferences.getString("MinTemp","");

        maxTempET.setText(maxTemp_str);
        minTempET.setText(minTemp_str);

    }


    public void saveClick(View view) {
        editor.clear();
        editor.putString("MaxTemp", maxTempET.getText().toString());
        editor.putString("MinTemp", minTempET.getText().toString());
        editor.commit();

        finish();


    }
}
