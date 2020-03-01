package ru.hse.cs.java2020.task01;

import java.io.File;
import java.util.ArrayList;

public class Main {
    private static long getFolderSize(File file) {
        if (file == null) {
            return 0;
        }
        if (file.isFile()) {
            return file.length();
        }

        File[] filesList = file.listFiles();
        if (filesList == null) {
            return 0;
        }

        long size = 0;
        for (File to : filesList) {
            size += getFolderSize(to);
        }

        return size;
    }

    public static class TableRenderer {
        private ArrayList<String[]> data;
        private long[] sizes;

        public TableRenderer(int columns) {
            data = new ArrayList<>();
            sizes = new long[columns];
        }

        public void addRow(String[] row) {
            data.add(row.clone());

            for (int i = 0; i < sizes.length; ++i) {
                sizes[i] = Math.max(sizes[i], row[i].length());
            }
        }

        public String render() {
            StringBuilder table = new StringBuilder();

            for (String[] datum : data) {
                for (int i = 0; i < sizes.length; ++i) {
                    table.append(String.format(String.format("%%%ds ", sizes[i]), datum[i]));
                }
                table.append("\n");
            }

            return table.toString();
        }
    }

    public static void main(String[] args) {
        File mainFolder = new File(args[0]);
        File[] filesList = mainFolder.listFiles();

        if (!mainFolder.isDirectory() || filesList == null) {
            System.err.println("Error");
            return;
        }

        long totalSize = 0;
        long[] folderSizes = new long[filesList.length];
        for (int i = 0; i < filesList.length; ++i) {
            folderSizes[i] = getFolderSize(filesList[i]);
            totalSize += folderSizes[i];
        }

        TableRenderer table = new TableRenderer(5);
        for (int i = 0; i < filesList.length; ++i) {
            String itemsCount;
            File[] tmp = filesList[i].listFiles();
            if (tmp != null) {
                itemsCount = String.format("%d items", tmp.length);
            } else {
                itemsCount = "no items";
            }

            String[] row = {
                    String.format("%d.", i + 1),
                    String.format("%s |", filesList[i].getName()),
                    String.format("%d B |", folderSizes[i]),
                    String.format("%.2f%% |", (double) (folderSizes[i]) / totalSize * 100.0),
                    itemsCount,
            };

            table.addRow(row);
        }

        System.out.println(table.render());
    }
}
