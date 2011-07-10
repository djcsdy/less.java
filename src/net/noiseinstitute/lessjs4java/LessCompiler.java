package net.noiseinstitute.lessjs4java;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class LessCompiler {
    private Scriptable globalScope;

    public LessCompiler() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("net/noiseinstitute/lessjs4java/less.js");
        try {
            try {
                Reader reader = new InputStreamReader(inputStream, "UTF-8");
                try {
                    Context context = Context.enter();
                    // context.setOptimizationLevel(-1); // Without this, Rhino hits a 64K bytecode limit and fails
                    try {
                        globalScope = context.initStandardObjects();


                        final ScriptableObject exports = new ScriptableObject() {
                            private static final long serialVersionUID = 1393015867036042893L;

                            @Override
                            public String getClassName () {
                                return "Object";
                            }

                            @Override
                            public Object getDefaultValue(Class<?> aClass) {
                                if (aClass == String.class) {
                                    return "[Native object: exports]";
                                } else if (aClass == Number.class) {
                                    return Double.NaN;
                                } else if (aClass == Boolean.class) {
                                    return true;
                                } else {
                                    return this;
                                }
                            }
                        };
                        globalScope.put("exports", globalScope, exports);

                        globalScope.put("require", globalScope, new JavaScriptFunction() {
                            public Object call (Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                                final String key = ((String) args[0]).split("/", 2)[1];
                                Object result = exports.get(key);
                                if (result == null) {
                                    result = new ScriptableObject() {
                                        private static final long serialVersionUID = -3992320302483207465L;

                                        @Override
                                        public String getClassName () {
                                            return "Object";
                                        }

                                        @Override
                                        public Object getDefaultValue(Class<?> aClass) {
                                            if (aClass == String.class) {
                                                return "[Native object: module " + key + "]";
                                            } else if (aClass == Number.class) {
                                                return Double.NaN;
                                            } else if (aClass == Boolean.class) {
                                                return true;
                                            } else {
                                                return this;
                                            }
                                        }
                                    };
                                    exports.put(key, exports, result);
                                }
                                return result;
                            }
                        });

                        context.evaluateReader(globalScope, reader, "less.js", 0, null);
                    } finally {
                        Context.exit();
                    }
                } finally {
                    reader.close();
                }
            } catch (UnsupportedEncodingException e) {
                throw new Error(e); // This should never happen
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            throw new Error(e); // This should never happen
        }
    }

    public String compile(String source) throws LessCompileException {
        Context context = Context.enter();
        try {
            Scriptable compileScope = context.newObject(globalScope);
            final AtomicReference<String> failureMessage = new AtomicReference<String>();
            final AtomicReference<String> output = new AtomicReference<String>();

            compileScope.put("source", compileScope, source);

            compileScope.put("writeOutput", compileScope, new JavaScriptFunction() {
                public Object call (Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                    output.set((String) args[0]);
                    return null;
                }
            });

            try {
                context.evaluateString(compileScope,
                        "new(exports.Parser)()." +
                                "parse(source, function(err, tree) {" +
                                "    if (err) {" +
                                "        throw err;" +
                                "    } else {" +
                                "        writeOutput(tree.toCSS());" +
                                "    }" +
                                "});",
                        "LessCompiler",
                        0,
                        null);
            } catch (JavaScriptException e) {
                wrapAndThrowException(e);
            }

            return output.get();
        } finally {
            Context.exit();
        }
    }

    private static void wrapAndThrowException (JavaScriptException e) throws LessCompileException {
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
