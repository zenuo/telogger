import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;

/**
 * @author 袁臻
 * 2017/11/22 00:05
 */
public class Tester {
    @Test
    public void lines() throws IOException {
        final Process process = Runtime.getRuntime().exec("tail -f log");
        final InputStream inputStream = process.getInputStream();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
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
