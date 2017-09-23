package com.example.user.wonderweather;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

/**
 * Created by user on 07.07.2017.
 */

public class NotificationService extends Service {

    private boolean coldTempflag = false;
    private boolean flag = false;

    private final String hotNoteTitle = "WonderWeather";
    private final String hotNoteText = "Temperature is too high";
    private final String coldNoteTitle = "WonderWeather";
    private final String coldNoteText = "Temperature is too low";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        Toast.makeText(this,"Service has Started", Toast.LENGTH_SHORT).show();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this){
                    try {
                        for(;;){

                            // send request every 5 second
                            wait(5000);
                            getLocation();
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }
        }); thread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "Service has Stopped", Toast.LENGTH_SHORT).show();
    }


    // SmartLoation
    private void getLocation() {

        SmartLocation.with(this).location().start(new OnLocationUpdatedListener() {
            @Override
            public void onLocationUpdated(Location location) {

                if (location == null) {
                    Toast.makeText(getApplicationContext(), "Location is not founded", Toast.LENGTH_SHORT).show();
                } else {

                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    // getJsonData
                    getJsonData(lat, lng);
                }
            }
        });

    }

    // Ion JsonParsing
    public void getJsonData(double currlat, double currlng){

        //if spData.xml is empty it means send Notification for firs time
        sharedPreferences = getSharedPreferences("spData", MODE_PRIVATE);
        String checkData = sharedPreferences.getString("flagKey","");
        if(checkData.isEmpty()){
            flag = true;
        }

        ///if Data.xml is empty it means send Notification for firs time
        sharedPreferences = getSharedPreferences("Data", MODE_PRIVATE);
        String checkDataFor0= sharedPreferences.getString("flagKeyFor0","");
        if(checkDataFor0.isEmpty()){
            coldTempflag = true;
        }

        if(isNetworkAvailable()){
            Ion.with(this)
                    .load("http://api.openweathermap.org/data/2.5/weather?lat="+currlat+"&lon="+currlng+"&APPID=07c2cec609851b780bbb495834301cf7")
                    .asString().setCallback(new FutureCallback<String>() {

                @Override
                public void onCompleted(Exception e, String result) {
                    if(e != null){
                        Toast.makeText(getApplicationContext(),"Low Internet connection", Toast.LENGTH_SHORT).show();
                    }else{
                        try {

                            JSONObject parentObj = new JSONObject(result);

                            //getTemperature
                            JSONObject mainObj = parentObj.getJSONObject("main");
                            double kelvinTemperature =Double.parseDouble(mainObj.getString("temp"));
                            double celsiusTemp = kelvinTemperature - 273.15;
                            int finalTemp = (int) (celsiusTemp/1);

                            //set desired maximum temperature for notification
                            sharedPreferences = getSharedPreferences("TempData", MODE_PRIVATE);
                            String maxTemp_str= sharedPreferences.getString("MaxTemp","");
                            int maxTemp_int = 40;//default 40°C
                            if(!maxTemp_str.isEmpty()){
                                maxTemp_int = Integer.parseInt(maxTemp_str);
                            }

                            // if temperature greater then desired(40°C)temp send Notification
                            if(finalTemp >= maxTemp_int && flag == true){
                                notification(hotNoteTitle, hotNoteText);
                                flag = false;
                                writeInSharedPref();
                            }
                            //little change of temperature (39°C then 40°C then 41°C ) app doesn't send notification
                            if(finalTemp <= maxTemp_int-5){
                                flag=true;
                                deleteFronSharedPref();
                            }

                            //---------------------------------------------
                            //set desired minimum temperature for notification
                            sharedPreferences = getSharedPreferences("TempData", MODE_PRIVATE);
                            String minTemp_str = sharedPreferences.getString("MinTemp","");
                            int minTemp_int = 0; //default 0°C
                            if(!minTemp_str.isEmpty()){
                                minTemp_int = Integer.parseInt(minTemp_str);
                            }
                            // if temperature lower then desired(0°C)temp send Notification
                            if(finalTemp <= minTemp_int && coldTempflag == true){
                                notification(coldNoteTitle, coldNoteText);
                                coldTempflag = false;
                                writeInSharedPrefFor0();
                            }
                            //little change of temperature (0°C then 1°C then 0°C ) app doesn't send notification
                            if(finalTemp >= minTemp_int+5){
                                coldTempflag = true;
                                deleteFronSharedPrefFor0();
                            }

                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                    }


                }
            });
        }

    }

    // Notification
    public void notification(String title, String text) {
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setSmallIcon(R.drawable.notification_icon);

        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);;

        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, notification);
    }

    //Write in SharedPreferenses for 40C
    public void writeInSharedPref(){

        //for flag (when you kill app Service is restarted and notification arrives second time)
        sharedPreferences = getSharedPreferences("spData", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString("flagKey", "getNotification");
        editor.commit();

    }
    //Delete in SharedPreferenses for 40°C
    public void deleteFronSharedPref(){
        sharedPreferences = getSharedPreferences("spData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    //Write in SharedPreferenses for 0°C
    public void writeInSharedPrefFor0(){

        //for flag (when you kill app Service is restarted and notification arrives second time)
        sharedPreferences = getSharedPreferences("Data", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString("flagKeyFor0", "getNotification");
        editor.commit();

    }

   //Delete in SharedPreferenses for 0°C
    public void deleteFronSharedPrefFor0(){
        sharedPreferences = getSharedPreferences("Data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    //for Checking network
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo != null && activeNetworkInfo.isConnected()){
            return true;
        }else return false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
