package net.noiseinstitute.less;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DefaultFileResolver implements FileResolver {
    private List<File> paths;

    public DefaultFileResolver () {
        paths = new ArrayList<File>();
    }

    public DefaultFileResolver (File path) {
        paths = new ArrayList<File>();
        if (path.isDirectory()) {
            paths.add(path);
        } else if (path.exists()) {
            paths.add(path.getParentFile());
        }
    }

    public DefaultFileResolver (List<File> paths) {
        this.paths = new ArrayList<File>();
        for (File path: paths) {
            if (path.isDirectory()) {
                this.paths.add(path);
            } else if (path.exists()) {
                this.paths.add(path.getParentFile());
            }
        }
    }

    public Reader resolve (String path) throws IOException {
        File file = new File(path);
        FileInputStream fileInputStream = null;

        if (file.isAbsolute()) {
            fileInputStream = new FileInputStream(file);
        } else {
            for (File basePath: paths) {
                file = new File(basePath, path);
                if (file.isFile()) {
                    fileInputStream = new FileInputStream(file);
                    break;
                }
            }
        }

        if (fileInputStream == null) {
            return null;
        } else {
            return new BufferedReader(
                    new InputStreamReader(fileInputStream));
        }
    }
}
