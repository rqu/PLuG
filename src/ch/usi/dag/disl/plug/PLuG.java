package ch.usi.dag.disl.plug;

import ch.usi.dag.disl.DiSL;
import ch.usi.dag.disl.exception.DiSLException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class PLuG {

    @SuppressWarnings("ConvertToTryWithResources")
    public static void main(String[] args) throws DiSLException, IOException {
        if(args.length != 1) {
            System.err.println("Expected exactly one argument");
            System.exit(1);
            return;
        }
        final ZipFile in = new ZipFile(args[0]);
        final ZipOutputStream out = new ZipOutputStream(System.out);
        final DiSL disl = DiSL.init();
        
        in.stream().forEach(entry -> {
            try {
                if(entry.isDirectory()) {
                    out.putNextEntry(entry);
                    out.closeEntry();
                    return;
                }
                byte[] bytes = new byte[long2int(entry.getSize(), "Zip entry too large")];
                try(final InputStream is = in.getInputStream(entry)) {
                    is.read(bytes);
                }
                if(entry.getName().endsWith(".class")) {
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
        
        disl.terminate();
        out.close();
        in.close();
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
