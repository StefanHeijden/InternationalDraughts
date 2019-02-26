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
    private int hash;
    private int[][] bitStrings;
    private int isWhiteToMove;
    private SimpleState[] states = new SimpleState [Integer.MAX_VALUE];
    
    TranspositionTable() {
        bitStrings = new int[5][50];
        for (int i = 0; i <5; i++) {
            for (int j = 0; j < 50; j++) {
                bitStrings[i][j] = (int) (((long) (Math.random() * Long.MAX_VALUE)) & 0xFFFFFFFF);
            }
        }
        hash = 0;
        isWhiteToMove = (int) (((long) (Math.random() * Long.MAX_VALUE)) & 0xFFFFFFFF);
    }
    
    public int setBoard(int[] board, boolean whiteTurn) {
        for(int i = 1 ; i<board.length;i++) {
             hash = hash ^ bitStrings[board[i]][i];
        }
        if(whiteTurn) {
            hash = hash ^ isWhiteToMove;
        }
        return hash;
    }
    
    public SimpleState getInformation(){
        if(states[hash] != null) {
            return states[hash];
        }
        return null;
    }
    
    public int getBoard(){
        return hash;
    }
    
    public void doMove(Move move) {
        
    }
    
    public void undoMove(Move move) {
        
    }
    
    public void add(int piece, int position) {
        hash = hash ^ bitStrings[piece][position];
    }
    
    public void remove(int piece, int position) {
        hash = hash ^ bitStrings[piece][position];
    }
    
}
