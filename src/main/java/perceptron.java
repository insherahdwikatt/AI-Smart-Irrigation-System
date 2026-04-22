import java.util.*;

public class perceptron
{
    private double[] w;
    private double f;
    private double lr;
    private final List<EpochStats> trainingHistory;

    public static class EpochStats
    {
        public final int epoch;
        public final int errors;
        public final int trainingErrors;
        public final int validationErrors;
        public final double accuracy;

        public EpochStats(int epoch, int trainingErrors, int validationErrors, double accuracy)
        {
            this.epoch = epoch;
            this.trainingErrors = trainingErrors;
            this.validationErrors = validationErrors;
            this.errors = trainingErrors;
            this.accuracy = accuracy;
        }
    }

    public perceptron(int nFeatures, double learningRate)
    {
        w = new double[nFeatures];
        lr = learningRate;
        trainingHistory = new ArrayList<>();

        Random rnd = new Random(42);
        double range = 0.5;

        f = (rnd.nextDouble() * 2 - 1) * range;

        for (int i = 0; i < nFeatures; i++)
        {
            w[i] = (rnd.nextDouble() * 2 - 1) * range;
        }
    }

    public double net(double[] x)
    {
        double sum = f;
        for (int i = 0; i < x.length; i++)
        {
            sum += w[i] * x[i];
        }
        return sum;
    }

    public int step(double netValue)
    {

        return netValue >= 0 ? 1 : 0;
    }

    public int predict(double[] x)
    {

        return step(net(x));
    }

    public void learnFromSample(double[] x, int yd)
    {
        int ya = predict(x);
        int error = yd - ya;

        f += lr * error;

        for (int i = 0; i < x.length; i++)
        {
            w[i] += lr * error * x[i];
        }
    }

    public double train(List<double[]> data, int epochs)
    {
        return train(data, null, epochs);
    }

    public double train(List<double[]> trainData, List<double[]> validationData, int epochs)
    {
        trainingHistory.clear();

        for (int e = 0; e < epochs; e++)
        {
            for (double[] row : trainData)
            {
                double[] x = {row[0], row[1], row[2]};
                int y = (int) row[3];
                learnFromSample(x, y);
            }

            int trainingErrors = errorCount(trainData);
            int validationErrors = validationData == null || validationData.isEmpty() ? 0 : errorCount(validationData);
            double epochAccuracy = accuracy(trainData);
            trainingHistory.add(new EpochStats(e + 1, trainingErrors, validationErrors, epochAccuracy));
        }

        return accuracy(trainData);
    }

    private int errorCount(List<double[]> data)
    {
        int errors = 0;

        for (double[] row : data)
        {
            double[] x = {row[0], row[1], row[2]};
            int y = (int) row[3];

            if (predict(x) != y)
            {
                errors++;
            }
        }

        return errors;
    }

    public double accuracy(List<double[]> data)
    {
        int correct = 0;

        for (double[] row : data)
        {
            double[] x = {row[0], row[1], row[2]};
            int y = (int) row[3];

            int pred = predict(x);
            if (pred == y)
                correct++;
        }

        return (double) correct / data.size();
    }

    public List<EpochStats> getTrainingHistory()
    {
        return new ArrayList<>(trainingHistory);
    }
}