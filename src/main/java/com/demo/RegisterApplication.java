package com.demo;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

public class RegisterApplication extends Application {

    private ConfigurableApplicationContext applicationContext;


    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(RegisterApplication.class.getResource("/Register.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Kassa");
        stage.setScene(scene);
        stage.show();

        applicationContext.publishEvent(new StageReadyEvent(stage));
    }


    @Override
    public void init(){
        applicationContext = new SpringApplicationBuilder(UIApplication.class).run();
    }

    @Override
    public void stop() throws Exception {
        applicationContext.stop();
        Platform.exit();
    }

    static class StageReadyEvent extends ApplicationEvent {
        public StageReadyEvent(Stage stage) {
            super(stage);
        }
        public Stage getStage(){
            return ((Stage) getSource());
        }
    }
}
