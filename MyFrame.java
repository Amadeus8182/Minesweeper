import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;

public class MyFrame extends JFrame implements MouseListener, MouseMotionListener, ActionListener {

	JMenuBar mb;
	JMenu difficulty, file;
	JMenuItem exit, minimize;
	JMenuItem beginner, intermediate, expert;
	MyPanel panel = new MyPanel(this);
	Coord compCoords;

	public MyFrame() {
		panel.addMouseMotionListener(panel);
		panel.addMouseListener(panel);
		Timer timer = new Timer(1, panel);
		timer.start();

		this.add(panel);
		panel.resetGame();

		/* I don't even know anymore */
		mb = new JMenuBar();
		mb.addMouseListener(this);
		mb.addMouseMotionListener(this);

		setJMenuBar(mb);

		difficulty = new JMenu("Difficulty");
		file = new JMenu("File");

		beginner = new JMenuItem("Beginner");
		intermediate = new JMenuItem("Intermediate");
		expert = new JMenuItem("Expert");

		exit = new JMenuItem("Exit");
		minimize = new JMenuItem("Minimize");

		beginner.addActionListener(this);
		intermediate.addActionListener(this);
		expert.addActionListener(this);

		minimize.addActionListener(this);
		exit.addActionListener(this);

		difficulty.add(beginner);
		difficulty.add(intermediate);
		difficulty.add(expert);

		file.add(minimize);
		file.add(exit);

		mb.add(file);
		mb.add(difficulty);
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if(cmd.equals("Minimize")) {
			this.setState(JFrame.ICONIFIED);
		} else if(cmd.equals("Exit")) {
			this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}

		if(cmd.equals("Beginner")) {
			panel.setDifficulty(9, 9, 10);
		} else if(cmd.equals("Intermediate")) {
			panel.setDifficulty(16, 16, 40);
		} else if(cmd.equals("Expert")) {
			panel.setDifficulty(30, 16, 99);
		}
		panel.resetGame();
	}

	public void mouseReleased(MouseEvent e) {
		compCoords = null;
	}

	public void mousePressed(MouseEvent e) {
		compCoords = new Coord(e.getX(), e.getY());
	}

	public void mouseDragged(MouseEvent e) {
		Point m = MouseInfo.getPointerInfo().getLocation();
		setLocation((int)(m.getX()-compCoords.getX()), (int)(m.getY()-compCoords.getY()));
	}

	public void mouseExited(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
}
