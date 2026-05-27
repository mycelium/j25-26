import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;
import loadtest.*;

/**
 * Point d'entrée du lab 3.
 */
public class Main {

    public static void main(String[] args) throws Exception {

        // Configuration simple via args
        int    port               = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        int    totalRequests      = args.length > 1 ? Integer.parseInt(args[1]) : 500;
        int    concurrentClients  = args.length > 2 ? Integer.parseInt(args[2]) : 20;
        int    threadCount        = args.length > 3 ? Integer.parseInt(args[3]) : 8;

        // Réduire le bruit des logs durant les tests
        Logger.getLogger("").setLevel(Level.WARNING);

        System.out.printf("  port=%d  requests=%d  clients=%d  threads=%d%n%n",
            port, totalRequests, concurrentClients, threadCount);

        // 4 combinaisons { virtual, useOwn }
        boolean[][] combos = {
            { true,  true  },   // Virtual + own parser
            { true,  false },   // Virtual + Gson
            { false, true  },   // Classic + own parser
            { false, false },   // Classic + Gson
        };
        String[] comboNames = {
            "Virtual+own", "Virtual+Gson", "Classic+own", "Classic+Gson"
        };

        List<LoadTestResult> allResults = new ArrayList<>();

        for (int c = 0; c < combos.length; c++) {
            boolean useVirtual  = combos[c][0];
            boolean useOwn      = combos[c][1];
            String  comboName   = comboNames[c];

            System.out.printf("▶ Running : %s%n", comboName);

            // Déterminer l'adapter JSON
            JsonAdapter adapter = useOwn ? JsonAdapter.OWN : JsonAdapter.GSON;

            // Démarrer le serveur - le constructeur démarre automatiquement le serveur
            LoadTestServer server = new LoadTestServer(
                "localhost",
                port,
                threadCount,
                useVirtual,
                adapter
            );

            // Attendre que le serveur soit prêt
            Thread.sleep(800);

            // Créer la config pour LoadTestRunner (différente de celle du serveur)
            LoadTestConfig config = LoadTestConfig.builder()
                .scenarioName(comboName)
                .virtualThreads(useVirtual)
                .jsonAdapter(adapter)
                .threadCount(concurrentClients)
                .totalRequests(totalRequests)
                .targetHost("localhost")
                .targetPort(port)
                .build();

            LoadTestRunner runner = new LoadTestRunner(config);

            // Exécuter les tests (run() sans paramètres retourne les 2 résultats)
            System.out.println("  [running tests]");
            List<LoadTestResult> results = runner.run();
            
            // Ajouter les résultats
            allResults.addAll(results);
            
            // Afficher les résultats
            for (LoadTestResult r : results) {
                System.out.printf("  %s: avg=%.1f ms, p95=%.1f ms, throughput=%.1f req/s%n",
                    r.requestLabel, r.avgMs, r.p95Ms, r.throughput);
            }

            // Arrêter le serveur
            server.stop();
            Thread.sleep(500);
            System.out.printf("  ✓ done%n%n");
        }

        // Affichage du tableau final - méthode manuelle car printTable n'existe pas
        System.out.println("\n=== FINAL RESULTS TABLE ===");
        System.out.printf("%-20s %-12s %10s %10s %10s%n", 
            "Scenario", "Request", "Avg(ms)", "p95(ms)", "Req/s");
        System.out.println("=".repeat(70));
        for (LoadTestResult r : allResults) {
            System.out.printf("%-20s %-12s %10.1f %10.1f %10.1f%n",
                r.scenarioName, r.requestLabel, r.avgMs, r.p95Ms, r.throughput);
        }
    }
}