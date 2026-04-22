import java.io.*;
import java.util.*;

public class DataManager
{
    private File selectedDatasetFile;
    private List<double[]> rawDataset;
    private List<double[]> rawTrainData;
    private List<double[]> rawValData;
    private List<double[]> rawTestData;
    private double soilMin;
    private double soilMax;
    private double lastMin;
    private double lastMax;
    private boolean normalizationEnabled;

    public DataManager()
    {
        this.rawDataset = new ArrayList<>();
        this.rawTrainData = new ArrayList<>();
        this.rawValData = new ArrayList<>();
        this.rawTestData = new ArrayList<>();
        this.soilMin = 0;
        this.soilMax = 0;
        this.lastMin = 0;
        this.lastMax = 0;
        this.normalizationEnabled = false;
    }

    public void loadDataset(File file) throws Exception
    {
        selectedDatasetFile = file;
        rawDataset = loadDatasetFromFile(file);

        if (rawDataset.size() < 3) {
            throw new IllegalArgumentException("Dataset must contain at least 3 samples.");
        }

        splitDataset(rawDataset);
    }

    public List<double[]> loadDatasetFromFile(File file) throws Exception
    {
        List<double[]> data = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        br.readLine();

        while ((line = br.readLine()) != null)
        {
            if (line.trim().isEmpty()) {
                continue;
            }

            String[] parts = line.split(",");
            if (parts.length < 4) {
                throw new IllegalArgumentException("Invalid CSV format. Expected 4 columns: soil,last,type,label.");
            }

            double soil = Double.parseDouble(parts[0].trim());
            double last = Double.parseDouble(parts[1].trim());
            double type = Double.parseDouble(parts[2].trim());
            double needs = Double.parseDouble(parts[3].trim());
            data.add(new double[]{soil, last, type, needs});
        }

        br.close();
        return data;
    }

    public void splitDataset(List<double[]> dataset)
    {
        List<double[]> shuffled = cloneDataset(dataset);
        Collections.shuffle(shuffled);

        int totalSize = shuffled.size();
        int trainEnd = (int) (totalSize * 0.7);
        int valEnd = (int) (totalSize * 0.85);

        rawTrainData = cloneDataset(shuffled.subList(0, trainEnd));
        rawValData = cloneDataset(shuffled.subList(trainEnd, valEnd));
        rawTestData = cloneDataset(shuffled.subList(valEnd, totalSize));
    }

    public List<double[]> cloneDataset(List<double[]> src)
    {
        List<double[]> copy = new ArrayList<>();
        for (double[] row : src) {
            copy.add(Arrays.copyOf(row, row.length));
        }
        return copy;
    }

    public List<double[]> normalizeDataset(List<double[]> source)
    {
        List<double[]> normalized = new ArrayList<>();

        for (double[] row : source) {
            double[] transformed = Arrays.copyOf(row, row.length);
            transformed[0] = normalizeValue(row[0], soilMin, soilMax);
            transformed[1] = normalizeValue(row[1], lastMin, lastMax);
            normalized.add(transformed);
        }

        return normalized;
    }

    public void computeNormalizationStats(List<double[]> trainData)
    {
        soilMin = Double.MAX_VALUE;
        soilMax = -Double.MAX_VALUE;
        lastMin = Double.MAX_VALUE;
        lastMax = -Double.MAX_VALUE;

        for (double[] row : trainData) {
            soilMin = Math.min(soilMin, row[0]);
            soilMax = Math.max(soilMax, row[0]);
            lastMin = Math.min(lastMin, row[1]);
            lastMax = Math.max(lastMax, row[1]);
        }
    }

    public double normalizeValue(double value, double min, double max)
    {
        if (Math.abs(max - min) < 1e-9) {
            return 0;
        }
        return (value - min) / (max - min);
    }

    public double[] applyNormalizationToFeatures(double soil, double last, int type)
    {
        if (!normalizationEnabled) {
            return new double[]{soil, last, type};
        }

        return new double[]{
                normalizeValue(soil, soilMin, soilMax),
                normalizeValue(last, lastMin, lastMax),
                type
        };
    }

    public List<double[]> getTrainDataForModel(boolean normalize)
    {
        if (normalize) {
            computeNormalizationStats(rawTrainData);
            return normalizeDataset(rawTrainData);
        }
        return cloneDataset(rawTrainData);
    }

    public List<double[]> getValDataForModel(boolean normalize)
    {
        if (normalize) {
            return normalizeDataset(rawValData);
        }
        return cloneDataset(rawValData);
    }

    public List<double[]> getTestDataForModel(boolean normalize)
    {
        if (normalize) {
            return normalizeDataset(rawTestData);
        }
        return cloneDataset(rawTestData);
    }

    public File getSelectedDatasetFile()
    {
        return selectedDatasetFile;
    }

    public List<double[]> getRawDataset()
    {
        return rawDataset;
    }

    public int getDatasetSize()
    {
        return rawDataset.size();
    }

    public boolean isDatasetLoaded()
    {
        return !rawDataset.isEmpty();
    }

    public void setNormalizationEnabled(boolean enabled)
    {
        this.normalizationEnabled = enabled;
    }

    public boolean isNormalizationEnabled()
    {
        return normalizationEnabled;
    }

    public void reset()
    {
        rawDataset = new ArrayList<>();
        rawTrainData = new ArrayList<>();
        rawValData = new ArrayList<>();
        rawTestData = new ArrayList<>();
        soilMin = 0;
        soilMax = 0;
        lastMin = 0;
        lastMax = 0;
        normalizationEnabled = false;
        selectedDatasetFile = null;
    }
}

