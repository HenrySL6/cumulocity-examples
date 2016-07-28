package c8y.trackeragent.server;

import static java.util.Arrays.asList;
import static java.util.Collections.synchronizedList;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import c8y.trackeragent.context.OperationContext;
import c8y.trackeragent.server.ConnectionDetails;
import c8y.trackeragent.server.ConnectionsContainer;
import c8y.trackeragent.server.TrackerServer;
import c8y.trackeragent.server.TrackerServerEventHandler;
import c8y.trackeragent.server.TrackerServerEvent.ReadDataEvent;
import c8y.trackeragent.tracker.ConnectedTracker;
import c8y.trackeragent.tracker.ConnectedTrackerFactory;

public class TrackerServerTestSupport {
    
    private static final Logger logger = LoggerFactory.getLogger(TrackerServerTestSupport.class);

    protected static final int PORT = 5100;
    protected static final Charset CHARSET = Charset.forName("US-ASCII");
    
    private TrackerServer server;
    private final ExecutorService executorService = newFixedThreadPool(100);
    protected CountDownLatch reportExecutorLatch;
    protected final List<ConnectedTrackerImpl> executors = synchronizedList(new ArrayList<ConnectedTrackerImpl>());
    
    @Before
    public void before() throws Exception {
        reportExecutorLatch = new CountDownLatch(0);
        TrackerServerEventHandler eventHandler = new TrackerServerEventHandler(new TestConnectedTrackerFactoryImpl(), new ConnectionsContainer());
        eventHandler.init();
        server = new TrackerServer(eventHandler);
        server.start(PORT);
        executorService.execute(server);
    }
    
    @After
    public void after() throws IOException {
        server.close();
    }
    
    protected SocketWriter newWriter() throws Exception {
        SocketWriter writer = new SocketWriter();
        executorService.execute(writer);
        return writer;
    }
    
    protected void assertThatReportsHandled(String... reports) {
        Object[] expected = new Object[reports.length];
        for (int index = 0; index < reports.length; index++) {
            expected[index] = new ConnectedTrackerImpl(reports[index]);
        }
        assertThat(executors).contains(expected);
    }
    
    protected class SocketWriter implements Runnable {

        volatile Deque<String> toW = new ArrayDeque<String>();
        Socket client;

        public SocketWriter() throws Exception {
            client = new Socket("localhost", PORT);
        }

        void push(String text) throws Exception {
            toW.addLast(text);
            Thread.sleep(1);
        }
                
        void stop() throws Exception {
            client.close();
            Thread.sleep(1);
        }

        @Override
        public void run() {
            while (true) {
                if (!toW.isEmpty()) {
                    String toWrite = toW.removeFirst();
                    write(toWrite);
                }
            }
        }

        private void write(String toWrite) {
            for (byte b : toWrite.getBytes(CHARSET)) {
                try {
                    client.getOutputStream().write(b);
                    Thread.sleep(1);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }

    }
    
    protected class TestConnectedTrackerFactoryImpl implements ConnectedTrackerFactory {
        
        @Override
        public ConnectedTracker create(ReadDataEvent readData) {
            ConnectedTrackerImpl result = new ConnectedTrackerImpl();
            logger.info("Created executor for data " + new String(readData.getData(), CHARSET));
            executors.add(result);
            logger.info("Total executors " + executors.size());
            return result;
        }
    }
    
    protected class ConnectedTrackerImpl implements ConnectedTracker {
        
        private final List<String> processed;
        
        public ConnectedTrackerImpl(String... processed) {
            this.processed = new ArrayList<String>();
            this.processed.addAll(asList(processed));
        }
        
        @Override
        public void executeOperation(OperationContext operation) throws Exception {
            // TODO Auto-generated method stub
            
        }
        @Override
        public void executeReport(ConnectionDetails connectionDetails, String report) {
            logger.info("Handled report: \'{}\'", report);
            processed.add(report);
            reportExecutorLatch.countDown();
        }

        @Override
        public String getReportSeparator() {
            return ";";
        }
        
        public List<String> getProcessed() {
            return processed;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((processed == null) ? 0 : processed.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ConnectedTrackerImpl other = (ConnectedTrackerImpl) obj;
            if (processed == null) {
                if (other.processed != null)
                    return false;
            } else if (!processed.equals(other.processed))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return processed.toString();
        }
    }

    
}
