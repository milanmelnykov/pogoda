package sample;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Controller {

    ObservableList<String> cities;

    @FXML
    private Button findBtn;

    @FXML
    private ComboBox<String> boxCities;

    @FXML
    private ComboBox<String> boxDays;

    @FXML
    private Label dataLabel;

    @FXML
    private DatePicker datePicker;

    private Document getPage(String location) throws IOException {
        return Jsoup.parse(new URL(location), 3000);
    }

    private void fillBoxCities() {
        Path path = Paths.get("./src/sample/files/cities.txt");

        try (Stream<String> lineStream = Files.newBufferedReader(path).lines()) {
            cities = FXCollections.observableArrayList(lineStream.collect(Collectors.toList()));
            boxCities.setItems(cities);
            boxCities.getSelectionModel().selectFirst();
        } catch (IOException ignored) {
        }
    }

    private boolean isNetAvailable() throws Exception {
        return java.lang.Runtime.getRuntime().exec("ping www.google.com").waitFor() == 0;
    }

    private String getDate() {
        if (boxDays.getValue().equals("сьогодні")) return null;
        else {
            long millis = System.currentTimeMillis();
            Date date;
            if (boxDays.getValue().equals("завтра")) date = new Date(millis + 86400000);
            else date = new Date(millis + 2 * 86400000);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // Задаем формат даты
            String formattedDate = sdf.format(date);
            return formattedDate;
        }
    }

    @FXML
    void initialize() {
        fillBoxCities();
        boxDays.setItems(FXCollections.observableArrayList(Arrays.asList("сьогодні", "завтра", "післязавтра")));
        boxDays.getSelectionModel().selectFirst();
        findBtn.setOnAction(actionEvent -> {
            try {
                if (isNetAvailable()) {
                    Document page = null;
                    if (boxCities.getValue().isEmpty()) dataLabel.setText("Некоректно введена назва локації");
                    else {
                        String loc = " https://ua.sinoptik.ua/погода-" + boxCities.getValue().toLowerCase();
                        if (getDate() != null) loc += "/" + getDate();
                        try {
                            page = getPage(loc);
                        } catch (IOException e) {
                            dataLabel.setText("Некоректно введена назва локації");
                        }
                        Element tableWth = page.select("table[class=weatherDetails]").first();

                        Elements values = tableWth.select("tr[class=temperature]").select("td");
                        String tempStr = new String();
                        for (Element value : values) {
                            tempStr += value.text() + "\t";
                        }
                        tempStr += '\n';

                        values = tableWth.select("tr").get(8).select("td");
                        for (Element value : values) {
                            tempStr += ' ' + value.text() + "\t\t";
                        }
                        dataLabel.setText(tempStr);
                    }
                } else dataLabel.setText("Нема доступу до мережі інтернет");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


    }
}
