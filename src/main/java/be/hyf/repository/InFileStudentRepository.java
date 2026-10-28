package be.hyf.repository;

import be.hyf.entity.Student;
import be.hyf.exception.StudentNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InFileStudentRepository implements StudentRepository {
    private final String path;
    private final String fileNameSuffix;

    public InFileStudentRepository(String path) {
        this.path = path;
        fileNameSuffix = ".data";
    }

    public InFileStudentRepository(String path, String fileSuffix) {
        this.path = path;
        fileNameSuffix = fileSuffix.trim();
    }

    @Override
    public void save(@NotNull Student student) throws IOException, IllegalArgumentException {
        // build file name
        String fn = buildFileName(student);
        String targetPath = path + File.separator + fn;

        if (fileExistsById(student.getId())) throw new IllegalArgumentException("student exists");
        // serialize to file
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(targetPath))) {
            oos.writeObject(student);
        }
    }

    private boolean fileExistsById(int id) throws IllegalArgumentException, IOException {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + id + "*.data");
        List<Path> targets;
        try (Stream<Path> stream = Files.walk(Path.of(path))) {
            targets = stream
                    .filter(matcher::matches)
                    .toList();
        }
        return !targets.isEmpty();
    }

    @Override
    public void update(@NotNull Student student) throws IOException {
        // build file name
        String fn = buildFileName(student);
        String targetPath = path + File.separator + fn;
        // serialize to file
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(targetPath))) {
            oos.writeObject(student);
        }
    }

    @Override
    public void delete(@NotNull Student student) throws IOException {
        // build file name
        String fn = buildFileName(student);
        String targetPath = path + File.separator + fn;
        //remove given file
        Files.delete(Path.of(targetPath));
    }

    @Override
    public Student getById(int id) throws IOException {
        // get list of all files in path
        Path startPath = Paths.get(path);

        // globbing
        String globPattern = "glob:" + id + "_*" + fileNameSuffix;
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher(globPattern);
        List<Path> targets;
        try (Stream<Path> stream = Files.walk(startPath)) {
            targets = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> matcher.matches(path.getFileName()))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        if (targets.isEmpty()) throw new StudentNotFoundException("student not found");
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(targets.getFirst().toString()))) {
            return (Student) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new StudentNotFoundException("student not found");
        }
    }

    @Override
    public Student getByName(String name) throws IOException {
        // get list of all files in path
        Path startPath = Paths.get(path);

        // globbing
        String globPattern = "glob:*_" + name + fileNameSuffix;
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher(globPattern);
        List<Path> targets;
        try (Stream<Path> stream = Files.walk(startPath)) {
            targets = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> matcher.matches(path.getFileName()))
                    .toList();
            if (targets.isEmpty()) throw new StudentNotFoundException("student not found");
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(targets.getFirst().toString()))) {
            return (Student) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new StudentNotFoundException("student not found");
        }
    }

    @Override
    public List<Student> getAll() throws IOException {
        // get list of all files in path
        Path startPath = Paths.get(path);

        // globbing
        String globPattern = "glob:*" + fileNameSuffix;
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher(globPattern);
        List<Path> targets;
        try (Stream<Path> stream = Files.walk(startPath)) {
            targets = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> matcher.matches(path.getFileName()))
                    .toList();
            if (targets.isEmpty()) throw new StudentNotFoundException("students not found");
        }
        return targets.stream().map((path) -> deserialize(path.toString())).collect(Collectors.toCollection(ArrayList::new));
    }

    Student deserialize(String path) throws StudentNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            return (Student) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new StudentNotFoundException("students not found");
        }
    }

    String buildFileName(Student student) {
        return student.getId() + "_" + student.getName() + this.fileNameSuffix;
    }
}
