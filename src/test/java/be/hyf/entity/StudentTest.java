package be.hyf.entity;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StudentTest {

    @Test
    void shouldExposeConstructorValuesWhenCreated() {
        Student student = new Student(1, "Alice", "alice@example.com", "Brussels");

        assertEquals(1, student.getId());
        assertEquals("Alice", student.getName());
        assertEquals("alice@example.com", student.getEmail());
        assertEquals("Brussels", student.getCity());
    }

    @Test
    void shouldChangeCityWhenSetCityCalled() {
        Student student = new Student(1, "Alice", "alice@example.com", "Brussels");

        student.setCity("Ghent");

        assertEquals("Ghent", student.getCity());
    }

    @Test
    void shouldKeepOtherFieldsWhenCityChanged() {
        Student student = new Student(1, "Alice", "alice@example.com", "Brussels");

        student.setCity("Ghent");

        assertEquals(1, student.getId());
        assertEquals("Alice", student.getName());
        assertEquals("alice@example.com", student.getEmail());
    }

    @Test
    void shouldAcceptNullCityWhenSetCityCalledWithNull() {
        Student student = new Student(1, "Alice", "alice@example.com", "Brussels");

        student.setCity(null);

        assertNull(student.getCity());
    }

    @Test
    void shouldRestoreAllFieldsWhenSerializedAndDeserialized() throws IOException, ClassNotFoundException {
        Student original = new Student(7, "Bob", "bob@example.com", "Liège");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bytes)) {
            oos.writeObject(original);
        }
        Student restored;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = (Student) ois.readObject();
        }

        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getName(), restored.getName());
        assertEquals(original.getEmail(), restored.getEmail());
        assertEquals(original.getCity(), restored.getCity());
    }
}
