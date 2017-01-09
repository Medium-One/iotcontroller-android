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

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.mediumone.iotcontroller.mediumone.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class WidgetViewAdapter extends RecyclerView.Adapter<WidgetViewHolder> {

    SharedPreferences sharedPref;
    JSONArray profiles;
    int profileIndex;
    JSONObject profile;

    ScheduledThreadPoolExecutor scheduler;
    MyLocationListener locationListener;
    Context context;
    RequestQueue queue;

    // Determines which widget shows up in profiles
    public WidgetViewAdapter(int profileName, JSONObject profile, RequestQueue queue, ScheduledThreadPoolExecutor scheduler, MyLocationListener locationListener, Context context) {
        this.profileIndex = profileName;
        this.profile = profile;
        this.queue = queue;
        this.scheduler = scheduler;
        this.locationListener = locationListener;
        this.context = context;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            profiles = new JSONArray(sharedPref.getString("profiles", "[]"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Creates new widget depending on view type
    @Override
    public WidgetViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {
        final WidgetViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(context);
        switch (viewType) {
            case 0:
                ViewPager GPSView = (ViewPager) inflater.inflate(R.layout.view_location, parent, false);
                viewHolder = new GPSViewHolder(GPSView);
                break;
            case 1:
                ViewPager switchView = (ViewPager) inflater.inflate(R.layout.view_switch, parent, false);
                viewHolder = new SwitchViewHolder(switchView);
                break;
            case 2:
                ViewPager sliderView = (ViewPager) inflater.inflate(R.layout.view_slider, parent, false);
                viewHolder = new SliderViewHolder(sliderView);
                break;
            case 3:
                ViewPager JSONView = (ViewPager) inflater.inflate(R.layout.view_json, parent, false);
                viewHolder = new JSONViewHolder(JSONView);
                break;
            case 4:
                ViewPager gaugeView = (ViewPager) inflater.inflate(R.layout.view_gauge, parent, false);
                viewHolder = new GaugeViewHolder(gaugeView);
                break;
            case 5:
                ViewPager logView = (ViewPager) inflater.inflate(R.layout.view_log, parent, false);
                viewHolder = new LogViewHolder(logView);
                break;
            case 6:
                ViewPager mapWidgetView = (ViewPager) inflater.inflate(R.layout.view_map, parent, false);
                viewHolder = new MapViewHolder(mapWidgetView);

                final MapView mapview = ((MapViewHolder) viewHolder).getMap();
                MapsInitializer.initialize(context);
                mapview.onCreate(new Bundle());
                mapview.onResume();
                mapview.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(final GoogleMap googleMap) {
                        googleMap.getUiSettings().setAllGesturesEnabled(false);
                        googleMap.getUiSettings().setZoomControlsEnabled(true);
                        ((MapViewHolder) viewHolder).setGoogleMap(googleMap);
                    }
                });
                break;
            case 7:
                ViewPager notificationView = (ViewPager) inflater.inflate(R.layout.view_notification, parent, false);
                viewHolder = new NotificationViewHolder(notificationView);
                break;
            default:
                View v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
                viewHolder = new LogViewHolder(v);
                break;
        }

        setupPager(viewHolder);
        return viewHolder;
    }

    private void setupPager(final WidgetViewHolder holder) {
        WidgetPagerAdapter adapter = new WidgetPagerAdapter();
        ViewPager viewPager = holder.getPager();
        viewPager.setAdapter(adapter);
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        viewPager.getLayoutParams().width = displayMetrics.widthPixels;
        viewPager.requestLayout();
    }

    // Sets up buttons that appear when you swipe left
    private void setupButtons(final WidgetViewHolder holder) {

        holder.getEditStream().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //stream
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                final EditText stream = new EditText(context);
                builder.setTitle("Set Stream");
                try {
                    final JSONObject widget = profile.getJSONArray("widgets").getJSONObject(holder.getAdapterPosition());
                    stream.setText(widget.optString("stream"));
                    builder.setView(stream);
                    builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                saveWidget(profile, profileIndex, holder.getItemViewType(), holder.getAdapterPosition(), stream.getText().toString(), widget.getString("tag"), sharedPref);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    builder.setNegativeButton("Cancel", null);
                    builder.create().show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        if (holder.getItemViewType() != 0) { //tag - for all widgets except GPS
            try {
                final JSONObject widget = profile.getJSONArray("widgets").getJSONObject(holder.getAdapterPosition());

                holder.getTagName().setText(widget.optString("tag"));
                holder.getEditTag().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        final EditText tag = new EditText(context);
                        InputFilter filter = new InputFilter() {
                            @Override
                            public CharSequence filter(CharSequence source, int start, int end,
                                                       Spanned dest, int dstart, int dend) {
                                for (int i = start; i < end; i++) {
                                    if (!(Character.isLetterOrDigit(source.charAt(i)) || Character.toString(source.charAt(i)).equals("_") || Character.toString(source.charAt(i)).equals("-"))) {
                                        return "";
                                    }
                                }
                                return null;
                            }
                        };
                        tag.setFilters(new InputFilter[]{filter});
                        builder.setTitle("Set Tag");
                        tag.setText(widget.optString("tag"));
                        builder.setView(tag);
                        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    saveWidget(profile, profileIndex, holder.getItemViewType(), holder.getAdapterPosition(), widget.getString("stream"), tag.getText().toString(), sharedPref);
                                    holder.getTagName().setText(tag.getText().toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        builder.setNegativeButton("Cancel", null);
                        builder.create().show();

                    }

                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        holder.getRemove().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //remove
                try {
                    if (holder.getItemView().getTag() != null) {
                        ((ScheduledFuture) holder.getItemView().getTag()).cancel(true);
                    }
                    holder.resetView();
                    profile.getJSONArray("widgets").remove(holder.getAdapterPosition());
                    saveProfile(profile, profileIndex, sharedPref);
                    notifyItemRemoved(holder.getAdapterPosition());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    // widget actions
    @Override
    public void onBindViewHolder(final WidgetViewHolder viewHolder, final int position) {
        try {
            final JSONObject widget = profile.getJSONArray("widgets").getJSONObject(viewHolder.getAdapterPosition());
            switch (viewHolder.getItemViewType()) {
                case 0: // GPS widget : sends every 5 seconds
                    GPSViewHolder gpsViewHolder = (GPSViewHolder) viewHolder;
                    View GPSView = gpsViewHolder.getItemView();
                    gpsViewHolder.getPager().setCurrentItem(0);

                    Switch location = (Switch) GPSView.findViewById(R.id.location);
                    if (gpsViewHolder.getItemView().getTag() == null || ((ScheduledFuture) gpsViewHolder.getItemView().getTag()).isCancelled()) {
                        final ScheduledFuture<?>[] locationFuture = new ScheduledFuture<?>[1];
                        location.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                try {
                                    JSONObject widget = profile.getJSONArray("widgets").getJSONObject(viewHolder.getAdapterPosition());
                                    if (b) {
                                        String stream = widget.getString("stream");
                                        if (stream.equals("")) {
                                            Toast.makeText(context, "Stream not set!", Toast.LENGTH_SHORT).show();
                                            compoundButton.setChecked(false);
                                        } else {
                                            locationFuture[0] = scheduler.scheduleWithFixedDelay(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        if (locationListener.getJSON() != null) {
                                                            JSONObject widget = profile.getJSONArray("widgets").getJSONObject(viewHolder.getAdapterPosition());
                                                            post(locationListener.getJSON(), widget.getString("stream"));
                                                        }
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }, 0, 5, TimeUnit.SECONDS);
                                        }

                                    } else {
                                        if (locationFuture[0] != null) {
                                            locationFuture[0].cancel(true);
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        gpsViewHolder.getItemView().setTag(locationFuture[0]);
                    }
                    setupButtons(gpsViewHolder);

                    break;
                case 1: // switch: send when changed
                    SwitchViewHolder switchViewHolder = (SwitchViewHolder) viewHolder;
                    switchViewHolder.getPager().setCurrentItem(0);
                    setupButtons(switchViewHolder);

                    switchViewHolder.getOnoffSwitch().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            try {
                                JSONObject widget = profile.getJSONArray("widgets").getJSONObject(viewHolder.getAdapterPosition());
                                String stream = widget.getString("stream");
                                String tag = widget.getString("tag");
                                if (stream.equals("") || tag.equals("")) {
                                    Toast.makeText(context, "Stream or tag not set!", Toast.LENGTH_SHORT).show();
                                    compoundButton.setChecked(false);
                                } else {
                                    saveProfile(profile, profileIndex, sharedPref);
                                    if (b) {
                                        post(new JSONObject().put(tag, "on"), stream);
                                    } else {
                                        post(new JSONObject().put(tag, "off"), stream);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
                case 2: // slider: send value when completely updated
                    final SliderViewHolder sliderViewHolder = (SliderViewHolder) viewHolder;
                    sliderViewHolder.getPager().setCurrentItem(0);
                    setupButtons(sliderViewHolder);

                    final int[] prevVal = {0};
                    sliderViewHolder.getSlider().setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) { // slider moving
                            sliderViewHolder.getSliderValue().setText(String.valueOf(i));
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) { // slider stops moving
                            try {
                                JSONObject widget = profile.getJSONArray("widgets").getJSONObject(viewHolder.getAdapterPosition());
                                String stream = widget.getString("stream");
                                String tag = widget.getString("tag");
                                if (stream.equals("") || tag.equals("")) {
                                    Toast.makeText(context, "Stream or tag not set!", Toast.LENGTH_SHORT).show();
                                    seekBar.setProgress(prevVal[0]);
                                } else {
                                    JSONObject eventData = new JSONObject()
                                            .put(tag, seekBar.getProgress());
                                    post(eventData, stream);
                                    prevVal[0] = seekBar.getProgress();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
                case 3: // JSON sender: sends when user wants
                    JSONViewHolder jsonViewHolder = (JSONViewHolder) viewHolder;
                    jsonViewHolder.getPager().setCurrentItem(0);
                    setupButtons(jsonViewHolder);

                    final EditText value = jsonViewHolder.getValue();
                    final ToggleButton bool = jsonViewHolder.getBool();
                    final Spinner dataType = jsonViewHolder.getType();
                    dataType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            switch (i) {
                                case 0: // string
                                    value.setVisibility(View.VISIBLE);
                                    bool.setVisibility(View.GONE);
                                    value.setText("");
                                    value.setInputType(InputType.TYPE_CLASS_TEXT);
                                    break;
                                case 1: // number
                                    value.setVisibility(View.VISIBLE);
                                    bool.setVisibility(View.GONE);
                                    value.setText("");
                                    value.setInputType(InputType.TYPE_CLASS_NUMBER);
                                    break;
                                case 2: // boolean
                                    value.setVisibility(View.GONE);
                                    bool.setVisibility(View.VISIBLE);
                                    break;
                            }
                            saveProfile(profile, profileIndex, sharedPref);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }


                    });

                    final Button send = jsonViewHolder.getSend();
                    send.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                JSONObject widget = profile.getJSONArray("widgets").getJSONObject(viewHolder.getAdapterPosition());
                                String stream = widget.getString("stream");
                                String tag = widget.getString("tag");
                                if (stream.equals("") || tag.equals("")) {
                                    Toast.makeText(context, "Stream or tag not set!", Toast.LENGTH_SHORT).show();
                                } else {
                                    JSONObject eventData = new JSONObject();
                                    int selectedType = dataType.getSelectedItemPosition();
                                    String val_str = value.getText().toString();
                                    switch (selectedType) {
                                        case 0:
                                            eventData.put(tag, val_str);
                                            post(eventData, stream);
                                            break;
                                        case 1:
                                            try {
                                                double num = Double.parseDouble(val_str);
                                                eventData.put(tag, num);
                                                post(eventData, stream);
                                            } catch (NumberFormatException e) {
                                                Toast.makeText(context, "Invalid number", Toast.LENGTH_SHORT).show();
                                            }
                                            break;
                                        case 2:
                                            eventData.put(tag, bool.isChecked());
                                            post(eventData, stream);
                                            break;
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    value.setOnKeyListener(new View.OnKeyListener() {
                        public boolean onKey(View v, int keyCode, KeyEvent event) {
                            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                                send.performClick();
                                return true;
                            }
                            return false;
                        }
                    });
                    break;
                case 4: // gauge: updates every 3 seconds
                    final GaugeViewHolder gaugeViewHolder = (GaugeViewHolder) viewHolder;
                    gaugeViewHolder.getPager().setCurrentItem(0);
                    final DecoView gauge = gaugeViewHolder.getGauge();
                    final TextView gaugeValue = gaugeViewHolder.getGaugeValue();
                    final int[] seriesIndex = new int[1];
                    gauge.addSeries(new SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                            .setRange(0, 100, 100)
                            .setInitialVisibility(true)
                            .setLineWidth(32f)
                            .build());
                    gauge.configureAngles(270, 0);


                    try {
                        final SeriesItem.Builder seriesBuilder = new SeriesItem.Builder(Color.argb(255, 3, 169, 244))
                                .setRange(widget.getInt("min"), widget.getInt("max"), widget.getInt("min"))
                                .setLineWidth(32f);

                        gaugeValue.setText(String.valueOf(widget.getInt("min")));

                        seriesIndex[0] = gauge.addSeries(seriesBuilder.build());
                        Log.d("series index", String.valueOf(seriesIndex[0]));
                        gauge.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
                                .setDelay(0)
                                .setDuration(0)
                                .build());
                        if (gaugeViewHolder.getItemView().getTag() == null || ((ScheduledFuture)gaugeViewHolder.getItemView().getTag()).isCancelled()) {
                            final ScheduledFuture<?> gaugeFuture = scheduler.scheduleWithFixedDelay(new Runnable() {
                                @Override
                                public void run() {
                                    if (profile.optBoolean("logged_in")) {
                                        try {
                                            final String stream = widget.getString("stream");
                                            final String tag = widget.getString("tag");

                                            int env = profile.getInt("env");
                                            String url = "";
                                            switch (env) {
                                                case 0:
                                                    url = "https://api-sandbox.mediumone.com/v2/";
                                                    break;
                                                case 1:
                                                    url = "https://api.mediumone.com/v2/";
                                                    break;
                                                case 2:
                                                    url = "https://api-renesas-na-sandbox.mediumone.com/v2/";
                                            }
                                            final JSONObject payload = new JSONObject();
                                            StringRequest get = new StringRequest(Request.Method.GET, url +
                                                    "events/" + stream + "/" + profile.getString("username") +
                                                    "?include=$.event_data." + tag + "&sort_by=observed_at&sort_direction=desc&limit=1",
                                                    new Response.Listener<String>() {
                                                        @Override
                                                        public void onResponse(String response) {
                                                            try {
                                                                Log.d("response", response);
                                                                JSONObject r = new JSONObject(response);
                                                                int min = widget.getInt("min");
                                                                int max = widget.getInt("max");
                                                                Boolean auto = widget.getBoolean("auto");
                                                                int val = r.getJSONObject("event_data").getInt(tag);
                                                                if (val >= min && val <= max) {
                                                                    gauge.addEvent(new DecoEvent.Builder(val).setIndex(seriesIndex[0]).setDuration(2).build());
                                                                    gaugeValue.setText(String.valueOf(val));
                                                                } else if (val > max && auto) {
                                                                    max = val + (int) Math.round(val * .1);
                                                                    widget.put("max", Integer.valueOf(max));
                                                                    profile.getJSONArray("widgets").put(viewHolder.getAdapterPosition(), widget);
                                                                    saveProfile(profile, profileIndex, sharedPref);
                                                                    gauge.deleteAll();
                                                                    gauge.addSeries(new SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                                                                            .setRange(0, 100, 100)
                                                                            .setInitialVisibility(true)
                                                                            .setLineWidth(32f)
                                                                            .build());
                                                                    seriesBuilder.setRange(widget.getInt("min"), max, val);
                                                                    seriesIndex[0] = gauge.addSeries(seriesBuilder.build());
                                                                    Log.d("series index", String.valueOf(seriesIndex[0]));
                                                                    gaugeValue.setText(String.valueOf(val));
                                                                } else if (val < min && auto) {
                                                                    min = val - (int) Math.round(val * .1);
                                                                    widget.put("min", Integer.valueOf(min));
                                                                    profile.getJSONArray("widgets").put(viewHolder.getAdapterPosition(), widget);
                                                                    saveProfile(profile, profileIndex, sharedPref);

                                                                    gauge.deleteAll();
                                                                    gauge.addSeries(new SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                                                                            .setRange(0, 100, 100)
                                                                            .setInitialVisibility(true)
                                                                            .setLineWidth(32f)
                                                                            .build());
                                                                    seriesBuilder.setRange(min, widget.getInt("max"), val);
                                                                    seriesIndex[0] = gauge.addSeries(seriesBuilder.build());
                                                                    Log.d("series index", String.valueOf(seriesIndex[0]));
                                                                    gaugeValue.setText(String.valueOf(val));
                                                                }
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    if (error.networkResponse != null) {
                                                        gaugeViewHolder.resetView();
                                                        try {
                                                            Log.e("GetError", new String(error.networkResponse.data, "UTF-8"));
                                                        } catch (UnsupportedEncodingException e) {
                                                            e.printStackTrace();
                                                        }
                                                    } else {
                                                        Toast.makeText(context,
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
                                            queue.add(get);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }, 0, 3, TimeUnit.SECONDS);
                            gaugeViewHolder.getItemView().setTag(gaugeFuture);
                        }
                        setupButtons(gaugeViewHolder);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    gaugeViewHolder.getEditRange().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Edit range");
                            LayoutInflater inflater = LayoutInflater.from(context);
                            View rangeDialog = inflater.inflate(R.layout.dialog_range, null);
                            final EditText min = (EditText) rangeDialog.findViewById(R.id.min);
                            final EditText max = (EditText) rangeDialog.findViewById(R.id.max);
                            final Switch auto = (Switch) rangeDialog.findViewById(R.id.auto);

                            try {
                                JSONObject widget = profile.getJSONArray("widgets").getJSONObject(viewHolder.getAdapterPosition());
                                min.setText(String.valueOf(widget.getInt("min")));
                                max.setText(String.valueOf(widget.getInt("max")));
                                auto.setChecked(widget.getBoolean("auto"));
                                if (auto.isChecked()) { // disable editing password
                                    min.setFocusable(false);
                                    min.setFocusableInTouchMode(false); // user touches widget on phone with touch screen
                                    min.setClickable(false); // user navigates with wheel and selects widget
                                    max.setFocusable(false);
                                    max.setFocusableInTouchMode(false); // user touches widget on phone with touch screen
                                    max.setClickable(false); // user navigates with wheel and selects widget
                                }
                                auto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                        if (b) { // disable editing password
                                            min.setFocusable(false);
                                            min.setFocusableInTouchMode(false); // user touches widget on phone with touch screen
                                            min.setClickable(false); // user navigates with wheel and selects widget
                                            max.setFocusable(false);
                                            max.setFocusableInTouchMode(false); // user touches widget on phone with touch screen
                                            max.setClickable(false); // user navigates with wheel and selects widget
                                        } else {
                                            min.setFocusable(true);
                                            min.setFocusableInTouchMode(true); // user touches widget on phone with touch screen
                                            min.setClickable(true); // user navigates with wheel and selects widget
                                            max.setFocusable(true);
                                            max.setFocusableInTouchMode(true); // user touches widget on phone with touch screen
                                            max.setClickable(true); // user navigates with wheel and selects widget
                                        }
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            builder.setView(rangeDialog);
                            builder.setPositiveButton("Save", null);
                            builder.setNegativeButton("Cancel", null);
                            final AlertDialog dialog = builder.create();
                            dialog.show();
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    try {
                                        if (Integer.valueOf(min.getText().toString()) < Integer.valueOf(max.getText().toString()))
                                        {
                                            JSONObject widget = profile.getJSONArray("widgets").getJSONObject(viewHolder.getAdapterPosition());
                                            widget.put("min", Integer.valueOf(min.getText().toString()));
                                            widget.put("max", Integer.valueOf(max.getText().toString()));
                                            widget.put("auto", auto.isChecked());
                                            profile.getJSONArray("widgets").put(viewHolder.getAdapterPosition(), widget);
                                            saveProfile(profile, profileIndex, sharedPref);
                                            gauge.deleteAll();
                                            gauge.addSeries(new SeriesItem.Builder(Color.argb(255, 218, 218, 218))
                                                    .setRange(0, 100, 100)
                                                    .setInitialVisibility(true)
                                                    .setLineWidth(32f)
                                                    .build());
                                            final SeriesItem.Builder seriesBuilder = new SeriesItem.Builder(Color.argb(255, 3, 169, 244))
                                                    .setRange(widget.getInt("min"), widget.getInt("max"), widget.getInt("min"))
                                                    .setLineWidth(32f);
                                            seriesBuilder.setRange(widget.getInt("min"), widget.getInt("max"), widget.getInt("min"));
                                            gauge.addSeries(seriesBuilder.build());
                                            dialog.dismiss();
                                        } else {
                                            Toast.makeText(context, "Invalid range", Toast.LENGTH_SHORT).show();
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        }
                    });
                    break;
                case 5: // log: updates every 3 seconds
                    final LogViewHolder logViewHolder = (LogViewHolder) viewHolder;
                    logViewHolder.getPager().setCurrentItem(0);

                    final TextView log = logViewHolder.getLog();
                    log.setMovementMethod(new ScrollingMovementMethod());
                    log.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            log.getParent().requestDisallowInterceptTouchEvent(true);
                            return false;
                        }
                    });
                    if (logViewHolder.getItemView().getTag() == null || ((ScheduledFuture)logViewHolder.getItemView().getTag()).isCancelled()) {
                        final ScheduledFuture<?> logFuture = scheduler.scheduleWithFixedDelay(new Runnable() {
                            @Override
                            public void run() {
                                if (profile.optBoolean("logged_in") && viewHolder.getAdapterPosition() != -1) {
                                    try {
                                        final JSONObject widget = profile.getJSONArray("widgets").getJSONObject(viewHolder.getAdapterPosition());
                                        final String stream = widget.getString("stream");
                                        final String tag = widget.getString("tag");

                                        int env = profile.getInt("env");
                                        String url = "";
                                        switch (env) {
                                            case 0:
                                                url = "https://api-sandbox.mediumone.com/v2/";
                                                break;
                                            case 1:
                                                url = "https://api.mediumone.com/v2/";
                                                break;
                                            case 2:
                                                url = "https://api-renesas-na-sandbox.mediumone.com/v2/";
                                        }

                                        final JSONObject payload = new JSONObject();
                                        StringRequest get = null;
                                        try {
                                            get = new StringRequest(Request.Method.GET, url +
                                                    "events/" + stream + "/" + profile.getString("username") +
                                                    "?include=$.event_data." + tag + "&sort_by=observed_at&sort_direction=desc&limit=10",
                                                    new Response.Listener<String>() {
                                                        @Override
                                                        public void onResponse(String response) {
                                                            String[] events = response.split("\n");
                                                            String logs = "";
                                                            try {
                                                                for (String event : events) {
                                                                    logs += new JSONObject(event).toString(2) + "\n";
                                                                }
                                                                log.setText(logs);
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }

                                                    }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    if (error.networkResponse != null) {
                                                        logViewHolder.resetView();
                                                        try {
                                                            Log.e("GetError", new String(error.networkResponse.data, "UTF-8"));
                                                        } catch (UnsupportedEncodingException e) {
                                                            e.printStackTrace();
                                                        }
                                                    } else {
                                                        Toast.makeText(context,
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
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        queue.add(get);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }, 0, 3, TimeUnit.SECONDS);
                        logViewHolder.getItemView().setTag(logFuture);
                    }
                    setupButtons(logViewHolder);

                    break;
                case 6: // map: updates every 3 seconds
                    final MapViewHolder mapViewHolder = (MapViewHolder) viewHolder;
                    mapViewHolder.getPager().setCurrentItem(0);

                    final ScheduledFuture<?>[] mapFuture = new ScheduledFuture<?>[1];
                    if (mapViewHolder.getItemView().getTag() == null || ((ScheduledFuture)mapViewHolder.getItemView().getTag()).isCancelled()) {
                        mapFuture[0] = scheduler.scheduleWithFixedDelay(new Runnable() {
                            @Override
                            public void run() {
                                if (profile.optBoolean("logged_in") && viewHolder.getAdapterPosition() != -1) {
                                    try {
                                        JSONObject widget = profile.getJSONArray("widgets").getJSONObject(viewHolder.getAdapterPosition());
                                        final String stream = widget.getString("stream");
                                        final String tag = widget.getString("tag");

                                        int env = profile.getInt("env");
                                        String url = "";
                                        switch (env) {
                                            case 0:
                                                url = "https://api-sandbox.mediumone.com/v2/";
                                                break;
                                            case 1:
                                                url = "https://api.mediumone.com/v2/";
                                                break;
                                            case 2:
                                                url = "https://api-renesas-na-sandbox.mediumone.com/v2/";
                                        }

                                        final JSONObject payload = new JSONObject();
                                        StringRequest get = null;
                                        try {
                                            get = new StringRequest(Request.Method.GET, url +
                                                    "events/" + stream + "/" + profile.getString("username") +
                                                    "?include=$.event_data." + tag + "&sort_by=observed_at&sort_direction=desc&limit=1",
                                                    new Response.Listener<String>() {
                                                        @Override
                                                        public void onResponse(String response) {
                                                            Log.d("response", response);
                                                            if (mapViewHolder.getGoogleMap() != null) {
                                                                try {
                                                                    JSONObject r = new JSONObject(response);
                                                                    String coords = r.getJSONObject("event_data").getString(tag);
                                                                    LatLng pos = new LatLng(Double.valueOf(coords.split(" ")[0]), Double.valueOf(coords.split(" ")[1]));
                                                                    mapViewHolder.getGoogleMap().clear();
                                                                    mapViewHolder.getGoogleMap().addMarker(new MarkerOptions().position(pos));
                                                                    mapViewHolder.getGoogleMap().animateCamera(CameraUpdateFactory.newLatLng(pos));

                                                                } catch (JSONException | java.lang.NumberFormatException | ArrayIndexOutOfBoundsException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        }
                                                    }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    if (error.networkResponse != null) {
                                                        mapViewHolder.resetView();
                                                        try {
                                                            Log.e("GetError", new String(error.networkResponse.data, "UTF-8"));
                                                        } catch (UnsupportedEncodingException e) {
                                                            e.printStackTrace();
                                                        }
                                                    } else {
                                                        Toast.makeText(context,
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
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        queue.add(get);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }, 0, 3, TimeUnit.SECONDS);
                        mapViewHolder.getItemView().setTag(mapFuture[0]);
                    }

                    setupButtons(mapViewHolder);

                    break;
                case 7: // notification: updates ever 3 seconds
                    final NotificationViewHolder notificationViewHolder = (NotificationViewHolder) viewHolder;
                    notificationViewHolder.getPager().setCurrentItem(0);

                    final TextView notification = notificationViewHolder.getNotification();
                    final TextView timestamp = notificationViewHolder.getTimestamp();
                    if (notificationViewHolder.getItemView().getTag() == null || ((ScheduledFuture) notificationViewHolder.getItemView().getTag()).isCancelled()) {
                        final ScheduledFuture<?> notificationFuture = scheduler.scheduleWithFixedDelay(new Runnable() {
                            @Override
                            public void run() {
                                if (profile.optBoolean("logged_in") && viewHolder.getAdapterPosition() != -1) {
                                    try {
                                        final JSONObject widget = profile.getJSONArray("widgets").getJSONObject(viewHolder.getAdapterPosition());
                                        final String stream = widget.getString("stream");
                                        final String tag = widget.getString("tag");

                                        int env = profile.getInt("env");
                                        String url = "";
                                        switch (env) {
                                            case 0:
                                                url = "https://api-sandbox.mediumone.com/v2/";
                                                break;
                                            case 1:
                                                url = "https://api.mediumone.com/v2/";
                                                break;
                                            case 2:
                                                url = "https://api-renesas-na-sandbox.mediumone.com/v2/";
                                        }

                                        final JSONObject payload = new JSONObject();
                                        StringRequest get = null;
                                        try {
                                            get = new StringRequest(Request.Method.GET, url +
                                                    "events/" + stream + "/" + profile.getString("username") +
                                                    "?include=$.event_data." + tag + "&sort_by=observed_at&sort_direction=desc&limit=1",
                                                    new Response.Listener<String>() {
                                                        @Override
                                                        public void onResponse(String response) {
                                                            if (notification != null) {
                                                                try {
                                                                    JSONObject r = new JSONObject(response);
                                                                    notification.setText(r.getJSONObject("event_data").getString(tag));
                                                                    widget.put("notification", notification.getText());
                                                                    timestamp.setText(r.getString("observed_at"));
                                                                    widget.put("timestamp", timestamp.getText());
                                                                } catch (JSONException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        }

                                                    }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    if (error.networkResponse != null) {
                                                        notificationViewHolder.resetView();
                                                        try {
                                                            Log.e("GetError", new String(error.networkResponse.data, "UTF-8"));
                                                        } catch (UnsupportedEncodingException e) {
                                                            e.printStackTrace();
                                                        }
                                                    } else {
                                                        Toast.makeText(context,
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
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        queue.add(get);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }, 0, 3, TimeUnit.SECONDS);
                        notificationViewHolder.getItemView().setTag(notificationFuture);
                    }
                    setupButtons(notificationViewHolder);
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Gets number of items
    @Override
    public int getItemCount(){
        try {
            return profile.getJSONArray("widgets").length();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Gets item type (GPS, widget, etc)
    @Override
    public int getItemViewType(int position) {
        try {
            return profile.getJSONArray("widgets").getJSONObject(position).getInt("type");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Saves widget settings
    private static void saveWidget(JSONObject profile, int profileIndex, int type, int index, String stream, String tag, SharedPreferences sharedPref) {
        try {
            profile.getJSONArray("widgets").getJSONObject(index).put("type", type)
                    .put("stream", stream)
                    .put("tag", tag);
            saveProfile(profile, profileIndex, sharedPref);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Saves entire profile
    private static void saveProfile(JSONObject profile, int profileIndex, SharedPreferences sharedPref) {
        try {
            Log.d("save profile", profile.toString(4));
            SharedPreferences.Editor editor = sharedPref.edit();
            JSONArray profiles = new JSONArray(sharedPref.getString("profiles", "[]"));
            profiles.put(profileIndex, profile);
            editor.putString("profiles", profiles.toString());
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Sends JSON
    private void post(final JSONObject event_data, final String stream) throws JSONException {
        if(profile.optBoolean("logged_in")) {
            int env = profile.getInt("env");
            String url = "";
            switch (env) {
                case 0:
                    url = "https://api-sandbox.mediumone.com/v2/";
                    break;
                case 1:
                    url = "https://api.mediumone.com/v2/";
                    break;
                case 2:
                    url = "https://api-renesas-na-sandbox.mediumone.com/v2/";
            }
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", java.util.Locale.getDefault());
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            final JSONObject payload = new JSONObject()
                    .put("event_data", event_data)
                    .put("observed_at", df.format(new Date()));
            Log.d("payload", payload.toString(4));
            StringRequest post = new StringRequest(Request.Method.POST, url + "events/" + stream + "/" + profile.getString("username"), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("PostResponse", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error.networkResponse != null) {
                        try {
                            Log.e("PostError", new String(error.networkResponse.data, "UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(context,
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
        } else
            Toast.makeText(context, "API login not set!", Toast.LENGTH_SHORT).show();
    }
}
