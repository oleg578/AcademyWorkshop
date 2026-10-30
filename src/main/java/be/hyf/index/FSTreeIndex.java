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
public class FSTreeIndex {
    private final String path;
    private TreeMap<Integer, List<Integer>> index;

    public FSTreeIndex(String path, String name) {
        this.path = path + File.separator + name + ".idx";
        index = new TreeMap<>();
    }

    public void load() throws IOException, ClassNotFoundException {
        try (ObjectInputStream os = new ObjectInputStream(new FileInputStream(path))) {
            // we trust the data
            @SuppressWarnings("unchecked")
            TreeMap<Integer, List<Integer>> loaded = (TreeMap<Integer, List<Integer>>) os.readObject();
            this.index = loaded;
        }
    }

    public void save() throws IOException {
        try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(path))) {
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
        save();
    }

    public void remove(int studentId) throws IOException {
        index.remove(studentId);
        save();
    }

    public void removeAll() throws IOException {
        index.clear();
        save();
    }
}
