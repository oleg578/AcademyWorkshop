package be.hyf.exception;

public class WorkshopNotFoundException extends RuntimeException {
    public WorkshopNotFoundException() {
        super();
    }

    public WorkshopNotFoundException(String message) {
        super(message);
    }
}
