package com.demo;

import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

public class TicketPrinter {

    public void print(List<Article> articleList) throws FileNotFoundException {

        Document document = new Document();
        double totalPrice = 0D;

        PdfWriter pdfwriter = PdfWriter.getInstance(document, new FileOutputStream("Ticket.pdf")); //afmaken

        document.open();

        document.add(new Paragraph("Kassa ticket"));


        Table table = new Table(4);
        table.hasBorders();
        table.setPadding(2);
        //headers
        Cell name = new Cell("Artikel");
        name.setHeader(true);
        name.setColspan(1);
        table.addCell(name);
        Cell price = new Cell("Eenheidsprijs");
        price.setHeader(true);
        price.setColspan(1);
        table.addCell(price);
        Cell amount = new Cell("Aantal");
        amount.setHeader(true);
        amount.setColspan(1);
        table.addCell(amount);
        Cell totalAmount = new Cell("Prijs");
        totalAmount.setHeader(true);
        totalAmount.setColspan(1);
        table.addCell(totalAmount);
        table.endHeaders();

        for (Article article : articleList) {
            table.addCell(article.getArticle());
            table.addCell(String.valueOf(article.getPrice()));
            table.addCell(String.valueOf(article.getAmount()));
            table.addCell(String.valueOf(article.getTotalPrice()));
            totalPrice += article.getTotalPrice();
        }

        document.add(table);
        document.add(new Paragraph("Eindtotaal: " + String.valueOf(totalPrice)));
        document.close();

    }
}
