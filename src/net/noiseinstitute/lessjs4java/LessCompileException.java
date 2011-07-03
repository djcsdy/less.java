package net.noiseinstitute.lessjs4java;

public class LessCompileException extends Exception {
    private static final long serialVersionUID = -3755438651544754051L;

    public LessCompileException (Exception e) {
        super(e);
    }

    public LessCompileException (String message) {
        super(message);
    }
}
