package io.github.denofo.mobilee2e.device.android;

public record AndroidDevice(
        String udid,
        String name,
        int systemPort
) {

    public String displayName() {
        return name + " | " + udid;
    }
}
