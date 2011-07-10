package net.noiseinstitute.lessjs4java;

import org.mozilla.javascript.JavaScriptException;

class LessCompileError extends Error {
    private static final long serialVersionUID = -1207803729594146589L;

    private JavaScriptException cause;

    public LessCompileError (JavaScriptException cause) {
        super(cause.getValue().toString(), cause);
        this.cause = cause;
    }

    public LessCompileError (String message, JavaScriptException cause) {
        super(message, cause);
        this.cause = cause;
    }

    public LessCompileError (String message) {
        super(message);
    }

    @Override
    public String toString() {
        if (cause == null) {
            return super.toString();
        } else {
            return String.format("less.js internal error: %1$s\n%2$s", getMessage(), cause.getScriptStackTrace());
        }
    }
}
