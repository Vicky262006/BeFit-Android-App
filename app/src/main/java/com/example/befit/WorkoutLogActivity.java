package com.example.befit;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class WorkoutLogActivity extends AppCompatActivity {

    TextView tvLog;
    FirebaseFirestore db;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_log);

        tvLog = findViewById(R.id.tvWorkoutLog);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        loadWorkoutHistory();
    }

    private void loadWorkoutHistory() {

        db.collection("users")
                .document(uid)
                .collection("workouts")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    StringBuilder builder = new StringBuilder();

                    queryDocumentSnapshots.forEach(doc -> {

                        Date date = doc.getDate("date");

                        if (date != null) {

                            SimpleDateFormat dateFormat =
                                    new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

                            SimpleDateFormat timeFormat =
                                    new SimpleDateFormat("hh:mm a", Locale.getDefault());

                            builder.append("📅 ")
                                    .append(dateFormat.format(date))
                                    .append("\n");

                            builder.append("⏰ ")
                                    .append(timeFormat.format(date))
                                    .append("\n\n");
                        }

                        ArrayList exercises =
                                (ArrayList) doc.get("exercises");

                        if (exercises != null) {

                            for (Object obj : exercises) {

                                Map exerciseMap = (Map) obj;

                                builder.append("🏋 ")
                                        .append(exerciseMap.get("exercise"))
                                        .append("\n");

                                ArrayList sets =
                                        (ArrayList) exerciseMap.get("sets");

                                if (sets != null) {

                                    int count = 1;

                                    for (Object setObj : sets) {

                                        Map setMap = (Map) setObj;

                                        builder.append("   Set ")
                                                .append(count)
                                                .append(": ")
                                                .append(setMap.get("reps"))
                                                .append(" reps | ")
                                                .append(setMap.get("weight"))
                                                .append(" kg\n");

                                        count++;
                                    }
                                }

                                builder.append("\n");
                            }
                        }

                        builder.append("────────────────────\n\n");
                    });

                    tvLog.setText(builder.toString());
                });
    }
}