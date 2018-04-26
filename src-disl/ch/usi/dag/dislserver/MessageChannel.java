package ch.usi.dag.dislserver;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

final class MessageChannel implements Closeable {

    private static final int __HEADER_LENGTH__ = 3 * Integer.BYTES;

    //

    private final SocketChannel __socket;

    private final ByteBuffer __head = __allocateDirect (4096);

    private final ByteBuffer [] __sendBuffers = new ByteBuffer [] {
        __head, null, null
    };

    //

    private ByteBuffer __body = __allocateDirect (128 * 1024);

    //

    public MessageChannel (final SocketChannel socket) {
        __socket = socket;
    }

    //

    public Message recvMessage () throws IOException {
        //
        // request protocol:
        //
        // java int - request flags
        // java int - control data length (cdl)
        // java int - payload data length (pdl)
        // bytes[cdl] - control data (contains class name)
        // bytes[pdl] - payload data (contains class code)
        //

        __head.clear ();
        __bufferRecvFrom (__socket, __HEADER_LENGTH__, __head);

        //

        __head.rewind ();

        final int flags = __head.getInt ();
        final int controlLength = __head.getInt ();
        final int payloadLength = __head.getInt ();

        //

        final int expectedLength = controlLength + payloadLength;
        __ensureBodyCapacity (expectedLength);

        __body.clear ();
        __bufferRecvFrom (__socket, expectedLength, __body);

        //

        __body.rewind ();

        final byte [] control = new byte [controlLength];
        __body.get (control);

        final byte [] payload = new byte [payloadLength];
        __body.get (payload);

        return new Message (flags, control, payload);
    }


    private void __bufferRecvFrom (
        final SocketChannel sc, final int length, final ByteBuffer buffer
    ) throws IOException {
        buffer.limit (buffer.position () + length);
        while (buffer.hasRemaining ()) {
            final int bytesRead = sc.read (buffer);
            if (bytesRead < 0) {
                throw new EOFException ("unexpected end of stream");
            }
        }
    }


    private void __ensureBodyCapacity (final int requiredCapacity) {
        if (__body.capacity () < requiredCapacity) {
            __body = __allocateDirect (__align (requiredCapacity, 12));
        }
    }

    private static int __align (final int value, final int bits) {
        final int mask = -1 << bits;
        final int fill = (1 << bits) - 1;
        return (value + fill) & mask;
    }

    //

    public void sendMessage (final Message message) throws IOException {
        //
        // response protocol:
        //
        // java int - response flags
        // java int - control data length (cdl)
        // java int - payload data length (pdl)
        // bytes[cdl] - control data (nothing, or error message)
        // bytes[pdl] - payload data (instrumented class code)
        //

        __head.clear ();

        __head.putInt (message.flags ());

        final int controlLength = message.control ().length;
        __head.putInt (controlLength);

        final int payloadLength = message.payload ().length;
        __head.putInt (payloadLength);

        //

        __sendBuffers [1] = ByteBuffer.wrap (message.control ());
        __sendBuffers [2] = ByteBuffer.wrap (message.payload ());

        //

        __head.flip ();

        do {
            __socket.write (__sendBuffers);
        } while (__sendBuffers [2].hasRemaining ());
    }


    @Override
    public void close () throws IOException {
        __socket.close ();
    }

    //

    private static ByteBuffer __allocateDirect (final int capacity) {
        return ByteBuffer.allocateDirect (capacity).order (ByteOrder.BIG_ENDIAN);
    }

}
