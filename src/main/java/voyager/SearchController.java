package voyager;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.RAMDirectory;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.store.Directory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import java.util.concurrent.*;
import java.util.logging.Logger;


@RestController
@RequestMapping("/api")
@CrossOrigin("*")

public class SearchController {
    static Directory directory = new RAMDirectory();
    static Analyzer analyzer = new StandardAnalyzer();
    static IndexWriterConfig config = new IndexWriterConfig(analyzer);
    static IndexWriter indexWriter;

    DirectoryReader indexReader = DirectoryReader.open(directory);
    IndexSearcher indexSearcher = new IndexSearcher(indexReader);
    QueryParser parser = new QueryParser("content", analyzer);


    private final static Logger log = Logger.getLogger(Writer.class.getName());

    static {
        try {
            indexWriter = new IndexWriter(directory, config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static LinkedBlockingQueue<Article> pages = new LinkedBlockingQueue<>();
    static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            4,
            4,
            2,
            TimeUnit.MINUTES,
            new LinkedBlockingQueue<>()
    );

    public SearchController() throws IOException {
    }

    public static String convertToURL(Path path){
        return path.toString().replace("|", "/");
    }

    public static void buildIndex() throws IOException, InterruptedException {
        log.info("Loading scraped web pages into queue");
        File folder = new File("collection");
        Path base = Paths.get("collection");

        for (final File fileEntry : folder.listFiles()) {
            String pageSource = FileUtils.readFileToString(fileEntry, StandardCharsets.UTF_8);
            Article article = new Article(pageSource, convertToURL(base.relativize(fileEntry.toPath())));
            pages.add(article);
        }
        log.info("Successfully loaded web pages into queue");

        for (int i = 0; i < 4; i++){
            threadPoolExecutor.execute(new Writer(indexWriter, pages));
        }

        threadPoolExecutor.shutdown();
        threadPoolExecutor.awaitTermination(5, TimeUnit.MINUTES);

        log.info("Number of docs in indexwriter: " + indexWriter.numDocs());

        indexWriter.close();
    }

    @GetMapping("/search")
    public List<Article> search( //TODO switch to List<Article>
            @RequestParam(required = false, defaultValue = "")  String query,
            @RequestParam(required = false, defaultValue = "")  Integer after) throws ParseException {
    }
}
