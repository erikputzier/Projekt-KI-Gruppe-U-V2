/**
 * Example demonstrating how to use the GeneticAlgorithm class to optimize
 * evaluation weights for the Guard & Towers game.
 * <p>
 * The GeneticAlgorithm class implements a genetic algorithm to find optimal weights
 * for the evaluation function. It works by:
 * <p>
 * 1. Creating a population of individuals, each with a different set of weights
 * 2. Evaluating each individual by playing games against other individuals
 * 3. Selecting the best individuals to create a new generation through crossover and mutation
 * 4. Repeating the process for multiple generations to find the best set of weights
 * <p>
 * This example demonstrates the basic usage of the GeneticAlgorithm class, as well as
 * how to customize it for specific needs.
 */

public class GeneticAlgorithmExample {

    /**
     * Main method demonstrating how to use the GeneticAlgorithm class.
     * <p>
     * The process involves:
     * 1. Creating a GeneticAlgorithm instance
     * 2. Running the evolution process
     * 3. Getting the best individual (solution)
     * 4. Using the optimized weights in your application
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Step 1: Create a GeneticAlgorithm instance
        // You can use the default constructor for random initialization
        // This will create a population of individuals with random weights
        GeneticAlgorithm ga = new GeneticAlgorithm();

        // Alternatively, you can provide a seed for reproducible results
        // This is useful for debugging or when you want consistent results
        // GeneticAlgorithm ga = new GeneticAlgorithm(42);

        System.out.println("Genetic Algorithm initialized.");

        // Step 2: Run the evolution process
        // This will:
        // - Evaluate the initial population by playing games between individuals
        // - Create new generations through selection, crossover, and mutation
        // - Run for MAX_GENERATIONS (default: 20) generations
        // - Return the best individual found during the evolution
        System.out.println("Starting evolution process...");
        GeneticAlgorithm.Individual bestIndividual = ga.evolve();

        // Step 3: Get the results
        // The best individual represents the optimal set of weights found
        System.out.println("\nEvolution complete!");
        System.out.println("Best individual found:");
        System.out.println(bestIndividual.toString());

        // Step 4: Use the optimized weights
        // The weights can be accessed from the best individual
        // These weights correspond to the parameters in the evaluation function
        int[] optimizedWeights = bestIndividual.getWeights();

        System.out.println("\nOptimized weights can now be used in your evaluation function.");
        System.out.println("For example, you could update the constants in Eval.java with these values.");

        // Example of how you might use these weights in a real application:
        // You would typically update the constants in your evaluation function
        // with the optimized weights found by the genetic algorithm
        System.out.println("\nExample of how to use the optimized weights:");
        System.out.println("private static final int WIN_LOSS_WEIGHT = " + optimizedWeights[0] + ";");
        System.out.println("private static final int MATERIAL_PER_PIECE = " + optimizedWeights[1] + ";");
        System.out.println("private static final int TOWER_EXTRA_PER_LEVEL = " + optimizedWeights[2] + ";");
        System.out.println("private static final int CENTER_CONTROL_BONUS = " + optimizedWeights[3] + ";");
        System.out.println("private static final int FILE_ALIGNED_GUARD_BONUS = " + optimizedWeights[4] + ";");
        System.out.println("private static final int GUARD_PROGRESS_BONUS = " + optimizedWeights[5] + ";");
        System.out.println("private static final int MOBILITY_PER_MOVE = " + optimizedWeights[6] + ";");
        System.out.println("private static final int BLOCKED_TOWER_PENALTY = " + optimizedWeights[7] + ";");
        System.out.println("private static final int GUARD_SAFETY_PER_FRIEND = " + optimizedWeights[8] + ";");
        System.out.println("private static final int GUARD_THREAT_PER_ENEMY = " + optimizedWeights[9] + ";");

        // Note: The weights correspond to the following parameters in order:
        // 1. WIN_LOSS_WEIGHT - Weight for winning/losing the game
        // 2. MATERIAL_PER_PIECE - Weight for each piece on the board
        // 3. TOWER_EXTRA_PER_LEVEL - Weight for extra tower levels
        // 4. CENTER_CONTROL_BONUS - Weight for controlling the center
        // 5. FILE_ALIGNED_GUARD_BONUS - Weight for pieces aligned with enemy guard
        // 6. GUARD_PROGRESS_BONUS - Weight for guard progress towards target
        // 7. MOBILITY_PER_MOVE - Weight for mobility (number of legal moves)
        // 8. BLOCKED_TOWER_PENALTY - Weight for blocked towers
        // 9. GUARD_SAFETY_PER_FRIEND - Weight for friendly pieces near guard
        // 10. GUARD_THREAT_PER_ENEMY - Weight for enemy pieces near guard
    }
}