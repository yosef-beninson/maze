package org.example;

import java.awt.*;

public class MazeConfig {
    private String wallCellColor;
    private String pathColor;
    private String drawGrid;
    private String gridColor;
    private String animationDelayMs;

    private int height,width;

    public Color getWallCellColor() { return Color.decode(wallCellColor); }
    public Color getPathColor() { return Color.decode(pathColor); }
    public Color getGridColor() { return Color.decode(gridColor); }
    public boolean getDrawGrid() { return Boolean.parseBoolean(drawGrid); }
    public int getAnimationDelayMs() { return Integer.parseInt(animationDelayMs); }

    public int getHeight() {return height;}
    public int getWidth() {return width;}

    public void setWidth(int width) {this.width = width;}
    public void setHeight(int height) {this.height = height;}
}
