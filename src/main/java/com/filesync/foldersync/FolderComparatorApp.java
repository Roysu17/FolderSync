package com.filesync.foldersync;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class FolderComparatorApp extends Application {

    private TextField folderPath1Field;
    private TextField folderPath2Field;
    private TextArea resultArea;
    private ProgressBar progressBar;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Folder Comparator");

        Label folder1Label = new Label("Folder 1:");
        folderPath1Field = new TextField();
        Button folder1Button = new Button("Browse...");
        folder1Button.setOnAction(e -> chooseDirectory(primaryStage, folderPath1Field));

        Label folder2Label = new Label("Folder 2:");
        folderPath2Field = new TextField();
        Button folder2Button = new Button("Browse...");
        folder2Button.setOnAction(e -> chooseDirectory(primaryStage, folderPath2Field));

        Button compareButton = new Button("Compare and Sync");
        compareButton.setOnAction(e -> compareFolders());

        resultArea = new TextArea();
        resultArea.setEditable(false);

        Label progressLabel = new Label("Progress:");
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300); // Set the preferred width of the progress bar

        HBox progressBox = new HBox(10, progressLabel, progressBar);
        progressBox.setAlignment(Pos.CENTER_LEFT);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.getChildren().addAll(
            folder1Label, folderPath1Field, folder1Button,
            folder2Label, folderPath2Field, folder2Button,
            compareButton, progressBox, resultArea
        );

        Scene scene = new Scene(layout, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void chooseDirectory(Stage stage, TextField textField) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            textField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    private void compareFolders() {
        String folderPath1 = folderPath1Field.getText();
        String folderPath2 = folderPath2Field.getText();

        FolderComparatorTask task = new FolderComparatorTask(folderPath1, folderPath2);

        task.setOnSucceeded(e -> {
            System.out.println("Comparison completed, back in GUI");
            //List<String> differences = task.getValue();
            resultArea.clear();
            resultArea.appendText("Comparison completed.\n\n");
            resultArea.appendText("Differences have been saved to the" + folderPath2 + "folder as a text file called 'differences.txt'.\n");
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
        });

        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error comparing folders: " + exception.getMessage(), ButtonType.OK);
            alert.showAndWait();
            resultArea.clear();
            resultArea.appendText("Comparison Failed, but some files may have been compared.\n\n");
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
        });

        progressBar.progressProperty().bind(task.progressProperty());

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}