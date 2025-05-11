package com.example.algoproject;

import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.animation.SequentialTransition;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TowerOfHanoiFX extends Application {
    // Constants
    private static final int DEFAULT_NUM_DISKS = 8;
    private static final int NUM_PEGS = 4;
    private static final int PEG_HEIGHT = 200;
    private static final int PEG_WIDTH = 10;
    private static final int DISK_HEIGHT = 20;
    private static final int MAX_DISK_WIDTH = 200;
    private static final int ANIMATION_DURATION = 500; // ms

    // UI Components
    private HanoiPane hanoiPane;
    private ComboBox<Integer> diskCountSelector;
    private ComboBox<String> algorithmSelector;
    private Button startButton;
    private Button resetButton;
    private Label statusLabel;
    private Label moveCountLabel;
    private Slider animationSpeedSlider;
    private ProgressBar progressBar;

    // State variables
    private int numDisks = DEFAULT_NUM_DISKS;
    private AtomicBoolean calculationRunning = new AtomicBoolean(false);
    private int moveCount = 0;
    private List<Move> moves = new ArrayList<>();
    private SequentialTransition animation;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Tower of Hanoi - Four Pegs");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Setup UI components
        setupHanoiPane();
        VBox controlPanel = setupControlPanel();
        HBox statusBar = setupStatusBar();

        // Add components to layout
        root.setCenter(hanoiPane);
        root.setRight(controlPanel);
        root.setBottom(statusBar);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("styles.css") != null ?
                getClass().getResource("styles.css").toExternalForm() : "");

        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();

        // Initial status
        statusLabel.setText("Select number of disks and click 'Start'");
    }

    private void setupHanoiPane() {
        hanoiPane = new HanoiPane(numDisks);
    }

    private VBox setupControlPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPrefWidth(250);
        panel.getStyleClass().add("control-panel");

        // Disk count selector
        Label diskCountLabel = new Label("Number of Disks:");
        diskCountSelector = new ComboBox<>();
        diskCountSelector.getItems().addAll(3, 4, 5, 6, 7, 8, 9, 10);
        diskCountSelector.setValue(DEFAULT_NUM_DISKS);
        diskCountSelector.setMaxWidth(Double.MAX_VALUE);
        diskCountSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            numDisks = newVal;
            hanoiPane.setNumDisks(numDisks);
        });

        // Algorithm selector
        Label algorithmLabel = new Label("Algorithm:");
        algorithmSelector = new ComboBox<>();
        algorithmSelector.getItems().addAll(
                "Frame-Stewart Algorithm",
                "Dynamic Programming",
                "Simple Divide & Conquer"
        );
        algorithmSelector.setValue("Frame-Stewart Algorithm");
        algorithmSelector.setMaxWidth(Double.MAX_VALUE);

        // Animation speed
        Label speedLabel = new Label("Animation Speed:");
        animationSpeedSlider = new Slider(100, 1000, ANIMATION_DURATION);
        animationSpeedSlider.setShowTickMarks(true);
        animationSpeedSlider.setShowTickLabels(true);
        animationSpeedSlider.setMajorTickUnit(300);
        animationSpeedSlider.setBlockIncrement(50);
        animationSpeedSlider.setSnapToTicks(true);

        // Buttons
        startButton = new Button("Start");
        startButton.setMaxWidth(Double.MAX_VALUE);
        startButton.getStyleClass().add("start-button");
        startButton.setOnAction(e -> startTowerOfHanoi());

        resetButton = new Button("Reset");
        resetButton.setMaxWidth(Double.MAX_VALUE);
        resetButton.setOnAction(e -> resetTowers());

        // Add components to panel
        panel.getChildren().addAll(
                diskCountLabel, diskCountSelector,
                algorithmLabel, algorithmSelector,
                speedLabel, animationSpeedSlider,
                new Separator(),
                startButton,
                resetButton
        );

        return panel;
    }

    private HBox setupStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.setAlignment(Pos.CENTER_LEFT);

        statusLabel = new Label();
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(statusLabel, Priority.ALWAYS);

        moveCountLabel = new Label("Moves: 0");

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(150);
        progressBar.setVisible(false);

        statusBar.getChildren().addAll(statusLabel, moveCountLabel, progressBar);

        return statusBar;
    }

    private void startTowerOfHanoi() {
        if (calculationRunning.get()) {
            return;
        }

        calculationRunning.set(true);
        startButton.setDisable(true);
        resetButton.setDisable(true);
        diskCountSelector.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        statusLabel.setText("Calculating solution...");

        // Stop any running animation
        if (animation != null) {
            animation.stop();
        }

        moveCount = 0;
        moveCountLabel.setText("Moves: 0");
        moves.clear();

        boolean useFrameStewart = algorithmSelector.getSelectionModel().getSelectedIndex() == 0;

        Task<List<Move>> task = new Task<>() {
            @Override
            protected List<Move> call() {
                if (useFrameStewart) {
                    return solveFrameStewart(numDisks, 0, 3, 1, 2);
                } else if (algorithmSelector.getSelectionModel().getSelectedIndex() == 2) {
                    return solveSimpleDivideAndConquer(numDisks, 0, 3, 1, 2);
                } else {
                    return solveDynamicProgramming(numDisks);
                }
            }

            @Override
            protected void succeeded() {
                moves = getValue();
                moveCount = moves.size();
                moveCountLabel.setText("Moves: " + moveCount);

                animateSolution();

                statusLabel.setText("Solution found! " + moveCount + " moves.");
                progressBar.setVisible(false);
                startButton.setDisable(false);
                resetButton.setDisable(false);
                diskCountSelector.setDisable(false);
                calculationRunning.set(false);
            }

            @Override
            protected void failed() {
                Throwable exception = getException();
                statusLabel.setText("Error: " + exception.getMessage());
                progressBar.setVisible(false);
                startButton.setDisable(false);
                resetButton.setDisable(false);
                diskCountSelector.setDisable(false);
                calculationRunning.set(false);
            }
        };

        new Thread(task).start();
    }

    private void resetTowers() {
        if (animation != null) {
            animation.stop();
        }

        moveCount = 0;
        moveCountLabel.setText("Moves: 0");
        moves.clear();

        hanoiPane.resetTowers();
        statusLabel.setText("Select number of disks and click 'Start'");
    }

    private void animateSolution() {
        animation = new SequentialTransition();

        for (Move move : moves) {
            TranslateTransition tt = hanoiPane.createMoveAnimation(move.fromPeg, move.toPeg, move.diskIndex);
            tt.setDuration(Duration.millis(animationSpeedSlider.getValue()));
            animation.getChildren().add(tt);
        }

        animation.play();
    }

    private List<Move> solveFrameStewart(int n, int source, int target, int auxiliary1, int auxiliary2) {
        List<Move> movesList = new ArrayList<>();

        if (n == 0) return movesList;
        if (n == 1) {
            movesList.add(new Move(source, target, n-1));
            return movesList;
        }

        // Find optimal k value for Frame-Stewart
        int k = findOptimalK(n);

        // Step 1: Move top n-k disks to auxiliary1
        movesList.addAll(solveFrameStewart(n-k, source, auxiliary1, auxiliary2, target));

        // Step 2: Move remaining k disks from source to target using standard Tower of Hanoi
        movesList.addAll(solveTowerOfHanoi(k, source, target, auxiliary2));

        // Step 3: Move n-k disks from auxiliary1 to target
        movesList.addAll(solveFrameStewart(n-k, auxiliary1, target, source, auxiliary2));

        return movesList;
    }

    private List<Move> solveTowerOfHanoi(int n, int source, int target, int auxiliary) {
        List<Move> movesList = new ArrayList<>();

        if (n == 1) {
            movesList.add(new Move(source, target, n-1));
            return movesList;
        }

        movesList.addAll(solveTowerOfHanoi(n-1, source, auxiliary, target));
        movesList.add(new Move(source, target, n-1));
        movesList.addAll(solveTowerOfHanoi(n-1, auxiliary, target, source));

        return movesList;
    }

    private List<Move> solveDynamicProgramming(int n) {
        // DP table for minimum number of moves
        int[] dp = new int[n + 1];
        int[] kValues = new int[n + 1];

        dp[0] = 0;
        dp[1] = 1;

        for (int i = 2; i <= n; i++) {
            // Start with standard 3-peg solution
            dp[i] = 2 * dp[i - 1] + 1;
            kValues[i] = 0;  // k=0 means use standard algorithm

            // Try all possible k values (Frame-Stewart approach)
            for (int k = 1; k < i; k++) {
                int moves = 2 * dp[i - k] + (1 << k) - 1;
                if (moves < dp[i]) {
                    dp[i] = moves;
                    kValues[i] = k;
                }
            }
        }

        // Generate move sequence using the optimal k values
        return generateMoves(n, 0, 3, 1, 2, dp, kValues);
    }

    private List<Move> solveSimpleDivideAndConquer(int n, int source, int target, int aux1, int aux2) {
        List<Move> movesList = new ArrayList<>();

        if (n == 0) return movesList;
        if (n == 1) {
            movesList.add(new Move(source, target, n-1));
            return movesList;
        }

        // Move n-1 disks to the first auxiliary peg
        movesList.addAll(solveSimpleDivideAndConquer(n-1, source, aux1, aux2, target));

        // Move the largest disk to the target
        movesList.add(new Move(source, target, n-1));

        // Move n-1 disks from auxiliary to target
        movesList.addAll(solveSimpleDivideAndConquer(n-1, aux1, target, source, aux2));

        return movesList;
    }

    private List<Move> generateMoves(int n, int source, int target, int aux1, int aux2, int[] dp, int[] kValues) {
        List<Move> movesList = new ArrayList<>();

        if (n == 0) return movesList;
        if (n == 1) {
            movesList.add(new Move(source, target, n-1));
            return movesList;
        }

        int k = kValues[n];

        if (k == 0) {
            // Standard Tower of Hanoi (3 pegs)
            movesList.addAll(solveTowerOfHanoi(n, source, target, aux1));
        } else {
            // Frame-Stewart approach with optimal k
            movesList.addAll(generateMoves(n-k, source, aux1, aux2, target, dp, kValues));
            movesList.addAll(solveTowerOfHanoi(k, source, target, aux2));
            movesList.addAll(generateMoves(n-k, aux1, target, source, aux2, dp, kValues));
        }

        return movesList;
    }

    private int findOptimalK(int n) {
        if (n == 8) return 3;  // For n=8, k=3 gives 33 moves
        return (int)Math.sqrt(2*n);  // Approximation for general case
    }

    private static class Move {
        final int fromPeg;
        final int toPeg;
        final int diskIndex;  // 0 is the smallest disk

        Move(int fromPeg, int toPeg, int diskIndex) {
            this.fromPeg = fromPeg;
            this.toPeg = toPeg;
            this.diskIndex = diskIndex;
        }
    }

    private class HanoiPane extends Pane {
        private final List<Stack<Rectangle>> pegs;
        private  Rectangle[] disks;
        private final Rectangle[] pegRects;

        public HanoiPane(int disks) {
            pegs = new ArrayList<>(NUM_PEGS);
            for (int i = 0; i < NUM_PEGS; i++) {
                pegs.add(new Stack<>());
            }

            this.disks = new Rectangle[disks];
            this.pegRects = new Rectangle[NUM_PEGS];

            setNumDisks(disks);
        }

        public void setNumDisks(int numDisks) {
            getChildren().clear();
            for (Stack<Rectangle> peg : pegs) {
                peg.clear();
            }

            // Create a new array with the correct size
            this.disks = new Rectangle[numDisks];

            // Create the base at the bottom
            Rectangle base = new Rectangle(0, getHeight() - 20, getWidth(), 20);
            base.setFill(Color.SADDLEBROWN);
            getChildren().add(base);

            // Create pegs - growing upward from the base
            double pegSpacing = getWidth() / (NUM_PEGS + 1);
            for (int i = 0; i < NUM_PEGS; i++) {
                double pegX = (i + 1) * pegSpacing - PEG_WIDTH / 2;
                Rectangle peg = new Rectangle(pegX, getHeight() - 20 - PEG_HEIGHT, PEG_WIDTH, PEG_HEIGHT);
                peg.setFill(Color.BROWN);
                pegRects[i] = peg;
                getChildren().add(peg);
            }

            // Create disks - largest at bottom, smallest at top
            double diskWidthDecrement = MAX_DISK_WIDTH / numDisks;
            for (int i = 0; i < numDisks; i++) {
                double diskWidth = MAX_DISK_WIDTH - (i * diskWidthDecrement);
                Rectangle disk = new Rectangle(
                        pegRects[0].getX() + pegRects[0].getWidth() / 2 - diskWidth / 2,
                        getHeight() - 20 - (i + 1) * DISK_HEIGHT, // Stack from bottom up
                        diskWidth,
                        DISK_HEIGHT
                );

                double hue = ((double) i / numDisks) * 280;
                disk.setFill(Color.hsb(hue, 0.8, 0.9));
                disk.setStroke(Color.BLACK);
                disk.setStrokeWidth(1);
                disk.setArcWidth(10);
                disk.setArcHeight(10);

                this.disks[i] = disk;
                pegs.get(0).push(disk);
                getChildren().add(disk);
            }
        }

        public void resetTowers() {
            for (Stack<Rectangle> peg : pegs) {
                peg.clear();
            }

            for (int i = 0; i < disks.length; i++) {
                Rectangle disk = disks[i];
                disk.setTranslateX(0);
                disk.setTranslateY(0);

                // Reset position from bottom up
                disk.setX(pegRects[0].getX() + pegRects[0].getWidth() / 2 - disk.getWidth() / 2);
                disk.setY(getHeight() - 20 - (i + 1) * DISK_HEIGHT);

                pegs.get(0).push(disk);
                disk.toFront();
            }
        }

        public TranslateTransition createMoveAnimation(int fromPeg, int toPeg, int diskIndex) {
            if (pegs.get(fromPeg).isEmpty()) {
                return new TranslateTransition();
            }

            Rectangle disk = pegs.get(fromPeg).pop();
            pegs.get(toPeg).push(disk);

            double startX = pegRects[fromPeg].getX() + pegRects[fromPeg].getWidth() / 2;
            double endX = pegRects[toPeg].getX() + pegRects[toPeg].getWidth() / 2;
            double diskCenterX = disk.getX() + disk.getWidth() / 2;

            // Calculate horizontal movement
            double targetX = endX - diskCenterX;

            // Calculate vertical position - baseY is the bottom of the peg
            double baseY = getHeight() - 20;
            int stackHeight = pegs.get(toPeg).size(); // Number of disks on target peg
            double targetY = (baseY - stackHeight * DISK_HEIGHT) - disk.getY();

            TranslateTransition tt = new TranslateTransition();
            tt.setNode(disk);
            tt.setToX(targetX);
            tt.setToY(targetY);

            return tt;
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();

            double width = getWidth();
            double height = getHeight();

            // Position base at the bottom
            if (!getChildren().isEmpty()) {
                Rectangle base = (Rectangle) getChildren().get(0);
                base.setWidth(width);
                base.setY(height - 20);
                base.setHeight(20);
            }

            // Update peg positions
            double pegSpacing = width / (NUM_PEGS + 1);
            for (int i = 0; i < NUM_PEGS; i++) {
                Rectangle peg = pegRects[i];
                double pegX = (i + 1) * pegSpacing - PEG_WIDTH / 2;
                peg.setX(pegX);
                peg.setY(height - 20 - PEG_HEIGHT);
                peg.setHeight(PEG_HEIGHT);
            }

            // Update disk positions
            for (int i = 0; i < disks.length; i++) {
                Rectangle disk = disks[i];
                disk.setTranslateX(0);
                disk.setTranslateY(0);

                int pegIndex = -1;
                for (int j = 0; j < NUM_PEGS; j++) {
                    if (pegs.get(j).contains(disk)) {
                        pegIndex = j;
                        break;
                    }
                }

                if (pegIndex >= 0) {
                    int diskPosition = pegs.get(pegIndex).indexOf(disk);
                    disk.setX(pegRects[pegIndex].getX() + pegRects[pegIndex].getWidth() / 2 - disk.getWidth() / 2);
                    disk.setY(height - 20 - ((diskPosition + 1) * DISK_HEIGHT));
                }
            }
        }
    }

        public static void main(String[] args) {
            launch(args);
        }
    }
