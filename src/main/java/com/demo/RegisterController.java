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
        amountColumn.setCellValueFactory(
                new PropertyValueFactory<>("Amount")
        );
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
        Article scannedArticle = new Article();

        //SMTPH-Iph12-1
        HttpResponse<Json> apiResponse = Unirest.get("http://localhost:8080/api/v1/articles/" + barcodeBox.getText()).asJson();


        //articleobject invullen
        scannedArticle = new Gson().fromJson(apiResponse.body().toString(), Article.class);
        scannedArticle.setBarcode(barcodeBox.getText());

        //check of het artikel al gescand is
        if (articleList.isEmpty()) {
            //niet gescand => toegevoegd aan List met aantal = 1
            scannedArticle.setAmount(1);
            articleList.add(scannedArticle);}
        else {
            //lijst met gescande articles doorlopen
            for (Article articleFromList : articleList) {
                //articleAlreadyScanned houdt bij of het artikel al gescand werd.
                if (articleAlreadyScanned == false) {
                   //De barcode van alle artikel in de lijst gecheckt met het nieuw gescande artikel. Als dit matched wordt de boolean op true gezet zodat dit niet meer wordt doorlopen
                    if (articleFromList.getBarcode().equals(scannedArticle.getBarcode()) && (articleAlreadyScanned == false)) {
                        articleAlreadyScanned = true;
                    }
                    index += 1;
                }
            }

            //als het article al gescanned werd wordt de amount aangepast
            if(articleAlreadyScanned){
                articleList.get(index-1).amount += 1;
                articleAlreadyScanned = false;}
            else{
                //anders gewoon toegevoegd met amount = 1
                    articleList.add(scannedArticle);
                    scannedArticle.setAmount(1);
                }
            index = 0;
            }


        //table wordt bij elke scan gecleared om de articleList volledig opnieuw in te laden (kon dit niet anders oplossen..)
        tableView.getItems().clear();
        for (Article tableArticle:articleList
             ) {
            tableView.getItems().add(tableArticle);
        }


        //Testcode
//        System.out.println("**********************************************");
//        for (Article articleFromList: articleList
//             ) {
//            System.out.println("---------------------------------------");
//            System.out.println("Barcode: "+ articleFromList.getBarcode());
//            System.out.println("Artikel: "+ articleFromList.getArticle());
//            System.out.println("Prijs: "+ articleFromList.getPrice());
//            System.out.println("Aantal: "+ articleFromList.getAmount());
//        }

    }

    //Clear all knop => nog op te kuisen want verwijdert ook kolommen
    public void clearAll(){
        tableView.getItems().clear();
        articleColumn.setCellValueFactory(
                new PropertyValueFactory<>("Article"));
        priceColumn.setCellValueFactory(
                new PropertyValueFactory<>("Price"));
        categoryColumn.setCellValueFactory(
                new PropertyValueFactory<>("Category"));
        amountColumn.setCellValueFactory(
                new PropertyValueFactory<>("Amount"));

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
