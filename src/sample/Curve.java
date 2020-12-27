package sample;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Curve extends Pane {

    private ArrayList<Double> values;
    private double xMin;
    private double xMax;
    private double xInc;
    private Axes axes;
    private Path path;
    private String[] behaviour = {"","","","",""};
    private String[] zeroes = {"", "", ""};

    public Curve(
            ArrayList<Double> values,
            double xMin, double xMax, double xInc,
            Axes axes
    ) {
        this.values = values;
        this.xMin = xMin;
        this.xMax = xMax;
        this.xInc = xInc;
        this.axes = axes;
        this.path = new Path();
        draw();
        analyseBehaviour();
    }


    private void draw(){

        path.setStroke(Color.ORANGE.deriveColor(0, 1, 1, 0.6));
        path.setStrokeWidth(2);
        path.setClip(
                new Rectangle(
                        0, 0,
                        axes.getPrefWidth(),
                        axes.getPrefHeight()
                )
        );

        double x = xMin;
        double y = calcYValue(x);

        path.getElements().add(
                new MoveTo(
                        mapX(x, axes), mapY(y, axes)
                )
        );

        x += xInc;
        while (x < xMax) {
            y = calcYValue(x);
            path.getElements().add(
                    new LineTo(
                            mapX(x, axes), mapY(y, axes)
                    )
            );
            x += xInc;
        }

        setMinSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
        setPrefSize(axes.getPrefWidth(), axes.getPrefHeight());
        setMaxSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);

        getChildren().setAll(path);
    }

    private double mapX(double x, Axes axes) {
        double tx = axes.getPrefWidth() / 2;
        double sx = axes.getPrefWidth() /
                (axes.getXAxis().getUpperBound() -
                        axes.getXAxis().getLowerBound());
        return x * sx + tx;
    }

    private double mapY(double y, Axes axes) {
        double ty = axes.getPrefHeight() / 2;
        double sy = axes.getPrefHeight() /
                (axes.getYAxis().getUpperBound() -
                        axes.getYAxis().getLowerBound());

        return -y * sy + ty;
    }

    // Gets precision up to 12 decimal places and up to 4 pre-decimal places
    // Removing 0's increments the amount of possible pre-decimal places and decrements the decimal places

    // The precision is two decimal places higher than the y-value, to ensure, that the zeroes can be found within the frame
    long ROUNDER_HIGH = 1_000_000_000_0L;
    long ROUNDER_LOW =  1_000_000_000L;
    double DIVISOR_HIGH = 1_000_000_000_0.0;
    double DIVISOR_LOW =  1_000_000_000.0;


    private double calcYValue(double x){
        double y = 0;

        for (int i = 0; i < values.size(); i++) {
            y += values.get(i) * Math.pow(x, i);
        }


        y = Math.round(y * ROUNDER_LOW) / DIVISOR_LOW;
        return y;
    }

    private double getSum(ArrayList<Double> values) {
        double sum = 0;
        for (Double value: values) {
            sum += value;
        }

        sum = Math.round(sum * ROUNDER_LOW) / DIVISOR_LOW;
        return sum;
    }

    private double rec_approx(double stepSize, double iterator, double stepRoof) {
        // TODO BigDecimal instead of floating points
        // TODO add functionality for higher degree function <- How can I make sure that it looks for three possible points?

        iterator = Math.round(iterator * ROUNDER_HIGH) / DIVISOR_HIGH;
        stepSize = Math.round(stepSize * ROUNDER_HIGH) / DIVISOR_HIGH;
        stepRoof = Math.round(stepRoof * ROUNDER_HIGH) / DIVISOR_HIGH;

        double yValue = calcYValue(iterator);
        yValue = Math.round(yValue * ROUNDER_HIGH) / DIVISOR_HIGH;         // Values are larger to enable more precise display of values


        double nextStep = iterator + stepSize;
        nextStep = Math.round(nextStep * ROUNDER_HIGH) / DIVISOR_HIGH;

        double smallerStep = stepSize * 0.1;
        smallerStep = Math.round(smallerStep * ROUNDER_HIGH) / DIVISOR_HIGH;           // Values are larger to enable steps down to a factor of 1 * 10^(-12)

        // basecase
        if ( yValue == 0 ) {

            return iterator;
        }
        else if ( iterator >= 1000 ) {
            iterator = rec_approx(-stepSize, 0.0, -100);
        }
        else if ( iterator <= -1000 ) {
            return Double.NaN;
        }
        else if ( Math.abs(iterator) >= Math.abs(stepRoof) && Math.signum(Math.abs(stepSize)) >= 0 ) {
            double nextRoof = stepRoof * 10.0;
            nextRoof = Math.round(nextRoof * ROUNDER_HIGH) / DIVISOR_HIGH;
            iterator = rec_approx(stepSize, iterator, nextRoof);
//            return Double.NaN;          // NaN is used as a error-message to signal no value was found up to the step roof
        }
        else if ( Math.signum(yValue) != Math.signum(calcYValue(nextStep)) && Math.signum(calcYValue(nextStep)) != 0) {
            double newStepRoof = stepRoof / 10 + iterator;
            newStepRoof = Math.round(newStepRoof * ROUNDER_HIGH) / DIVISOR_HIGH;
            iterator = rec_approx(smallerStep, iterator, newStepRoof);
        }
        else {
            iterator = rec_approx(stepSize, nextStep, stepRoof);
        }
        return iterator;
    }

    private void zeroDegreeZeroes() {
        if (values.get(0) != 0) {
            zeroes[0] = "Nicht vorhanden";
            zeroes[1] = "Nicht vorhanden";
            zeroes[2] = "Nicht vorhanden";
        } else {
            zeroes[0] = "Infinite";
            zeroes[1] = "Infinite";
            zeroes[2] = "Infinite";
        }
    }

    private void firstDegreeZeroes() {
        // 0 = mx + b -> -b/m = x           =>       (-1 * values.get(0)) / values.get(1) = x
        zeroes[0] = String.valueOf((-1 * values.get(0)) / values.get(1));
        zeroes[1] = "Nicht vorhanden";
        zeroes[2] = "Nicht vorhanden";
    }

    private void secondDegreeZeroes() {
        // f(x) = ax^2 + bx + c -- p = b/a, q = c/a --> f(x) = ax^2 + p + q
        double p = values.get(1) / values.get(2);
        double q = values.get(0) / values.get(2);
        double precedeEquation = (-1 * p) / 2;
        double sqrtEquation = Math.sqrt(Math.pow((p / 2), 2) - q);

        zeroes[0] = String.valueOf(precedeEquation + sqrtEquation);
        zeroes[1] = String.valueOf(precedeEquation - sqrtEquation);
        zeroes[2] = "Nicht vorhanden";
    }

    private void thirdDegreeZeroes() {
        zeroes[0] = "sss Nicht vorhanden";
        zeroes[1] = "fff Nicht vorhanden";
        zeroes[2] = "ggg Nicht vorhanden";
    }

    private void fourthDegreeZeroes() {
        zeroes[0] = "hhh Nicht vorhanden";
        zeroes[1] = "iii Nicht vorhanden";
        zeroes[2] = "jjj Nicht vorhanden";
    }

    private void analyseBehaviour(){
        if (this.getSum(values) != 0 && values.get(3) == 0 && values.get(1) == 0) {
            this.behaviour[0] = "Achsensymmetrisch";
        } else if (this.getSum(values) != 0 && values.get(4) == 0 && values.get(2) == 0 && values.get(0) == 0) {
            this.behaviour[0] = "Punktsymmetrisch";
        } else {
            // TODO: Add control for all symmetries
            this.behaviour[0] = "Keine Symmetrien";
        }

        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) > 0) {
                this.behaviour[2] = String.valueOf(i);
            }
        }

        // TODO: Add enum (or similar) to get function references in a list to iterate through
        if (values.get(4) != 0) {
            fourthDegreeZeroes();
        } else if (values.get(3) != 0) {
            thirdDegreeZeroes();
        } else if (values.get(2) != 0) {
            zeroes[0] = String.valueOf(rec_approx(1, 0, 100));
            if ( zeroes[0] == "NaN") {
                zeroes[0] = "No value found!";
            }
            //            secondDegreeZeroes();
        } else if (values.get(1) != 0) {
            zeroes[0] = String.valueOf(rec_approx(1, 0, 100));
            if ( zeroes[0] == "NaN") {
                zeroes[0] = "No value found!";
            }
//            firstDegreeZeroes();
        } else if (values.get(0) != 0) {
            zeroDegreeZeroes();
        }

        this.behaviour[3] = String.valueOf(values.get(0));
        this.behaviour[4] = String.format("1. Nullstelle: %s\n2. Nullstelle: %s\n3. Nullstelle: %s", zeroes[0], zeroes[1], zeroes[2]);

    }

    public String getBehaviour(int index){
        if (index>=0 && index <=4) {
            return behaviour[index];
        }
        else{return "none";}
    }

   /* public void setFunction(ArrayList<Double> values){
        this.values = values;
        draw();
    }*/
}
