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
public class TranspositionTable {
    // Current bit string for the state of the board
    // Used as index for the list of Simple States
    private int hash;
    
    // All the different bitstrings for each possible 
    private int[][] bitStrings;
    
    // Bitstring of whether it is whites turn
    // Xoring this with the hash switches the turn for the same board
    private int isWhiteToMove;
    
    // The list with all of the states previously searched with AlphaBeta
    private SimpleState[] states = new SimpleState [Integer.MAX_VALUE];
    
    TranspositionTable() {
        // The checker board has 5 different types of pieces: white piece, white king
        // white king, black piece, black king, empty and has 50 different positions
        // where it can be
        bitStrings = new int[5][50];
        
        // Determine a unique bitstring for each combination of piece and position
        // with zobric hashing
        for (int i = 0; i <5; i++) {
            for (int j = 0; j < 50; j++) {
                bitStrings[i][j] = (int) (((long) (Math.random() * Long.MAX_VALUE)) & 0xFFFFFFFF);
            }
        }
        
        // Determine a unique bitstring for whether it is white turn
        // Used to switch turns
        isWhiteToMove = (int) (((long) (Math.random() * Long.MAX_VALUE)) & 0xFFFFFFFF);
        
        // Set hash to empty board
        hash = 0;
    }
    
    // Initializes the hash to a certain board state
    public int setBoard(int[] board, boolean whiteTurn) {
        hash = 0;
        for(int i = 1 ; i<board.length;i++) {
             hash = hash ^ bitStrings[board[i]][i];
        }
        if(whiteTurn) {
            hash = hash ^ isWhiteToMove;
        }
        return hash;
    }
    
    // Returns the information of the current state
    public SimpleState retrieve(){
        if(states[hash] != null) {
            return states[hash];
        }
        return null;
    }
    
    public void store(Move bestMove, int bestScore, int type, int depthSearched) {
        states[hash] = new SimpleState(bestMove, bestScore, type, depthSearched);
    }
    
    // Returns the hash(index) for the current state
    public int getBoard(){
        return hash;
    }
    
    // Update the the current board by doing a certain move
    public void doMove(Move move) {
        
    }
    
    // Update the the current board by undoing a certain move
    public void undoMove(Move move) {
        
    }
    
    public void add(int piece, int position) {
        hash = hash ^ bitStrings[piece][position];
    }
    
    public void remove(int piece, int position) {
        hash = hash ^ bitStrings[piece][position];
    }
    
}
