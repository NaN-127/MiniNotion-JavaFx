package org.duo.notionminimain1;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.*;

public class Main extends Application {

    private final ArrayList<String> pages = new ArrayList<>();
    private final HashMap<String, String> pageContents = new HashMap<>();
    private final Stack<String> undoStack = new Stack<>();
    private String currentPage = null;
    private boolean isContentChanging = false;

    private ListView<String> listView;
    private TextField textField;
    private TextArea contentArea;
    private Label text;
    private Button addButton;
    private Button deleteButton;
    private Button undoButton;

    @Override
    public void start(Stage scaffold) {
        BorderPane flutterScaffold = new BorderPane();
        Scene scene = new Scene(flutterScaffold, 1000, 700);
        scene.setFill(Color.web("#121212"));

        setupDrawer(flutterScaffold);
        setupBody(flutterScaffold);

        updateEditorForNoPage();

        scaffold.setTitle("Mini Notion");
        scaffold.setScene(scene);
        scaffold.show();
    }

    private void setupDrawer(BorderPane flutterScaffold) {
        VBox column = new VBox(10);
        column.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 10;");
        column.setPrefWidth(200);

        Label textHeading = new Label("PAGES");
        textHeading.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        HBox row = new HBox(10);

        addButton = createButton("+", "Add new page");
        deleteButton = createButton("-", "Delete current page");
        undoButton = createButton("Undo", "Undo last change");

        row.getChildren().addAll(addButton, deleteButton, undoButton);

        listView = new ListView<>();
        listView.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: white;");

        column.getChildren().addAll(textHeading, row, listView);
        flutterScaffold.setLeft(column);

        addButton.setOnAction(e -> addNewPage());
        deleteButton.setOnAction(e -> deleteCurrentPage());
        undoButton.setOnAction(e -> undoLastChange());

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (oldVal != null) {
                pageContents.put(oldVal, contentArea.getText());
            }
            if (newVal != null) {
                loadPage(newVal);
            }
        });
    }

    private void setupBody(BorderPane flutterScaffold) {
        VBox column = new VBox(10);
        column.setStyle("-fx-background-color: #121212; -fx-padding: 10;");

        textField = new TextField();
        textField.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white;");
        textField.setPromptText("Page title...");
        textField.setOnAction(e -> updatePageTitle());

        contentArea = new TextArea();
        contentArea.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: white; -fx-prompt-text-fill: white;");
        contentArea.setPromptText("Write your content here...");

        contentArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (currentPage != null && !isContentChanging) {
                undoStack.push(oldVal);
                updateStatus("Editing: " + currentPage);
            }
        });

        text = new Label("Ready");
        text.setStyle("-fx-text-fill: #b0b0b0;");

        column.getChildren().addAll(textField, contentArea, text);
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        flutterScaffold.setCenter(column);
    }

    private Button createButton(String label, String tooltipText) {
        Button btn = new Button(label);
        btn.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");
        btn.setTooltip(new Tooltip(tooltipText));
        return btn;
    }

    private void addNewPage() {
        String newPage = "Page " + (pages.size() + 1);
        pages.add(newPage);
        pageContents.put(newPage, "");
        listView.getItems().add(newPage);
        listView.getSelectionModel().select(newPage);
        textField.requestFocus();
        updateStatus("Created: " + newPage);
    }

    private void loadPage(String page) {
        isContentChanging = true;
        currentPage = page;
        textField.setText(page);
        contentArea.setText(pageContents.getOrDefault(page, ""));
        isContentChanging = false;
        updateStatus("Viewing: " + page);
    }

    private void deleteCurrentPage() {
        if (currentPage == null || pages.size() == 1) {
            updateStatus("Cannot delete the only page.");
            return;
        }

        pages.remove(currentPage);
        pageContents.remove(currentPage);
        listView.getItems().remove(currentPage);

        if (!pages.isEmpty()) {
            loadPage(pages.get(0));
        } else {
            updateEditorForNoPage();
        }

        updateStatus("Deleted page");
    }

    private void updatePageTitle() {
        if (currentPage == null) return;

        String newTitle = textField.getText().trim();
        if (newTitle.isEmpty()) {
            textField.setText(currentPage);
            return;
        }

        String content = pageContents.remove(currentPage);
        pageContents.put(newTitle, content);

        int index = pages.indexOf(currentPage);
        pages.set(index, newTitle);

        int listIndex = listView.getItems().indexOf(currentPage);
        listView.getItems().set(listIndex, newTitle);

        currentPage = newTitle;
        listView.getSelectionModel().select(newTitle);
        updateStatus("Renamed to: " + newTitle);
    }

    private void undoLastChange() {
        if (undoStack.isEmpty() || currentPage == null) {
            updateStatus("Nothing to undo");
            return;
        }

        isContentChanging = true;
        String previousContent = undoStack.pop();
        contentArea.setText(previousContent);
        pageContents.put(currentPage, previousContent);
        isContentChanging = false;
        updateStatus("Undo successful");
    }

    private void updateStatus(String message) {
        text.setText(message);
    }

    private void updateEditorForNoPage() {
        currentPage = null;
        textField.setText("");
        contentArea.setText("");
        updateStatus("No pages. Click + to add one.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}