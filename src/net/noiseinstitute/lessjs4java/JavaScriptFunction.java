package net.noiseinstitute.lessjs4java;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

abstract class JavaScriptFunction implements Function {
    public Scriptable construct (Context context, Scriptable scriptable, Object[] objects) {
        throw new LessCompileError("Internal JavaScript function called as constructor");
    }

    public String getClassName () {
        return "Function";
    }

    public Object get (String s, Scriptable scriptable) {
        return null;
    }

    public Object get (int i, Scriptable scriptable) {
        return null;
    }

    public boolean has (String s, Scriptable scriptable) {
        return false;
    }

    public boolean has (int i, Scriptable scriptable) {
        return false;
    }

    public void put (String s, Scriptable scriptable, Object o) {
        throw new LessCompileError("Tried to set property on internal JavaScript function");
    }

    public void put (int i, Scriptable scriptable, Object o) {
        throw new LessCompileError("Tried to set property on internal JavaScript function");
    }

    public void delete (String s) {
        throw new LessCompileError("Tried to delete property from internal JavaScript function");
    }

    public void delete (int i) {
        throw new LessCompileError("Tried to delete property from internal JavaScript function");
    }

    public Scriptable getPrototype () {
        return null;
    }

    public void setPrototype (Scriptable scriptable) {
        throw new LessCompileError("Tried to set prototype of internal JavaScript function");
    }

    public Scriptable getParentScope () {
        return null;
    }

    public void setParentScope (Scriptable scriptable) {
        throw new LessCompileError("Tried to set parent scope of internal JavaScript function");
    }

    public Object[] getIds () {
        return new Object[0];
    }

    public Object getDefaultValue (Class<?> aClass) {
        return null;
    }

    public boolean hasInstance (Scriptable scriptable) {
        return false;
    }
}
