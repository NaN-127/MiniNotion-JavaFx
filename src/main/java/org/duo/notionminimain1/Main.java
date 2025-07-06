package org.duo.notionminimain1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

public class Main extends Application {

    private final ArrayList<String> pages = new ArrayList<>();
    private final HashMap<String, String> pageContents = new HashMap<>();
    private final Stack<String> undoStack = new Stack<>();
    private final LinkedList<String> recentPages = new LinkedList<>();

    private final ListView<String> pageList = new ListView<>();
    private final TextField titleField = new TextField();
    private final TextArea contentArea = new TextArea();
    private final Label statusLabel = new Label("Welcome to Mini Notion");
    private final Label titleLabel = new Label("PAGES");
    private final Label emptyStateLabel = new Label("No pages yet\nClick + to create your first page");

    private String currentPage = null;

    @Override
    public void start(Stage stage) {
        FontIcon addIcon = new FontIcon(Feather.PLUS);
        FontIcon undoIcon = new FontIcon(Feather.CORNER_UP_LEFT);
        FontIcon helpIcon = new FontIcon(Feather.HELP_CIRCLE);
        FontIcon pagesIcon = new FontIcon(Feather.FILE_TEXT);
        FontIcon deleteIcon = new FontIcon(Feather.TRASH_2);

        Color iconColor = Color.web("#9ca3af");
        addIcon.setIconColor(iconColor);
        undoIcon.setIconColor(iconColor);
        helpIcon.setIconColor(iconColor);
        pagesIcon.setIconColor(iconColor);
        deleteIcon.setIconColor(iconColor);

        Button addPageBtn = new Button("", addIcon);
        addPageBtn.setTooltip(new Tooltip("Create new page (Ctrl+N)"));

        Button undoBtn = new Button("", undoIcon);
        undoBtn.setTooltip(new Tooltip("Undo last change (Ctrl+Z)"));

        Button helpBtn = new Button("", helpIcon);
        helpBtn.setTooltip(new Tooltip("Show help"));

        Button deleteBtn = new Button("", deleteIcon);
        deleteBtn.setTooltip(new Tooltip("Delete current page (Del)"));

        emptyStateLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-style: italic;");
        emptyStateLabel.setPadding(new Insets(20));

        StackPane listContainer = new StackPane(pageList, emptyStateLabel);
        updateEmptyStateVisibility();

        VBox sidebar = new VBox(
                new HBox(5, pagesIcon, titleLabel),
                new HBox(10, addPageBtn, undoBtn, deleteBtn, helpBtn),
                listContainer
        );
        sidebar.setPrefWidth(220);
        sidebar.setPadding(new Insets(0, 10, 10, 10));
        sidebar.setStyle("-fx-background-color: #1e1e1e;");

        titleField.setPromptText("Page title...");
        titleField.setStyle("-fx-prompt-text-fill: #6b7280;");

        contentArea.setPromptText("Write your notes here...\n\nTip: Use Ctrl+Z to undo changes");
        contentArea.setStyle("-fx-prompt-text-fill: #6b7280; -fx-control-inner-background: #1e1e1e; -fx-text-fill: white;");

        VBox editor = new VBox(10, titleField, contentArea, statusLabel);
        editor.setPadding(new Insets(10));
        editor.setStyle("-fx-background-color: #1a1a1a;");
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        statusLabel.setStyle("-fx-text-fill: #9ca3af;");

        BorderPane root = new BorderPane();
        root.setTop(new MenuBar());
        root.setLeft(sidebar);
        root.setCenter(editor);
        root.setStyle("-fx-background-color: #1a1a1a;");

        Scene scene = new Scene(root, 1000, 700);
        scene.setFill(Color.TRANSPARENT);

        try {
            scene.getStylesheets().add(getClass().getResource("/styles/theme.css").toExternalForm());
        } catch (NullPointerException e) {
            System.err.println("Dark theme CSS not found. Using inline styles.");
        }

        stage.setTitle("Mini Notion");
        stage.setScene(scene);
        stage.show();

        addPageBtn.setOnAction(e -> addNewPage());
        undoBtn.setOnAction(e -> undoLastChange());
        helpBtn.setOnAction(e -> showHelpDialog());
        deleteBtn.setOnAction(e -> deleteCurrentPage());

        titleField.setOnAction(e -> updatePageTitle());
        titleField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) updatePageTitle();
        });

        pageList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (oldVal != null) pageContents.put(oldVal, contentArea.getText());
            if (newVal != null) loadPage(newVal);
            else if (pageList.getItems().isEmpty()) clearEditor();
        });

        contentArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (currentPage != null && !newVal.equals(pageContents.get(currentPage))) {
                undoStack.push(oldVal);
                updateStatus("Editing: " + currentPage);
            }
        });

        scene.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown()) {
                switch (event.getCode()) {
                    case N: addNewPage(); break;
                    case Z: undoLastChange(); break;
                }
            } else if (event.getCode() == javafx.scene.input.KeyCode.DELETE) {
                deleteCurrentPage();
            }
        });

        pageList.getItems().addListener((javafx.collections.ListChangeListener<String>) c -> {
            updateEmptyStateVisibility();
        });

        addNewPage();
    }

    private void updateEmptyStateVisibility() {
        emptyStateLabel.setVisible(pageList.getItems().isEmpty());
    }

    private void addNewPage() {
        String newPage = "Untitled " + (pages.size() + 1);
        pages.add(newPage);
        pageContents.put(newPage, "");
        pageList.getItems().add(newPage);
        pageList.getSelectionModel().select(newPage);
        titleField.requestFocus();
        updateStatus("Created: " + newPage);
    }

    private void loadPage(String page) {
        currentPage = page;
        titleField.setText(page);
        contentArea.setText(pageContents.getOrDefault(page, ""));
        recentPages.addFirst(page);
        updateStatus("Editing: " + page);
    }

    private void clearEditor() {
        currentPage = null;
        titleField.setText("");
        contentArea.setText("");
    }

    private void deleteCurrentPage() {
        if (currentPage == null) {
            updateStatus("No page selected to delete");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Page");
        alert.setHeaderText("Delete '" + currentPage + "'?");
        alert.setContentText("This action cannot be undone.");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1e1e1e;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                int index = pages.indexOf(currentPage);
                pages.remove(currentPage);
                pageContents.remove(currentPage);
                pageList.getItems().remove(currentPage);

                if (!pages.isEmpty()) {
                    int newIndex = Math.min(index, pages.size() - 1);
                    loadPage(pages.get(newIndex));
                } else {
                    addNewPage();
                }
                updateStatus("Deleted page");
            }
        });
    }

    private void updatePageTitle() {
        if (currentPage == null) return;

        String newTitle = titleField.getText().trim();
        if (newTitle.isEmpty()) {
            titleField.setText(currentPage);
            updateStatus("Page title cannot be empty");
            return;
        }

        if (newTitle.equals(currentPage)) return;

        String content = pageContents.remove(currentPage);
        pageContents.put(newTitle, content);

        int index = pages.indexOf(currentPage);
        pages.set(index, newTitle);

        int lvIndex = pageList.getItems().indexOf(currentPage);
        pageList.getItems().set(lvIndex, newTitle);

        currentPage = newTitle;
        pageList.getSelectionModel().select(newTitle);
        updateStatus("Renamed page to: " + newTitle);
    }

    private void undoLastChange() {
        if (currentPage != null && !undoStack.isEmpty()) {
            String previousContent = undoStack.pop();
            contentArea.setText(previousContent);
            pageContents.put(currentPage, previousContent);
            updateStatus("Undone: " + currentPage);
        } else {
            updateStatus("Nothing to undo");
        }
    }

    private void showHelpDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText("Mini Notion Help");
        alert.setContentText(
                "How to use:\n\n" +
                        "• Click + to create new pages\n" +
                        "• Edit page titles by clicking them\n" +
                        "• Use Ctrl+Z to undo changes\n" +
                        "• Press Del to delete current page\n" +
                        "• All changes are saved automatically\n\n" +
                        "Shortcuts:\n" +
                        "Ctrl+N - New page\n" +
                        "Ctrl+Z - Undo\n" +
                        "Del - Delete current page"
        );

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1e1e1e;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");

        alert.showAndWait();
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    public static void main(String[] args) {
        launch();
    }
}