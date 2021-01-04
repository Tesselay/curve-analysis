package sample;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;

// TODO Replace doubles with BigDecimal
// TODO Optimization and refactoring
// TODO add signChange search as own method
// TODO Streamline naming for not found zero points
// TODO is zeroes[] emptier needed?
// TODO Add control for all kind of symmetries
// TODO Add enum (or similar) to get function references in a list to iterate through
// TODO Refactor zero point search to search until no value is found anymore
// TODO Make gui pretty
// FIXME deeper search for double sign changes leads to buggy behavious

public class Curve extends Pane {

    private ArrayList<Double> values;
    private double xMin;
    private double xMax;
    private double xInc;
    private Axes axes;
    private Path path;
    private String[] behaviour = {"","","","",""};
    private String[] zeroes = {"", "", "", ""};

    private int decimalPlaces = 10;
    private int COUNT = 0;

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

    private BigDecimal calcYValueBD(BigDecimal x) {
        BigDecimal y = new BigDecimal("0", MathContext.DECIMAL128);

        for ( int i = 0; i < values.size(); i++ ) {
            BigDecimal tempValue = new BigDecimal( String.valueOf(values.get(i)) );
            BigDecimal powerTo = x.pow(i);
            BigDecimal newTemp = tempValue.multiply(powerTo);
            y = y.add(newTemp, MathContext.DECIMAL64);
        }

        y = y.setScale(15, RoundingMode.DOWN);
        y = y.stripTrailingZeros();

        return y;
    }

    private BigDecimal getSumBD(ArrayList<Double> values) {
        BigDecimal sum = new BigDecimal("0");
        for ( Double value: values ) {
            BigDecimal tempValue = new BigDecimal(String.valueOf(value));
            sum = sum.add(tempValue);
        }

        sum = sum.setScale(15, RoundingMode.DOWN);
        sum = sum.stripTrailingZeros();

        return sum;
    }

    private void rec_runThroughBD() {
        while (zeroes[0].equals("") || zeroes[1].equals("") || zeroes[2].equals("") || zeroes[3].equals("")) {

            BigDecimal zero = rec_approxBD(new BigDecimal("1"), new BigDecimal("0"), new BigDecimal("10"));
            COUNT = 0;

            for ( int i = 0 ; i <= zeroes.length ; i++ ) {
                if (zeroes[i].equals("")) {
                    zeroes[i] = zero.setScale(13, RoundingMode.DOWN).stripTrailingZeros().toString();
                    break;
                }
            }
        }


    }

    private BigDecimal rec_approxBD(BigDecimal stepSize, BigDecimal iterator, BigDecimal stepRoof) {

        for ( int i = 0; i <= 2; i++ ) {

            if (stepSize.abs().compareTo(new BigDecimal("1")) < 0 ) {
                break;
            }

            BigDecimal testStepSize = stepSize.divide(new BigDecimal("10").pow(i));

            for ( BigDecimal ii = iterator; ii.abs().compareTo(iterator.add(stepSize).abs()) < 1; ii = ii.add(testStepSize) ) {
                System.out.println("step: " + ii.toString());

                System.out.println("y(" + iterator + ") = " + calcYValueBD(iterator));
                System.out.println("y(" + ii + ") = " + calcYValueBD(ii));

                if ( calcYValueBD(iterator).signum() != calcYValueBD(ii).signum()) {
                    System.out.println("FOUND");

                    iterator = ii.subtract(testStepSize);
                    stepSize = testStepSize;

                    System.out.println("iterator: " + iterator);
                    System.out.println("stepSize: " + stepSize);

                    i = 5;
                    break;
                }
            }

        }

        BigDecimal yValue = calcYValueBD(iterator);

        BigDecimal nextStep = iterator.add(stepSize);
        nextStep = nextStep.stripTrailingZeros();

        BigDecimal smaller = new BigDecimal("0.1");
        BigDecimal smallerStep = stepSize.multiply(smaller);
        smallerStep = smallerStep.stripTrailingZeros();

        if (
                yValue.equals(new BigDecimal("0"))
                && Arrays.stream(zeroes).noneMatch(iterator.setScale(13, RoundingMode.DOWN).stripTrailingZeros().toString()::equals)
        ) {
            return iterator;
        }
        else if ( Arrays.stream(zeroes).anyMatch(iterator.setScale(13, RoundingMode.DOWN).stripTrailingZeros().toString()::equals) ) {

            if ( stepSize.compareTo(new BigDecimal("0")) > 0 ) {
                iterator = iterator.add(stepSize);
                stepSize = new BigDecimal("1");
                stepRoof = new BigDecimal("100");
            } else if ( stepSize.compareTo(new BigDecimal("0")) < 0 ) {
                iterator = iterator.add(stepSize);
                stepSize = new BigDecimal("-1");
                stepRoof = new BigDecimal("-100");
            }

            iterator = rec_approxBD(stepSize, iterator, stepRoof);
        }
        else if ( iterator.compareTo(new BigDecimal("1E+1")) > 0 ) {
            iterator = rec_approxBD( new BigDecimal("-1"), new BigDecimal("0"), new BigDecimal("-100") );
        }
        else if ( iterator.compareTo(new BigDecimal("-1E+1")) < 0 ) {
            return new BigDecimal("-99999999");
        }
        else if ( (iterator.abs()).compareTo(stepRoof.abs()) > 0 && stepSize.abs().signum() >= 0 ) {
            BigDecimal nextRoof = stepRoof.multiply( new BigDecimal("10") );
            iterator = rec_approxBD(stepSize, iterator, nextRoof);
        }
        else if ( yValue.signum() != calcYValueBD(nextStep).signum() && calcYValueBD(nextStep).signum() != 0 ) {
            BigDecimal newStepRoof = stepRoof.divide( new BigDecimal("10") );
            newStepRoof = newStepRoof.add(iterator);
            iterator = rec_approxBD(smallerStep, iterator, newStepRoof);
        }
        else {
            iterator = rec_approxBD(stepSize, nextStep, stepRoof);
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

    private void emptyZeroes() {
        zeroes[0] = "";
        zeroes[1] = "";
        zeroes[2] = "";
    }

    private void analyseBehaviour(){
        if (this.getSum(values) != 0 && values.get(3) == 0 && values.get(1) == 0) {
            this.behaviour[0] = "Achsensymmetrisch";
        } else if (this.getSum(values) != 0 && values.get(4) == 0 && values.get(2) == 0 && values.get(0) == 0) {
            this.behaviour[0] = "Punktsymmetrisch";
        } else {
            this.behaviour[0] = "Keine Symmetrien";
        }

        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) > 0) {
                this.behaviour[2] = String.valueOf(i);
            }
        }

        if (values.get(4) != 0) {
            emptyZeroes();
            rec_runThroughBD();
        } else if (values.get(3) != 0) {
            emptyZeroes();
            rec_runThroughBD();
        } else if (values.get(2) != 0) {
            emptyZeroes();
            rec_runThroughBD();
            //            secondDegreeZeroes();
        } else if (values.get(1) != 0) {
            emptyZeroes();
            rec_runThroughBD();
//            firstDegreeZeroes();
        } else if (values.get(0) != 0) {
            zeroDegreeZeroes();
        }

        this.behaviour[3] = String.valueOf(values.get(0));
        this.behaviour[4] = String.format("1. Nullstelle: %s\n2. Nullstelle: %s\n3. Nullstelle: %s\n4. Nullstelle: %s", zeroes[0], zeroes[1], zeroes[2], zeroes[3]);

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
