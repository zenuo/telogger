import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
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

    @Test
    public void watch() throws IOException, InterruptedException {

        WatchService watcher = FileSystems.getDefault().newWatchService();

        final Path path = Paths.get("/home/user/project/omts/log");
        path.register(
                watcher,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY
        );
        while (true) {

            WatchKey key = watcher.take();

            for (WatchEvent<?> event : key.pollEvents()) {
                System.out.println(event.kind().toString() + ":" + event.context());
            }

            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }

    }

    @Test
    public void osname() {
        System.out.println(System.getProperty("os.name"));
    }

}
