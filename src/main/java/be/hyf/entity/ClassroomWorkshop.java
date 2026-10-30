package be.hyf.entity;

import java.util.List;

public class ClassroomWorkshop extends Workshop {

    public ClassroomWorkshop(
            int id,
            String title,
            int capacity,
            List<Student> participants,
            String instructions) {
        super(id, title, capacity, participants, instructions);
    }
}
