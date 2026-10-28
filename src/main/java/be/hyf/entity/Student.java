package be.hyf.entity;

import java.io.Serializable;

public class Student implements Serializable {
    private final int id;
    private final String name;
    private final String email;
    private String city;

    public Student(int id, String name, String email, String city) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.city = city;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
