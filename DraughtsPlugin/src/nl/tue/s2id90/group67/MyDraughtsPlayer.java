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
public class MyDraughtsPlayer  extends DraughtsPlayer{
    private int bestValue=0;
    int maxSearchDepth;
    Move[][] listOfPreviousMoves;
    int[] alpha;
    int[] beta;
    int currentDepth;
    
    /** boolean that indicates that the GUI asked the player to stop thinking. */
    private boolean stopped;

    public MyDraughtsPlayer(int maxSearchDepth) {
        super("best.png"); // ToDo: replace with your own icon
        this.maxSearchDepth = maxSearchDepth;
    }
    
    @Override public Move getMove(DraughtsState s) {
        Move bestMove = null;
        bestValue = 0;
        maxSearchDepth = 100;
        DraughtsNode node = new DraughtsNode(s);    // the root of the search tree
        // Get the current state of the board
        DraughtsState state = node.getState();

        // Get the current moves for the state
        List<Move> firstMoves = state.getMoves();
        Collections.shuffle(firstMoves);
        // put a if statement for if no moves are found????
        
        // This list is used to remember the all the bestmoves for each move
        listOfPreviousMoves = new Move[firstMoves.size()][];

        // Add the current moves to the listOfPreviousMoves
        for(int i=0;i<firstMoves.size();i++){
            listOfPreviousMoves[i] = new Move[maxSearchDepth + 1];
            listOfPreviousMoves[i][0] = firstMoves.get(i);
        }
        alpha = new int[maxSearchDepth + 1];
        beta = new int[maxSearchDepth + 1];
        
        try {           
            // Iterative Deepening on AlphaBeta 
            currentDepth = 1;
            while(currentDepth <= maxSearchDepth) {
                int index = 0;
                //List<List<Move>> copyOfListOfPreviousMoves = listOfPreviousMoves;
                for(Move[] moveList : listOfPreviousMoves) {
                    DraughtsNode newNode = node;
                    DraughtsState newState;
                    for(Move m : moveList) {
                        if(m == null) {
                            break;
                        }
                        // Set the state of the board to after doing the next move in the list
                        newState = newNode.getState();
                        newState.doMove(m);
                        newNode = new DraughtsNode(newState);
                    }
                    newState = newNode.getState();
                    
                     // compute bestMove and bestValue in a call to alphabeta
                    alphaBeta(newState, index);
                    index++;
                }
                currentDepth++;
                
                // for testing only, not neccessary in real run
                bestMove = calcBestMove(state);
            }
        } catch (AIStoppedException ex) {
            
            System.out.println("Stopped in getMove()");
        }
    
        bestMove = calcBestMove(state);
        
        if (bestMove==null) {
            System.err.println("no valid move found!");
            return getRandomValidMove(s);
        } else {
            return bestMove;
        }
    } 
    
    Move calcBestMove(DraughtsState state) {
        int indexForBestMove = 0;
        if (state.isWhiteToMove()) {
            bestValue = alpha[0];
            for(int i = 1;i<listOfPreviousMoves.length;i++) {
                if(alpha[i] < bestValue) {
                    bestValue = alpha[i];
                    indexForBestMove = i;
                }
            }
        } else  {
            bestValue = beta[0];
            for(int i = 1;i<listOfPreviousMoves.length;i++) {
                if(beta[i] > bestValue) {
                    bestValue = beta[i];
                    indexForBestMove = i;
                }
            }
        }
        
        // print the results for debugging reasons
        System.err.format(
        "%s: depth= %2d, best move = %5s, value=%d\n", 
        this.getClass().getSimpleName(),currentDepth - 1, 
        listOfPreviousMoves[indexForBestMove][0], bestValue
        );
        
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
            System.out.println("No moves found");
        }else{
            // Evaluate each possible move for the state of the board
            Move currentBestMove = moves.get(0);
            for (Move m : moves) { // Continue untill no new moves are found
                
                // Evaluate the the result of the next move
                int result = evaluateMove(state, m);

                // Check whether the next move is better then the previous ones
                if (result > alpha[index]) {
                    currentBestMove = m;
                    alpha[index] = result;
                }
                //.....
                if (alpha[index] >= beta[index]) {
                    currentBestMove = m;
                    break;
                    // alpha[index] = beta[index];
                }
            }
            // Save the best move
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
            System.out.println("No moves found");
        }else{
            // Evaluate each possible move for the state of the board
            Move currentBestMove = moves.get(0);
            for (Move m : moves) { // Continue untill no new moves are found
                
                // Evaluate the the result of the next move
                int result = evaluateMove(state, m);

                // Check whether the next move is better then the previous ones
                if (result < beta[index]) {
                    currentBestMove = m;
                    beta[index] = result;
                }
                //.....
                if (beta[index] <= alpha[index]) {
                    currentBestMove = m;
                    break;
                    // alpha[index] = beta[index];
                }
            }
            // Save the best move
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
        
        // compute a value for t h i s s t a t e , e . g .
        // by comparing p[ i ] to WHITEPIECE, WHITEKING, e t c
        int computedValue = 0;
        for (int piece : pieces) {
        switch (piece) {
            case 0: // empty spot
                break;
            case DraughtsState.WHITEPIECE: // piece is a white piece
                computedValue++;
                break;
            case DraughtsState.BLACKPIECE: // piece is a black piece
                computedValue--;
                break;
            case DraughtsState.WHITEKING: // piece is a white king
                computedValue = computedValue + 2;
                break;
            case DraughtsState.BLACKKING: // piece is a black king
                computedValue = computedValue - 2;
                break;
        }         
            
        }
        return computedValue ;
    }
}
