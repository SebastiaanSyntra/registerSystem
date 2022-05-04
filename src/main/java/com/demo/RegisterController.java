package com.demo;


import com.fasterxml.jackson.databind.JsonNode;

import com.google.gson.Gson;
import io.joshworks.restclient.http.HttpResponse;
import io.joshworks.restclient.http.Json;
import io.joshworks.restclient.http.Unirest;
import javafx.beans.value.ObservableIntegerValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

@Component
@FxmlView
public class RegisterController implements Initializable {

    @FXML
    TextField barcodeBox;
    @FXML
    TextField employeeField;
    @FXML
    TableColumn articleColumn;
    @FXML
    TableColumn priceColumn;
    @FXML
    TableColumn categoryColumn;
    @FXML
    TableColumn amountColumn;
    @FXML
    TableColumn calculatedPriceColumn;
    @FXML
    TableView tableView;

    Article scannedArticle;



    public RegisterController(){}



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    public void newSaleHeader() throws IOException {
        URL url = new URL("http://localhost:8080/api/v1/sale-headers");
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        http.setRequestMethod("POST");
        http.setRequestProperty("Content-Type", "application/json");
        http.setRequestProperty("Accept", "application/json");
        http.setDoOutput(true);

        String data = "{\n  \"nameSalesPerson\": \"" + employeeField.getText() +"\"}";



        byte[] out = data.getBytes(StandardCharsets.UTF_8);
        OutputStream stream = http.getOutputStream();
        stream.write(out);


        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(http.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
        }

        http.disconnect();
        System.out.println("het werkt");


    }

    public void scanArticle(){
        //SMTPH-Iph12-1
        HttpResponse<Json> apiResponse =  Unirest.get("http://localhost:8080/api/v1/articles/"+barcodeBox.getText()).asJson();

        scannedArticle =  new Gson().fromJson(apiResponse.body().toString(), Article.class);


        articleColumn.setCellValueFactory(
                new PropertyValueFactory<>("Article"));

        priceColumn.setCellValueFactory(
                new PropertyValueFactory<>("Price"));

        categoryColumn.setCellValueFactory(
                new PropertyValueFactory<>("Category"));


        tableView.getItems().add(scannedArticle);


//        if (!articleColumn.getText().contains(scannedArticle.getArticle())) {
//
//        } else {
//            int newAmount = 0;
//            newAmount = Integer.parseInt(amountColumn.getCellFactory().toString()) + 1;
//            amountColumn.setCellValueFactory(
//                    new PropertyValueFactory<>(String.valueOf(newAmount)));
//        }
    }


    public void payCash() throws IOException {
        URL url = new URL("http://localhost:8080/api/v1/sale-headers");
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        http.setRequestMethod("POST");
        http.setRequestProperty("Content-Type", "application/json");
        http.setRequestProperty("Accept", "application/json");
        http.setDoOutput(true);

        String data = "{\n" +
                "    \"barcode\":\"SMTPH-Iph12-1\",\n" +
                "    \"quantity\": 5,\n" +
                "    \"unitPrice\": 5,\n" +
                "    \"saleHeaderId\":1\n" +
                "}";

        byte[] out = data.getBytes(StandardCharsets.UTF_8);
        OutputStream stream = http.getOutputStream();
        stream.write(out);
       http.disconnect();


    }

}
