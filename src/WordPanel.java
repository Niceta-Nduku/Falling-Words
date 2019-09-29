import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CountDownLatch;

import javax.swing.JButton;
import javax.swing.JPanel;

public class WordPanel extends JPanel implements Runnable {

		public static volatile boolean paused;
		private WordRecord[] words;//shared
		private int noWords;
		private int maxY;
		private Controller c;
		private boolean over = false;
	
		WordPanel(WordRecord[] words, int maxY, Controller c) {
			this.words=words; //will this work? lol no
			noWords = words.length;
			paused = false; // game has been paused
			this.maxY=maxY;	
			this.c = c;	

		}

		public void paintComponent(Graphics g) {
		    int width = getWidth();
		    int height = getHeight();
		    g.clearRect(0,0,width,height);
		    g.setColor(Color.red);
		    g.fillRect(0,maxY-10,width,height);

		    g.setColor(Color.black);
		    g.setFont(new Font("Helvetica", Font.PLAIN, 26));
	  
	  		if(c.gameRunning()){
				for (int i=0;i<noWords;i++){//for each word	    	

					g.drawString(words[i].getWord(),words[i].getX(),words[i].getY());
				}
			}
			else {
				g.drawString("Click Start",400,height/2);
				g.drawString(c.getOutcome(),365,320);
				g.drawString(c.getScores(),250,400);
			}
			
		}
   
		
		public void run() {
			while (!over){//if the game is not over				
				if(paused)
					continue;// if the game was paused, do nothing
				else if(c.isChanged()){ //there is a change to the game
					repaint();//refreshh the panel
					c.resetState(false);//let the contoller know the change has been made
				}	
			}			
			try{
				Thread.sleep(100);
			}
			catch(InterruptedException e){
				System.out.println ( "Exception: " + e.getMessage() );
			}
		}

		public void reset(){
			over = true;
		}
	}


