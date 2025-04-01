import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class DataLoader {

    public static void loadStopIdToIndex(String filename) throws IOException {
        StaticData.stopIdToIndex = new HashMap<>();
        int index = 0;

        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        br.readLine();

        while ((line = br.readLine()) != null) {
            String[] parts = line.split(";");
            int stopId = Integer.parseInt(parts[0]);
            StaticData.stopIdToIndex.put(stopId, index);
            index++;
        }

        br.close();
    }

    public static void loadTrips(String filePath) throws IOException {
        StaticData.trips = new ArrayList<>();
        
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        boolean first = true;

        while ((line = br.readLine()) != null) {
            if (first) { first = false; continue; }

            String[] parts = line.split(";");
            int id = Integer.parseInt(parts[0]);
            int startStop = StaticData.stopIdToIndex.get(Integer.parseInt(parts[4]));
            int endStop = StaticData.stopIdToIndex.get(Integer.parseInt(parts[5]));
            int startTime = Integer.parseInt(parts[6]);
            int endTime = Integer.parseInt(parts[7]);
            double energy = Double.parseDouble(parts[9]);

            StaticData.trips.add(new Trip(id, startStop, endStop, startTime, endTime, energy));
        }

        br.close();
        StaticData.trips.remove(0);
        StaticData.trips.remove(StaticData.trips.size() - 1);

    }

    public static void loadMatrixKm(String filePath, int size) throws IOException {
        StaticData.matrixKm = new double[size][size];
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        int row = 0;

        while ((line = br.readLine()) != null && row < size) {
            String[] parts = line.split(";");
            for (int col = 0; col < size; col++) {
                StaticData.matrixKm[row][col] = Double.parseDouble(parts[col]);
            }
            row++;
        }
        
        br.close();
    }

    public static void loadMatrixTime(String filePath, int size) throws IOException {
        StaticData.matrixTime = new int[size][size];
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        int row = 0;

        while ((line = br.readLine()) != null && row < size) {
            String[] parts = line.split(";");
            for (int col = 0; col < size; col++) {
                StaticData.matrixKm[row][col] = Integer.parseInt(parts[col]);
            }
            row++;
        }
        
        br.close();
    }
}
