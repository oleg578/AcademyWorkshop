package be.hyf.index;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * This class represents a student workshop index.
 * There is a tree map that stores the index of the student workshop.
 * For each student id the list of workshop ids is stored.
 */
public class FSStudentWorkshopIndex {
    private final String path;
    private final String fileNameSuffix;
    private TreeMap<Integer, List<Integer>> index;

    public FSStudentWorkshopIndex(String path) {
        this.path = path;
        this.fileNameSuffix = ".idx";
        index = new TreeMap<>();
    }

    public void load(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream os = new ObjectInputStream(new FileInputStream(filePath))) {
            // we trust the data
            @SuppressWarnings("unchecked")
            TreeMap<Integer, List<Integer>> loaded = (TreeMap<Integer, List<Integer>>) os.readObject();
            this.index = loaded;
        }
    }

    public void save(String filePath) throws IOException {
        try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(filePath))) {
            os.writeObject(index);
        }
    }

    public TreeMap<Integer, List<Integer>> getIndex() {
        return index;
    }

    public List<Integer> get(int studentId) {
        return index.get(studentId);
    }

    public void add(int studentId, int workshopId) throws IOException {
        index.computeIfAbsent(studentId, k -> new ArrayList<>()).add(workshopId);
        save(this.path);
    }

    public void remove(int studentId) throws IOException {
        index.remove(studentId);
        save(this.path);
    }

    public void removeAll() throws IOException {
        index.clear();
        save(this.path);
    }
}
