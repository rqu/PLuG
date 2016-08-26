package ch.usi.dag.disl.test.suite.shadowstring.instr;

import ch.usi.dag.disl.annotation.AfterReturning;
import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.marker.BodyMarker;


public class DiSLClass {

    @Before (marker = BodyMarker.class, scope = "TargetClass.main")
    public static void onMethodEntrance () {
        CodeExecutedRE.sendObject ();
    }

    @Before (marker = BodyMarker.class, scope = "TargetClass.empty")
    public static void flush () {
        CodeExecutedRE.empty ();
    }


    @AfterReturning (marker = BodyMarker.class, scope = "TargetClass.main")
    public static void onMethodExit () {
        CodeExecutedRE.sendData ();
    }

}
