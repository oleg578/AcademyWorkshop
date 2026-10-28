package be.hyf.service;

import be.hyf.entity.Student;
import be.hyf.exception.StudentNotFoundException;
import be.hyf.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentServiceTest {
    private FakeStudentRepository repository;
    private StudentService service;

    @BeforeEach
    void setUp() {
        repository = new FakeStudentRepository();
        service = new StudentService(repository);
    }

    @Test
    void shouldDelegateToRepositoryWhenSaving() throws IOException {
        Student student = new Student(1, "Paul", "paul@hyf.be", "Brussels");

        service.save(student);

        assertEquals(List.of(student), repository.students);
    }

    @Test
    void shouldPropagateIllegalArgumentExceptionWhenSavingDuplicate() throws IOException {
        service.save(new Student(1, "Paul", "paul@hyf.be", "Brussels"));

        assertThrows(IllegalArgumentException.class,
                () -> service.save(new Student(2, "Paul", "other@hyf.be", "Ghent")));
    }

    @Test
    void shouldPropagateIOExceptionWhenSaveFails() {
        repository.ioFailure = new IOException("disk full");

        IOException thrown = assertThrows(IOException.class,
                () -> service.save(new Student(1, "Paul", "paul@hyf.be", "Brussels")));

        assertEquals("disk full", thrown.getMessage());
    }

    @Test
    void shouldDelegateToRepositoryWhenUpdating() throws IOException {
        Student student = new Student(1, "Paul", "paul@hyf.be", "Brussels");
        service.save(student);
        student.setCity("Antwerp");

        service.update(student);

        assertSame(student, repository.lastUpdated);
        assertEquals("Antwerp", repository.getById(1).getCity());
    }

    @Test
    void shouldPropagateIOExceptionWhenUpdateFails() {
        repository.ioFailure = new IOException("write error");

        assertThrows(IOException.class,
                () -> service.update(new Student(1, "Paul", "paul@hyf.be", "Brussels")));
    }

    @Test
    void shouldDelegateToRepositoryWhenDeleting() throws IOException {
        Student student = new Student(1, "Paul", "paul@hyf.be", "Brussels");
        service.save(student);

        service.delete(student);

        assertTrue(repository.students.isEmpty());
    }

    @Test
    void shouldPropagateIOExceptionWhenDeleteFails() {
        repository.ioFailure = new IOException("delete error");

        assertThrows(IOException.class,
                () -> service.delete(new Student(1, "Paul", "paul@hyf.be", "Brussels")));
    }

    @Test
    void shouldReturnStudentWhenIdExists() throws IOException {
        Student student = new Student(2, "Gayatri", "gayatri@hyf.be", "Ghent");
        service.save(student);

        assertSame(student, service.getById(2));
    }

    @Test
    void shouldThrowStudentNotFoundWhenIdDoesNotExist() {
        assertThrows(StudentNotFoundException.class, () -> service.getById(42));
    }

    @Test
    void shouldReturnStudentWhenNameExists() throws IOException {
        Student student = new Student(3, "Oleh", "oleh@hyf.be", "Liège");
        service.save(student);

        assertSame(student, service.getByName("Oleh"));
    }

    @Test
    void shouldThrowStudentNotFoundWhenNameDoesNotExist() {
        assertThrows(StudentNotFoundException.class, () -> service.getByName("Nobody"));
    }

    @Test
    void shouldReturnAllStudentsWhenRepositoryIsPopulated() throws IOException {
        Student first = new Student(1, "Paul", "paul@hyf.be", "Brussels");
        Student second = new Student(2, "Gayatri", "gayatri@hyf.be", "Ghent");
        service.save(first);
        service.save(second);

        assertEquals(List.of(first, second), service.getAll());
    }

    @Test
    void shouldReturnEmptyListWhenRepositoryIsEmpty() throws IOException {
        assertTrue(service.getAll().isEmpty());
    }

    @Test
    void shouldPropagateIOExceptionWhenGetAllFails() {
        repository.ioFailure = new IOException("read error");

        assertThrows(IOException.class, () -> service.getAll());
    }

    private static class FakeStudentRepository implements StudentRepository {
        private final List<Student> students = new ArrayList<>();
        private IOException ioFailure;
        private Student lastUpdated;

        private void failIfConfigured() throws IOException {
            if (ioFailure != null) {
                throw ioFailure;
            }
        }

        @Override
        public void save(Student student) throws IOException {
            failIfConfigured();
            boolean isDuplicate = students.stream().anyMatch(s -> s.getName().equals(student.getName()));
            if (isDuplicate) {
                throw new IllegalArgumentException("Student already exists: " + student.getName());
            }
            students.add(student);
        }

        @Override
        public void update(Student student) throws IOException {
            failIfConfigured();
            lastUpdated = student;
        }

        @Override
        public void delete(Student student) throws IOException {
            failIfConfigured();
            students.remove(student);
        }

        @Override
        public Student getById(int id) throws StudentNotFoundException, IOException {
            failIfConfigured();
            return students.stream()
                    .filter(s -> s.getId() == id)
                    .findFirst()
                    .orElseThrow(() -> new StudentNotFoundException("id " + id));
        }

        @Override
        public Student getByName(String name) throws StudentNotFoundException, IOException {
            failIfConfigured();
            return students.stream()
                    .filter(s -> s.getName().equals(name))
                    .findFirst()
                    .orElseThrow(() -> new StudentNotFoundException("name " + name));
        }

        @Override
        public List<Student> getAll() throws IOException {
            failIfConfigured();
            return List.copyOf(students);
        }
    }
}
