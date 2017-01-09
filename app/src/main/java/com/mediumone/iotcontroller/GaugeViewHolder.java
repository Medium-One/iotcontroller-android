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
import android.widget.ImageButton;
import android.widget.TextView;

import com.hookedonplay.decoviewlib.DecoView;
import com.mediumone.iotcontroller.mediumone.R;

class GaugeViewHolder extends WidgetViewHolder {

    private View itemView;
    private DecoView gauge;
    private TextView gaugeValue;
    private ImageButton editRange;

    GaugeViewHolder(View itemView) {
        super(itemView);
        gauge = (DecoView) itemView.findViewById(R.id.gauge);
        gaugeValue = (TextView) itemView.findViewById(R.id.gauge_value);
        editRange = (ImageButton) itemView.findViewById(R.id.editrange);
    }

    @Override
    void resetView() {
        gauge.deleteAll();
        gaugeValue.setText("0");
    }

    DecoView getGauge() {
        return gauge;
    }

    TextView getGaugeValue() {
        return gaugeValue;
    }

    ImageButton getEditRange() {
        return editRange;
    }
}