package com.touchmediaproductions.pneumocheck.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.touchmediaproductions.pneumocheck.R;
import com.touchmediaproductions.pneumocheck.helpers.DateHelper;
import com.touchmediaproductions.pneumocheck.helpers.FirestoreRepository;
import com.touchmediaproductions.pneumocheck.helpers.PictureHelper;
import com.touchmediaproductions.pneumocheck.helpers.ToastHelper;
import com.touchmediaproductions.pneumocheck.ml.MLHelper;
import com.touchmediaproductions.pneumocheck.models.SubmissionModel;
import com.touchmediaproductions.pneumocheck.ui.submissions.SubmissionsFragment;
import com.touchmediaproductions.pneumocheck.ui.submissions.UpdateSubmissionFragmentDialog;

import java.util.ArrayList;

public class SubmissionsRecyclerViewAdapter extends RecyclerView.Adapter<SubmissionsRecyclerViewAdapter.MyViewHolder> {

    private static final String TAG = "SubmissionsRecyclerViewAdapter";

    private Context context;
    private ArrayList<SubmissionModel> data;
    private OnItemClickListener adapterOnClickListener;
    private SubmissionsFragment parentFragment;

    public interface OnItemClickListener {
        void OnItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        adapterOnClickListener = listener;
    }

    public SubmissionsRecyclerViewAdapter(Context context, ArrayList<SubmissionModel> data, SubmissionsFragment parentFragment) {
        this.context = context;
        this.data = data;
        this.parentFragment = parentFragment;
    }

    public void clearData() {
        data.clear();
    }

    public void addAll(ArrayList<SubmissionModel> data) {
        this.data.addAll(data);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.fragment_submission_card, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        String firstname = data.get(position).getFirstName();
        String lastname = data.get(position).getLastName();

        String initialsBadge = "";
        if (firstname.length() > 0 && lastname.length() > 0) {
            initialsBadge = firstname.charAt(0) + String.valueOf(lastname.charAt(0));
        } else if (firstname.length() > 0) {
            initialsBadge = firstname.charAt(0) + "";
        }
        initialsBadge = initialsBadge.toUpperCase();

        String scanCreationDateString = DateHelper.fromDateToDisplayPrettyShortDateString(data.get(position).getScanCreationDate());
        String submissionCreationDateString = DateHelper.fromDateToDisplayPrettyShortDateString(data.get(position).getSubmissionCreationDate());

        Timestamp learntAt = data.get(position).getLearntAt();
        String learntAtText = "";
        if (learntAt != null) {
            learntAtText = data.get(position).getLearntAt().toDate().toString();
        }

        holder.textview_initials.setText(initialsBadge);
        holder.textview_xrayid.setText(String.valueOf(data.get(position).getId()));
        holder.textview_firstname.setText(firstname);
        holder.textview_lastname.setText(lastname);
        holder.textview_age.setText(String.valueOf(data.get(position).getAge()));
        holder.textview_scancreationdate.setText(scanCreationDateString);
        holder.textview_submissioncreationdate.setText(submissionCreationDateString);
        holder.textview_sex.setText(data.get(position).getSex());
        SubmissionModel.Confirmation predictionConfirmation = data.get(position).getConfirmation();
        MLHelper.Prediction prediction = data.get(position).getPrediction();
        byte[] photoInByte = data.get(position).getCxrPhoto();
        Bitmap bitmap = PictureHelper.byteArrayToBitmap(photoInByte);
        holder.imageview_xrayphoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.imageview_xrayphoto.setImageBitmap(bitmap);

        //Prediction:
        String result;
        String resultInfo;
        if (predictionConfirmation == SubmissionModel.Confirmation.UNCONFIRMED) {
            if (prediction == null) {
                result = "Pending...";
                resultInfo = "";
            } else {
                result = prediction.getFirst();
                resultInfo = prediction.toString();
                holder.textview_result.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            }
        } else {
            result = predictionConfirmation.name();
            holder.textview_result.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            resultInfo = "CONFIRMED";
        }
        //Make it red if COVID, blue if Normal and Orange if Pneumonia
        if (result.toUpperCase().contains("COVID")) {
            holder.textview_result.setTextColor(Color.RED);
        } else if (result.toUpperCase().contains("PNEUMONIA")) {
            holder.textview_result.setTextColor(Color.rgb(255, 165, 0));
        } else if (result.toUpperCase().contains("NORMAL")) {
            holder.textview_result.setTextColor(context.getResources().getColor(R.color.colorPrimaryLight, context.getTheme()));
        }
        holder.textview_result.setText(result);
        holder.textview_resultinfo.setText(resultInfo);

        final int cp = position;
        final SubmissionModel info = data.get(position);

        holder.btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteItemWithAlert(info);
            }
        });

        holder.btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Start Update Submission Fragment Dialog
                FragmentManager fm = parentFragment.getParentFragmentManager();
                UpdateSubmissionFragmentDialog updateDialogFragment = UpdateSubmissionFragmentDialog.newInstance(position, info);
                //Important for the fragment to return here
                updateDialogFragment.setTargetFragment(parentFragment, UpdateSubmissionFragmentDialog.DIALOG_UPDATE_SUBMISSION_REQUEST_CODE);
                updateDialogFragment.show(fm, null);
            }
        });

        holder.linearlayout_learntAt.setVisibility(View.GONE);
        if (!learntAtText.trim().isEmpty()) {
            holder.linearlayout_learntAt.setVisibility(View.VISIBLE);
            holder.textview_learntAt.setText(learntAtText);
        }

        holder.rowInRecyclerView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ToastHelper.showShortToast(context, "Submission Creation Date: " + info.getSubmissionCreationDate().toString());
                return true;
            }
        });


    }

    /**
     * Remove item info (SubmissionModel) from the Recycler view.
     *
     * @param info
     */
    public void removeItem(SubmissionModel info) {
        int position = data.indexOf(info);
        data.remove(position);
        ToastHelper.showShortToast(context, "Deleted Submission");
        notifyItemRemoved(position);
    }

    /**
     * Update item at the given position with the data from the given info (SubmissionModel)
     *
     * @param position
     * @param info
     */
    public void updateItem(int position, SubmissionModel info) {
        data.set(position, info);
        notifyItemChanged(position);
    }

    /**
     * Delete an item in the Recycler View as well as the database once prompt is confirmed by user
     *
     * @param info
     */
    private void deleteItemWithAlert(final SubmissionModel info) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("Alert!");
        builder.setBackground(context.getDrawable(R.drawable.dialog_background));
        builder.setMessage("Are you sure you want to remove submission?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (removeFromDatabase(info)) {
                            //Remove item
                            removeItem(info);
                            //Show notification that database is empty if database is empty
                            parentFragment.shouldShowCard(data.isEmpty());
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

//    private boolean removeFromDatabase(SubmissionModel submissionModel) {
//        DataBaseHelper dataBaseHelper = new DataBaseHelper(context);
//        return dataBaseHelper.delete(submissionModel.getId());
//    }

    private boolean removeFromDatabase(SubmissionModel submissionModel) {
        FirestoreRepository.deleteSubmission(submissionModel);
        return true;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textview_initials, textview_xrayid, textview_firstname,
                textview_lastname, textview_age, textview_sex, textview_scancreationdate,
                textview_submissioncreationdate, textview_result, textview_resultinfo, textview_learntAt;
        ImageView imageview_xrayphoto;
        Button btn_edit, btn_delete;
        MaterialCardView rowInRecyclerView;
        LinearLayout linearlayout_learntAt;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            //Correlate each object to its view in the fragment
            //Form Fields
            textview_initials = itemView.findViewById(R.id.textview_submissioncard_initials_badge);
            textview_xrayid = itemView.findViewById(R.id.textview_submissioncard_xrayid);
            textview_firstname = itemView.findViewById(R.id.textview_submissionscard_firstname);
            textview_lastname = itemView.findViewById(R.id.textview_submissionscard_lastname);
            textview_age = itemView.findViewById(R.id.textview_submissionscard_age);
            textview_sex = itemView.findViewById(R.id.textview_submissionscard_sex);
            textview_scancreationdate = itemView.findViewById(R.id.textview_submissioncard_scancreationdate);
            textview_submissioncreationdate = itemView.findViewById(R.id.textview_submissioncard_submissioncreationdate);
            //X-Ray preview
            imageview_xrayphoto = itemView.findViewById(R.id.imageview_submissioncard_xraythumbnail);
            //Prediction Results
            textview_result = itemView.findViewById(R.id.textView_submissioncard_result);
            textview_resultinfo = itemView.findViewById(R.id.textview_submissioncard_resultinfo);

            rowInRecyclerView = itemView.findViewById(R.id.cardview_submissioncard_entry);

            //Buttons
            btn_edit = itemView.findViewById(R.id.btn_submissioncard_edit);
            btn_delete = itemView.findViewById(R.id.btn_submissioncard_delete);

            //Learnt At
            linearlayout_learntAt = itemView.findViewById(R.id.linearlayout_submissioncard_learntAt_display);
            textview_learntAt = itemView.findViewById(R.id.textview_submissioncard_learntAt);
        }
    }
}