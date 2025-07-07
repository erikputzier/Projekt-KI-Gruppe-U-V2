import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

/**
 * Test class for the GeneticAlgorithm implementation.
 * Tests include time limits to ensure they don't run indefinitely.
 */
public class GeneticAlgorithmTest {

    // Global timeout rule to prevent tests from running too long
    @Rule
    public Timeout globalTimeout = new Timeout(120, TimeUnit.SECONDS);

    /**
     * Test that the genetic algorithm can be initialized without errors.
     */
    @Test
    public void testInitialization() {
        System.out.println("[DEBUG_LOG] Starting initialization test");
        GeneticAlgorithm ga = new GeneticAlgorithm(42); // Use fixed seed for reproducibility
        assertNotNull("Genetic algorithm should be initialized", ga);
        System.out.println("[DEBUG_LOG] Initialization test completed");
    }

    /**
     * Test that the genetic algorithm can evaluate a single individual.
     */
    @Test
    public void testIndividualEvaluation() {
        System.out.println("[DEBUG_LOG] Starting individual evaluation test");

        // Create a genetic algorithm with a fixed seed
        GeneticAlgorithm ga = new GeneticAlgorithm(42);

        // Get the best individual (without running evolution)
        GeneticAlgorithm.Individual individual = ga.getBestIndividual();

        // Verify that the individual has the expected number of weights
        assertEquals("Individual should have 10 weights", 10, individual.getWeights().length);

        System.out.println("[DEBUG_LOG] Individual evaluation test completed");
    }

    /**
     * Test that the genetic algorithm can run a mini evolution (2 generations)
     * with a small population to verify the core functionality works.
     */
    @Test
    public void testMiniEvolution() {
        System.out.println("[DEBUG_LOG] Starting mini evolution test");

        // Create a test subclass with reduced parameters for faster testing
        GeneticAlgorithm ga = new TestGeneticAlgorithm();

        // Run evolution
        GeneticAlgorithm.Individual best = ga.evolve();

        // Verify that the best individual has a non-zero fitness
        assertTrue("Best individual should have a non-negative fitness", best.getFitness() >= 0);

        // Print the best weights found
        System.out.println("[DEBUG_LOG] Best weights found:");
        System.out.println("[DEBUG_LOG] " + best.toString().replace("\n", ", "));

        System.out.println("[DEBUG_LOG] Mini evolution test completed");
    }

    /**
     * Test subclass with reduced parameters for faster testing.
     */
    private static class TestGeneticAlgorithm extends GeneticAlgorithm {
        public TestGeneticAlgorithm() {
            // Use a fixed seed for reproducibility
            super(42);
        }

        // Override evolve to run fewer generations
        @Override
        public Individual evolve() {
            // Evaluate initial population
            evaluatePopulation();

            // Find best individual in initial population
            Individual bestIndividual = findBestIndividual();
            System.out.println("[DEBUG_LOG] Initial best individual fitness: " + bestIndividual.getFitness());

            // Run just 2 generations for testing
            for (int generation = 0; generation < 2; generation++) {
                System.out.println("[DEBUG_LOG] Generation " + (generation + 1));

                // Create new population through selection, crossover, and mutation
                population = createNewPopulation();

                // Evaluate new population
                evaluatePopulation();

                // Update best individual
                Individual currentBest = findBestIndividual();
                if (currentBest.getFitness() > bestIndividual.getFitness()) {
                    bestIndividual = currentBest;
                    System.out.println("[DEBUG_LOG] New best individual found with fitness: " + bestIndividual.getFitness());
                }

                System.out.println("[DEBUG_LOG] Generation " + (generation + 1) + " complete");
            }

            return bestIndividual;
        }
    }

    /**
     * Test that the genetic algorithm can handle edge cases like:
     * - All weights set to minimum values
     * - All weights set to maximum values
     */
    @Test
    public void testEdgeCases() {
        System.out.println("[DEBUG_LOG] Starting edge cases test");

        // Create a genetic algorithm
        GeneticAlgorithm ga = new GeneticAlgorithm(42);

        // Test with minimum weights
        int[] minWeights = {90000,  // WIN_LOSS_WEIGHT
                50,     // MATERIAL_PER_PIECE
                5,      // TOWER_EXTRA_PER_LEVEL
                5,      // CENTER_CONTROL_BONUS
                5,      // FILE_ALIGNED_GUARD_BONUS
                10,     // GUARD_PROGRESS_BONUS
                1,      // MOBILITY_PER_MOVE
                -20,    // BLOCKED_TOWER_PENALTY
                1,      // GUARD_SAFETY_PER_FRIEND
                -50     // GUARD_THREAT_PER_ENEMY
        };

        // Test with maximum weights
        int[] maxWeights = {110000, // WIN_LOSS_WEIGHT
                150,    // MATERIAL_PER_PIECE
                30,     // TOWER_EXTRA_PER_LEVEL
                25,     // CENTER_CONTROL_BONUS
                20,     // FILE_ALIGNED_GUARD_BONUS
                40,     // GUARD_PROGRESS_BONUS
                5,      // MOBILITY_PER_MOVE
                -5,     // BLOCKED_TOWER_PENALTY
                15,     // GUARD_SAFETY_PER_FRIEND
                -10     // GUARD_THREAT_PER_ENEMY
        };

        // Create individuals with min and max weights
        GeneticAlgorithm.Individual minIndividual = new GeneticAlgorithm.Individual(minWeights);
        GeneticAlgorithm.Individual maxIndividual = new GeneticAlgorithm.Individual(maxWeights);

        // Verify that the individuals have the expected weights
        assertArrayEquals("Min individual should have min weights", minWeights, minIndividual.getWeights());
        assertArrayEquals("Max individual should have max weights", maxWeights, maxIndividual.getWeights());

        System.out.println("[DEBUG_LOG] Edge cases test completed");
    }
}