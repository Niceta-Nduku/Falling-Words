import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CountDownLatch;

import javax.swing.JButton;
import javax.swing.JPanel;

public class WordPanel extends JPanel implements Runnable {
		public static volatile boolean done;
		public static volatile boolean paused;
		private WordRecord[] words;
		private int noWords;
		private int maxY;
	
		WordPanel(WordRecord[] words, int maxY) {
			this.words=words; //will this work?
			noWords = words.length;
			done =false; // game is over
			paused = false; // game has been paused
			this.maxY=maxY;		
		}

		public void paintComponent(Graphics g) {
		    int width = getWidth();
		    int height = getHeight();
		    g.clearRect(0,0,width,height);
		    g.setColor(Color.red);
		    g.fillRect(0,maxY-10,width,height);

		    g.setColor(Color.black);
		    g.setFont(new Font("Helvetica", Font.PLAIN, 26));
		   //draw the words
		   //animation must be added 

		    if(done == false){
				for (int i=0;i<noWords;i++){//for each word	    	
				    	//g.drawString(words[i].getWord(),words[i].getX(),words[i].getY());
				    	
			    	if (words[i].getY()>=maxY){
			    		words[i].resetWord();
			    		g.drawString(words[i].getWord(),words[i].getX(),words[i].getY());
			    	}
			    	else
			    	  g.drawString(words[i].getWord(),words[i].getX(),words[i].getY());
				}
			}

			else
				g.drawString("Click Start",width/2-50,height/2);

			run();

		}
   
		
		public void run() {
			//add in code to animate this
			if(paused==false)
				for (WordRecord word: words){
					if(! word.dropped())
						word.drop(word.getSpeed());
			};
	
			this.revalidate();
			this.repaint();

			try{
				Thread.sleep(200);
			}
			catch(InterruptedException e){
				System.out.println ( "Exception: " + e.getMessage() );
			}
		}
	}


