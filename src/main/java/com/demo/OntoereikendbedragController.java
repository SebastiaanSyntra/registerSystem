package com.demo;

import javafx.application.Platform;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.awt.*;

public class OntoereikendbedragController {

    @FXML
    Button okayButton = new Button();

    public void closeWindow(){
        Stage stage = (Stage) okayButton.getScene().getWindow();
        stage.close();

    }
}
