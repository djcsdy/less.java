package net.noiseinstitute.less;

import java.io.File;
import java.util.Vector;

public class Options {
    public boolean verbose = false;
    public boolean silent = false;
    public boolean compress = false;
    public int optimizationLevel = 1;
    public Vector<File> paths = new Vector<File>();
}
