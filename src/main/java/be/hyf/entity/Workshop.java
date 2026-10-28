package be.hyf.entity;

import java.util.ArrayList;
import java.util.List;

public abstract class Workshop<T> {
    private final int id;
    private final String title;
    private int capacity;
    private String instructions;
    private List<T> participants;

    public Workshop(int id, String title, int capacity, List<T> participants, String instructions) {
        this.id = id;
        this.title = title;
        this.capacity = capacity;
        this.participants = new ArrayList<>(participants);
        this.instructions = instructions;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<T> getParticipants() {
        return List.copyOf(participants);
    }

    public void setParticipants(List<T> participants) {
        this.participants = new ArrayList<>(participants);
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}