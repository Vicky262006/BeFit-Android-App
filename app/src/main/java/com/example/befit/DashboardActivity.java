package com.example.befit;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.app.AlertDialog;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardActivity extends AppCompatActivity {

    MaterialCardView btnWorkout, btnTracker, btnProgress;
    TextView tvGreeting;
    BottomNavigationView bottomNavigation;

    FirebaseFirestore db;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnWorkout = findViewById(R.id.btnWorkout);
        btnTracker = findViewById(R.id.btnTracker);
        btnProgress = findViewById(R.id.btnProgress);
        tvGreeting = findViewById(R.id.tvGreeting);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        MaterialCardView btnWorkoutLog = findViewById(R.id.btnWorkoutLog);



        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        loadUserName();
        updateWeeklyStatus();

        btnWorkout.setOnClickListener(v ->
                startActivity(new Intent(this, WorkoutPlanActivity.class)));

        btnTracker.setOnClickListener(v ->
                startActivity(new Intent(this, TrackerActivity.class)));

        btnProgress.setOnClickListener(v ->
                startActivity(new Intent(this, ProgressActivity.class)));
        btnWorkoutLog.setOnClickListener(v ->
                startActivity(new Intent(this, WorkoutLogActivity.class))
        );
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Already on the dashboard
            } else if (itemId == R.id.nav_plan) {
                startActivity(new Intent(this, WorkoutPlanActivity.class));
            } else if (itemId == R.id.nav_tracker) {
                startActivity(new Intent(this, TrackerActivity.class));
            } else if (itemId == R.id.nav_progress) {
                startActivity(new Intent(this, ProgressActivity.class));
            }
            return true;
        });
    }

    private void loadUserName() {

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists() && doc.getString("name") != null) {

                        String name = doc.getString("name");
                        tvGreeting.setText(getString(R.string.greeting, name));

                    } else {
                        askUserName();
                    }
                });
    }

    private void askUserName() {

        EditText input = new EditText(this);
        input.setHint("Enter your name");

        new AlertDialog.Builder(this)
                .setTitle("Welcome!")
                .setMessage("Enter your name")
                .setView(input)
                .setCancelable(false)
                .setPositiveButton("Save", (dialog, which) -> {

                    String name = input.getText().toString().trim();

                    if (!name.isEmpty()) {

                        java.util.Map<String, Object> map = new java.util.HashMap<>();
                        map.put("name", name);

                        db.collection("users")
                                .document(uid)
                                .set(map, com.google.firebase.firestore.SetOptions.merge());

                        tvGreeting.setText(getString(R.string.greeting, name));
                    }
                })
                .show();
    }
    private void updateWeeklyStatus() {

        String[] days = {"Monday","Tuesday","Wednesday",
                "Thursday","Friday","Saturday"};

        for (String day : days) {

            db.collection("users")
                    .document(uid)
                    .collection("workouts")
                    .document(day)
                    .get()
                    .addOnSuccessListener(doc -> {

                        TextView tv = getDayView(day);
                        if (tv == null) return;

                        if (doc.exists()) {
                            tv.setText("✔");
                            tv.setTextColor(ContextCompat.getColor(
                                    this, android.R.color.holo_green_dark));
                        } else {
                            tv.setText("✖");
                            tv.setTextColor(ContextCompat.getColor(
                                    this, android.R.color.holo_red_dark));
                        }
                    });
        }
    }

    private TextView getDayView(String day) {

        switch (day) {
            case "Monday": return findViewById(R.id.mon);
            case "Tuesday": return findViewById(R.id.tue);
            case "Wednesday": return findViewById(R.id.wed);
            case "Thursday": return findViewById(R.id.thu);
            case "Friday": return findViewById(R.id.fri);
            case "Saturday": return findViewById(R.id.sat);
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_logout) {

            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
