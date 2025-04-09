import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        String version = "A_4";
        DataLoader.loadStopIdToIndex("data/ZastavkyAll.csv");
        DataLoader.loadChargers("data/chargers_" + version + ".csv");
        DataLoader.loadChargingEvents("data/ChEvents_" + version + ".csv");
        DataLoader.loadTrips("data/spoje_id_" + version + ".csv");
        List<Trip> trips = StaticData.trips;

        DataLoader.loadMatrixKm("data/matrixKm.txt", StaticData.stopIdToIndex.size());
        DataLoader.loadMatrixTime("data/matrixTime.txt", StaticData.stopIdToIndex.size());

        MemeticAlgorithm ma = new MemeticAlgorithm(50, 100, 0.5, trips);
        ma.run();
    }
}
