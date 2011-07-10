package net.noiseinstitute.lessjs4java;

import org.mozilla.javascript.JavaScriptException;

import java.util.List;

public class LessCompileException extends Exception {
    private static final long serialVersionUID = -3755438651544754051L;

    private String type;
    private String filename;
    private int index;
    private int line;
    private int callLine;
    private String callExtract;
    private int column;
    private List<String> extract;

    public LessCompileException (String type, String message, String filename, int index, int line, int callLine,
            String callExtract, int column, List<String> extract, JavaScriptException cause) {
        super(message, cause);
        this.type = type;
        this.filename = filename;
        this.index = index;
        this.line = line;
        this.callLine = callLine;
        this.callExtract = callExtract;
        this.column = column;
        this.extract = extract;
    }
}
