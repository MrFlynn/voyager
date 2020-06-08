package voyager;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

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

        new SpringApplicationBuilder(LuceneSearcherApplication.class)
                .bannerMode(Banner.Mode.OFF)
                .logStartupInfo(false)
                .initializers(new LoggingInitializer())
                .properties(String.format("server.port=%s", serverPort))
                .build()
                .run();

        SearchController.indexWriter.close();
        SearchController.directory.close();

        Thread.currentThread().join();
    }

    private static class LoggingInitializer implements ApplicationContextInitializer {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            LoggerContext c = (LoggerContext) LoggerFactory.getILoggerFactory();

            // Disable annoying messages in Spring Boot.
            c.getLogger("org.springframework").setLevel(Level.WARN);
            c.getLogger("org.springframework.web").setLevel(Level.INFO);
        }
    }
}
