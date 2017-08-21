package com.example.user.wonderweather;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.OkHttpClient;

/**
 * Created by user on 09.07.2017.
 */

public class JsonParser_OkHttp extends AsyncTask<String, Void, String>{

    Context context;

    static String finalSunrise, finalSunset;
    static int finalTemp_min, finalTemp_max;
    static double humidity, pressure, speed;

    public JsonParser_OkHttp(Context context){
        this.context = context;

    }

    @Override
    protected String doInBackground(String... params) {
        //OkHttp
        OkHttpClient httpClient = new OkHttpClient();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(params[0])
                .build();

        try {
            okhttp3.Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                return null;
            }

            return response.body().string();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(s != null){

            try {

                JSONObject parentObj = new JSONObject(s);

                //get service stasuse code, if 200 is Ok
                int status = parentObj.getInt("cod");
                if(status == 200){

                    //get location name
                    String name = parentObj.getString("name");
                    MainActivity.nameTV.setText(name);

                    //get temperature
                    JSONObject mainObj = parentObj.getJSONObject("main");
                    double kelvinTemperature =Double.parseDouble(mainObj.getString("temp"));
                    double celsiusTemp = kelvinTemperature - 273.15;
                    double farenhiteTemp = celsiusTemp *9/5+32;

                    int finalFarTemp = (int) (farenhiteTemp/1);
                    int finalTemp = (int) (celsiusTemp/1);
                    MainActivity.tempTV.setText(String.valueOf(finalTemp+"°C | "+finalFarTemp+"°F"));

                    //get temp_min and temp_max
                    double temp_min = Double.parseDouble(mainObj.getString("temp_min")) - 273.15;
                    double temp_max = Double.parseDouble(mainObj.getString("temp_max")) - 273.15;
                    finalTemp_min = (int) (temp_min)/1;
                    finalTemp_max = (int) (temp_max)/1;

                    //get humidity and pressure
                    humidity = Double.parseDouble(mainObj.getString("humidity"));
                    pressure = Double.parseDouble(mainObj.getString("pressure"));

                    //get weather image icon
                    JSONArray jsonArray = parentObj.getJSONArray("weather");
                    JSONObject weatherObj = jsonArray.getJSONObject(0);
                    String icon = weatherObj.getString("icon");

                    //Picasso Library
                    Picasso.with(context).load("http://openweathermap.org/img/w/"+icon+".png").into(MainActivity.condImgV);

                    //get sunrise and sunset
                    JSONObject sysObject = parentObj.getJSONObject("sys");
                    String countryName = sysObject.getString("country");
                    MainActivity.nameTV.append(", "+ countryName);

                    double sunrise = Double.parseDouble(sysObject.getString("sunrise"));
                    double timestempSunrise = sunrise * 1000L;
                    Date date = new Date((long)timestempSunrise);
                    finalSunrise = new SimpleDateFormat("hh:mm").format(date);

                    double sunset = Double.parseDouble(sysObject.getString("sunset"));
                    double timestempSunset = sunset * 1000L;
                    Date date1 = new Date((long)timestempSunset);
                    finalSunset = new SimpleDateFormat("hh:mm").format(date1);
                    // end sun-sun

                    //get wind speed
                    JSONObject windObj = parentObj.getJSONObject("wind");
                    speed = Double.parseDouble(windObj.getString("speed"));
                    //end wind
                    MainActivity.pd.dismiss();

                }else{ Toast.makeText(context, "Invalid Location Name", Toast.LENGTH_SHORT).show();}

            } catch (JSONException e1) {e1.printStackTrace(); }

        }else {
            MainActivity.nameTV.setText("Server Error");
            MainActivity.condImgV.setImageResource(android.R.color.transparent);
            MainActivity.tempTV.setText("Invalid request");

            finalSunrise = null;
            finalSunset = null;
            finalTemp_min = 0;
            finalTemp_max = 0;
            humidity = 0.0;
            pressure = 0.0;
            speed = 0.0;

            MainActivity.pd.dismiss();

        }
    }
}
