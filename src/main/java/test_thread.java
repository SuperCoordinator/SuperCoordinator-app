import java.util.concurrent.atomic.AtomicInteger;

public class test_thread extends Thread {

    private int value;

    public test_thread(int value) {
        this.value = value;
    }

    @Override
    public void run(){

        value++;
        //System.out.println(Thread.currentThread().getName() + " value: " + value);

    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
