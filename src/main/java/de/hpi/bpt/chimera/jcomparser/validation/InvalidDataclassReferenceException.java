package de.hpi.bpt.chimera.jcomparser.validation;

/**
 *
 */
public class InvalidDataclassReferenceException extends RuntimeException {
    public InvalidDataclassReferenceException(String message) {
        super(message);
    }

    public InvalidDataclassReferenceException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
