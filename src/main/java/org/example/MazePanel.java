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

    private Color wallColor,pathColor,gridColor,solutionColor;
    private boolean drawGrid;

    private int CELL_SIZE;

    public MazePanel(){
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

        int maxAvailableWidth = (int) (getParent().getWidth() * 0.90);
        int maxAvailableHeight = (int) (getParent().getHeight() * 0.90);

        int calculatedWidth = maxAvailableWidth / config.getWidth();
        int calculatedHeight = maxAvailableHeight / config.getHeight();

        this.CELL_SIZE = Math.min(calculatedWidth, calculatedHeight);
        CELL_SIZE = Math.max(CELL_SIZE, 1);

        int width = mazeMap[0].length * CELL_SIZE;
        int height = mazeMap.length * CELL_SIZE;
        setPreferredSize(new Dimension(width, height));

        revalidate();
        repaint();
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

    private void animate(Runnable onAnimationComplete){
        int delay =config.getAnimationDelayMs();
//        delay=0;

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

    public void clearSolution() {
        stopAnimation();
        this.solutionPath = null;
        this.currentStep = 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (mazeMap == null || config == null) return;

        long start = System.nanoTime();

        int cols = mazeMap[0].length;
        int rows = mazeMap.length;
        int mazePixelWidth = cols * CELL_SIZE;
        int mazePixelHeight = rows * CELL_SIZE;

        int startX = (getWidth() - mazePixelWidth) / 2;
        int startY = (getHeight() - mazePixelHeight) / 2;

        g.setColor(pathColor);
        g.fillRect(startX, startY, mazePixelWidth, mazePixelHeight);

        g.setColor(wallColor);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
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

            for (int r = 0; r <= rows; r++) {
                int y = startY + (r * CELL_SIZE);
                g.drawLine(startX, y, startX + mazePixelWidth, y);
            }

            for (int c = 0; c <= cols; c++) {
                int x = startX + (c * CELL_SIZE);
                g.drawLine(x, startY, x, startY + mazePixelHeight);
            }
        }

        long end = System.nanoTime();
        double drawTimeMs = (end - start) / 1_000_000.0;
        System.out.printf("Draw time: %.3fms%n", drawTimeMs);
    }

}