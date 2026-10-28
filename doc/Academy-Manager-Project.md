# Academy Workshop Manager — One-Day Java Review Project

## The story

Your academy runs short workshops. Some take place in a classroom; others take place online. Build a small Java application that registers students for workshops and produces a readable report.

For example: register Chitrakalpa and Paul for a Java workshop with two places. Reject a third registration because it is full. Remove Paul, register Gayatri, and export the updated list.

**Work in pairs. Target: one teaching day with approximately 6–7 focused hours.** Use Java 17+ and JUnit 5, with the JUnit setup you already used in class.
Start with a scripted `main()`; a console menu is a bonus. Swap the keyboard every 30 minutes.

This is a guided review, not an architecture assignment. The suggested classes and contracts below keep the task small. Write tests alongside the implementation.

## 1. What the application does

1. Read a small student list from a text file.
2. Create two workshops in Java code: one classroom workshop and one online workshop.
3. Register a student, reject invalid registrations, and remove a registration.
4. List participants and show how to join each workshop.
5. Search students and produce three small reports.
6. Write the report to a text file and verify behaviour with JUnit.

There are no grades, dates, enrollment statuses, waiting lists, repository layers, or enrollment policies. A registration simply means that a student is currently in a workshop's participant list. Removing it frees a place; the same student may register again later.

## 2. A small model

Use separate top-level classes. Suggested structure:

| Type | Responsibility |
| --- | --- |
| `ContactDetails` | Immutable email and city |
| `Student` | Immutable positive ID, name, and contact details |
| abstract `Workshop` | ID, title, capacity, participant list, registration rules |
| `ClassroomWorkshop` | Adds a room; joining instructions describe where to go |
| `OnlineWorkshop` | Adds a meeting URL; joining instructions describe how to connect |
| `Academy` | Stores students/workshops, finds them by ID, builds reports |
| `ReportOutput` | Interface for sending report lines somewhere |
| `ConsoleReportOutput` | Prints the report |
| `FileReportOutput` | Writes the report using the supplied file helper |
| `Main` | Loads data, creates objects, runs the demonstration |

The `SimpleFiles` utility in section 6 is supplied code: copy it, do not implement it from scratch.

### Composition

A student **has contact details**. Store a `ContactDetails` object inside `Student`, rather than adding email and city directly to `Student`. Treat that object as an owned, immutable value.

A workshop keeps references to existing students. Removing a registration must not delete the student from the academy. These shared references are associations, not ownership of the student objects.

### Inheritance and polymorphism

Both concrete workshop classes extend `Workshop` and implement:

```java
public abstract String joiningInstructions();
```

Example results:

- Classroom: `Go to room B12.`
- Online: `Join at https://example.com/java-room.`

Store both kinds together as `Workshop` values. When building a report, call `workshop.joiningInstructions()` through the base type. Do not use `instanceof` or a type switch to choose the instructions.

Keep common registration behaviour in the base class. Subclasses only add their location and joining instructions.

## 3. Collections and operations

Use these collections so that each has a clear purpose:

| Location | Collection | Why? |
| --- | --- | --- |
| Students in `Academy` | `Map<Integer, Student>` backed by `HashMap` | Find a student by ID |
| Workshops in `Academy` | `Map<Integer, Workshop>` backed by `LinkedHashMap` | Find by ID and retain creation order |
| Participants in each workshop | `List<Student>` backed by `ArrayList` | Retain registration order and practise iterator removal |
| Cities of all students | `TreeSet<String>` | Unique city names in alphabetical order |
| Participant counts by city for one workshop | `TreeMap<String, Long>` | Grouped counts with alphabetical keys |

Keep collections private. Getters return copies, such as `List.copyOf(participants)`, rather than the internal collection. Student and contact fields are immutable, so callers cannot change them through a returned list.

### Suggested method contracts

```java
// Academy
void addStudent(Student student);
void addWorkshop(Workshop workshop);
Optional<Student> findStudent(int id);
Optional<Workshop> findWorkshop(int id);
void register(int studentId, int workshopId);
boolean unregister(int studentId, int workshopId);

// Workshop
void register(Student student);
boolean unregister(int studentId);
List<Student> getParticipants();
int availablePlaces();
```

`Academy.register` and `Academy.unregister` find the existing objects and delegate to the workshop. Workshop methods own the capacity and duplicate-registration rules. Do not maintain a second registration list in `Academy`.

### Rules

- IDs and capacity must be positive. Names, email, city, titles, room and meeting URL must be non-null and non-blank. Trim text. Detailed email/URL validation is outside scope.
- Reject duplicate student IDs and duplicate workshop IDs in `Academy`. Names need not be unique. Email uniqueness is outside scope.
- A student can join several different workshops, but only once in each workshop.
- A workshop with capacity 2 accepts its first and second participants, then rejects the third.
- A failed registration leaves the participant list unchanged.
- Removing an existing participant returns `true`; removing a known student who is not registered returns `false`.
- `Academy.register` and `Academy.unregister` throw `IllegalArgumentException` if either referenced ID is unknown. `find...` methods return an empty `Optional` for an unknown ID.
- Use `IllegalArgumentException` for invalid constructor values or duplicate academy IDs; use `IllegalStateException` for duplicate registration or a full workshop.
- In workshop registration, check duplicate registration before capacity. Reject a null student with `IllegalArgumentException`.

**Iterator requirement:** implement `Workshop.unregister` with an explicit `Iterator<Student>` and `iterator.remove()`. Do not remove from the list directly inside an enhanced `for` loop. This is deliberately an iterator exercise, even though `removeIf` could also solve it.

Compare student IDs when detecting duplicates or removing participants. Implementing entity `equals()`/`hashCode()` is an optional extension for this one-day version.

## 4. Small reports with Streams

Reports return data; they do not print. Add these to `Academy`:

| Method | Result and behaviour |
| --- | --- |
| `List<Student> searchStudents(String text)` | Case-insensitive name substring, sorted by name then ID; blank text returns all; null throws `IllegalArgumentException` |
| `List<String> participantNames(int workshopId)` | Names sorted alphabetically, with student ID as a tie-breaker; unknown workshop throws `IllegalArgumentException` |
| `TreeMap<String, Long> participantCountsByCity(int workshopId)` | Count participants in that workshop by city; unknown workshop throws `IllegalArgumentException` |
| `TreeSet<String> allCities()` | Unique cities of all academy students, alphabetically sorted |
| `List<String> buildReport()` | One section per workshop, in workshop creation order |

Practise `filter`, `sorted`, `map`, and `collect`. For counts, use `Collectors.groupingBy` with a `TreeMap` supplier and `Collectors.counting()`. For cities, use `Collectors.toCollection(TreeSet::new)`.

Use `Locale.ROOT` for case-insensitive search. Other sorting/grouping uses ordinary Java string ordering, case-sensitive. Empty data returns empty collections; an empty workshop still appears in `buildReport()` with count 0.

Report format for each workshop:

```text
Java Basics | 2/2 participants
Go to room B12.
- Chitrakalpa
- Gayatri
```

Use the sorted participant names in each section and one blank line between sections. `buildReport()` must not change the stored registration order. Exact wording of joining instructions can vary; keep it consistent in your tests.

## 5. An interface with two useful implementations

```java
public interface ReportOutput {
    void write(List<String> lines) throws IOException;
}
```

Add the necessary `java.util.List` and `java.io.IOException` imports.

- `ConsoleReportOutput`: print each line. An implementation may omit `throws IOException` if it does not need it.
- `FileReportOutput`: receive a `Path` in the constructor, then delegate to `SimpleFiles.writeLines(path, lines)`.

In `Main`, create both implementations and use the same report with both:

```java
List<ReportOutput> outputs = List.of(
        new ConsoleReportOutput(),
        new FileReportOutput(Path.of("output", "report.txt"))
);

List<String> report = academy.buildReport();
for (ReportOutput output : outputs) {
    output.write(report);
}
```

Handle `IOException` at the application boundary with a clear message. Do not hide it inside the file output class or print “saved” after a failed write. The loop uses interface polymorphism; it does not know how each implementation writes.

## 6. Reading and writing without a large persistence task

Create `data/students.txt` with this content (UTF-8, no header):

```text
1;Chitrakalpa;chitrakalpa@example.com;Brussels
2;Paul;paul@example.com;Ghent
3;Gayatri;gayatri@example.com;Brussels
4;Oleh;oleh@example.com;Antwerp
```

The four columns are ID, name, email, city. Values cannot contain semicolons. Blank lines may be skipped. This is a tiny classroom format, not general CSV support.

Copy this supplied helper into `SimpleFiles.java` and use it:

```java
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class SimpleFiles {
    private SimpleFiles() {}

    public static List<String> readLines(Path path) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    public static void writeLines(Path path, List<String> lines) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
}
```

Read the helper together: identify buffering, try-with-resources, and where an `IOException` goes. Students write the small conversion from file lines into `Student` objects. Start with this pattern:

```java
for (String line : SimpleFiles.readLines(Path.of("data", "students.txt"))) {
    if (line.isBlank()) continue;
    String[] fields = line.split(";", -1);
    if (fields.length != 4) {
        throw new IllegalArgumentException("Expected four columns: " + line);
    }
    int id = Integer.parseInt(fields[0].trim());
    // Create ContactDetails, then Student, then call academy.addStudent(...).
}
```

If the file is missing or a row is invalid, show an error and stop the demo before any registrations or report export. Catch `IOException` and `IllegalArgumentException` at the startup boundary; `NumberFormatException` is already a subtype of `IllegalArgumentException`.

Use a fresh local `Academy` during startup. No interactive reload or rollback mechanism is needed. Set the IDE working directory to the folder containing `data/`.

**Reading and writing have different purposes here:** read the student list; write a human-readable report. The report is not a save file. Registrations are recreated by the demo on each run. Full save/reload and Java object serialization are outside this day's scope.

## 7. JUnit: test behaviours, including edge cases

Use fresh objects in each test. Write tests while building each feature, not only at the end. No Mockito is needed.

| Area | Required cases |
| --- | --- |
| Validation | Capacity 0 rejected; blank student name rejected; duplicate student ID rejected |
| Registration | First registration succeeds; duplicate rejected; last place accepted; next student rejected without changing the list |
| Removal | Middle participant removed safely; absent participant returns false; removing a participant frees a place; that student can register again |
| Lookup | Missing lookup gives empty `Optional`; registration with unknown student or workshop ID throws |
| Polymorphism | Calling `joiningInstructions()` through each `Workshop` reference returns the appropriate instructions |
| Search | Mixed-case query works; no match gives empty list; blank query returns all sorted; equal names retain both students |
| Ordered collections | Duplicate cities occur once in `allCities()`; city keys are alphabetical; counts are correct |
| Reports | Empty workshop is included; no students gives empty search/city results; building a sorted report preserves registration order |
| File integration | Export to `@TempDir`, read the file back, compare lines with the report |

For ordered collections, assert their iteration order explicitly, for example with `assertIterableEquals(List.of("Antwerp", "Brussels"), actualCities)`. Set or map equality alone does not verify order.

For failed operations, check both `assertThrows(...)` and the resulting participant list/available places. For the file test, exercise `FileReportOutput`, then read the saved file and compare its lines. Use `@TempDir`; never write test output over the input file.

## 8. Build order and time boxes

| Stage | Approximate time | Working checkpoint |
| --- | --- | --- |
| Read the scenario and sketch classes | 20 min | Explain one registration in plain English |
| Model, inheritance, validation + tests | 60 min | Both workshop types work through base references |
| Academy, registration, iterator removal + tests | 90 min | Capacity and removal rules work |
| Stream reports and ordering + tests | 65 min | Results are correct, empty cases included |
| Interface outputs and file input/output + integration test | 50 min | Input read and report exported |
| Finish tests, debug, rehearse and demonstrate | 75 min | Reproducible demo and green tests |

Total: about **6 hours of focused work**, plus breaks and setup. This is a target for pairs familiar with these topics, not a guarantee. If the group needs more scaffolding, provide constructors/getters and empty method signatures beforehand. Keep the menu and extra features out of the day; preserve time for tests.

### Final demonstration

Create these workshops in this order:

- ID 10: `ClassroomWorkshop`, “Java Basics”, capacity 2, room “B12”.
- ID 20: `OnlineWorkshop`, “JUnit Practice”, capacity 3, URL `https://example.com/junit`.

Load the four students from the file. Then:

1. Register Chitrakalpa and Paul for Java Basics.
2. Attempt to register Gayatri; catch the expected full-workshop exception and continue.
3. Remove Paul; register Gayatri successfully.
4. Register Chitrakalpa and Oleh for JUnit Practice.
5. Print and export the report. Run the tests.

Expected final results:

| Check | Expected |
| --- | --- |
| Java participants, sorted | Chitrakalpa, Gayatri |
| Java available places | 0 |
| JUnit participants, sorted | Chitrakalpa, Oleh |
| JUnit available places | 1 |
| JUnit participants by city | Antwerp → 1, Brussels → 1 |
| All student cities | Antwerp, Brussels, Ghent |
| Search `ARL` | Chitrakalpa |

Submit the Java source, tests, input file, generated report, and a few lines explaining how to run the program. In the demo, explain one collection choice, the composition relationship, and the two forms of polymorphism. No separate design document is required.

## 9. Topic checklist and optional next steps

| Topic | Where you practise it |
| --- | --- |
| Collections | Student/workshop maps and participant lists |
| Composition | Student owns ContactDetails |
| Inheritance | ClassroomWorkshop and OnlineWorkshop extend Workshop |
| Exceptions | Validation, failed registration, file failures |
| Interfaces | ReportOutput with two implementations |
| Iterators | Safe removal of a participant |
| Polymorphism | Workshop instructions and report output dispatch |
| Streams | Search, participant names, city grouping |
| Ordered collections | LinkedHashMap creation order; TreeSet/TreeMap alphabetical order |
| Reading/writing | Load student text; export report through supplied buffered I/O |
| JUnit | Normal, boundary, failure, ordering and file integration cases |

Only after the core works: add a console menu, implement student equality by ID and test it with a HashSet, or compare a TreeSet of students with different Comparators. Do not attempt to use every collection type in the core.

For the next SQL lesson, discuss how the student–workshop relationship becomes a registration table with student ID and workshop ID. You do not need to implement that table or an Enrollment class today.

---

## Instructor note: using this version with the old starter

This brief replaces the larger Academy Manager assignment. It is self-contained: distribute this document and create the small text input above. The old `AcademyFiles`, `AcademyData`, transport classes, `academy.properties`, and `StarterDemo` belong to the previous snapshot-based project and are **not required or compatible substitutes for this version's instructions**. Do not give students both sets of setup instructions for this exercise.

`ConsoleInput` can be reused if a pair reaches the optional menu. The old GETTING-STARTED and test README should not be presented as instructions for this version. Keep the larger project separately for a later multi-day extension.
