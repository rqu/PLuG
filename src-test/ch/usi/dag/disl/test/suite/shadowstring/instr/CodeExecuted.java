package ch.usi.dag.disl.test.suite.shadowstring.instr;

import ch.usi.dag.dislreserver.remoteanalysis.RemoteAnalysis;
import ch.usi.dag.dislreserver.shadow.ShadowObject;
import ch.usi.dag.dislreserver.shadow.ShadowString;


// NOTE that this class is not static anymore
public class CodeExecuted extends RemoteAnalysis {

    public static void receiveObject (final ShadowObject o) {
        if (o != null) {
            System.out.println (o.toString ());
        }
    }


    public static void receiveData (final ShadowString o) {
        if (o != null) {
            System.out.println (o.toString ());
        }
    }

    public static void empty () {
    }


    @Override
    public void atExit () {
    }


    @Override
    public void objectFree (final ShadowObject netRef) {
    }

}
