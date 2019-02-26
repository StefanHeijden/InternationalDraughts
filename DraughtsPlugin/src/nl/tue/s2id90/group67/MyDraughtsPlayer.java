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
        Move bestMove = null;
        bestValue = 0;
        maxSearchDepth = 12;
        DraughtsNode node = new DraughtsNode(s);    // the root of the search tree
        transpositionTable = new TranspositionTable();
        transpositionTable.setBoard(s.getPieces(), s.isWhiteToMove());
        try {
            // Iterative Deepening on AlphaBeta
            int depth = 1;

            while(depth <= maxSearchDepth) {
                // compute bestMove and bestValue in a call to alphabeta
                bestValue = alphaBeta(node, MIN_VALUE, MAX_VALUE, depth);

                // store the bestMove found uptill now
                // NB this is not done in case of an AIStoppedException in alphaBeat()
                bestMove  = node.getBestMove();

                // print the results for debugging reasons
                //System.err.format(
                //"%s: depth= %2d, best move = %5s, value=%d\n",
                //this.getClass().getSimpleName(),depth, bestMove, bestValue
                //);
                System.out.println("bestMove: " + bestMove + " best Value:  " + bestValue);
                System.out.println("-------------------------" + depth + 
                        "-------------------------");
                // Increase depth
                depth++;
            }
        } catch (AIStoppedException ex) {  
            System.out.println("stopped");
        /* nothing to do */  }

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
        int bestResult;
        boolean done = false;
        int hash = transpositionTable.getBoard();
       
        // Get the current state of the board
        DraughtsState state = node.getState();
        boolean whiteTurn = state.isWhiteToMove();
         
        // Get the information from the transposition table
        SimpleState information = transpositionTable.retrieve();// state
            
        // If the information of the state has been calculated with enough depth 
        // then check if the information is valid
        if(information != null && information.depthSearched >= depth) {
            System.out.println("found a good move from the TT");
            bestResult = information.bestScore;
            // If the move has been calculated exactly
            if(information.type == 0) {
                System.out.println("and that move is calculated exaclty so we stop this branch");
                return bestResult;
            }
            // If the move is a lower bound
            if(information.type == 1) {
                if( bestResult >= beta) {
                     return bestResult;
                }
                alpha = Math.max(alpha, bestResult);
            }
            // If the move is an upper bound
            if(information.type == 2) {
                if( bestResult <= alpha) {
                     return bestResult;
                }
                beta = Math.min(beta, bestResult);
            }
        }else {
            System.out.println();
        }
        
        // Get the current moves for the state
        List<Move> newMoves = state.getMoves();

        // If no moves are possible return the evaluation of the current state
        if (depth == 0 || newMoves.isEmpty()) {
            if(newMoves.isEmpty()) {
                System.out.println("no more moves found");
            }
            return evaluate(state);
        }
        
        Move currentBestMove;
        
        // Check the oldMove first
        if(information != null && information.depthSearched >= 0) {     
            System.out.println("check the move from TT first");
            state.doMove(information.bestMove);
            transpositionTable.doMove(information.bestMove);
            
            bestResult = alphaBeta(new DraughtsNode(state),
                   alpha, beta, depth - 1 );
            
            state.undoMove(information.bestMove);
            transpositionTable.undoMove(information.bestMove);
            
            currentBestMove = information.bestMove;
            if(whiteTurn) {
                if(bestResult >= beta) {
                System.out.println("since this is such a good move we stop bestResult:" + bestResult + " > = beta:  " + beta);
                done = true;
            }
            }else {
                if(bestResult <= alpha) {
                    System.out.println("since this is such a good move we stop bestResult:" + bestResult + " > = beta:  " + beta);
                    done = true;
                }
            }
        }else {
            currentBestMove = newMoves.get(0);
            
            if(whiteTurn) {
                bestResult = MIN_VALUE;
            }else {
                bestResult = MAX_VALUE;
            }
        }
        
        int newAlpha = alpha;
        int newBeta = beta;
        
        // Evaluate each possible move for the state of the board
        while (!newMoves.isEmpty() && !done) { // Continue untill no new moves are found
            DraughtsState testState = state;
            // Set the state of the board to after doing the next move in the list
            state.doMove(newMoves.get(0));
            transpositionTable.doMove(newMoves.get(0));
            state.undoMove(newMoves.get(0));
            transpositionTable.undoMove(newMoves.get(0));
            if(testState != state) {
                System.out.println("NOT THE SAME STATEEEEEEEEEEE!!!!!!!!!!!");
            }
            // Set the state of the board to after doing the next move in the list
            state.doMove(newMoves.get(0));
            transpositionTable.doMove(newMoves.get(0));
            if(whiteTurn) {
                // Get the best move of the opponent
                int newResult = alphaBeta(new DraughtsNode(state),
                   newAlpha, beta, depth - 1 );
                
                // Check whether the next move is better then the previous ones
                if (newResult > bestResult) {
                    currentBestMove = newMoves.get(0);
                    bestResult = newResult;
                }
                // -----------------------------------------------------------?????
                if (bestResult >= beta) {
                    node.setBestMove(newMoves.get(0));
                    bestResult = newResult;
                    //System.out.println("since this is such a good move in FORLOOP we stop bestResult:" + bestResult + " > = beta:  " + beta);
                    done = true;
                }
                newAlpha = Math.max(newAlpha, bestResult);
            }else {
                // Get the best move of the opponent
                int newResult = alphaBeta(new DraughtsNode(state),
                   alpha, newBeta, depth - 1 );
                
                // Check whether the next move is better then the previous ones
                if (newResult < bestResult) {
                    currentBestMove = newMoves.get(0);
                    bestResult = newResult;
                }
                // -----------------------------------------------------------?????
                if (bestResult <= alpha) {
                    node.setBestMove(newMoves.get(0));
                    bestResult = newResult;
                    //System.out.println("since this is such a good move in FORLOOP we stop bestResult:" + bestResult + " > = beta:  " + beta);
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
        /// TESTING PURPOSE --------------------------------------------!!!!!!!!
        if(hash != transpositionTable.getBoard()) {
            System.out.println("hash not the same!! oldhash: " + 
                    hash + " new " + transpositionTable.getBoard() );
        }
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