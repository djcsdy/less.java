package net.noiseinstitute.less;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;

public class PathModule extends Module {
    private static final long serialVersionUID = 8319553789807255839L;

    public PathModule () {
        put("join", this, new JavaScriptFunction() {
            public Object call (Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
                StringBuilder builder = new StringBuilder();
                for (Object pathElement : args) {
                    builder.append(ScriptRuntime.toString(pathElement));
                    builder.append("/");
                }
                return builder.substring(0, builder.length() - 1);
            }
        });
    }

    @Override
    public String getClassName () {
        return "PathModule";
    }
}
