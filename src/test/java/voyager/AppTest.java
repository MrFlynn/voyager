package voyager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    App app;
    CommandLine cmd;

    @BeforeEach
    void setup() {
        this.app = new App();
        this.cmd = new CommandLine(this.app);
    }

    @Test
    void testCompleteParams() {
        int exitStatus = cmd.execute("/workdir", "--port=5001");
        assertAll(
                () -> assertEquals(0, exitStatus),
                () -> assertEquals(5001, app.getPort()),
                () -> assertEquals("/workdir", app.getHtmlDirectory().toString())
        );
    }

    @Test
    void testIncompleteParams() {
        int exitStatus = cmd.execute();
        assertEquals(2, exitStatus);
    }

    @Test
    void testDefaultPort() {
        int exitStatus = cmd.execute("/workdir");
        assertAll(
                () -> assertEquals(0, exitStatus),
                () -> assertEquals(5000, app.getPort())
        );
    }
}
