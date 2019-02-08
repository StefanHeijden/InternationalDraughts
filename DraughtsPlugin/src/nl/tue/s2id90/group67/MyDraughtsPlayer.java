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
    int maxSearchDepth;
    
    /** boolean that indicates that the GUI asked the player to stop thinking. */
    private boolean stopped;

    public MyDraughtsPlayer(int maxSearchDepth) {
        super("best.png"); // ToDo: replace with your own icon
        this.maxSearchDepth = maxSearchDepth;
    }
    
    @Override public Move getMove(DraughtsState s) {
        Move bestMove = null;
        bestValue = 0;
        DraughtsNode node = new DraughtsNode(s);    // the root of the search tree
        try {
            // compute bestMove and bestValue in a call to alphabeta
            bestValue = alphaBeta(node, MIN_VALUE, MAX_VALUE, maxSearchDepth);
            
            // store the bestMove found uptill now
            // NB this is not done in case of an AIStoppedException in alphaBeat()
            bestMove  = node.getBestMove();
            
            // print the results for debugging reasons
            System.err.format(
                "%s: depth= %2d, best move = %5s, value=%d\n", 
                this.getClass().getSimpleName(),maxSearchDepth, bestMove, bestValue
            );
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
     int alphaBetaMin(DraughtsNode node, int alpha, int beta, int depth)
            throws AIStoppedException {
        if (stopped) { stopped = false; throw new AIStoppedException(); }
        DraughtsState state = node.getState();
        // ToDo: write an alphabeta search to compute bestMove and value
        List<Move> newMoves = state.getMoves();
        if (depth == maxSearchDepth || newMoves.isEmpty()) {
            return evaluate(state);
        }
        Move currentBestMove = newMoves.get(0);
        while (!newMoves.isEmpty()) {
            state.doMove(newMoves.get(0));
            DraughtsNode newNode = new DraughtsNode(state);
            state.undoMove(newMoves.get(0));
            int newAlpha = alphaBetaMax(newNode, alpha, beta, depth + 1);
            if (newAlpha > alpha) {
                currentBestMove = newMoves.get(0);
                alpha = newAlpha;
            }
            if (alpha >= beta) {
                node.setBestMove(newMoves.get(0));
                return beta;
            }
            newMoves.remove(0);
        }
        node.setBestMove(currentBestMove);
        return alpha;
     }
    
    int alphaBetaMax(DraughtsNode node, int alpha, int beta, int depth)
            throws AIStoppedException {
        if (stopped) { stopped = false; throw new AIStoppedException(); }
        DraughtsState state = node.getState();
        // ToDo: write an alphabeta search to compute bestMove and value
        List<Move> newMoves = state.getMoves();
        if (depth == maxSearchDepth || newMoves.isEmpty()) {
            return evaluate(state);
        }
        Move currentBestMove = newMoves.get(0);
        while (!newMoves.isEmpty()) {
            state.doMove(newMoves.get(0));
            DraughtsNode newNode = new DraughtsNode(state);
            state.undoMove(newMoves.get(0));
            int newBeta = alphaBetaMin(newNode, alpha, beta, depth + 1);
            if (newBeta < beta) {
                currentBestMove = newMoves.get(0);
                alpha = newBeta;
            }
            if (beta <= alpha) {
                node.setBestMove(newMoves.get(0));
                return alpha;
            }
            newMoves.remove(0);
        }
        node.setBestMove(currentBestMove);
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
        for (int piece : pieces) {
        switch (piece) {
            case 0: // empty spot
                break;
            case 1: // piece is a white piece
                computedValue++;
                break;
            case 2: // piece is a white king
                computedValue = computedValue + 2;
                break;
            case 3: // piece is a black piece
                computedValue--;
                break;
            case 4: // piece is a black king
                computedValue = computedValue - 2;
                break;
        }         
        }
        return computedValue ;
    }
}
