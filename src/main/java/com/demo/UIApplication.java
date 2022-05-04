			package com.demo;

			import javafx.application.Application;
			import org.springframework.boot.autoconfigure.SpringBootApplication;
			import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

			@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
			public class UIApplication {

				public static void main(String[] args) {
					Application.launch(RegisterApplication.class, args);

				}

			}
