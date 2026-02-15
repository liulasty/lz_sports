package com.lz.Solution;


import java.util.ArrayList;
import java.util.List;

/**
 * 工作人员分配
 *
 * @author lz
 * @date 2024/03/14
 */
public class workerAllocation {
    public static void main(String[] args) {
        int maxWorkers = calculateMaxWorkers();
        System.out.println("生产车间最多可以容纳 " + maxWorkers + " 个工人");
        List<List<String>> validGroups = generateValidGroups();
        System.out.println("符合条件的所有分组可能情况：");
        for (List<String> group : validGroups) {
            System.out.println(group);
        }
    }

    public static int calculateMaxWorkers() {
        return 3 * 6; // 每个部门3个工人，共4个部门
    }

    public static List<List<String>> generateValidGroups() {
        List<List<String>> validGroups = new ArrayList<>();
        String[] departments = {"电子组装部", "注塑部", "喷油部", "电子附加部"};
        for (String department : departments) {
            for (int i = 1; i <= 3; i++) {
                for (int j = i; j <= 3; j++) {
                    List<String> group = new ArrayList<>();
                    group.add(department + "工人" + i);
                    if (i != j) {
                        group.add(department + "工人" + j);
                    }
                    validGroups.add(group);
                }
            }
        }
        return validGroups;
    }
}