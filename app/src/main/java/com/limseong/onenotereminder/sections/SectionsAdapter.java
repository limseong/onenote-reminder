// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

// <EventListAdapterSnippet>
package com.limseong.onenotereminder.sections;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.limseong.onenotereminder.R;
import com.microsoft.graph.models.extensions.OnenoteSection;
import java.util.List;

import androidx.annotation.NonNull;

public class SectionsAdapter extends ArrayAdapter<OnenoteSection> {
    private Context mContext;
    private int mResource;

    // Used for the ViewHolder pattern
    // https://developer.android.com/training/improving-layouts/smooth-scrolling
    static class ViewHolder {
        TextView title;
        TextView parentSectionGroup;
        TextView parentNotebook;
    }

    public SectionsAdapter(Context context, int resource, List<OnenoteSection> events) {
        super(context, resource, events);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OnenoteSection section = getItem(position);
        Log.d("AAAAAAAAAAAA", section.displayName);

        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);

            holder = new ViewHolder();
            holder.title = convertView.findViewById(R.id.section_title);
            holder.parentSectionGroup = convertView.findViewById(R.id.section_parentSectionGroup);
            holder.parentNotebook = convertView.findViewById(R.id.section_parentNotebook);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(section.displayName);
        if (section.parentSectionGroup != null)
            holder.parentSectionGroup.setText(section.parentSectionGroup.displayName);
        if (section.parentNotebook != null)
            holder.parentNotebook.setText(section.parentNotebook.displayName);

        return convertView;
    }
}
// </EventListAdapterSnippet>
