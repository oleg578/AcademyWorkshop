package be.hyf.entity;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkshopTest {

    private Student alice() {
        return new Student(1, "Alice", "alice@example.com", "Brussels");
    }

    private Student bob() {
        return new Student(2, "Bob", "bob@example.com", "Ghent");
    }

    private Workshop workshop(int capacity, List<Student> participants) {
        return new OnlineWorkshop(10, "Java", capacity, participants, "Bring a laptop");
    }

    @Test
    void shouldExposeConstructorValuesWhenWorkshopCreated() {
        Workshop workshop = workshop(5, List.of(alice()));

        assertEquals(10, workshop.getId());
        assertEquals("Java", workshop.getTitle());
        assertEquals(5, workshop.getCapacity());
        assertEquals("Bring a laptop", workshop.getInstructions());
        assertEquals(1, workshop.getParticipants().size());
    }

    @Test
    void shouldReturnParticipantsOrderedByIdWhenAddedInReverseOrder() {
        Workshop workshop = workshop(5, List.of(bob(), alice()));

        assertEquals(List.of(1, 2), workshop.getParticipants().stream().map(Student::getId).toList());
    }

    @Test
    void shouldKeepSingleEntryWhenParticipantsContainDuplicateId() {
        Workshop workshop = workshop(5, List.of(alice(), new Student(1, "Alice2", "a2@example.com", "Mons")));

        assertEquals(1, workshop.getParticipants().size());
    }

    @Test
    void shouldAddParticipantWhenWorkshopHasFreePlace() {
        Workshop workshop = workshop(2, List.of());

        workshop.addParticipant(alice());

        assertEquals(1, workshop.getParticipants().size());
        assertEquals("Alice", workshop.getParticipants().getFirst().getName());
    }

    @Test
    void shouldThrowIllegalStateWhenAddingParticipantToFullWorkshop() {
        Workshop workshop = workshop(1, List.of(alice()));

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> workshop.addParticipant(bob()));

        assertEquals("Workshop is full", error.getMessage());
        assertEquals(1, workshop.getParticipants().size());
    }

    @Test
    void shouldThrowIllegalStateWhenCapacityIsZero() {
        Workshop workshop = workshop(0, List.of());

        assertThrows(IllegalStateException.class, () -> workshop.addParticipant(alice()));
    }

    @Test
    void shouldReplaceExistingEntryWhenAddingParticipantWithSameId() {
        Workshop workshop = workshop(2, List.of(alice()));

        workshop.addParticipant(new Student(1, "Alice", "new@example.com", "Mons"));

        assertEquals(1, workshop.getParticipants().size());
        assertEquals("new@example.com", workshop.getParticipants().getFirst().getEmail());
    }

    @Test
    void shouldReturnTrueWhenDeletingExistingParticipant() {
        Workshop workshop = workshop(2, List.of(alice(), bob()));

        assertTrue(workshop.deleteParticipant(1));
        assertEquals(List.of(2), workshop.getParticipants().stream().map(Student::getId).toList());
    }

    @Test
    void shouldReturnFalseWhenDeletingUnknownParticipant() {
        Workshop workshop = workshop(2, List.of(alice()));

        assertFalse(workshop.deleteParticipant(404));
        assertEquals(1, workshop.getParticipants().size());
    }

    @Test
    void shouldFreePlaceWhenParticipantDeleted() {
        Workshop workshop = workshop(1, List.of(alice()));
        workshop.deleteParticipant(1);

        workshop.addParticipant(bob());

        assertEquals(2, workshop.getParticipants().getFirst().getId());
    }

    @Test
    void shouldReplaceAllParticipantsWhenSetParticipantsCalled() {
        Workshop workshop = workshop(5, List.of(alice()));

        workshop.setParticipants(List.of(bob()));

        assertEquals(List.of(2), workshop.getParticipants().stream().map(Student::getId).toList());
    }

    @Test
    void shouldClearParticipantsWhenSetParticipantsCalledWithEmptyList() {
        Workshop workshop = workshop(5, List.of(alice()));

        workshop.setParticipants(List.of());

        assertTrue(workshop.getParticipants().isEmpty());
    }

    @Test
    void shouldThrowUnsupportedOperationWhenModifyingReturnedParticipantsList() {
        Workshop workshop = workshop(5, List.of(alice()));

        assertThrows(UnsupportedOperationException.class, () -> workshop.getParticipants().add(bob()));
    }

    @Test
    void shouldNotChangeWorkshopWhenSourceListModifiedAfterCreation() {
        java.util.ArrayList<Student> source = new java.util.ArrayList<>(List.of(alice()));
        Workshop workshop = workshop(5, source);

        source.add(bob());

        assertEquals(1, workshop.getParticipants().size());
    }

    @Test
    void shouldUpdateCapacityWhenSetCapacityCalled() {
        Workshop workshop = workshop(1, List.of(alice()));

        workshop.setCapacity(3);
        workshop.addParticipant(bob());

        assertEquals(3, workshop.getCapacity());
        assertEquals(2, workshop.getParticipants().size());
    }

    @Test
    void shouldUpdateInstructionsWhenSetInstructionsCalled() {
        Workshop workshop = workshop(1, List.of());

        workshop.setInstructions("Room 4");

        assertEquals("Room 4", workshop.getInstructions());
    }

    @Test
    void shouldRestoreStateAndConcreteTypeWhenSerializedAndDeserialized() throws IOException, ClassNotFoundException {
        Workshop original = new ClassroomWorkshop(7, "Spring", 4, List.of(bob(), alice()), "Room 2");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(original);
        }
        Workshop restored;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = (Workshop) in.readObject();
        }

        assertInstanceOf(ClassroomWorkshop.class, restored);
        assertEquals(7, restored.getId());
        assertEquals("Spring", restored.getTitle());
        assertEquals(4, restored.getCapacity());
        assertEquals("Room 2", restored.getInstructions());
        assertEquals(List.of(1, 2), restored.getParticipants().stream().map(Student::getId).toList());
    }
}
