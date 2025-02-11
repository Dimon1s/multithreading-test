package ru.dkrotov;

import lombok.SneakyThrows;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    @SneakyThrows
    public static void main(String[] args) {
        File file = new File("/tmp/log1.txt");
        file.createNewFile();
        SynchronousQueue<Integer> queue = new SynchronousQueue<>();
        SynchronousQueue<Integer> fileWriteQueue = new SynchronousQueue<>();

        try (FileWriter writer = new FileWriter(file.getAbsoluteFile());
             FileReader reader = new FileReader(file.getAbsoluteFile());
             ExecutorService executorService = Executors.newFixedThreadPool(3)) {

            executorService.execute(() -> {
                for (int i = 0; i < 100; i++) {
                    writeToFile(queue, true, writer, fileWriteQueue);
                }
            });
            executorService.execute(() -> {
                for (int i = 0; i < 100; i++) {
                    writeToFile(queue, false, writer, fileWriteQueue);
                }
            });
            executorService.execute(() -> {
                for (int i = 0; i < 200; i++) {
                    readFromFile(queue, fileWriteQueue, reader);
                }
            });
        }
    }

    @SneakyThrows
    private static void readFromFile(SynchronousQueue<Integer> queue, SynchronousQueue<Integer> fileWriteQueue, FileReader reader) {
        queue.take();
        Integer length = fileWriteQueue.take();
        char[] arr = new char[length];
        reader.read(arr);
        System.out.println(new String(arr));
    }

    @SneakyThrows
    private static void writeToFile(SynchronousQueue<Integer> queue, boolean evenValue, FileWriter writer, SynchronousQueue<Integer> fileWriteQueue) {
        int value = getRandomInt(evenValue);
        queue.put(value);
        String s = String.valueOf(value);
        writer.write(s);
        writer.flush();
        fileWriteQueue.put(s.length());
    }

    private static int getRandomInt(boolean evenValue) {
        int i = ThreadLocalRandom.current().nextInt();
        if (evenValue) {
            return i / 2;
        } else {
            return 1 + i / 2;
        }
    }
}
