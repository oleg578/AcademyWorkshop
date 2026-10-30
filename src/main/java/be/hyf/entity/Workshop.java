package be.hyf.entity;

import java.io.Serializable;
import java.util.List;
import java.util.TreeMap;

public abstract class Workshop implements Serializable {
    private final int id;
    private final String title;
    private int capacity;
    private String instructions;
    private TreeMap<Integer, Student> participants;

    public Workshop(int id, String title, int capacity, List<Student> participants, String instructions) {
        this.id = id;
        this.title = title;
        this.capacity = capacity;
        this.participants = new TreeMap<>();
        participants.forEach(p -> this.participants.put(p.getId(), p));
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

    public List<Student> getParticipants() {
        return participants.values().stream().toList();
    }

    public void addParticipant(Student student) {
        if (participants.size() >= capacity) {
            throw new IllegalStateException("Workshop is full");
        }
        participants.put(student.getId(), student);
    }

    public boolean deleteParticipant(int id) {
        return participants.remove(id) != null;
    }

    public void setParticipants(List<Student> participants) {
        this.participants = new TreeMap<>();
        participants.forEach(p -> this.participants.put(p.getId(), p));
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}