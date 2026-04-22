import java.util.*;

public class SA
{

    public static class SALogEntry
    {
        public final int iteration;
        public final double temperature;
        public final double currentCost;
        public final double nextCost;
        public final boolean accepted;
        public final boolean improvedBest;
        public final double bestCost;

        public SALogEntry(
                int iteration,
                double temperature,
                double currentCost,
                double nextCost,
                boolean accepted,
                boolean improvedBest,
                double bestCost
        ) {
            this.iteration = iteration;
            this.temperature = temperature;
            this.currentCost = currentCost;
            this.nextCost = nextCost;
            this.accepted = accepted;
            this.improvedBest = improvedBest;
            this.bestCost = bestCost;
        }
    }

    private static final List<SALogEntry> lastRunLog = new ArrayList<>();

    public static double distance(plants p1, plants p2)
    {
        double dx = p1.x - p2.x;
        double dy = p1.y - p2.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static double totalDistance(List<plants> route)
    {
        double total = 0;
        for (int i = 0; i < route.size() - 1; i++) {
            total += distance(route.get(i), route.get(i + 1));
        }
        return total;
    }

    public static List<plants> swapTwo(List<plants> route)
    {
        List<plants> newRoute = new ArrayList<>(route);

        Random random = new Random();
        if (newRoute.size() < 2) return newRoute;
        int i = random.nextInt(newRoute.size());
        int j;
        do {
            j = random.nextInt(newRoute.size());
        }
        while (j == i);
        Collections.swap(newRoute, i, j);
        return newRoute;
    }


    public static double cost(List<plants> route, List<plants> allPlants)
    {
        double dist = totalDistance(route);
        int missed = 0;
        int extra = 0;

        for (plants p : allPlants) {
            boolean inRoute = route.contains(p);

            if (p.needsWater == 1 && !inRoute)
                missed++;
            if (p.needsWater == 0 && inRoute)
                extra++;
        }

        return missed + dist + extra;
    }

    public static List<plants> SIM_ANNEAL(List<plants> m, int selectedCount, int iterMax, double T)
    {

        List<plants> shuffled = new ArrayList<>(m);
        Collections.shuffle(shuffled);

        if (selectedCount > shuffled.size()) {
            selectedCount = shuffled.size();
        }

        List<plants> current = new ArrayList<>(shuffled.subList(0, selectedCount));
        Collections.shuffle(current);

        List<plants> best = new ArrayList<>(current);
        double bestCost = cost(best, m);

        int j = 1;

        while (true)
        {
            T *= 0.995;
            double Tc = T;

            if (Tc <= 2 || j > iterMax)
                break;

            List<plants> next = swapTwo(current);
            double currentCost = cost(current, m);
            double nextCost = cost(next, m);
            double diff = nextCost - currentCost;
            boolean accepted = false;
            boolean improvedBest = false;

            if (diff < 0)
            {
                current = next;
                currentCost = nextCost;
                accepted = true;

                if (currentCost < bestCost)
                {
                    best = new ArrayList<>(current);
                    bestCost = currentCost;
                    improvedBest = true;
                }
            }
            else
            {
                double p = Math.exp(-diff / Tc);
                double r = Math.random();

                if (p > r)
                {
                    current = next;
                    accepted = true;
                    currentCost = nextCost;
                }
            }

            lastRunLog.add(new SALogEntry(j, Tc, currentCost, nextCost, accepted, improvedBest, bestCost));
            j++;
        }
        return best;
    }

    public static List<SALogEntry> getLastRunLog()
    {
        return new ArrayList<>(lastRunLog);
    }
}