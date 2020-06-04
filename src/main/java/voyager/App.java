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

    @Option(
            names = {"--test"},
            description = "Skip execution and run tests.",
            hidden = true
    )
    private boolean test;

    @Option(
            names = {"-t", "--threads"},
            description = "Number of threads to run application with."
    )
    private Integer threads = 4;

    @Parameters(
            index = "0",
            description = "A directory containing HTML files used to build index."
    )
    private String htmlDirectory;

    public Integer getPort() {
        return port;
    }

    public String getHtmlDirectory() {
        return htmlDirectory;
    }

    public Integer getThreads() {
        return threads;
    }

    @Override
    public Integer call() throws Exception {
        if (!this.test) {
            LuceneSearcherApplication.start(this.getHtmlDirectory(), this.getPort(), this.getThreads());
        }

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
