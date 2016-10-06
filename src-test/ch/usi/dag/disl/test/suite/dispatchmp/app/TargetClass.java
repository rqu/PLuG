package ch.usi.dag.disl.test.suite.dispatchmp.app;


public class TargetClass {

    protected static int THREAD_COUNT = 3;

    public static class TCTask implements Runnable {

        @Override
        public void run() {
            final int COUNT = 2000000;
            final TargetClass ta[] = new TargetClass[COUNT];

            for (int i = 0; i < COUNT; ++i) {
                ta[i] = new TargetClass();
            }

            System.out.println("Allocated " + COUNT + " objects");
        }

    }

    public static void main(final String[] args) throws InterruptedException {
        final TCTask task = new TCTask();

        for (int i = 0; i < THREAD_COUNT; ++i) {
            new Thread(task).start();
        }
    }

}
