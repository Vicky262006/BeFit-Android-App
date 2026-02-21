package com.example.befit;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ProgressActivity extends AppCompatActivity {

    LineChart lineChart;
    Spinner spinnerExercise;

    FirebaseFirestore db;
    String uid;

    ArrayList<String> exerciseNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        lineChart = findViewById(R.id.lineChart);
        spinnerExercise = findViewById(R.id.spinnerExercise);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        if (uid != null) {
            loadExercises();
        } else {
            // Handle user not being signed in
            Toast.makeText(this, "You need to be logged in to see progress.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadExercises() {

        db.collection("users")
                .document(uid)
                .collection("plans")
                .get()
                .addOnSuccessListener(planDocuments -> {

                    if (planDocuments.isEmpty()) {
                        Toast.makeText(this, "No workout plans found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Set<String> uniqueExerciseNames = new HashSet<>();
                    for (var planDoc : planDocuments.getDocuments()) {
                        Object exercisesObject = planDoc.get("exercises");
                        if (exercisesObject instanceof List) {
                            List<?> rawList = (List<?>) exercisesObject;
                            if (!rawList.isEmpty() && rawList.get(0) instanceof Map) {
                                @SuppressWarnings("unchecked")
                                List<Map<String, Object>> list = (List<Map<String, Object>>) rawList;

                                for (Map<String, Object> map : list) {
                                    Object exerciseNameObject = map.get("exercise");
                                    if (exerciseNameObject != null) {
                                        uniqueExerciseNames.add(exerciseNameObject.toString());
                                    }
                                }
                            }
                        }
                    }

                    exerciseNames.clear();
                    exerciseNames.addAll(uniqueExerciseNames);

                    if (exerciseNames.isEmpty()) {
                        Toast.makeText(this, "No exercises found in your plans.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ArrayAdapter<String> adapter =
                            new ArrayAdapter<>(this,
                                    android.R.layout.simple_spinner_dropdown_item,
                                    exerciseNames);

                    spinnerExercise.setAdapter(adapter);

                    spinnerExercise.setOnItemSelectedListener(
                            new android.widget.AdapterView.OnItemSelectedListener() {

                                @Override
                                public void onItemSelected(
                                        android.widget.AdapterView<?> parent,
                                        android.view.View view,
                                        int position,
                                        long id) {

                                    loadProgress(exerciseNames.get(position));
                                }

                                @Override
                                public void onNothingSelected(
                                        android.widget.AdapterView<?> parent) {
                                }
                            });
                    // Load progress for the first item initially
                    if (!exerciseNames.isEmpty()) {
                        loadProgress(exerciseNames.get(0));
                    }
                });
    }

    private void loadProgress(String exerciseName) {

        db.collection("users")
                .document(uid)
                .collection("workouts")
                .orderBy("date", Query.Direction.ASCENDING) // Order by date
                .get()
                .addOnSuccessListener(query -> {

                    ArrayList<Entry> entries = new ArrayList<>();

                    for (var doc : query.getDocuments()) {

                        Object dateObject = doc.get("date");
                        if (!(dateObject instanceof Timestamp)) {
                            continue; // Skip if no valid date
                        }
                        long timeInMillis = ((Timestamp) dateObject).toDate().getTime();

                        Object exercisesObject = doc.get("exercises");
                        if (exercisesObject instanceof List) {
                            List<?> rawList = (List<?>) exercisesObject;
                            if (!rawList.isEmpty() && rawList.get(0) instanceof Map) {
                                @SuppressWarnings("unchecked")
                                List<Map<String, Object>> exercises = (List<Map<String, Object>>) rawList;

                                for (Map<String, Object> ex : exercises) {
                                    Object exerciseObject = ex.get("exercise");

                                    if (exerciseObject != null && exerciseObject.toString().equals(exerciseName)) {

                                        Object setsObject = ex.get("sets");
                                        if (setsObject instanceof List) {
                                            List<?> rawSetsList = (List<?>) setsObject;
                                            if (!rawSetsList.isEmpty() && rawSetsList.get(0) instanceof Map) {
                                                @SuppressWarnings("unchecked")
                                                List<Map<String, Object>> sets = (List<Map<String, Object>>) rawSetsList;

                                                if (!sets.isEmpty()) {
                                                    Map<String, Object> firstSet = sets.get(0);
                                                    Object weightObject = firstSet.get("weight");
                                                    if (weightObject != null) {
                                                        try {
                                                            float weight = Float.parseFloat(weightObject.toString());
                                                            entries.add(new Entry(timeInMillis, weight));
                                                            // We found the exercise and weight for this date, break inner loop
                                                            break;
                                                        } catch (NumberFormatException e) {
                                                            // Weight is not a valid float, ignore
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    LineDataSet dataSet = new LineDataSet(entries, "Progress for " + exerciseName);

                    dataSet.setLineWidth(3f);
                    dataSet.setCircleRadius(5f);

                    LineData lineData = new LineData(dataSet);

                    XAxis xAxis = lineChart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setValueFormatter(new DateAxisValueFormatter());
                    xAxis.setLabelRotationAngle(-45); // Rotate labels to prevent overlap

                    lineChart.getDescription().setEnabled(false); // No description
                    lineChart.setData(lineData);
                    lineChart.invalidate(); // refresh
                });
    }

    // Inner class for formatting X-axis date values
    private static class DateAxisValueFormatter extends ValueFormatter {
        private final SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

        @Override
        public String getFormattedValue(float value) {
            return mFormat.format(new Date((long) value));
        }
    }
}
