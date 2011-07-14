package net.noiseinstitute.less;

import org.mozilla.javascript.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class LessCompiler {
    private static final String CHARSET = "UTF-8";

    private Module lessModule;

    public LessCompiler() {
        ModuleLoader moduleLoader = new ModuleLoader();
        try {
            lessModule = moduleLoader.require("less/index");
        } catch (IOException e) {
            throw new LessCompileError(e);
        }
    }

    public String compile (File file) throws IOException, LessCompileException {
        return compile(file, new Options());
    }

    public String compile (File file, Options options) throws IOException, LessCompileException {
        final FileResolver resolver = new DefaultFileResolver(file);
        return compile(file, resolver, options);
    }

    public String compile (File file, FileResolver resolver) throws IOException, LessCompileException {
        return compile(file, resolver, new Options());
    }

    public String compile (File file, FileResolver resolver, Options options) throws IOException, LessCompileException {
        final String source = FileReader.readFile(file, CHARSET);
        return compile(source, file.getName(), resolver, options);
    }

    public String compile (String source) throws LessCompileException {
        return compile(source, null, new DefaultFileResolver(), new Options());
    }

    public String compile (String source, Options options) throws LessCompileException {
        return compile(source, null, new DefaultFileResolver(), options);
    }

    public String compile (String source, FileResolver resolver) throws LessCompileException {
        return compile(source, null, resolver, new Options());
    }

    public String compile (String source, String filename) throws LessCompileException {
        return compile(source, filename, new DefaultFileResolver(), new Options());
    }

    public String compile (String source, String filename, FileResolver resolver) throws LessCompileException {
        return compile(source, filename, resolver, new Options());
    }

    public String compile (String source, String filename, Options options) throws LessCompileException {
        return compile(source, filename, new DefaultFileResolver(), options);
    }

    public String compile (String source, String filename, FileResolver resolver, Options options)
            throws LessCompileException {
        final NativeObject parserOptions = new NativeObject();
        parserOptions.put("optimization", parserOptions, options.optimizationLevel);
        if (filename != null) {
            parserOptions.put("filename", parserOptions, filename);
        }

        final NativeObject toCssOptions = new NativeObject();
        toCssOptions.put("compress", toCssOptions, options.compress);

        final AtomicReference<String> output = new AtomicReference<String>();
        final AtomicReference<Object> error = new AtomicReference<Object>();

        final Context context = Context.enter();
        try {
            final Scriptable globalScope = context.initStandardObjects();
            final Scriptable parser = ((Function) lessModule.get("Parser"))
                    .construct(context, globalScope, new Object[]{parserOptions});
            final Callable parse = (Callable) parser.get("parse", parser);
            final Object[] parseArgs = new Object[]{
                    source,
                    new JavaScriptFunction() {
                        public Object call (Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                            final Object err = args[0];
                            if (ScriptRuntime.toBoolean(err)) {
                                error.set(err);
                            } else {
                                final Scriptable tree = (Scriptable) args[1];
                                final Callable toCss = (Callable) tree.get("toCSS", tree);
                                output.set((String) toCss.call(cx, globalScope, tree, new Object[]{toCssOptions}));
                            }
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

        if (error.get() == null) {
            return output.get();
        } else {
            throw wrapAndThrow((Scriptable) error.get());
        }
    }

    private static LessCompileException wrapAndThrow (Scriptable error) throws LessCompileException {
        throw wrapAndThrow(error, null);
    }

    private static LessCompileException wrapAndThrow (Scriptable error, JavaScriptException cause)
            throws LessCompileException {
        Object typeObj = error.get("type", error);
        Object messageObj = error.get("message", error);
        Object filenameObj = error.get("filename", error);
        Object indexObj = error.get("index", error);
        Object lineObj = error.get("line", error);
        Object callLineObj = error.get("callLine", error);
        Object callExtractObj = error.get("callExtract", error);
        Object columnObj = error.get("column", error);
        Object extractObj = error.get("extract", error);

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

        throw new LessCompileException(
                type, message, filename, index, line, callLine, callExtract, column, extract, cause);
    }

    private static LessCompileException wrapAndThrow (JavaScriptException cause) throws LessCompileException {
        Object value = cause.getValue();
        if (value instanceof Scriptable) {
            Scriptable scriptableValue = (Scriptable) value;
            Object typeObj = scriptableValue.get("type", scriptableValue);
            Object messageObj = scriptableValue.get("message", scriptableValue);

            String type = typeObj instanceof String
                    ? (String) typeObj
                    : null;

            String message = messageObj instanceof String
                    ? (String) messageObj
                    : null;

            if (type == null) {
                if (message == null) {
                    throw new LessCompileError(cause);
                } else {
                    throw new LessCompileError(message, cause);
                }
            } else {
                throw wrapAndThrow(scriptableValue, cause);
            }
        } else {
            throw new LessCompileError(cause);
        }
    }
}
