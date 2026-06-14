package org.example;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MazeSolver {
    private final short[][] map;
    int rows, columns;
    private List<Point> solution;

    public MazeSolver(short[][] map, int rows, int columns) {
        this.map = map;
        this.rows = rows;
        this.columns = columns;
        solve();
    }

    public void solve() {
        List<Point> path = new ArrayList<>();

        if (map[0][0] == 1 || map[rows - 1][columns - 1] == 1) {
            solution = null;
            return;
        }

        Queue<Point> queue = new LinkedList<>();
        boolean[][] visited = new boolean[rows][columns];
        Point[][] parent = new Point[rows][columns];


        int[] dRow = {-1, 1, 0, 0};
        int[] dCol = {0, 0, -1, 1};


        queue.add(new Point(0, 0));
        visited[0][0] = true;

        boolean reachedEnd = false;

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            int row = current.y;
            int column = current.x;


            if (row == rows - 1 && column == columns - 1) {
                reachedEnd = true;
                break;
            }

            for (int i = 0; i < 4; i++) {
                int newRow = row + dRow[i];
                int newCol = column + dCol[i];

                boolean isWithinBounds = newRow >= 0 && newRow < rows && newCol >= 0 && newCol < columns;

                if (isWithinBounds) {
                    if (map[newRow][newCol] == 0 && !visited[newRow][newCol]) {
                        queue.add(new Point(newCol, newRow));
                        visited[newRow][newCol] = true;
                        parent[newRow][newCol] = current;
                    }
                }
            }
        }

        if (reachedEnd) {
            Point step = new Point(columns - 1, rows - 1);
            while (step != null) {
                path.addFirst(step);
                step = parent[step.y][step.x];
            }
        }

        solution = path;
    }

    public List<Point> getSolution() {
        if (solution.isEmpty()) solve();
        return solution;
    }
}
