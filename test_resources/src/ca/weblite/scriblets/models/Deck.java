/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.scriblets.models;

import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author shannah
 */
public class Deck {
    private Deque<Card> cards;
    private Random rand = new Random();
    public Deck(){
        cards = new LinkedList<Card>();
        
        for ( Rank r : Rank.ALL){
            for ( Suit s : Suit.ALL){
                cards.add(new Card(r,s));
            }
        }
        
        Collections.sort((LinkedList)cards, new Comparator<Card>(){

            public int compare(Card o1, Card o2) {
                if ( o1 == o2 ){
                    return 0;
                }
                if ( rand.nextDouble() < 0.5 ){
                    return -1;
                } else {
                    return 1;
                }
            }
            
        });
        
    }
    
    public Card peek(){
        return cards.peek();
    }
    
    public Card pop(){
        return cards.pop();
    }
    
    public int numCardsRemaining(){
        return cards.size();
    }
    
    public boolean isEmpty(){
        return cards.isEmpty();
    }
}
