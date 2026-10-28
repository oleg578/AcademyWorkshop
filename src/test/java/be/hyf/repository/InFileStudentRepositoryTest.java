package be.hyf.repository;

import be.hyf.entity.Student;
import be.hyf.exception.StudentNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InFileStudentRepositoryTest {
    private static final String STORAGE_PATH = "./storage/students/";
    // dedicated suffix: tests never read or delete real student files in the storage directory
    private static final String TEST_SUFFIX = ".test";

    private InFileStudentRepository repository;

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(Path.of(STORAGE_PATH));
        removeTestFiles();
        repository = new InFileStudentRepository(STORAGE_PATH, TEST_SUFFIX);
    }

    @AfterEach
    void tearDown() throws IOException {
        removeTestFiles();
    }

    private void removeTestFiles() throws IOException {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(Path.of(STORAGE_PATH), "*" + TEST_SUFFIX)) {
            for (Path file : files) {
                Files.delete(file);
            }
        }
    }

    private Student alice() {
        return new Student(1, "Alice", "alice@example.com", "Brussels");
    }

    @Test
    void shouldBuildFileNameFromIdNameAndSuffixWhenStudentGiven() {
        assertEquals("1_Alice.test", repository.buildFileName(alice()));
    }

    @Test
    void shouldUseDefaultSuffixWhenCreatedWithoutSuffix() {
        InFileStudentRepository defaultRepository = new InFileStudentRepository(STORAGE_PATH);

        assertEquals("1_Alice.data", defaultRepository.buildFileName(alice()));
    }

    @Test
    void shouldTrimSuffixWhenSuffixHasSurroundingSpaces() {
        InFileStudentRepository trimmed = new InFileStudentRepository(STORAGE_PATH, "  .test  ");

        assertEquals("1_Alice.test", trimmed.buildFileName(alice()));
    }

    @Test
    void shouldCreateFileInStorageWhenStudentSaved() throws IOException {
        repository.save(alice());

        assertTrue(Files.isRegularFile(Path.of(STORAGE_PATH, "1_Alice.test")));
    }

    @Test
    void shouldReturnSameStudentWhenSavedAndGotById() throws IOException {
        repository.save(alice());

        Student found = repository.getById(1);

        assertEquals(1, found.getId());
        assertEquals("Alice", found.getName());
        assertEquals("alice@example.com", found.getEmail());
        assertEquals("Brussels", found.getCity());
    }

    @Test
    void shouldReturnSameStudentWhenSavedAndGotByName() throws IOException {
        repository.save(alice());

        Student found = repository.getByName("Alice");

        assertEquals(1, found.getId());
        assertEquals("alice@example.com", found.getEmail());
    }

    @Test
    void shouldReturnMatchingStudentWhenSeveralStudentsSaved() throws IOException {
        repository.save(alice());
        repository.save(new Student(2, "Bob", "bob@example.com", "Ghent"));

        assertEquals("Bob", repository.getById(2).getName());
        assertEquals(1, repository.getByName("Alice").getId());
    }

    @Test
    void shouldNotMatchLongerIdWhenGetByIdCalledWithPrefixId() throws IOException {
        repository.save(new Student(11, "Carl", "carl@example.com", "Namur"));

        assertThrows(StudentNotFoundException.class, () -> repository.getById(1));
    }

    @Test
    void shouldPersistNewCityWhenStudentUpdated() throws IOException {
        Student student = alice();
        repository.save(student);

        student.setCity("Ghent");
        repository.update(student);

        assertEquals("Ghent", repository.getById(1).getCity());
    }

    @Test
    void shouldRemoveFileWhenStudentDeleted() throws IOException {
        Student student = alice();
        repository.save(student);

        repository.delete(student);

        assertFalse(Files.exists(Path.of(STORAGE_PATH, "1_Alice.test")));
        assertThrows(StudentNotFoundException.class, () -> repository.getById(1));
    }

    @Test
    void shouldThrowNotFoundWhenGetByIdUnknownId() {
        assertThrows(StudentNotFoundException.class, () -> repository.getById(404));
    }

    @Test
    void shouldThrowNotFoundWhenGetByNameUnknownName() {
        assertThrows(StudentNotFoundException.class, () -> repository.getByName("Nobody"));
    }

    @Test
    void shouldThrowIoErrorWhenDeletingStudentThatWasNeverSaved() {
        assertThrows(NoSuchFileException.class, () -> repository.delete(alice()));
    }

    @Test
    void shouldThrowIoErrorWhenSavingIntoMissingDirectory() {
        InFileStudentRepository broken = new InFileStudentRepository("./storage/missing-dir", TEST_SUFFIX);

        assertThrows(IOException.class, () -> broken.save(alice()));
    }

    @Test
    void shouldThrowIoErrorWhenReadingFromMissingDirectory() {
        InFileStudentRepository broken = new InFileStudentRepository("./storage/missing-dir", TEST_SUFFIX);

        assertThrows(IOException.class, () -> broken.getById(1));
    }

    @Test
    void shouldReturnAllStudentsWhenSeveralStudentsSaved() throws IOException {
        repository.save(alice());
        repository.save(new Student(2, "Bob", "bob@example.com", "Ghent"));
        repository.save(new Student(3, "Carl", "carl@example.com", "Namur"));

        List<Student> students = repository.getAll();

        assertEquals(3, students.size());
        assertEquals(Set.of("Alice", "Bob", "Carl"),
                students.stream().map(Student::getName).collect(Collectors.toSet()));
    }

    @Test
    void shouldReturnSingleStudentWhenOnlyOneSaved() throws IOException {
        repository.save(alice());

        List<Student> students = repository.getAll();

        assertEquals(1, students.size());
        assertEquals("alice@example.com", students.get(0).getEmail());
    }

    @Test
    void shouldReturnUpdatedDataWhenStudentUpdatedBeforeGetAll() throws IOException {
        Student student = alice();
        repository.save(student);
        student.setCity("Ghent");
        repository.update(student);

        assertEquals("Ghent", repository.getAll().get(0).getCity());
    }

    @Test
    void shouldIgnoreFilesWithOtherSuffixWhenGetAllCalled() throws IOException {
        repository.save(alice());
        Path foreignFile = Path.of(STORAGE_PATH, "9_Foreign.other");
        Files.writeString(foreignFile, "not a student");
        try {
            assertEquals(1, repository.getAll().size());
        } finally {
            Files.delete(foreignFile);
        }
    }

    @Test
    void shouldThrowNotFoundWhenGetAllOnEmptyStorage() {
        assertThrows(StudentNotFoundException.class, () -> repository.getAll());
    }

    @Test
    void shouldThrowNotFoundWhenGetAllFindsCorruptedFile() throws IOException {
        Files.writeString(Path.of(STORAGE_PATH, "5_Broken.test"), "not a serialized object");

        assertThrows(StudentNotFoundException.class, () -> repository.getAll());
    }

    @Test
    void shouldThrowIoErrorWhenGetAllReadsFromMissingDirectory() {
        InFileStudentRepository broken = new InFileStudentRepository("./storage/missing-dir", TEST_SUFFIX);

        assertThrows(IOException.class, broken::getAll);
    }
}
