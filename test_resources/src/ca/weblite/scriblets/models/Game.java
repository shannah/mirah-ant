/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.scriblets.models;

import java.util.Deque;
import java.util.LinkedList;

/**
 *
 * @author shannah
 */
public class Game {
    private Player[] players;
    private final Board board;
    private Deck deck;
    private Deque<Move> moves;
    
    
    
    public Game(){
        board = new Board(9, 7);
        players = new Player[]{new Player(), new Player()};
        deck = new Deck();
        deal(players);
        
        moves = new LinkedList<Move>();
        
        
        
    }
    
    private void deal(Player[] players){
        boolean dealDone = false;
        while ( !dealDone ){
            boolean someoneTookCard = false;
            for ( int i=0; i< players.length; i++){
                Card card = deck.pop();
                if ( card == null ){
                    dealDone = true;
                } else if ( players[i].addCard(card)){
                    someoneTookCard = true;
                }
            }
            if ( !someoneTookCard ){
                dealDone = true;
            }
        }
    }

    /**
     * @return the board
     */
    public Board getBoard() {
        return board;
    }
    
    public Player getPlayer(int index){
        return players[index];
    }
    
    public Move createMove(Player player){
        Move m = new Move(this, player);
        moves.push(m);
        return m;
    }
    
    public void commit(Move move){
        move.getBoardSnapshot().copyInto(board);
        move.setDone(true);
        deal(new Player[]{move.getPlayer()});
        
    }
    
    public Move getCurrentMove(){
        if ( moves.isEmpty()){
            return null;
        } else {
            return moves.peek();
        }
    }
    
    
    
    
}
