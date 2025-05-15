import java.io.IOException;
import java.util.List;

public class Greedy {
    public static void main(String[] args) throws IOException {
        String filename = "src/txt/berlin52.txt";
        TSP tsp = new TSP();
        List<TSP.Point> points = TSP.readPoints(filename);

        int startIndex = 0;

        long startTime = System.nanoTime();
        TSP.Result result = TSP.solveTSP(points, startIndex);
        long endTime = System.nanoTime();

        List<Integer> path = result.path;
        double dist = result.totalDistance;

        System.out.print("Najkrótsza trasa: ");
        for (int i = 0; i < path.size(); i++) {
            System.out.print(path.get(i));
            System.out.print(" -> ");
        }
        System.out.println(path.get(0));

        System.out.printf("Długość: %.4f%n", dist);
        System.out.printf("Czas wykonania: %d ns%n", endTime - startTime);
    }
}
