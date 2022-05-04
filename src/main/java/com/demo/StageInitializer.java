package com.demo;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class StageInitializer implements ApplicationListener<RegisterApplication.StageReadyEvent> {

    @Value("classpath:/Register.fxml")
    private Resource registerResource;
    private String applicationTitle;
    private ApplicationContext applicationContext;



    public StageInitializer(@Value("${spring.application.ui.title}") String applicationTitle,
                            ApplicationContext applicationContext) {
        this.applicationTitle = applicationTitle;
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(RegisterApplication.StageReadyEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(registerResource.getURL());
            fxmlLoader.setControllerFactory(RegisterController -> applicationContext.getBean(RegisterController));
            Parent parent = fxmlLoader.load();
        Stage stage = event.getStage();
        stage.setScene(new Scene(parent, 800, 600));
        stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
