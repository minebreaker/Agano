package agano.messaging;

import agano.util.AganoException;
import com.google.common.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;

// TODO: GuavaのServiceを実装
public final class UdpServer implements Closeable {

    private static Logger logger = LoggerFactory.getLogger(UdpServer.class);

    private final DatagramChannel channel;
    private final SocketAddress address;
    private final EventBus eventBus;
    private final ExecutorService executorService;
    private final int bufferCapacity = 1024;

    public UdpServer(@Nonnull EventBus eventBus, @Nonnull SocketAddress address) {

        checkNotNull(eventBus);
        checkNotNull(address);

        this.eventBus = eventBus;
        this.address = address;

        try {
            this.channel = DatagramChannel.open();
            this.channel.socket().setReuseAddress(true);
            this.channel.configureBlocking(false);
            this.channel.socket().bind(address);
        } catch (IOException e) {
            logger.warn("Failed to initialize socket", e);
            throw new AganoException(e);
        }

        this.executorService = Executors.newSingleThreadExecutor();

    }

    public void start() throws Exception {
        executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                logger.info("Starts listening...");

                Selector selector = Selector.open();
                channel.register(selector, SelectionKey.OP_READ);
                while (selector.select() > 0) {
                    for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext(); ) {
                        SelectionKey key = i.next();
                        i.remove();
                        if (key.isValid() && key.isReadable()) {
                            logger.info("Received");
                            DatagramChannel acceptable = (DatagramChannel) key.channel();
                            ByteBuffer buf = ByteBuffer.allocate(bufferCapacity);
                            acceptable.receive(buf);
                            buf.flip();
                            eventBus.post(buf);
                        }
                    }
                }
                return null;
            }
        });
    }

    public void stop() {
        try {
            executorService.shutdown();
            channel.close();
        } catch (IOException e) {
            logger.warn("Failed to close socket", e);
            throw new AganoException(e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        stop();
        super.finalize();
    }

    @Override
    public void close() {
        stop();
    }

}