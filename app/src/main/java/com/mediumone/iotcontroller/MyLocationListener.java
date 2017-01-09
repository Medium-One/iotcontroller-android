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

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public class MyLocationListener implements LocationListener {
    public JSONObject locObj;


    public void onLocationChanged(Location loc) {
        try {
            locObj = new JSONObject().put("device_speed", loc.getSpeed())
                                     .put("horizontal_accuracy", loc.getAccuracy())
                                     .put("device_location", String.valueOf(loc.getLatitude()) + " " + String.valueOf(loc.getLongitude()) + " " + loc.getAltitude());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJSON() {
        return locObj;
    }


    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
}