package c8y.trackeragent.operations;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import c8y.trackeragent.TrackerAgent;
import c8y.trackeragent.TrackerDevice;
import c8y.trackeragent.TrackerPlatform;
import c8y.trackeragent.devicebootstrap.DeviceCredentials;
import c8y.trackeragent.utils.TrackerContext;

public class OperationDispatchers {

    private static final int THREAD_POOL_SIZE = 10;
    private static final long POLLING_DELAY = 5;
    private static final long POLLING_INTERVAL = 5;

    private final TrackerContext trackerContext;
    private final TrackerAgent trackerAgent;
    private final ScheduledExecutorService operationsExecutor;

    public OperationDispatchers(TrackerContext trackerContext, TrackerAgent trackerAgent) {
        this.trackerContext = trackerContext;
        this.trackerAgent = trackerAgent;
        this.operationsExecutor = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
    }

    public void start() {
        for (DeviceCredentials deviceCredentials : trackerContext.getDeviceCredentials()) {
            start(deviceCredentials.getImei());
        }
    }

    /**
     * TODO: call it after bootstraping new device
     */
    public void start(String imei) {
        TrackerDevice trackerDevice = trackerAgent.getOrCreateTrackerDevice(imei);
        TrackerPlatform devicePlatform = trackerContext.getDevicePlatform(imei);
        // Could be replace by device control notifications
        OperationDispatcher task = new OperationDispatcher(devicePlatform, trackerDevice);
        operationsExecutor.scheduleWithFixedDelay(task, POLLING_DELAY, POLLING_INTERVAL, SECONDS);
    }
}