package net.noiseinstitute.less;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class Paths extends ScriptableObject {
    private static final long serialVersionUID = 3199126771150495250L;

    public Paths () {
        put("unshift", this, new JavaScriptFunction() {
            public Object call (Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                return null;
            }
        });
    }

    @Override
    public String getClassName () {
        return "Paths";
    }
}
