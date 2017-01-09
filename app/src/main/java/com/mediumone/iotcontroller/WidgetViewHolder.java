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

import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mediumone.iotcontroller.mediumone.R;

abstract class WidgetViewHolder extends RecyclerView.ViewHolder{

    View itemView;
    private ViewPager pager;
    private ImageButton editStream;
    private ImageButton editTag;
    private ImageButton remove;
    private TextView tag;

    public WidgetViewHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
        pager = (ViewPager) itemView.findViewById(R.id.viewPager);
        editStream = (ImageButton) itemView.findViewById(R.id.editstream);
        editTag = (ImageButton) itemView.findViewById(R.id.edittag);
        remove = (ImageButton) itemView.findViewById(R.id.remove);
        tag = (TextView) itemView.findViewById(R.id.tag);
    }

    View getItemView() {
        return itemView;
    }

    ViewPager getPager() {
        return pager;
    }

    ImageButton getEditStream() {
        return editStream;
    }

    ImageButton getEditTag() {
        return editTag;
    }

    TextView getTagName() {
        return tag;
    }

    ImageButton getRemove() {
        return remove;
    }

    abstract void resetView();
}
