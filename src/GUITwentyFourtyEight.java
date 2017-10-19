/*
 * 2048 GUI 
 * Ian Schank
 * EECS 1510
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class GUITwentyFourtyEight extends Application
{
	//Global declarations
	static int [][][] board = new int[4][4][11];
	Rectangle[][] recBoard = new Rectangle[4][4];
	Label[][] lblBoard = new Label[4][4];
	static int [][] moveScore = new int [2][11];
	static int undoCount = 0;
	static boolean extPlay = false;
	
	@Override
	public void start(Stage mainStage)
	{
		for(int r = 0; r < 4; r++)		//This loop will instantiate all the rectangles and labels in the arrays
			for(int c = 0; c < 4; c++)
			{
				recBoard[r][c] = new Rectangle();
				lblBoard[r][c] = new Label();
			}
		//things to declare for main user interface
		HBox bottomBox = new HBox(10);
		HBox topBox = new HBox(10);
		Button btExit = new Button("Exit");
		Button btHelp = new Button("Help");
		Button btUndo = new Button("Undo ("+undoCount+")");
		Button btSave = new Button("Save");
		Button btLoad = new Button("Load");
		Label lblMove = new Label("Move Count: "+moveScore[0][0]);
		Label lblScore = new Label("		Score: "+moveScore[1][0]);
		//main interface
		//put everything into it's proper pane
		bottomBox.getChildren().addAll(btExit,btHelp,btLoad,btSave,btUndo);
		bottomBox.setPadding(new Insets(10));
		topBox.getChildren().addAll(lblMove,lblScore);
		topBox.setPadding(new Insets(10));
		BorderPane pane = new BorderPane();
		pane.setStyle("-fx-background-color: rgb(186, 172, 159);");
		pane.setCenter(printGUI());
		pane.setTop(topBox);
		pane.setBottom(bottomBox);
		Scene scene = new Scene(pane,620,690);
		
		//Help menu interface
		Pane txtPane = new Pane(); 
		Text instructions = new Text("\n2048\n\nUse the arrow keys to move the tiles\nctrl + z "
				+"to undo up to ten moves\nalt + help for help menu\nalt + s to save game\n"
				+"alt + l to load a saved game\nalt + x to exit");
		instructions.setFont(Font.font(24));
		instructions.setTextAlignment(TextAlignment.CENTER);
		txtPane.getChildren().add(instructions);
		txtPane.setStyle("-fx-background-color: rgb(186, 172, 159);");
		Scene helpScene = new Scene(txtPane, 500, 300);
		Stage helpStage = new Stage();
		helpStage.setScene(helpScene);
		
		//Game over interface
		BorderPane endPane = new BorderPane();
		Label lblGameOver = new Label("Game Over");
		lblGameOver.setFont(Font.font(100));
		lblGameOver.setAlignment(Pos.CENTER);
		Label lblHigh = new Label("High Score: "+highScore());
		lblHigh.setFont(Font.font(30));
		lblHigh.setAlignment(Pos.CENTER);
		endPane.setStyle("-fx-background-color: rgb(250,124,95);");
		endPane.setCenter(lblGameOver);
		endPane.setBottom(lblHigh);
		Scene endScene = new Scene(endPane,580,660);
		Stage endStage = new Stage();
		endStage.setScene(endScene);
		
		//Win interface
		Label lblWin = new Label("Winner");
		lblWin.setFont(Font.font(100));
		lblWin.setAlignment(Pos.CENTER);
		BorderPane winPane = new BorderPane();
		winPane.setPadding(new Insets(10));
		Button btCont = new Button("Continue Playing");
		Button btQuit = new Button("Quit");
		winPane.setTop(lblWin);
		winPane.setCenter(btCont);
		winPane.setBottom(btQuit);
		winPane.setStyle("-fx-background-color: rgb(237,197,1);");
		Scene winScene = new Scene(winPane);
		Stage winStage = new Stage();
		winStage.setScene(winScene);
		
		//button handlers
		btExit.setOnAction(e -> { 
									if(highScore() < moveScore[1][0])
									{
										lblHigh.setText("New High Score: "+highScore());
										endPane.setBottom(lblHigh);
									MediaPlayer mediaPlayer = new MediaPlayer(new Media(new File("assets/TaDa.mp3").toURI().toString()));
									mediaPlayer.play();
									}
									endStage.show();
									PauseTransition pause = new PauseTransition(Duration.millis(2000));
									pause.setOnFinished(f -> 
									{
										System.exit(0);
									});
									pause.play();
									
								});
		btQuit.setOnAction(e -> {
									System.out.println("if");
									if(highScore() < moveScore[1][0])
									{
										lblHigh.setText("New High Score: "+highScore());
										endPane.setBottom(lblHigh);
										MediaPlayer mediaPlayer = new MediaPlayer(new Media(new File("assets/TaDa.mp3").toURI().toString()));
										mediaPlayer.play();
									}
									endStage.show();
									PauseTransition pause = new PauseTransition(Duration.millis(2000));
									pause.setOnFinished(f -> 
									{
										System.exit(0);
									});
									pause.play();
									
								});
		btHelp.setOnAction(e -> {helpStage.show();});
		btUndo.setOnAction(e -> {
									undo(); 
									lblMove.setText("Move Count: "+moveScore[0][0]);
									lblScore.setText("		Score: "+moveScore[1][0]);
									pane.setCenter(printGUI());
									btUndo.setText("Undo ("+undoCount+")");
								});
		btSave.setOnAction(e -> {save();});
		btLoad.setOnAction(e -> {
									load();
									lblMove.setText("Move Count: "+moveScore[0][0]);
									lblScore.setText("		Score: "+moveScore[1][0]);
									pane.setCenter(printGUI());
									btUndo.setText("Undo ("+undoCount+")");
								});
		btCont.setOnAction(e -> {extPlay = true; winStage.close();});
		
		placeRandom();
		pane.setCenter(printGUI());
		
		//key commands
		scene.setOnKeyPressed(e -> 
		{
			switch(e.getCode()) 		//this switch statement has cases for all key commands to easily select the correct input
			{
			case UP: upMethods(); break;
			case DOWN: downMethods(); break;
			case LEFT: leftMethods(); break;
			case RIGHT: rightMethods(); break;
			case Z: if(e.isControlDown()) undo();	    break;  //ctrl z for undo
			case H: if(e.isAltDown()) helpStage.show(); break;	//alt h for help
			case S: if(e.isAltDown()) save(); break;			//alt s for save
			case L: if(e.isAltDown()) break;					//alt l for load
			case X: if(e.isAltDown()) System.exit(0); break;	//alt x for exit
			default: break; 	//if any key/key combo other than these is pressed, nothing will happen
			}
			//after a key is typed, these four lines update all facets of the interface
			lblMove.setText("Move Count: "+moveScore[0][0]);	
			lblScore.setText("		Score: "+moveScore[1][0]);
			pane.setCenter(printGUI());
			btUndo.setText("Undo ("+undoCount+")");
			
			if(!extPlay && isWinner())	//this will check for a winner only if extPlay is false, meaning no win has already occurred
				winStage.show();
			
			if(isGameOver())
			{
				if(highScore() < moveScore[1][0])
				{
					lblHigh.setText("New High Score: "+highScore());
					endPane.setBottom(lblHigh);
					MediaPlayer mediaPlayer = new MediaPlayer(new Media(new File("assets/TaDa.mp3").toURI().toString()));
					mediaPlayer.play();
				}
				endStage.show();
				PauseTransition pause = new PauseTransition(Duration.millis(2000));
				pause.setOnFinished(f -> 
				{
					System.exit(0);
				});
				pause.play();
				
			}
		});
			
		mainStage.setTitle("2048");
		mainStage.setResizable(false);
		mainStage.setScene(scene);
		mainStage.show();
	} //end start
	
	public static void main(String[] args)
	{
		Application.launch(args);
	} //end main
	
	public static void placeRandom()
	{
		//This method will make a list of which spaces on the board are empty and assign on of them at random
		//a new tile of either value 2 or 4
		
		//Things to Declare:
		int[][] emptyList = new int [16][2];	//this array will serve as a running list of the empty slot
		int numEmpty = 0;						//this counter will tally the total empty slots 
		int randomSpot = 0;						//this number will be the 1-16, the randomly chosen spot
		int ranX = 0;
		int ranY = 0; 							//these will house the coordinates of the random spot to place into the board
		int newNum = 0;							//this will store the 2 or 4 to be placed on the board
		
		//check if spaces are empty and add those that are to a list
		//I will do this with a set of nested for loops to check a whole row them move down to the next one
		/*
		 *I need to draw the board and list so I don't get the columns and rows mixed up
		 *
		 *				board			 	list
		 *				  0  1  2  3	 	  0  1
		 *				0[ ][ ][ ][ ]		0[x][y]
		 *				1[ ][ ][ ][ ]		1[ ][ ]
		 *				2[ ][ ][ ][ ]		2[ ][ ]
		 *				3[ ][ ][ ][ ]		3[ ][ ]
		 * 									4[ ][ ]
		 * 				numEmpty is	the 	5[ ][ ]
		 *  			last occupied	    6[ ][ ]
		 *   			number				7[ ][ ]	
		 *   								8[ ][ ]
		 * 									9[ ][ ]
		 *  							   10[ ][ ]
		 *   							   11[ ][ ]	
		 * 								   12[ ][ ]
		 * 								   13[ ][ ]
		 *  							   14[ ][ ]
		 *   							   15[ ][ ]									
		 */
		
		
		for(int y = 0;y < 4;y++)
		{
			for(int x = 0;x < 4;x++)
			{
				if(board[x][y][0] == 0)
				{
					emptyList[numEmpty][0]=x;
					emptyList[numEmpty][1]=y;
					numEmpty++;							//by starting at zero and incrementing after the spot has been recorded, numEmpty can
				}	//end if							//possibly reach 16 but it will not go out of index because the list is filled first.
			}	//end inner loop
		}	//end outer loop
		
		//choose a random empty spot from the list
		randomSpot = (int)(Math.random()*numEmpty);		//this statement will generate a random int between 0 and the amount of empty spaces
		
		ranX = emptyList[randomSpot][0];				//Since I chosen the random spot from a list of all places that was numbered 0-15,
		ranY = emptyList[randomSpot][1];				//I now have to take that one dimensional numbering system and chance it back to 
														//the Cartesian system for the board array.
		
		//determine if placing a 2 or 4
		if((int)(Math.random()*10)==9)					//this statement will compare a random number 0-9
			newNum = 4;									//10% chance of the number being 9 and this clause running
		else											//90% chance of the number being !9 and this clause running
			newNum = 2;
		
		//place determined valued at random empty location
		board[ranX][ranY][0] = newNum;
		
		if(numEmpty==16)								//By adding this statement, it will place two random numbers the first time
			placeRandom();							//After the first time, there will never be 16 empty spaces again so it will
														//only place one new number from then on. This may not be the most orthodox solution,
														//but it works and it's only two lines of code.
			
	}	//end placeRandom

	public static boolean canMoveUp()
	{	//This method will determine if it is possible to move up, i.e. there are empty to be squeezed out or adjacent tiles that can be merged vertically
		for(int col = 0;col < 4;col++)
		{
			for(int row = 1;row < 4;row++)	//the check will start at the second row from the top because it will compare to the value above itself
			{
				if(board[row][col][0] != 0)	//finding a lone zero tells us no information about the possibility of a move 
				{
					if((board[row][col][0] == board[row-1][col][0])||board[row-1][col][0] == 0)	//the board can move up if the value
					{																	//above is the same (merge) or zero (squeeze out)
						moveScore[0][0]++;	//here I will increment the move because the only reason to do so is if a move is possible
						return true;
					}	//end inner if
				}	//end outer if
			}	//end inner loop
		}	//end outer loop
		return false;
	}	//end canMoveUp		

	public static void moveBoardUp()
	{	//this method will "pull" the tiles to the top, squeezing out empty space
		for(int col = 0; col < 4;col++)		
			for(int row = 0; row < 3;row++)	//row is always the topmost empty column, because I will create a new index variable to do the checking
				if(board[row][col][0] == 0)	//if the row already contains a value, it will be skipped
				{
					for(int index = (row+1); index < 4; index++)	//index will move down the column, away from the position of "row" looking for a non-zero number
						if(board[index][col][0] != 0)					//so if the spot that the index checks is zero, index will increment and check the next spot for a value
							{
								board[row][col][0] = board[index][col][0];	//once the index finds a non-zero value, t will move that value to the position of "row," which is the topmost empty spot
								board[index][col][0] = 0;					//then the index spot will be set to zero, since it has been "moved" to the top
								break;	//once a value has been found and moved up, the index loop has to be broken so row will increment							
							}			//and the next found value doesn't just get placed over the value in the top spot. (This was discovered through via trial and error)
				}
	}	//end moveBoardUp

	public static void mergeUp()
	{	//this method will merge adjacent tile and leave a zero in the lower spot, in main I will run moveBoard again to squeeze this out 
		for(int col = 0; col < 4;col++)	
			//picturing only one column at a time
			for(int row = 0; row < 3;row++)		//checking top down, since I am merging up
				if(board[row][col][0] == board[row+1][col][0])	//if the tile below is the same, that when a merge occurs
					{
						board[row][col][0] = board[row][col][0]*2;	//multiplying the current tile by two == adding the tiles together 
						board[row+1][col][0] = 0;	//the lower tile is set to zero because its value was moved up
						moveScore[1][0] += board[row][col][0];	//the value of the tiles added together, the same value in the top tile, is added to the score
					}
	}	//end mergeUp
	
	public static boolean canMoveDown()	
	{	//This method will determine if it is possible to move down, it is essentially the same as canMoveUp but inverted
		//all of the directional methods are structurally identical, the only differences are the add/subtract signs or the inversion of row and col
		//I may not comment as much for these because the comments for the three up methods describe the other nine if you replace "up" with the respective direction
		for(int col = 0;col < 4;col++)
		{
			for(int row = 2;row > -1;row--)	//the check will start at the second row from the bottom because it will compare to the value below itself
			{
				if(board[row][col][0] != 0)	//finding a lone zero tells us no information about the possibility of a move 
				{
					if((board[row][col][0] == board[row+1][col][0])||board[row+1][col][0] == 0)	//the board can move down if and only if the value
					{																	//above is the same (merge) or zero (squeeze out)
						moveScore[0][0]++;
						return true;
					}	//end inner if
				}	//end outer if
			}	//end inner loop
		}	//end outer loop
		return false;
	}	//end canMoveDown
	
	public static void moveBoardDown()
	{	//this method is essentially the same as moveUp, but the signs are reversed
		for(int col = 0; col < 4;col++)
			for(int row = 3; row > 0;row--)		//row will always be the bottom most value
				if(board[row][col][0] == 0)		
				{
					for(int index = (row-1); index > -1; index--)	//index will check the value above the row 
						if(board[index][col][0] != 0)					//if the index value is zero it will move up and check the next row
							{
								board[row][col][0] = board[index][col][0];
								board[index][col][0] = 0;
								break;	
							}
				}
	}	//end moveBoardUp

	public static void mergeDown()
	{
		for(int col = 0; col < 4;col++)
			//picturing only one column at a time
			for(int row = 3; row > 0;row--)
				if(board[row][col][0] == board[row-1][col][0])
					{
						board[row][col][0] = board[row][col][0]*2;
						board[row-1][col][0] = 0;
						moveScore[1][0] += board[row][col][0];
					}
	}	//end mergeDown

	public static boolean canMoveLeft()
	{	//checking/moving/merging left is the same as moving "up" horizontally,
		//since 0 is at the left of the the board. The methods for this direction will
		//be the same as they were for up, but row and col will be inverted.
		for(int row = 0;row < 4;row++)
		{
			for(int col = 1;col < 4;col++)
			{
				if(board[row][col][0] != 0)	
				{
					if((board[row][col][0] == board[row][col-1][0])||board[row][col-1][0] == 0)	
					{																	
						moveScore[0][0]++;
						return true;
					}	//end inner if
				}	//end outer if
			}	//end inner loop
		}	//end outer loop
		return false;
	}	//end canMoveLeft	

	public static void moveBoardLeft()
	{
		for(int row = 0; row < 4;row++)
			for(int col = 0; col < 3;col++)
				if(board[row][col][0] == 0)
				{
					for(int index = (col+1); index < 4; index++)
						if(board[row][index][0] != 0)
							{
								board[row][col][0] = board[row][index][0];
								board[row][index][0] = 0;
								break;	
							}
				}
	}	//end moveBoardLeft

	public static void mergeLeft()
	{
		for(int row = 0; row < 4;row++)
			//picturing only row column at a time
			for(int col = 0; col < 3;col++)
				if(board[row][col][0] == board[row][col+1][0])
					{
						board[row][col][0] = board[row][col][0]*2;
						board[row][col+1][0] = 0;
						moveScore[1][0] += board[row][col][0];
					}
	}	//end mergeLeft

	public static boolean canMoveRight()
	{	//down and right share the same relationship as left and up
		//these next three methods will resemble down, with inverted row and col
		for(int row = 0;row < 4;row++)	//start at the top
		{
			for(int col = 2;col > -1;col--)	//start at one from the right
			{
				if(board[row][col][0] != 0)	
				{
					if((board[row][col][0] == board[row][col+1][0])||board[row][col+1][0] == 0)	
					{																	
						moveScore[0][0]++;
						return true;
					}	//end inner if
				}	//end outer if
			}	//end inner loop
		}	//end outer loop
		return false;
	}	//end canMoveRight	

	public static void moveBoardRight()
	{
		for(int row = 0; row < 4;row++)
			for(int col = 3; col > 0;col--)	
				if(board[row][col][0] == 0)		
				{
					for(int index = (col-1); index > -1; index--)	 
						if(board[row][index][0] != 0)					
							{
								board[row][col][0] = board[row][index][0];
								board[row][index][0] = 0;
								break;	
							}
				}
	}	//end moveBoardRight

	public static void mergeRight()
	{
		for(int row = 0; row < 4;row++)
			//picturing only row column at a time
			for(int col = 3; col > 0;col--)
				if(board[row][col][0] == board[row][col-1][0])
					{
						board[row][col][0] = board[row][col][0]*2;
						board[row][col-1][0] = 0;
						moveScore[1][0] += board[row][col][0];
					}
	}	//end mergeRight

	public static void upMethods()
	{
		if(canMoveUp())
		{
			copyBoard();
			moveBoardUp();
			mergeUp();
			moveBoardUp();
			placeRandom();
			if(undoCount < 10)
				undoCount++;
		}
	}
	
	public static void downMethods()
	{
		if(canMoveDown())
		{
			copyBoard();
			moveBoardDown();
			mergeDown();
			moveBoardDown();
			placeRandom();
			if(undoCount < 10)
				undoCount++;
		}
	}
	
	public static void leftMethods()
	{
		if(canMoveLeft())
		{
			copyBoard();
			moveBoardLeft();
			mergeLeft();
			moveBoardLeft();
			placeRandom();
			if(undoCount < 10)
				undoCount++;
		}
	}
	
	public static void rightMethods()
	{
		if(canMoveRight())
		{
			copyBoard();
			moveBoardRight();
			mergeRight();
			moveBoardRight();
			placeRandom();
			if(undoCount < 10)
				undoCount++;
		}
	}
	
	public GridPane printGUI()
	{

		GridPane fourGrid = new GridPane();
		fourGrid.setAlignment(Pos.CENTER);		
		fourGrid.setHgap(18);
		fourGrid.setVgap(18);
		
		for(int row = 0; row < 4; row ++)
		{
			for(int col = 0; col < 4; col++)
			{
				recBoard[row][col].setFill(cellColor(row,col));
				recBoard[row][col].setWidth(132);
				recBoard[row][col].setHeight(132);
				recBoard[row][col].setArcWidth(20);
				recBoard[row][col].setArcHeight(20);
				
				if(board[row][col][0]==0)
					lblBoard[row][col].setText(" ");
				else
					lblBoard[row][col].setText(Integer.toString(board[row][col][0]));
				
				if(     board[row][col][0] < 100)
				   	 lblBoard[row][col].setFont(Font.font(90));
				else if(board[row][col][0] < 1000)
					 lblBoard[row][col].setFont(Font.font(70));
				else if(board[row][col][0] < 10000)
				   	 lblBoard[row][col].setFont(Font.font(50));
				else if(board[row][col][0] < 100000)
					 lblBoard[row][col].setFont(Font.font(40));
				
				if((board[row][col][0]==2)||(board[row][col][0]==4))
					lblBoard[row][col].setTextFill(Color.rgb(119,110,101));	
				else
					lblBoard[row][col].setTextFill(Color.WHITE);
				
				//fourGrid.add(new StackPane().getChildren().addAll(recBoard[row][col],lblBoard[row][col]), col, row);
				
			}
		}
		
		StackPane cellNo1 = new StackPane();
		cellNo1.getChildren().addAll(recBoard[0][0],lblBoard[0][0]);

		StackPane cellNo2 = new StackPane();
		cellNo2.getChildren().addAll(recBoard[0][1],lblBoard[0][1]);

		StackPane cellNo3 = new StackPane();
		cellNo3.getChildren().addAll(recBoard[0][2],lblBoard[0][2]);

		StackPane cellNo4 = new StackPane();
		cellNo4.getChildren().addAll(recBoard[0][3],lblBoard[0][3]);

		StackPane cellNo5 = new StackPane();
		cellNo5.getChildren().addAll(recBoard[1][0],lblBoard[1][0]);

		StackPane cellNo6 = new StackPane();
		cellNo6.getChildren().addAll(recBoard[1][1],lblBoard[1][1]);

		StackPane cellNo7 = new StackPane();
		cellNo7.getChildren().addAll(recBoard[1][2],lblBoard[1][2]);

		StackPane cellNo8 = new StackPane();
		cellNo8.getChildren().addAll(recBoard[1][3],lblBoard[1][3]);

		StackPane cellNo9 = new StackPane();
		cellNo9.getChildren().addAll(recBoard[2][0],lblBoard[2][0]);

		StackPane cellNo10 = new StackPane();
		cellNo10.getChildren().addAll(recBoard[2][1],lblBoard[2][1]);

		StackPane cellNo11 = new StackPane();
		cellNo11.getChildren().addAll(recBoard[2][2],lblBoard[2][2]);

		StackPane cellNo12 = new StackPane();
		cellNo12.getChildren().addAll(recBoard[2][3],lblBoard[2][3]);

		StackPane cellNo13 = new StackPane();
		cellNo13.getChildren().addAll(recBoard[3][0],lblBoard[3][0]);

		StackPane cellNo14 = new StackPane();
		cellNo14.getChildren().addAll(recBoard[3][1],lblBoard[3][1]);

		StackPane cellNo15 = new StackPane();
		cellNo15.getChildren().addAll(recBoard[3][2],lblBoard[3][2]);

		StackPane cellNo16 = new StackPane();
		cellNo16.getChildren().addAll(recBoard[3][3],lblBoard[3][3]);
		
		//The cell numbering scheme will go in reading order like this
		/*
		 *  ___ ___ ___ ___
		 * | 1 | 2 | 3 | 4 |
		 *  ___ ___ ___ ___
		 * | 5 | 6 | 7 | 8 |
		 *  ___ ___ ___ ___
		 * | 9 |10 |11 |12 |
		 *  ___ ___ ___ ___
		 * |13 |14 |15 |16 |
		 *  ___ ___ ___ ___
		 * 
		 * That was poorly drawn but it will give me a visual reference
		 */
		
		fourGrid.add(cellNo1, 0, 0);
		fourGrid.add(cellNo2, 1, 0);
		fourGrid.add(cellNo3, 2, 0);
		fourGrid.add(cellNo4, 3, 0);
		fourGrid.add(cellNo5, 0, 1);
		fourGrid.add(cellNo6, 1, 1);
		fourGrid.add(cellNo7, 2, 1);
		fourGrid.add(cellNo8, 3, 1);
		fourGrid.add(cellNo9, 0, 2);
		fourGrid.add(cellNo10, 1, 2);
		fourGrid.add(cellNo11, 2, 2);
		fourGrid.add(cellNo12, 3, 2);
		fourGrid.add(cellNo13, 0, 3);
		fourGrid.add(cellNo14, 1, 3);
		fourGrid.add(cellNo15, 2, 3);
		fourGrid.add(cellNo16, 3, 3);
		
		return fourGrid;
	}

	public static Color cellColor(int row, int col)
	{
		Color a;
		switch(board[row][col][0])
		{
			case 0:    a = Color.rgb(200,187,175); break;
			case 2:    a = Color.rgb(238,228,218); break;
			case 4:    a = Color.rgb(236,223,199); break;
			case 8:    a = Color.rgb(241,176,120); break;
			case 16:   a = Color.rgb(245,149,99); break;
			case 32:   a = Color.rgb(245,124,95); break;
			case 64:   a = Color.rgb(246,89,55); break;
			case 128:  a = Color.rgb(243,216,107); break;
			case 256:  a = Color.rgb(242,208,75); break;
			case 512:  a = Color.rgb(229,193,43); break;
			case 1024: a = Color.rgb(227,186,20); break;
			case 2048: a = Color.rgb(237,197,1); break;
			default:   a = Color.BLACK; break;
		}
		return a;
	}
	
	public static boolean isGameOver()
	{
		if(!canMoveUp() && !canMoveDown() && !canMoveLeft() && !canMoveRight())
			return true;
		else 
		{
		moveScore[0][0]--;	//Calling the canMove methods increments moveCount if one is true, so I have to compensate here
		return false;
		}
	}
	
	public static boolean isWinner()
	{
		for(int r = 0; r < 4; r++)					//This loop will instantiate all the rectangles and labels in the arrays
			for(int c = 0; c < 4; c++)
				if(board[r][c][0] == 2048)
					return true;
		return false;			
	}

	public static void copyBoard()
	{	//This method will copy the board, score, and move count into reserved variables
		//right before the move is made, in case the player wants to undo that move
		
			for(int depth = 9; depth >= 0; depth--) //moves all copies of the board over one spot
			{
				for(int col = 0; col < 4; col++)
					for(int row = 0; row < 4; row++)	//these loops will cycle through the board,
						board[row][col][depth+1] = board[row][col][depth];	//copying the board values to copyBoard
				
				moveScore[0][depth+1] = moveScore[0][depth];	//finally, I'll copy the score and move counter to reset them
				moveScore[1][depth+1] = moveScore[1][depth];
			}
	}

	public static void undo()
	{
		if(undoCount > 0)
		{
			for(int depth = 0; depth <= 9; depth++) //moves all copies of the board back one
			{
				for(int col = 0; col < 4; col++)
					for(int row = 0; row < 4; row++)	//these loops will cycle through the board,
						board[row][col][depth] = board[row][col][depth+1];	//copying the board values to copyBoard
				
				moveScore[0][depth] = moveScore[0][depth+1];
				moveScore[1][depth] = moveScore[1][depth+1];
			}
			undoCount--;
		}
	}

	public static void save()
	{
		try
		{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("assets/2048.dat"));
			out.writeObject(board);
			out.writeObject(moveScore);
			out.writeObject(undoCount);
			out.close();
			System.out.println("save");
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void load()
	{
		try
		{
			ObjectInputStream input = new ObjectInputStream(new FileInputStream("assets/2048.dat"));
			try
			{
				board = (int[][][])input.readObject();
				moveScore = (int[][])input.readObject();
				undoCount = (int)input.readObject();
				System.out.println("load");
			} catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			input.close();
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static int highScore()
	{
		int highScore = 0;
	
		try
		{
			ObjectInputStream input = new ObjectInputStream(new FileInputStream("assets/highScore.dat"));
			try
			{
				highScore = (int)input.readObject();
				input.close();
				if(moveScore[1][0] > highScore)
				{
					highScore = moveScore[1][0];
					
					try
					{
						ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream("assets/highScore.dat"));
						output.writeObject(highScore);
						output.close();
					} 
					catch (FileNotFoundException e)
					{
						e.printStackTrace();
					} 
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			} 
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
		} 
		catch (FileNotFoundException e1)
		{
			e1.printStackTrace();
		} 
		catch (IOException e1)
		{
			e1.printStackTrace();
		}	
		return highScore;
	}
	
}//end class





