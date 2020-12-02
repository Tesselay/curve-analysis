package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;

import javafx.scene.control.Label;
import javafx.scene.control.Spinner;

import javafx.event.ActionEvent;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class CurveAnalysisController implements Initializable {


    @FXML private Button analyze;
    @FXML private Spinner x0;
    @FXML private Spinner x1;
    @FXML private Spinner x2;
    @FXML private Spinner x3;
    @FXML private Spinner x4;
    @FXML private Label lx;
    @FXML private Label lx1;
    @FXML private Label lx2;
    @FXML private Label lx3;
    @FXML private Label lx4;
    @FXML private Label completeCurve;
    @FXML private Label symmetries;
    @FXML private Label yAxesCrossing;
    @FXML private Label zeroPoints;

    @FXML private ArrayList<Double> curValues = new ArrayList<>();

    @FXML private StackPane myPane;
    @FXML private Axes myAxes;


    @FXML public void addCurve(ActionEvent actionEvent) {
        analyze.setText("Ahhh, it tickles");
        createCurve();
    }

    private void createCurve(){

        completeCurve.setText("f(x) = "+lx4.getText()+" "+lx3.getText()+" "+lx2.getText()+" "+lx1.getText()+" "+lx.getText());
        Curve curve = new Curve( curValues, -8, 8, 0.1,myAxes );
        int numberofChildren = myPane.getChildren().size();
        for (int i = numberofChildren -1; i > 0 ; i--) {
            myPane.getChildren().remove(i);
        }
        curve.setId("curve"+numberofChildren);
        myPane.getChildren().add(curve);
        symmetries.setText(curve.getBehaviour(0));
        yAxesCrossing.setText(curve.getBehaviour(3));
        zeroPoints.setText(curve.getBehaviour(4));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        curValues.add(Double.parseDouble(x0.getValue().toString()));
        curValues.add(Double.parseDouble(x1.getValue().toString()));
        curValues.add(Double.parseDouble(x2.getValue().toString()));
        curValues.add(Double.parseDouble(x3.getValue().toString()));
        curValues.add(Double.parseDouble(x4.getValue().toString()));

        x0.valueProperty().addListener((Observable, oldValue, newValue) -> {
            curValues.set(0, Double.parseDouble(newValue.toString()));
            lx.setText(createTextByValue(newValue.toString(),""));
            createCurve();
        });
        x1.valueProperty().addListener((Observable, oldValue, newValue) -> {
            curValues.set(1, Double.parseDouble(newValue.toString()));
            lx1.setText(createTextByValue(newValue.toString(),"x"));
            createCurve();
        });
        x2.valueProperty().addListener((Observable, oldValue, newValue) -> {
            curValues.set(2, Double.parseDouble(newValue.toString()));
            lx2.setText(createTextByValue(newValue.toString(),"x²"));
            createCurve();
        });
        x3.valueProperty().addListener((Observable, oldValue, newValue) -> {
            curValues.set(3, Double.parseDouble(newValue.toString()));
            lx3.setText(createTextByValue(newValue.toString(),"x³"));
            createCurve();
        });
        x4.valueProperty().addListener((Observable, oldValue, newValue) -> {
            curValues.set(4, Double.parseDouble(newValue.toString()));
            lx4.setText(createTextByValue(newValue.toString(),"x⁴"));
            createCurve();
        });
        x0.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_VERTICAL);
        x1.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_VERTICAL);
        x2.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_VERTICAL);
        x3.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_VERTICAL);
        x4.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_VERTICAL);
    }
    private String createTextByValue(String value,String xPower){
        String text = "";
        if(Double.parseDouble(value)>0){text = "+" +value+" "+xPower;}
        else if(Double.parseDouble(value)<0){text = value+" "+xPower;}
        return text;
    }
}
