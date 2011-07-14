package net.noiseinstitute.less;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final String CHARSET = "UTF-8";
    private static final int BUFFER_SIZE = 262144;
    private static final int BUFFER_OFFSET = 0;

    private static final Pattern OPTION_REGEXP =
            Pattern.compile("^--?([a-z-][0-9a-z-]*)(?:=([^\\s]+))?$", Pattern.CASE_INSENSITIVE);

    public static void main(String[] args) throws FileNotFoundException {
        new Main().execute(args, System.in, System.out);
    }

    public void execute (String[] args, InputStream in, PrintStream out) {
        CommandLineOptions options = readOptions(args);
        if (options.showVersion) {
            showVersion(out);
        } else if (options.showHelp) {
            showHelp(out);
        } else {
            execute(options, in, out);
        }
    }

    private void showVersion (PrintStream out) {
        out.println("less.java"); // TODO
    }

    private void showHelp (PrintStream out) {
        out.println("less.java"); // TODO
    }

    private void execute (CommandLineOptions options, InputStream in, OutputStream out) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        Reader reader = null;
        Writer writer = null;
        BufferedWriter bufferedWriter = null;
        try {
            try {
                if (options.inputFilename == null) {
                    reader = new InputStreamReader(in, CHARSET);
                } else {
                    fileInputStream = new FileInputStream(options.inputFilename);
                    reader = new InputStreamReader(fileInputStream, CHARSET);
                }

                if (options.outputFilename == null) {
                    writer = new OutputStreamWriter(out, CHARSET);
                } else {
                    fileOutputStream = new FileOutputStream(options.outputFilename);
                    writer = new OutputStreamWriter(fileOutputStream, CHARSET);
                }
                bufferedWriter = new BufferedWriter(writer);

                String source = readSource(reader);
                writer.write(new LessCompiler().compile(source, options));
            } finally {
                if (reader != null) {
                    reader.close();
                }
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                if (writer != null) {
                    writer.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            }
        } catch (LessCompileException e) {
            System.err.println(e.toString());
        } catch (LessCompileError e) {
            System.err.println(e.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CommandLineOptions readOptions (String[] args) {
        final CommandLineOptions options = new CommandLineOptions();

        boolean escaped=false;
        for (String arg: args) {
            final Matcher matcher = OPTION_REGEXP.matcher(arg);
            if (escaped || !matcher.matches()) {
                if (options.inputFilename == null) {
                    options.inputFilename = arg;
                } else if (options.outputFilename == null) {
                    options.outputFilename = arg;
                } else {
                    throw new CommandLineOptionsException("Unexpected argument: " + arg);
                }
            } else if ("--".equals(arg)) {
                escaped = true;
            } else {
                final String name = matcher.group(1);
                final String value = matcher.group(2);
                if ("v".equals(name)
                        || "version".equals(name)) {
                    options.showVersion = true;
                } else if ("h".equals(name)
                        || "help".equals(name)) {
                    options.showHelp = true;
                } else if ("verbose".equals(name)) {
                    options.verbose = true;
                } else if ("s".equals(name)
                        || "silent".equals(name)) {
                    options.silent = true;
                } else if ("x".equals(name)
                        || "compress".equals(name)) {
                    options.compress = true;
                } else if ("i".equals(name)
                        || "input-file".equals(name)) {
                    if (value == null || "".equals(name)) {
                        throw new CommandLineOptionsException("Must specify a value for input-file");
                    }
                    options.inputFilename = value;
                } else if ("o".equals(name)
                        || "output-file".equals(name)) {
                    if (value == null || "".equals(value)) {
                        throw new CommandLineOptionsException("Must specify a value for output-file");
                    }
                    options.outputFilename = value;
                } else if ("include-path".equals(name)) {
                    if (value == null || "".equals(value)) {
                        throw new CommandLineOptionsException("Must specify a value for include-path");
                    }
                    options.paths.add(new File(value));
                } else if ("O0".equals(name)) {
                    options.optimizationLevel = 0;
                } else if ("O1".equals(name)) {
                    options.optimizationLevel = 1;
                } else if ("O2".equals(name)) {
                    options.optimizationLevel = 2;
                } else {
                    throw new CommandLineOptionsException("Unknown option: " + name);
                }
            }
        }

        return options;
    }

    private String readSource (Reader reader) throws IOException {
        StringBuilder builder = new StringBuilder(BUFFER_SIZE);
        char[] buffer = new char[BUFFER_SIZE];
        int numCharsRead = reader.read(buffer, BUFFER_OFFSET, BUFFER_SIZE);
        while (numCharsRead >= 0) {
            builder.append(buffer, BUFFER_OFFSET, numCharsRead);
            numCharsRead = reader.read(buffer, BUFFER_OFFSET, BUFFER_SIZE);
        }
        return builder.toString();
    }
}
