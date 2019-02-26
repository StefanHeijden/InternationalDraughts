/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.s2id90.group67;
import org10x10.dam.game.Move;

/**
 *
 * @author stefa
 */
public class SimpleState {
    // Best move for this state
    Move bestMove;
    
    // Result of the evaluation of the best move
    int bestScore;
    
    // Whether the score is exact, lowerbound or upperbound
    int type;
    
    // Maximum depth that was searched while calculating the best move
    int depthSearched;
    
    SimpleState(Move bestMove, int bestScore, int type, int depthSearched) {
        this.bestMove = bestMove;
        this.bestScore = bestScore;
        this.type = type;
        this.depthSearched = depthSearched;
    }
   
}
