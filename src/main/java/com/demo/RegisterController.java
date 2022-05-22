package com.demo;


import com.google.gson.Gson;
import io.joshworks.restclient.http.HttpResponse;
import io.joshworks.restclient.http.Json;
import io.joshworks.restclient.http.Unirest;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;

@Component
@FxmlView
public class RegisterController implements Initializable {

    @FXML
    TextField barcodeBox;
    @FXML
    TextField employeeField;
    @FXML
    TextField totalAmountTextfield = new TextField();
    @FXML
    TableColumn articleColumn;
    @FXML
    TableColumn priceColumn;
    @FXML
    TableColumn categoryColumn;
    @FXML
    TableColumn amountColumn;
    @FXML
    TableColumn totalPriceColumn;
    @FXML
    TableView tableView;
    @FXML
    ImageView articleImage;


    List<Article> articleList = new ArrayList<Article>();
    String saleHeaderId;
    boolean articleAlreadyScanned;
    int articleFoundIndex;
    Double totalPrice = 0D;
    boolean scannedException;



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
                new PropertyValueFactory<>("Amount"));
        amountColumn.setCellValueFactory(
                new PropertyValueFactory<>("Amount"));
        totalPriceColumn.setCellValueFactory(
                new PropertyValueFactory<>("TotalPrice")
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
                new InputStreamReader(http.getInputStream(), StandardCharsets.UTF_8))) {
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

    public void scanArticle() throws IOException {
        scannedException = false;
        int index = 0;
        Article scannedArticle = new Article();


        //SMTPH-Iph12-1
        HttpResponse<Json> apiResponse = Unirest.get("http://localhost:8080/api/v1/articles/" + barcodeBox.getText()).asJson();


        //articleobject invullen
        try {
            scannedArticle = new Gson().fromJson(apiResponse.body().toString(), Article.class);
        } catch (Exception e) {
            barcodeBox.setText("Artikel niet gevonden");
            scannedException = true;
        }
        if (scannedException) {
        } else {
            scannedArticle.setBarcode(barcodeBox.getText());
            scannedArticle.setTotalPrice(scannedArticle.price);

            if (scannedArticle.getArticleImage() == null) {
                articleImage.setImage(null);
            } else {
                byte[] imageBytes = Base64.getDecoder().decode(scannedArticle.getArticleImage());
                ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                BufferedImage scannedImage = ImageIO.read(bis);
                Image image = SwingFXUtils.toFXImage(scannedImage, null);
                articleImage.setImage(image);
            }
            //check of het artikel al gescand is
            if (articleList.isEmpty()) {
                //niet gescand => toegevoegd aan List met aantal = 1
                scannedArticle.setAmount(1);
                articleList.add(scannedArticle);
            } else {
                //lijst met gescande articles doorlopen
                for (Article articleFromList : articleList) {
                    //articleAlreadyScanned houdt bij of het artikel al gescand werd.
                    if (!articleAlreadyScanned) {
                        //De barcode van alle artikel in de lijst gecheckt met het nieuw gescande artikel. Als dit matched wordt de boolean op true gezet zodat dit niet meer wordt doorlopen
                        if (articleFromList.getBarcode().equals(scannedArticle.getBarcode()) && (!articleAlreadyScanned)) {
                            articleAlreadyScanned = true;

                        }
                        index += 1;
                    }
                }

                //als het article al gescanned werd wordt de amount aangepast
                if (articleAlreadyScanned) {
                    articleList.get(index - 1).amount += 1;
                    articleList.get(index - 1).totalPrice = (articleList.get(index - 1).price * articleList.get(index - 1).amount);
                    articleAlreadyScanned = false;
                } else {
                    //anders gewoon toegevoegd met amount = 1
                    articleList.add(scannedArticle);
                    scannedArticle.setAmount(1);
                }
                index = 0;
            }


            //table wordt bij elke scan gecleared om de articleList volledig opnieuw in te laden (kon dit niet anders oplossen..)
            tableView.getItems().clear();
            totalPrice = 0D;

            for (Article tableArticle : articleList
            ) {
                tableView.getItems().add(tableArticle);
                totalPrice += tableArticle.getAmount() * tableArticle.getPrice();
            }

            totalAmountTextfield.setText(String.valueOf(totalPrice));
        }
    }

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
        totalPriceColumn.setCellValueFactory(
                new PropertyValueFactory<>("totalPrice")
        );
        tableView.getItems();
    }

    //totaalprijsveld nog aanpassenx
    public void removeLastLine(){
        int articleIndex;
        Article removedArticle = new Article();
        if(articleList.isEmpty()){

        } else{
            removedArticle = articleList.get(articleList.size()-1);
        }

        if(removedArticle.amount > 1 ){

            removedArticle.amount -= 1;
            removedArticle.totalPrice = (removedArticle.getAmount() * removedArticle.getPrice());
        } else {
            articleList.remove(articleList.size() - 1);
        }
        tableView.getItems().clear();
        totalPrice = 0D;

        for (Article tableArticle:articleList
        ) {
            tableView.getItems().add(tableArticle);
            totalPrice += tableArticle.getAmount() * tableArticle.getPrice();

        }

        totalAmountTextfield.setText(String.valueOf(totalPrice));

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
