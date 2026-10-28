package be.hyf;

import be.hyf.entity.Student;
import be.hyf.repository.InFileStudentRepository;
import be.hyf.repository.StudentRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Academy Workshop Manager Application
 */
public class App {
    private static final Path CSV_PATH = Path.of("./dumps/students.csv");
    private static final String STORAGE_PATH = "./storage/students/";

    public static void main(String[] args) throws IOException {
        Files.createDirectories(Path.of(STORAGE_PATH));
        StudentRepository repository = new InFileStudentRepository(STORAGE_PATH);

        for (Student student : readStudents(CSV_PATH)) {
            repository.save(student);
        }
        System.out.println("======== GET by ID 2 ==========");
        print(repository.getById(2));

        System.out.println("======== GET by name Oleh ==========");
        print(repository.getByName("Oleh"));

        System.out.println("======== CHANGE city ==========");
        Student student = repository.getById(3);
        print(student);
        student.setCity("Namur");
        repository.update(student);
        print(repository.getById(3));

        System.out.println("=======   LIST OF STUDENTS   =======");
        List<Student> students = repository.getAll();
        students.forEach(App::print);
    }

    private static List<Student> readStudents(Path csvPath) throws IOException {
        return Files.readAllLines(csvPath).stream()
                .filter(line -> !line.isBlank())
                .map(App::parseStudent)
                .toList();
    }

    private static Student parseStudent(String line) {
        String[] fields = line.split(",", -1);
        if (fields.length != 4) {
            throw new IllegalArgumentException("invalid CSV line: " + line);
        }
        return new Student(Integer.parseInt(fields[0].trim()), fields[1].trim(), fields[2].trim(), fields[3].trim());
    }

    private static void print(Student student) {
        System.out.printf("%4d | %20s | %32s | %s%n",
                student.getId(), student.getName(), student.getEmail(), student.getCity());
    }
}
