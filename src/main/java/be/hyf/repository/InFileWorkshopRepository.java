package be.hyf.repository;

import be.hyf.entity.Student;
import be.hyf.entity.Workshop;
import be.hyf.exception.WorkshopNotFoundException;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InFileWorkshopRepository implements WorkshopRepository {
    private final String path;
    private final String fileNameSuffix;

    public InFileWorkshopRepository(String path) {
        this.path = path;
        fileNameSuffix = ".ser";
    }

    public InFileWorkshopRepository(String path, String fileNameSuffix) {
        this.path = path;
        this.fileNameSuffix = fileNameSuffix.trim();
    }

    @Override
    public void save(Workshop workshop) throws IOException {
        // build file name
        String fn = buildFileName(workshop);
        String targetPath = path + File.separator + fn;

        if (fileExistsById(workshop.getId())) throw new IllegalArgumentException("workshop exists");
        // serialize to file
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(targetPath))) {
            oos.writeObject(workshop);
        }
    }

    @Override
    public void update(Workshop workshop) throws IOException {
        // build file name
        String fn = buildFileName(workshop);
        String targetPath = path + File.separator + fn;
        // serialize to file
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(targetPath))) {
            oos.writeObject(workshop);
        }
    }

    @Override
    public void delete(Workshop workshop) throws IOException {
        // build file name
        String fn = buildFileName(workshop);
        String targetPath = path + File.separator + fn;
        //remove given file
        Files.delete(Path.of(targetPath));
    }

    @Override
    public Workshop getById(int id) throws IOException {
        // get list of all files in path
        Path startPath = Paths.get(path);

        // globbing
        String globPattern = "glob:" + id + "_*" + fileNameSuffix;
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher(globPattern);
        List<Path> targets;
        try (Stream<Path> stream = Files.walk(startPath)) {
            targets = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> matcher.matches(path.getFileName()))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        if (targets.isEmpty()) throw new WorkshopNotFoundException("workshop not found");
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(targets.getFirst().toString()))) {
            return (Workshop) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new WorkshopNotFoundException("workshop not found");
        }
    }

    @Override
    public Workshop getByTitle(String title) throws IOException {
        // get list of all files in path
        Path startPath = Paths.get(path);

        // globbing
        String globPattern = "glob:*_" + title + fileNameSuffix;
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher(globPattern);
        List<Path> targets;
        try (Stream<Path> stream = Files.walk(startPath)) {
            targets = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> matcher.matches(path.getFileName()))
                    .toList();
            if (targets.isEmpty()) throw new WorkshopNotFoundException("workshop not found");
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(targets.getFirst().toString()))) {
            return (Workshop) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new WorkshopNotFoundException("workshop not found");
        }
    }

    @Override
    public List<Workshop> getAll() throws IOException{
        // get list of all files in path
        Path startPath = Paths.get(path);

        // globbing
        String globPattern = "glob:*" + fileNameSuffix;
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher(globPattern);
        List<Path> targets;
        try (Stream<Path> stream = Files.walk(startPath)) {
            targets = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> matcher.matches(path.getFileName()))
                    .toList();
            if (targets.isEmpty()) throw new WorkshopNotFoundException("workshops not found");
        }
        return targets.stream()
                .map((path) -> deserialize(
                        path.toString())).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<Student> getParticipants(Workshop workshop) {
        return workshop.getParticipants();
    }

    @Override
    public boolean isFull(Workshop workshop) {
        return workshop.getParticipants().size() >= workshop.getCapacity();
    }

    @Override
    public int availablePlaces(Workshop workshop) {
        return workshop.getCapacity() - workshop.getParticipants().size();
    }

    @Override
    public void register(Workshop workshop, Student student) throws IllegalStateException {
        if (!isFull(workshop)) {
            workshop.addParticipant(student);
        } else {
            throw new IllegalStateException("Workshop is full");
        }
    }

    @Override
    public boolean unregister(Workshop workshop, Student student) {
        return workshop.deleteParticipant(student.getId());
    }

    @Override
    public boolean isRegistered(Workshop workshop, Student student) {
        return workshop.getParticipants().stream().anyMatch(p -> p.getId() == student.getId());
    }

    private boolean fileExistsById(int id) throws IOException {
        PathMatcher matcher = FileSystems.getDefault()
                .getPathMatcher("glob:" + id + "_*" + fileNameSuffix);
        try (Stream<Path> stream = Files.walk(Path.of(path))) {
            return stream
                    .filter(Files::isRegularFile)
                    .anyMatch(file -> matcher.matches(file.getFileName()));
        }
    }

    String buildFileName(Workshop workshop) {
        return workshop.getId() + "_" + workshop.getTitle() + this.fileNameSuffix;
    }

    Workshop deserialize(String path) throws WorkshopNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            return (Workshop) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new WorkshopNotFoundException("workshops not found");
        }
    }
}
