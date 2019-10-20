package com.cumulocity.agent.snmp.platform.pubsub.queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.cumulocity.agent.snmp.config.GatewayProperties;

@RunWith(MockitoJUnitRunner.class)
public class MeasurementQueueTest {

    @Mock
    private GatewayProperties gatewayProperties;

    private MeasurementQueue measurementQueue;

    private Path persistentFolderPath;


    @Before
    public void setUp() {
        Mockito.when(gatewayProperties.getGatewayIdentifier()).thenReturn(this.getClass().getSimpleName());

        persistentFolderPath = Paths.get(
                System.getProperty("user.home"),
                ".snmp",
                gatewayProperties.getGatewayIdentifier().toLowerCase(),
                "chronicle",
                "queues",
                "MEASUREMENT".toLowerCase());

        clearParentFolder();

        measurementQueue = Mockito.spy(new MeasurementQueue(gatewayProperties));
    }

    @After
    public void tearDown() {
        if(measurementQueue != null) {
            measurementQueue.close();
        }

        clearParentFolder();
    }

    private void clearParentFolder() {
        if(persistentFolderPath.toFile().exists()) {
            try {
                Files.list(persistentFolderPath).forEach(fileInTheFolder -> fileInTheFolder.toFile().delete());
            } catch (IOException e) {
            }
        }
    }

    @Test
    public void shouldCreateQueueWithCorrectName() {
        assertEquals("MEASUREMENT", measurementQueue.getName());
    }

    @Test
    public void shouldCreateRequiredPersistenceFolder() {
        assertTrue(measurementQueue.getPersistenceFolder().exists());
        assertEquals(persistentFolderPath.toString(), measurementQueue.getPersistenceFolder().getPath());
    }
}