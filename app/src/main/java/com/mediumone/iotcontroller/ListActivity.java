//  This file is part of Medium One IoT App for Android
//  Copyright (c) 2016 Medium One.
//
//  The Medium One IoT App for Android is free software: you can redistribute
//  it and/or modify it under the terms of the GNU General Public License as
//  published by the Free Software Foundation, either version 3 of the
//  License, or (at your option) any later version.
//
//  The Medium One IoT App for Android is distributed in the hope that it will
//  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
//  Public License for more details.
//
//  You should have received a copy of the GNU General Public License along
//  with Medium One IoT App for Android. If not, see
//  <http://www.gnu.org/licenses/>.

package com.mediumone.iotcontroller;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mediumone.iotcontroller.mediumone.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {
    private ArrayList<String> profiles;
    private ArrayAdapter<String> profilesAdapter;
    private ListView profilesListView;
    private SharedPreferences sharedPref;
    private FloatingActionButton newProfile;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        // location permissions
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 12345);

        // top bar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // profile list
        profilesListView = (ListView) findViewById(R.id.profiles);
        setupListViewListener();

        // add profile button
        newProfile = (FloatingActionButton) findViewById(R.id.add);
        newProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("name", "");
                    obj.put("env", 0);
                    obj.put("api_key", "");
                    obj.put("username", "");
                    obj.put("password", "");
                    obj.put("logged_in", false);
                    obj.put("widgets", new JSONArray());

                    JSONArray JSONProfiles = new JSONArray(sharedPref.getString("profiles", "[]"));
                    JSONProfiles.put(obj); // add to profile list
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("profiles", JSONProfiles.toString());
                    editor.apply();

                    // open profile
                    Intent intent = new Intent(ListActivity.this, ProfileActivity.class);
                    intent.putExtra("profile_index", JSONProfiles.length()-1);
                    startActivity(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onResume() {
        profiles = new ArrayList<>();
        profilesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, profiles);
        profilesListView.setAdapter(profilesAdapter);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        try { // add all profiles to list
            JSONArray JSONProfiles = new JSONArray(sharedPref.getString("profiles", "[]"));
            for(int i = 0; i < JSONProfiles.length(); i++){
                profiles.add(JSONProfiles.getJSONObject(i).getString("name"));
            }
            profilesAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    private void setupListViewListener() {
        // short click = open profile
        profilesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos,
                                    long id) {
                Intent intent = new Intent(ListActivity.this, ProfileActivity.class);
                intent.putExtra("profile_index", pos);
                startActivity(intent);
            }
        });

        // long click = popup to delete profile
        profilesListView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapter,
                                                   View item, final int pos, long id) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
                        builder.setCancelable(true);
                        builder.setTitle("Delete Profile");
                        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    JSONArray JSONProfiles = new JSONArray(sharedPref.getString("profiles", "[]"));
                                    JSONProfiles.remove(pos);
                                    editor.putString("profiles", JSONProfiles.toString());
                                    editor.apply();
                                    profiles.remove(pos);
                                    profilesAdapter.notifyDataSetChanged();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });

                        builder.setNegativeButton("Cancel", null);
                        builder.create().show();

                        return true;
                    }

                });
    }

}