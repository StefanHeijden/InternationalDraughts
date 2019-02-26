package nl.tue.s2id90.group67;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import java.util.Collections;
import java.util.List;
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
    int kingWorth;
    boolean improved;
    
    /** boolean that indicates that the GUI asked the player to stop thinking. */
    private boolean stopped;

    public MyDraughtsPlayer(int kingWorth, boolean improved) {
        super("best.png"); // ToDo: replace with your own icon
        this.kingWorth = kingWorth;
        this.improved = improved;
    }
    
    @Override public Move getMove(DraughtsState s) {
        Move bestMove = null;
        bestValue = 0;
        DraughtsNode node = new DraughtsNode(s);    // the root of the search tree
        try {
            // Iterative Deepening on AlphaBeta 
            int depth = 1;
            
            while(true) { 
                // compute bestMove and bestValue in a call to alphabeta
                bestValue = alphaBeta(node, MIN_VALUE, MAX_VALUE, depth);
                
                // store the bestMove found uptill now
                // NB this is not done in case of an AIStoppedException in alphaBeat()
                bestMove  = node.getBestMove();
                
                // print the results for debugging reasons
                System.err.format(
                "%s: depth= %2d, best move = %5s, value=%d\n", 
                this.getClass().getSimpleName(),depth, bestMove, bestValue
                );
                // Increase depth
                depth++;
            }
        } catch (AIStoppedException ex) {  /* nothing to do */  }
        
        if (bestMove==null) {
            System.err.println("no valid move found!");
            return getRandomValidMove(s);
        } else {
            return bestMove;
        }
    } 

    /** This method's return value is displayed in the AICompetition GUI.
     * 
     * @return the value for the draughts state s as it is computed in a call to getMove(s). 
     */
    @Override public Integer getValue() { 
       return bestValue;
    }

    @Override
    public String getName() {
        return "King: " + kingWorth + " Improved: " + improved;
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
    int alphaBeta(DraughtsNode node, int alpha, int beta, int depth)
            throws AIStoppedException
    {
        if (node.getState().isWhiteToMove()) {
            return alphaBetaMax(node, alpha, beta, depth);
        } else  {
            return alphaBetaMin(node, alpha, beta, depth);
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
     int alphaBetaMax(DraughtsNode node, int alpha, int beta, int depth)
            throws AIStoppedException {
        // Stop the search if a certain amount of time has passed
        if (stopped) { 
            stopped = false; 
            System.out.println("stopped in AlphaBetaMax");
            throw new AIStoppedException(); 
        }
        
        // Get the current state of the board
        DraughtsState state = node.getState();
        
        // Get the current moves for the state
        List<Move> newMoves = state.getMoves();
        
        // If no moves are possible return the evaluation of the current state
        if (depth == 0 || newMoves.isEmpty()) {
            return evaluate(state);
        }
        
        // Evaluate each possible move for the state of the board
        Move currentBestMove = newMoves.get(0);
        while (!newMoves.isEmpty()) { // Continue untill no new moves are found
            
            // Set the state of the board to after doing the next move in the list
            state.doMove(newMoves.get(0));
            DraughtsNode newNode = new DraughtsNode(state);
            
            // Get the best move of the opponent
            int newAlpha = alphaBetaMin(newNode, alpha, beta, depth - 1);
            
            // Set the state of the board to before the next move
            state.undoMove(newMoves.get(0));
            
            // Check whether the next move is better then the previous ones
            if (newAlpha > alpha) {
                currentBestMove = newMoves.get(0);
                alpha = newAlpha;
            }
            //.....
            if (alpha >= beta) {
                node.setBestMove(newMoves.get(0));
                return beta;
            }
            // Remove the next move in the list
            newMoves.remove(0);
        }
        
        // Save the best move and alpha of that move
        node.setBestMove(currentBestMove);
        return alpha;
     }
     
    
    int alphaBetaMin(DraughtsNode node, int alpha, int beta, int depth)
            throws AIStoppedException {
        if (stopped) { 
            stopped = false; 
            System.out.println("stopped in AlphaBetaMin");
            throw new AIStoppedException(); 
        }
        
        DraughtsState state = node.getState();
        //String test = "max " + depth + ": ";
        List<Move> newMoves = state.getMoves();
        if (depth == 0 || newMoves.isEmpty()) {
            return evaluate(state);
        }
        Move currentBestMove = newMoves.get(0);
        while (!newMoves.isEmpty()) {
            state.doMove(newMoves.get(0));
            DraughtsNode newNode = new DraughtsNode(state);
            int newBeta = alphaBetaMax(newNode, alpha, beta, depth - 1);
            //test += newBeta + " ";
            state.undoMove(newMoves.get(0));
            if (newBeta < beta) {
                currentBestMove = newMoves.get(0);
                beta = newBeta;
            }
            if (beta <= alpha) {
                node.setBestMove(newMoves.get(0));
                //System.out.println(test);
                return alpha;
            }
            newMoves.remove(0);
        }
        
        node.setBestMove(currentBestMove);
        //System.out.println(test);
        return beta;
    }

    /** A method that evaluates the given state. */
    // ToDo: write an appropriate evaluation function
    int evaluate(DraughtsState state) { 
        //obtain pieces array
        int[] pieces = state.getPieces();
        
        // compute a value for t h i s s t a t e , e . g .
        // by comparing p[ i ] to WHITEPIECE, WHITEKING, e t c
        int computedValue = 0;
        int nPieces = 0;
        int nSpot = 1;
        boolean middle = false;
        for (int piece : pieces) {
            switch (piece) {
                case 0: // empty spot
                    break;
                case DraughtsState.WHITEPIECE: // piece is a white piece
                    computedValue = computedValue + 10;
                    if (middle && improved) {
                        computedValue++;
                    }
                    nPieces++;
                    break;
                case DraughtsState.BLACKPIECE: // piece is a black piece
                    computedValue = computedValue - 10;
                    if (middle && improved) {
                        computedValue--;
                    }
                    nPieces++;
                    break;
                case DraughtsState.WHITEKING: // piece is a white king
                    computedValue = computedValue + kingWorth;
                    nPieces++;  
                    break;
                case DraughtsState.BLACKKING: // piece is a black king
                    computedValue = computedValue - kingWorth;
                    nPieces++;
                    break;
            }         
            nSpot++;
            if (nSpot == 21 || nSpot == 27) {
                middle = true;
            } else if (nSpot == 26 || nSpot == 31) {
                middle = false;
            }
        }
        if (improved) {
            if (computedValue < -19) {
                computedValue = computedValue - nPieces / 4;
            } else if (computedValue > 19) {
                computedValue = computedValue + nPieces / 4;
            }
                
        }
        return computedValue ;
    }
}
