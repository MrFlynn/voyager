package voyager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.logging.Logger;

@SpringBootApplication
public class LuceneSearcherApplication {
    public static void start(String directory, Integer serverPort, Integer threads)
            throws IOException, InterruptedException {
        // Handle ctrl-c event without leaking memory.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            SearchController.threadPoolExecutor.shutdown();

            try {
                SearchController.directory.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        SearchController.buildIndex(directory, threads);

        SpringApplication.run(LuceneSearcherApplication.class, "--server.port=" + serverPort);
        SearchController.indexWriter.close();
        SearchController.directory.close();

        Thread.currentThread().join();
    }
}
