/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.scriblets;

import ca.weblite.scriblets.models.Card;
import com.codename1.io.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author shannah
 */
public class Calculator {
    
    
    private static List<Collection<Card>> getSetsWithSum(Collection<Card> cardsArr, int sum){
        List<Collection<Card>> out = new ArrayList<Collection<Card>>();
        LinkedList<Card> tmp = new LinkedList<Card>(cardsArr);
        
        if ( tmp.isEmpty() ){
            return out;
        } else {
            Card card = tmp.pop();
            
            if ( card.getValue() == sum ){
                out.add(new LinkedList<Card>(Arrays.asList(new Card[]{card})));
            } else if ( card.getValue() < sum ){
                List<Collection<Card>> setsWithThisCard = getSetsWithSum(tmp, sum - card.getValue());
                //for ( int j=0; j<len; j++){
                for ( Collection<Card> s : setsWithThisCard) {
                    s.add(card);
                    out.add(s);
                }
            }
            
            // Get sets without this card
            
            List<Collection<Card>> setsWithoutThisCard = getSetsWithSum(tmp, sum);
            //for ( var j=0; j<setsWithoutThisCard.length; j++ ){
            for ( Collection<Card> s : setsWithoutThisCard ){
                out.add(s);
            }
            
            return out;
        }
    }
    
    public static final List<Collection<Card>> getFifteens(Card[] cards){
        return getFifteens(Arrays.asList(cards));
    }
    public static final List<Collection<Card>> getFifteens(Collection<Card> cardsArr){
        return getSetsWithSum(cardsArr, 15);
    }
    
    
    public static final List<List<Card>> getRuns(Card[] cards){
        return getRuns(Arrays.asList(cards));
    }
    public static final List<List<Card>> getRuns(Collection<Card> cardsArr){
        List<Card> tmp = new ArrayList<Card>(cardsArr);
        Collections.sort(tmp, new Comparator<Card>(){

            public int compare(Card a, Card b) {
                if ( a.getOrder() < b.getOrder() ) return -1;
                else if ( a.getOrder() > b.getOrder() ) return 1;
                else return 0;
            }
            
        });
        
        
        List<List<Card>> runs = new ArrayList<List<Card>>();
        //for ( var i=0; i<tmp.length; i++  ){
        //    runs.push([tmp[i]]);
        for ( Card c : tmp ){
            
            //for ( var j=0; j<runs.length; j++ ){
            boolean inRun = false;
            List<List<Card>> runsToAdd = new ArrayList<List<Card>>();
            for ( List<Card> r : runs){
                int runLen = r.size();
                if ( r.get(runLen-1).getOrder() == c.getOrder() -1 ){
                    r.add(c);
                    inRun = true;
                } else if ( r.get(runLen-1).getOrder() == c.getOrder() ){
                    ArrayList<Card> copy = new ArrayList<Card>(r);
                    copy.remove(runLen-1);
                    copy.add(c);
                    runsToAdd.add(copy);
                    inRun = true;
                }
                
            }
            if ( !inRun ){
                runs.add(new ArrayList<Card>(Arrays.asList(new Card[]{c})));
            }
            runs.addAll(runsToAdd);
        }
        List<List<Card>> out = new ArrayList<List<Card>>();
        //for ( var i=0; i<runs.length; i++ ){
        for ( List<Card> r : runs){
            if ( r.size() > 2 ){
                out.add(r);
            }
        }
        return out;
        
    }
    
    
    public static final Collection<Collection<Card>> getMatches(Card[] cards){
        return getMatches(Arrays.asList(cards));
    }
    public static final Collection<Collection<Card>> getMatches(Collection<Card> cardsArr){
        List<Card> tmp = new ArrayList<Card>(cardsArr);
        Collections.sort(tmp, new Comparator<Card>(){

            public int compare(Card a, Card b) {
                if ( a.getOrder() < b.getOrder() ) return -1;
                else if ( a.getOrder() > b.getOrder() ) return 1;
                else return 0;
            }
            
        });
        
        LinkedList<LinkedList<Card>> out = new LinkedList<LinkedList<Card>>();
        LinkedList<Card> top;
        
        //for (var i=0; i<tmp.length; i++){
        //    var card = tmp[i];
        for ( Card card : tmp ){
            
            if ( !out.isEmpty() ){
                top = out.pop();
            } else {
                top = new LinkedList<Card>();
            }
            if ( top.isEmpty() || top.peek().getRank() == card.getRank() ){
                top.push(card);
                out.push(top);
            } else {
                out.push(top);
                out.push(new LinkedList<Card>(Arrays.asList(new Card[]{card})));
            }
            //out.push(top);
        }
        
        LinkedList<LinkedList<Card>> out2 = new LinkedList<LinkedList<Card>>();
        while ( !out.isEmpty() ){
            LinkedList<Card> curr = out.pop();
            if ( curr.size() > 1 ){
                out2.push(curr);
            }
        }
        
        Collection<Collection<Card>> out3 = new LinkedList<Collection<Card>>(out2);
        return out3;
        
    }
    
    
    public static final Collection<Card> getUnscoringCards(Card[] cards){
        return getUnscoringCards(Arrays.asList(cards));
    }
    public static final Collection<Card> getUnscoringCards(Collection<Card> cardArr){
        Collection<List<Card>> runs = getRuns(cardArr);
        Collection<Collection<Card>> fifteens = getFifteens(cardArr);
        Collection<Collection<Card>> matches = getMatches(cardArr);
        
        LinkedList<Card> out = new LinkedList<Card>();
        LinkedList<Card> tmp = new LinkedList<Card>(cardArr);
        
        while ( !tmp.isEmpty() ){
            Card card = tmp.pop();
            boolean found = false;
            //for ( var i=0; i<runs.length; i++ ){
            for ( List<Card> run : runs ){
                if ( run.contains(card)){
                    found = true;
                    break;
                }
            }
            
            if ( found ){
                continue;
            }
            
            //for ( var i=0; i<fifteens.length; i++){
            for ( Collection<Card> fifteen : fifteens){
                if ( fifteen.contains(card) ){
                    found = true;
                    break;
                }
            }
            
            
            if ( found ){
                continue;
            }
            
            //for ( var i=0; i<matches.length; i++){
            for ( Collection<Card> match : matches){
                if ( match.contains(card)){
                    found = true;
                    break;
                }
            }
            
            if ( !found ){
                out.push(card);
            }
        }
        
        return out;
    }
    
    public static final boolean isValidHand(Card[] cards){
        return isValidHand(Arrays.asList(cards));
    }
    public static final boolean isValidHand(Collection<Card> cardsArr){
        return getUnscoringCards(cardsArr).isEmpty();
    }
    
    public static final int calculateScore(Card[] cards){
        return calculateScore(Arrays.asList(cards));
    }
    public static final int calculateScore(Collection<Card>cardsArr){
        int score = 0;
        
        score += (getFifteens(cardsArr).size() * 2);
        
        Collection<List<Card>> runs = getRuns(cardsArr);
        
        //for ( var i=0; i<runs.length; i++){
        for ( List<Card> run : runs){
            //var run = runs[i];
            score += run.size();
        }
        
        Collection<Collection<Card>> matches = getMatches(cardsArr);
        //for ( var i=0; i<matches.length; i++){
        for (Collection<Card> match : matches){
            //var match = matches[i];

            // n choose 2
            score += 2 * (rFact(match.size()) / 2 / rFact(match.size() - 2));
        }
        
        return score;
    }
    
    private static int rFact(int num)
    {
        if (num == 0)
          { return 1; }
        else
          { return num * rFact( num - 1 ); }
    }

    
}
