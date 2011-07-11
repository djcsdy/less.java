package net.noiseinstitute.less;

import org.mozilla.javascript.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class LessCompiler {
    private Module lessModule;

    public LessCompiler() {
        ModuleLoader moduleLoader = new ModuleLoader();
        try {
            lessModule = moduleLoader.require("less/index");
        } catch (IOException e) {
            throw new LessCompileError(e);
        }
    }

    public String compile (String source) throws LessCompileException {
        final AtomicReference<String> output = new AtomicReference<String>();
        final Context context = Context.enter();
        try {
            final Scriptable globalScope = context.initStandardObjects();
            final Scriptable parser = ((Function) lessModule.get("Parser"))
                    .construct(context, globalScope, new Object[0]);
            final Callable parse = (Callable) parser.get("parse", parser);
            final Object[] parseArgs = new Object[]{
                    source,
                    new JavaScriptFunction() {
                        public Object call (Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                            final Object err = args[0];
                            if (ScriptRuntime.toBoolean(err)) {
                                throw new JavaScriptException(err, "LessCompiler.java", -1);
                            }
                            final Scriptable tree = (Scriptable) args[1];
                            final Callable toCss = (Callable) tree.get("toCSS", tree);
                            output.set((String) toCss.call(cx, globalScope, tree, new Object[0]));
                            return null;
                        }
                    }
            };
            parse.call(context, globalScope, parser, parseArgs);
        } catch (JavaScriptException e) {
            throw wrapAndThrow(e);
        } finally {
            Context.exit();
        }

        return output.get();
    }

    private static LessCompileException wrapAndThrow (JavaScriptException e) throws LessCompileException {
        Object value = e.getValue();
        if (value instanceof Scriptable) {
            Scriptable scriptableValue = (Scriptable) value;
            Object typeObj = scriptableValue.get("type", scriptableValue);
            Object messageObj = scriptableValue.get("message", scriptableValue);
            Object filenameObj = scriptableValue.get("filename", scriptableValue);
            Object indexObj = scriptableValue.get("index", scriptableValue);
            Object lineObj = scriptableValue.get("line", scriptableValue);
            Object callLineObj = scriptableValue.get("callLine", scriptableValue);
            Object callExtractObj = scriptableValue.get("callExtract", scriptableValue);
            Object columnObj = scriptableValue.get("column", scriptableValue);
            Object extractObj = scriptableValue.get("extract", scriptableValue);

            String type = typeObj instanceof String
                    ? (String) typeObj
                    : null;

            String message = messageObj instanceof String
                    ? (String) messageObj
                    : null;

            String filename = filenameObj instanceof String
                    ? (String) filenameObj
                    : null;

            int index;
            if (indexObj instanceof Number) {
                index = ((Number) indexObj).intValue();
            } else {
                index = -1;
            }

            int line;
            if (lineObj instanceof Number) {
                line = ((Number) lineObj).intValue();
            } else {
                line = -1;
            }

            int callLine;
            if (callLineObj instanceof Number) {
                callLine = ((Number) callLineObj).intValue();
            } else {
                callLine = -1;
            }

            String callExtract = callExtractObj instanceof String
                    ? (String) callExtractObj
                    : null;

            int column;
            if (columnObj instanceof Number) {
                column = ((Number) columnObj).intValue();
            } else {
                column = -1;
            }

            List<String> extract = null;
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

            if (type == null) {
                if (message == null) {
                    throw new LessCompileError(e);
                } else {
                    throw new LessCompileError(message, e);
                }
            } else {
                throw new LessCompileException(type, message, filename, index, line, callLine, callExtract, column, extract, e);
            }
        } else {
            throw new LessCompileError(e);
        }
    }
}
