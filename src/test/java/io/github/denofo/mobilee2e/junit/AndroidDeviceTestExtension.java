package io.github.denofo.mobilee2e.junit;

import io.github.denofo.mobilee2e.device.android.AndroidDevice;
import io.github.denofo.mobilee2e.device.android.AndroidDeviceContext;
import io.github.denofo.mobilee2e.device.android.AndroidDeviceProvider;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

public final class AndroidDeviceTestExtension
        implements TestTemplateInvocationContextProvider {

    @Override
    public boolean supportsTestTemplate(
            ExtensionContext context
    ) {
        return context.getTestMethod()
                .map(method ->
                        method.isAnnotationPresent(AndroidDeviceTest.class)
                )
                .orElse(false);
    }

    @Override
    public Stream<TestTemplateInvocationContext>
    provideTestTemplateInvocationContexts(
            ExtensionContext context
    ) {
        return AndroidDeviceProvider.discover()
                .stream()
                .map(this::createInvocationContext);
    }

    private TestTemplateInvocationContext createInvocationContext(
            AndroidDevice device
    ) {
        return new TestTemplateInvocationContext() {

            @Override
            public String getDisplayName(int invocationIndex) {
                return device.displayName();
            }

            @Override
            public List<Extension> getAdditionalExtensions() {
                return List.of(
                        new AndroidDeviceInvocationExtension(device)
                );
            }
        };
    }

    private static final class AndroidDeviceInvocationExtension
            implements BeforeEachCallback, AfterEachCallback {

        private static final ConcurrentMap<String, ReentrantLock>
                DEVICE_LOCKS = new ConcurrentHashMap<>();

        private final AndroidDevice device;
        private final ReentrantLock deviceLock;
        private boolean lockAcquired;

        private AndroidDeviceInvocationExtension(
                AndroidDevice device
        ) {
            this.device = device;
            this.deviceLock = DEVICE_LOCKS.computeIfAbsent(
                    device.udid(),
                    ignored -> new ReentrantLock(true)
            );
        }

        @Override
        public void beforeEach(
                ExtensionContext context
        ) throws InterruptedException {
            deviceLock.lockInterruptibly();
            lockAcquired = true;
            AndroidDeviceContext.set(device);
        }

        @Override
        public void afterEach(
                ExtensionContext context
        ) {
            try {
                AndroidDeviceContext.clear();
            } finally {
                if (lockAcquired) {
                    lockAcquired = false;
                    deviceLock.unlock();
                }
            }
        }
    }
}
