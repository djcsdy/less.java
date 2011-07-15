package net.noiseinstitute.less;

import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;

import java.util.ArrayList;
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

    public LessCompileException (Scriptable error) {
        super(getMessage(error));
        extractInfo(error);
    }

    public LessCompileException (JavaScriptException cause) {
        super(getMessage(cause), cause);
        extractInfo(cause);
    }

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

    public String getType () {
        return type;
    }

    public String getFilename () {
        return filename;
    }

    public int getIndex () {
        return index;
    }

    public int getLine () {
        return line;
    }

    public int getCallLine () {
        return callLine;
    }

    public String getCallExtract () {
        return callExtract;
    }

    public int getColumn () {
        return column;
    }

    public List<String> getExtract () {
        return new ArrayList<String>(extract);
    }

    private static String getMessage (Scriptable error) {
        Object message = error.get("message", error);
        if (message instanceof String) {
            return (String) message;
        } else {
            return null;
        }
    }

    private static String getMessage (JavaScriptException cause) {
        Object value = cause.getValue();
        if (value instanceof Scriptable) {
            String message = getMessage((Scriptable) value);
            if (message != null) {
                return message;
            }
        }

        return null;
    }

    private void extractInfo (JavaScriptException cause) {
        Object value = cause.getValue();
        if (value instanceof Scriptable) {
            extractInfo((Scriptable) value);
        }
    }

    private void extractInfo (Scriptable error) {
        Object typeObj = error.get("type", error);
        Object filenameObj = error.get("filename", error);
        Object indexObj = error.get("index", error);
        Object lineObj = error.get("line", error);
        Object callLineObj = error.get("callLine", error);
        Object callExtractObj = error.get("callExtract", error);
        Object columnObj = error.get("column", error);
        Object extractObj = error.get("extract", error);

        type = typeObj instanceof String
                ? (String) typeObj
                : null;

        filename = filenameObj instanceof String
                ? (String) filenameObj
                : null;

        if (indexObj instanceof Number) {
            index = ((Number) indexObj).intValue();
        } else {
            index = -1;
        }

        if (lineObj instanceof Number) {
            line = ((Number) lineObj).intValue();
        } else {
            line = -1;
        }

        if (callLineObj instanceof Number) {
            callLine = ((Number) callLineObj).intValue();
        } else {
            callLine = -1;
        }

        callExtract = callExtractObj instanceof String
                ? (String) callExtractObj
                : null;

        if (columnObj instanceof Number) {
            column = ((Number) columnObj).intValue();
        } else {
            column = -1;
        }

        if (extractObj instanceof Scriptable) {
            Scriptable scriptableExtract = (Scriptable) extractObj;
            Object lengthObj = scriptableExtract.get("length", scriptableExtract);
            if (lengthObj instanceof Number) {
                int length = ((Number) lengthObj).intValue();
                extract = new ArrayList<String>(length);
                for (int i=0; i<length; ++i) {
                    Object extractLineObj = scriptableExtract.get(i, scriptableExtract);
                    extract.add(extractLineObj.toString());
                }
            }
        }
    }
}
