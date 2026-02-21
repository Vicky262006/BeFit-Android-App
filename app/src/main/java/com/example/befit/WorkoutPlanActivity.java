package com.example.befit;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutPlanActivity extends AppCompatActivity {

    Spinner daySpinner;
    EditText etExercise, etSets;
    Button btnAdd, btnSave;
    LinearLayout listContainer;

    FirebaseFirestore db;
    String uid;

    // store exercises for the selected day
    List<Map<String, Object>> exerciseList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_plan);

        daySpinner = findViewById(R.id.daySpinner);
        etExercise = findViewById(R.id.etExercise);
        etSets = findViewById(R.id.etSets);
        btnAdd = findViewById(R.id.btnAddExercise);
        btnSave = findViewById(R.id.btnSavePlan);
        listContainer = findViewById(R.id.listContainer);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        setupDaySpinner();

        btnAdd.setOnClickListener(v -> addExercise());
        btnSave.setOnClickListener(v -> saveDayPlan());
    }

    private void setupDaySpinner() {
        String[] days = {
                "Monday", "Tuesday", "Wednesday",
                "Thursday", "Friday", "Saturday", "Sunday"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                days
        );
        daySpinner.setAdapter(adapter);
    }

    private void addExercise() {
        String exercise = etExercise.getText().toString().trim();
        String setsText = etSets.getText().toString().trim();

        if (exercise.isEmpty() || setsText.isEmpty()) {
            Toast.makeText(this, "Enter exercise and sets", Toast.LENGTH_SHORT).show();
            return;
        }

        int sets = Integer.parseInt(setsText);

        // store in list
        Map<String, Object> data = new HashMap<>();
        data.put("exercise", exercise);
        data.put("sets", sets);

        exerciseList.add(data);

        // show on UI
        TextView tv = new TextView(this);
        tv.setText(exercise + "\nSets: " + sets);
        tv.setTextSize(16f);
        tv.setPadding(0, 16, 0, 16);

        listContainer.addView(tv);

        etExercise.setText("");
        etSets.setText("");
    }

    private void saveDayPlan() {
        if (exerciseList.isEmpty()) {
            Toast.makeText(this, "Add at least one exercise", Toast.LENGTH_SHORT).show();
            return;
        }

        String day = daySpinner.getSelectedItem().toString();

        Map<String, Object> plan = new HashMap<>();
        plan.put("exercises", exerciseList);

        db.collection("users")
                .document(uid)
                .collection("plans")
                .document(day)
                .set(plan)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Workout plan saved ✅", Toast.LENGTH_SHORT).show();
                    exerciseList.clear();
                    listContainer.removeAllViews();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
