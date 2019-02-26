/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tue.s2id90.group67;
import org10x10.dam.game.Move;
import java.util.HashMap;
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
    private HashMap<Integer, SimpleState> states;
    
    // Save the board state
    int[] board;
    
    char dash = 45;
    char cross = 42; // ??? really
    
    TranspositionTable() {
        // The checker board has 5 different types of pieces: white piece, white king
        // white king, black piece, black king, empty and has 50 different positions
        // where it can be
        bitStrings = new int[5][50];
        
        // Determine a unique bitstring for each combination of piece and position
        // with zobrist hashing
        for (int i = 0; i <5; i++) {
            for (int j = 0; j < 50; j++) {
                bitStrings[i][j] = (int) (((long) (Math.random() * Long.MAX_VALUE)) & 0xFFFFFFFF);
                //System.out.println(bitStrings[i][j]);
            }
        }
        
        // Determine a unique bitstring for whether it is white turn
        // Used to switch turns
        isWhiteToMove = (int) (((long) (Math.random() * Long.MAX_VALUE)) & 0xFFFFFFFF);
        
        // Set hash to empty board
        hash = 0;
        states = new HashMap<Integer, SimpleState>();
    }
    
    // Initializes the hash to a certain board state
    public int setBoard(int[] board, boolean whiteTurn) {
        hash = 0;
        this.board = board;
        for(int i = 1 ; i<board.length;i++) {
            //System.out.println(hash);
            hash = hash ^ bitStrings[board[i]][i-1];
        }
        if(whiteTurn) {
            hash = hash ^ isWhiteToMove;
        }
        return hash;
    }
    
    // Returns the information of the current state
    public SimpleState retrieve(){
        //System.out.println(hash);
        SimpleState state = states.get(hash);
        return state;
    }
    
    public void store(Move bestMove, int bestScore, int type, int depthSearched) {
        states.put(hash, new SimpleState(bestMove, bestScore, type, depthSearched));
    }
    
    // Returns the hash(index) for the current state
    public int getBoard(){
        return hash;
    }
    
    // Update the the current board by doing a certain move
    public void doMove(Move move) {
        int beginPiece = move.getBeginField();
        int endPiece = move.getEndField();
        String stringOfMove = move.toString();
        
        // Update hash code
        hash = hash ^ bitStrings[board[beginPiece]][beginPiece];
        hash = hash ^ bitStrings[board[endPiece]][endPiece];

        // Update board
        board[endPiece] = board[beginPiece];
        board[beginPiece] = 0;
            
        if(stringOfMove.charAt(2) != dash ) {
            // System.out.print("move: " + move + "is a different move: " + stringOfMove);
            for(int i = 0; i < move.getCaptureCount();i++) {
                int o = move.getCapturedField(i);
                hash = hash ^ bitStrings[board[o]][o];
                board[o] = 0;
            }
        }
        
    }
    
    // Update the the current board by undoing a certain move
    public void undoMove(Move move) {
        int beginPiece = move.getBeginField();
        int endPiece = move.getEndField();
        String stringOfMove = move.toString();
        
        // Update board
        board[beginPiece] = board[endPiece];
        board[endPiece] = 0;

        // Update hash code
        hash = hash ^ bitStrings[board[beginPiece]][beginPiece];
        hash = hash ^ bitStrings[board[endPiece]][endPiece];
            
        if(stringOfMove.charAt(2) != dash ) {
            // System.out.print("move: " + move + "is a different move: " + stringOfMove);
            for(int i = 0; i < move.getCaptureCount();i++) {
                int o = move.getCapturedField(i);
                board[o] = move.getCapturedPiece(i);
                hash = hash ^ bitStrings[board[o]][o];
            }
        }
    }
    
    public void add(int piece, int position) {
        hash = hash ^ bitStrings[piece][position];
    }
    
    public void remove(int piece, int position) {
        hash = hash ^ bitStrings[piece][position];
    }
    
}
