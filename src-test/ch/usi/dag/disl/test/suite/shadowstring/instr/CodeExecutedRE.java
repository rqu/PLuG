package ch.usi.dag.disl.test.suite.shadowstring.instr;

import ch.usi.dag.dislre.REDispatch;

// Optimally, this class is automatically created on analysis machine
// and redefines during loading the CodeExecuted class on the client vm


// Even more optimally, this is automatically generated native class with same
// functionality
public class CodeExecutedRE {

    private static short RO = REDispatch.registerMethod (
        "ch.usi.dag.disl.test.suite.shadowstring.instr.CodeExecuted.receiveObject");
    private static short RD = REDispatch.registerMethod (
    "ch.usi.dag.disl.test.suite.shadowstring.instr.CodeExecuted.receiveData");
    private static short EM = REDispatch.registerMethod (
    "ch.usi.dag.disl.test.suite.shadowstring.instr.CodeExecuted.empty");

    public static final String DATA = "shadowstring";

    public static void sendObject () {
        REDispatch.analysisStart (RO);
        REDispatch.sendObject (DATA);
        REDispatch.analysisEnd ();
    }

    public static void sendData () {
        REDispatch.analysisStart (RD);
        REDispatch.sendObjectPlusData (DATA);
        REDispatch.analysisEnd ();
    }

    public static void empty () {
        REDispatch.analysisStart (EM);
        REDispatch.analysisEnd ();
    }

}
