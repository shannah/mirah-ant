/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.scriblets.models;

/**
 *
 * @author shannah
 */
public enum Suit {
    DIAMONDS,
    HEARTS,
    CLUBS,
    SPADES;
    
    public String shortLabel(){
        switch (this){
            case DIAMONDS: return "D";
            case HEARTS: return "H";
            case CLUBS: return "C";
            case SPADES: return "S";
        }
        return "?";
    }
    
    public static final Suit[] ALL = new Suit[]{DIAMONDS, HEARTS, CLUBS, SPADES};
}
