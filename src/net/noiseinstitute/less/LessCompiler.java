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
            throw new LessCompileException(e);
        } finally {
            Context.exit();
        }

        if (error.get() == null) {
            return output.get();
        } else {
            throw new LessCompileException((Scriptable) error.get());
        }
    }
}
