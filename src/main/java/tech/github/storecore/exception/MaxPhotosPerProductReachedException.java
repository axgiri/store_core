package tech.github.storecore.exception;

public class MaxPhotosPerProductReachedException extends RuntimeException {
    public MaxPhotosPerProductReachedException(String message) {
        super(message);
    }
}
