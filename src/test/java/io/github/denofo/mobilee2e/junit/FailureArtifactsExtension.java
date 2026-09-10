package io.github.denofo.mobilee2e.junit;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.LifecycleMethodExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.opentest4j.TestAbortedException;

public final class FailureArtifactsExtension implements TestExecutionExceptionHandler,
        LifecycleMethodExecutionExceptionHandler {

    public interface Source {
        void captureFailureArtifacts(String label) throws Exception;
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable failure)
            throws Throwable {
        captureAndRethrow(context, failure);
    }

    @Override
    public void handleBeforeEachMethodExecutionException(ExtensionContext context,
                                                        Throwable failure) throws Throwable {
        captureAndRethrow(context, failure);
    }

    private void captureAndRethrow(ExtensionContext context, Throwable failure) throws Throwable {
        // These handlers run before @AfterEach closes the Appium session.
        if (!(failure instanceof TestAbortedException)) {
            try {
                if (context.getRequiredTestInstance() instanceof Source source) {
                    source.captureFailureArtifacts(context.getRequiredTestClass().getSimpleName()
                            + "-" + context.getRequiredTestMethod().getName());
                }
            } catch (Exception | AssertionError captureFailure) {
                System.err.println("Could not collect failure artifacts ("
                        + captureFailure.getClass().getSimpleName() + ")");
            }
        }
        throw failure;
    }
}
