import java.io.IOException;
import java.util.List;

public class Main2 {
    public static void main(String[] args) throws IOException {
        String filename = "src/points_01.txt";
        List<TSP.Point> points = TSP.readPoints(filename);
        int n = points.size();

        TSP.Result bestResult = null;
        int bestStartIndex = -1;

        long startTime = System.nanoTime();
        // Przechodzimy przez wszystkie możliwe punkty startowe (0-indexowane)
        for (int startIndex = 0; startIndex < n; startIndex++) {
            TSP.Result result = TSP.solveTSP(points, startIndex);
            if (bestResult == null || result.totalDistance < bestResult.totalDistance) {
                bestResult = result;
                bestStartIndex = startIndex;
            }
        }
        long endTime = System.nanoTime();

        System.out.println("Najlepszy punkt startowy: " + (bestStartIndex + 1));
        System.out.print("Najkrótsza trasa: ");
        for (Integer idx : bestResult.path) {
            System.out.print(idx + " -> ");
        }
        // Powrót do punktu startowego:
        System.out.println(bestResult.path.get(0));

        System.out.printf("Długość: %.4f%n", bestResult.totalDistance);
        System.out.printf("Czas wykonania (wszystkie kombinacje): %d ns%n", endTime - startTime);
    }
}
