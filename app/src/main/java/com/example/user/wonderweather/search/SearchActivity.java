package com.example.user.wonderweather.search;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.wonderweather.JsonParser_OkHttp;
import com.example.user.wonderweather.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {
    private AutoCompleteTextView searchET;
    private TextView localityTV, latTV,lngTV;
    private ImageButton closeBtn;
    private String input;
    static ArrayAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        init();

        //autocomplite
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                input = s.toString();
                if(input.isEmpty()){
                    //if input empty erase close button
                    closeBtn.setImageResource(android.R.color.transparent);

                }else {
                    GooglePlace_autoComplite parser = new GooglePlace_autoComplite(SearchActivity.this, searchET);
                    parser.execute("https://maps.googleapis.com/maps/api/place/autocomplete/json?input="+input+"&types=geocode&key=AIzaSyD9D8z0lx4esNYWiKGyOpnFp_Y9wE2Ct7w");
                    closeBtn.setImageResource(R.drawable.close4);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        //for hiding keybord
        searchET.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                hideKeybord();
            }
        });

    }

    public void init(){
        closeBtn = (ImageButton)findViewById(R.id.CloseButtonId);
        localityTV = (TextView)findViewById(R.id.localityId);
        latTV = (TextView)findViewById(R.id.latitudeId);
        lngTV = (TextView)findViewById(R.id.longitudeId);
        searchET = (AutoCompleteTextView) findViewById(R.id.searchEditText);


    }
    //Search Button
    public void searchBtnClick(View view) {

           Geocoder geocoder = new Geocoder(this);
            List<Address> listAddress = null;
            String searchInput_s = searchET.getText().toString();

            try {
                listAddress = geocoder.getFromLocationName(searchInput_s, 1);


            } catch (IOException e) {
                e.printStackTrace();
            }


            if (listAddress == null || listAddress.isEmpty()) {
                Toast.makeText(this, "Location is not founded", Toast.LENGTH_SHORT).show();

            } else {

                Address add = listAddress.get(0);

                double latSrch_s = add.getLatitude();
                double lngSrch_s = add.getLongitude();

                String locality_s = add.getLocality();

                JsonParser_OkHttp parser_okHttp = new JsonParser_OkHttp(getApplicationContext());
                parser_okHttp.execute("http://api.openweathermap.org/data/2.5/weather?lat="+latSrch_s+"&lon="+lngSrch_s+"&APPID=07c2cec609851b780bbb495834301cf7");

                Toast.makeText(this, locality_s, Toast.LENGTH_LONG).show();
                finish();
            }

    }


    //Button Taj Mahal
    public void tajmahalButton(View view) {
        searchET.setText(getResources().getString(R.string.taj_name));
        localityTV.setText("");
        latTV.setText("");
        lngTV.setText("");
    }

    // Get latitude and longitude
    public void locationClick(View view) {
        //Search
        String searchInput_l = searchET.getText().toString();

        latTV.setText("");
        lngTV.setText("");
        localityTV.setText("");

        Geocoder geocoder = new Geocoder(this);
        List<Address> listAddress = null;

        try {
            listAddress = geocoder.getFromLocationName(searchInput_l, 1);

        } catch (IOException e) {
            e.printStackTrace();
        }


        if(listAddress == null || listAddress.isEmpty()){

            Toast.makeText(this, "Location is not founded", Toast.LENGTH_SHORT).show();
            localityTV.setText("");
            latTV.setText("");
            lngTV.setText("");

        }else{
            Address add  = listAddress.get(0);

            double latSrch_l = add.getLatitude();
            double lngSrch_l = add.getLongitude();


            String locality_l = add.getLocality();

            localityTV.setText(locality_l);
            latTV.setText("Lat:  "+String.valueOf(latSrch_l));
            lngTV.setText("Lng:  "+String.valueOf(lngSrch_l));

        }


    }

    //microfon button
    public void micBtnClick(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(intent, 0);
        }catch (ActivityNotFoundException e){
            Toast.makeText(getApplicationContext(),"Your device doesn't support speech input",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       if(requestCode == 0 && data != null){
           ArrayList<String> list = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
           if(!list.isEmpty()){
               searchET.setText(list.get(0));
           }
       }
    }

    //hideKeybord
    public void hideKeybord(){
        View v = this.getCurrentFocus();
        if(v != null){
            InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(v.getWindowToken(),0);
        }


    }

    //close Button, delete autocomplite text
    public void closeClick(View view) {
        searchET.setText("");
        localityTV.setText("");
        latTV.setText("");
        lngTV.setText("");
    }
}
