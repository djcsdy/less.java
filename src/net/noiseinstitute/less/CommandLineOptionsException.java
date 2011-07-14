package net.noiseinstitute.less;

public class CommandLineOptionsException extends RuntimeException {
    private static final long serialVersionUID = 2730172602929111243L;

    public CommandLineOptionsException (String message) {
        super(message);
    }
}
