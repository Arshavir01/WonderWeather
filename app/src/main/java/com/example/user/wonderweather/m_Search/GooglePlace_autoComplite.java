package com.example.user.wonderweather.m_Search;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by user on 09.08.2017.
 */

public class GooglePlace_autoComplite extends AsyncTask<String, String, String> {
    Context context;
    AutoCompleteTextView autoTextView;

    public GooglePlace_autoComplite(Context context, AutoCompleteTextView autoTextView) {
        this.context = context;
        this.autoTextView = autoTextView;
    }

    @Override
    protected String doInBackground(String... params) {
        Ion.with(context).load(params[0]) //JSON data link
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        ArrayList<String> resultList = new ArrayList<>();

                        if(e != null){
                            Toast.makeText(context, "Low Internet Connection", Toast.LENGTH_SHORT).show();
                        }else {
                            try {
                                JSONObject parentObj = new JSONObject(result);
                                JSONArray predArray = parentObj.getJSONArray("predictions");
                                String status = parentObj.getString("status");

                                if(status.equals("OK")){
                                    for (int i = 0; i < predArray.length(); i++) {

                                        JSONObject object = predArray.getJSONObject(i);
                                        String description = object.getString("description");

                                        resultList.add(description);

                                    }

                                    SearchActivity.adapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, resultList);
                                    autoTextView.setAdapter(SearchActivity.adapter);
                                    SearchActivity.adapter.notifyDataSetChanged();


                                }else {Toast.makeText(context, "Invalide Result", Toast.LENGTH_SHORT).show();}



                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }


                    }
                });

        return null;
    }
}
