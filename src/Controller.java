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

	public AtomicInteger wordCount;

	ReentrantLock lock = new ReentrantLock();

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

		wordCount = new AtomicInteger(totalWords-noWords);
		System.out.println(wordCount.toString());
		newScore = false;
		fallingWords = new WordThread [noWords];

		paused = false;
		running = false;
		ended = true;
	}
	
	/**
	*  	Creates and starts the threads of words 
	*/ 

	public void fallWords(){
		for (int i=0;i<noWords;i++){
			fallingWords[i] = new WordThread(words[i],this);//create threads 
		}
		for (int i=0;i<noWords;i++){
			new Thread (fallingWords[i]).start();
		}

	}

	/**
	*  	Creates and starts the threads of words 
	*/ 
	public void runGame(){
		ended = false;
		running = true;
		updateScores();
		fallWords();
	}

	/**
	*  	Creates and starts the threads of words 
	*/ 
	public boolean gameRunning(){
		return running;
	}

	/**
	*  	Creates and starts the threads of words 
	*/ 
	public synchronized boolean scoresUpdated(){
		return newScore;
	}

	/**
	*  	Creates and starts the threads of words 
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
	*  	Creates and starts the threads of words 
	*/ 
	public synchronized void checkAnswer(String text){	
		
		for (WordRecord w:words){
			if(w.matchWord(text)){
				score.caughtWord(text.length());
				newScore = true;
				updateScores();
				wordCount.decrementAndGet();
				//System.out.println(wordCount.get()); //used to know what is in bank
			}
		} 

		if (score.getTotal()>=totalWords){
			this.endGame();
		}
	}

	/**
	*  	Creates and starts the threads of words 
	*/ 
	public synchronized void missed(){
		score.missedWord();
		newScore = true;
		//put lock
		updateScores();

		modified = true;	
		wordCount.decrementAndGet();// to know how many words left in bank

		if (score.getTotal()>=totalWords){
			this.endGame();
		}	
	}

	/**
	*  	Creates and starts the threads of words 
	*/ 
	public void pauseGame(){
		if(!ended && !paused )//if the game is already over it doen't matter
			paused = true;
	}

	/**
	*  	Creates and starts the threads of words 
	*/ 
	public synchronized boolean gamePaused(){
		return paused;
	}

	/**
	*  	Creates and starts the threads of words 
	*/ 
	public void continueGame(){
		if(!ended && paused)// need to do this to ensure that pause if not set to true before restart
			paused = false;
	}

	/**
	*  	Creates and starts the threads of words 
	*/ 
	public synchronized void endGame(){
		ended = true;
		newScore = true;
		modified = true;
		updateScores();
		score.resetScore();
		

		if(fallingWords!=null)
			for (WordThread w: fallingWords){
				try{
					w.terminate();
					w.join();
				}
				catch(InterruptedException e){
					System.out.println ( "Exception: " + e.getMessage() );
				}	
		};
		
		running = false;
		paused = false;
		wordCount.set(totalWords-noWords);

	}

	/**
	*  	Creates and starts the threads of words 
	*/ 
	public synchronized boolean gameEnded(){
		return ended;
	}
	
	/**
	*  	Creates and starts the threads of words 
	*/ 
	public synchronized void resetState(){
		if (!modified) // 
			modified = true;
		else 
			modified = false;
	}

	/**
	*  	Creates and starts the threads of words 
	*/ 
	public synchronized boolean isChanged(){
		return modified;
	}

	/**
	*  	Creates and starts the threads of words 
	*/ 
	public synchronized void endThread(WordThread w){
		try{
			w.join();
		}
		catch(InterruptedException e){
			System.out.println ( "Exception: " + e.getMessage() );
		}
		if (score.getTotal()>=totalWords){
			this.endGame();
		}	
		modified = true;
	}

	/**
	*  	Creates and starts the threads of words 
	*/ 
	public void resetGame(){
		this.endGame();
		this.runGame();
	}

	/**
	*  	Creates and starts the threads of words 
	*/ 
	public synchronized String getScores(){

		return ""+scoresTable[0].getText()+ " "+
		scoresTable[1].getText()+" "+
		scoresTable[2].getText();
	}

	/**
	*  	Creates and starts the threads of words 
	*/ 
	class WordThread extends Thread{

		private WordRecord word;
		private Controller c;
		private boolean destroyed = false;

		/**
		*  	Creates and starts the threads of words 
		*/ 
		WordThread(WordRecord word,Controller c){
			this.word = word;
			this.c = c;
		}

		/**
		*  	Creates and starts the threads of words 
		*/ 
		public void reset(){
			word.resetPos();
	
		}

		/**
		*  	Creates and starts the threads of words 
		*/ 
		public void terminate(){
			destroyed = true;
		}

		/**
		*  	Creates and starts the threads of words 
		*/ 
		public void run(){

			while (!c.gameEnded() && !destroyed){//if the game is not over	



					if(c.gamePaused())
						continue;// if the game was paused, do nothing

					else if (word.caught()){
						
						if (c.wordCount.get()>=0){								
							//System.out.println("caught: "+word.getWord());
							word.resetWord();	
						}
						else{
							//System.out.println("caught: "+word.caught());
							//System.out.println("crunning: "+this.isAlive());
							destroyed = true;
							word.resetWord();						
							c.endThread(this);
						}
					}

					else if(word.dropped()){ //there is a change to the game
						c.missed();
						if (c.wordCount.get()>=0){	
							System.out.println("dropped: "+word.getWord());
							word.resetWord();
						}

						else{
							System.out.println("dropped: "+word.dropped());
							System.out.println("drunning: "+this.isAlive());
							destroyed = true;
							word.resetWord();						
							c.endThread(this);
						}					
					}

					else{
						word.drop(1); //drop the word by 1
						c.resetState();//let the contoller know there is a modification
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