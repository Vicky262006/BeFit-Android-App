package com.example.befit;

public class ExerciseModel {

    private String name;
    private int sets;

    public ExerciseModel() {}

    public ExerciseModel(String name, int sets) {
        this.name = name;
        this.sets = sets;
    }

    public String getName() {
        return name;
    }

    public int getSets() {
        return sets;
    }
}
