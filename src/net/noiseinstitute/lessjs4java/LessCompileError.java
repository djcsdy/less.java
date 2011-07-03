package net.noiseinstitute.lessjs4java;

class LessCompileError extends Error {
    private static final long serialVersionUID = -1207803729594146589L;

    public LessCompileError (String message) {
        super(message);
    }
}
