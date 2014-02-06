package ch.usi.dag.disl.test.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ClientServerRunner extends Runner {

    private Job __client;

    private Job __server;

    private boolean clientOutNull;

    private boolean clientErrNull;

    private boolean serverOutNull;

    private boolean serverErrNull;

    //

    public ClientServerRunner (final Class <?> testClass) {
        super (testClass);

        clientOutNull = true;
        clientErrNull = true;
        serverOutNull = true;
        serverErrNull = true;
    }


    @Override
    protected void _start (
        final File testInstrJar, final File testAppJar
    ) throws IOException {
        __server = __startServer (testInstrJar);

        _INIT_TIME_LIMIT_.softSleep ();

        if (! __server.isRunning ()) {
            throw new IOException ("server failed: "+ __server.getError ());
        }

        //

        __client = __startClient (testInstrJar, testAppJar);
    }


    private Job __startClient (
        final File testInstrJar, final File testAppJar
    ) throws IOException {
        final List <String> clientCommand = newLinkedList (
            _JAVA_COMMAND_,
            String.format ("-agentpath:%s", _DISL_AGENT_LIB_),
            String.format ("-javaagent:%s", _DISL_AGENT_JAR_),
            String.format (
                "-Xbootclasspath/a:%s",
                makeClassPath (_DISL_AGENT_JAR_, testInstrJar)
            )
        );

        clientCommand.addAll (propertiesStartingWith ("disl."));
        clientCommand.addAll (Arrays.asList (
            "-jar", testAppJar.toString ()
        ));

        //

        final Job result = new Job (clientCommand);
        result.start ();
        return result;
    }


    private Job __startServer (final File testInstrJar) throws IOException {
        final List <String> serverCommand = newLinkedList (
            _JAVA_COMMAND_,
            "-cp", makeClassPath (testInstrJar, _DISL_SERVER_JAR_)
        );

        serverCommand.addAll (propertiesStartingWith ("dislserver."));
        serverCommand.add (_DISL_SERVER_MAIN_CLASS_.getName ());

        //

        final Job result = new Job (serverCommand);
        result.start ();
        return result;
    }


    @Override
    protected boolean _waitFor (final Duration duration) {
        boolean finished = true;
        finished = finished & __client.waitFor (duration);
        finished = finished & __server.waitFor (duration);
        return finished;
    }


    public void assertIsStarted () {
        assertTrue ("client not started", __client.isStarted ());
        assertTrue ("server not started", __server.isStarted ());
    }


    public void assertIsFinished () {
        assertTrue ("client not finished", __client.isFinished ());
        assertTrue ("server not finished", __server.isFinished ());
    }


    public void assertIsSuccessful () {
        assertTrue ("client failed", __client.isSuccessful ());
        assertTrue ("server failed", __server.isSuccessful ());
    }


    public void destroyIfRunningAndFlushOutputs () throws IOException {
        _destroyIfRunningAndDumpOutputs (__client, "client");
        _destroyIfRunningAndDumpOutputs (__server, "server");
    }


    public void assertClientOut (final String fileName) throws IOException {
        clientOutNull = false;
        assertEquals (
            "client out does not match",
            _readResource (fileName), __client.getOutput ()
        );
    }


    public void assertClientOutNull () throws IOException {
        clientOutNull = false;
        assertEquals ("client out does not match", "", __client.getOutput ());
    }


    public void assertClientErr (final String fileName) throws IOException {
        clientErrNull = false;
        assertEquals (
            "client err does not match",
            _readResource (fileName), __client.getError ()
        );
    }


    public void assertClientErrNull () throws IOException {
        clientErrNull = false;
        assertEquals ("client err does not match", "", __client.getError ());
    }


    public void assertServerOut (final String fileName) throws IOException {
        serverOutNull = false;
        assertEquals (
            "server out does not match",
            _readResource (fileName), __server.getOutput ()
        );
    }


    public void assertServerOutNull () throws IOException {
        serverOutNull = false;
        assertEquals ("server out does not match", "", __server.getOutput ());
    }


    public void assertServerErr (final String fileName) throws IOException {
        serverErrNull = false;
        assertEquals (
            "server err does not match",
            _readResource (fileName), __server.getError ()
        );
    }


    public void assertServerErrNull () throws IOException {
        serverErrNull = false;
        assertEquals ("server err does not match", "", __server.getError ());
    }


    public void assertRestOutErrNull () throws IOException {
        if (clientOutNull) {
            assertClientOutNull ();
        }
        if (clientErrNull) {
            assertClientErrNull ();
        }
        if (serverOutNull) {
            assertServerOutNull ();
        }
        if (serverErrNull) {
            assertServerErrNull ();
        }
    }


    public void destroy () {
        if (__client != null) {
            __client.destroy ();
        }
        if (__server != null) {
            __server.destroy ();
        }
    }
}
