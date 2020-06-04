package voyager;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

public class Writer implements Runnable {
    private LinkedBlockingQueue<Article> pages;
    private IndexWriter indexWriter;

    private final static Logger log = Logger.getLogger(Writer.class.getName());

    public Writer(IndexWriter indexWriter, LinkedBlockingQueue<Article>  pages){
        this.indexWriter = indexWriter;
        this.pages = pages;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Article article = this.pages.poll();
                if (article != null){
                    Source source = new Source(article.pageSource);
                    source.fullSequentialParse();

                    String title = "";
                    if (source.getFirstElement("title") != null)
                        title = source.getFirstElement("title").getContent().toString();
                    else
                        title = "";

                    TextExtractor text = new TextExtractor(source);
                    Document doc = new Document();

                    doc.add(new Field("content", text.toString(), TextField.TYPE_STORED));
                    doc.add(new Field("title", title, StoredField.TYPE));
                    doc.add(new Field("URL", article.URL, StoredField.TYPE));

                    indexWriter.addDocument(doc);
                } else break;
            }
        } catch (IOException e) {
            log.info("Thread exiting...");
        }
    }
}
