package io.github.denofo.mobilee2e.device.android;

public final class AndroidDeviceContext {

    private static final ThreadLocal<AndroidDevice> CURRENT_DEVICE =
            new ThreadLocal<>();

    private AndroidDeviceContext() {
    }

    public static void set(AndroidDevice device) {
        CURRENT_DEVICE.set(device);
    }

    public static AndroidDevice get() {
        return CURRENT_DEVICE.get();
    }

    public static AndroidDevice getRequired() {
        AndroidDevice device = CURRENT_DEVICE.get();

        if (device == null) {
            throw new IllegalStateException(
                    "Android device is not assigned to the current test thread."
            );
        }

        return device;
    }

    public static void clear() {
        CURRENT_DEVICE.remove();
    }
}
