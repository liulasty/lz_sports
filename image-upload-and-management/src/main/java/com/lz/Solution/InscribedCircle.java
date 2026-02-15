package com.lz.Solution;

/*
  Created with IntelliJ IDEA.
 
  @Author: lz
 * @Date: 2024/03/21/17:40
 * @Description:
 */

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 内切圆
 *
 * @author lz
 * @date 2024/03/21
 */
public class InscribedCircle {
    /**
     * 网 格
     * 假设grid是一个10x10的二维数组，用于表示二维坐标系的第一象限。
     *  二维数组表示方格;
     */
    private static final int[][] GRID = {  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                            {0, 0, 1, 1, 1, 1, 1, 0, 0, 0},
                                            {0, 0, 1, 1, 1, 1, 0, 0, 0, 0},
                                            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                                            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}}; 

    public static void main(String[] args) {
        // 示例：初始化grid，1表示黑色方格，0表示白色方格
        // 这里应该有一个初始化grid的过程，根据实际情况填充黑白方格
        
        List<Point> boundaryPoints = findBoundaryPoints();
        Circle maxInCircle = findMaxInCircle(boundaryPoints);
        
        System.out.println("最大内切圆的圆心: (" + maxInCircle.center.x + ", " + maxInCircle.center.y + ")，半径: " + maxInCircle.radius);
    }

    /**
     * 查找边界点
     *
     * @return {@code List<Point>}
     */
    private static List<Point> findBoundaryPoints() {
        List<Point> boundaryPoints = new ArrayList<>();
        for (int x = 0; x < GRID.length; x++) {
            for (int y = 0; y < GRID[x].length; y++) {
                if (isBoundaryPoint(x, y)) {
                    boundaryPoints.add(new Point(x, y));
                }
            }
        }
        return boundaryPoints;
    }

    /**
     * 是边界点
     *
     * @param x x
     * @param y y
     *
     * @return boolean
     */
    private static boolean isBoundaryPoint(int x, int y) {
        if (GRID[x][y] == 0) {
            return false;
        }
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : directions) {
            int nx = x + d[0], ny = y + d[1];
            if (nx >= 0 && nx < GRID.length && ny >= 0 && ny < GRID[0].length) {
                if (GRID[nx][ny] == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 查找最大内圈
     *
     * @param boundaryPoints 边界点
     *
     * @return {@code Circle}
     */
    private static Circle findMaxInCircle(List<Point> boundaryPoints) {
        double maxRadius = 0;
        Point center = new Point(0, 0);
        for (int x = 0; x < GRID.length; x++) {
            for (int y = 0; y < GRID[x].length; y++) {
                double minDistance = Double.MAX_VALUE;
                for (Point boundaryPoint : boundaryPoints) {
                    double distance = boundaryPoint.distance(x, y);
                    minDistance = Math.min(minDistance, distance);
                }
                if (minDistance > maxRadius) {
                    maxRadius = minDistance;
                    center = new Point(x, y);
                }
            }
        }
        return new Circle(center, maxRadius);
    }

    /**
     * 圈
     *
     * @author lz
     * @date 2024/03/21
     */
    static class Circle {
        Point center;
        double radius;

        public Circle(Point center, double radius) {
            this.center = center;
            this.radius = radius;
        }
    }
    
}