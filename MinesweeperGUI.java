import java.awt.*;
import javax.swing.*;

public class MinesweeperGUI {

	public static void main(String[] args) {

		Font f = new Font("sans-serif", Font.PLAIN, 12);
		UIManager.put("Menu.font", f);
		UIManager.put("MenuItem.font", f);
		UIManager.put("MenuBar.font", f);

		MyFrame frame = new MyFrame();
		frame.setUndecorated(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);
	}

}

