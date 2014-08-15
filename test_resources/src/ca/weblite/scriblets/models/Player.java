/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.scriblets.models;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author shannah
 */
public class Player {
    private List<Card> hand;
    private String name;
    private int score;
    public static final int MAX_CARDS=5;
    
    public Player(){
        hand = new ArrayList<Card>();
    }
    
    public Card[] getHand(){
        return hand.toArray(new Card[0]);
    }
    
    public boolean removeCard(Card card){
        return hand.remove(card);
           
    }
    
    public boolean addCard(Card card){
        if (hand.size() < MAX_CARDS ){
            hand.add(card);
            return true;
        } else {
            return false;
        }
    }
}
