import org.junit.Test;

import java.io.*;
import java.time.LocalDateTime;

/**
 * 测试类
 *
 * @author 袁臻
 * 2017/11/22 00:05
 */
public class Tester {

    private Process process = Runtime.getRuntime().exec("tail -f log");

    private BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

    public Tester() throws IOException {
    }

    @Test
    public void print() throws IOException {
        reader.lines().forEach(System.out::println);
    }

    @Test
    public void log() throws IOException, InterruptedException {
        final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("log", false)));
        for (int i = 0; i < 100; i++) {
            bufferedWriter.write(LocalDateTime.now().toString() + "\n");
            bufferedWriter.flush();
            Thread.sleep(1000L);
        }
    }
}
