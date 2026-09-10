package io.github.denofo.mobilee2e.junit;

import io.github.denofo.mobilee2e.tests.android.BaseAndroidTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.opentest4j.TestAbortedException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@Execution(ExecutionMode.SAME_THREAD)
class FailureArtifactsExtensionTest {
    private static final List<String> events = new ArrayList<>();
    private static final AssertionError original = new AssertionError("original test failure");
    private static String mode;

    @Test
    void capturesBeforeTeardownAndPreservesOriginalFailure() {
        run("test-failure", 1, 0);
        assertEquals(List.of("setup", "test", "capture", "teardown"), events);
    }

    @Test
    void capturesSetupFailureBeforeTeardown() {
        run("setup-failure", 1, 0);
        assertEquals(List.of("setup", "capture", "teardown"), events);
    }

    @Test
    void captureFailureDoesNotReplaceOriginalOrPreventTeardown() {
        run("capture-failure", 1, 0);
        assertEquals(List.of("setup", "test", "capture", "teardown"), events);
    }

    @Test
    void successfulTestDoesNotCapture() {
        run("success", 0, 0);
        assertEquals(List.of("setup", "test", "teardown"), events);
    }

    @Test
    void abortedTestDoesNotCapture() {
        run("abort", 0, 1);
        assertEquals(List.of("setup", "test", "teardown"), events);
    }

    private void run(String scenario, int failed, int aborted) {
        mode = scenario;
        events.clear();
        var results = EngineTestKit.engine("junit-jupiter")
                .selectors(selectClass(Fixture.class))
                .configurationParameter("junit.jupiter.execution.parallel.enabled", "false")
                .execute();
        results.testEvents().assertStatistics(stats -> stats.started(1)
                .failed(failed).aborted(aborted).succeeded(1 - failed - aborted));
        results.testEvents().failed().stream().forEach(event ->
                assertSame(original, event.getRequiredPayload(
                        org.junit.platform.engine.TestExecutionResult.class)
                        .getThrowable().orElseThrow()));
    }

    // Exercise inherited extension registration and real JUnit lifecycle without a device.
    public static class Fixture extends BaseAndroidTest {
        @Override
        @BeforeEach
        public void setUp() {
            events.add("setup");
            if (mode.equals("setup-failure")) throw original;
        }

        @Test
        void scenario() {
            events.add("test");
            if (mode.equals("abort")) throw new TestAbortedException("aborted");
            if (!mode.equals("success")) throw original;
        }

        @Override
        public void captureFailureArtifacts(String label) {
            events.add("capture");
            assertTrue(label.contains("Fixture-scenario"));
            if (mode.equals("capture-failure")) throw new IllegalStateException("disk full");
        }

        @Override
        @AfterEach
        public void tearDown() {
            events.add("teardown");
        }
    }
}
