import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Arrays;


public class Main {
    public static void main(String[] args) {
        Set<Integer> states = new HashSet<>();
        states.add(1);
        states.add(2);
        states.add(3);
        states.add(4);
        int numStates = states.size();
        Set<Integer> actions = new HashSet<>();
        actions.add(1);
        actions.add(2);
        int numActions = actions.size();
        System.out.println(states);
        System.out.println(actions);
        int numNewStates = numStates;

        // Just a tensor of transition probabilities
        double[][][] P = new double[numStates][numActions][numNewStates];
        System.out.println(Arrays.deepToString(P));

        // ---------------------------------------------------------------------
        // Populate transition probabilities: P[s][a][s_next]
        // ---------------------------------------------------------------------

        // --- State 1 (Index 0) ---
        // Action 1 (Index 0): 80% stay at State 1, 20% move to State 2
        P[0][0][0] = 0.8;
        P[0][0][1] = 0.2;
        // Action 2 (Index 1): Deterministic jump to State 2
        P[0][1][1] = 1.0;

        // --- State 2 (Index 1) ---
        // Action 1 (Index 0): 10% back to State 1, 70% stay at State 2, 20% forward to State 3
        P[1][0][0] = 0.1;
        P[1][0][1] = 0.7;
        P[1][0][2] = 0.2;
        // Action 2 (Index 1): 90% jump to State 3, 10% stay at State 2
        P[1][1][1] = 0.1;
        P[1][1][2] = 0.9;

        // --- State 3 (Index 2) ---
        // Action 1 (Index 0): 15% to State 2, 70% stay at State 3, 15% to State 4
        P[2][0][1] = 0.15;
        P[2][0][2] = 0.70;
        P[2][0][3] = 0.15;
        // Action 2 (Index 1): Deterministic jump to State 4
        P[2][1][3] = 1.0;

        // --- State 4 (Index 3) ---
        // Action 1 (Index 0): Absorbing / reset - 100% stay at State 4
        P[3][0][3] = 1.0;
        // Action 2 (Index 1): Reset action - 100% return to State 1
        P[3][1][0] = 1.0;

        System.out.println(Arrays.deepToString(P)); // Note that these must sum to 1


    // ---------------------------------------------------------------------
        // Reward tensor: R[s][a][s_next] (Same dimensions: 4 x 2 x 4)
        // ---------------------------------------------------------------------
        double[][][] R = new double[numStates][numActions][numNewStates];
        Random rand = new Random(42); // Seeded for reproducibility

        double minReward = -5.0;
        double maxReward = 10.0;

        for (int s = 0; s < numStates; s++) {
            for (int a = 0; a < numActions; a++) {
                for (int sNext = 0; sNext < numNewStates; sNext++) {
                    // Populate random values in range [minReward, maxReward)
                    // Note: Transitions with P[s][a][sNext] == 0 will get filtered
                    // out naturally during expected reward calculations.
                    R[s][a][sNext] = Math.round((minReward + (maxReward - minReward) * rand.nextDouble()) * 100.0) / 100.0;
                }
            }
        }        
        
        System.out.println(Arrays.deepToString(R)); // Note that these must sum to 1

        // =====================================================================
        // TIME HORIZON & SALVAGE BASE CASE (WEEKLY EPOCHS)
        // =====================================================================

        // Horizon: 4 weeks (t = 0, 1, 2, 3 are decision epochs; t = 4 is terminal)
        int T = 4;

        // Weekly discount factor (5% annual rate -> ~0.999 weekly, or 1.0 if undiscounted)
        double gamma = 0.999;

        // Value function table: V[epoch_in_weeks][state_index]
        double[][] V = new double[T + 1][numStates];
        int[][] policy = new int[T][numStates];

        // --- Base Case: Salvage Reward Vector R_T(s) at Week T ---
        // Higher states represent degradation or different asset values:
        double[] salvageRewards = new double[numStates];
        salvageRewards[0] = 50.0;  // State 1: high salvage / liquidation value
        salvageRewards[1] = 30.0;  // State 2: moderate value
        salvageRewards[2] = 10.0;  // State 3: low value
        salvageRewards[3] = 0.0;   // State 4: zero salvage value (scrap)

        // Initialize the base case V_T
        for (int s = 0; s < numStates; s++) {
            V[T][s] = salvageRewards[s];
        }

        // ---------------------------------------------------------------------
        // BACKWARDS INDUCTION LOOP (Stepping back week by week)
        // ---------------------------------------------------------------------
        for (int t = T - 1; t >= 0; t--) {
            for (int s = 0; s < numStates; s++) {
                double maxExpectedValue = Double.NEGATIVE_INFINITY;
                int bestAction = -1;

                for (int a = 0; a < numActions; a++) {
                    double expectedQ = 0.0;

                    for (int sNext = 0; sNext < numNewStates; sNext++) {
                        double prob = P[s][a][sNext];
                        if (prob > 0.0) {
                            // Weekly transition reward + discounted terminal/downstream value
                            expectedQ += prob * (R[s][a][sNext] + gamma * V[t + 1][sNext]);
                        }
                    }

                    if (expectedQ > maxExpectedValue) {
                        maxExpectedValue = expectedQ;
                        bestAction = a + 1; // Store 1-based action (1 or 2)
                    }
                }
                V[t][s] = maxExpectedValue;
                policy[t][s] = bestAction;
            }
        }
        System.out.println(Arrays.deepToString(V));
        System.out.println(Arrays.deepToString(policy));

    }

}