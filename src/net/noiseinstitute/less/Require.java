package net.noiseinstitute.less;

import org.mozilla.javascript.*;

import java.io.IOException;

public class Require extends ScriptableObject implements Callable {
    private static final long serialVersionUID = 309787393221008671L;
    private ModuleLoader moduleLoader;

    public Require (ModuleLoader moduleLoader) {
        this.moduleLoader = moduleLoader;
        put("paths", this, new Paths());
    }

    public Object call (Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        final String modulePath = ((String) args[0]);
        try {
            return moduleLoader.require(modulePath);
        } catch (IOException e) {
            throw new WrappedException(e);
        }
    }

    @Override
    public String getClassName () {
        return "Require";
    }
}
