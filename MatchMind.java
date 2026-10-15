import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * NEURAL_MATCH_OS // Sistema de entrenamiento cognitivo y memoria visual.
 * Desarrollado con JavaFX bajo estética Cyberpunk / DedSec.
 * 
 * @author Ricardo Martínez (Rickcm22)
 */
public class MatchMind extends Application {

    // --- CONFIGURACIÓN DEL JUEGO ---
    private static final int BOARD_SIZE = 4;
    private static final int TOTAL_PAIRS = 8;

    // --- ESTADOS Y COMPONENTES ---
    private final Button[][] cards = new Button[BOARD_SIZE][BOARD_SIZE];
    private final int[][] cardValues = new int[BOARD_SIZE][BOARD_SIZE];
    private final boolean[][] revealed = new boolean[BOARD_SIZE][BOARD_SIZE];

    private int firstSelectionRow = -1;
    private int firstSelectionCol = -1;
    private int pairsFound = 0;
    private int attempts = 0;
    private boolean blockClicks = false; // Bandera para evitar doble clic durante transiciones

    private Label attemptsLabel;
    private Label pairsLabel;
    private Label timerLabel;
    private long startTime;
    private Timer timer;

    // --- PALETA DE ESTILOS DEDSEC (UI/UX) ---
    private static final String BG_COLOR = "#0D0D0D";
    private static final String PURPLE_NEON = "#BC00FF";
    private static final String GREEN_NEON = "#00FF41";
    private static final String CARD_BACK = "#1A1A1A";
    private static final String FONT_STYLE = "-fx-font-family: 'Courier New';";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("NEURAL_MATCH_OS // Rickcm22");

        initializeGame();

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle(
                "-fx-padding: 20; " +
                        "-fx-background-color: " + BG_COLOR + "; " +
                        "-fx-border-color: " + PURPLE_NEON + "; " +
                        "-fx-border-width: 2;");

        VBox infoPanel = createInfoPanel();
        GridPane board = createBoard();

        Button resetButton = new Button("REBOOT_SYSTEM");
        resetButton.setStyle(
                "-fx-font-size: 14; " + FONT_STYLE +
                        "-fx-background-color: transparent; " +
                        "-fx-text-fill: " + PURPLE_NEON + "; " +
                        "-fx-border-color: " + PURPLE_NEON + ";");
        resetButton.setOnAction(e -> resetGame());

        root.getChildren().addAll(infoPanel, board, resetButton);

        Scene scene = new Scene(root, 500, 650);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        startTimer();
    }

    /**
     * Crea el panel superior con las estadísticas del sistema (Intentos, Pares y
     * Uptime).
     */
    private VBox createInfoPanel() {
        VBox infoPanel = new VBox(10);
        infoPanel.setAlignment(Pos.CENTER);

        Label title = new Label(">> NEURAL_MATCH_OS");
        title.setStyle("-fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: " + PURPLE_NEON + "; " + FONT_STYLE);

        attemptsLabel = new Label("ATTEMPTS: 0");
        pairsLabel = new Label("PAIRS_SYNCED: 0/" + TOTAL_PAIRS);
        timerLabel = new Label("UPTIME: 0s");

        String labelStyle = "-fx-font-size: 14; -fx-text-fill: " + GREEN_NEON + "; " + FONT_STYLE;
        attemptsLabel.setStyle(labelStyle);
        pairsLabel.setStyle(labelStyle);
        timerLabel.setStyle(labelStyle);

        infoPanel.getChildren().addAll(title, attemptsLabel, pairsLabel, timerLabel);
        return infoPanel;
    }

    /**
     * Construye la cuadrícula (GridPane) del tablero de juego.
     */
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

    /**
     * Inicializa cada tarjeta individual del tablero.
     */
    private Button createCard(int row, int col) {
        Button card = new Button("?");
        card.setPrefSize(85, 85);
        card.setStyle(
                "-fx-font-size: 28; " +
                        "-fx-background-color: " + CARD_BACK + "; " +
                        "-fx-text-fill: " + PURPLE_NEON + "; " +
                        "-fx-border-color: " + PURPLE_NEON + "; " +
                        "-fx-border-radius: 5;");

        card.setOnAction(e -> handleCardClick(row, col));
        return card;
    }

    /**
     * Gestiona la lógica al hacer clic en una tarjeta del tablero.
     */
    private void handleCardClick(int row, int col) {
        if (blockClicks || revealed[row][col] || (firstSelectionRow == row && firstSelectionCol == col)) {
            return;
        }

        revealCard(row, col);

        // Si es la primera tarjeta seleccionada del par
        if (firstSelectionRow == -1) {
            firstSelectionRow = row;
            firstSelectionCol = col;
        } else {
            // Es la segunda tarjeta seleccionada
            attempts++;
            attemptsLabel.setText("ATTEMPTS: " + attempts);

            if (cardValues[row][col] == cardValues[firstSelectionRow][firstSelectionCol]) {
                // Coincidencia encontrada (Match)
                revealed[row][col] = true;
                revealed[firstSelectionRow][firstSelectionCol] = true;
                pairsFound++;
                pairsLabel.setText("PAIRS_SYNCED: " + pairsFound + "/" + TOTAL_PAIRS);

                applyMatchStyle(row, col);
                applyMatchStyle(firstSelectionRow, firstSelectionCol);

                if (pairsFound == TOTAL_PAIRS) {
                    endGame();
                }

                resetSelection();
            } else {
                // No hay coincidencia -> Bloquea clics temporalmente y oculta las cartas
                blockClicks = true;
                PauseTransition pause = new PauseTransition(Duration.seconds(0.8));
                pause.setOnFinished(event -> {
                    hideCard(row, col);
                    hideCard(firstSelectionRow, firstSelectionCol);
                    resetSelection();
                    blockClicks = false;
                });
                pause.play();
            }
        }
    }

    private void revealCard(int row, int col) {
        Button card = cards[row][col];
        card.setText(String.valueOf(cardValues[row][col]));
        card.setStyle(
                "-fx-font-size: 28; " +
                        "-fx-background-color: " + PURPLE_NEON + "; " +
                        "-fx-text-fill: black; " +
                        "-fx-font-weight: bold;");
    }

    private void hideCard(int row, int col) {
        if (!revealed[row][col]) {
            Button card = cards[row][col];
            card.setText("?");
            card.setStyle(
                    "-fx-font-size: 28; " +
                            "-fx-background-color: " + CARD_BACK + "; " +
                            "-fx-text-fill: " + PURPLE_NEON + "; " +
                            "-fx-border-color: " + PURPLE_NEON + ";");
        }
    }

    private void applyMatchStyle(int row, int col) {
        cards[row][col].setStyle(
                "-fx-background-color: #002200; " +
                        "-fx-text-fill: " + GREEN_NEON + "; " +
                        "-fx-border-color: " + GREEN_NEON + "; " +
                        "-fx-font-size: 28;");
    }

    private void resetSelection() {
        firstSelectionRow = -1;
        firstSelectionCol = -1;
    }

    /**
     * Distribuye valores aleatorios en la matriz del juego y reinicia contadores.
     */
    private void initializeGame() {
        List<Integer> values = new ArrayList<>();
        for (int i = 1; i <= TOTAL_PAIRS; i++) {
            values.add(i);
            values.add(i);
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
        resetSelection();
        blockClicks = false;
    }

    private void startTimer() {
        startTime = System.currentTimeMillis();
        if (timer != null)
            timer.cancel();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateTimer();
            }
        }, 0, 1000);
    }

    private void updateTimer() {
        long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        javafx.application.Platform.runLater(() -> timerLabel.setText("UPTIME: " + elapsedSeconds + "s"));
    }

    /**
     * Muestra la ventana de victoria al completar todos los pares.
     */
    private void endGame() {
        timer.cancel();
        long totalTime = (System.currentTimeMillis() - startTime) / 1000;

        javafx.application.Platform.runLater(() -> {
            Stage resultStage = new Stage();
            VBox resultBox = new VBox(20);
            resultBox.setAlignment(Pos.CENTER);
            resultBox.setStyle(
                    "-fx-padding: 30; " +
                            "-fx-background-color: black; " +
                            "-fx-border-color: " + GREEN_NEON + "; " +
                            "-fx-border-width: 3;");

            Label congrats = new Label("SYSTEM_COMPROMISED");
            congrats.setStyle(
                    "-fx-font-size: 22; " + FONT_STYLE + " -fx-font-weight: bold; -fx-text-fill: " + GREEN_NEON);

            Label stats = new Label("Time: " + totalTime + "s\nAttempts: " + attempts + "\n\nAccess Granted.");
            stats.setStyle("-fx-font-size: 14; " + FONT_STYLE + " -fx-text-fill: white; -fx-text-alignment: center;");

            Button closeButton = new Button("[ CLOSE_SESSION ]");
            closeButton.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-text-fill: " + GREEN_NEON + "; " +
                            "-fx-border-color: " + GREEN_NEON + "; " +
                            FONT_STYLE);
            closeButton.setOnAction(e -> resultStage.close());

            resultBox.getChildren().addAll(congrats, stats, closeButton);
            resultStage.setScene(new Scene(resultBox, 350, 250));
            resultStage.show();
        });
    }

    private void resetGame() {
        if (timer != null)
            timer.cancel();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                hideCard(row, col);
            }
        }
        initializeGame();
        attemptsLabel.setText("ATTEMPTS: 0");
        pairsLabel.setText("PAIRS_SYNCED: 0/" + TOTAL_PAIRS);
        startTimer();
    }

    public static void main(String[] args) {
        launch(args);
    }
}