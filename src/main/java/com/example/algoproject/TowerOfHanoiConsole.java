package com.example.algoproject;

import java.util.*;

public class TowerOfHanoiConsole {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Tower of Hanoi with Four Pegs");
        System.out.println("-----------------------------");

        // Get number of disks
        System.out.print("Enter number of disks (3-10): ");
        int numDisks = scanner.nextInt();
        if (numDisks < 3 || numDisks > 10) {
            System.out.println("Invalid number of disks. Using default (8).");
            numDisks = 8;
        }

        // Menu for algorithm selection
        System.out.println("\nSelect algorithm:");
        System.out.println("1. Frame-Stewart Algorithm");
        System.out.println("2. Dynamic Programming");
        System.out.println("3. Simple Divide & Conquer");
        System.out.print("Enter your choice (1-3): ");

        int choice = scanner.nextInt();
        scanner.close();


        List<Stack<Integer>> pegs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            pegs.add(new Stack<>());
        }


        for (int i = numDisks - 1; i >= 0; i--) {
            pegs.get(0).push(i);
        }

        System.out.println("\nInitial state: All disks on Peg 1");


        List<Move> moves = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        switch (choice) {
            case 1:
                System.out.println("\nSolving with Frame-Stewart Algorithm...");
                moves = solveFrameStewart(numDisks, 0, 3, 1, 2);
                break;
            case 2:
                System.out.println("\nSolving with Dynamic Programming...");
                moves = solveDynamicProgramming(numDisks);
                break;
            case 3:
                System.out.println("\nSolving with Simple Divide & Conquer...");
                moves = solveSimpleDivideAndConquer(numDisks, 0, 3, 1, 2);
                break;
            default:
                System.out.println("Invalid choice. Using Frame-Stewart Algorithm.");
                moves = solveFrameStewart(numDisks, 0, 3, 1, 2);
        }

        long endTime = System.currentTimeMillis();
        double computationTime = (endTime - startTime) / 1000.0;

        System.out.println("\nSolution found!");
        System.out.println("Total moves: " + moves.size());
        System.out.println("Computation time: " + computationTime + " seconds");


        System.out.println("\nExecuting solution (showing moves only):");
        executeAndPrintMoves(pegs, moves);


        System.out.println("\nComplexity Analysis:");
        printComplexityAnalysis(choice, numDisks);
    }

    private static void executeAndPrintMoves(List<Stack<Integer>> pegs, List<Move> moves) {
        int moveNum = 1;
        for (Move move : moves) {

            int disk = pegs.get(move.fromPeg).pop();
            pegs.get(move.toPeg).push(disk);


            System.out.println("Move " + moveNum + ": Disk " + (disk + 1) +
                    " from Peg " + (move.fromPeg + 1) +
                    " to Peg " + (move.toPeg + 1));
            moveNum++;
        }
    }

    private static void printComplexityAnalysis(int algorithm, int n) {
        System.out.println("For n = " + n + " disks:");

        switch (algorithm) {
            case 1:
                System.out.println("Frame-Stewart Algorithm:");
                System.out.println("- Time Complexity: O(2^√(2n)) ≈ O(" + String.format("%.2f", Math.pow(2, Math.sqrt(2*n))) + ")");
                System.out.println("- Space Complexity: O(2^√(2n))");
                System.out.println("- This is much better than the standard O(2^n) = O(" + (1<<n) + ") solution");
                break;
            case 2:
                System.out.println("Dynamic Programming Approach:");
                System.out.println("- Time Complexity for building DP table: O(n²) = O(" + (n*n) + ")");
                System.out.println("- Total Time Complexity: O(2^√(2n)) ≈ O(" + String.format("%.2f", Math.pow(2, Math.sqrt(2*n))) + ")");
                System.out.println("- Space Complexity: O(2^√(2n))");
                break;
            case 3:
                System.out.println("Simple Divide & Conquer:");
                System.out.println("- Time Complexity: O(2^n) = O(" + (1<<n) + ")");
                System.out.println("- Space Complexity: O(2^n)");
                System.out.println("- This algorithm doesn't fully utilize the 4th peg");
                break;
        }
    }


    private static List<Move> solveFrameStewart(int n, int source, int target, int auxiliary1, int auxiliary2) {
        List<Move> movesList = new ArrayList<>();

        if (n == 0) return movesList;
        if (n == 1) {
            movesList.add(new Move(source, target, n-1));
            return movesList;
        }

        int k = findOptimalK(n);

        System.out.println("  For n=" + n + ", using k=" + k + " (splitting into " + (n-k) + " and " + k + " disks)");


        System.out.println("  Step 1: Moving top " + (n-k) + " disks from peg " + (source+1) + " to peg " + (auxiliary1+1));
        movesList.addAll(solveFrameStewart(n-k, source, auxiliary1, auxiliary2, target));


        System.out.println("  Step 2: Moving bottom " + k + " disks from peg " + (source+1) + " to peg " + (target+1) + " using 3-peg algorithm");
        movesList.addAll(solveTowerOfHanoi(k, source, target, auxiliary2));


        System.out.println("  Step 3: Moving " + (n-k) + " disks from peg " + (auxiliary1+1) + " to peg " + (target+1));
        movesList.addAll(solveFrameStewart(n-k, auxiliary1, target, source, auxiliary2));

        return movesList;
    }

    private static List<Move> solveTowerOfHanoi(int n, int source, int target, int auxiliary) {
        List<Move> movesList = new ArrayList<>();

        if (n == 1) {
            movesList.add(new Move(source, target, n-1));
            return movesList;
        }

        movesList.addAll(solveTowerOfHanoi(n-1, source, auxiliary, target));
        movesList.add(new Move(source, target, n-1));
        movesList.addAll(solveTowerOfHanoi(n-1, auxiliary, target, source));

        return movesList;
    }

    private static List<Move> solveDynamicProgramming(int n) {

        int[] dp = new int[n + 1];
        int[] kValues = new int[n + 1];

        dp[0] = 0;
        dp[1] = 1;

        System.out.println("Building DP table:");
        System.out.println("  dp[0] = 0 (base case)");
        System.out.println("  dp[1] = 1 (base case)");

        for (int i = 2; i <= n; i++) {

            dp[i] = 2 * dp[i - 1] + 1;
            kValues[i] = 0;

            System.out.print("  dp[" + i + "] initial (standard 3-peg): " + dp[i] + " moves");


            for (int k = 1; k < i; k++) {
                int moves = 2 * dp[i - k] + (1 << k) - 1;
                if (moves < dp[i]) {
                    dp[i] = moves;
                    kValues[i] = k;
                    System.out.print(" → with k=" + k + ": " + dp[i] + " moves");
                }
            }
            System.out.println("\n  Final dp[" + i + "] = " + dp[i] + " moves with optimal k = " + kValues[i]);
        }

        System.out.println("DP table built, optimal number of moves for n=" + n + ": " + dp[n]);
        System.out.println("Generating move sequence with optimal k values...");


        return generateMoves(n, 0, 3, 1, 2, dp, kValues);
    }

    private static List<Move> solveSimpleDivideAndConquer(int n, int source, int target, int aux1, int aux2) {
        List<Move> movesList = new ArrayList<>();

        if (n == 0) return movesList;
        if (n == 1) {
            movesList.add(new Move(source, target, n-1));
            return movesList;
        }

        System.out.println("  Dividing problem: Moving " + n + " disks from peg " + (source+1) + " to peg " + (target+1));


        System.out.println("  Step 1: Moving " + (n-1) + " disks from peg " + (source+1) + " to peg " + (aux1+1));
        movesList.addAll(solveSimpleDivideAndConquer(n-1, source, aux1, aux2, target));


        System.out.println("  Step 2: Moving disk " + n + " from peg " + (source+1) + " to peg " + (target+1));
        movesList.add(new Move(source, target, n-1));


        System.out.println("  Step 3: Moving " + (n-1) + " disks from peg " + (aux1+1) + " to peg " + (target+1));
        movesList.addAll(solveSimpleDivideAndConquer(n-1, aux1, target, source, aux2));

        return movesList;
    }

    private static List<Move> generateMoves(int n, int source, int target, int aux1, int aux2, int[] dp, int[] kValues) {
        List<Move> movesList = new ArrayList<>();

        if (n == 0) return movesList;
        if (n == 1) {
            movesList.add(new Move(source, target, n-1));
            return movesList;
        }

        int k = kValues[n];

        if (k == 0) {

            System.out.println("  Using standard 3-peg algorithm for " + n + " disks");
            movesList.addAll(solveTowerOfHanoi(n, source, target, aux1));
        } else {

            System.out.println("  For n=" + n + ", using optimal k=" + k);
            System.out.println("  Step 1: Moving top " + (n-k) + " disks from peg " + (source+1) + " to peg " + (aux1+1));
            movesList.addAll(generateMoves(n-k, source, aux1, aux2, target, dp, kValues));

            System.out.println("  Step 2: Moving bottom " + k + " disks from peg " + (source+1) + " to peg " + (target+1));
            movesList.addAll(solveTowerOfHanoi(k, source, target, aux2));

            System.out.println("  Step 3: Moving " + (n-k) + " disks from peg " + (aux1+1) + " to peg " + (target+1));
            movesList.addAll(generateMoves(n-k, aux1, target, source, aux2, dp, kValues));
        }

        return movesList;
    }

    private static int findOptimalK(int n) {
        if (n == 8) return 3;
        if (n <= 3) return 1;  // For small n, k=1 is optimal
        return (int)Math.sqrt(2*n);  // Approximation for general case
    }

    private static class Move {
        final int fromPeg;
        final int toPeg;
        final int diskIndex;  // 0 is the smallest disk

        Move(int fromPeg, int toPeg, int diskIndex) {
            this.fromPeg = fromPeg;
            this.toPeg = toPeg;
            this.diskIndex = diskIndex;
        }
    }
}