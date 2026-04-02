import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.util.*;

public class MatchMind extends Application {
    private static final int BOARD_SIZE = 4;
    private static final int TOTAL_PAIRS = 8;
    
    private Button[][] cards = new Button[BOARD_SIZE][BOARD_SIZE];
    private int[][] cardValues = new int[BOARD_SIZE][BOARD_SIZE];
    private boolean[][] revealed = new boolean[BOARD_SIZE][BOARD_SIZE];
    
    private int firstSelectionRow = -1;
    private int firstSelectionCol = -1;
    private int pairsFound = 0;
    private int attempts = 0;
    
    private Label attemptsLabel;
    private Label pairsLabel;
    private Label timerLabel;
    private long startTime;
    private Timer timer;

    // --- ESTILOS DEDSEC ---
    private final String BG_COLOR = "#0D0D0D";
    private final String PURPLE_NEON = "#BC00FF";
    private final String GREEN_NEON = "#00FF41";
    private final String CARD_BACK = "#1A1A1A";
    private final String FONT_STYLE = "-fx-font-family: 'Courier New';";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("NEURAL_MATCH_OS // Rickcm22");
        
        initializeGame();
        
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 20; -fx-background-color: " + BG_COLOR + "; -fx-border-color: " + PURPLE_NEON + "; -fx-border-width: 2;");
        
        VBox infoPanel = createInfoPanel();
        GridPane board = createBoard();
        
        Button resetButton = new Button("REBOOT_SYSTEM");
        resetButton.setStyle("-fx-font-size: 14; " + FONT_STYLE + " -fx-background-color: transparent; -fx-text-fill: " + PURPLE_NEON + "; -fx-border-color: " + PURPLE_NEON + ";");
        resetButton.setOnAction(e -> resetGame());
        
        root.getChildren().addAll(infoPanel, board, resetButton);
        
        Scene scene = new Scene(root, 500, 650);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        
        startTimer();
    }
    
    private VBox createInfoPanel() {
        VBox infoPanel = new VBox(10);
        infoPanel.setAlignment(Pos.CENTER);
        
        Label title = new Label(">> NEURAL_MATCH_OS");
        title.setStyle("-fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: " + PURPLE_NEON + "; " + FONT_STYLE);
        
        attemptsLabel = new Label("ATTEMPTS: 0");
        pairsLabel = new Label("PAIRS_SYNCED: 0/8");
        timerLabel = new Label("UPTIME: 0s");
        
        String labelStyle = "-fx-font-size: 14; -fx-text-fill: " + GREEN_NEON + "; " + FONT_STYLE;
        attemptsLabel.setStyle(labelStyle);
        pairsLabel.setStyle(labelStyle);
        timerLabel.setStyle(labelStyle);
        
        infoPanel.getChildren().addAll(title, attemptsLabel, pairsLabel, timerLabel);
        return infoPanel;
    }
    
    private GridPane createBoard() {
        GridPane board = new GridPane();
        board.setAlignment(Pos.CENTER);
        board.setHgap(12);
        board.setVgap(12);
        
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Button card = createCard(row, col);
                cards[row][col] = card;
                board.add(card, col, row);
            }
        }
        return board;
    }
    
    private Button createCard(int row, int col) {
        Button card = new Button("?");
        card.setPrefSize(85, 85);
        card.setStyle("-fx-font-size: 28; -fx-background-color: " + CARD_BACK + "; -fx-text-fill: " + PURPLE_NEON + "; -fx-border-color: " + PURPLE_NEON + "; -fx-border-radius: 5;");
        
        card.setOnAction(e -> handleCardClick(row, col));
        return card;
    }
    
    private void handleCardClick(int row, int col) {
        if (revealed[row][col] || (firstSelectionRow != -1 && firstSelectionCol != -1)) return;
        
        revealCard(row, col);
        
        if (firstSelectionRow == -1) {
            firstSelectionRow = row;
            firstSelectionCol = col;
        } else {
            attempts++;
            attemptsLabel.setText("ATTEMPTS: " + attempts);
            
            if (cardValues[row][col] == cardValues[firstSelectionRow][firstSelectionCol]) {
                revealed[row][col] = true;
                revealed[firstSelectionRow][firstSelectionCol] = true;
                pairsFound++;
                pairsLabel.setText("PAIRS_SYNCED: " + pairsFound + "/8");
                
                cards[row][col].setStyle("-fx-background-color: #002200; -fx-text-fill: " + GREEN_NEON + "; -fx-border-color: " + GREEN_NEON + "; -fx-font-size: 28;");
                cards[firstSelectionRow][firstSelectionCol].setStyle("-fx-background-color: #002200; -fx-text-fill: " + GREEN_NEON + "; -fx-border-color: " + GREEN_NEON + "; -fx-font-size: 28;");
                
                if (pairsFound == TOTAL_PAIRS) endGame();
                
                firstSelectionRow = -1;
                firstSelectionCol = -1;
            } else {
                PauseTransition pause = new PauseTransition(Duration.seconds(0.8));
                pause.setOnFinished(event -> {
                    hideCard(row, col);
                    hideCard(firstSelectionRow, firstSelectionCol);
                    firstSelectionRow = -1;
                    firstSelectionCol = -1;
                });
                pause.play();
            }
        }
    }
    
    private void revealCard(int row, int col) {
        Button card = cards[row][col];
        card.setText(String.valueOf(cardValues[row][col]));
        card.setStyle("-fx-font-size: 28; -fx-background-color: " + PURPLE_NEON + "; -fx-text-fill: black; -fx-font-weight: bold;");
    }
    
    private void hideCard(int row, int col) {
        if (!revealed[row][col]) {
            Button card = cards[row][col];
            card.setText("?");
            card.setStyle("-fx-font-size: 28; -fx-background-color: " + CARD_BACK + "; -fx-text-fill: " + PURPLE_NEON + "; -fx-border-color: " + PURPLE_NEON + ";");
        }
    }
    
    private void initializeGame() {
        List<Integer> values = new ArrayList<>();
        for (int i = 1; i <= TOTAL_PAIRS; i++) {
            values.add(i); values.add(i);
        }
        Collections.shuffle(values);
        
        int index = 0;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                cardValues[row][col] = values.get(index++);
                revealed[row][col] = false;
            }
        }
        pairsFound = 0;
        attempts = 0;
        firstSelectionRow = -1;
        firstSelectionCol = -1;
    }
    
    private void startTimer() {
        startTime = System.currentTimeMillis();
        if (timer != null) timer.cancel();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() { updateTimer(); }
        }, 0, 1000);
    }
    
    private void updateTimer() {
        long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        javafx.application.Platform.runLater(() -> timerLabel.setText("UPTIME: " + elapsedSeconds + "s"));
    }
    
    private void endGame() {
        timer.cancel();
        long totalTime = (System.currentTimeMillis() - startTime) / 1000;
        
        javafx.application.Platform.runLater(() -> {
            Stage resultStage = new Stage();
            VBox resultBox = new VBox(20);
            resultBox.setAlignment(Pos.CENTER);
            resultBox.setStyle("-fx-padding: 30; -fx-background-color: black; -fx-border-color: " + GREEN_NEON + "; -fx-border-width: 3;");
            
            Label congrats = new Label("SYSTEM_COMPROMISED");
            congrats.setStyle("-fx-font-size: 22; " + FONT_STYLE + " -fx-font-weight: bold; -fx-text-fill: " + GREEN_NEON);
            
            Label stats = new Label("Time: " + totalTime + "s\nAttempts: " + attempts + "\n\nAccess Granted.");
            stats.setStyle("-fx-font-size: 14; " + FONT_STYLE + " -fx-text-fill: white; -fx-text-alignment: center;");
            
            Button closeButton = new Button("[ CLOSE_SESSION ]");
            closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + GREEN_NEON + "; -fx-border-color: " + GREEN_NEON + "; " + FONT_STYLE);
            closeButton.setOnAction(e -> resultStage.close());
            
            resultBox.getChildren().addAll(congrats, stats, closeButton);
            resultStage.setScene(new Scene(resultBox, 350, 250));
            resultStage.show();
        });
    }
    
    private void resetGame() {
        if (timer != null) timer.cancel();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) hideCard(row, col);
        }
        initializeGame();
        attemptsLabel.setText("ATTEMPTS: 0");
        pairsLabel.setText("PAIRS_SYNCED: 0/8");
        startTimer();
    }
    
    public static void main(String[] args) { launch(args); }
}