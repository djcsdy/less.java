package net.noiseinstitute.less;

import java.io.*;

public class FileReader extends BufferedReader {
    private static final int BUFFER_OFFSET = 0;
    private static final int BUFFER_SIZE = 262144;

    public FileReader (File file, String charsetName) throws FileNotFoundException, UnsupportedEncodingException {
        super(getInputStreamReader(file, charsetName));
    }

    public static String readFile (File file, String charsetName) throws IOException {
        FileReader reader = new FileReader(file, charsetName);
        try {
            StringBuilder builder = new StringBuilder(BUFFER_SIZE);
            char[] buffer = new char[BUFFER_SIZE];
            int numCharsRead = reader.read(buffer, BUFFER_OFFSET, BUFFER_SIZE);
            while (numCharsRead >= 0) {
                builder.append(buffer, BUFFER_OFFSET, numCharsRead);
                numCharsRead = reader.read(buffer, BUFFER_OFFSET, BUFFER_SIZE);
            }
            return builder.toString();
        } finally {
            reader.close();
        }
    }

    private static Reader getInputStreamReader (File file, String charsetName)
            throws FileNotFoundException, UnsupportedEncodingException {
        InputStream inputStream = new FileInputStream(file);
        return new InputStreamReader(inputStream, charsetName);
    }
}
