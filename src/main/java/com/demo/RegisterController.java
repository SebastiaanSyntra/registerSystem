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

    List<Article> articleList = new ArrayList<>();
    String saleHeaderId;
    boolean articleAlreadyScanned;
    int articleFoundIndex;



    public RegisterController(){}



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        articleColumn.setCellValueFactory(
                new PropertyValueFactory<>("Article"));

        priceColumn.setCellValueFactory(
                new PropertyValueFactory<>("Price"));

        categoryColumn.setCellValueFactory(
                new PropertyValueFactory<>("Category"));
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
            saleHeaderId = response.toString();
            System.out.println(response.toString());
        }
        http.disconnect();


    }

    public void scanArticle() {

        int index = 0;

        //SMTPH-Iph12-1
        HttpResponse<Json> apiResponse = Unirest.get("http://localhost:8080/api/v1/articles/" + barcodeBox.getText()).asJson();

        Article scannedArticle = new Article();

        scannedArticle = new Gson().fromJson(apiResponse.body().toString(), Article.class);
        scannedArticle.setBarcode(barcodeBox.getText());


        if (articleList.isEmpty()) {
            scannedArticle.setAmount(1);
            articleList.add(scannedArticle);

        } else {


            for (Article articleFromList : articleList) {
                if (articleAlreadyScanned == false) {
                    if (articleFromList.getBarcode().equals(scannedArticle.getBarcode()) && (articleAlreadyScanned == false)) {
                        articleAlreadyScanned = true;
                    }
                    index += 1;
                } else {
                }

            }

            if(articleAlreadyScanned){
                articleList.get(index-1).amount += 1;
                articleAlreadyScanned = false;


                } else{
                    articleList.add(scannedArticle);
                    scannedArticle.setAmount(1);

                }
            index = 0;
            }


         tableView.getItems().add(scannedArticle);


        System.out.println("**********************************************");
        for (Article articleFromList: articleList
             ) {
            System.out.println("---------------------------------------");
            System.out.println("Barcode: "+ articleFromList.getBarcode());
            System.out.println("Artikel: "+ articleFromList.getArticle());
            System.out.println("Prijs: "+ articleFromList.getPrice());
            System.out.println("Aantal: "+ articleFromList.getAmount());
        }
    }


    public void payCash() throws IOException {



        //Iterating through all the scanned articles
        for (Article scannedArticle: articleList
             ) {
            //Opening the connection
            URL url = new URL("http://localhost:8080/api/v1/sale-lines");
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("POST");
            http.setRequestProperty("Content-Type", "application/json");
            http.setRequestProperty("Accept", "application/json");
            http.setDoOutput(true);
            //Building the JSON
            String data = "{\n" +
                    "    \"barcode\":\""+scannedArticle.getBarcode()+"\",\n" +
                    "    \"quantity\": "+scannedArticle.getAmount()+",\n" +
                    "    \"unitPrice\": "+scannedArticle.getPrice()+",\n" +
                    "    \"saleHeaderId\": "+saleHeaderId+"\n" +
                    "}";

            //Sending to API
            byte[] out = data.getBytes(StandardCharsets.UTF_8);
            OutputStream stream = http.getOutputStream();
            stream.write(out);
            System.out.println(http.getResponseCode());

            http.disconnect();
        }



    }

}
