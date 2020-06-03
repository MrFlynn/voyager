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
    private static Integer port = 5000;

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

    @Override
    public Integer call() throws Exception {
        return 0;
    }

    public static void main(String ...args) {
        int exitStatus;

        try {
            exitStatus = new CommandLine(new App()).execute(args);
            LuceneSearcherApplication.start(App.getHtmlDirectory(), App.getPort());
        } catch (Exception e) {
            exitStatus = 1;
            System.err.println(e.toString());
        }

        System.exit(exitStatus);
    }
}
