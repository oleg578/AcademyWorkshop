package be.hyf;

import be.hyf.entity.Student;
import be.hyf.repository.InFileStudentRepository;
import be.hyf.repository.StudentRepository;
import be.hyf.service.StudentService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Academy Workshop Manager Application
 */
public class App {


    public static void main(String[] args) throws IOException {
        playWithStudents();
    }

    private static void playWithStudents() throws IOException {
        final Path CSV_PATH = Path.of("./dumps/students.csv");
        final String STORAGE_STUDENTS = "./storage/students/";
        Files.createDirectories(Path.of(STORAGE_STUDENTS));
        StudentRepository repository = new InFileStudentRepository(STORAGE_STUDENTS);

        StudentService service = new StudentService(repository);

        for (Student student : readStudents(CSV_PATH)) {
            service.save(student);
        }
        System.out.println("=======   LIST OF LOADED STUDENTS   =======");
        List<Student> studentsLoaded = service.getAll();
        studentsLoaded.forEach(App::print);

        System.out.println("======== GET by ID 2 ==========");
        print(service.getById(2));

        System.out.println("======== GET by name Oleh ==========");
        print(service.getByName("Oleh"));

        System.out.println("======== CHANGE city ==========");
        Student student = service.getById(3);
        print(student);
        student.setCity("Namur");
        service.update(student);
        print(service.getById(3));

        System.out.println("=======   LIST OF STUDENTS   =======");
        List<Student> students = service.getAll();
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
