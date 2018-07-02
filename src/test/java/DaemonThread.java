/**
 * From https://stackoverflow.com/questions/2213340/what-is-daemon-thread-in-java
 */
public class DaemonThread extends Thread {
    public static void main(String[] args) {
        System.out.println("Entering main Method");

        DaemonThread t = new DaemonThread();
        t.setDaemon(true);
        t.start();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException x) {
            x.printStackTrace();
        }

        System.out.println("Leaving main method");
    }

    public void run() {
        System.out.println("Entering run method");

        try {
            System.out.println("In run Method: currentThread() is" + Thread.currentThread());

            while (true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException x) {
                    break;
                }

                System.out.println("In run method: woke up again");
            }
        } finally {
            System.out.println("Leaving run Method");
        }
    }
}
