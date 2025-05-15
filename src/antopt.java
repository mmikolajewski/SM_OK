import java.io.*;
import java.util.*;

class antopt {

    static class Edge {
        int to;
        double weight;
        double pheromone;

        Edge(int to, double weight) {
            this.to = to;
            this.weight = weight;
            this.pheromone = 1.0;
        }
    }

    static List<double[]> coords;
    static Map<Integer, List<Edge>> graph = new HashMap<>();

    static double euclidean(double[] p1, double[] p2) {
        return Math.hypot(p1[0] - p2[0], p1[1] - p2[1]);
    }

    static void readCoords(String filepath) throws IOException {
        coords = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filepath));
        int n = Integer.parseInt(br.readLine().trim());
        for (int i = 0; i < n; i++) {
            String[] parts = br.readLine().trim().split(" ");
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            coords.add(new double[]{x, y});
        }
        br.close();
    }

    static void buildGraph() {
        int n = coords.size();
        for (int i = 0; i < n; i++) {
            graph.put(i, new ArrayList<>());
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    double dist = euclidean(coords.get(i), coords.get(j));
                    graph.get(i).add(new Edge(j, dist));
                }
            }
        }
    }

    static int chooseNextNode(int current, Set<Integer> visited, double alpha, double beta) {
        List<Edge> neighbors = graph.get(current);
        List<Double> probabilities = new ArrayList<>();
        double total = 0.0;

        for (Edge edge : neighbors) {
            if (!visited.contains(edge.to)) {
                double desirability = Math.pow(edge.pheromone, alpha) * Math.pow(1.0 / edge.weight, beta);
                probabilities.add(desirability);
                total += desirability;
            } else {
                probabilities.add(0.0);
            }
        }

        double r = Math.random() * total;
        double cumulative = 0.0;
        for (int i = 0; i < neighbors.size(); i++) {
            if (visited.contains(neighbors.get(i).to)) continue;
            cumulative += probabilities.get(i);
            if (cumulative >= r) return neighbors.get(i).to;
        }
        return -1;
    }

    static List<Integer> buildPath(int start, double alpha, double beta) {
        Set<Integer> visited = new HashSet<>();
        List<Integer> path = new ArrayList<>();
        int current = start;
        path.add(current);
        visited.add(current);

        while (visited.size() < coords.size()) {
            int next = chooseNextNode(current, visited, alpha, beta);
            if (next == -1) break;
            path.add(next);
            visited.add(next);
            current = next;
        }
        return path;
    }

    static double totalLength(List<Integer> path) {
        double dist = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            dist += euclidean(coords.get(path.get(i)), coords.get(path.get(i + 1)));
        }
        dist += euclidean(coords.get(path.get(path.size() - 1)), coords.get(path.get(0)));
        return dist;
    }

    static List<Integer> twoOpt(List<Integer> path) {
        boolean improvement = true;
        while (improvement) {
            improvement = false;
            for (int i = 1; i < path.size() - 2; i++) {
                for (int j = i + 1; j < path.size() - 1; j++) {
                    double delta = -euclidean(coords.get(path.get(i - 1)), coords.get(path.get(i)))
                            - euclidean(coords.get(path.get(j)), coords.get(path.get(j + 1)))
                            + euclidean(coords.get(path.get(i - 1)), coords.get(path.get(j)))
                            + euclidean(coords.get(path.get(i)), coords.get(path.get(j + 1)));
                    if (delta < -1e-6) {
                        Collections.reverse(path.subList(i, j + 1));
                        improvement = true;
                    }
                }
            }
        }
        return path;
    }

    static void updatePheromones(List<List<Integer>> allPaths, List<Double> lengths, double evaporation, double Q, List<Integer> bestPath, double bestLength) {
        for (List<Edge> edges : graph.values()) {
            for (Edge e : edges) {
                e.pheromone *= (1.0 - evaporation);
            }
        }

        for (int i = 0; i < allPaths.size(); i++) {
            List<Integer> path = allPaths.get(i);
            double contribution = Q / lengths.get(i);
            for (int j = 0; j < path.size(); j++) {
                int from = path.get(j);
                int to = path.get((j + 1) % path.size());
                for (Edge edge : graph.get(from)) {
                    if (edge.to == to) {
                        edge.pheromone += contribution;
                        break;
                    }
                }
            }
        }

        // Dodanie feromonu elitarnego dla najlepszej ścieżki
        double eliteContrib = Q * 5.0 / bestLength;
        for (int j = 0; j < bestPath.size(); j++) {
            int from = bestPath.get(j);
            int to = bestPath.get((j + 1) % bestPath.size());
            for (Edge edge : graph.get(from)) {
                if (edge.to == to) {
                    edge.pheromone += eliteContrib;
                    break;
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        readCoords("src/txt/bier127.txt");
        buildGraph();

        int n_ants = 10000;
        int n_iterations = 100;
        double alpha = 1.2, beta = 4.0, evaporation = 0.1, Q = 500.0;

        List<Integer> bestPath = null;
        double bestLength = Double.POSITIVE_INFINITY;

        for (int it = 0; it < n_iterations; it++) {
            List<List<Integer>> allPaths = new ArrayList<>();
            List<Double> lengths = new ArrayList<>();

            for (int a = 0; a < n_ants; a++) {
                int start = new Random().nextInt(coords.size());
                List<Integer> path = buildPath(start, alpha, beta);
                if (path.size() != coords.size()) continue;
                path = twoOpt(path);
                double len = totalLength(path);
                allPaths.add(path);
                lengths.add(len);
                if (len < bestLength) {
                    bestLength = len;
                    bestPath = new ArrayList<>(path);
                }
            }
            updatePheromones(allPaths, lengths, evaporation, Q, bestPath, bestLength);
            System.out.printf("Iteracja %d: najlepsza długość = %.2f\n", it + 1, bestLength);
        }

        System.out.println("\nNajlepsza długość trasy: " + bestLength);
        System.out.println("Najlepsza trasa: " + bestPath);
    }
}
