package net.noiseinstitute.less;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrappedException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class ModuleLoader {
    private Map<String, Module> modules;

    public ModuleLoader() {
        modules = new HashMap<String, Module>();
    }

    public Module require(String modulePath) throws IOException {
        if (modules.containsKey(modulePath)) {
            return modules.get(modulePath);
        } else {
            final ClassLoader classLoader = getClass().getClassLoader();
            final InputStream inputStream = classLoader.getResourceAsStream("net/noiseinstitute/less/" + modulePath + ".js");
            if (inputStream == null) {
                throw new FileNotFoundException("Module not found: " + modulePath);
            } else try {
                final Reader reader = new InputStreamReader(inputStream, "UTF-8");
                try {
                    final Context context = Context.enter();
                    try {
                        final Scriptable scope = context.initStandardObjects();

                        final Module module = new Module();
                        modules.put(modulePath, module);
                        scope.put("exports", scope, module);

                        scope.put("require", scope, new JavaScriptFunction() {
                            public Object call (Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                                final String modulePath = ((String) args[0]);
                                try {
                                    return require(modulePath);
                                } catch (IOException e) {
                                    throw new WrappedException(e);
                                }
                            }
                        });

                        context.evaluateReader(scope, reader, modulePath, 1, null);

                        return module;
                    } finally {
                        Context.exit();
                    }
                } finally {
                    reader.close();
                }
            } finally {
                inputStream.close();
            }
        }
    }
}
