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

class FSTreeIndexTest {

    @TempDir
    Path storage;

    private FSTreeIndex index;

    @BeforeEach
    void setUp() {
        index = newIndex();
    }

    private FSTreeIndex newIndex() {
        return new FSTreeIndex(storage.toString(), "students");
    }

    private FSTreeIndex reload() throws IOException, ClassNotFoundException {
        FSTreeIndex reloaded = newIndex();
        reloaded.load();
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

        FSTreeIndex reloaded = reload();

        assertEquals(List.of(10), reloaded.get(1));
        assertEquals(List.of(20), reloaded.get(2));
    }

    @Test
    void shouldRemoveStudentAndPersistWhenRemoving() throws IOException, ClassNotFoundException {
        index.add(1, 10);
        index.add(2, 20);

        index.remove(1);

        assertNull(index.get(1));
        FSTreeIndex reloaded = reload();
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
        FSTreeIndex other = newIndex();
        other.add(2, 20);

        index.load();

        assertNull(index.get(1));
        assertEquals(List.of(20), index.get(2));
    }

    @Test
    void shouldThrowWhenLoadingMissingFile() {
        FSTreeIndex missing = new FSTreeIndex(storage.toString(), "missing");

        assertThrows(IOException.class, missing::load);
    }

    @Test
    void shouldThrowWhenSavingToMissingDirectory() {
        FSTreeIndex unreachable =
                new FSTreeIndex(storage.resolve("no-such-dir").toString(), "x");

        assertThrows(IOException.class, unreachable::save);
    }
}
