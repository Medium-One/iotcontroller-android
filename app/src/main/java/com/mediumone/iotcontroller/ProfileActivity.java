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
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mediumone.iotcontroller.mediumone.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;


public class ProfileActivity extends AppCompatActivity {

    private static RequestQueue queue;
    JSONArray profiles;
    JSONObject profile;
    int profileIndex;

    RecyclerView container;

    FloatingActionButton add;
    SharedPreferences sharedPref;
    LocationManager locationManager;
    ScheduledThreadPoolExecutor scheduler;
    MyLocationListener locationListener;
    private boolean isFabMenuOpen = false;
    FloatingActionButton settings;
    ViewGroup newWidgetMenu;
    Toolbar toolbar;

    private FastOutSlowInInterpolator fastOutSlowInInterpolator = new FastOutSlowInInterpolator();

    RecyclerView.Adapter adapter;
    LinearLayoutManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // location services
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 12345);

        profileIndex = getIntent().getIntExtra("profile_index", -1); // position in profile list
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        scheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(5);

        // set up for GPS
        locationListener = new MyLocationListener();
        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        newWidgetMenu = (ViewGroup) findViewById(R.id.addmenu);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // show name at top of profile
        try {
            profiles = new JSONArray(sharedPref.getString("profiles", "[]"));
            profile = profiles.getJSONObject(profileIndex);
            getSupportActionBar().setTitle(profile.optString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // set up widget container
        container = (RecyclerView) findViewById(R.id.widgets);
        manager = new LinearLayoutManager(this);
        container.setLayoutManager(manager);
        queue = Volley.newRequestQueue(this);
        adapter = new WidgetViewAdapter(profileIndex, profile, queue, scheduler, locationListener, ProfileActivity.this);
        container.setAdapter(adapter);
        container.setHasFixedSize(true);

        // display settings
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        final int heightScreen = dm.heightPixels;
        final int heightView = newWidgetMenu.getHeight();

        add = (FloatingActionButton) findViewById(R.id.add);

        // add menu items
        final Item[] items = {
                new Item("GPS", R.drawable.ic_gps_fixed_black_24dp),
                new Item("Switch", R.drawable.ic_power_settings_new_black_24dp),
                new Item("Slider", R.drawable.ic_ray_vertex_black_24dp),
                new Item("JSON", R.drawable.ic_code_braces_black_24dp),
                new Item("Gauge", R.drawable.ic_speedometer_black_24dp),
                new Item("Log", R.drawable.ic_format_align_left_black_24dp),
                new Item("Map", R.drawable.ic_location_on_black_24dp),
                new Item("Notification", R.drawable.ic_announcement_black_24dp)
        };

        // adapter for add widget button - used for phones that can't use expandable menu
        final ListAdapter dialogListAdapter = new ArrayAdapter<Item>(
                this,
                android.R.layout.select_dialog_item,
                android.R.id.text1,
                items) {
            @NonNull
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v.findViewById(android.R.id.text1);
                tv.setCompoundDrawablesWithIntrinsicBounds(items[position].icon, 0, 0, 0);
                int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 1f);
                tv.setCompoundDrawablePadding(dp5);
                return v;
            }
        };

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && heightScreen > heightView) {
                    if (isFabMenuOpen)
                        collapseFabMenu();
                    else
                        expandFabMenu();
                } else { // alternative to expandable menu
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    builder.setTitle("Add a Widget");
                    builder.setAdapter(dialogListAdapter, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            addWidget(which);
                        }
                    });
                    builder.create().show();
                }
            }
        });

        settings = (FloatingActionButton) findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSettings();
            }
        });

        // add isn't clickable if acct information is incomplete
        if (profile.optString("apikey").equals("") || profile.optString("username").equals("") || profile.optString("password").equals("")) {
            add.setClickable(false);
            add.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            showSettings();
        } else {
            try {
                login();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    }

    // Shows API settings when gear button is pressed
    private void showSettings() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Settings");
        View dialog = inflater.inflate(R.layout.dialog_settings, null);

        // get text boxes
        final EditText name = (EditText) dialog.findViewById(R.id.name);
        final EditText apikey = (EditText) dialog.findViewById(R.id.apikey);
        final EditText username = (EditText) dialog.findViewById(R.id.username);
        final EditText password = (EditText) dialog.findViewById(R.id.password);
        final CheckBox showPass = (CheckBox) dialog.findViewById(R.id.checkBox);
        final Spinner env = (Spinner) dialog.findViewById(R.id.env);

        // allow user to see password
        showPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked) {
                    password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });

        // show previous values
        name.setText(profile.optString("name"));
        apikey.setText(profile.optString("apikey"));
        username.setText(profile.optString("username"));
        password.setText(profile.optString("password"));
        env.setSelection(profile.optInt("env"));

        builder.setView(dialog);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    getSupportActionBar().setTitle(profile.optString("name"));

                    // save values
                    profile.put("name", name.getText().toString());
                    profile.put("apikey", apikey.getText().toString());
                    profile.put("username", username.getText().toString());
                    profile.put("password", password.getText().toString());
                    profile.put("env", env.getSelectedItemPosition());

                    saveProfile();
                    login();
                    adapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    // Logs into Medium One
    private void login() throws JSONException {
        final JSONObject credentials = new JSONObject()
                .put("login_id", profile.get("username"))
                .put("password", profile.get("password"))
                .put("api_key", profile.get("apikey"));

        int env = profile.getInt("env");
        String url = "";
        switch (env) { // determines base URL for logging in
            case 0:
                url = "https://api-sandbox.mediumone.com/v2/";
                break;
            case 1:
                url = "https://api.mediumone.com/v2/";
                break;
            case 2:
                url = "https://api-renesas-na-sandbox.mediumone.com/v2/";
        }
        final String finalUrl = url;
        StringRequest loginRequest = new StringRequest(Request.Method.POST, url + "login", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.equals("true")) {
                    try {
                        // saves info
                        profile.put("logged_in", true);
                        saveProfile();
                        adapter.notifyDataSetChanged();
                        add.setClickable(true);
                        add.setBackgroundTintList(ColorStateList.valueOf(ResourcesCompat.getColor(getResources(), R.color.ColorAccent, null)));

                        // sends notification token
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", java.util.Locale.getDefault());
                        df.setTimeZone(TimeZone.getTimeZone("UTC"));
                        String token = FirebaseInstanceId.getInstance().getToken();
                        final JSONObject payload = new JSONObject()
                                .put("event_data", new JSONObject()
                                    .put("FCM_token", token))
                                .put("observed_at", df.format(new Date()));
                        Log.d("payload", payload.toString(4));
                        StringRequest post = new StringRequest(Request.Method.POST, finalUrl + "events/raw/" + profile.getString("username"), new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("PostResponse", response);
                            }
                        }, new Response.ErrorListener() { // token error
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (error.networkResponse != null) {
                                    try {
                                        Log.e("PostError", new String(error.networkResponse.data, "UTF-8"));
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Toast.makeText(ProfileActivity.this,
                                        "Network connection error",
                                        Toast.LENGTH_SHORT).show();
                                }
                            }
                        }) {
                            @Override
                            public byte[] getBody() throws AuthFailureError {
                                return payload.toString().getBytes();
                            }
                            @Override
                            public String getBodyContentType() {
                                return "application/json";
                            }
                        };
                        queue.add(post);
                        } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() { // login error
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    try {
                        Toast.makeText(ProfileActivity.this, 
                            new JSONObject(new String(error.networkResponse.data, "UTF-8")).optJSONObject("error").optString("message"),
                            Toast.LENGTH_SHORT).show();
                        profile.put("logged_in", false);
                        saveProfile();
                        adapter.notifyDataSetChanged();
                        add.setClickable(false);
                        add.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(ProfileActivity.this,
                        "Network connection error",
                        Toast.LENGTH_SHORT).show();
                }
            }
        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return credentials.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        queue.add(loginRequest);
    }

    // Finishes application on home button press
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        Log.d("destroy",
                "destroy");
        scheduler.shutdown();
        super.onDestroy();
    }

    // add widget press
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        for (int index = 0; index < newWidgetMenu.getChildCount(); index++) {
            final View view = newWidgetMenu.getChildAt(index);
            Rect viewRect = new Rect();
            view.getGlobalVisibleRect(viewRect);
            if (!isFabMenuOpen || viewRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                return super.dispatchTouchEvent(ev);
            }
        }
        collapseFabMenu();
        return true;
    }

    // back button press
    @Override
    public void onBackPressed() {
        if (isFabMenuOpen) {
            collapseFabMenu();
        } else {
            super.onBackPressed();
        }
    }

    // Opens widget menu
    private void expandFabMenu() {
        for (int index = newWidgetMenu.getChildCount() - 1; index >= 0; index--) {
            // set on click listener for all widget options
            final View view = newWidgetMenu.getChildAt(index);
            final int finalIndex = index;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addWidget(finalIndex);
                    collapseFabMenu();
                }
            });

            // sets view for each option
            view.setScaleX(0f);
            view.setScaleY(0f);
            view.setAlpha(0f);
            view.setVisibility(View.VISIBLE);
            ViewCompat.animate(view)
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setInterpolator(fastOutSlowInInterpolator)
                    .setDuration(150)
                    .setStartDelay((long) ((newWidgetMenu
                            .getChildCount() - 1 - index) * 75 * 0.5f))

                    .setListener(new ViewPropertyAnimatorListener() {
                        @Override
                        public void onAnimationStart(View view) {
                        }

                        @Override
                        public void onAnimationEnd(View view) {

                        }

                        @Override
                        public void onAnimationCancel(View view) {

                        }
                    })
                    .start();
        }

        // animates add button
        ViewCompat.animate(add)
                .rotation(45.0F)
                .z(TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 12,
                        getResources().getDisplayMetrics()))
                .withLayer()
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator(0.0F))
                .start();
        final ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), 0x00FFFFFF, 0xF0FFFFFF);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            ColorDrawable colorDrawable = new ColorDrawable(0x00FFFFFF);

            @Override
            public void onAnimationUpdate(final ValueAnimator animator) {
                colorDrawable.setColor((Integer) animator.getAnimatedValue());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    container.setForeground(colorDrawable);
                }
            }
        });
        colorAnimation.setDuration(150);
        colorAnimation.start();
        settings.hide();
        isFabMenuOpen = true;

    }

    // Closes widget menu
    private void collapseFabMenu() {
        for (int index = 0; index < newWidgetMenu.getChildCount(); index++) {
            // sets view for each option
            final View view = newWidgetMenu.getChildAt(index);
            view.setScaleX(1f);
            view.setScaleY(1f);
            view.setAlpha(1f);
            view.setVisibility(View.VISIBLE);
            ViewCompat.animate(view)
                    .alpha(0f)
                    .scaleX(0f)
                    .scaleY(0f)
                    .setInterpolator(fastOutSlowInInterpolator)
                    .setDuration(150)
                    .setStartDelay((long) (index * 75 * 0.5f))
                    .setListener(new ViewPropertyAnimatorListener() {
                        @Override
                        public void onAnimationStart(View view) {
                        }

                        @Override
                        public void onAnimationEnd(View view) {
                            view.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationCancel(View view) {

                        }
                    })
                    .start();
        }

        // animates add button
        ViewCompat.animate(add)
                .rotation(0.0F)
                .z(TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 12,
                        getResources().getDisplayMetrics()))
                .withLayer()
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator(0.0F))
                .start();
        final ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), 0xF0FFFFFF, 0x00FFFFFF);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            ColorDrawable colorDrawable = new ColorDrawable(0xF0FFFFFF);
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onAnimationUpdate(final ValueAnimator animator) {
                colorDrawable.setColor((Integer) animator.getAnimatedValue());
                container.setForeground(colorDrawable);
            }
        });
        colorAnimation.setDuration(150);
        colorAnimation.start();

        settings.show();

        isFabMenuOpen = false;

    }

    // adds widget
    public void addWidget(int which) {
        try {
            // limit widget amount to 10
            if (profile.getJSONArray("widgets").length() >= 10) {
                Toast.makeText(ProfileActivity.this, "Too many widgets!", Toast.LENGTH_SHORT).show();
            } else {
                JSONObject widget = new JSONObject().put("type", which) // add widget
                        .put("stream", "")
                        .put("tag", "");
                if (which == 4) { // special settings for gauge
                    widget.put("min", 0);
                    widget.put("max", 100);
                    widget.put("auto", false);
                }

                // add widget to array and go to new widget
                profile.getJSONArray("widgets").put(widget);
                adapter.notifyItemInserted(adapter.getItemCount());
                manager.scrollToPosition(adapter.getItemCount()-1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        saveProfile();
    }

    // Saves all profile data
    private void saveProfile() {
        try {
            SharedPreferences.Editor editor = sharedPref.edit();
            profiles.put(profileIndex, profile);
            editor.putString("profiles", profiles.toString());
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static RequestQueue getRequestQueue() {
        return queue;
    }

    class Item {
        public final String text;
        public final int icon;
        Item(String text, Integer icon) {
            this.text = text;
            this.icon = icon;
        }
        @Override
        public String toString() {
            return text;
        }
    }
}

