package animalkeeping.ui.widgets;
import animalkeeping.model.HousingUnit;
import animalkeeping.model.SpeciesType;
import animalkeeping.ui.Main;
import animalkeeping.util.DateTimeHelper;
import animalkeeping.util.EntityHelper;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.controlsfx.control.CheckComboBox;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/******************************************************************************
 animalBase
 animalkeeping.ui.widgets

 Copyright (c) 2017 Neuroethology Lab, University of Tuebingen,
 Jan Grewe <jan.grewe@g-node.org>,
 Dennis Huben <dennis.huben@rwth-aachen.de>

 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this list
 of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice, this
 list of conditions and the following disclaimer in the documentation and/or other
 materials provided with the distribution.

 3. Neither the name of the copyright holder nor the names of its contributors may
 be used to endorse or promote products derived from this software without specific
 prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 DAMAGE.

 * Created by jan on 24.03.18.

 *****************************************************************************/

public class PopulationStackedChart extends VBox {
    private CategoryAxis xAxis = new CategoryAxis();
    private StackedAreaChart<String, Number> chart;
    private DatePicker startDate, endDate;
    private ComboBox<String> intervalCombo;
    private Button exportBtn;
    private ProgressIndicator busy;
    private VBox vbox;
    private HousingUnit housingUnit = null;
    private CheckBox resolveSpecies;
    private SimpleBooleanProperty refreshRunning = new SimpleBooleanProperty(false);
    private CheckComboBox<SpeciesType> speciesTypeCheckComboBox;

    public PopulationStackedChart() {
        this.xAxis.setCategories(FXCollections.observableArrayList
                (Arrays.asList("1750", "1800", "1850", "1900", "1950", "1999", "2050" )));
        this.xAxis.setLabel("Date");
        this.xAxis.tickLabelFontProperty().set(Font.font(9));
        NumberAxis yAxis = new NumberAxis();
        yAxis.setForceZeroInRange(true);
        yAxis.setLowerBound(0.);
        yAxis.setUpperBound(100.);
        yAxis.setTickUnit(25.);
        yAxis.setLabel("Population");
        this.chart = new StackedAreaChart<>(xAxis, yAxis);

        GridPane grid = new GridPane();
        ColumnConstraints column1 = new ColumnConstraints(100,100, Double.MAX_VALUE);
        column1.setHgrow(Priority.ALWAYS);
        ColumnConstraints column2 = new ColumnConstraints(100, 150, Double.MAX_VALUE);
        column2.setHgrow(Priority.ALWAYS);
        ColumnConstraints column3 = new ColumnConstraints(100, 150, Double.MAX_VALUE);
        column3.setHgrow(Priority.ALWAYS);
        ColumnConstraints column4 = new ColumnConstraints(0, 150, Double.MAX_VALUE);
        column3.setHgrow(Priority.ALWAYS);
        ColumnConstraints column5 = new ColumnConstraints(100, 150, Double.MAX_VALUE);
        column3.setHgrow(Priority.NEVER);
        grid.getColumnConstraints().addAll(column1, column2, column3, column4, column5);

        this.busy = new ProgressIndicator();
        this.busy.setProgress(0.0);
        this.busy.setPrefSize(75, 75);

        this.startDate = new DatePicker(java.time.LocalDate.now().minusYears(1));
        this.startDate.prefWidthProperty().bind(column1.maxWidthProperty());

        this.endDate = new DatePicker(DateTimeHelper.toLocalDate(new Date()));
        this.endDate.prefWidthProperty().bind(column2.maxWidthProperty());

        this.intervalCombo = new ComboBox<>();
        this.intervalCombo.getItems().addAll("monthly");
        this.intervalCombo.getSelectionModel().select(0);
        this.intervalCombo.prefWidthProperty().bind(column3.maxWidthProperty());

        this.resolveSpecies = new CheckBox("select species");
        this.resolveSpecies.setSelected(false);
        this.resolveSpecies.prefWidthProperty().bind(column5.maxWidthProperty());

        this.exportBtn = new Button("export");
        this.exportBtn.setDisable(true);
        this.exportBtn.setOnAction(event -> exportPopulationCounts());
        this.exportBtn.prefWidthProperty().bind(column5.maxWidthProperty());

        Button updateBtn = new Button("update");
        updateBtn.setOnAction(event -> getPupulationCounts());
        updateBtn.prefWidthProperty().bind(column5.maxWidthProperty());

        ObservableList<SpeciesType> speciesTypes = FXCollections.observableArrayList();
        List<SpeciesType> sp = EntityHelper.getEntityList("FROM SpeciesType ORDER BY name ASC", SpeciesType.class);
        speciesTypes.addAll(sp);
        this.speciesTypeCheckComboBox = new CheckComboBox<>(speciesTypes);
        this.speciesTypeCheckComboBox.setConverter(new StringConverter<SpeciesType>() {
            @Override
            public String toString(SpeciesType object) {
                return object.getName() + ": " + object.getTrivial();
            }

            @Override
            public SpeciesType fromString(String string) {
                return null;
            }
        });
        speciesTypeCheckComboBox.prefWidthProperty().bind(column3.maxWidthProperty());
        speciesTypeCheckComboBox.disableProperty().bind(resolveSpecies.selectedProperty().not());

        grid.setHgap(5);
        grid.add(new Label("Start date:"), 0, 0);
        grid.add(this.startDate, 0, 1);
        grid.add(new Label("End date:"), 1, 0);
        grid.add(this.endDate, 1,1);
        grid.add(new Label("Interval:"), 2, 0);
        grid.add(this.intervalCombo, 2, 1);
        grid.add(this.resolveSpecies, 3, 0);
        grid.add(this.speciesTypeCheckComboBox, 3, 1);
        grid.add(updateBtn, 4, 0);
        grid.add(this.exportBtn, 4, 1);

        vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().add(busy);
        vbox.setVisible(false);

        StackPane spane = new StackPane();
        spane.getChildren().addAll(chart, vbox);

        BorderPane pane = new BorderPane();
        pane.setTop(grid);
        pane.setCenter(spane);
        this.getChildren().add(pane);
        this.setFillWidth(true);
        this.setPadding(new Insets(2, 10, 2, 10));
    }


    public void setHousingUnit(HousingUnit housingUnit) {
        this.housingUnit = housingUnit;
        getPupulationCounts();
    }


    private Vector<Date> getDueDates() {
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();
        Vector<Date> dueDates = new Vector<>();
        if (intervalCombo.getValue().equalsIgnoreCase("monthly")) {
            start = start.plusDays(start.lengthOfMonth() - start.getDayOfMonth());
        } else if (intervalCombo.getValue().equalsIgnoreCase("weekly")) {
            start.plusDays(DayOfWeek.SUNDAY.getValue() - start.getDayOfWeek().getValue());
        }
        dueDates.add(DateTimeHelper.localDateToUtilDate(start));
        while (start.isBefore(end)) {
            if (intervalCombo.getValue().equalsIgnoreCase("weekly")) {
                start = start.plusWeeks(1);
            } else if (intervalCombo.getValue().equalsIgnoreCase("monthly")) {
                start = start.plusMonths(1);
                start = start.plusDays(start.lengthOfMonth() - start.getDayOfMonth());
            }
            dueDates.add(DateTimeHelper.localDateToUtilDate(start));
        }
        return dueDates;
    }


    private void getPupulationCounts() {
        if (refreshRunning.get()) {
            return;
        }
        chart.setAnimated(false);
        chart.getData().clear();

        this.xAxis.getCategories().clear();
        Vector<Date> dueDates = getDueDates();
        Vector<XYChart.Series<String, Number>> series = new Vector<>();
        ObservableList<SpeciesType> selection = speciesTypeCheckComboBox.getCheckModel().getCheckedItems();

        RefreshTask refresh_task = new RefreshTask(dueDates, series, selection);

        refresh_task.setOnSucceeded(event -> {
            ArrayList<String> dates = new ArrayList<>();
            for (Date d : refresh_task.dueDates) {
                dates.add(refresh_task.fmt.format(d));
            }
            this.xAxis.getCategories().addAll(dates);
            for (XYChart.Series<String, Number> s : series)
                this.chart.getData().add(s);
        });

        Platform.runLater(() -> {
            busy.progressProperty().unbind();
            vbox.visibleProperty().unbind();
            refreshRunning.unbind();
            vbox.visibleProperty().bind(refresh_task.runningProperty());
            busy.progressProperty().bind(refresh_task.progressProperty());
            exportBtn.disableProperty().bind(refresh_task.runningProperty());
            refreshRunning.bind(refresh_task.runningProperty());
        });

        new Thread(refresh_task).start();
    }

    private void exportPopulationCounts() {
        final XSSFWorkbook workbook = new XSSFWorkbook();

        Task<Void> export_task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                double progress = 0.0;
                double maxWork = chart.getData().size() * chart.getData().get(0).getData().size() +
                        chart.getData().get(0).getData().size() + chart.getData().size();
                updateProgress(progress, maxWork);
                XSSFSheet sheet = workbook.createSheet("Population history");
                XSSFRow row = sheet.createRow(0);
                XSSFCell cell;
                String firstCellAddr, lastCellAddr;

                for (int i = 0; i < chart.getData().size(); i++) {
                    XYChart.Series<String, Number> s = chart.getData().get(i);
                    if (row.getRowNum() > 0) {
                        row = sheet.getRow(0);
                    }
                    row.createCell(i + 1).setCellValue(s.getName());
                    for ( int j = 0; j < s.getData().size(); j++) {
                        XYChart.Data<String, Number> d = s.getData().get(j);
                        if (i == 0) {
                            row = sheet.createRow(j + 1);
                            row.createCell(0).setCellValue(d.getXValue());
                        } else {
                            row = sheet.getRow(j + 1);
                        }
                        cell = row.createCell(i + 1);
                        cell.setCellValue(d.getYValue().intValue());
                        progress++;
                        updateProgress(progress, maxWork);
                    }
                    Thread.sleep(25);
                }
                // sum of counts in each row
                row = sheet.getRow(0);
                row.createCell(row.getLastCellNum()).setCellValue("Total");
                for (int i = 1; i < sheet.getLastRowNum(); i++) {
                    row = sheet.getRow(i);
                    cell = row.createCell(row.getLastCellNum());
                    cell.setCellType(CellType.FORMULA);
                    firstCellAddr = row.getCell(1).getAddress().toString();
                    lastCellAddr = row.getCell(cell.getColumnIndex()-1).getAddress().toString();
                    cell.setCellFormula("SUM(" + firstCellAddr + ":" + lastCellAddr + ")");
                    progress++;
                    updateProgress(progress, maxWork);
                }
                Thread.sleep(25);

                // add row for averages
                XSSFRow firstRow = sheet.getRow(1);
                XSSFRow lastRow = sheet.getRow(sheet.getLastRowNum() - 1);
                row = sheet.createRow(sheet.getLastRowNum());
                row.createCell(0).setCellValue("Average");
                for (int i = 1; i < lastRow.getLastCellNum(); i++) {
                    firstCellAddr = firstRow.getCell(i).getAddress().toString();
                    lastCellAddr = lastRow.getCell(i).getAddress().toString();

                    cell = row.createCell(i);
                    cell.setCellType(CellType.FORMULA);
                    cell.setCellFormula("AVERAGEA(" + firstCellAddr + ":" + lastCellAddr + ")");
                    progress++;
                    updateProgress(progress, maxWork);
                }
                Thread.sleep(25);

                for (int i = 0; i < row.getLastCellNum(); i ++) {
                    sheet.autoSizeColumn(i);
                }
                updateProgress(maxWork, maxWork);
                Thread.sleep(25);
                return null;
            }
        };

        export_task.setOnSucceeded(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select output file");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel doc(*.xlsx)", "*.xlsx"));
            chooser.setInitialFileName("populationHistory.xlsx");
            File f = chooser.showSaveDialog(Main.getPrimaryStage());
            if (f != null) {
                try {
                    FileOutputStream out = new FileOutputStream(f);
                    workbook.write(out);
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        busy.progressProperty().unbind();
        vbox.visibleProperty().unbind();
        vbox.visibleProperty().bind(export_task.runningProperty());
        busy.progressProperty().bind(export_task.progressProperty());
        new Thread(export_task).start();
    }


    class RefreshTask extends Task<Void> {
        private Vector<Date> dueDates = new Vector<>();
        private Vector<XYChart.Series<String, Number>> series = new Vector<>();
        private SimpleDateFormat fmt = new SimpleDateFormat("MM.yyyy");
        private ObservableList<SpeciesType> selection = null;

        RefreshTask(Vector<Date> dueDates, Vector<XYChart.Series<String, Number>> series, ObservableList<SpeciesType> selection) {
            super();
            this.dueDates = dueDates;
            this.series = series;
            this.selection = selection;
        }

        @Override
        protected Void call() throws Exception {
            if (housingUnit == null) {
                populationHistory();
            } else {
                populationHistoryOfHousingUnit();
            }
            return null;
        }

        private void populationHistory() {
            String q;
            List<SpeciesType> species = new LinkedList<>();
            int maxWork = dueDates.size();
            if (resolveSpecies.isSelected()) {
                species = EntityHelper.getEntityList("FROM SpeciesType", SpeciesType.class);
                maxWork = dueDates.size() * species.size();
                q = "SELECT COUNT(distinct(s.id)) FROM census_subject s, census_housing h, census_speciestype st " +
                        "where h.subject_id = s.id AND s.species_id = st.id AND st.id = :species " +
                        "AND h.start_datetime < :end AND (h.end_datetime is null OR h.end_datetime > :end)";
            } else {
                q = "SELECT COUNT(distinct(s.id)) FROM census_subject s, census_housing h " +
                        "WHERE h.subject_id = s.id " +
                        "AND h.start_datetime < :end AND (h.end_datetime is null OR h.end_datetime > :end)";
            }
            updateProgress(0, maxWork);

            Session session = Main.sessionFactory.openSession();
            Query query = session.createNativeQuery(q);
            int progress = 0;
            for (int i = 0; i < dueDates.size(); i++) {
                Date d = dueDates.get(i);
                if (!resolveSpecies.isSelected()) {
                    XYChart.Series<String, Number> s;
                    if (i == 0) {
                        s = new XYChart.Series<>();
                        s.setName("all species");
                        series.add(s);
                    } else {
                        s = series.get(0);
                    }

                    query.setParameter("end", d);
                    Number count = (Number) query.getSingleResult();
                    s.getData().add(new XYChart.Data<>(fmt.format(d), count));
                    progress++;
                    updateProgress(progress, maxWork);
                } else {
                    for (int j = 0; j < selection.size(); j++) {
                        SpeciesType sp = selection.get(j);
                        if (!species.contains(sp)) {
                            progress++;
                            updateProgress(progress, maxWork);
                        }
                        XYChart.Series<String, Number> s;
                        if (i == 0) {
                            s = new XYChart.Series<>();
                            s.setName(sp.getName());
                            series.add(s);
                        } else {
                            s = series.get(j);
                        }
                        query.setParameter("species", sp.getId());
                        query.setParameter("end", d);
                        Number count = (Number) query.getSingleResult();
                        s.getData().add(new XYChart.Data<>(fmt.format(d), count));

                        progress++;
                        updateProgress(progress, maxWork);
                    }
                }
            }
            session.close();
            updateProgress(maxWork, maxWork);
        }

        private void populationHistoryOfHousingUnit() {
            Session session = Main.sessionFactory.openSession();
            updateProgress(0, 3);
            HousingUnit hu = session.get(HousingUnit.class, housingUnit.getId());
            Set<HousingUnit> units = hu.getChildHousingUnits(true);
            List<Long> ids = units.stream().map(HousingUnit::getId).collect(Collectors.toList());
            ids.add(hu.getId());
            List<SpeciesType> species = new LinkedList<>();
            int progress = 0;
            String q;

            if (resolveSpecies.isSelected()) {
                q = "SELECT DISTINCT(st) FROM Subject s, SpeciesType st, Housing h, HousingUnit hu WHERE s.speciesType.id = st.id AND " +
                        "h.subject.id = s.id AND hu.id = h.housing.id AND h.housing.id IN :housings";
                Vector<String> params = new Vector<>();
                params.add("housings");
                Vector<Object> values = new Vector<>();
                values.add(ids);
                species = EntityHelper.getEntityList(q, params, values, SpeciesType.class);
            }
            int maxWork = resolveSpecies.isSelected() ? dueDates.size() * species.size() : dueDates.size();
            updateProgress(3, maxWork);

            if (resolveSpecies.isSelected()) {
                q = "SELECT COUNT(distinct(s.id)) FROM census_subject s, census_housing h, census_speciestype st " +
                        "where h.subject_id = s.id AND s.species_id = st.id AND st.id = :species " +
                        "AND h.start_datetime < :end AND (h.end_datetime is null OR h.end_datetime > :end) AND "
                        + "h.type_id IN :housings";

            } else {
                q = "SELECT COUNT(distinct(s.id)) FROM census_subject s, census_housing h " +
                        "where h.subject_id = s.id " +
                        "AND h.start_datetime < :end AND (h.end_datetime is null OR h.end_datetime > :end) AND "
                        + "h.type_id IN :housings";
            }
            Query query = session.createNativeQuery(q);
            query.setParameter("housings", ids);
            for (int i = 0; i < dueDates.size(); i++) {
                Date d = dueDates.get(i);
                if (resolveSpecies.isSelected()) {
                    for (int j = 0; j < selection.size(); j++) {
                        SpeciesType sp = selection.get(j);
                        if (!selection.contains(sp)) {
                            progress++;
                            updateProgress(progress, maxWork);
                            continue;
                        }
                        XYChart.Series<String, Number> s;
                        if (i == 0) {
                            s = new XYChart.Series<>();
                            s.setName(sp.getName());
                            series.add(s);
                        } else {
                            s = series.get(j);
                        }
                        query.setParameter("species", sp.getId());
                        query.setParameter("end", d);
                        Number count = (Number) query.getSingleResult();
                        s.getData().add(new XYChart.Data<>(fmt.format(d), count));
                        progress++;
                        updateProgress(progress, maxWork);
                    }
                } else {
                    XYChart.Series<String, Number> s;
                    if (i == 0) {
                        s = new XYChart.Series<>();
                        s.setName("all species");
                        series.add(s);
                    } else {
                        s = series.get(0);
                    }
                    query.setParameter("end", d);
                    Number count = (Number) query.getSingleResult();
                    s.getData().add(new XYChart.Data<>(fmt.format(d), count));
                    progress++;
                    updateProgress(progress, maxWork);
                }
            }
            updateProgress(maxWork, maxWork);
            session.close();
        }
    }

}