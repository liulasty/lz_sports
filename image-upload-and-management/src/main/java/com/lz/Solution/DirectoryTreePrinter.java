package com.lz.Solution;

import java.io.File;

/**
 * 目录树打印机
 *
 * @author lz
 * @date 2024/02/08
 */
public class DirectoryTreePrinter {

    public static void main(String[] args) {
        String targetDirectory = "D:\\project\\health_parent";
        printDirectoryTree2(targetDirectory, "");
    }

    public static void printDirectoryTree(String dirPath, String indent) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    System.out.println(indent + "[" + file.getName() + "]");
                    printDirectoryTree(file.getAbsolutePath(), indent + "   ");
                } else {
                    System.out.println(indent + file.getName());
                }
            }
        }
    }

    public static void printDirectoryTree2(String dirPath, String indent) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    System.out.println(indent + "[" + file.getName() + "]");
                    if (indent.length() <= 6) {
                        printDirectoryTree2(file.getAbsolutePath(), indent + 
                                "   ");
                    }
                } else {
                    System.out.println(indent + file.getName());
                }
            }
        }
    }
}