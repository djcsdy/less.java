package net.noiseinstitute.less;

import java.io.IOException;
import java.io.Reader;

public interface FileResolver {
    Reader resolve(String path) throws IOException;
}
