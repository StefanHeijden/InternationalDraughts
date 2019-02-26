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
        maxSearchDepth = 20;
        DraughtsNode node = new DraughtsNode(s);    // the root of the search tree
        transpositionTable = new TranspositionTable();
        transpositionTable.setBoard(s.getPieces(), s.isWhiteToMove());
        try {
            // Iterative Deepening on AlphaBeta
            int depth = 1;

            while(depth <= maxSearchDepth) {
                // compute bestMove and bestValue in a call to alphabeta
                if (s.isWhiteToMove()) {
                    bestValue = alphaBeta(node, MIN_VALUE, MAX_VALUE, depth);
                }else {
                    bestValue =  -1 * alphaBeta(node, MIN_VALUE, MAX_VALUE, depth);
                }

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
        int oldAlpha = alpha;
        Move oldMove = new Move();// -------------------------------------!!!!!!!!
        int result = 0;
        int oldType = -1;
        int oldDepth = -1;
        boolean done = false;
       
        // Get the current state of the board
        DraughtsState state = node.getState();
        // Get the information from the transposition table
        int[] information = retrieveInformation(0);// state
        // If no information for the state is found set depth to -1
        if(information != null) {
            oldMove = intToMove(information[0]);
            result = information[1];
            oldType = information[2];
            oldDepth = information[3];
        }
            
        // If the information of the state has been calculated with enough depth 
        if(oldDepth >= depth) {
            // If the move has been calculated exactly
            if(oldType == 0) {
                return result;
            }
            // If the move is lower bound
            if(oldType == 1) {
                alpha = Math.max(alpha, result);
            }
            // If the move is upper bound
            if(oldType == 2) {
                beta = Math.min(beta, result);
            }
        }
        
        // Get the current moves for the state
        List<Move> newMoves = state.getMoves();

        // If no moves are possible return the evaluation of the current state
        if (depth == 0 || newMoves.isEmpty()) {
            return evaluate(state);
        }
        
        
        Move currentBestMove = newMoves.get(0);
        // Check the oldMove first
        if(oldDepth >= 0) {
            state.doMove(oldMove);
            result = -1 * alphaBeta(new DraughtsNode(state),
                    -1 * beta, -1 * alpha, depth - 1 );
            state.undoMove(oldMove);
            currentBestMove = oldMove;
            if(result >= beta) {
                done = true;
            }
        }else {
            result = MIN_VALUE;
        }
        
        // Evaluate each possible move for the state of the board
        while (!newMoves.isEmpty() && !done) { // Continue untill no new moves are found

            // Set the state of the board to after doing the next move in the list
            state.doMove(newMoves.get(0));

            // Get the best move of the opponent
            int newAlpha = -1 * alphaBeta(new DraughtsNode(state),
                    -1 * beta, -1 * alpha, depth - 1 );

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
                alpha = newAlpha;
                done = true;
            }
            // Remove the next move in the list
            newMoves.remove(0);
        }

        if(result <= oldAlpha) {
            oldType = 1;
        }else {
            if(result >= beta) {
              oldType = 2;  
            }else {
              oldType = 0;
            }
        }
        storeInformation(stateToInt(state), moveToInt(currentBestMove), result, oldType, depth);
        // Save the best move and alpha of that move
        node.setBestMove(currentBestMove);
        return alpha;
     }
    
    // Here we retreive the information of a certain state from the transposition table
    int[] retrieveInformation(int state, boolean isWhiteToMove) {
        boolean found = false;
        // try finding state in transposition table
        if(found) {
            int[] information = new int[4];
            // Get the best move
            information[0] = 0;
            // Get the score for that move
            information[1] = 0;
            // Whether the score is exact(0), lower bound (1), upper bound (2)
            information[2] = 0;
            // max depth with which move was calculated
            information[3] = 0;
            return information;
        }
        
        return null;
    }
    
    // Store the information for a state in the transposition table
    void storeInformation(int state, int move, int score, int type, int depth) {
        
    }
    
    int stateToInt(DraughtsState state) {
        return 0;
    }
    
    Move intToMove(int move){
        return null;
    }
    
    int moveToInt(Move move) {
        return 0;
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