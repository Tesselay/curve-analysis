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

    private double calcYValue(double x){
        double y = 0;

        for (int i = 0; i < values.size(); i++) {
            y += values.get(i) * Math.pow(x, i);
        }

        return y;
    }

    private double getSum(ArrayList<Double> values) {
        double sum = 0;
        for (Double value: values) {
            sum += value;
        }

        return sum;
    }

    private double rec_approx(double stepSize, double iterator) {
        // TODO add functionality for negative steps as well and both rising/falling
        // cast j to int and back to double to fix floating point errors.
        int temp = (int)(iterator*100.0);
        iterator = ((double) temp / 100.0);
        double yValue = calcYValue(iterator);
        temp = (int)(yValue*100.0);
        yValue = ((double) temp / 100.0);
        // basecase
        System.out.println(Math.signum(yValue));
        System.out.println(Math.signum(calcYValue(iterator+stepSize)));
        if ( yValue == 0 ) {
            return iterator;
        }
        else if ( Math.signum(yValue) != Math.signum(calcYValue(iterator+stepSize)) && Math.signum(calcYValue(iterator+stepSize)) != 0) {
            iterator = rec_approx(stepSize * 0.1, iterator);
        }
        else {
            iterator = rec_approx(stepSize, iterator + stepSize);
        }
        return iterator;
    }

    private double approximation() {
        double stepSize = 1;
        int steps = 100;

        for (int i = 0; i <= 100; i++) {
            double yValue = calcYValue(i);          // Saved as var instead of calculating anew in every condition, to save calculation time.
            if (yValue == 0) {
                return i;
            }

            // Compares the sign of the y-value of i and the y-value of i + size of step
            if ( Math.signum(yValue) != Math.signum(calcYValue(i+1)) ) {

                if ( yValue < calcYValue(i+1) ) {

                    for ( double j = i; j <= i + 1; j+=0.1 ) {
                        // cast j to int and back to double to fix floating point errors.
                        int temp = (int)(j*100.0);
                        j = ((double) temp / 100.0);

                        yValue = calcYValue(j);
                        if (yValue == 0) {
                            return j;
                        }
                    }

                }

                if ( yValue > calcYValue(i+1) ) {

                    for ( double j = i; j <= i + 1; j+=0.1 ) {
                        // cast j to int and back to double to fix floating point errors.
                        int temp = (int)(j*100.0);
                        j = ((double) temp / 100.0);

                        yValue = calcYValue(j);
                        if (yValue == 0) {
                            return j;
                        }
                    }

                } } }

        for (int i = 0; i >= -100; i--) {
            double yValue = calcYValue(i);          // Saved as var instead of calculating anew in every condition, to save calculation time.
            if (yValue == 0) {
                return i;
            }

            // Compares the sign of the y-value of i and the y-value of i + size of step
            if ( Math.signum(yValue) != Math.signum(calcYValue(i-1)) ) {

                if ( yValue < calcYValue(i-1) ) {

                    for ( double j = i; j >= i - 1; j-=0.1 ) {
                        // cast j to int and back to double to fix floating point errors.
                        int temp = (int)(j*100.0);
                        j = ((double) temp / 100.0);

                        yValue = calcYValue(j);
                        if (yValue == 0) {
                            return j;
                        }
                    }

                }

                if ( yValue > calcYValue(i-1) ) {

                    for ( double j = i; j >= i - 1; j-=0.1 ) {
                        // cast j to int and back to double to fix floating point errors.
                        int temp = (int)(j*100.0);
                        j = ((double) temp / 100.0);

                        yValue = calcYValue(j);
                        if (yValue == 0) {
                            return j;
                        }
                    }

                } } }

        return -9999;
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
        double precedEquation = (-1 * p) / 2;
        double sqrtEquation = Math.sqrt(Math.pow((p / 2), 2) - q);

        zeroes[0] = String.valueOf(precedEquation + sqrtEquation);
        zeroes[1] = String.valueOf(precedEquation - sqrtEquation);
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
            secondDegreeZeroes();
        } else if (values.get(1) != 0) {
            zeroes[0] = String.valueOf(rec_approx(1, 0));
//            zeroes[0] = String.valueOf(approximation());
            // firstDegreeZeroes();
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
