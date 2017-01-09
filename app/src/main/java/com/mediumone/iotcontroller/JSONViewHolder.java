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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.mediumone.iotcontroller.mediumone.R;

class JSONViewHolder extends WidgetViewHolder {

    private EditText value;
    private ToggleButton bool;
    private Spinner type;
    private Button send;

    JSONViewHolder(View itemView) {
        super(itemView);
        value = (EditText) itemView.findViewById(R.id.value);
        bool = (ToggleButton) itemView.findViewById(R.id.bool);
        type = (Spinner) itemView.findViewById(R.id.type);
        send = (Button) itemView.findViewById(R.id.post);
    }

    @Override
    void resetView() {
        value.setText("");
        bool.setChecked(false);
        type.setSelection(0);
    }

    public EditText getValue() {
        return value;
    }

    public ToggleButton getBool() {
        return bool;
    }

    Spinner getType() {
        return type;
    }

    Button getSend() {
        return send;
    }
}