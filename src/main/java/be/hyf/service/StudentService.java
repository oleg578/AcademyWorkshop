package be.hyf.service;

import be.hyf.entity.Student;
import be.hyf.exception.StudentNotFoundException;
import be.hyf.repository.StudentRepository;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public class StudentService {
    private final StudentRepository repository;

    public StudentService(@NotNull StudentRepository repository) {
        this.repository = repository;
    }

    public void save(@NotNull Student student) throws IOException {
        repository.save(student);
    }

    public void update(@NotNull Student student) throws IOException {
        repository.update(student);
    }

    public void delete(@NotNull Student student) throws IOException {
        repository.delete(student);
    }

    public Student getById(int id) throws StudentNotFoundException, IOException {
        return repository.getById(id);
    }

    public Student getByName(String name) throws StudentNotFoundException, IOException {
        return repository.getByName(name);
    }

    public List<Student> getAll() throws IOException {
        return repository.getAll();
    }
}
