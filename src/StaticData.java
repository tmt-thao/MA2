import java.util.HashMap;
import java.util.List;

public class StaticData {
    public static HashMap<Integer, Integer> stopIdToIndex;
    public static List<Trip> trips;
    public static double[][] matrixKm;
    public static int[][] matrixTime;
    public static double consumptionPerKm = 1.5;

    public static double getDeadheadEnergy(int from, int to) {
        return getTravelDistance(from, to) * consumptionPerKm;
    }

    public static int getTravelTime(int from, int to) {
        return matrixTime[from][to];
    }

    public static double getTravelDistance(int from, int to) {
        return matrixKm[from][to];
    }
}
