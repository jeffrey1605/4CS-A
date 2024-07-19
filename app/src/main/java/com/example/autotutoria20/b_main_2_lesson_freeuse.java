package com.example.autotutoria20;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class b_main_2_lesson_freeuse extends Fragment {
    private static TextView learningModeText;
    private FrameLayout lockedOverlayCard1, lockedOverlayCard2, lockedOverlayCard3, lockedOverlayCard4;
    private ProgressBar progressBarCard1, progressBarCard2, progressBarCard3, progressBarCard4;
    private TextView progressTextCard1, progressTextCard2, progressTextCard3, progressTextCard4;
    private Button incrementCard1Button;
    private boolean isProgressiveMode = true; // Default mode is progressive mode
    private static View view;
    private Dialog dialog;
    private Dialog loadingDialog;

    // Define card progress array
    private int[] cardProgress = new int[z_Lesson_steps.total_module_count]; // refer to the assigned value

    // Track completion status of each card
    private boolean[] cardCompletionStatus = {false, false, false, false}; // Default is false for all cards

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.b_main_2_lesson_freeuse, container, false);

        String TAG = "Free Use Mode";

        Log.e(TAG, "Imma launch na already");

        // Initialize view
        initializeViews();

        Log.e(TAG, "Done na mag initializeViews()");

        // Show loading dialog
        showLoadingDialog();

        Log.e(TAG, "Done na mag showLoadingDialog");

//        // Set up the increment button
//        Button increment = view.findViewById(R.id.increment_progress);
//        increment.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                for (int i = 0; i < 4; i++) {
//                    if (cardProgress[i] < 100) {
//                        incrementCard(i, 25);
//                        break;
//                    }
//                    if (i == 3 && cardProgress[i] >= 100) {
//                        showToast("Progressive Mode is Complete.");
//                    }
//                }
//            }
//        });

        return view;
    }

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_loading, null);
        builder.setView(dialogView);
        builder.setCancelable(false); // Prevent closing the dialog
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        Log.e("HIDE", "Hide Loading Dialog PLEASE!");
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    // onResume is called everytime the user returns to main menu
    // From the Lessons Layout
    @Override
    public void onResume() {
        super.onResume();
        fetchAllProgressData();
    }

    private void fetchAllProgressData() {
        Log.d("fetchAllProgressData", "Method called");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String TAG = "fetchAllProgressData()";

        // Reference to the user's progress data collection
        CollectionReference progressRef =
                db.collection("users")
                        .document(userId)
                        .collection("Free Use Mode");

        // Directory ends at Progressive Mode
        // Meaning, this retrieves all of the Lessons

        // Fetch all lessons
        progressRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot lessonDoc : task.getResult()) {
                        String lesson = lessonDoc.getId();
                        Log.d(TAG, "Lesson: " + lesson);

                        // Initialize variables for total progress calculation
                        int totalProgress = 0;
                        int totalMaxProgress = 0;

                        // Get lesson number from the lesson name

                        // substring 7 kasi nasa 7th character yung number ng Lesson
                        int lessonNumber = Integer.parseInt(lesson.substring(7).trim());
                        int[] maxProgressValues = z_Lesson_steps.getLessonSteps(lessonNumber);

                        // This part of the code retrieves the individual modules
                        // within a certain Lesson

                        // Iterate over all fields to get module progress
                        for (int i = 0; i < maxProgressValues.length; i++) {
                            String key = "M" + (i + 1);
                            Long moduleProgress = lessonDoc.getLong(key);
                            if (moduleProgress != null) {
                                // Update total progress and max progress
                                totalProgress += moduleProgress;
                                totalMaxProgress += maxProgressValues[i];

                                // Log the progress for checking
                                Log.d(TAG, "Free Use Mode | " + lesson + " | " + key + " Progress: " + moduleProgress + "/" + maxProgressValues[i]);
                            } else {
                                Log.d(TAG, lesson + " | " + key + " Progress data not found.");
                            }
                        }

                        // Calculate overall progress for the lesson
                        if (totalMaxProgress > 0) {
                            double overallProgress = ((double) totalProgress / totalMaxProgress) * 100;
                            int overallProgressInt = (int) Math.round(overallProgress);

                            Log.d(TAG, lesson + " | Overall Progress: " + overallProgress + "%");

                            Log.e(TAG, "Let's call incrementCard() method");
                            incrementCard(lessonNumber, overallProgressInt);
                        } else {
                            Log.d(TAG, lesson + " | No progress data available.");
                        }
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void initializeViews() {

        // Progress Bar
        progressBarCard1 = view.findViewById(R.id.progressBar_card_1);
        progressBarCard2 = view.findViewById(R.id.progressBar_card_2);
        progressBarCard3 = view.findViewById(R.id.progressBar_card_3);
        progressBarCard4 = view.findViewById(R.id.progressBar_card_4);

        // Progress Text
        progressTextCard1 = view.findViewById(R.id.progressText_card_1);
        progressTextCard2 = view.findViewById(R.id.progressText_card_2);
        progressTextCard3 = view.findViewById(R.id.progressText_card_3);
        progressTextCard4 = view.findViewById(R.id.progressText_card_4);

        setCardClickListeners();

    }

    // Method to set click listeners for the cards
    private void setCardClickListeners() {
        // Card 1 click listener
        view.findViewById(R.id.card1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), c_Lesson_freeuse_1.class);
                startActivity(intent);
            }
        });


        // Card 2 click listener
        view.findViewById(R.id.card2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), c_Lesson_freeuse_2.class);
                startActivity(intent);
            }
        });

        // Card 3 click listener
        view.findViewById(R.id.card3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), c_Lesson_freeuse_3.class);
                startActivity(intent);
            }
        });

        // Card 4 click listener
        view.findViewById(R.id.card4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), c_Lesson_freeuse_4.class);
                startActivity(intent);
            }
        });
    }

    // Method to display a toast message
    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void incrementCard(int cardId, int incrementValue) {
        // Card ID from called method ranges from [1 -> 4]
        // But the Array              ranges from [0 -> 3]
        // So decrementing by 1 fixes the problem.
        cardId -= 1;

        String TAG = "incrementCard()"; // For debugging purposes

        // add the current progress with the increment value
        // and set to an int variable
        int newProgress = cardProgress[cardId] + incrementValue;

        // set the new value
        // Math.min is used to ensure it will only be maxed to 100%
        cardProgress[cardId] = Math.min(newProgress, 100);

        // if current card is complete
        // array is still from [0 -> 3]
        // so no need to increment the cardId yet.
        if (cardProgress[cardId] >= 100) {
            cardCompletionStatus[cardId] = true;
        }

        // increment cardId in this part
        // because the layout uses range from [1 -> 4]
        // rather than how array works   from [0 -> 3]
        cardId += 1;

        // Update UI for the current card
        ProgressBar progressBar = view.findViewById(getProgressBarId(cardId));
        TextView progressText = view.findViewById(getProgressTextId(cardId));

        if (progressBar == null || progressText == null) {
            Log.e(TAG, "ProgressBar or ProgressText is null for cardId: " + cardId);
            return;
        }

        // decrement again
        // same reason, but why??
        // because the progressBar and progressText
        // should be initialized first
        cardId -= 1;

        progressBar.setProgress(cardProgress[cardId]);
        progressText.setText(cardProgress[cardId] + "% Completed");

        if (cardId == 3) {
            showToast("This is the end my friend");
            Log.e(TAG, "This is the end my friend");

            // Change this value accordingly
            double delayInSeconds = 2.5;
            int delayMillis = (int) (delayInSeconds * 1000);

            Log.e(TAG, "Imma end this dialog");

            // Add a delay before hiding the loading dialog
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "ETO NAAAA ");
                    hideLoadingDialog();
                    // Show main menu or update UI accordingly
                }
            }, delayMillis);
        }
    }

    // Helper methods to get the ProgressBar and TextView IDs
    private int getProgressBarId(int index) {
        switch (index) {
            case 1:
                return R.id.progressBar_card_1;
            case 2:
                return R.id.progressBar_card_2;
            case 3:
                return R.id.progressBar_card_3;
            case 4:
                return R.id.progressBar_card_4;
            default:
                return 0;
        }
    }

    private int getProgressTextId(int index) {
        switch (index) {
            case 1:
                return R.id.progressText_card_1;
            case 2:
                return R.id.progressText_card_2;
            case 3:
                return R.id.progressText_card_3;
            case 4:
                return R.id.progressText_card_4;
            default:
                return 0;
        }
    }


}
