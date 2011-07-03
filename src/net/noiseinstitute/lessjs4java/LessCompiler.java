package net.noiseinstitute.lessjs4java;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;

import java.io.*;
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

            compileScope.put("fail", compileScope, new JavaScriptFunction() {
                public Object call (Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                    failureMessage.set((String) args[0]);
                    return null;
                }
            });

            compileScope.put("writeOutput", compileScope, new JavaScriptFunction() {
                public Object call (Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                    output.set((String) args[0]);
                    return null;
                }
            });

            try {
                context.evaluateString(compileScope,
                        "new(less.Parser)()." +
                                "parse(source, function(err, tree) {" +
                                "    if (err) {" +
                                "        fail(err);" +
                                "    } else {" +
                                "        try {" +
                                "            css = tree.toCSS();" +
                                "            writeOutput(css);" +
                                "        } catch (err) {" +
                                "            fail(err);" +
                                "        })" +
                                "    }" +
                                "});",
                        "LessCompiler",
                        0,
                        null);
            } catch (JavaScriptException e) {
                throw new LessCompileException(e);
            }

            // TODO loop events

            if (failureMessage.get() != null) {
                throw new LessCompileException(failureMessage.get());
            }

            return output.get();
        } finally {
            Context.exit();
        }
    }
}
