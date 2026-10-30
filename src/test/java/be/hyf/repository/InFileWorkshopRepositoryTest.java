package be.hyf.repository;

import be.hyf.entity.ClassroomWorkshop;
import be.hyf.entity.OnlineWorkshop;
import be.hyf.entity.Student;
import be.hyf.entity.Workshop;
import be.hyf.exception.WorkshopNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the {@link WorkshopRepository} contract through its only implementation.
 */
class InFileWorkshopRepositoryTest {
    private static final String TEST_SUFFIX = ".test";

    @TempDir
    Path storage;

    private InFileWorkshopRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InFileWorkshopRepository(storage.toString(), TEST_SUFFIX);
    }

    private Student alice() {
        return new Student(1, "Alice", "alice@example.com", "Brussels");
    }

    private Student bob() {
        return new Student(2, "Bob", "bob@example.com", "Ghent");
    }

    private Workshop java(int capacity, List<Student> participants) {
        return new OnlineWorkshop(1, "Java", capacity, participants, "Bring a laptop");
    }

    // ---- file naming / construction

    @Test
    void shouldBuildFileNameFromIdTitleAndSuffixWhenWorkshopGiven() {
        assertEquals("1_Java.test", repository.buildFileName(java(3, List.of())));
    }

    @Test
    void shouldUseDefaultSuffixWhenCreatedWithoutSuffix() {
        InFileWorkshopRepository defaultRepository = new InFileWorkshopRepository(storage.toString());

        assertEquals("1_Java.ser", defaultRepository.buildFileName(java(3, List.of())));
    }

    @Test
    void shouldTrimSuffixWhenSuffixHasSurroundingSpaces() {
        InFileWorkshopRepository trimmed = new InFileWorkshopRepository(storage.toString(), "  .test  ");

        assertEquals("1_Java.test", trimmed.buildFileName(java(3, List.of())));
    }

    // ---- save

    @Test
    void shouldCreateFileWhenWorkshopSaved() throws IOException {
        repository.save(java(3, List.of()));

        assertTrue(Files.isRegularFile(storage.resolve("1_Java.test")));
    }

    @Test
    void shouldThrowIllegalArgumentWhenSavingWorkshopWhoseIdAlreadyExists() throws IOException {
        repository.save(java(3, List.of()));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> repository.save(new ClassroomWorkshop(1, "Other title", 3, List.of(), "x")));

        assertEquals("workshop exists", error.getMessage());
    }

    @Test
    void shouldKeepOriginalDataWhenSavingDuplicateWorkshop() throws IOException {
        repository.save(java(3, List.of()));

        assertThrows(IllegalArgumentException.class,
                () -> repository.save(new OnlineWorkshop(1, "Java", 99, List.of(), "changed")));

        assertEquals(3, repository.getById(1).getCapacity());
    }

    @Test
    void shouldSaveAgainWhenPreviousFileDeleted() throws IOException {
        Workshop workshop = java(3, List.of());
        repository.save(workshop);
        repository.delete(workshop);

        repository.save(workshop);

        assertTrue(Files.isRegularFile(storage.resolve("1_Java.test")));
    }

    @Test
    void shouldThrowIoErrorWhenSavingIntoMissingDirectory() {
        InFileWorkshopRepository broken = new InFileWorkshopRepository(storage.resolve("missing").toString(), TEST_SUFFIX);

        assertThrows(IOException.class, () -> broken.save(java(3, List.of())));
    }

    // ---- getById / getByTitle

    @Test
    void shouldReturnSameWorkshopWhenSavedAndGotById() throws IOException {
        repository.save(java(3, List.of(alice())));

        Workshop found = repository.getById(1);

        assertEquals("Java", found.getTitle());
        assertEquals(3, found.getCapacity());
        assertEquals("Bring a laptop", found.getInstructions());
        assertEquals(List.of(1), found.getParticipants().stream().map(Student::getId).toList());
    }

    @Test
    void shouldKeepConcreteTypeWhenWorkshopRoundTripped() throws IOException {
        repository.save(new ClassroomWorkshop(2, "Spring", 5, List.of(), "Room 2"));
        repository.save(java(3, List.of()));

        assertInstanceOf(ClassroomWorkshop.class, repository.getById(2));
        assertInstanceOf(OnlineWorkshop.class, repository.getById(1));
    }

    @Test
    void shouldReturnSameWorkshopWhenSavedAndGotByTitle() throws IOException {
        repository.save(java(3, List.of()));

        assertEquals(1, repository.getByTitle("Java").getId());
    }

    @Test
    void shouldReturnMatchingWorkshopWhenSeveralSaved() throws IOException {
        repository.save(java(3, List.of()));
        repository.save(new ClassroomWorkshop(2, "Spring", 5, List.of(), "Room 2"));

        assertEquals("Spring", repository.getById(2).getTitle());
        assertEquals(1, repository.getByTitle("Java").getId());
    }

    @Test
    void shouldNotMatchLongerIdWhenGetByIdCalledWithPrefixId() throws IOException {
        repository.save(new OnlineWorkshop(11, "Docker", 3, List.of(), "x"));

        assertThrows(WorkshopNotFoundException.class, () -> repository.getById(1));
    }

    @Test
    void shouldThrowNotFoundWhenGetByIdUnknownId() {
        assertThrows(WorkshopNotFoundException.class, () -> repository.getById(404));
    }

    @Test
    void shouldThrowNotFoundWhenGetByTitleUnknownTitle() {
        assertThrows(WorkshopNotFoundException.class, () -> repository.getByTitle("Nothing"));
    }

    @Test
    void shouldThrowIoErrorWhenReadingFromMissingDirectory() {
        InFileWorkshopRepository broken = new InFileWorkshopRepository(storage.resolve("missing").toString(), TEST_SUFFIX);

        assertThrows(IOException.class, () -> broken.getById(1));
        assertThrows(IOException.class, () -> broken.getByTitle("Java"));
        assertThrows(IOException.class, broken::getAll);
    }

    // ---- update / delete

    @Test
    void shouldPersistNewStateWhenWorkshopUpdated() throws IOException {
        Workshop workshop = java(3, List.of());
        repository.save(workshop);

        workshop.setCapacity(10);
        workshop.setInstructions("Room 5");
        workshop.addParticipant(alice());
        repository.update(workshop);

        Workshop stored = repository.getById(1);
        assertEquals(10, stored.getCapacity());
        assertEquals("Room 5", stored.getInstructions());
        assertEquals(1, stored.getParticipants().size());
    }

    @Test
    void shouldRemoveFileWhenWorkshopDeleted() throws IOException {
        Workshop workshop = java(3, List.of());
        repository.save(workshop);

        repository.delete(workshop);

        assertFalse(Files.exists(storage.resolve("1_Java.test")));
        assertThrows(WorkshopNotFoundException.class, () -> repository.getById(1));
    }

    @Test
    void shouldThrowIoErrorWhenDeletingWorkshopThatWasNeverSaved() {
        assertThrows(NoSuchFileException.class, () -> repository.delete(java(3, List.of())));
    }

    // ---- getAll

    @Test
    void shouldReturnAllWorkshopsWhenSeveralSaved() throws IOException {
        repository.save(java(3, List.of()));
        repository.save(new ClassroomWorkshop(2, "Spring", 5, List.of(), "Room 2"));
        repository.save(new OnlineWorkshop(3, "Docker", 5, List.of(), "x"));

        List<Workshop> workshops = repository.getAll();

        assertEquals(Set.of("Java", "Spring", "Docker"),
                workshops.stream().map(Workshop::getTitle).collect(Collectors.toSet()));
    }

    @Test
    void shouldIgnoreFilesWithOtherSuffixWhenGetAllCalled() throws IOException {
        repository.save(java(3, List.of()));
        Files.writeString(storage.resolve("9_Foreign.other"), "not a workshop");

        assertEquals(1, repository.getAll().size());
    }

    @Test
    void shouldThrowNotFoundWhenGetAllOnEmptyStorage() {
        assertThrows(WorkshopNotFoundException.class, () -> repository.getAll());
    }

    @Test
    void shouldThrowNotFoundWhenGetAllFindsCorruptedFile() throws IOException {
        Files.writeString(storage.resolve("5_Broken.test"), "not a serialized object");

        assertThrows(WorkshopNotFoundException.class, () -> repository.getAll());
    }

    // ---- participants / capacity helpers

    @Test
    void shouldReturnParticipantsOfWorkshop() {
        Workshop workshop = java(3, List.of(alice(), bob()));

        assertEquals(2, repository.getParticipants(workshop).size());
    }

    @Test
    void shouldReportNotFullWhenPlacesRemain() {
        assertFalse(repository.isFull(java(3, List.of(alice()))));
    }

    @Test
    void shouldReportFullWhenParticipantsReachCapacity() {
        assertTrue(repository.isFull(java(2, List.of(alice(), bob()))));
    }

    @Test
    void shouldReportFullWhenCapacityIsZero() {
        assertTrue(repository.isFull(java(0, List.of())));
    }

    @Test
    void shouldComputeAvailablePlacesWhenWorkshopPartiallyFilled() {
        assertEquals(2, repository.availablePlaces(java(3, List.of(alice()))));
    }

    @Test
    void shouldReturnZeroAvailablePlacesWhenWorkshopFull() {
        assertEquals(0, repository.availablePlaces(java(1, List.of(alice()))));
    }

    // ---- register / unregister / isRegistered

    @Test
    void shouldAddStudentWhenRegisteredToWorkshopWithFreePlace() {
        Workshop workshop = java(3, List.of());

        repository.register(workshop, alice());

        assertEquals(1, workshop.getParticipants().size());
        assertEquals(1, repository.availablePlaces(workshop) - 1);
    }

    @Test
    void shouldThrowIllegalStateWhenRegisteringToFullWorkshop() {
        Workshop workshop = java(1, List.of(alice()));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> repository.register(workshop, bob()));

        assertEquals("Workshop is full", error.getMessage());
        assertEquals(1, workshop.getParticipants().size());
    }

    @Test
    void shouldReturnTrueWhenUnregisteringRegisteredStudent() {
        Workshop workshop = java(3, List.of(alice()));

        assertTrue(repository.unregister(workshop, alice()));
        assertTrue(workshop.getParticipants().isEmpty());
    }

    @Test
    void shouldReturnFalseWhenUnregisteringStudentThatIsNotRegistered() {
        assertFalse(repository.unregister(java(3, List.of(alice())), bob()));
    }

    @Test
    void shouldReportRegisteredWhenStudentIsParticipant() {
        Student alice = alice();

        assertTrue(repository.isRegistered(java(3, List.of(alice)), alice));
    }

    @Test
    void shouldReportNotRegisteredWhenStudentIsNotParticipant() {
        assertFalse(repository.isRegistered(java(3, List.of(alice())), bob()));
    }

    @Test
    void shouldReportRegisteredWhenStudentWithSameIdComesFromDeserializedWorkshop() throws IOException {
        repository.save(java(3, List.of(alice())));

        assertTrue(repository.isRegistered(repository.getById(1), alice()));
    }
}
