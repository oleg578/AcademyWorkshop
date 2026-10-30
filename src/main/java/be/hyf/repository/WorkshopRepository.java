package be.hyf.repository;

import be.hyf.entity.Student;
import be.hyf.entity.Workshop;

import java.io.IOException;
import java.util.List;

public interface WorkshopRepository {

    /**
     * Saves a workshop.
     *
     * @param workshop the workshop to save
     */
    void save(Workshop workshop) throws IOException;

    /**
     * Updates a workshop.
     *
     * @param workshop the workshop to update
     */
    void update(Workshop workshop) throws IOException;

    /**
     * Deletes a workshop.
     *
     * @param workshop the workshop to delete
     */
    void delete(Workshop workshop) throws IOException;

    /**
     * Gets a workshop by its ID.
     *
     * @param id the ID of the workshop
     * @return the workshop with the given ID, or null if no such workshop exists
     */
    Workshop getById(int id) throws IOException;

    /**
     * Gets a workshop by its title.
     *
     * @param title the title of the workshop
     * @return the workshop with the given title, or null if no such workshop exists
     */
    Workshop getByTitle(String title) throws IOException;

    /**
     *
     * @return List of workshops
     * @throws IOException any IO exception
     */
    List<Workshop> getAll() throws IOException;

    /**
     * Gets the participants of a workshop.
     *
     * @param workshop the workshop to get the participants of
     * @return the participants of the workshop
     */
    List<Student> getParticipants(Workshop workshop);

    /**
     * Checks if a workshop is full.
     *
     * @param workshop the workshop to check
     * @return true if the workshop is full, false otherwise
     */
    boolean isFull(Workshop workshop);

    /**
     * Gets the available places in a workshop.
     *
     * @param workshop the workshop to get the available places of
     * @return the available places in the workshop
     */
    int availablePlaces(Workshop workshop);

    /**
     * Registers a student to a workshop.
     *
     * @param workshop instance of a workshop
     * @param student instance of a student
     */
    void register(Workshop workshop, Student student);

    /**
     * Unregisters a student from a workshop.
     *
     * @param workshop instance of a workshop
     * @param student instance of a student
     * @return true if the student was successfully unregistered, false otherwise
     */
    boolean unregister(Workshop workshop, Student student);

    /**
     * Checks if a student is registered to a workshop.
     *
     * @param workshop instance of a workshop
     * @param student instance of a student
     * @return true if the student is registered, false otherwise
     */
    boolean isRegistered(Workshop workshop, Student student);
}
