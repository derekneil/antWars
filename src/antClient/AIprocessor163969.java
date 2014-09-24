package antClient;

import gameboard.Ant;
import gameboard.Field;
import gameboard.Gameboard;
import gameboard.GlobalGameboard;

/**
 * Implementation of Rand player - a sample automatic AntWars player. The Rand player makes in each round a random move,
 * uniformly choosing from a list of currently available moves. 
 * 
 * @author Marek Lipczak || lipczak@cs.dal.ca || www.cs.dal.ca/~lipczak/
 * @version 1.0 || 2009-02-10
 *
 */

 /*
  * Whenever you modify the code please describe your contribution here:
  *
 */

public class AIprocessor163969 extends AIprocessor
{
	public void startGameSignal(int position)
	{
		if(GlobalGameboard.doEcho) { System.out.println("Game started"); }
		
		/** Reset game state information */
		clearBoard();
		board[boardCenter][boardCenter] = ME;
		direction = "";
		
		boardHasUnseen = true;
		boardHasUneaten    = false;
		foodInFields       = false;
		otherAntInField    = false;
		otherAntIsAdjacent = false;
		inDoorway          = false;
		
		foodEaten = 0;
		destX = meX = boardCenter;
		destY = meY = boardCenter;
		distToDest = distToDestX = distToDestY = 0;
		
		otherAntX = 99;
		otherAntY = 99;
		
		turn = 1;
	}
	
	public void setCurrentPosition(String xmlPosition)
	{
		if(GlobalGameboard.doEcho) System.out.println("Current position: "+xmlPosition);
		//I don't care	
	}
	
	// Game state information -------------------------------------------------
	
	private final boolean DEBUG = true;
	
	private final int UNKNOWN = -1;
	private final int FLOOR = 0;
	private final int WALL = 1;
	private final int DOOR = 2;
	private final int DOORUSED = 3;
	private final int DOOROPEN = 4;
	private final int FOOD = 5;
	private final int OTHERANT = 8;
	private final int ME = 9;
	private final int boardSize = 11;
	private final int boardBuffer = 3;
	private final int boardArraySize = boardSize+2*boardBuffer;
	private final int boardCenter = (boardSize+2*boardBuffer)/2;
	private final int fieldRadius = 2;
	private int board[][] = new int[boardArraySize][boardArraySize];
	private int turn = 1;
	
	private boolean boardHasUnseen = true;
	private boolean boardHasUneaten = false;
	private boolean foodInFields = false;
	private boolean inDoorway = false;

	private int otherAntX = 99;
	private int otherAntY = 99;
	private boolean otherAntInField = false;
	private boolean otherAntIsAdjacent = false;
	private boolean avoidOtherAnt = false;
	
	
	private String direction = "";    //really this is just the last move
	private int foodEaten = 0;

	private int meX = boardCenter;
	private int meY = boardCenter;
	
	private int destX = meX;
	private int destY = meY;
	private int distToDestX = 0;
	private int distToDestY = 0;
	private int distToDest = 0;
	
	// Worker methods to keep everything in this class ------------------------
	
	/**
	 * update meX and meY coordinates based on move being made
	 */
	private void updateMe(String move) {
		if (DEBUG) { System.out.println("moved from meX:"+meX+" meY:"+meY); }
		// remember, meX and meY are indexes into 2D array
		// going N (up) means decrement meY
		// going S (down) means increment meY
		if      (move.equals("N")) { meY--; }
		else if (move.equals("E")) { meX++; }
		else if (move.equals("S")) { meY++; }
		else if (move.equals("W")) { meX--; }
		direction = move;

		if (DEBUG) { System.out.println("moved to meX:"+meX+" meY:"+meY); }
	}

	/** 
	 * update board absolute positions with fields that are relative to me 
	 * update inDoorway, foodInFields, OtherAntInField and isOtherAntAdjacent
	 */
	private void updateBoard(Field[][] fields) {
		int fieldCenterX = fields.length/2;
		int fieldCenterY = fields[fieldCenterX].length/2;
		int myId = fields[fieldCenterX][fieldCenterY].getAntId();
		inDoorway = false;
		foodInFields = false;
		otherAntInField = otherAntIsAdjacent = avoidOtherAnt = false;
		
		//clear location of otherAnt since scanBoard may not run for a while
		if (otherAntX<99 && otherAntY<99) {
			board[otherAntX][otherAntY] = FLOOR;
		}
		otherAntX = otherAntY = 99;
		
		for(int i=0; i<fields[0].length; i++) {
			for(int j=0; j<fields.length; j++) {
				
				int content = fields[j][i].getType();
				int distToX = j-fieldCenterX, distToY = i-fieldCenterY;
				int absDistToX = Math.abs(distToX), absDistToY = Math.abs(distToY);
				int absDistToXY = absDistToX + absDistToY;
				int x = meX+distToX;
				int y = meY+distToY;
				
				if (content == FLOOR) {
					
					int antId = fields[j][i].getAntId();
					if (antId !=-1 && antId != myId) {
						content = OTHERANT;
						otherAntInField = true;
						otherAntX = x;
						otherAntY = y;
						if (DEBUG) { System.out.println("OTHERANT otherAntX:"+otherAntX+" otherAntY:"+otherAntY); }
						// |43234|
						// |32123| //danger urgency
						// |21*12| // eat anything in 1
						// |32123| // avoid going into 1 if 2 is another ant
						// |43234|
						if (absDistToXY<2) {
							otherAntIsAdjacent = true;
							//intentional lack of distToXY < distToDest condition
							distToDest = absDistToXY; //this should be 1
							distToDestX = absDistToX;
							distToDestY = absDistToY;
							destX       = x;
							destY       = y;
							if (DEBUG) { System.out.println("OTHERANT destX:"+destX+" destY:"+destY); }
							
						}
						else if (absDistToXY<3) {
							avoidOtherAnt = true;
						}
					}
					
					if (fields[j][i].hasFood()) {
						foodInFields = true;
						content = FOOD;
						//this will favor first item seen to break ties
						if (absDistToXY < distToDest) {
							distToDest  = absDistToXY;
							distToDestX = absDistToX;
							distToDestY = absDistToY;
							destX       = x;
							destY       = y;
							if (DEBUG) { System.out.println("FOOD destX:"+destX+" destY:"+destY); }
						}
					}
				}
				
				//walls, and doors are already well defined
				if (content == DOOR) {
					if (board[x][y]==DOORUSED) {
						content = DOORUSED;
					}
					else if (fields[j][i].isDoorOpen()) {
						content = DOOROPEN;
					}
					if (absDistToXY==0) {
						inDoorway = true;
						if (DEBUG) { System.out.println("inDoorway"); }
					}
				}
				if (absDistToXY == 0) {
					content = ME;
					int lastContent = board[x][y];
					if (lastContent == FOOD) {
						foodEaten++;
					}
				}
				board[x][y] = content;
				
			}
		}
	}
	
	/**
	 * scan within walls of quadrant (does not include walls)
	 * update boardHasUnseen, boardHasUneaten and set closest
	 * distToDest, distToDestX, DistToDesty, destX and destY
	 * also remove information about otherAnt out of field of view
	 */
	private void scanBoard() {
		boardHasUneaten = boardHasUnseen = false;
		final int start = boardBuffer;
		final int end = boardBuffer + boardSize;
		for(int i=start; i<end; i++) { //scan within perimeter of walls and doors
			for(int j=start; j<end; j++) {
				
				int content = board[j][i];
				int distToX = j-meX, distToY = i-meY;
				int absDistToX = Math.abs(distToX), absDistToY = Math.abs(distToY);
				int absDistToXY = absDistToX + absDistToY;
				int x = meX+distToX;
				int y = meY+distToY;
				
				if (content == UNKNOWN) {
					boardHasUnseen = true;
				}
				else if (content == FOOD) {
					boardHasUneaten = true;
				}
				if (content == UNKNOWN || content == FOOD) {
					//if it's closer then the last one we saw
					if (absDistToXY < distToDest) { 
						distToDest  = absDistToXY;
						distToDestX = absDistToX;
						distToDestY = absDistToY;
						destX       = x;
						destY       = y;
						if (DEBUG) { System.out.println("UNKNOWN || FOOD destX:"+destX+" destY:"+destY); }
					}
				}
			}
		}
		
		//include the four possible door locations as unknowns
		findClosestDoor(UNKNOWN);
	}
	
	/**
	 * set destination as closest door we didn't enter through
	 */
	private void findClosestDoor(final int target) {
		final int doorLoc = Math.max(boardBuffer-1, 0);
		final int doorLoc2 = boardSize + boardBuffer;
		int possibleDoors[][] = { 	{ board[boardCenter][doorLoc2], boardCenter, doorLoc2 }, 
									{ board[doorLoc][boardCenter],  doorLoc,  boardCenter }, 
									{ board[boardCenter][doorLoc],  boardCenter,  doorLoc }, 
									{ board[doorLoc2][boardCenter], doorLoc2, boardCenter } };

		for (int i=0; i<possibleDoors.length; i++) {
			if (possibleDoors[i][0] == target || (target==DOOR)&&(DOOROPEN==possibleDoors[i][0]) ) {
				if (target==UNKNOWN) { boardHasUnseen = true; }
				int x = possibleDoors[i][1];
				int y = possibleDoors[i][2];
				int absDistToX = Math.abs(x-meX), absDistToY = Math.abs(y-meY);
				int absDistToXY = absDistToX + absDistToY;
				if (absDistToXY < distToDest) {
					distToDest  = absDistToXY;
					distToDestX = absDistToX;
					distToDestY = absDistToY;
					destX       = x;
					destY       = y;
					if (DEBUG) { System.out.println("UNKNOWN || DOOR destX:"+destX+" destY:"+destY); }
				}
			}
		}
	}
	
	/**
	 * figure next move to destX, destY
	 */
	private String nextMoveToDest(Gameboard gameboard) {
		
		String nextMove = "stay";
		if (distToDest==0) { return nextMove; }
		
		boolean goodMove = false;
		int tries = 0;
		
		Field[][] fields = gameboard.getFields();
		int centerX = fields.length/2;
		int centerY = fields[centerX].length/2;
		int moveX = 0;
		int moveY = 0;
			
		while (!goodMove && tries < 4) {
			moveX = moveY = 0;
			tries++;
			
			if (distToDest==1) { //just go there!
				moveX = destX - meX;
				moveY = destY - meY;
			}
			
			else { 
				//XXX bias direction if one will expose more unknown fields
				if (distToDestX==distToDestY) {
					if(tries==1) {
						moveX = (destX-meX >0)? 1:-1;
					}
					else if (tries==2) {
						moveY = (destY-meY >0)? 1:-1;
					}
					else if (tries==3) {
						// try going ortho to destination
						moveY = (destY-meY >0)? -1:1; //reversed 1:-1
					}
					else if(tries==4) {
						// try going backwards
						moveX = (destX-meX >0)? -1:1; //reversed 1:-1
					}
				}
				else if (distToDestX > distToDestY) {
					if(tries==1) {
						moveX = (destX-meX >0)? 1:-1;
					}
					else if (tries==2) {
						moveY = (destY-meY >0)? 1:-1;
					}
					else if (tries==3) {
						// try going ortho to destination
						moveY = (destY-meY >0)? -1:1; //reversed 1:-1
					}
					else if(tries==4) {
						// try going backwards
						moveX = (destX-meX >0)? -1:1; //reversed 1:-1
					}
				}
				else if (distToDestY > distToDestX) {
					if(tries==1) {
						moveY = (destY-meY >0)? 1:-1;
					}
					else if (tries==2) {
						moveX = (destX-meX >0)? 1:-1;
					}
					else if (tries==3) {
						// try going ortho to destination
						moveX = (destX-meX >0)? -1:1; //reversed 1:-1
					}
					else if (tries==4) {
						// try going backwards
						moveY = (destY-meY >0)? -1:1; //reversed 1:-1
					}
				}
			}
			
			//are there walls in the way?
			if( gameboard.isMoveable(centerX+moveX, centerY+moveY)) {
				goodMove = true;
				if (DEBUG) { System.out.println("isMoveable moveX:" + moveX + " moveY:" + moveY); }
			}
			else {
				if (DEBUG) { System.out.println("!isMoveable moveX:" + moveX + " moveY:" + moveY); }
			}
			
			//do we need to avoid another ant?
			if (goodMove && avoidOtherAnt) {
				// will Move Make Other Ant Adjacent?
				int absDistX=Math.abs(meX-otherAntX);
				int absDistY=Math.abs(meY-otherAntY);
				absDistX = (meX<otherAntX)? Math.abs(absDistX-moveX) : Math.abs(absDistX+moveX);
				absDistY = (meY<otherAntY)? Math.abs(absDistY-moveY) : Math.abs(absDistY+moveY);
				if (DEBUG) { System.out.println("avoidOtherAnt absDistX:"+absDistX+" absDistY:"+absDistY); }
				if (absDistX+absDistY < 2) {
					goodMove = false;
					if (DEBUG) { System.out.println("avoidOtherAnt goodMove = false"); }
				}
				else {
					if (DEBUG) { System.out.println("avoidOtherAnt goodMove = true"); }
				}
			}

		}
		
		if (goodMove) {
			if(moveY == -1) nextMove = "N";
			if(moveX ==  1) nextMove = "E";
			if(moveY ==  1) nextMove = "S";
			if(moveX == -1) nextMove = "W";
		}
		
		if (DEBUG) { System.out.println("nextMove: "+nextMove); }
		
		return nextMove;
	}

	private void clearBoard() {
		for(int i=0; i<board[0].length; i++) {
			for(int j=0; j<board.length; j++) {
				int debugCheck = board[j][i];
				board[j][i] = UNKNOWN;
			}
		}
	}

	/**
	 * mark door so you don't try to leave quadrant through the same one you entered through
	 * figure out where i am in the new quadrant
	 * @param direction
	 */
	private void markDoorICameThroughAndPlaceMe(String direction) {
		final int doorLoc = Math.max(boardBuffer-1, 0);
		final int doorLoc2 = boardSize + boardBuffer;
		if (direction.equals("N")) {
			board[boardCenter][doorLoc2] = DOORUSED;
			meX=boardCenter; meY=doorLoc2-1;
		}
		else if(direction.equals("E")) {
			board[doorLoc][boardCenter] = DOORUSED;
			meX=doorLoc+1; meY=boardCenter;
		}
		else if(direction.equals("S")) {
			board[boardCenter][doorLoc] = DOORUSED;
			meX=boardCenter; meY=doorLoc+1;
		}
		else if(direction.equals("W")) {
			board[doorLoc2][boardCenter] = DOORUSED;
			meX=doorLoc2-1; meY=boardCenter;
		}
		board[meX][meY] = ME;
		if (DEBUG) { System.out.println("markDoorICameThroughAndPlaceMe meX:"+meX+" meY:"+meY); }
	}
	
	//DEBUG ONLY --------------------------------------------------------------
	private void printFields(Field[][] fields) {
		for(int i=0; i<fields[0].length; i++) {
			System.out.print("|");
			for(int j=0; j<fields.length; j++) {
				String out = "_";
				int type = fields[j][i].getType();
				if (type==FLOOR && fields[j][i].hasFood()) { out = "F"; }
				else if (type==WALL) { out = "X"; }
				else if (type==DOOR) {
					if (fields[j][i].isDoorOpen()) { out = "O"; }
					else out = "D";
				}
				int ant = fields[j][i].getAntId();
				if (ant !=-1) { 
					out = "*";
					if (i!=2&&j!=2) { out = "A"; }
				}
				System.out.print(out);
			}
			System.out.println("|");
		}
		System.out.println();
	}
	
	private void printBoard() {
		for(int i=0; i<board[0].length; i++) {
			System.out.print("|");
			for(int j=0; j<board.length; j++) {
				String out = " ";
				int type = board[j][i];
				if (type == FLOOR) { out = "_"; }
				else if (type == FOOD) { out = "F"; }
				else if (type == WALL) { out = "X"; }
				else if (type == DOOR) { out = "D"; }
				else if (type == DOOROPEN) { out = "O"; }
				else if (type == DOORUSED) { out = "U"; }
				else if (type == OTHERANT) { out = "A"; }
				else if (type == ME) { out = "*"; }
				System.out.print(out);
			}
			System.out.println("|");
		}
		System.out.println();
	}
	//END DEBUG ONLY ----------------------------------------------------------
	
	// End Worker methods -----------------------------------------------------

	
	/**
	 * This is the main function of any automatic player.
	 * The player receives a new game board setting and it should make decision about the move.
	 * There are five available moves up <code>N</code>, right <code>E</code>, down <code>S</code>, left <code>W</code> and stay <code>stay</code>
	 * The move should be submitted to the server using {@link sendMove(String move)} function.
	 * 
	 * @param gameboard extraction of the game board that shows the fields around the player's ant
	 */
	public void setGameboard(Gameboard gameboard)
	{
		Field[][] fields =  gameboard.getFields();

		if (DEBUG) { 
			System.out.println("\n---- turn " + turn++ +"--------");
			printFields(fields);
		}

		//reset destination information, "direction" variable remembers the last state
		distToDest = distToDestX = distToDestY = destX = destY = 99;
		
		updateBoard(fields); //update inDoorway, foodInFields, OtherAntInField and isOtherAntAdjacent
		
		if (DEBUG) { printBoard(); }
		
		if (inDoorway) {
			
			clearBoard();
			markDoorICameThroughAndPlaceMe(direction);
			if (DEBUG) { printBoard(); }
			sendMove(direction);
			return;
		}
		
		else if (otherAntIsAdjacent || foodInFields) {
			//destination set in updateBoard()
		}

		else {
			scanBoard();
			
			if (!boardHasUnseen && !boardHasUneaten) {
				findClosestDoor(DOOR);
			}
		}
		
		String move = nextMoveToDest(gameboard);
		
		//do move
		updateMe(move);
		sendMove(move);
	}
	
	public void gotKilled()
	{
		if(GlobalGameboard.doEcho) System.out.println("I'm dead");
		isDead = true;		
	}
	
	public void endGameSignal(String msg)
	{
		if(GlobalGameboard.doEcho) System.out.println("Game ended");
		//I don't care
	}
}
