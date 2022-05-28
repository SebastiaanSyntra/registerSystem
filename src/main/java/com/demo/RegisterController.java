package com.demo;


import com.google.gson.Gson;
import io.joshworks.restclient.http.HttpResponse;
import io.joshworks.restclient.http.Json;
import io.joshworks.restclient.http.Unirest;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
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
    TextField barcodeBox = new TextField();
    @FXML
    TextField employeeField = new TextField();
    @FXML
    TextField totalAmountTextfield = new TextField();
    @FXML
    TextField totalAmountPaidTxt = new TextField();
    @FXML
    TextField returnAmountTxt = new TextField();
    @FXML
    Button removeLastButton = new Button();
    @FXML
    Button newSaleButton = new Button();
    @FXML
    Button articleScannerButton = new Button();
    @FXML
    Button payCashButton = new Button();
    @FXML
    Button removeAllButton = new Button();
    @FXML
    Button endSaleButton = new Button();

    @FXML
    TableColumn articleColumn = new TableColumn();
    @FXML
    TableColumn priceColumn = new TableColumn();
    @FXML
    TableColumn categoryColumn = new TableColumn();
    @FXML
    TableColumn amountColumn = new TableColumn();
    @FXML
    TableColumn totalPriceColumn = new TableColumn();
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
    TicketPrinter printer = new TicketPrinter();

    public RegisterController(){}


    //SMTPH-Iph12-1
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
        barcodeBox.setDisable(true);
        totalAmountTextfield.setDisable(true);
        payCashButton.setDisable(true);
        articleScannerButton.setDisable(true);
        removeAllButton.setDisable(true);
        removeLastButton.setDisable(true);
        endSaleButton.setDisable(false);
        returnAmountTxt.setDisable(true);
    }

    //TODO: opvangen als applicatie gescande gebruiker niet herkent

    //TODO: wisselgeld

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

    barcodeBox.setDisable(false);
    employeeField.setDisable(true);
    articleScannerButton.setDisable(false);
    newSaleButton.setDisable(true);
    endSaleButton.setDisable(true);
    }

    public void scanArticle() throws IOException {
        scannedException = false;
        int index = 0;
        Article scannedArticle = new Article();



        HttpResponse<Json> apiResponse = Unirest.get("http://localhost:8080/api/v1/articles/" + barcodeBox.getText()).asJson();


        //article object invullen
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
        removeAllButton.setDisable(false);
        removeLastButton.setDisable(false);
        payCashButton.setDisable(false);
    }

    public void clearAll(){
        articleList.clear();
        totalAmountTextfield.clear();
        barcodeBox.clear();
        employeeField.clear();
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
        barcodeBox.setDisable(false);
        employeeField.setDisable(true);
        removeAllButton.setDisable(true);
        removeLastButton.setDisable(true);
        payCashButton.setDisable(true);
        articleImage.setImage(null);
    }

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

    public void endSale(){
        newSaleButton.setDisable(false);
        employeeField.clear();
        barcodeBox.setDisable(true);
        totalAmountTextfield.setDisable(true);
        payCashButton.setDisable(true);
        articleScannerButton.setDisable(true);
        removeAllButton.setDisable(true);
        removeLastButton.setDisable(true);
        articleList.clear();
        saleHeaderId = null;
        articleAlreadyScanned = false;
        articleFoundIndex = 0;
        totalPrice = 0D;
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
        printer.print(articleList);
        returnAmountTxt.setText(String.valueOf( Double.parseDouble(totalAmountPaidTxt.getText()) - Double.parseDouble(totalAmountTextfield.getText()) ));

        //Clear everything
        totalAmountPaidTxt.clear();
        totalAmountTextfield.clear();
        barcodeBox.setDisable(true);
        barcodeBox.clear();
        articleScannerButton.setDisable(true);
        employeeField.setDisable(false);
        newSaleButton.setDisable(false);
        employeeField.clear();
        removeAllButton.setDisable(true);
        removeLastButton.setDisable(true);
        payCashButton.setDisable(true);
        articleImage.setImage(null);
        tableView.getItems().clear();
        articleList.clear();

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

    public void addNumber1(){
        totalAmountPaidTxt.setText(totalAmountPaidTxt.getText() + 1);
    }
    public void addNumber2(){
        totalAmountPaidTxt.setText(totalAmountPaidTxt.getText() + 2);
    }
    public void addNumber3(){
        totalAmountPaidTxt.setText(totalAmountPaidTxt.getText() + 3);
    }
    public void addNumber4(){
        totalAmountPaidTxt.setText(totalAmountPaidTxt.getText() + 4);
    }
    public void addNumber5(){
        totalAmountPaidTxt.setText(totalAmountPaidTxt.getText() + 5);
    }
    public void addNumber6(){
        totalAmountPaidTxt.setText(totalAmountPaidTxt.getText() + 6);
    }
    public void addNumber7(){
        totalAmountPaidTxt.setText(totalAmountPaidTxt.getText() + 7);
    }
    public void addNumber8(){
        totalAmountPaidTxt.setText(totalAmountPaidTxt.getText() + 8);
    }
    public void addNumber9(){
        totalAmountPaidTxt.setText(totalAmountPaidTxt.getText() + 9);
    }
    public void addNumber0(){
        totalAmountPaidTxt.setText(totalAmountPaidTxt.getText() + 0);
    }
    public void addDecimalPoint(){
        totalAmountPaidTxt.setText(totalAmountPaidTxt.getText() + ".");
    }



}
