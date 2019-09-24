import java.util.Arrays;

public class Controller {

	private volatile boolean ended;  //must be volatile
	private volatile boolean paused;
	private volatile boolean running;
	private volatile boolean modified;
	private WordThread[] fallingWords;
	private int noWords;
	private final int totalWords;

	public Controller(){
		//when created, nothing should happed just yet.
		noWords= WordApp.noWords;
		totalWords = WordApp.totalWords;
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
			fallingWords[i] = new WordThread(WordApp.words[i],this);
		}
		running = true;
		fallWords();
	}

	public boolean gameRunning(){
		return running;
	}

	public synchronized void checkAnswer(String text){	
		
		int index = Arrays.asList(WordApp.words).indexOf(text);

		if(WordApp.words[index].matchWord(text)){
			WordApp.score.caughtWord(text.length());
		}

	}

	public synchronized void missed(){
		WordApp.score.missedWord();
		if(WordApp.score.getTotal()>totalWords){
			ended = true;
		}
	}

	public void pauseGame(){
		if(!ended)//if the game is already over it doen't matter
			paused = true;
	}

	public synchronized boolean gamePaused(){
		return paused;
	}

	public void continueGame(){
		if(!ended)// need to do this to ensure that pause if not set to true before restart
			paused = true;

	}

	public void endGame(){
		ended = true;
		running = false;
		WordApp.score.resetScore();
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
		ended = true;
		WordApp.score.resetScore();
		runGame();
	}

}