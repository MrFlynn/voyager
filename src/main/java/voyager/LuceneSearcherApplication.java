package voyager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class LuceneSearcherApplication {

    public static void start(String directory, Integer serverPort) throws IOException, InterruptedException {
        SearchController.buildIndex(directory);

        SpringApplication.run(LuceneSearcherApplication.class, "--server.port=" + serverPort);
        SearchController.indexWriter.close();
        SearchController.directory.close();

        Thread.currentThread().join();
    }
}
