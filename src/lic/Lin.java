package lic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Lin {
    public static void main(String[] s) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        T t = new T();
        String[] arr = {"A", "B"};
        executorService.submit(t);

    }
}
