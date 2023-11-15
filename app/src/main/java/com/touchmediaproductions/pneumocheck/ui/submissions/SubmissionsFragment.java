package com.touchmediaproductions.pneumocheck.ui.submissions;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.touchmediaproductions.pneumocheck.R;
import com.touchmediaproductions.pneumocheck.adapters.SubmissionsRecyclerViewAdapter;
import com.touchmediaproductions.pneumocheck.helpers.DimensionHelper;
import com.touchmediaproductions.pneumocheck.helpers.FirestoreRepository;
import com.touchmediaproductions.pneumocheck.helpers.ToastHelper;
import com.touchmediaproductions.pneumocheck.models.SubmissionModel;
import com.touchmediaproductions.pneumocheck.models.UserProfile;
import com.touchmediaproductions.pneumocheck.ui.settings.SettingsViewModel;

import java.util.ArrayList;
import java.util.List;

public class SubmissionsFragment extends Fragment{

    private static final String TAG = "SubmissionsFragment";

    private TextView pageTitle;

    private RecyclerView submissionsRecyclerView;
    private SubmissionsRecyclerViewAdapter submissionsRecyclerAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<SubmissionModel> submissionModelArrayList = new ArrayList<>();

    //Notification Card
    private CardView notificationCard;
    private TextView notificationContent;

    //Authenticated User
    private String authenticatedUserUID;


    //MVVM Pattern
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_submissions, container, false);

        //Connect the ViewModel
        SubmissionsViewModel model = new ViewModelProvider(getActivity()).get(SubmissionsViewModel.class);

        //Set Authenticated User
        authenticatedUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Pane Title
        pageTitle = root.findViewById(R.id.textview_pagetitle_submissions);
        pageTitle.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        pageTitle.setText("SUBMISSIONS");

        //RecyclerView
        swipeRefreshLayout = root.findViewById(R.id.swiperefresh_submissions);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Refresh the data
                model.forceRefreshAllSubmissionsForUser(authenticatedUserUID).observe(requireActivity(), submissionModels -> {
                    submissionModelArrayList.clear();
                    submissionModelArrayList.addAll(submissionModels);
                    submissionsRecyclerAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        });

        //Logo Next to Pane Title
        int dp8 = (int) DimensionHelper.dpTopixel(getContext(), 8);
        int dp35 = (int) DimensionHelper.dpTopixel(getContext(), 35);
        Drawable drawable = getResources().getDrawable(R.drawable.logoicon, getContext().getTheme());
        drawable.setBounds(0, 0, dp35, dp35);
        pageTitle.setCompoundDrawables(drawable, null, null, null);
        pageTitle.setCompoundDrawablePadding(dp8);

        //Notification Card
        notificationCard = root.findViewById(R.id.card_submissions_notificationcard);
        notificationContent = root.findViewById(R.id.textview_submissions_notificationtext);

        //Submissions
        submissionsRecyclerView = root.findViewById(R.id.recyclerview_submissionsfragment_submissions);

        populateData();

        model.getAllSubmissionsForUser(authenticatedUserUID).observe(requireActivity(), submissionModels -> {
            //Load Data
            submissionModelArrayList.clear();
            submissionModelArrayList.addAll((ArrayList<SubmissionModel>) submissionModels);
            submissionsRecyclerAdapter.notifyDataSetChanged();
            //Show or Hide Notification Card if Array is empty
            shouldShowCard(submissionModelArrayList.isEmpty());
        });

        return root;
    }

    private void populateData(){
        submissionsRecyclerAdapter = new SubmissionsRecyclerViewAdapter(getContext(), submissionModelArrayList, this);
        submissionsRecyclerView.setAdapter(submissionsRecyclerAdapter);

        //Setup a Linear Layout Manager so it shows in reverse order, newest at the top
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        submissionsRecyclerView.setLayoutManager(linearLayoutManager);

        //Show or Hide Notification Card if Array is empty
        shouldShowCard(submissionModelArrayList.isEmpty());
    }

    public void shouldShowCard(boolean isArrayListEmpty) {
        if(isArrayListEmpty){
            notificationContent.setText(R.string.notification_database_is_empty);
            notificationCard.setVisibility(View.VISIBLE);
        } else {
            notificationCard.setVisibility(View.GONE);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UpdateSubmissionFragmentDialog.DIALOG_UPDATE_SUBMISSION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.getExtras().containsKey("updatedSubmission")) {
                    SubmissionModel updatedSubmission = (SubmissionModel) data.getExtras().getSerializable("updatedSubmission");
                    int submissionToUpdatePosition = data.getExtras().getInt("position");
//                    if(updateSubmissionInAdapter(updatedSubmission, submissionToUpdatePosition)){
//                        ToastHelper.showShortToast(getContext(), "Submission Updated");
//                    }
                }
            }
        }
    }
}