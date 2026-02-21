package com.example.befit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackerAdapter extends RecyclerView.Adapter<TrackerAdapter.Holder> {

    private ArrayList<ExerciseModel> list;
    private List<Map<String, Object>> previousData = new ArrayList<>();
    private RecyclerView recyclerView;

    public TrackerAdapter(ArrayList<ExerciseModel> list) {
        this.list = list;
    }

    public void attachRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    // 🔥 THIS WAS MISSING
    public void setPreviousData(List<Map<String, Object>> previousData) {
        this.previousData = previousData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tracker_row, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        ExerciseModel model = list.get(position);
        holder.tvName.setText(model.getName());
        holder.setContainer.removeAllViews();

        List<Map<String, Object>> prevSets = null;

        // 🔥 Match previous data by exercise name
        if (previousData != null) {
            for (Map<String, Object> map : previousData) {
                if (map.get("exercise").equals(model.getName())) {
                    prevSets = (List<Map<String, Object>>) map.get("sets");
                }
            }
        }

        for (int i = 0; i < model.getSets(); i++) {

            View row = LayoutInflater.from(holder.itemView.getContext())
                    .inflate(R.layout.item_set_row, holder.setContainer, false);

            EditText etWeight = row.findViewById(R.id.etWeight);
            EditText etReps = row.findViewById(R.id.etReps);

            // 🔥 Auto fill previous week values
            if (prevSets != null && i < prevSets.size()) {
                etWeight.setText(prevSets.get(i).get("weight").toString());
                etReps.setText(prevSets.get(i).get("reps").toString());
            }

            holder.setContainer.addView(row);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // 🔥 SAVE DATA PROPERLY
    public List<Map<String, Object>> getWorkoutData() {

        List<Map<String, Object>> saveList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {

            RecyclerView.ViewHolder viewHolder =
                    recyclerView.findViewHolderForAdapterPosition(i);

            if (viewHolder instanceof Holder) {

                Holder holder = (Holder) viewHolder;

                Map<String, Object> exerciseMap = new HashMap<>();
                exerciseMap.put("exercise", list.get(i).getName());

                List<Map<String, Object>> setList = new ArrayList<>();

                for (int j = 0; j < holder.setContainer.getChildCount(); j++) {

                    View row = holder.setContainer.getChildAt(j);

                    EditText etWeight = row.findViewById(R.id.etWeight);
                    EditText etReps = row.findViewById(R.id.etReps);

                    Map<String, Object> setMap = new HashMap<>();
                    setMap.put("weight", etWeight.getText().toString());
                    setMap.put("reps", etReps.getText().toString());

                    setList.add(setMap);
                }

                exerciseMap.put("sets", setList);
                saveList.add(exerciseMap);
            }
        }

        return saveList;
    }

    static class Holder extends RecyclerView.ViewHolder {

        TextView tvName;
        LinearLayout setContainer;

        public Holder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvExerciseName);
            setContainer = itemView.findViewById(R.id.setContainer);
        }
    }
}
