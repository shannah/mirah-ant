package ca.weblite.scriblets

import ca.weblite.scriblets.models.Player
import java.util.HashSet
import java.util.Arrays
import ca.weblite.scriblets.models.Card
import ca.weblite.scriblets.models.Board
import ca.weblite.scriblets.models.Board.*
import ca.weblite.scriblets.models.Move





class ComputerPlayer2 
	
  attr_accessor move:Move,
    player:Player
  
  
  def direction:Direction
    Direction(nil)
  end
  
  def findElligibleTiles:Tile[]
    a = 5
    b = 6
    x = lambda int do
      a+b
    end
    
    puts x
    return nil
    
    board = move.getBoardSnapshot
    used = move.getTilesUsed
    out = []
    if used.length > 0
      dir = move.getMainMoveDirection
      range = move.getRange
      # Case 1: Horizontal placement
      if dir == Direction.HORIZONTAL
        minCol = range[1]
        maxCol = range[3]
        row = range[0]
        if minCol > 0 and board.getTile(row, minCol-1).getCard == nil
          out.add(board.getTile(row,minCol-1))
        end
        if maxCol < board.getWidth-1 and
            board.getTile(row, maxCol+1).getCard == nil
          out.add(board.getTile(row,maxCol+1))
        end
        if used.length == 1
          # There is only one tile down so far, we can pick any direction
          # we want
          minRow=range[0]
          if minRow>0 and board.getTile(minRow-1,minCol).getCard == nil
            out.add(board.getTile(minRow-1,minCol))
          end
          if minRow<board.getHeight-1 and
              board.getTile(minRow+1,minCol).getCard == nil
            out.add(board.getTile(minRow+1,minCol))
          end
        end
      #Case 2: Vertical Placement
      else
        minRow=range[0]
        maxRow=range[2]
        col=range[1]
        if minRow > 0 and board.getTile(minRow-1,col).getCard == nil
          out.add(board.getTile(minRow-1,col))
        end
        if maxRow < board.getHeight-1 and 
            board.getTile(maxRow+1, col).getCard == nil
          out.add(board.getTile(maxRow+1, col))
        end
        if used.length == 1
          # There is only one tile down so far, we can pick any direction
          # we want
          if col>0 and board.getTile(minRow,col-1).getCard == nil
            out.add(board.getTile(minRow,col-1))
          end
          if col<board.getWidth-1 and
              board.getTile(minRow,col+1).getCard == nil
            out.add(board.getTile(minRow,col+1))
          end
        end
      end
      
    else    
      
    end
    
    out.toArray(Tile[0])
  end
  
  def findTileForCard card:Card
    move.getBoardSnapshot.getTiles.each do |tile:Tile|
       if tile.getCard != nil
         return
       end
         
       #move.placeCard(card, tile)
       
       
    end
    nil
  end
  
end
