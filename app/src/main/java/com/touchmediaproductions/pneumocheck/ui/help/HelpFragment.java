package com.touchmediaproductions.pneumocheck.ui.help;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.touchmediaproductions.pneumocheck.R;
import com.touchmediaproductions.pneumocheck.helpers.DimensionHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Help Page - shows FAQ
 */
public class HelpFragment extends Fragment {

    private ExpandableListView expandableListView;
    private FAQExpandableListAdapter expandableListAdapter;
    private List<String> expandableListTitle;
    private HashMap<String, List<String>> expandableListDetail;

    private TextView pageTitle;

    //Loading UI
    private ProgressBar loadingBar;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_help, container, false);

        pageTitle = root.findViewById(R.id.text_help);
        pageTitle.setText("HELP");

        int dp8 = (int) DimensionHelper.dpTopixel(getContext(), 8);
        int dp35 = (int) DimensionHelper.dpTopixel(getContext(), 35);
        Drawable drawable = getResources().getDrawable(R.drawable.logoicon, getContext().getTheme());
        drawable.setBounds(0, 0, dp35, dp35);

        pageTitle.setCompoundDrawables(drawable, null, null, null);
        pageTitle.setCompoundDrawablePadding(dp8);

        loadingBar = root.findViewById(R.id.progressbar_help_loading);

        expandableListView = root.findViewById(R.id.expandablelistview_help_faq);

        loadingBar.setVisibility(View.VISIBLE);
        ExpandableListDataInjector.getData().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        setupUI((HashMap<String, List<String>>) task.getResult().get("strings"));
                    }
                }
            }
        });

        return root;
    }

    private void setupUI(HashMap<String, List<String>> strings) {
        expandableListDetail = strings;
        expandableListTitle = new ArrayList<>(expandableListDetail.keySet());
        expandableListAdapter = new FAQExpandableListAdapter(getContext(), expandableListTitle, expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);

        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                expandableListTitle.get(groupPosition);
            }
        });

        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                expandableListTitle.get(groupPosition);
            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                expandableListTitle.get(groupPosition);
                expandableListDetail.get(expandableListTitle.get(groupPosition)).get(childPosition);
                return false;
            }
        });

        loadingBar.post(new Runnable() {
            @Override
            public void run() {
                loadingBar.setVisibility(View.GONE);
            }
        });
    }
}