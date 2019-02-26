package nl.tue.s2id90.group67;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import java.util.Collections;
import java.util.List;
import nl.tue.s2id90.draughts.DraughtsState;
import nl.tue.s2id90.draughts.player.DraughtsPlayer;
import nl.tue.s2id90.group67.AIStoppedException;
import org10x10.dam.game.Move;

/**
 * Implementation of the DraughtsPlayer interface.
 * @author huub
 */
// ToDo: rename this class (and hence this file) to have a distinct name
//       for your player during the tournament
public class MyDraughtsPlayer  extends DraughtsPlayer{
    private int bestValue=0;
    int maxSearchDepth;
    TranspositionTable transpositionTable;

    /** boolean that indicates that the GUI asked the player to stop thinking. */
    private boolean stopped;

    public MyDraughtsPlayer(int maxSearchDepth) {
        super("best.png"); // ToDo: replace with your own icon
        this.maxSearchDepth = maxSearchDepth;
    }

    @Override public Move getMove(DraughtsState s) {
        Move bestMove = null; // Best move for this state
        bestValue = 0; // Result of the evaluation of the best move
        maxSearchDepth = 20; // Set max search depth in case of bugs
        DraughtsNode node = new DraughtsNode(s);    // the root of the search tree
        // Initialize the transposition table, which is used to speed
        // up searches on the same state
        transpositionTable = new TranspositionTable();
        transpositionTable.setBoard(s.getPieces(), s.isWhiteToMove());
        try {
            // Iterative Deepening on AlphaBeta
            int depth = 1;
            while(depth <= maxSearchDepth) {
                // compute bestMove and bestValue in a call to alphabeta
                bestValue = alphaBeta(node, MIN_VALUE, MAX_VALUE, depth);
                
                // store the bestMove found uptill now
                bestMove  = node.getBestMove();
                
                // Increase depth
                depth++;
            }
        } catch (AIStoppedException ex) {  
        /* nothing to do */  }
        if (bestMove==null) {
            return getRandomValidMove(s);
        } else {
            System.out.println("bestMove: " + bestMove + " bestValue: " + bestValue);
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
     int alphaBeta(DraughtsNode node, int alpha, int beta, int depth)
            throws AIStoppedException {
        // Stop the search if a certain amount of time has passed
        if (stopped) {
            stopped = false;
            System.out.println("stopped in AlphaBeta");
            throw new AIStoppedException();
        }
        // The evaluation of the best move found so far
        int bestResult;
        // The best move found so far
        Move currentBestMove; 
        // Done is used if an value is found that is maximum
        //, thus stopping the search
        boolean done = false;
        // Get the current state of the board
        DraughtsState state = node.getState();
        boolean whiteTurn = state.isWhiteToMove();
        // Get the current moves for the state
        List<Move> newMoves = state.getMoves();
        // Get the information from the transposition table
        SimpleState information = transpositionTable.retrieve();

        // If no moves are possible return the evaluation of the current state
        if (depth == 0 || newMoves.isEmpty()) {
            return evaluate(state);
        }
        
        // This is a check , in case of bugs
        boolean ok = true;
        if(information != null && !newMoves.contains(information.bestMove)) {
            ok = false;
        }
 
        // If the information of the state has been calculated with enough depth 
        // and the information is valid, check the precision of the results
        if(ok && information != null && information.depthSearched >= depth) {
            bestResult = information.bestScore;
            // If the move has been calculated exactly
            if(information.type == 0) {
                node.setBestMove(information.bestMove);
                return bestResult;
            }
            // If the move is a lower bound
            if(information.type == 1) {
                if( bestResult >= beta) {
                    node.setBestMove(information.bestMove);
                    return bestResult;
                }
                alpha = Math.max(alpha, bestResult);
            }
            // If the move is an upper bound
            if(information.type == 2) {
                if( bestResult <= alpha) {
                    node.setBestMove(information.bestMove);
                    return bestResult;
                }
                beta = Math.min(beta, bestResult);
            }
        }
        
        // Check the oldMove first, since there is a high chance this is the best
        // move already. This is only done if the there is any (valid) information
        // about the state.
        if(ok && information != null && information.depthSearched >= 0) {  
            // Do the move
            state.doMove(information.bestMove);
            transpositionTable.doMove(information.bestMove);
            
            // Start calculating the best results for the next moves
            bestResult = alphaBeta(new DraughtsNode(state),
                   alpha, beta, depth - 1 );
            
            // Undo the move
            state.undoMove(information.bestMove);
            transpositionTable.undoMove(information.bestMove);
            
            // Safe the move from the transition table as the best move
            currentBestMove = information.bestMove;
            
            // Check depending on whos turn it is whether the results
            // are already the best results by comparing them with alpha/beta
            if(whiteTurn) {
                if(bestResult >= beta) {
                done = true;
            }}else {
                if(bestResult <= alpha) {
                    done = true;
                }
            }// if not then continue
        }else {
            // Safe the first move from the transition table as the best move
            // just in case there are no good moves
            currentBestMove = newMoves.get(0);
            
            // How bestResult is initialized depends on whos turn it is
            // since white will try to get a maximum result and black minimum
            if(whiteTurn) {
                bestResult = MIN_VALUE;
            }else {
                bestResult = MAX_VALUE;
            }
        }
        
        // Initialize newAlpha and newBeta with alpha and beta
        int newAlpha = alpha;
        int newBeta = beta;
        
        // Evaluate each possible move for the state of the board
        while (!newMoves.isEmpty() && !done) { // Continue untill no new moves are found
            // Set the state of the board to after doing the next move in the list
            state.doMove(newMoves.get(0));
            transpositionTable.doMove(newMoves.get(0));
            
            if(whiteTurn) {// if it is a maxnode e.g. white turn
                // Get the best move of the opponent
                int newResult = alphaBeta(new DraughtsNode(state),
                   newAlpha, beta, depth - 1 );
                
                // Check whether the next move is better then the previous ones
                if (newResult > bestResult) {
                    currentBestMove = newMoves.get(0);
                    bestResult = newResult;
                }
                
                if (bestResult >= beta) {
                    node.setBestMove(newMoves.get(0));
                    bestResult = newResult;
                    //System.out.println("since this is such a good move in FORLOOP we stop bestResult:" + bestResult + " > = beta:  " + beta);
                    done = true;
                }
                newAlpha = Math.max(newAlpha, bestResult);
            }else {// if it is a minnode, e.g. black turn
                // Get the best move of the opponent
                int newResult = alphaBeta(new DraughtsNode(state),
                   alpha, newBeta, depth - 1 );
                
                // Check whether the next move is better then the previous ones
                if (newResult < bestResult) {
                    currentBestMove = newMoves.get(0);
                    bestResult = newResult;
                }
                
                if (bestResult <= alpha) {
                    node.setBestMove(newMoves.get(0));
                    bestResult = newResult;
                    done = true;
                }

                newBeta = Math.min(newBeta, bestResult);
            }

            // Set the state of the board to before the next move
            state.undoMove(newMoves.get(0));
            transpositionTable.undoMove(newMoves.get(0));
            
            // Remove the next move in the list
            newMoves.remove(0);
        }

        // Determine whether the results are exact, a lowerbound or upperbound
        int type;
        if(bestResult <= alpha) {
            type = 1; // lowerbound
        }else {
            if(bestResult >= beta) {
              type = 2; // uppperbound
            }else {
              type = 0; // exact
            }
        }
        // Save the results in the transposition table, as to speed
        // up future searches on the same state
        transpositionTable.store( currentBestMove, bestResult, type, depth);
        // Save the best move and alpha of that move
        node.setBestMove(currentBestMove);
        return bestResult;
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