package ru.hse.cs.java2020.task01;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.StringJoiner;
import java.util.TreeSet;

public class Main {
    public static class DiskAnalyzer {
        private final long topCount = 10;
        private final int folderTableColumnsNum = 5;
        private final int biggestFilesTableColumnsNum = 3;
        private final double bytesInKiB = 1024;
        private final double percent = 100;

        private File mainFolder;
        private File[] filesList;
        private long totalSize = 0;
        private final long[] folderSizes;
        private TreeSet<File> biggestFiles = new TreeSet<>(new FileSizeComparator());

        static class FileSizeComparator implements Comparator<File> {
            @Override
            public int compare(File file1, File file2) {
                return -Long.compare(file1.length(), file2.length());
            }
        }

        private void checkFile(File f) {
            biggestFiles.add(f);

            if (biggestFiles.size() > topCount) {
                biggestFiles.remove(biggestFiles.last());
            }
        }

        public DiskAnalyzer(String path) throws IllegalArgumentException {
            mainFolder = new File(path);
            filesList = mainFolder.listFiles();

            if (!mainFolder.isDirectory() || filesList == null) {
                throw new IllegalArgumentException("Expected folder");
            }

            folderSizes = new long[filesList.length];
            for (int i = 0; i < filesList.length; ++i) {
                folderSizes[i] = getFolderSize(filesList[i]);
                totalSize += folderSizes[i];
            }
        }

        public String getFolderTable() {
            TableRenderer table = new TableRenderer(folderTableColumnsNum);

            for (int i = 0; i < filesList.length; ++i) {
                String itemsCount;
                File[] tmp = filesList[i].listFiles();
                if (tmp != null) {
                    itemsCount = String.format("%d items", tmp.length);
                } else {
                    itemsCount = "file";
                }

                String[] row = {
                        String.format("%d.", i + 1),
                        String.format("%s |", filesList[i].getName()),
                        String.format("%.2f KiB |", folderSizes[i] / bytesInKiB),
                        String.format("%.2f%% |", (double) (folderSizes[i]) / totalSize * percent),
                        itemsCount,
                };

                table.addRow(row);
            }

            return table.render();
        }

        public String getBiggestFilesTable() {
            TableRenderer table = new TableRenderer(biggestFilesTableColumnsNum);

            int i = 1;

            for (File f : biggestFiles) {
                String[] row = {
                        String.format("%d.", i),
                        String.format("%.2f KiB |", f.length() / bytesInKiB),
                        String.format("%s", f.getAbsolutePath()),
                };

                table.addRow(row);
                ++i;
            }

            return table.render();
        }

        private long getFolderSize(File file) {
            if (file == null) {
                return 0;
            }
            if (file.isFile()) {
                checkFile(file);
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
            if (data.isEmpty()) {
                return "No data";
            }

            StringJoiner table = new StringJoiner("\n");

            for (String[] datum : data) {
                StringBuilder row = new StringBuilder();
                for (int j = 0; j < sizes.length; ++j) {
                    row.append(String.format(String.format("%%%ds ", sizes[j]), datum[j]));
                }

                table.add(row.toString());
            }

            return table.toString();
        }
    }

    public static void main(String[] args) {
        DiskAnalyzer diskAnalyzer = new DiskAnalyzer(args[0]);

        System.out.println();
        System.out.println(" Folder table:");
        System.out.println(diskAnalyzer.getFolderTable());
        System.out.println();
        System.out.println(" Biggest files:");
        System.out.println(diskAnalyzer.getBiggestFilesTable());
    }
}
