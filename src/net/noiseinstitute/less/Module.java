package net.noiseinstitute.less;

import org.mozilla.javascript.ScriptableObject;

public class Module extends ScriptableObject {
    private static final long serialVersionUID = 5689689406934695901L;

    @Override
    public String getClassName () {
        return "Module";
    }
}
