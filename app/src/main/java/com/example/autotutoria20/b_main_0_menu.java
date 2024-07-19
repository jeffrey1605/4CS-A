package com.example.autotutoria20;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class b_main_0_menu extends AppCompatActivity {
    private boolean[] cardStates = {true, false, false}; // Example state array for 3 cards
    private static final int PICK_IMAGE_REQUEST = 1;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private ViewPager viewPager;
    private CustomPagerAdapter pagerAdapter;
    private static TextView greetUserName;
    private List<Fragment> progressiveFragmentList;
    private List<Fragment> freeUseFragmentList;
    private long backPressedTime;
    private Button increment_progress;
    private View module, description;
    private ShapeableImageView profileImageView;
    private FrameLayout profileFrameLayout;
    private boolean isProgressiveMode = true;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.b_main_0_menu);

        mAuth = FirebaseAuth.getInstance();

        // Initialize the NavigationView and its header view
        navigationView = findViewById(R.id.navigation_view);
        View headerView = navigationView.getHeaderView(0);
        greetUserName = headerView.findViewById(R.id.user_firstName);
        profileImageView = headerView.findViewById(R.id.user_profile_picture); // Initialize profileImageView here

        // Retrieve user session data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);
        String firstName = sharedPreferences.getString("firstName", null);
        String lastName = sharedPreferences.getString("lastName", null);
        String email = sharedPreferences.getString("email", null);
        String gender = sharedPreferences.getString("gender", null);
        int age = sharedPreferences.getInt("age", -1);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (firstName != null && lastName != null && email != null && gender != null && age != -1) {
            greetUserName.setText("Hello, " + firstName);
        } else {
            Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
        }

        if (isLoggedIn) {
            // User is logged in, set up the UI with user information
            greetUserName.setText("Hello, " + firstName);

            // Retrieve lesson data for both "Progressive Mode" and "Free Use Mode"
            HashMap<String, Map<String, Object>> progressiveModeData = getLessonDataFromPreferences(sharedPreferences, "Progressive Mode");
            HashMap<String, Map<String, Object>> freeUseModeData = getLessonDataFromPreferences(sharedPreferences, "Free Use Mode");

            Log.d("User Info", "Nasa Main Menu kana, eto mga retrieved:");
            Log.d("User Info", "First Name: " + firstName);
            Log.d("User Info", "Gender: " + gender);

            // Check if the user has a custom profile picture
            boolean hasCustomProfilePicture = sharedPreferences.getBoolean("hasCustomProfilePicture", false);

            if (hasCustomProfilePicture) {
                // Set layout picture to custom profile picture
                String customProfilePictureUrl = sharedPreferences.getString("profilePictureUrl", null);
                if (customProfilePictureUrl != null) {
                    Picasso.get().load(customProfilePictureUrl).into(profileImageView);
                }
            } else {
                // User does not have a custom profile picture yet
                if (gender != null) {
                    if (gender.equalsIgnoreCase("male")) {
                        // Apply male anonymous avatar
                        profileImageView.setImageResource(R.drawable.default_male);
                    } else if (gender.equalsIgnoreCase("female")) {
                        // Apply female anonymous avatar
                        profileImageView.setImageResource(R.drawable.deafult_female);
                    }
                } else {
                    // Handle case where gender is null
                    Toast.makeText(this, "Par wala kang Gender??", Toast.LENGTH_SHORT).show();
                }
            }

            Log.d("Main Menu", "Meron nako'ng data ng Lessons");

            // Log and process Progressive Mode data
            if (progressiveModeData != null) {
                List<String> sortedLessonNames = new ArrayList<>(progressiveModeData.keySet());
                Collections.sort(sortedLessonNames); // Sort lesson names alphabetically

                for (String lessonName : sortedLessonNames) {
                    Map<String, Object> lessonData = progressiveModeData.get(lessonName);
                    for (Map.Entry<String, Object> lessonEntry : lessonData.entrySet()) {
                        String moduleName = lessonEntry.getKey();
                        Object moduleValue = lessonEntry.getValue();
                        Log.d("LessonData", "hihi Progressive Mode: " + lessonName + ", Field: " + moduleName + ", Value: " + moduleValue);
                    }
                }
            } else {
                Toast.makeText(this, "Wala par", Toast.LENGTH_SHORT).show();
                Log.d("No Progressive Mode", "Wala ako nakitang Progressive Mode par, sensya na you");
            }

            // Log and process Free Use Mode data
            if (freeUseModeData != null) {
                List<String> sortedLessonNames = new ArrayList<>(freeUseModeData.keySet());
                Collections.sort(sortedLessonNames); // Sort lesson names alphabetically

                for (String lessonName : sortedLessonNames) {
                    Map<String, Object> lessonData = freeUseModeData.get(lessonName);
                    for (Map.Entry<String, Object> lessonEntry : lessonData.entrySet()) {
                        String moduleName = lessonEntry.getKey();
                        Object moduleValue = lessonEntry.getValue();
                        Log.d("LessonData", "hihi Free Use Mode: " + lessonName + ", Field: " + moduleName + ", Value: " + moduleValue);
                    }
                }
            } else {
                Toast.makeText(this, "Wala par", Toast.LENGTH_SHORT).show();
                Log.d("No Free Use Mode", "Wala ako nakitang Free Use Mode par, sensya na you");
            }

        } else {
            // User is not logged in, redirect to login screen
            Intent loginIntent = new Intent(this, a_user_1_login.class);
            startActivity(loginIntent);
            finish();
        }

        // Initialize ViewPager
        viewPager = findViewById(R.id.view_pager);

        if (progressiveFragmentList == null) {
            progressiveFragmentList = new ArrayList<>();
            progressiveFragmentList.add(new b_main_1_lesson_progressive());
            progressiveFragmentList.add(new b_main_3_description());
        }

        if (freeUseFragmentList == null) {
            freeUseFragmentList = new ArrayList<>();
            freeUseFragmentList.add(new b_main_2_lesson_freeuse());
            freeUseFragmentList.add(new b_main_3_description());
        }

        pagerAdapter = new CustomPagerAdapter(getSupportFragmentManager(), progressiveFragmentList);
        viewPager.setAdapter(pagerAdapter);

        // Button highlighter for Header
        module = findViewById(R.id.modulesSelected);
        description = findViewById(R.id.descriptionSelected);

        // OPEN NAVIGATION DRAWER
        Button toggleDrawerButton = findViewById(R.id.toggle_drawer_button);
        toggleDrawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // NAVIGATION DRAWER BUTTONS
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Check if the ActionBar is not null before accessing it
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.settings) {
                    Toast.makeText(b_main_0_menu.this, "Settings", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(b_main_0_menu.this, b_main_0_menu_settings.class));
                } else if (id == R.id.progressive_mode) {
                    showSwitchModeDialog("Progressive Mode");
                } else if (id == R.id.free_use_mode) {
                    showSwitchModeDialog("Free Use Mode");
                } else if (id == R.id.rate) {
                    showRateUsDialog();
                } else if (id == R.id.follow) {
                    openFacebookPage();
                } else if (id == R.id.log_out) {
                    showLogoutDialog();
                }
                // Handle other options if necessary
                return false;
            }
        });

        // SHOW MODULE FRAGMENT
        Button modulesButton = findViewById(R.id.module_button);
        modulesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                description.setBackgroundColor(Color.TRANSPARENT);
                module.setBackgroundColor(Color.WHITE);
                viewPager.setCurrentItem(0);
            }
        });

        // SHOW DESCRIPTION FRAGMENT
        Button descriptionButton = findViewById(R.id.description_button);
        descriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                module.setBackgroundColor(Color.TRANSPARENT);
                description.setBackgroundColor(Color.WHITE);
                viewPager.setCurrentItem(1);
            }
        });

        // Add ViewPager OnPageChangeListener to update button highlights
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // No action needed here
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    description.setBackgroundColor(Color.TRANSPARENT);
                    module.setBackgroundColor(Color.WHITE);
                } else if (position == 1) {
                    module.setBackgroundColor(Color.TRANSPARENT);
                    description.setBackgroundColor(Color.WHITE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // No action needed here
            }
        });

        // Initialize profile image view and frame layout
        profileFrameLayout = headerView.findViewById(R.id.profile_frame_layout);

        profileFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });
    }

    private void showRateUsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.b_main_0_menu_rateus, null);
        builder.setView(dialogView);

        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        Button submitButton = dialogView.findViewById(R.id.submit_button);

        AlertDialog dialog = builder.create();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float rating = ratingBar.getRating();
                Toast.makeText(b_main_0_menu.this, "Rating: " + rating, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.b_main_0_menu_logout, null);
        builder.setView(dialogView);

        Button cancelButton = dialogView.findViewById(R.id.button_cancel);
        Button logoutButton = dialogView.findViewById(R.id.button_logout);

        AlertDialog dialog = builder.create();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // Dismiss the dialog without action
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear user session data
                SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear(); // Clear all session data
                editor.apply();

                Toast.makeText(b_main_0_menu.this, "Logout Successful", Toast.LENGTH_SHORT).show();

                // Dismiss the dialog
                dialog.dismiss();

                // Redirect to login screen
                Intent loginIntent = new Intent(b_main_0_menu.this, a_user_1_login.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);

                // Finish the current activity
                finish();
            }
        });

        // Set the dialog window attributes
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                // Convert dp to pixels
                int width = (int) (400 * getResources().getDisplayMetrics().density);
                int height = (int) (400 * getResources().getDisplayMetrics().density);

                // Set the fixed size for the dialog
                dialog.getWindow().setLayout(width, height);

                // Set the dim amount for the background
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                lp.dimAmount = 0.5f; // Change this value to adjust the dim level (0.0 to 1.0)
                dialog.getWindow().setAttributes(lp);
                dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            }
        });

        dialog.show();
    }


    private void switchMode() {
        Log.d("switchMode()", "Switching mode...");
        if (isProgressiveMode) {
            pagerAdapter.setFragmentList(progressiveFragmentList);
            viewPager.setCurrentItem(progressiveFragmentList.indexOf(new b_main_1_lesson_progressive()));
        } else {
            pagerAdapter.setFragmentList(freeUseFragmentList);
            viewPager.setCurrentItem(freeUseFragmentList.indexOf(new b_main_2_lesson_freeuse()));
        }

        // Close the navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START);
//        pagerAdapter.notifyDataSetChanged();
        Log.d("switchMode()", "Mode switched.");
    }

    private void showSwitchModeDialog(final String mode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(b_main_0_menu.this);
        builder.setMessage("Do you want to switch to " + mode + "?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mode.equals("Progressive Mode")) {
                            isProgressiveMode = true;
                        } else if (mode.equals("Free Use Mode")) {
                            isProgressiveMode = false;
                        }
                        switchMode();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            long currentTime = System.currentTimeMillis();
            if (currentTime - backPressedTime < 2000) {
                super.onBackPressed();
                return;
            } else {
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            }
            backPressedTime = currentTime;
        }
    }

    private void openFacebookPage() {
        String facebookUrl = "https://www.facebook.com/pncofficial";
        String facebookAppUrl = "fb://page/100070669176471"; // Assuming this is the page ID
        String facebookLiteUrl = "fb://lite/page/100070669176471"; // Assuming this is the page ID

        Intent intent = new Intent(Intent.ACTION_VIEW);

        // Try to open the Facebook app
        if (isAppInstalled("com.facebook.katana")) {
            intent.setData(Uri.parse(facebookAppUrl));
        }
        // Try to open Facebook Lite
        else if (isAppInstalled("com.facebook.lite")) {
            intent.setData(Uri.parse(facebookLiteUrl));
        }
        // Fallback to web browser
        else {
            intent.setData(Uri.parse(facebookUrl));
        }

        // Check if there's an app that can handle the intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "No application can handle this request.", Toast.LENGTH_LONG).show();
        }
    }

    // Helper method to check if an app is installed
    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private HashMap<String, Map<String, Object>> getLessonDataFromPreferences(SharedPreferences sharedPreferences, String mode) {
        HashMap<String, Map<String, Object>> lessonData = new HashMap<>();
        Map<String, ?> allEntries = sharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(mode + ": ")) {
                String[] keyParts = key.split(", ");
                if (keyParts.length == 2) {
                    String lessonName = keyParts[0].substring((mode + ": ").length());
                    String fieldName = keyParts[1];
                    int value = (int) entry.getValue();

                    if (!lessonData.containsKey(lessonName)) {
                        lessonData.put(lessonName, new HashMap<String, Object>());
                    }
                    lessonData.get(lessonName).put(fieldName, value);
                }
            }
        }
        return lessonData;
    }
}
