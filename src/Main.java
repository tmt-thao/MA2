import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        DataLoader.loadStopIdToIndex("data/ZastavkyAll.csv");
        DataLoader.loadTrips("data/spoje_id_T1_3.csv");
        List<Trip> trips = StaticData.trips;

        DataLoader.loadMatrixKm("data/matrixKm.txt", StaticData.stopIdToIndex.size());
        DataLoader.loadMatrixTime("data/matrixTime.txt", StaticData.stopIdToIndex.size());

        MemeticAlgorithm ma = new MemeticAlgorithm(50, 100, 0.1, trips);
        ma.run();
    }
}
