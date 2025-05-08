import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TSP_2 {

    public static class Point {
        public final double x, y;
        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class Result {
        public final List<Integer> path;   // Ścieżka – indeksy zwiększone o 1 (dla prezentacji)
        public final double totalDistance;
        public Result(List<Integer> path, double totalDistance) {
            this.path = path;
            this.totalDistance = totalDistance;
        }
    }

    // Wczytuje punkty z pliku – zakłada, że każda linia zawiera trzy elementy: numer, x, y.
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

    // Oblicza euklidesową odległość między dwoma punktami.
    public static double distance(Point a, Point b) {
        double dx = a.x - b.x, dy = a.y - b.y;
        return Math.hypot(dx, dy);
    }

    // Oblicza dystans kwadratowy (bez pierwiastka) – używany do porównań.
    public static double distanceSquared(Point a, Point b) {
        double dx = a.x - b.x, dy = a.y - b.y;
        return dx * dx + dy * dy;
    }

    // Metoda najbliższego sąsiada – generuje początkową trasę jako tablica indeksów (0-indexowane).
    public static int[] nearestNeighbor(List<Point> points, int startIndex) {
        int n = points.size();
        boolean[] visited = new boolean[n];
        int[] route = new int[n];
        int curr = startIndex;
        route[0] = curr;
        visited[curr] = true;
        for (int i = 1; i < n; i++) {
            int next = -1;
            double best = Double.POSITIVE_INFINITY;
            for (int j = 0; j < n; j++) {
                if (!visited[j]) {
                    double d = distance(points.get(curr), points.get(j));
                    if (d < best) {
                        best = d;
                        next = j;
                    }
                }
            }
            route[i] = next;
            visited[next] = true;
            curr = next;
        }
        return route;
    }

    // Oblicza całkowitą długość cyklicznej trasy.
    public static double calculateTotalDistance(int[] route, List<Point> points) {
        double total = 0.0;
        int n = route.length;
        for (int i = 0; i < n - 1; i++) {
            total += distance(points.get(route[i]), points.get(route[i + 1]));
        }
        total += distance(points.get(route[n - 1]), points.get(route[0]));
        return total;
    }

    // 2-opt: wykonuje zamianę segmentu trasy między indeksami i i k.
    public static int[] twoOptSwap(int[] route, int i, int k) {
        int n = route.length;
        int[] newRoute = new int[n];
        // Kopiujemy segment [0, i-1]
        for (int c = 0; c < i; c++) {
            newRoute[c] = route[c];
        }
        // Odwracamy segment [i, k]
        int dec = 0;
        for (int c = i; c <= k; c++) {
            newRoute[c] = route[k - dec];
            dec++;
        }
        // Kopiujemy resztę trasy [k+1, n-1]
        for (int c = k + 1; c < n; c++) {
            newRoute[c] = route[c];
        }
        return newRoute;
    }

    // Ulepszenie trasy metodą 2-opt – iteracyjnie przeszukuje zamiany dwóch krawędzi.
    public static int[] twoOptImprovement(int[] route, List<Point> points) {
        int n = route.length;
        boolean improvement = true;
        int[] bestRoute = Arrays.copyOf(route, n);
        double bestDistance = calculateTotalDistance(bestRoute, points);

        while (improvement) {
            improvement = false;
            for (int i = 1; i < n - 1; i++) {
                for (int k = i + 1; k < n; k++) {
                    int[] newRoute = twoOptSwap(bestRoute, i, k);
                    double newDistance = calculateTotalDistance(newRoute, points);
                    if (newDistance < bestDistance) {
                        bestRoute = newRoute;
                        bestDistance = newDistance;
                        improvement = true;
                    }
                }
            }
        }
        return bestRoute;
    }

    // =========================
    // METODY POMOCNICZE DLA 3-opt
    // =========================

    // Odwraca tablicę int.
    public static int[] reverse(int[] arr) {
        int n = arr.length;
        int[] rev = new int[n];
        for (int i = 0; i < n; i++) {
            rev[i] = arr[n - 1 - i];
        }
        return rev;
    }

    // Łączy (konkatenuje) wiele tablic int w jedną.
    public static int[] concat(int[]... arrays) {
        int totalLength = 0;
        for (int[] arr : arrays) {
            totalLength += arr.length;
        }
        int[] result = new int[totalLength];
        int pos = 0;
        for (int[] arr : arrays) {
            System.arraycopy(arr, 0, result, pos, arr.length);
            pos += arr.length;
        }
        return result;
    }

    // Generuje kandydatów 3-opt dla danej kombinacji indeksów (i, j, k).
    public static int[][] generate3OptCandidates(int[] route, int i, int j, int k) {
        int n = route.length;
        // Segmenty: A = route[0..i], B = route[i+1..j], C = route[j+1..k], D = route[k+1..n-1]
        int[] A = Arrays.copyOfRange(route, 0, i + 1);
        int[] B = Arrays.copyOfRange(route, i + 1, j + 1);
        int[] C = Arrays.copyOfRange(route, j + 1, k + 1);
        int[] D = Arrays.copyOfRange(route, k + 1, n);

        List<int[]> candidates = new ArrayList<>();

        // Kandydat 1: Odwróć B: A + reverse(B) + C + D
        candidates.add(concat(A, reverse(B), C, D));

        // Kandydat 2: Odwróć C: A + B + reverse(C) + D
        candidates.add(concat(A, B, reverse(C), D));

        // Kandydat 3: Zamień B i C: A + C + B + D
        candidates.add(concat(A, C, B, D));

        // Kandydat 4: Odwróć B i C: A + reverse(B) + reverse(C) + D
        candidates.add(concat(A, reverse(B), reverse(C), D));

        // Kandydat 5: A + reverse(C) + B + D
        candidates.add(concat(A, reverse(C), B, D));

        // Kandydat 6: A + C + reverse(B) + D
        candidates.add(concat(A, C, reverse(B), D));

        return candidates.toArray(new int[0][]);
    }

    // Ulepszenie trasy metodą 3-opt – przeszukuje wszystkie kombinacje trzech krawędzi.
    public static int[] threeOptImprovement(int[] route, List<Point> points) {
        int n = route.length;
        boolean improvement = true;
        int[] bestRoute = Arrays.copyOf(route, n);
        double bestDistance = calculateTotalDistance(bestRoute, points);

        while (improvement) {
            improvement = false;
            for (int i = 0; i < n - 2; i++) {
                for (int j = i + 1; j < n - 1; j++) {
                    for (int k = j + 1; k < n; k++) {
                        int[][] candidates = generate3OptCandidates(bestRoute, i, j, k);
                        for (int[] candidate : candidates) {
                            double candidateDistance = calculateTotalDistance(candidate, points);
                            if (candidateDistance < bestDistance) {
                                bestDistance = candidateDistance;
                                bestRoute = candidate;
                                improvement = true;
                            }
                        }
                    }
                }
            }
        }
        return bestRoute;
    }

    // =========================
    // GŁÓWNA METODA SOLVE TSP – łączymy NN, 2-opt i 3-opt
    // =========================
    public static Result solveTSP(List<Point> points, int startIndex) {
        int[] nnRoute = nearestNeighbor(points, startIndex);
        int[] route2Opt = twoOptImprovement(nnRoute, points);
        int[] route3Opt = threeOptImprovement(route2Opt, points);
        double totalDist = calculateTotalDistance(route3Opt, points);

        // Konwersja do List<Integer> i zwiększenie indeksów o 1 (dla prezentacji)
        List<Integer> outputRoute = new ArrayList<>();
        for (int r : route3Opt) {
            outputRoute.add(r + 1);
        }
        return new Result(outputRoute, totalDist);
    }
}
