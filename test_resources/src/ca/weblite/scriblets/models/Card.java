/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.scriblets.models;

import com.codename1.components.StorageImageAsync;
import com.codename1.io.Storage;
import com.codename1.ui.Image;
import com.codename1.ui.geom.Dimension;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author shannah
 */
public class Card {
    private final Suit suit;
    private final Rank rank;
    private Image icon;
    
    private final static Map<String,Image> placeholderImages = new HashMap<String,Image>();
    
    public Card(Rank rank, Suit suit){
        this.suit = suit;
        this.rank = rank;
    }
    
    public static String getIconStorageKey(Rank rank, Suit suit, Dimension size){
        return "icons__"+suit.name()+"_"+rank.name()+"_"+size.getWidth()+"x"+size.getHeight();
    }
    
    public static Image getPlaceholderImage(Dimension size){
        String key = size.getWidth()+"x"+size.getHeight();
        if ( !placeholderImages.containsKey(key)){
            placeholderImages.put(key, Image.createImage(size.getWidth(), size.getHeight(), 0x0));
        }
        return placeholderImages.get(key);
    }
    
    public static Image getIcon(Rank rank, Suit suit, Dimension size ){
        String key = getIconStorageKey(rank, suit, size);
        if ( Storage.getInstance().exists(key)){
            return StorageImageAsync.create(key, getPlaceholderImage(size));
        } else {
            return null;
        }
    }
    
    public Image getIcon(Dimension size){
        if ( icon == null || icon.getWidth() != size.getWidth() || icon.getHeight() != size.getHeight() ){
            icon = getIcon(rank, suit, size);
        }
        return icon;
    }
    
    public String getLabel(){
        return rank.name() + " of "+suit.name();
    }
    
    public String getShortLabel(){
        return rank.shortLabel()+suit.shortLabel();
    }
    
    public int getValue(){
        return rank.value();
    }
    
    public int getOrder(){
        return rank.order();
    }
    
    public Rank getRank(){
        return rank;
    }
    
    public Suit getSuit(){
        return suit;
    }
    
    public String toString(){
        return getLabel();
    }
}
