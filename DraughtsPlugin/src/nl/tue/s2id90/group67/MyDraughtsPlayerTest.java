package nl.tue.s2id90.group67;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import nl.tue.s2id90.draughts.DraughtsState;
import nl.tue.s2id90.draughts.player.DraughtsPlayer;
import nl.tue.s2id90.group67.AIStoppedException;
import org10x10.dam.game.Move;
import java.lang.Math.*;

/**
 * Implementation of the DraughtsPlayer interface.
 * @author huub
 */
// ToDo: rename this class (and hence this file) to have a distinct name
//       for your player during the tournament
public class MyDraughtsPlayerTest  extends DraughtsPlayer{
    private int bestValue=0;
    int maxSearchDepth;
    Move[][] listOfPreviousMoves;
    int[] alpha;
    int[] beta;
    int currentDepth;
    boolean whiteIsToMove;
    
    /** boolean that indicates that the GUI asked the player to stop thinking. */
    private boolean stopped;

    public MyDraughtsPlayerTest(int maxSearchDepth) {
        super("best.png"); // ToDo: replace with your own icon
        this.maxSearchDepth = maxSearchDepth;
    }
    
    @Override public Move getMove(DraughtsState s) {
        Move bestMove = null;
        bestValue = 0;
        maxSearchDepth = 20; // Make sure this is even!!
        DraughtsNode node = new DraughtsNode(s);    // the root of the search tree
        // Get the current state of the board
        DraughtsState state = node.getState();
        whiteIsToMove = state.isWhiteToMove();
        
        // Get the current moves for the state
        List<Move> firstMoves = state.getMoves();
        Collections.shuffle(firstMoves);
        // put a if statement for if no moves are found????
        
        // This list is used to remember the all the bestmoves for each move
        listOfPreviousMoves = new Move[firstMoves.size()][];

        // Initialize the lists of alphas, betas and PreviousMoves
        // and add the current moves to the listOfPreviousMoves
        alpha = new int[firstMoves.size()];
        beta = new int[firstMoves.size()];
        
        for(int i=0;i<firstMoves.size();i++){
            listOfPreviousMoves[i] = new Move[maxSearchDepth + 1];
            listOfPreviousMoves[i][0] = firstMoves.get(i);
            alpha[i] = Integer.MIN_VALUE;
            beta[i] = Integer.MAX_VALUE;
        }
        System.out.println("=================================="
            + "=== start move =====================================");
        try {           
            // Iterative Deepening on AlphaBeta 
            currentDepth = 1;
            while(currentDepth <= maxSearchDepth) {
                System.out.println("---------------------- " + currentDepth +  " ----------------------------");
                int index = 0;
                //List<List<Move>> copyOfListOfPreviousMoves = listOfPreviousMoves;
                for(Move[] moveList : listOfPreviousMoves) {
                    System.out.println("");
                    DraughtsNode newNode = new DraughtsNode(s); 
                    DraughtsState newState = null;
                    List<Move> reverseList = new ArrayList<Move>();
                    for(Move m : moveList) {
                        // If the next move exist
                        if(m != null) {
                            reverseList.add(0,m);
                            System.out.print(m + " ");
                            // Set the state of the board to after doing the next move in the list
                            newState = newNode.getState();
                            newState.doMove(m);
                            newNode = new DraughtsNode(newState);
                        // If there are no more moves
                        }else {
                            // compute bestMove and bestValue in a call to alphabeta
                            newState = newNode.getState();
                            alphaBeta(newState, index);
                            break;   
                        }
                    }
                    for(Move m : reverseList) {
                       newState.undoMove(m);
                    }
                    
                    index++;
                }
                currentDepth++;
                
                // for testing only, not neccessary in real run
                bestMove = calcBestMove();
            }
        } catch (AIStoppedException ex) {
            
            System.out.println("Stopped in getMove()");
        }
    
        bestMove = calcBestMove();
        System.out.println("=================================="
            + "=== ========== =====================================");
        if (bestMove==null) {
            System.err.println("no valid move found!");
            return getRandomValidMove(s);
        } else {
            return bestMove;
        }
    } 
    
    
    Move calcBestMove() {
        int indexForBestMove = 0;
        if (whiteIsToMove) {
            bestValue = alpha[0];
            for(int i = 1;i<listOfPreviousMoves.length;i++) {
                if(alpha[i] > bestValue) {
                    bestValue = alpha[i];
                    indexForBestMove = i;
                }
                if(alpha[i] == bestValue && Math.random() > 0.5) {
                    bestValue = alpha[i];
                    indexForBestMove = i;
                }
            }
        } else  {
            System.out.println("Black move");
            bestValue = beta[0];
            for(int i = 1;i<listOfPreviousMoves.length;i++) {
                if(beta[i] < bestValue) {
                    bestValue = beta[i];
                    indexForBestMove = i;
                }
                if(beta[i] == bestValue && Math.random() > 0.5) {
                    bestValue = beta[i];
                    indexForBestMove = i;
                }
            }
        }
        
        // print the results for debugging reasons
        //System.err.format(
        //"%s: depth= %2d, best move = %5s, value=%d\n", 
        //this.getClass().getSimpleName(),currentDepth - 1, 
        //listOfPreviousMoves[indexForBestMove][0], bestValue
        //);
        System.out.println(" best move " + 
                listOfPreviousMoves[indexForBestMove][0] + " best value " + bestValue);
        System.out.println("");
        for(Move m : listOfPreviousMoves[indexForBestMove]) {
            System.out.print(m + " ");
        }
        System.out.println("");
        
        return listOfPreviousMoves[indexForBestMove][0];
    }

    /** This method's return value is displayed in the AICompetition GUI.
     * 
     * @return the value for the draughts state s as it is computed in a call to getMove(s). 
     */
    @Override public Integer getValue() { 
       return bestValue;
    }

    /** Tries to make alphabeta search stop. Search should be implemented such that it
     * throws an AIStoppedException when boolean stopped is set to true;
    **/
    @Override public void stop() {
       stopped = true; 
    }
    
    /** returns random valid move in state s, or null if no moves exist. */
    Move getRandomValidMove(DraughtsState s) {
        List<Move> moves = s.getMoves();
        Collections.shuffle(moves);
        return moves.isEmpty()? null : moves.get(0);
    }
    
    /** Implementation of alphabeta that automatically chooses the white player
     *  as maximizing player and the black player as minimizing player.
     * @param node contains DraughtsState and has field to which the best move can be assigned.
     * @param alpha
     * @param beta
     * @param depth maximum recursion Depth
     * @return the computed value of this node
     * @throws AIStoppedException
     **/
    void alphaBeta(DraughtsState state, int index)
            throws AIStoppedException
    {
        if (state.isWhiteToMove()) {
            alphaBetaMax(state, index);
        } else  {
             alphaBetaMin(state, index);
        }
    }
    
    /** Does an alphabeta computation with the given alpha and beta
     * where the player that is to move in node is the minimizing player.
     * 
     * <p>Typical pieces of code used in this method are:
     *     <ul> <li><code>DraughtsState state = node.getState()</code>.</li>
     *          <li><code> state.doMove(move); .... ; state.undoMove(move);</code></li>
     *          <li><code>node.setBestMove(bestMove);</code></li>
     *          <li><code>if(stopped) { stopped=false; throw new AIStoppedException(); }</code></li>
     *     </ul>
     * </p>
     * @param node contains DraughtsState and has field to which the best move can be assigned.
     * @param alpha
     * @param beta
     * @param depth  maximum recursion Depth
     * @return the compute value of this node
     * @throws AIStoppedException thrown whenever the boolean stopped has been set to true.
     */
     void alphaBetaMax(DraughtsState state, int index)
            throws AIStoppedException {
        // Stop the search if a certain amount of time has passed
        if (stopped) { 
            stopped = false; 
            System.out.println("stopped in AlphaBetaMax");
            throw new AIStoppedException(); 
        }
        
        // Retreive the available moves for this state
        List<Move> moves = state.getMoves();
        
        // If no moves are possible return the evaluation of the current state
        if (moves.isEmpty()) {// Heb je hier niet gewoon verloren of gewonnen?
            
        }else{
            // Evaluate each possible move for the state of the board
            Move currentBestMove = moves.get(0);
            System.out.println();
            for (Move m : moves) { // Continue untill no new moves are found
                
                // Evaluate the the result of the next move
                int result = evaluateMove(state, m);
                System.out.print(" move: " + m + " result: " + result);
                
                // Check whether the next move is better then the previous ones
                if (result > alpha[index]) {
                    currentBestMove = m;
                    alpha[index] = result;
                }
                //.....
                if (beta[index] <= alpha[index]) {
                    currentBestMove = m;
                    alpha[index] = beta[index];
                    break;
                }
            }
            // Save the best move
            System.out.println(" best move: " + currentBestMove + " alpha: " + alpha[index]);
            listOfPreviousMoves[index][currentDepth] = currentBestMove;
        }
     }

    void alphaBetaMin(DraughtsState state, int index)
            throws AIStoppedException {
        if (stopped) { 
            stopped = false; 
            System.out.println("stopped in AlphaBetaMin");
            throw new AIStoppedException(); 
        }

        // Retreive the available moves for this state
        List<Move> moves = state.getMoves();
        
        // If no moves are possible return the evaluation of the current state
        if (moves.isEmpty()) {// Heb je hier niet gewoon verloren of gewonnen?
            //System.out.println("No moves found");
        }else{
            // Evaluate each possible move for the state of the board
            Move currentBestMove = moves.get(0);
            System.out.println();
            for (Move m : moves) { // Continue untill no new moves are found
                
                // Evaluate the the result of the next move
                int result = evaluateMove(state, m);
                System.out.print(" move: " + m + " result: " + result);
                
                // Check whether the next move is better then the previous ones
                if (result < beta[index]) {
                    currentBestMove = m;
                    beta[index] = result;
                }
                //.....
                if (beta[index] <= alpha[index]) {
                    currentBestMove = m;
                    beta[index] = alpha[index];
                    break;
                }
            }
            // Save the best move
            System.out.println(" best move: " + currentBestMove + " beta: " + beta[index]);
            listOfPreviousMoves[index][currentDepth] = currentBestMove;
        }
    }
    
      /** A method that evaluates the given state after a certain move. */
      int evaluateMove(DraughtsState state, Move m) {
        // Set the state of the board to after doing the next move in the list
        state.doMove(m);
        DraughtsNode newNode = new DraughtsNode(state);

        // Evaluate the current state
        int result = evaluate(newNode.getState());

        // Set the state of the board to before the next move
        state.undoMove(m);
        
        return result;
      }

    /** A method that evaluates the given state. */
    // ToDo: write an appropriate evaluation function
    int evaluate(DraughtsState state) { 
        //obtain pieces array
        int[] pieces = state.getPieces();
        boolean whiteWins = true;
        boolean blackWins = true;
        // compute a value for t h i s s t a t e , e . g .
        // by comparing p[ i ] to WHITEPIECE, WHITEKING, e t c
        int computedValue = 0;
        for (int piece : pieces) {
        switch (piece) {
            case 0: // empty spot
                break;
            case DraughtsState.WHITEPIECE: // piece is a white piece
                computedValue++;
                blackWins = false;
                break;
            case DraughtsState.BLACKPIECE: // piece is a black piece
                computedValue--;
                whiteWins = false;
                break;
            case DraughtsState.WHITEKING: // piece is a white king
                computedValue = computedValue + 4;
                blackWins = false;
                break;
            case DraughtsState.BLACKKING: // piece is a black king
                computedValue = computedValue - 4;
                whiteWins = false;
                break;
        }
        
        // Check whether white or black wins with this move
        if (whiteWins) {
            computedValue = computedValue + 1000;
        }
        if(blackWins) {
            computedValue = computedValue - 1000;
        }
        
        }
        return computedValue ;
    }
    
}
