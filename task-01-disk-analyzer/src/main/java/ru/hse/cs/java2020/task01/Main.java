package ru.hse.cs.java2020.task01;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.StringJoiner;
import java.util.TreeSet;

public class Main {
    public static class DiskAnalyzer {
        private final long topCount = 10;
        private final double bytesInKiB = 1024;
        private final double percent = 100;

        private File mainFolder;
        private ArrayList<File> filesList;
        private long totalSize = 0;
        private ArrayList<Pair> foldersInfo;
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
            File[] files = mainFolder.listFiles();

            if (!mainFolder.isDirectory() || files == null) {
                throw new IllegalArgumentException("Expected folder");
            }

            filesList = new ArrayList<>();
            foldersInfo = new ArrayList<>();

            for (File file : files) {
                if (Files.isSymbolicLink(file.toPath())) {
                    continue;
                }

                filesList.add(file);
                foldersInfo.add(getFolderSize(file));
                totalSize += foldersInfo.get(foldersInfo.size() - 1).size;
            }
        }

        public String getFolderTable() {
            TableRenderer table = new TableRenderer();

            for (int i = 0; i < filesList.size(); ++i) {
                String itemsCount;
                if (filesList.get(i).isDirectory()) {
                    itemsCount = String.format("%d item", foldersInfo.get(i).count);
                    if (foldersInfo.get(i).count > 1) {
                        itemsCount += "s";
                    }
                } else {
                    itemsCount = "file";
                }

                ArrayList<String> row = new ArrayList<>();

                row.add(String.format("%d.", i + 1));
                row.add(String.format("%s |", filesList.get(i).getName()));
                row.add(String.format("%.2f KiB |", foldersInfo.get(i).size / bytesInKiB));
                row.add(String.format("%.2f%% |", (double) (foldersInfo.get(i).size) / totalSize * percent));
                row.add(itemsCount);

                table.addRow(row);
            }

            return table.render();
        }

        public String getBiggestFilesTable() {
            TableRenderer table = new TableRenderer();

            int i = 1;

            for (File f : biggestFiles) {
                ArrayList<String> row = new ArrayList<>();

                row.add(String.format("%d.", i));
                row.add(String.format("%.2f KiB |", f.length() / bytesInKiB));
                row.add(String.format("%s", f.getAbsolutePath()));

                table.addRow(row);
                ++i;
            }

            return table.render();
        }

        private Pair getFolderSize(File file) {
            if (file == null || Files.isSymbolicLink(file.toPath())) {
                return new Pair(0, 0);
            }
            if (file.isFile()) {
                checkFile(file);
                return new Pair(file.length(), 1);
            }

            File[] filesList = file.listFiles();
            if (filesList == null) {
                return new Pair(0, 0);
            }

            Pair ret = new Pair(0, 0);
            for (File to : filesList) {
                Pair get = getFolderSize(to);
                ret.size += get.size;
                ret.count += get.count;
            }

            return ret;
        }

        private static class Pair {
            private long size, count;

            Pair(long setSize, long setCount) {
                size = setSize;
                count = setCount;
            }
        }
    }

    public static class TableRenderer {
        private ArrayList<ArrayList<String>> data;
        private ArrayList<Long> sizes;

        public TableRenderer() {
            data = new ArrayList<>();
            sizes = new ArrayList<>();
        }

        public void addRow(ArrayList<String> row) {
            data.add(row);

            for (int i = 0; i < row.size(); ++i) {
                if (sizes.size() == i) {
                    sizes.add(0L);
                }
                sizes.set(i, Math.max(sizes.get(i), row.get(i).length()));
            }
        }

        public String render() {
            if (data.isEmpty()) {
                return "No data";
            }

            StringJoiner table = new StringJoiner("\n");

            for (ArrayList<String> datum : data) {
                StringBuilder row = new StringBuilder();
                for (int j = 0; j < sizes.size(); ++j) {
                    row.append(String.format(String.format("%%%ds ", sizes.get(j)), datum.get(j)));
                }

                table.add(row.toString());
            }

            return table.toString();
        }
    }

    private static final double SECS_IN_NANO_SEC = 1e9;

    public static void main(String[] args) {
        DiskAnalyzer diskAnalyzer = new DiskAnalyzer(args[0]);

        long startTime = System.nanoTime();
        System.out.println();
        System.out.println(" Folder table:");
        System.out.println(diskAnalyzer.getFolderTable());
        System.out.println();
        System.out.println(" Biggest files:");
        System.out.println(diskAnalyzer.getBiggestFilesTable());
        System.out.println();
        System.out.format("Elapsed time: %.5f sec%n", (System.nanoTime() - startTime) / SECS_IN_NANO_SEC);
    }
}
