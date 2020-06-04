package voyager;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@Command(
        name = "voyager",
        mixinStandardHelpOptions = true
)
public class App implements Callable<Integer> {
    @Option(
            names = {"-p", "--port"},
            description = "Which port the HTTP server should run on."
    )
    private Integer port = 5000;

    @Parameters(
            index = "0",
            description = "A directory containing HTML files used to build index."
    )
    private static String htmlDirectory;

    public static Integer getPort() {
        return port;
    }

    public static String getHtmlDirectory() {
        return htmlDirectory;
    }

    public Integer getPort() {
        return this.port;
    }

    public Path getHtmlDirectory() {
        return this.htmlDirectory;
    }

    @Override
    public Integer call() throws Exception {
        LuceneSearcherApplication.start(App.getHtmlDirectory(), App.getPort());
        return 0;
    }

    public static void main(String ...args) {
        int exitStatus;

        try {
            exitStatus = new CommandLine(new App()).execute(args);
        } catch (Exception e) {
            exitStatus = 1;
            System.err.println(e.toString());
        }

        System.exit(exitStatus);
    }
}
