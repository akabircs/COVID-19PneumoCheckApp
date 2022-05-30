package com.touchmediaproductions.pneumocheck.ui.info;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.touchmediaproductions.pneumocheck.R;
import com.touchmediaproductions.pneumocheck.helpers.DimensionHelper;

/**
 * Info fragment - part of elements added programmatically (example of other way of populating ui)
 */
public class InfoFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_info, container, false);
        LinearLayout linearLayout = (LinearLayout) root.getRootView();
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        //Need to convert from DP to pixels as sizes are in pixels for methods - Using the DimensionsHelper class
        int dp8 = (int) DimensionHelper.dpTopixel(getContext(), 8);

        //Add Notification TextView
        TextView textView1 = new TextView(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.MarginLayoutParams marginLayoutParams = (LinearLayout.MarginLayoutParams) linearLayout.getLayoutParams();
        //Set margins in pixels of equivalence to 8dp
        marginLayoutParams.setMargins(dp8, dp8, dp8, dp8);
        textView1.setLayoutParams(layoutParams);
        textView1.setText("COVID-19 PneumoCheck App is not a self-diagnostic tool, its only for medical research purposes.");

        //Set an icon on the notification box
        Drawable drawable = getResources().getDrawable(R.drawable.ic_baseline_info_24, getContext().getTheme());
        drawable.setTint(getResources().getColor(R.color.colorPrimaryLight, getContext().getTheme()));
        textView1.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        textView1.setCompoundDrawablePadding(dp8);

        textView1.setPadding(dp8, dp8, dp8, dp8);

        //Notification Card at Top - Dismissable
        final MaterialCardView materialCardView = new MaterialCardView(getContext());
        materialCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialCardView.setVisibility(View.GONE);
            }
        });
        materialCardView.setRadius(DimensionHelper.dpTopixel(getContext(), 20));
        materialCardView.addView(textView1);
        materialCardView.setPadding(dp8, dp8, dp8, dp8);

        //add button to the layout
        linearLayout.addView(materialCardView, 2);

        //Hide Notification
        materialCardView.setVisibility(View.GONE);

        return root;
    }



}