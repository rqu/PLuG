package ch.usi.dag.disl.plug;

import ch.usi.dag.disl.DiSL;
import ch.usi.dag.disl.exception.DiSLException;
import java.io.IOException;
import java.io.InputStream;
import static java.util.Arrays.stream;
import java.util.zip.CRC32;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class PLuG {

    public static void main(String[] args) throws DiSLException, IOException {
        if(args.length != 2) {
            System.err.println("Expected exactly two arguments (input, libraries)");
            System.exit(1);
            return;
        }
        final String in = args[0];
        final String[] libs = args[1].split(":");
        
        try(final ZipOutputStream out = new ZipOutputStream(System.out)) {
            // instrument/copy files from input to output
            final DiSL disl = DiSL.init();
            try(final ZipFile inzip = new ZipFile(in)) {
                zipCopy(inzip, out, disl, false);
            } finally {
                disl.terminate();
            }
            
            // copy classfiles from libraries to output
            stream(libs).forEachOrdered(lib -> {
                try(final ZipFile libzip = new ZipFile(lib)) {
                    zipCopy(libzip, out, null, true);
                } catch(IOException e) {
                    System.err.printf("Ignoring library path entry \"%s\": %s", lib, e.getMessage());
                }
            });
        }
    }

    private static void zipCopy(final ZipFile in, final ZipOutputStream out, final DiSL disl, final boolean onlyClasses) {
        in.stream().forEachOrdered(entry -> {
            try {
                final boolean isDir = entry.isDirectory();
                final boolean isClass = entry.getName().endsWith(".class");
                if(isDir) {
                    out.putNextEntry(entry);
                    out.closeEntry();
                    return;
                }
                if(!isClass && onlyClasses) {
                    return;
                }
                byte[] bytes = new byte[long2int(entry.getSize(), "Zip entry too large")];
                try(final InputStream is = in.getInputStream(entry)) {
                    is.read(bytes);
                }
                if(isClass && disl != null) {
                    byte[] newBytes = disl.instrument(bytes);
                    if(newBytes != null) {
                        bytes = newBytes;
                        entry.setSize(newBytes.length);
                        entry.setCrc(crc32(bytes));
                    }
                }
                entry.setCompressedSize(-1);
                out.putNextEntry(entry);
                out.write(bytes);
                out.closeEntry();
            } catch(final DiSLException | IOException | RuntimeException | Error t) {
                System.err.printf("Error processing %s\n", entry.getName());
                t.printStackTrace(System.err);
                System.exit(1);
            }
        });
    }

    private static int long2int(long num, String msg) {
        if(num > Integer.MAX_VALUE)
            throw new ArithmeticException(msg);
        return (int) num;
    }
    
    private static long crc32(byte[] bytes) {
        CRC32 state = new CRC32();
        state.update(bytes);
        return state.getValue();
    }
}
