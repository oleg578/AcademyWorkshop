package be.hyf.index;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FSStudentWorkshopIndexTest {

    @TempDir
    Path storage;

    private Path indexFile;
    private FSStudentWorkshopIndex index;

    @BeforeEach
    void setUp() {
        indexFile = storage.resolve("students.idx");
        index = new FSStudentWorkshopIndex(indexFile.toString());
    }

    private FSStudentWorkshopIndex reload() throws IOException, ClassNotFoundException {
        FSStudentWorkshopIndex reloaded = new FSStudentWorkshopIndex(indexFile.toString());
        reloaded.load(indexFile.toString());
        return reloaded;
    }

    @Test
    void shouldBeEmptyWhenCreated() {
        assertTrue(index.getIndex().isEmpty());
    }

    @Test
    void shouldReturnNullWhenStudentIsUnknown() {
        assertNull(index.get(42));
    }

    @Test
    void shouldStoreWorkshopIdsInInsertionOrderWhenStudentHasSeveralWorkshops() throws IOException {
        index.add(1, 10);
        index.add(1, 30);
        index.add(1, 20);

        assertEquals(List.of(10, 30, 20), index.get(1));
    }

    @Test
    void shouldKeepStudentsSeparateWhenAddingForDifferentStudents() throws IOException {
        index.add(1, 10);
        index.add(2, 20);

        assertEquals(List.of(10), index.get(1));
        assertEquals(List.of(20), index.get(2));
    }

    @Test
    void shouldPersistToDiskWhenAdding() throws IOException, ClassNotFoundException {
        index.add(1, 10);
        index.add(2, 20);

        FSStudentWorkshopIndex reloaded = reload();

        assertEquals(List.of(10), reloaded.get(1));
        assertEquals(List.of(20), reloaded.get(2));
    }

    @Test
    void shouldRemoveStudentAndPersistWhenRemoving() throws IOException, ClassNotFoundException {
        index.add(1, 10);
        index.add(2, 20);

        index.remove(1);

        assertNull(index.get(1));
        FSStudentWorkshopIndex reloaded = reload();
        assertNull(reloaded.get(1));
        assertEquals(List.of(20), reloaded.get(2));
    }

    @Test
    void shouldNotFailWhenRemovingUnknownStudent() throws IOException {
        index.add(1, 10);

        index.remove(99);

        assertEquals(List.of(10), index.get(1));
    }

    @Test
    void shouldClearAndPersistWhenRemovingAll() throws IOException, ClassNotFoundException {
        index.add(1, 10);
        index.add(2, 20);

        index.removeAll();

        assertTrue(index.getIndex().isEmpty());
        assertTrue(reload().getIndex().isEmpty());
    }

    @Test
    void shouldReplaceCurrentContentWhenLoading() throws IOException, ClassNotFoundException {
        index.add(1, 10);
        FSStudentWorkshopIndex other = new FSStudentWorkshopIndex(indexFile.toString());
        other.add(2, 20);

        index.load(indexFile.toString());

        assertNull(index.get(1));
        assertEquals(List.of(20), index.get(2));
    }

    @Test
    void shouldThrowWhenLoadingMissingFile() {
        String missing = storage.resolve("missing.idx").toString();

        assertThrows(IOException.class, () -> index.load(missing));
    }

    @Test
    void shouldThrowWhenSavingToMissingDirectory() {
        String unreachable = storage.resolve("no-such-dir").resolve("x.idx").toString();

        assertThrows(IOException.class, () -> index.save(unreachable));
    }
}
