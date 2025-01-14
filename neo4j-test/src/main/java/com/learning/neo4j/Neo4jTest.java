package com.learning.neo4j;

import org.neo4j.driver.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class Neo4jTest {

    private static final String DATABASE = "neo4j";
    private static final AccessMode ACCESS_MODE = AccessMode.READ;

    private static final String QUERY_0 = "MATCH (n) RETURN COUNT(n)";
    private static final String QUERY_1 = """
                CALL db.labels() YIELD label
                CALL {
                  WITH label
                  MATCH (n)
                  WHERE label IN labels(n)
                  RETURN count(n) AS count
                }
                RETURN label, count;
                """;
    private static final String QUERY_2 = """
                CALL {
                  MATCH (n)
                  RETURN labels(n) AS nodeLabels, count(*) AS c
                }
                UNWIND nodeLabels AS label
                RETURN label, sum(c) AS count;
                """;
    private static final String QUERY_3 = """
            CALL apoc.meta.stats() YIELD labels
            UNWIND keys(labels) AS label
            RETURN label, labels[label] AS count;
            """;


    public static void main(String[] args) {

        Driver driver = createDriver();
        driver.verifyConnectivity();

        populateData(driver);

        String query = QUERY_0;
        sessionRun(driver, query);
        transactionRun(driver, query);

        driver.close();
    }

    private static Driver createDriver() {
        String url = "neo4j://localhost:7687";
        String user = "neo4j";
        String pass = "Admin@123";
        Config config = Config.builder().build();

        return GraphDatabase.driver(url, AuthTokens.basic(user, pass), config);
    }

    private static void sessionRun(Driver driver, String query) {
        SessionConfig sessionConfig = SessionConfig.builder()
                .withDefaultAccessMode(ACCESS_MODE)
                .withDatabase(DATABASE).build();
        Session session = driver.session(sessionConfig);

        TransactionConfig sessionTransactionConfig = TransactionConfig.builder()
                .withTimeout(Duration.ofSeconds(30L))
                .build();

        long start = System.currentTimeMillis();
        try {
            Result result = session.run(query, sessionTransactionConfig);
            System.out.println("Result:" + result.list() + " TimeInSeconds:" + ((System.currentTimeMillis() - start) / 1000));
        } catch (Exception e) {
            System.out.println("TimeInSeconds:" + ((System.currentTimeMillis() - start) / 1000));
            e.printStackTrace();
            session.close();
        }
    }

    private static void transactionRun(Driver driver, String query) {
        SessionConfig sessionConfig = SessionConfig.builder()
                .withDefaultAccessMode(AccessMode.READ)
                .withDatabase("neo4j").build();
        Session session = driver.session(sessionConfig);

        TransactionConfig transactionConfig = TransactionConfig.builder()
                .withTimeout(Duration.ofSeconds(30L))
                .build();
        Transaction transaction = session.beginTransaction(transactionConfig);

        long start = System.currentTimeMillis();
        try {
            Result result = transaction.run(query);
            System.out.println("Result:" + result.list() + " TimeInSeconds:" + ((System.currentTimeMillis() - start) / 1000));
        } catch (Exception e) {
            System.out.println("TimeInSeconds:" + ((System.currentTimeMillis() - start) / 1000));
            e.printStackTrace();
            transaction.rollback();
            session.close();
        }
    }


    private static void populateData(Driver driver) {

        int index = 0;
        for (int i = 0; i < 10; i++) {
            if (index > 9) {
                index = 0;
            }
            System.out.println("PopulateData LoopIndex:" + i + " LabelIndex:" + index);

            int numberOfNodes = 1_000_000;

            String createQuery = "UNWIND range(1, $numNodes) AS id " +
                    "CREATE (:TestNode" + index++ + " {id: id, name: 'Node_' + id})";
            Map<String, Object> params = new HashMap<>();
            params.put("numNodes", numberOfNodes);

            SessionConfig sessionConfig = SessionConfig.builder()
                    .withDefaultAccessMode(AccessMode.READ)
                    .withDatabase("neo4j").build();
            Session session = driver.session(sessionConfig);

            try {
                session.executeWrite(txn -> {
                    txn.run(createQuery, params);
                    return null;
                });
            } catch (Exception e) {
                e.printStackTrace();
                session.close();
            }
        }
    }
}
