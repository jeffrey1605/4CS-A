package com.example.autotutoria20;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class c_Lesson_progressive_1 extends AppCompatActivity {

    private AlertDialog dialog;
    private boolean[] cardCompletionStatus = {false, false, false, false}; // Track completion status of each card
    private CustomLoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.c_lesson_progressive_1);

        FrameLayout card1 = findViewById(R.id.card1);
        FrameLayout card2 = findViewById(R.id.card2);
        FrameLayout card3 = findViewById(R.id.card3);
        FrameLayout card4 = findViewById(R.id.card4);

        // Assuming numberOfSteps is determined based on your logic
        int numberOfStepsForCard1 = z_Lesson_steps.lesson_1_steps[0];
        int numberOfStepsForCard2 = z_Lesson_steps.lesson_1_steps[1];
        int numberOfStepsForCard3 = z_Lesson_steps.lesson_1_steps[2];
        int numberOfStepsForCard4 = z_Lesson_steps.lesson_1_steps[3];

        setCardClickListener(card1, 1, numberOfStepsForCard1);
        setCardClickListener(card2, 2, numberOfStepsForCard2);
        setCardClickListener(card3, 3, numberOfStepsForCard3);
        setCardClickListener(card4, 4, numberOfStepsForCard4);

        fetchProgressData();

//        showLoadingDialog(); // Show the loading dialog

        Button exitButton = findViewById(R.id.exitButton);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("onResume()", "I has returned");

        // Fetch the latest progress data
        fetchProgressData();
    }

    private void fetchProgressData() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference progressRef =
                db.collection("users")
                        .document(userId)
                        .collection("Progressive Mode")
                        .document("Lesson 1");

        progressRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                String TAG = "fetchProgressData()";
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> progressData = document.getData();
                        if (progressData != null) {
                            int totalSteps = progressData.size();
                            int currentStep = 0;

                            for (Map.Entry<String, Object> entry : progressData.entrySet()) {
                                String key = entry.getKey();
                                Object value = entry.getValue();
                                if (value instanceof Long) {
                                    int progress = ((Long) value).intValue();
                                    int moduleNumber = Character.getNumericValue(key.charAt(1));
                                    updateUI(moduleNumber, progress);
                                    currentStep++;
//                                    updateProgress((currentStep * 100) / totalSteps); // Update progress
                                }
                            }
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }

//                hideLoadingDialog(); // Hide the loading dialog after data is fetched and processed
            }
        });
    }

//    private void showLoadingDialog() {
//        loadingDialog = new CustomLoadingDialog(this);
//        loadingDialog.setCancelable(false); // Prevent closing the dialog
//        loadingDialog.show();
//    }
//
//    private void updateProgress(int progress) {
//        if (loadingDialog != null) {
//            loadingDialog.setProgress(progress);
//        }
//    }
//
//    private void hideLoadingDialog() {
//        if (loadingDialog != null && loadingDialog.isShowing()) {
//            loadingDialog.dismiss();
//        }
//    }

    private void updateUI(int key, int progress) {
        Log.d("updateUI()", "ETO NA MAG A-UPDATE NA AKOOOO LEZGOOO");

        // Update progress text views
        TextView module1ProgressText = findViewById(R.id.progressive_lesson_1_module_1);
        TextView module2ProgressText = findViewById(R.id.progressive_lesson_1_module_2);
        TextView module3ProgressText = findViewById(R.id.progressive_lesson_1_module_3);
        TextView module4ProgressText = findViewById(R.id.progressive_lesson_1_module_4);

        // Update locked overlays visibility
        FrameLayout card1LockedOverlay = findViewById(R.id.card1_locked_overlay);
        FrameLayout card2LockedOverlay = findViewById(R.id.card2_locked_overlay);
        FrameLayout card3LockedOverlay = findViewById(R.id.card3_locked_overlay);
        FrameLayout card4LockedOverlay = findViewById(R.id.card4_locked_overlay);

        Log.d("updateUI()", "Module: " + key + " | Progress : " + progress);

        String newText;

        card1LockedOverlay.setVisibility(View.GONE);

        switch (key) {
            case 1:
                newText = progress + "/" + z_Lesson_steps.lesson_1_steps[0];
                module1ProgressText.setText(newText);

                if (progress >= z_Lesson_steps.lesson_1_steps[0]) {
                    card2LockedOverlay.setVisibility(View.GONE);
                    setCardCompletionStatus(key, true);
                }
                break;
            case 2:
                newText = progress + "/" + z_Lesson_steps.lesson_1_steps[1];
                module2ProgressText.setText(newText);

                if (progress >= z_Lesson_steps.lesson_1_steps[1]) {
                    card3LockedOverlay.setVisibility(View.GONE);
                    setCardCompletionStatus(key, true);
                }
                break;
            case 3:
                newText = progress + "/" + z_Lesson_steps.lesson_1_steps[2];
                module3ProgressText.setText(newText);

                if (progress >= z_Lesson_steps.lesson_1_steps[2]) {
                    card4LockedOverlay.setVisibility(View.GONE);
                    setCardCompletionStatus(key, true);
                }
                break;
            case 4:
                newText = progress + "/" + z_Lesson_steps.lesson_1_steps[3];
                module4ProgressText.setText(newText);

                if (progress >= z_Lesson_steps.lesson_1_steps[3]) {
                    setCardCompletionStatus(key, true);
                    Log.d("Completed Lesson!", "Lesson 1 Completed! :D");
                }
                break;
            default:
                Log.d("updateUI", "Invalid module number: " + key);
                break;
        }
    }

    private void setCardCompletionStatus(int cardIndex, boolean isCompleted) {
        cardIndex -= 1; // Because Card starts at 0 :>
        Log.d("setCardStatus", "Card " + cardIndex + " Completed!");
        if (cardIndex >= 0 && cardIndex < cardCompletionStatus.length) {
            Log.d("setCardStatus", "Lemme set it");
            cardCompletionStatus[cardIndex] = isCompleted;
        }
    }

    private void navigateToModule(int cardNumber, int numberOfSteps) {
        switch (cardNumber) {
            case 1:
                navigateToModuleActivity(d_Lesson_container.class, numberOfSteps, cardNumber);
                break;
            case 2:
                if (cardCompletionStatus[0])
                    navigateToModuleActivity(d_Lesson_container.class, numberOfSteps, cardNumber);
                else
                    showCustomDialog();
                break;
            case 3:
                if (cardCompletionStatus[1])
                    navigateToModuleActivity(d_Lesson_container.class, numberOfSteps, cardNumber);
                else
                    showCustomDialog();
                break;
            case 4:
                if (cardCompletionStatus[2])
                    navigateToModuleActivity(d_Lesson_container.class, numberOfSteps, cardNumber);
                else
                    showCustomDialog();
                break;
            default:
                Log.e("navigateToModule()", "Invalid Card: Card " + cardNumber);
                break;
        }
    }

    private void navigateToModuleActivity(Class<?> moduleActivityClass, int numberOfSteps, int cardNumber) {
        SharedPreferences sharedPreferences = getSharedPreferences("ModulePreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("numberOfSteps", numberOfSteps);
        editor.putString("learningMode", "Progressive Mode");
        editor.putString("currentLesson", "Lesson 1");
        editor.putString("currentModule", "M" + cardNumber);
        editor.apply();

        Intent intent = new Intent(c_Lesson_progressive_1.this, moduleActivityClass);
        startActivity(intent);
    }

    private void setCardClickListener(FrameLayout card, int cardNumber, int numberOfSteps) {
        card.setOnClickListener(v -> navigateToModule(cardNumber, numberOfSteps));
    }

    private void showCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(c_Lesson_progressive_1.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_complete_previous_lesson, null);
        builder.setView(dialogView);

        Button okayButton = dialogView.findViewById(R.id.okay_button);
        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}