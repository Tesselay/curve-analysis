package sample;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;

// TODO Replace doubles with BigDecimal where sensible
// TODO Add enum (or similar) to get function references in a list to iterate through
// TODO Refactor zero point search to search until no value is found anymore
// TODO Make gui pretty
// TODO add settings to alter maxDepth and maxRoof
// TODO improve performance
    // instead of searching to max in positive and then negative, switch up = pos till 10 - neg till 10 - pos till 100 - etc.
// FIXME deeper search for double sign changes leads to buggy behaviour (synchro issue?)
// FIXME remove unnecessary behavious value

public class Curve extends Pane {

    private ArrayList<Double> values;
    private double xMin;
    private double xMax;
    private double xInc;
    private Axes axes;
    private Path path;
    private String[] behaviour = {"","","","",""};          // 0 = symmetries ; 2 = degree of function ; 3 = y-intercept ; 4 = zeroes
    private String[] zeroes = {"", "", "", ""};

    private int maxDepth = 2;       // Defines max depth distance between double sign changes
    // To prevent rounding errors, the decimal places are dependent on maxDepth (and the min-value 0.01 which is ignored here for the time being)
    // For better understanding: 0,01x^4 -> 0,01 * ( 1 / 10^maxDepth )^4 = 1 * 10^( 2 + maxDepth * 4 )
    private int decimalPlaces = 2 + maxDepth * 4;
    private BigDecimal roofMultiplier = new BigDecimal("10");           // Changes multiplier for new step roof calculations
    private BigDecimal maxRoof = new BigDecimal("100");         // Changing this to higher values heavily impacts performance

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
        double y = calcYValueBD(new BigDecimal(String.valueOf(x))).doubleValue();

        path.getElements().add(
                new MoveTo(
                        mapX(x, axes), mapY(y, axes)
                )
        );

        x += xInc;
        while (x < xMax) {
            y = calcYValueBD(new BigDecimal(String.valueOf(x))).doubleValue();
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

    private double getSum(ArrayList<Double> values) {
        double sum = 0;
        for (Double value: values) {
            sum += value;
        }

        return sum;
    }

    private BigDecimal calcYValueBD(BigDecimal x) {
        BigDecimal y = new BigDecimal("0");

        for ( int i = 0; i < values.size(); i++ ) {
            BigDecimal tempValue = new BigDecimal( String.valueOf(values.get(i)) );
            BigDecimal powerTo = x.pow(i);
            BigDecimal newTemp = tempValue.multiply(powerTo);
            y = y.add(newTemp);
        }

        y = y.setScale(decimalPlaces, RoundingMode.DOWN);
        y = y.stripTrailingZeros();

        return y;
    }

    private Pair<BigDecimal, BigDecimal> doubleSignChangeSearch(BigDecimal iterator, BigDecimal stepSize) {
        if (stepSize.abs().compareTo(new BigDecimal("1")) >= 0 ) {

            for ( int i = 0; i <= maxDepth; i++ ) {

                BigDecimal newStepSize = stepSize.divide(new BigDecimal("10").pow(i));      // stepSize / 10^i
                for ( BigDecimal ii = iterator; ii.abs().compareTo(iterator.add(stepSize).abs()) < 1; ii = ii.add(newStepSize) ) {
                    if ( calcYValueBD(iterator).signum() != calcYValueBD(ii).signum() ) {
                        iterator = ii.subtract(newStepSize);
                        stepSize = newStepSize;

                        return new Pair<>(iterator, stepSize);
                    }
                }

            }
        }

        return new Pair<>(iterator, stepSize);
    }

    private BigDecimal rec_approxBD(BigDecimal stepSize, BigDecimal iterator, BigDecimal stepRoof) {

        // TODO replace with Lambda
        Pair<BigDecimal, BigDecimal> p = doubleSignChangeSearch(iterator, stepSize);
        iterator = p.getKey();
        stepSize = p.getValue();

        BigDecimal yValue = calcYValueBD(iterator);

        BigDecimal nextStep = iterator.add(stepSize);
        nextStep = nextStep.stripTrailingZeros();

        if (    yValue.equals(new BigDecimal("0"))
                && Arrays.stream(zeroes).noneMatch(iterator.setScale(decimalPlaces - 2, RoundingMode.DOWN).stripTrailingZeros().toString()::equals) ) {
            return iterator;
        }
        else if ( Arrays.stream(zeroes).anyMatch(iterator.setScale(decimalPlaces - 2, RoundingMode.DOWN).stripTrailingZeros().toString()::equals) ) {

            if ( stepSize.compareTo(new BigDecimal("0")) > 0 ) {
                iterator = iterator.add(new BigDecimal("1").divide(new BigDecimal("10").pow(maxDepth)));            // Since it can find multiple sign changes only up to maxDepth, no need to go deeper here
                stepSize = new BigDecimal("1");
                stepRoof = maxRoof;             // TODO change new stepRoof depending on iterator position
            } else if ( stepSize.compareTo(new BigDecimal("0")) < 0 ) {
                iterator = iterator.add(new BigDecimal("-1").divide(new BigDecimal("10").pow(maxDepth)));
                stepSize = new BigDecimal("-1");
                stepRoof = maxRoof.negate();
            }

            iterator = rec_approxBD(stepSize, iterator, stepRoof);
        }
        else if ( iterator.compareTo(maxRoof) > 0 ) {
            iterator = rec_approxBD( new BigDecimal("-1"), new BigDecimal("0"), new BigDecimal("-100") );
        }
        else if ( iterator.compareTo(maxRoof.negate()) < 0 ) {
            return new BigDecimal("-99999");
        }
        else if ( (iterator.abs()).compareTo(stepRoof.abs()) > 0 && stepSize.abs().signum() >= 0 ) {
            BigDecimal nextRoof = stepRoof.multiply( roofMultiplier );
            iterator = rec_approxBD(stepSize, iterator, nextRoof);
        }
        else if (   yValue.signum() != calcYValueBD(nextStep).signum()
                    && calcYValueBD(nextStep).signum() != 0 ) {
            BigDecimal newStepRoof = stepRoof.divide( roofMultiplier );
            newStepRoof = newStepRoof.add(iterator);
            iterator = rec_approxBD(stepSize.multiply(new BigDecimal("0.1")), iterator, newStepRoof);
        }
        else {
            iterator = rec_approxBD(stepSize, nextStep, stepRoof);
        }
        return iterator;
    }

    private void rec_runThroughBD() {
        while (zeroes[0].equals("") || zeroes[1].equals("") || zeroes[2].equals("") || zeroes[3].equals("")) {

            BigDecimal zero = rec_approxBD(new BigDecimal("1"), new BigDecimal("0"), new BigDecimal("10"));

            for ( int i = 0 ; i <= zeroes.length ; i++ ) {
                if (zeroes[i].equals("")) {
                    zeroes[i] = zero.setScale(decimalPlaces - 2, RoundingMode.DOWN).stripTrailingZeros().toString();
                    break;
                }
            }
        }


    }

    private void zeroDegreeZeroes() {
        if (values.get(0) == 0) {
            for ( int i = 0; i < zeroes.length; i++ ) {
                if (zeroes[i].equals("")) {
                    zeroes[i] = "Infinite!";
                }
            }
        }
    }

    private void firstDegreeZeroes() {
        // 0 = mx + b -> -b/m = x           =>       (-1 * values.get(0)) / values.get(1) = x
        zeroes[0] = String.valueOf((-1 * values.get(0)) / values.get(1));
    }

    private void secondDegreeZeroes() {
        // f(x) = ax^2 + bx + c -- p = b/a, q = c/a --> f(x) = ax^2 + p + q
        double p = values.get(1) / values.get(2);
        double q = values.get(0) / values.get(2);
        double precedeEquation = (-1 * p) / 2;
        double sqrtEquation = Math.sqrt(Math.pow((p / 2), 2) - q);

        zeroes[0] = String.valueOf(precedeEquation + sqrtEquation);
        zeroes[1] = String.valueOf(precedeEquation - sqrtEquation);
    }

    private void analyseSymmetries() {
        if (this.getSum(values) != 0 && values.get(3) == 0 && values.get(1) == 0) {
            this.behaviour[0] = "Even symmetry";
        } else if (this.getSum(values) != 0 && values.get(4) == 0 && values.get(2) == 0 && values.get(0) == 0) {
            this.behaviour[0] = "Odd symmetry";
        } else {
            this.behaviour[0] = "No symmetries";
        }
    }

    private void analyseDegree() {
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) != 0) {
                this.behaviour[2] = String.valueOf(i);
            }
        }
    }

    private void analyseRoots() {
        if (values.get(4) != 0) {
            rec_runThroughBD();
        } else if (values.get(3) != 0) {
            rec_runThroughBD();
        } else if (values.get(2) != 0) {
            secondDegreeZeroes();
        } else if (values.get(1) != 0) {
            firstDegreeZeroes();
        } else if (values.get(0) != 0) {
            zeroDegreeZeroes();
        }

        for ( int i = 0; i < zeroes.length; i++ ) {
            if (zeroes[i].equals("") || zeroes[i].equals("-99999")) {
                zeroes[i] = "No value found!";
            }
        }

        this.behaviour[4] = String.format("1. Root: %s\n2. Root: %s\n3. Root: %s\n4. Root: %s", zeroes[0], zeroes[1], zeroes[2], zeroes[3]);
    }

    private void analyseYIntercept() {
        this.behaviour[3] = String.valueOf(values.get(0));
    }

    private void analyseBehaviour(){
        analyseSymmetries();
        analyseDegree();
        analyseRoots();
        analyseYIntercept();
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
