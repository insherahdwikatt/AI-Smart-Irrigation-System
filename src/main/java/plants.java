class plants
{
    public double soilMoisture;
    public double lastWatered;
    public int plantType;
    public double x;
    public double y;
    public int needsWater;

    public plants(double soilMoisture, double lastWatered, int plantType, double x, double y)
    {
        this.soilMoisture = soilMoisture;
        this.lastWatered = lastWatered;
        this.plantType = plantType;
        this.x = x;
        this.y = y;
        this.needsWater = 0;
    }

    public double[] getFeatures()
    {
        return new double[]{soilMoisture, lastWatered, plantType};
    }
}