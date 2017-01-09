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

import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.mediumone.iotcontroller.mediumone.R;

class MapViewHolder extends WidgetViewHolder {

    private MapView map;
    private GoogleMap googleMap;

    MapViewHolder(View itemView) {
        super(itemView);
        map = (MapView) itemView.findViewById(R.id.mapview);
    }

    @Override
    void resetView() {
        googleMap.clear();
    }

    MapView getMap() {
        return map;
    }

    GoogleMap getGoogleMap() {
        return googleMap;
    }

    void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }
}