package org.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class Main {
    public static void main(String[] args) throws IOException {

        try (Selector selector = Selector.open();
             ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            serverSocketChannel.bind(new InetSocketAddress(8181));
            // async channel
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                int readyKeyCount = selector.select();
                if (readyKeyCount == 0) {
                    continue;
                }

                Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                while (selectionKeyIterator.hasNext()) {
                    SelectionKey key = selectionKeyIterator.next();
                    try {
                        if (key.isAcceptable()) {
                            SocketChannel channel = serverSocketChannel.accept();
                            if (channel != null) {
                                // async channel
                                channel.configureBlocking(false);
                                channel.register(selector, SelectionKey.OP_READ);
                            }
                            continue;
                        }

                        if (key.isReadable()) {
                            // read the first message and then close the channel
                            try (SocketChannel channel = (SocketChannel) key.channel()) {
                                ByteBuffer buffer = ByteBuffer.allocate(4048);
                                channel.read(buffer);
                                buffer.flip();
                                String data = new String(buffer.array(), buffer.position(), buffer.remaining());
                                System.out.println("Received: " + data);
                                buffer.clear();
                            }
                        }
                    } finally {
                        selectionKeyIterator.remove();
                    }
                }
            }
        }
    }
}