#AntWars
A third year Artificial Intelligence assignment, it was the first time I coded something that i played against. It was weird losing to a program i wrote. 
![SCREENSHOTS](https://raw.github.com/derekneil/antWars/master/antWarsQuadrantAndBoard.png "antWars quadrant and gameboard")
Table visualization of 2D array ant uses to remember state of current quadrant. Note size of array is determined by radius when ant is in a doorway, not by the radius at the edge or corner of the 11x11 board. Due to the restraint that the board was not going to change size, it made sense to remember it explicitly, instead of favoring a graphing approach that would involve some type of search and discovery algorithm.

###Logic
Added logic to prioritize (in order):
* eating or avoiding other ants nearby based on proximity
* getting closest food in field of view
* exploring closest unknown area / returning to uneaten food
* finding closest door to leave through

Other logic implemented includes:
* remembering state of current quadrant of the board
* remembering what door you entered a quadrant from
* moving away from your intended destination if heading towards it fails

###Basic Algorithm
```
board[][] = {unSeen}

while (1)
	update(board, fields) //update board with new fields visible, inDoorway, otherAnt, adjacent, foodClose
	if (inDoorway)
		clear(board)
		move = direction
		destination = null
		rememberDoorIcameThroughAndPlaceMe(board, move)
		sendMove(move)
		continue
	else if (otherAnt && adjacent)
		destination = otherAnt
	else if (foodClose) 
		destination = food
	else
		scan(board) //update hasUnseen, hasUneatenFood
		if (hasUnseen || hasUneatenFood)
			destination = closestUnseenOrFood()
		else
			destination = closestDoor(board)
	while (tries<4 && !goodMove)
		nextMoveTo(destination, move, tries) //updates move, or nothing if destination null
		if (otherAnt && moveWillMakeOtheAntAdjacent)
			goodMove = false
			move = stay
	direction = move
	updateMe(board, move)
	sendMove(move)
```

##Problems:
Doors were understood to operate on a random %10 probability of changing state, meaning that tracking their state was of little use, since it was only in our field of view for a limited time, and we only need the door to be open for two consecutive states.

Starting positions, first door taken, and actual distribution of food.
If two ants start in adjacent quadrants, eat all the food in their quadrant, then leave, depending on what door they exit through, with respect to the other player, could determine the winner of the game (assuming one ant doesn't eat the other before all the food is gone). This is because if ant1 eats all it's food, then enters the quadrant ant2 just finished eating all the food in (and just left), by the time ant1 realizes there's no food in the quadrant, ant2 will likely have already completed eating all the food in it's second quadrant, putting ant2 one quadrant of food ahead of ant1. Furthermore, even if ant1 immediately took the shortest route to the last possible remaining quadrant, by the time it got there, ant2 would be nearly done eating all the food in that quadrant.
Since neither ant can known what quadrant the other is in, and it is a random startup variable, having ants start in adjacent quadrants can only have two outcomes assuming both have equally efficient search algorithms, equal access to units of food, that one ant doesn’t eat the other ant, and neither is killed by a door:
1. the ant that doesn't go into the quadrant that was previously occupied by the other ant will win
2. if both ants don't go into the quadrant that was previously occupied by the other ant, it will most likely be a draw

If the actual distribution of food favored one ant over the other, then that ant would win in case 2 
(ie: one ant had access to 21 items of food across the two quadrants it happened to visit, and the other had access to 19 item of food across the two quadrants it visited).
