import java.util.Arrays;
import javax.swing.*;
import java.util.concurrent.locks.ReentrantLock;

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

	ReentrantLock lock = new ReentrantLock();

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

	public void fallWords(){
		//start dropping 
		for (int i=0;i<noWords;i++){
			new Thread (fallingWords[i]).start();
		}

	}

	public void runGame(){

		//create threads 
		for (int i=0;i<noWords;i++){
			fallingWords[i] = new WordThread(words[i],this);
		}
		ended = false;
		running = true;
		fallWords();
	}

	public boolean gameRunning(){
		return running;
	}

	public synchronized boolean scoresUpdated(){
		return newScore;
	}

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

	public synchronized void checkAnswer(String text){	
		
		for (WordRecord w:words){
			if(w.matchWord(text)){
				score.caughtWord(text.length());
				newScore = true;
				updateScores();
			}
		} 
		if(score.getTotal()>=totalWords){
			this.endGame();
		}

	}

	public synchronized void missed(){
		score.missedWord();
		newScore = true;
		//put lock
		updateScores();
		
		if(score.getTotal()>=totalWords){
			this.endGame();
		}
		modified = true;
	}

	public void pauseGame(){
		if(!ended && !paused )//if the game is already over it doen't matter
			paused = true;
	}

	public synchronized boolean gamePaused(){
		return paused;
	}

	public void continueGame(){
		if(!ended && paused)// need to do this to ensure that pause if not set to true before restart
			paused = false;
	}

	public synchronized void endGame(){
		ended = true;
		running = false;
		paused = false;

		for (WordThread w: fallingWords){
			w.reset();
		}

		score.resetScore();
		updateScores();
		modified = true;
	}

	public synchronized boolean gameEnded(){
		return ended;
	}
	
	public synchronized void resetState(){
		if (!modified) // 
			modified = true;
		else 
			modified = false;
	}

	public synchronized boolean isChanged(){
		return modified;
	}

	public void resetGame(){
		this.endGame();
		this.runGame();
	}

	class WordThread implements Runnable{

		private WordRecord word;
		private Controller c;

		WordThread(WordRecord word,Controller c){
			this.word = word;
			this.c = c;
		}

		public void reset(){
			word.resetPos();
		}

		public void run(){

			while (!c.gameEnded()){//if the game is not over				
					if(c.gamePaused())
						continue;// if the game was paused, do nothing
					else if(word.dropped()){ //there is a change to the game
						c.missed();//a word was missed
						word.resetWord();//refreshh the panel
						c.resetState();//let the contoller know the change has been made
					}
					else{
						word.drop(1); //drop the word by 1
						c.resetState();//let the contoller know there is a modification
					}			
				try{
					Thread.sleep(word.getSpeed()/10);//it will drop the word at the rate 
				}
				catch(InterruptedException e){
					System.out.println ( "Exception: " + e.getMessage() );
				}
			}
		}
	}

}