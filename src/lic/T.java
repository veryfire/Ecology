package lic;

public class T implements Runnable {
    private String[] arr;

    @Override
    public void run() {
        if (arr.length > 0) {
            System.out.println(arr);
        }
    }

    public String[] getArr() {
        return arr;
    }

    public void setArr(String[] arr) {
        this.arr = arr;
    }
}
