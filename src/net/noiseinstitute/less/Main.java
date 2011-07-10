package net.noiseinstitute.less;

import java.io.*;

public class Main {
    private static final int BUFFER_SIZE = 262144;
    private static final int BUFFER_OFFSET = 0;

    public static void main(String[] args) throws FileNotFoundException {
        new Main().execute(args, System.in, System.out);
    }

    public void execute (String[] args, InputStream in, PrintStream out) {
        execute(readOptions(args), in, out);
    }

    private void execute (Options options, InputStream in, PrintStream out) {
        String source = readSourceFrom(in);
        try {
            out.print(new LessCompiler().compile(source));
        } catch (LessCompileException e) {
            System.err.println(e.toString());
        } catch (LessCompileError e) {
            System.err.println(e.toString());
        }
    }

    private Options readOptions (String[] args) {
        Options options = new Options();
        for (String arg: args) {
            // TODO
        }
        return options;
    }

    private String readSourceFrom(InputStream inputStream) {
        final InputStreamReader streamReader = new InputStreamReader(inputStream);
        try {
            try {
                StringBuilder builder = new StringBuilder(BUFFER_SIZE);
                char[] buffer = new char[BUFFER_SIZE];
                int numCharsRead = streamReader.read(buffer, BUFFER_OFFSET, BUFFER_SIZE);
                while (numCharsRead >= 0) {
                    builder.append(buffer, BUFFER_OFFSET, numCharsRead);
                    numCharsRead = streamReader.read(buffer, BUFFER_OFFSET, BUFFER_SIZE);
                }
                return builder.toString();
            } finally {
                streamReader.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
