package net.noiseinstitute.less;

import java.io.File;
import java.util.Vector;

public class Options {
    public boolean compress = false;
    public int optimizationLevel = 1;
    public boolean silent = false;
    public Vector<File> paths = new Vector<File>();
}
