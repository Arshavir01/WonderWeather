package com.example.user.wonderweather;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.wonderweather.search.SearchActivity;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

public class MainActivity extends AppCompatActivity {

    private TextView sunriseTV;
    private TextView sunsetTV;
    private TextView humidityTV;
    private TextView pressureTV;
    private TextView temp_maxTV;
    private TextView temp_minTV;
    private TextView windTV;
    static TextView nameTV;
    static TextView tempTV;
    static ImageView condImgV;
    static ProgressDialog pd;
    private ImageView earthImgV;
    private ImageView turn_OnOff;

    private double lat;
    private double lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        getLocationAndData();

        //for notification service image indicator
        if(isMyServiceRunning(NotificationService.class)){
            turn_OnOff.setImageResource(R.drawable.turnon);
        }

    }

    public void init(){
        pd = new ProgressDialog(this);
        pd.setMessage("Loading...");
        pd.show();

        earthImgV = (ImageView)findViewById(R.id.earthImgId);
        condImgV = (ImageView)findViewById(R.id.condImgId);
        turn_OnOff = (ImageView)findViewById(R.id.turn_offId);

        nameTV = (TextView) findViewById(R.id.cityNameId);
        tempTV = (TextView) findViewById(R.id.tempId);
        sunriseTV = (TextView) findViewById(R.id.sunriseId);
        sunsetTV = (TextView) findViewById(R.id.sunsetId);
        temp_maxTV = (TextView) findViewById(R.id.maxTempId);
        temp_minTV = (TextView) findViewById(R.id.minTempId);
        humidityTV = (TextView) findViewById(R.id.humidityId);
        pressureTV = (TextView) findViewById(R.id.pressureId);
        windTV = (TextView) findViewById(R.id.windId);

    }

    private void getLocationAndData(){
        //check for my phone Internet is turned off/on
        if(isNetworkAvailable()){

            //check for my phone location is turned off/on
            if(SmartLocation.with(MainActivity.this).location().state().isAnyProviderAvailable()) {
                SmartLocation.with(this).location().start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {

                        if (location == null) {
                            Toast.makeText(getApplicationContext(), "Location is not founded", Toast.LENGTH_SHORT).show();
                        } else {

                            lat = location.getLatitude();
                            lng = location.getLongitude();

                            JsonParser_OkHttp parser_okHttp = new JsonParser_OkHttp(MainActivity.this);
                            parser_okHttp.execute("http://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lng+"&APPID=07c2cec609851b780bbb495834301cf7");

                        }
                    }
                });

            }else {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "Turn On Location", Toast.LENGTH_LONG).show();

            } //end Location


        }else {
            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivity(intent);
            Toast.makeText(getApplicationContext(), "Turn On Internet", Toast.LENGTH_LONG).show();

        } //end Network checking


    }

    // Turn On notification
    public void notificationOnClick(View view) {
        if(SmartLocation.with(MainActivity.this).location().state().isAnyProviderAvailable()) {
            //Service started if Location provider is ON
            Intent intent = new Intent(this, NotificationService.class);
            startService(intent);
            turn_OnOff.setImageResource(R.drawable.turnon);

        }
    }
    // Turn Off notification
    public void notificationOffClick(View view) {
        Intent intent = new Intent(this, NotificationService.class);
        stopService(intent);

        turn_OnOff.setImageResource(R.drawable.turnoff);

        //clear data.xml file for lower then 40°C notification
        SharedPreferences preferences = getSharedPreferences("spData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();

        //clear data.xml file for lower then 0°C notification
        SharedPreferences pref = getSharedPreferences("Data", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.clear();
        edit.commit();

    }

    // Button Details
    public void detailsClick(View view) {

           Toast.makeText(this, "Details", Toast.LENGTH_SHORT).show();
           earthImgV.setImageResource(R.drawable.transparent_details);

           sunriseTV.setText("Sunrise:  "  + JsonParser_OkHttp.finalSunrise+" AM");
           sunsetTV.setText("Sunset:  "    + JsonParser_OkHttp.finalSunset+" PM");
           temp_minTV.setText("Min_Temp:  "+ JsonParser_OkHttp.finalTemp_min+ "°C");
           temp_maxTV.setText("Max_Temp:  "+ JsonParser_OkHttp.finalTemp_max+ "°C");
           humidityTV.setText("Humidity:  "+ JsonParser_OkHttp.humidity+"%");
           pressureTV.setText("Pressure:  "+ JsonParser_OkHttp.pressure+"hPa");
           windTV.setText("Wind:  "+ JsonParser_OkHttp.speed+"mps");


    }

    // Button Refresh
    public void refreshClick(View view) {

        pd.show();
        Toast.makeText(this, "Refresh", Toast.LENGTH_SHORT).show();
        getLocationAndData();

        earthImgV.setImageResource(R.drawable.earth__img);
        sunriseTV.setText("");
        sunsetTV.setText("");
        temp_minTV.setText("");
        temp_maxTV.setText("");
        humidityTV.setText("");
        pressureTV.setText("");
        windTV.setText("");
    }

    //Button Search
    public void searchClick(View view) {

        Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);

        earthImgV.setImageResource(R.drawable.earth__img);
        sunriseTV.setText("");
        sunsetTV.setText("");
        temp_minTV.setText("");
        temp_maxTV.setText("");
        humidityTV.setText("");
        pressureTV.setText("");
        windTV.setText("");
    }

    //Button Location
    public void locationClick(View view) {

        getLocationAndData();

        earthImgV.setImageResource(R.drawable.earth__img);
        sunriseTV.setText("");
        sunsetTV.setText("");
        temp_maxTV.setText("");
        humidityTV.setText("");
        pressureTV.setText("");

        temp_minTV.setText("Latitude: "+lat);
        windTV.setText("Longitude: "+lng);

    }

    //go to Notification Activity and set Minimum and Maximum temperature for notification
    public void noteificationActivityClick(View view) {
        Toast.makeText(this, "Set Max/Min Temperature", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, SetTemperatureActivity.class);
        startActivity(intent);
    }

    //for Checking network
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo != null && activeNetworkInfo.isConnected()){
            return true;
        }else return false;
    }

    //checking whether service running or not for indicator image
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


}
