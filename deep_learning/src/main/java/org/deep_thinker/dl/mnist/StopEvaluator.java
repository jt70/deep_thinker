package org.deep_thinker.dl.mnist;

import org.deep_thinker.dl.network.NeuralNetwork;

import java.util.LinkedList;

/**
 * The StopEvaluator keeps track of whether it is meaningful to continue
 * to train the network or if the error rate of the test data seems to
 * be on the raise (i.e. when we might be on our way to overfit the network).
 *
 * It also keeps a copy of the best neural network seen.
 */
class StopEvaluator {

    private int windowSize;
    private final NeuralNetwork network;
    private Double acceptableErrorRate;
    private final LinkedList<Double> errorRates;

    private String bestNetSoFar;
    private double lowestErrorRate = Double.MAX_VALUE;
    private double lastErrorAverage = Double.MAX_VALUE;

    public StopEvaluator(NeuralNetwork network, int windowSize, Double acceptableErrorRate) {
        this.windowSize = windowSize;
        this.network = network;
        this.acceptableErrorRate = acceptableErrorRate;
        this.errorRates = new LinkedList<>();
    }

    // See if there is any point in continuing ...
    public boolean stop(double errorRate) {
        // Save config of neural network if error rate is lowest we seen
        if (errorRate < lowestErrorRate) {
            lowestErrorRate = errorRate;
            bestNetSoFar = network.toJson(true);
        }

        if (acceptableErrorRate != null && lowestErrorRate < acceptableErrorRate) return true;

        // update moving average
        errorRates.addLast(errorRate);

        if (errorRates.size() < windowSize) {
            return false;   // never stop if we have not filled moving average
        }

        if (errorRates.size() > windowSize)
            errorRates.removeFirst();

        double avg = getAverage(errorRates);

        // see if we should stop
        if (avg > lastErrorAverage) {
            return true;
        } else {
            lastErrorAverage = avg;
            return false;
        }
    }

    public String getBestNetSoFar() {
        return bestNetSoFar;
    }

    public double getLowestErrorRate() {
        return lowestErrorRate;
    }

    private double getAverage(LinkedList<Double> list) {
        return list.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
    }
}
