package ch.usi.dag.disl.test.suite.scope.app;

public class TargetClass {
	
    private void complete(String text, boolean b1, boolean b2) {
        System.out.println(text + " " + b1 + " " + b2);
    }

    public static void main(String[] args) {
        new TargetClass().complete("test", true, false);
    }
}