import java.util.Arrays;
import javax.swing.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;

public class Controller {

	private volatile boolean ended;  
	private volatile boolean paused;
	private volatile boolean running;
	private volatile boolean modified;
	private volatile boolean newScore;

	private WordThread[] fallingWords;

	private final int noWords;
	private final int totalWords;
	private final WordRecord[] words;

	static JLabel[] scoresTable;
	private static Score score;

	private AtomicInteger threadCount = new AtomicInteger(0);//to see how many words have dropped;

	ReentrantLock lock = new ReentrantLock();

	String outcome;

	/**
	* This is the constructor for the Controller class. 
	* @param noWords the total number of falling on the screen
	* @param totalWords the total words to fall over the entire game
	* @param words the array of words falling at an instance
	* @param score This is the score object for the game 
	*/ 
	public Controller(int noWords, int totalWords, WordRecord[] words, Score score){
		//when created, nothing should happed just yet.
		this.noWords= noWords;
		this.totalWords = totalWords;
		this.words = words;
		this.score = score;
		
		newScore = false;
		fallingWords = new WordThread [noWords];

		paused = false;
		running = false;
		ended = true;
	}
	
	/**
	*  	Creates and starts the threads of words 
	*/ 

	public void dropWords(){
		modified = true;
		for (int i=0;i<noWords;i++){
			fallingWords[i] = new WordThread(words[i],this);//create threads 
		}

		for (int i=0;i<noWords;i++){

			new Thread (fallingWords[i]).start();
		}
		
	}

	/**
	* Increments for each word falling.
	*/

	public synchronized void countThreads(){
		lock.lock();
		try{
			threadCount.incrementAndGet();
		}
		finally{
			lock.unlock();
		}
		// System.out.print(": "+ threadCount); // can also be printed in the threads
	}

	/**
	*  	Starts the game.
	*/ 
	public void runGame(){

		threadCount.set(0);

		score.resetScore();
		newScore = true;
		updateScores();

		ended = false;
		running = true;
		//System.out.println("RUNNING");

		dropWords();
		
	}

	/**
	*  	@return the running state of the game
	*/ 
	public boolean gameRunning(){
		return running;
	}

	/**
	*  	@return a boolean of the state of the scores. true if they are updated, false if not
	*/ 
	public synchronized boolean scoresUpdated(){
		return newScore;
	}

	/**
	*  	This updates the score label. 
	*	Used a lock to ensure thread safety 
	*/ 
	public void updateScores(){
		lock.lock();
		try{
			if (newScore){
				
				scoresTable[0].setText("Caught: " + score.getCaught() + "    ");
				scoresTable[1].setText("Missed:" + score.getMissed()+ "    ");
				scoresTable[2].setText("Score:" + score.getScore()+ "    ");

			}	
			newScore = false;
		}
		finally{
			lock.unlock();
		}

	}

	/**
	*  	Checks the user input and matches the text. 
	*	Score will be updated.
	*/ 
	public synchronized void checkAnswer(String text){	
		
		for (WordRecord w:words){
			if(w.matchWord(text)){
				score.caughtWord(text.length());
				newScore = true;
				updateScores();

			}
		} 
	}

	/**
	*  	Increased the missed word count
	*	Score will be updated.
	*/
	public synchronized void missed(){
		score.missedWord();
		newScore = true;
		//put lock
		updateScores();

		modified = true;
	}

	/**
	*  	Pauses the game if the game is not over or already paused
	*/ 
	public void pauseGame(){
		if(!ended && !paused )//if the game is already over it doen't matter
			paused = true;
	}

	/**
	*	true if paused, false if not.
	*  	@return the pause state
	*/ 
	public synchronized boolean gamePaused(){
		return paused;
	}

	/**
	*  	Continues the game if paused or not ended 
	*/ 
	public void continueGame(){
		if(!ended && paused)// need to do this to ensure that pause if not set to true before restart
			paused = false;
	}

	/**
	*  	Ends the game
	*	The scores table is updated to reflct the final scores and the score is reset
	*	All running threads are terminated.
	*/ 
	public synchronized void endGame(){
		
		ended = true;
		newScore = true;
		modified = true;
		updateScores();
		
		if (score.getMissed() == 1)
			outcome = "You were so close o_o";
		else if (score.getMissed() == totalWords)
			outcome = "Did you even try??";
		else if(score.getCaught() == totalWords)
			outcome = "You won!! :D";
		else 
			outcome = "Better next time :'( \n";

		score.resetScore();
		newScore = true;

		if(fallingWords!=null)
			for (WordThread w: fallingWords){
				try{
						w.reset();
						if (w.isAlive()){
							w.terminate();
							w.join();
						}										
				}
				catch(InterruptedException e){
					System.out.println ( "Exception: " + e.getMessage() );
				}	
		};
		
		running = false;
		paused = false;
		//System.out.println("ENDED");
	}

	/**
	*  	@return the end state 
	*/ 
	public synchronized boolean gameEnded(){
		return ended;
	}
	
	/**
	*  	This sets the modified boolean according to the parameter
	*	@param m the boolean to be set 
	*/ 
	public synchronized void resetState(boolean m){
		if (m) // 
			modified = true;
		else 
			modified = false;
	}

	/**
	*  	@return the modified state
	*/ 
	public synchronized boolean isChanged(){
		return modified;
	}

	/**
	*	This ends a thread 
	*  	@param w the word Thread to be joined
	*/ 
	public synchronized void endThread(WordThread w){

		try{
			w.join();
		}
		catch(InterruptedException e){
			System.out.println ( "Exception: " + e.getMessage() );
		}
		modified = true;

		if (score.getTotal()>=totalWords){
			this.endGame();
		}
	}

	/**
	*  	Resets a game
	*/ 
	public void resetGame(){
		this.endGame();
		this.runGame();
	}

	/**
	*	This returns a string version of the scores
	*  	@return String version of score table 
	*/ 
	public synchronized String getScores(){

		String s = ""+scoresTable[0].getText()+ " "+
		scoresTable[1].getText()+" "+
		scoresTable[2].getText();

		return s;	
	}

	/**
	*	This is a message to be displayed based on the users performance
	* 	@return the outcome of the game. 
	*/ 
	public synchronized String getOutcome(){
		if (outcome!=null)
			return outcome;
		return " ";	
	}


	/**
	*  	This is the WordThread class
	*	@extends Thread
	*/ 
	class WordThread extends Thread{

		private WordRecord word;
		private Controller c;
		private boolean destroyed = false;


		/**
		*  	A new WordThread
		*	@param word WordRecord
		*	@param c controller
		*/ 
		WordThread(WordRecord word,Controller c){
			this.word = word;
			this.c = c;
		}

		/**
		*  	Resets the position of the word
		*/ 
		public void reset(){
			word.resetPos();
	
		}

		/**
		*  	resets the position of the word and sets destroyed to true
		*/ 
		public void terminate(){
			word.resetPos();
			destroyed = true;
			c.resetState(true);
		}

		/**
		*  	Runs the thread if the thread is not destroyed or the game is not over.
		*/ 
		public void run(){

			while (!c.gameEnded() && !destroyed){//if the game is not over	
					int count = c.threadCount.get();
					if(c.gamePaused())
						continue;// if the game was paused, do nothing
					else if (word.caught()){							
						//System.out.println(":"+count); //current count of threads

						if (((totalWords) - count <= c.noWords)){//if the word count has been reached
							
							destroyed = true;
							this.reset();						
							c.endThread(this);
						}
						else{
							word.resetWord();	
							c.countThreads(); // if a new word is added increase
						}
					}

					else if(word.dropped()){ //there is a change to the game
						
						//System.out.println(":"+count);
						if (((totalWords) - count <= c.noWords)){
	
							destroyed = true;
							this.reset();
							c.missed();						
							c.endThread(this);
						}

						else{
							c.missed();
							word.resetWord();
							c.countThreads();
						}
					}

					else{
						word.drop(1); //drop the word by 1
						c.resetState(true);//let the contoller know there is a modification
					}			
				try{
					Thread.sleep(word.getSpeed()/20);//it will drop the word at the rate 
				}
				catch(InterruptedException e){
					System.out.println ( "Exception: " + e.getMessage() );
				}
			}
		}
	}

}