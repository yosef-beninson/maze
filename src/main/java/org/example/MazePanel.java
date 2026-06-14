package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MazePanel extends JPanel {
    private short[][] mazeMap;
    private MazeConfig config;

    private List<Point> solutionPath;
    private int currentStep = 0;
    private Timer animationTimer;

    private Color wallColor, pathColor, gridColor, solutionColor;
    private boolean drawGrid;

    private int row, column;
    private int CELL_SIZE, startX, startY,mazePixelWidth,mazePixelHeight;

    public MazePanel() {
        setBackground(Color.LIGHT_GRAY);
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
    }

    public void setMazeData(short[][] mazeMap, MazeConfig config) {
        this.mazeMap = mazeMap;
        this.config = config;
        this.solutionPath = null;

        wallColor = config.getWallCellColor();
        pathColor = Color.WHITE;
        gridColor = config.getGridColor();
        solutionColor = config.getPathColor();
        drawGrid = config.getDrawGrid();

        this.column = mazeMap[0].length;
        this.row = mazeMap.length;

        updateDiminutions();

        revalidate();
        repaint();
    }

    private void updateDiminutions() {
        int maxAvailableWidth = (int) (getWidth() * 0.95);
        int maxAvailableHeight = (int) (getHeight() * 0.95);

        int calculatedWidth = maxAvailableWidth / column;
        int calculatedHeight = maxAvailableHeight / row;

        this.CELL_SIZE = Math.min(calculatedWidth, calculatedHeight);
        CELL_SIZE = Math.max(CELL_SIZE, 1);

        mazePixelWidth = column * CELL_SIZE;
        mazePixelHeight = row * CELL_SIZE;

        startX = (getWidth() - mazePixelWidth) / 2;
        startY = (getHeight() - mazePixelHeight) / 2;
    }


    public void animateSolution(List<Point> path, Runnable onAnimationComplete) {
        this.solutionPath = path;
        this.currentStep = 0;

        animate(onAnimationComplete);
    }

    public void stopAnimation() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
    }

    public void resumeAnimation(Runnable onAnimationComplete) {
        if (solutionPath == null || currentStep >= solutionPath.size()) return;
        animate(onAnimationComplete);
    }

    public void clearSolution() {
        stopAnimation();
        this.solutionPath = null;
        this.currentStep = 0;
        repaint();
    }

    private void animate(Runnable onAnimationComplete) {
        int delay = config.getAnimationDelayMs();
        delay = 0;

        animationTimer = new Timer(delay, e -> {
            currentStep++;
            repaint();

            if (currentStep >= solutionPath.size()) {
                animationTimer.stop();
                onAnimationComplete.run();
            }
        });

        animationTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (mazeMap == null || config == null) return;

        long start = System.nanoTime();

        updateDiminutions();

        System.out.println("width: " + getWidth() + " height: " + getHeight());

        g.setColor(pathColor);
        g.fillRect(startX, startY, mazePixelWidth, mazePixelHeight);

        g.setColor(wallColor);
        for (int r = 0; r < row; r++) {
            for (int c = 0; c < column; c++) {
                if (mazeMap[r][c] == 1) {
                    int x = startX + (c * CELL_SIZE);
                    int y = startY + (r * CELL_SIZE);
                    g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        if (solutionPath != null) {
            g.setColor(solutionColor);
            for (int i = 0; i < currentStep && i < solutionPath.size(); i++) {
                Point point = solutionPath.get(i);
                int x = startX + (point.x * CELL_SIZE);
                int y = startY + (point.y * CELL_SIZE);

                g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }

        if (drawGrid) {
            g.setColor(gridColor);

            for (int r = 0; r <= row; r++) {
                int y = startY + (r * CELL_SIZE);
                g.drawLine(startX, y, startX + mazePixelWidth, y);
            }

            for (int c = 0; c <= column; c++) {
                int x = startX + (c * CELL_SIZE);
                g.drawLine(x, startY, x, startY + mazePixelHeight);
            }
        }

        long end = System.nanoTime();
        double drawTimeMs = (end - start) / 1_000_000.0;
        System.out.printf("Draw time: %.3fms%n", drawTimeMs);
    }

}