package be.hyf.repository;

import be.hyf.entity.Student;
import be.hyf.exception.StudentNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public interface StudentRepository {
    /**
     * @param student instance, can't be null
     * @throws IllegalArgumentException if an student with the same name already exists
     * @throws IOException              on save error
     *
     */
    void save(@NotNull Student student) throws IOException;

    /**
     *
     * @param student instance, can't be null
     * @throws IOException              on save error
     */
    void update(@NotNull Student student) throws IOException;

    /**
     * @param student instance, can't be null
     * @throws IllegalArgumentException if an student with the same name already exists
     * @throws IOException              on save error
     *
     */
    void delete(@NotNull Student student) throws IOException;


    /**
     *
     * @param id integer value - ID of a student
     * @return student instance
     * @throws StudentNotFoundException  on not found
     */
    Student getById(int id) throws StudentNotFoundException, IOException;

    /**
     *
     * @param name String value - name of a student
     * @return student instance
     * @throws StudentNotFoundException  on not found
     */
    Student getByName(String name) throws StudentNotFoundException, IOException;

    /**
     *
     * @return List of students
     * @throws IOException
     */
    List<Student> getAll() throws IOException;
}
