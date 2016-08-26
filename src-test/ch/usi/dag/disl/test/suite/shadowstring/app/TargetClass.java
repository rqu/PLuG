package ch.usi.dag.disl.test.suite.shadowstring.app;
public class TargetClass {

    static void empty () {
    }

	public static void main(final String[] args) throws InterruptedException {
	    for (int i = 0; i< 20000; i ++) {
	        empty();
	    }
	}

}
