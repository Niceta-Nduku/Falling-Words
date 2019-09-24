public class WordThread implements Runnable{

	private WordRecord word;
	private Controller con;

	WordThread(WordRecord word,Controller c){
		this.word = word;
		con = c;
	}

	public void run(){

		while (!c.gameEnded())//if the game is not over				
				if(paused)
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
				Thread.sleep(word.getSpeed());//it will drop the word at the rate 
			}
			catch(InterruptedException e){
				System.out.println ( "Exception: " + e.getMessage() );
			}
	}
}