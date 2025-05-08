import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TSP {

    public static class Point {
        double x, y;
        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class Result {
        public List<Integer> path; // indeksy zwiększone o 1, do outputu
        public double totalDistance;
        public Result(List<Integer> path, double totalDistance) {
            this.path = path;
            this.totalDistance = totalDistance;
        }
    }

    // Wczytuje punkty z pliku, zakładając, że każda linia zawiera trzy elementy (numer, x, y)
    public static List<Point> readPoints(String filepath) throws IOException {
        List<Point> points = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filepath));
        for (String line : lines) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length == 3) {
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                points.add(new Point(x, y));
            }
        }
        return points;
    }

    // Funkcja pomocnicza: liczba kwadratowa dystansu (bez pierwiastka)
    public static double distanceSquared(Point a, Point b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        return dx * dx + dy * dy;
    }

    // Tworzy macierz symetryczną, która przechowuje tylko elementy dla i < j.
    // Dla każdego i, tablica distHalf[i] ma długość (n - i - 1).
    public static double[][] computeSymmetricDistanceMatrix(List<Point> points) {
        int n = points.size();
        double[][] distHalf = new double[n][];
        for (int i = 0; i < n; i++) {
            int len = n - i - 1;
            distHalf[i] = new double[len];
            for (int j = i + 1; j < n; j++) {
                distHalf[i][j - i - 1] = distanceSquared(points.get(i), points.get(j));
            }
        }
        return distHalf;
    }

    // Pobiera kwadrat dystansu między punktami i oraz j z macierzy symetrycznej
    public static double getDistanceSquared(double[][] distHalf, int i, int j) {
        if (i == j) return 0;
        if (i < j) {
            return distHalf[i][j - i - 1];
        } else {
            return distHalf[j][i - j - 1];
        }
    }

    // Rozwiązuje problem komiwojażera metodą najbliższego sąsiada
    // Używamy BitSet do przechowywania informacji o odwiedzonych punktach.
    public static Result solveTSP(List<Point> points, Integer startIndex) {
        int n = points.size();
        if (n <= 1) {
            return new Result(Collections.singletonList(1), 0);
        }

        if (startIndex == null) {
            startIndex = new Random().nextInt(n);
        }

        BitSet visited = new BitSet(n);
        visited.set(startIndex);
        List<Integer> path = new ArrayList<>(n);
        path.add(startIndex);
        double totalDist = 0;

        // Obliczamy macierz symetryczną kwadratów odległości
        double[][] distHalf = computeSymmetricDistanceMatrix(points);

        for (int i = 0; i < n - 1; i++) {
            int last = path.get(path.size() - 1);
            int nearest = -1;
            double nearestDistSq = Double.POSITIVE_INFINITY;

            // Szukamy najbliższego nieodwiedzonego punktu
            for (int j = 0; j < n; j++) {
                if (!visited.get(j)) {
                    double d = getDistanceSquared(distHalf, last, j);
                    if (d < nearestDistSq) {
                        nearestDistSq = d;
                        nearest = j;
                    }
                }
            }

            visited.set(nearest);
            path.add(nearest);
            totalDist += Math.sqrt(nearestDistSq); // dodajemy faktyczną odległość
        }

        // Dodatkowo dodajemy dystans powrotny do punktu startowego
        Point lastPoint = points.get(path.get(path.size() - 1));
        Point startPoint = points.get(path.get(0));
        totalDist += Math.sqrt(distanceSquared(lastPoint, startPoint));

        // Jeśli chcesz zachować wyświetlanie indeksów zaczynając od 1, konwertujemy:
        List<Integer> convertedPath = new ArrayList<>(n);
        for (int i : path) {
            convertedPath.add(i + 1);
        }

        return new Result(convertedPath, totalDist);
    }
}
