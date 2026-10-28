package be.hyf.entity;

import java.util.List;

public class OnlineWorkshop extends Workshop<Student>{

    public OnlineWorkshop(
            int id,
            String title,
            int capacity,
            List<Student> participants,
            String instructions) {
        super(id, title, capacity, participants, instructions);
    }
}
