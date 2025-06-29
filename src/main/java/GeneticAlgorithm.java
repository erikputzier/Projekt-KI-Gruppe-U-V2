import java.util.*;

/**
 * Genetic Algorithm implementation for optimizing evaluation function weights
 * through self-play. This class evolves a population of weight configurations
 * to find the best set of weights for the evaluation function.
 */
public class GeneticAlgorithm {
    // Constants for genetic algorithm parameters
    private static final int POPULATION_SIZE = 40;
    private static final int TOURNAMENT_SIZE = 4;
    private static final double MUTATION_RATE = 0.2;
    private static final double CROSSOVER_RATE = 0.7;
    private static final int ELITISM_COUNT = 2;
    private static final int MAX_GENERATIONS = 30;
    private static final int GAMES_PER_EVALUATION = 10;
    private static final int MOVE_LIMIT = 150;
    private static final long GAME_TIME_LIMIT_MS = 50000;

    // Weight ranges for mutation and initialization
    private static final int[] MIN_WEIGHTS = {10000,  // WIN_LOSS_WEIGHT
            10,     // MATERIAL_PER_PIECE
            3,      // TOWER_EXTRA_PER_LEVEL
            3,      // CENTER_CONTROL_BONUS
            3,      // FILE_ALIGNED_GUARD_BONUS
            5,     // GUARD_PROGRESS_BONUS
            1,      // MOBILITY_PER_MOVE
            -40,    // BLOCKED_TOWER_PENALTY
            1,      // GUARD_SAFETY_PER_FRIEND
            -100     // GUARD_THREAT_PER_ENEMY
    };

    private static final int[] MAX_WEIGHTS = {200000, // WIN_LOSS_WEIGHT
            500,    // MATERIAL_PER_PIECE
            90,     // TOWER_EXTRA_PER_LEVEL
            85,     // CENTER_CONTROL_BONUS
            80,     // FILE_ALIGNED_GUARD_BONUS
            100,     // GUARD_PROGRESS_BONUS
            20,      // MOBILITY_PER_MOVE
            -2,     // BLOCKED_TOWER_PENALTY
            50,     // GUARD_SAFETY_PER_FRIEND
            -2     // GUARD_THREAT_PER_ENEMY
    };

    private static final String[] WEIGHT_NAMES = {"WIN_LOSS_WEIGHT", "MATERIAL_PER_PIECE", "TOWER_EXTRA_PER_LEVEL", "CENTER_CONTROL_BONUS", "FILE_ALIGNED_GUARD_BONUS", "GUARD_PROGRESS_BONUS", "MOBILITY_PER_MOVE", "BLOCKED_TOWER_PENALTY", "GUARD_SAFETY_PER_FRIEND", "GUARD_THREAT_PER_ENEMY"};

    protected List<Individual> population;
    protected Random random;
    protected Individual bestIndividual;
    protected CustomAI referenceAI; // Fixed reference AI for fitness evaluation

    /**
     * Represents an individual in the population with a set of weights
     * and a fitness score.
     */
    public static class Individual {
        private int[] weights;
        private int fitness;

        public Individual(int[] weights) {
            this.weights = weights.clone();
            this.fitness = 0;
        }

        public Individual() {
            this.weights = new int[WEIGHT_NAMES.length];
            this.fitness = 0;
        }

        public int[] getWeights() {
            return weights;
        }

        public int getFitness() {
            return fitness;
        }

        public void setFitness(int fitness) {
            this.fitness = fitness;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Fitness: ").append(fitness).append("\n");
            for (int i = 0; i < weights.length; i++) {
                sb.append(WEIGHT_NAMES[i]).append(": ").append(weights[i]).append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * Represents the result of a game between two individuals.
     */
    public enum GameResult {
        WIN, LOSS, DRAW
    }

    /**
     * Constructor initializes the genetic algorithm with a random population.
     */
    public GeneticAlgorithm() {
        this.random = new Random();
        this.population = new ArrayList<>();
        initializePopulation();
        this.bestIndividual = population.get(0); // Initialize with first individual

        // Initialize reference AI with hand-tuned weights
        int[] currentWeights = {100000, // WIN_LOSS_WEIGHT
                100,    // MATERIAL_PER_PIECE
                15,     // TOWER_EXTRA_PER_LEVEL
                12,     // CENTER_CONTROL_BONUS
                10,     // FILE_ALIGNED_GUARD_BONUS
                20,     // GUARD_PROGRESS_BONUS
                2,      // MOBILITY_PER_MOVE
                -10,    // BLOCKED_TOWER_PENALTY
                5,      // GUARD_SAFETY_PER_FRIEND
                -30     // GUARD_THREAT_PER_ENEMY
        };
        this.referenceAI = new CustomAI(currentWeights);
    }

    /**
     * Constructor with a specific seed for reproducibility.
     */
    public GeneticAlgorithm(long seed) {
        this.random = new Random(seed);
        this.population = new ArrayList<>();
        initializePopulation();
        this.bestIndividual = population.get(0); // Initialize with first individual

        // Initialize reference AI with hand-tuned weights
        int[] currentWeights = {100000, // WIN_LOSS_WEIGHT
                100,    // MATERIAL_PER_PIECE
                15,     // TOWER_EXTRA_PER_LEVEL
                12,     // CENTER_CONTROL_BONUS
                10,     // FILE_ALIGNED_GUARD_BONUS
                20,     // GUARD_PROGRESS_BONUS
                2,      // MOBILITY_PER_MOVE
                -10,    // BLOCKED_TOWER_PENALTY
                5,      // GUARD_SAFETY_PER_FRIEND
                -30     // GUARD_THREAT_PER_ENEMY
        };
        this.referenceAI = new CustomAI(currentWeights);
    }

    /**
     * Initialize the population with random individuals.
     */
    private void initializePopulation() {
        // Add current weights as one individual
        int[] currentWeights = {100000, // WIN_LOSS_WEIGHT
                100,    // MATERIAL_PER_PIECE
                15,     // TOWER_EXTRA_PER_LEVEL
                12,     // CENTER_CONTROL_BONUS
                10,     // FILE_ALIGNED_GUARD_BONUS
                20,     // GUARD_PROGRESS_BONUS
                2,      // MOBILITY_PER_MOVE
                -10,    // BLOCKED_TOWER_PENALTY
                5,      // GUARD_SAFETY_PER_FRIEND
                -30     // GUARD_THREAT_PER_ENEMY
        };
        population.add(new Individual(currentWeights));

        // Generate random individuals for the rest of the population
        for (int i = 1; i < POPULATION_SIZE; i++) {
            Individual individual = new Individual();
            for (int j = 0; j < WEIGHT_NAMES.length; j++) {
                individual.weights[j] = random.nextInt(MAX_WEIGHTS[j] - MIN_WEIGHTS[j] + 1) + MIN_WEIGHTS[j];
            }
            population.add(individual);
        }
    }

    /**
     * Run the genetic algorithm for a specified number of generations.
     *
     * @return The best individual found.
     */
    public Individual evolve() {
        // Evaluate initial population
        evaluatePopulation();

        // Find best individual in initial population
        bestIndividual = findBestIndividual();
        System.out.println("Initial best individual: " + bestIndividual);

        // Evolution loop
        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            System.out.println("Generation " + (generation + 1));

            // Create new population
            population = createNewPopulation();

            // Evaluate new population
            evaluatePopulation();

            // Update best individual
            Individual currentBest = findBestIndividual();
            if (currentBest.getFitness() > bestIndividual.getFitness()) {
                bestIndividual = currentBest;
                System.out.println("New best individual found: " + bestIndividual);
            }

            System.out.println("Generation " + (generation + 1) + " complete");
        }

        return bestIndividual;
    }

    /**
     * Evaluate the fitness of all individuals in the population.
     */
    protected void evaluatePopulation() {
        // Use parallel streams to evaluate individuals concurrently
        population.parallelStream().forEach(this::evaluateFitness);
    }

    /**
     * Evaluate the fitness of an individual by playing games against a fixed reference AI.
     * This provides a stable baseline for fitness evaluation, preventing drift and cycles.
     *
     * @param individual The individual to evaluate.
     */
    private void evaluateFitness(Individual individual) {
        int wins = 0;
        int losses = 0;
        int draws = 0;

        // Create an AI for the individual being evaluated
        CustomAI individualAI = new CustomAI(individual.getWeights());

        for (int i = 0; i < GAMES_PER_EVALUATION; i++) {
            // Play game with individual as RED (first player) against reference AI
            Board board = new Board();
            int moveCount = 0;
            long startTime = System.currentTimeMillis();

            while (moveCount < MOVE_LIMIT && (System.currentTimeMillis() - startTime) < GAME_TIME_LIMIT_MS) {

                MovePair move;
                if (board.getCurrentPlayer() == Player.RED) {
                    move = individualAI.getBestMove(board);
                } else {
                    move = referenceAI.getBestMove(board);
                }

                board = Board.makeMove(move, board);
                moveCount++;

                if (Board.checkplayerWon(board, Player.RED)) {
                    wins++;
                    break;
                } else if (Board.checkplayerWon(board, Player.BLUE)) {
                    losses++;
                    break;
                }
            }

            // If no winner after move limit, determine result based on evaluation
            if (moveCount >= MOVE_LIMIT || (System.currentTimeMillis() - startTime) >= GAME_TIME_LIMIT_MS) {
                // Use individual's weights for evaluation
                int evaluation = evaluateWithWeights(board, individual.getWeights());
                if (evaluation > 0) {
                    wins++;
                } else if (evaluation < 0) {
                    losses++;
                } else {
                    draws++;
                }
            }

            // Play game with individual as BLUE (second player) against reference AI
            board = new Board();
            moveCount = 0;
            startTime = System.currentTimeMillis();

            while (moveCount < MOVE_LIMIT && (System.currentTimeMillis() - startTime) < GAME_TIME_LIMIT_MS) {

                MovePair move;
                if (board.getCurrentPlayer() == Player.RED) {
                    move = referenceAI.getBestMove(board);
                } else {
                    move = individualAI.getBestMove(board);
                }

                board = Board.makeMove(move, board);
                moveCount++;

                if (Board.checkplayerWon(board, Player.RED)) {
                    losses++;
                    break;
                } else if (Board.checkplayerWon(board, Player.BLUE)) {
                    wins++;
                    break;
                }
            }

            // If no winner after move limit, determine result based on evaluation
            if (moveCount >= MOVE_LIMIT || (System.currentTimeMillis() - startTime) >= GAME_TIME_LIMIT_MS) {
                // Use individual's weights for evaluation
                int evaluation = evaluateWithWeights(board, individual.getWeights());
                if (evaluation > 0) {
                    losses++;
                } else if (evaluation < 0) {
                    wins++;
                } else {
                    draws++;
                }
            }
        }

        // Calculate fitness: 3 points for a win, 1 point for a draw
        individual.setFitness(wins * 3 + draws);
    }

    /**
     * Play a game between two individuals with custom evaluation weights using full AI search.
     * Note: This method is no longer used in the current implementation, which uses a fixed
     * reference AI for fitness evaluation. It's kept for reference purposes.
     *
     * @param redPlayer  Individual playing as RED
     * @param bluePlayer Individual playing as BLUE
     * @return The result of the game from redPlayer's perspective
     */
    private GameResult playGame(Individual redPlayer, Individual bluePlayer) {
        // Create a new board with the initial position
        Board board = new Board();

        // Create custom AI instances for each player
        CustomAI redAI = new CustomAI(redPlayer.getWeights());
        CustomAI blueAI = new CustomAI(bluePlayer.getWeights());

        // Play the game until a player wins or move limit is reached
        int moveCount = 0;
        long startTime = System.currentTimeMillis();

        while (moveCount < MOVE_LIMIT && (System.currentTimeMillis() - startTime) < GAME_TIME_LIMIT_MS) {

            // Get the best move using the appropriate AI
            MovePair move;
            if (board.getCurrentPlayer() == Player.RED) {
                move = redAI.getBestMove(board);
            } else {
                move = blueAI.getBestMove(board);
            }

            // Apply move
            board = Board.makeMove(move, board);
            moveCount++;

            // Check for win conditions
            if (Board.checkplayerWon(board, Player.RED)) {
                return GameResult.WIN;
            } else if (Board.checkplayerWon(board, Player.BLUE)) {
                return GameResult.LOSS;
            }
        }

        // If no winner after move limit, determine winner based on evaluation using the current player's weights
        int[] myWeights = (board.getCurrentPlayer() == Player.RED) ? redPlayer.getWeights() : bluePlayer.getWeights();
        int evaluation = evaluateWithWeights(board, myWeights);
        if (evaluation > 0) {
            return GameResult.WIN;
        } else if (evaluation < 0) {
            return GameResult.LOSS;
        } else {
            return GameResult.DRAW;
        }
    }

    /**
     * Custom AI class that uses custom weights for evaluation.
     */
    private class CustomAI {
        private int[] weights;

        public CustomAI(int[] weights) {
            this.weights = weights;
        }

        /**
         * Get the best move for the current board position using minimax search.
         *
         * @param board The current board position
         * @return The best move
         */
        public MovePair getBestMove(Board board) {
            // Use GeneticAlgorithm's getBestMove method which uses custom weights
            return GeneticAlgorithm.this.getBestMove(board, weights);
        }
    }

    /**
     * Get the best move for the current board position using custom weights.
     * This is a simplified version of AI.pickMove that uses custom weights.
     *
     * @param board   The current board position
     * @param weights The weights to use for evaluation
     * @return The best move
     */
    private MovePair getBestMove(Board board, int[] weights) {
        List<MovePair> legalMoves = MoveGenerator.generateAllLegalMoves(board);

        // If only one legal move, return it
        if (legalMoves.size() == 1) {
            return legalMoves.get(0);
        }

        boolean maximizingPlayer = board.getCurrentPlayer() == Player.RED;
        MovePair bestMove = null;
        int bestValue = maximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        // Evaluate each move
        for (MovePair move : legalMoves) {
            Board newBoard = Board.makeMove(move, board.copy());
            int eval = evaluateWithWeights(newBoard, weights);

            if (maximizingPlayer && eval > bestValue) {
                bestValue = eval;
                bestMove = move;
            } else if (!maximizingPlayer && eval < bestValue) {
                bestValue = eval;
                bestMove = move;
            }
        }

        return bestMove;
    }

    /**
     * Evaluate a board position using custom weights.
     *
     * @param board   The board to evaluate
     * @param weights The weights to use
     * @return The evaluation score
     */
    private int evaluateWithWeights(Board board, int[] weights) {
        int redScore = evaluateSideWithWeights(board, Player.RED, weights);
        int blueScore = evaluateSideWithWeights(board, Player.BLUE, weights);
        return redScore - blueScore;
    }

    /**
     * Evaluate a side using custom weights.
     *
     * @param board   The board to evaluate
     * @param side    The side to evaluate
     * @param weights The weights to use
     * @return The evaluation score for the side
     */
    private int evaluateSideWithWeights(Board board, Player side, int[] weights) {
        int score = 0;

        // Win/Loss detection
        if (Board.checkplayerWon(board, side)) return weights[0]; // WIN_LOSS_WEIGHT
        if (Board.checkplayerWon(board, Eval.opposite(side))) return -weights[0];

        // Material
        int pieces = board.numPieces(side);
        score += pieces * weights[1]; // MATERIAL_PER_PIECE

        // Tower height
        score += Eval.totalExtraTowerLevels(board, side) * weights[2]; // TOWER_EXTRA_PER_LEVEL

        // Center control
        score += Eval.countInCenter(board, side) * weights[3]; // CENTER_CONTROL_BONUS

        // Aligned attack
        score += Eval.alignedWithEnemyGuard(board, side) * weights[4]; // FILE_ALIGNED_GUARD_BONUS

        // Guard progress
        score += (12 - Eval.guardDistanceToTarget(board, side)) * weights[5]; // GUARD_PROGRESS_BONUS

        // Mobility (if enabled)
        // score += MoveGenerator.generateAllLegalMoves(board).size() * weights[6]; // MOBILITY_PER_MOVE

        // Blocked towers
        score += Eval.countBlockedTowers(board, side) * weights[7]; // BLOCKED_TOWER_PENALTY

        // Guard safety
        score += Eval.friendsNearGuard(board, side) * weights[8]; // GUARD_SAFETY_PER_FRIEND

        // Guard threat
        score += Eval.enemiesNearOurGuard(board, side) * weights[9]; // GUARD_THREAT_PER_ENEMY

        return score;
    }

    /**
     * Tournament selection to choose a parent.
     *
     * @return The selected individual
     */
    private Individual tournamentSelection() {
        List<Individual> tournament = new ArrayList<>();
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            int randomIndex = random.nextInt(population.size());
            tournament.add(population.get(randomIndex));
        }

        return tournament.stream().max(Comparator.comparingInt(Individual::getFitness)).orElse(population.get(0));
    }

    /**
     * Perform crossover between two parents to create two offspring.
     *
     * @param parent1 First parent
     * @param parent2 Second parent
     * @return Array of two offspring
     */
    private Individual[] crossover(Individual parent1, Individual parent2) {
        Individual[] offspring = new Individual[2];
        offspring[0] = new Individual();
        offspring[1] = new Individual();

        // Single-point crossover
        int crossoverPoint = random.nextInt(WEIGHT_NAMES.length);

        for (int i = 0; i < WEIGHT_NAMES.length; i++) {
            if (i < crossoverPoint) {
                offspring[0].weights[i] = parent1.weights[i];
                offspring[1].weights[i] = parent2.weights[i];
            } else {
                offspring[0].weights[i] = parent2.weights[i];
                offspring[1].weights[i] = parent1.weights[i];
            }
        }

        return offspring;
    }

    /**
     * Mutate an individual by adding small Gaussian noise to some of its weights.
     * This uses perturbative mutation which preserves prior tuning by nudging
     * the genome instead of randomizing it.
     *
     * @param individual The individual to mutate
     */
    private void mutate(Individual individual) {
        for (int i = 0; i < WEIGHT_NAMES.length; i++) {
            if (random.nextDouble() < MUTATION_RATE) {
                // Calculate sigma as a percentage of the weight's absolute value
                // Using 5% of the weight's value as the standard deviation
                double sigma = 0.05 * Math.abs(individual.weights[i]);
                // Ensure sigma is at least 1 to allow some mutation even for small weights
                sigma = Math.max(1.0, sigma);

                // Apply Gaussian noise to the existing weight
                double delta = random.nextGaussian() * sigma;
                int mutated = (int) Math.round(individual.weights[i] + delta);

                // Clamp the result to ensure it stays within the allowed range
                individual.weights[i] = Math.max(MIN_WEIGHTS[i], Math.min(MAX_WEIGHTS[i], mutated));
            }
        }
    }

    /**
     * Find the best individual in the current population.
     *
     * @return The best individual
     */
    protected Individual findBestIndividual() {
        return population.stream().max(Comparator.comparingInt(Individual::getFitness)).orElse(population.get(0));
    }

    /**
     * Create a new population through selection, crossover, and mutation.
     *
     * @return The new population
     */
    protected List<Individual> createNewPopulation() {
        List<Individual> newPopulation = new ArrayList<>();

        // Elitism - add best individuals directly to new population
        List<Individual> sortedPopulation = population.stream().sorted(Comparator.comparingInt(Individual::getFitness).reversed()).toList();

        for (int i = 0; i < ELITISM_COUNT; i++) {
            newPopulation.add(sortedPopulation.get(i));
        }

        // Fill the rest of the new population with offspring
        while (newPopulation.size() < POPULATION_SIZE) {
            // Select parents
            Individual parent1 = tournamentSelection();
            Individual parent2 = tournamentSelection();

            // Crossover
            Individual[] offspring;
            if (random.nextDouble() < CROSSOVER_RATE) {
                offspring = crossover(parent1, parent2);
            } else {
                offspring = new Individual[]{new Individual(parent1.getWeights()), new Individual(parent2.getWeights())};
            }

            // Mutation
            mutate(offspring[0]);
            mutate(offspring[1]);

            // Add to new population
            newPopulation.add(offspring[0]);
            if (newPopulation.size() < POPULATION_SIZE) {
                newPopulation.add(offspring[1]);
            }
        }

        return newPopulation;
    }

    /**
     * Get the best individual found during evolution.
     *
     * @return The best individual
     */
    public Individual getBestIndividual() {
        return bestIndividual;
    }
}