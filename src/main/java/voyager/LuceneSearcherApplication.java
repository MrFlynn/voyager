package voyager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

@SpringBootApplication
public class LuceneSearcherApplication {

    public static void main(String[] args) throws IOException, InterruptedException {
        SearchController.buildIndex();

        SpringApplication.run(LuceneSearcherApplication.class, args);
    }
}
