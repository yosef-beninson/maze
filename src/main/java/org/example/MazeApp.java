package org.example;

import com.google.gson.Gson;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public class MazeApp extends JFrame {

    private JLabel wallColorLabel, delayLabel, pathColorLabel, gridColorLabel, drawGridLabel;
    private JTextField widthField, heightField;
    private JButton checkSolutionBtn, actionBtn, refreshConfigBtn, getMazeBtn;

    private final MazePanel mazeDisplayArea = new MazePanel();
    private MazeConfig config = new MazeConfig();

    private MazeSolver solver;
    private short[][] mazeMap;

    public MazeApp() {

        setTitle("Maze Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        JPanel controlPanel = makeControlPanel();

        add(controlPanel, BorderLayout.NORTH);
        add(mazeDisplayArea, BorderLayout.CENTER);

        setVisible(true);

    }


    private JPanel makeConfigPanel() {
        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        configPanel.setBorder(BorderFactory.createTitledBorder("Server Configurations"));

        initializeLabels();
        fetchConfigurations();

        configPanel.add(wallColorLabel);
        configPanel.add(pathColorLabel);
        configPanel.add(gridColorLabel);
        configPanel.add(drawGridLabel);
        configPanel.add(delayLabel);

        return configPanel;
    }
    private JPanel makeInputPanel() {
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));

        inputPanel.add(new JLabel("Width:"));
        widthField = new JTextField(5);
        widthField.setText("30");
        inputPanel.add(widthField);

        inputPanel.add(new JLabel("Height:"));
        heightField = new JTextField(5);
        heightField.setText("30");
        inputPanel.add(heightField);

        return inputPanel;
    }
    private JPanel makeButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        initializeButtons();

        buttonPanel.add(refreshConfigBtn);
        buttonPanel.add(getMazeBtn);
        buttonPanel.add(checkSolutionBtn);
        buttonPanel.add(actionBtn);

        return buttonPanel;
    }
    private JPanel makeControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel configPanel = makeConfigPanel();
        JPanel inputPanel = makeInputPanel();
        JPanel buttonPanel = makeButtonPanel();

        controlPanel.add(configPanel);
        controlPanel.add(inputPanel);
        controlPanel.add(buttonPanel);

        return controlPanel;
    }

    private void makeCheckSolutionBtn() {
        checkSolutionBtn = new JButton("Check Solution");

        checkSolutionBtn.addActionListener(e -> {
            if (checkSolutionBtn.getText().equals("Resume Solution")) {
                checkSolutionBtn.setEnabled(false);
                refreshConfigBtn.setEnabled(false);
                getMazeBtn.setEnabled(false);

                actionBtn.setText("Stop Solution");

                mazeDisplayArea.resumeAnimation(() -> {
                    checkSolutionBtn.setText("Check Solution");
                    checkSolutionBtn.setEnabled(true);
                    refreshConfigBtn.setEnabled(true);
                    getMazeBtn.setEnabled(true);
                    actionBtn.setText("Clear Solution");
                });

            } else {
                mazeDisplayArea.clearSolution();

                if (solver.getSolution() == null || solver.getSolution().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No solution found", "Result", JOptionPane.WARNING_MESSAGE);
                } else {
                    checkSolutionBtn.setEnabled(false);
                    refreshConfigBtn.setEnabled(false);
                    getMazeBtn.setEnabled(false);

                    actionBtn.setText("Stop Solution");
                    actionBtn.setVisible(true);

                    mazeDisplayArea.animateSolution(solver.getSolution(), () -> {
                        checkSolutionBtn.setEnabled(true);
                        refreshConfigBtn.setEnabled(true);
                        getMazeBtn.setEnabled(true);
                        actionBtn.setText("Clear Solution");
                    });
                }
            }
        });

        checkSolutionBtn.setEnabled(false);
    }
    private void makeActionBtn() {
        actionBtn = new JButton("Stop Solution");
        actionBtn.setVisible(false);

        actionBtn.addActionListener(e -> {
            if (actionBtn.getText().equals("Stop Solution")) {
                mazeDisplayArea.stopAnimation();

                checkSolutionBtn.setText("Resume Solution");
                checkSolutionBtn.setEnabled(true);
                refreshConfigBtn.setEnabled(true);
                getMazeBtn.setEnabled(true);

                actionBtn.setText("Clear Solution");

            } else if (actionBtn.getText().equals("Clear Solution")) {
                mazeDisplayArea.clearSolution();

                checkSolutionBtn.setText("Check Solution");

                actionBtn.setVisible(false);
            }
        });

    }
    private void makeRefreshConfigBtn() {
        refreshConfigBtn = new JButton("Refresh Config");
        refreshConfigBtn.addActionListener(e -> fetchConfigurations());
    }
    private void makeGetMazeButton() {
        getMazeBtn = new JButton("GET MAZE");
        getMazeBtn.addActionListener(e -> {
            try {
                int userWidth = normalizeUserInput(Integer.parseInt(widthField.getText()));
                int userHeight = normalizeUserInput(Integer.parseInt(heightField.getText()));
                config.setWidth(userWidth);
                config.setHeight(userHeight);

                new Thread(() -> {
                    BufferedImage mazeImage = fetchMaze(userWidth, userHeight);

                    if (mazeImage != null) {
                        System.out.println("Image downloaded! Size: " + mazeImage.getWidth() + "x" + mazeImage.getHeight());

                        SwingUtilities.invokeLater(() -> {
                            mazeDisplayArea.removeAll();
                            mazeDisplayArea.revalidate();
                            mazeDisplayArea.repaint();

                            checkSolutionBtn.setText("Check Solution");
                            actionBtn.setVisible(false);

                            checkSolutionBtn.setEnabled(true);
                            mapMaze(mazeImage);
                            solver = new MazeSolver(mazeMap, config.getHeight(), config.getWidth());
                        });
                    }
                }).start();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid integer numbers for width and height.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    private void initializeButtons() {
        makeCheckSolutionBtn();
        makeActionBtn();
        makeRefreshConfigBtn();
        makeGetMazeButton();
    }

    private void initializeLabels() {
        wallColorLabel = new JLabel("Wall Color");
        pathColorLabel = new JLabel("Path Color");
        gridColorLabel = new JLabel("Grid Color");
        drawGridLabel = new JLabel("Draw Grid: " );
        delayLabel = new JLabel("Animation Delay: "  + "ms");



        setLabelsToPending();
    }
    private void setLabelsToPending() {
        wallColorLabel.setForeground(Color.BLACK);
        pathColorLabel.setForeground(Color.BLACK);
        gridColorLabel.setForeground(Color.BLACK);

        drawGridLabel.setText("Draw Grid: pending");
        delayLabel.setText("Animation Delay: pending");

    }

    private void fetchConfigurations() {
        setLabelsToPending();

        HttpClient client= HttpClient.newBuilder().build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://backend-qcf9.onrender.com/fm1/get-render-config"))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(s -> {
                    Gson gson = new Gson();
                    config = gson.fromJson(s, MazeConfig.class);

                    SwingUtilities.invokeLater(() -> {
                        wallColorLabel.setForeground(config.getWallCellColor());
                        pathColorLabel.setForeground(config.getPathColor());
                        gridColorLabel.setForeground(config.getGridColor());
                        drawGridLabel.setText("Draw Grid: " + config.getDrawGrid());
                        delayLabel.setText("Animation Delay: " + config.getAnimationDelayMs() + "ms");
                    });
                })
                .exceptionally(e -> {
                    System.err.println("Failed to fetch configuration: " + e.getMessage());
                    return null;
                });
    }
    private BufferedImage fetchMaze(int width, int height) {
        String urlString = "https://backend-qcf9.onrender.com/fm1/get-maze-image?width=" + width + "&height=" + height;
        try {
            return ImageIO.read(URI.create(urlString).toURL());
        } catch (IOException e) {
            System.err.println("Failed to download maze image: " + e.getMessage());
            return null;
        }
    }

    private static int normalizeUserInput(int userInput) {
        if (userInput < 5 || userInput > 100) return 30;
        return userInput;
    }
    private void mapMaze(BufferedImage mazeImg) {
        int mazeWidth = config.getWidth();
        int mazeHeight = config.getHeight();
        int cellWidth = mazeImg.getWidth() / mazeWidth;
        int cellHeight = mazeImg.getHeight() / mazeHeight;
        short[][] mazeMap = new short[mazeHeight][mazeWidth];
        short row = 0, column = 0;
        for (int i = 2; i < mazeImg.getHeight(); i += cellHeight) {
            for (int j = 2; j < mazeImg.getWidth(); j += cellWidth) {
                Color c = new Color(mazeImg.getRGB(j, i));
                if (Objects.equals(Color.WHITE, c))
                    mazeMap[row][column] = 0;
                else mazeMap[row][column] = 1;
                column++;
            }
            column = 0;
            row++;
        }
        this.mazeMap = mazeMap;
        mazeDisplayArea.setMazeData(mazeMap, config);
    }



}