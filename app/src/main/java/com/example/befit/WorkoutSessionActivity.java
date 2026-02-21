package com.example.befit;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class WorkoutSessionActivity extends AppCompatActivity {

    EditText etExercise, etSets;
    Button btnAdd, btnSave;
    LinearLayout listContainer;

    FirebaseFirestore db;
    FirebaseAuth auth;

    List<Map<String, Object>> exerciseList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_session);

        etExercise = findViewById(R.id.etExercise);
        etSets = findViewById(R.id.etSets);
        btnAdd = findViewById(R.id.btnAddExercise);
        btnSave = findViewById(R.id.btnSavePlan);
        listContainer = findViewById(R.id.listContainer);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnAdd.setOnClickListener(v -> addExercise());
        btnSave.setOnClickListener(v -> saveWorkout());
    }

    private void addExercise() {
        String name = etExercise.getText().toString().trim();
        String sets = etSets.getText().toString().trim();

        if (name.isEmpty() || sets.isEmpty()) {
            Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("sets", Integer.parseInt(sets));
        exerciseList.add(map);

        TextView tv = new TextView(this);
        tv.setText(name + "  |  Sets: " + sets);
        tv.setTextSize(16f);
        listContainer.addView(tv);

        etExercise.setText("");
        etSets.setText("");
    }

    private void saveWorkout() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());

        Map<String, Object> data = new HashMap<>();
        data.put("exercises", exerciseList);

        db.collection("users")
                .document(uid)
                .collection("workouts")
                .document(date)
                .set(data)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Workout saved ✅", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
