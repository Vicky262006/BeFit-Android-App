package com.example.befit;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TrackerActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TrackerAdapter adapter;
    ArrayList<ExerciseModel> exerciseList;

    FirebaseFirestore db;
    String uid;
    String today;

    Button btnSave;

    // 🔥 TIMER VARIABLES
    TextView tvTimer;
    Button btnStart, btnStop, btnReset;

    private Handler handler = new Handler();
    private long startTime = 0L;
    private long timeInMilliseconds = 0L;
    private long timeSwapBuff = 0L;
    private long updatedTime = 0L;
    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        recyclerView = findViewById(R.id.recyclerTracker);
        btnSave = findViewById(R.id.btnSaveWorkout);

        // 🔥 TIMER VIEW INITIALIZATION
        tvTimer = findViewById(R.id.tvTimer);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnReset = findViewById(R.id.btnReset);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        today = new SimpleDateFormat("EEEE", Locale.getDefault())
                .format(new Date());

        exerciseList = new ArrayList<>();

        loadTodayPlan();

        btnSave.setOnClickListener(v -> saveWorkout());

        // 🔥 TIMER BUTTON LOGIC
        btnStart.setOnClickListener(v -> startTimer());
        btnStop.setOnClickListener(v -> stopTimer());
        btnReset.setOnClickListener(v -> resetTimer());
    }

    // ================= TIMER FUNCTIONS =================

    private void startTimer() {
        if (!isRunning) {
            startTime = SystemClock.uptimeMillis();
            handler.postDelayed(updateTimerThread, 0);
            isRunning = true;
        }
    }

    private void stopTimer() {
        if (isRunning) {
            timeSwapBuff += timeInMilliseconds;
            handler.removeCallbacks(updateTimerThread);
            isRunning = false;
        }
    }

    private void resetTimer() {
        startTime = 0L;
        timeInMilliseconds = 0L;
        timeSwapBuff = 0L;
        updatedTime = 0L;
        tvTimer.setText("00:00");
        handler.removeCallbacks(updateTimerThread);
        isRunning = false;
    }

    private Runnable updateTimerThread = new Runnable() {
        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;

            int seconds = (int) (updatedTime / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            tvTimer.setText(String.format("%02d:%02d", minutes, seconds));

            handler.postDelayed(this, 1000);
        }
    };

    // ================= FIREBASE LOGIC (UNCHANGED) =================

    private void loadTodayPlan() {

        db.collection("users")
                .document(uid)
                .collection("plans")
                .document(today)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {

                        ArrayList list =
                                (ArrayList) doc.get("exercises");

                        exerciseList.clear();

                        for (Object obj : list) {

                            java.util.Map map = (java.util.Map) obj;

                            String name = map.get("exercise").toString();
                            int sets = ((Long) map.get("sets")).intValue();

                            exerciseList.add(new ExerciseModel(name, sets));
                        }

                        adapter = new TrackerAdapter(exerciseList);
                        recyclerView.setLayoutManager(
                                new LinearLayoutManager(this));
                        recyclerView.setAdapter(adapter);
                        adapter.attachRecyclerView(recyclerView);

                        loadPreviousWorkout();
                    }
                });
    }

    private void loadPreviousWorkout() {

        db.collection("users")
                .document(uid)
                .collection("workouts")
                .document(today)
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {

                        ArrayList previous =
                                (ArrayList) doc.get("exercises");

                        adapter.setPreviousData(previous);
                    }
                });
    }

    private void saveWorkout() {

        java.util.List saveList = adapter.getWorkoutData();

        java.util.Map<String, Object> data =
                new java.util.HashMap<>();

        data.put("date", new Date());
        data.put("exercises", saveList);

        // 🔥 OPTIONAL: Save workout duration in seconds
        int totalSeconds = (int) (updatedTime / 1000);
        data.put("duration_seconds", totalSeconds);

        db.collection("users")
                .document(uid)
                .collection("workouts")
                .document(today)
                .set(data)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this,
                                "Workout Saved ✅",
                                Toast.LENGTH_SHORT).show());
    }
}
