package tech.github.oldlabclient.exception;

public class MaxPhotosPerProductReachedException extends RuntimeException {
    public MaxPhotosPerProductReachedException(String message) {
        super(message);
    }
}
