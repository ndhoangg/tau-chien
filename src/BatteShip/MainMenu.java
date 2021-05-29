package BatteShip;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;

public class MainMenu implements ActionListener {
	private JFrame frame;

	private JLabel welcome; // nền khi bắt đầu game
	private ImageIcon introIcon; // nền khi vào game
	private JButton play; // kèm nền khi vào game

	private JLabel menu; // phần menu tiếp theo (gồm các thành phần bên dưới)
	private JButton exit;
	private JButton gamemode;
	private JButton start;
	private JButton highScore;
	private JButton sound;
	private JLabel showScore; // show điểm cao
	private JLabel[] score;
	private JButton resetScore;

	private JLabel select; // phần menu thứ 3, khi ấn start sẽ hiện ra để chọn số lượng tàu
	private JLabel ship[]; // mảng chứa các hình ảnh tàu
	private JButton numShip[];
	private JButton back, next;
	
	private Creator creator;
	private Clip clip;
	private static boolean isPlaySound;

	// khởi tạo Menu ban đầu
	public MainMenu(int w, int h, boolean playSound) {
		frame = new JFrame("Battle Ship");
		isPlaySound = playSound;
		frame.setIconImage(loadImage("/img/logo.png", 90, 90));
		frame.setSize(w, h);
		welcome = new JLabel();
		welcome.setSize(w, h);
		play = new JButton();
		play.setSize(30, 20);
		play.setActionCommand("Play");
		play.addActionListener(this);
		ImageIcon playIcon = new ImageIcon(loadImage("/img/play.jpg", 80, 50));
		play.setIcon(playIcon);
		play.setBounds(w / 2 - 40, 3 * h / 4, 80, 50);

		introIcon = new ImageIcon(loadImage("/img/Title.png", w, h));
		welcome.add(play);
		welcome.setIcon(introIcon);
		frame.add(welcome);

		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);
		playSound("/sound/sound.wav");
		if (!isPlaySound)
			clip.stop();
	}

	// menu chứa các lựa chọn: exit, start, sound, highScore,...
	public void menu2() {
		menu = new JLabel();
		menu.setSize(1120, 690);
		menu.setIcon(new ImageIcon(loadImage("/img/blue.png", 1120, 690)));
		// nút start
		start = new JButton("BATTLE");
		start.setFont(new Font("Arial", Font.PLAIN, 25));
		start.setBounds(485, 511, 120, 65);

		start.setActionCommand("start");
		start.addActionListener(this);
		start.setBackground(Color.decode("#D2EDFE"));
		start.setBorder(new LineBorder(Color.decode("#D2EDFE")));
		// nút exit
		exit = new JButton();
		exit.setBounds(804, 511, 78, 65);
		exit.setIcon(new ImageIcon(loadImage("/img/exitRight.png", 78, 65)));
		exit.setActionCommand("exit");
		exit.addActionListener(this);
		exit.setBackground(Color.decode("#D2EDFE"));
		exit.setBorder(new LineBorder(Color.decode("#D2EDFE")));
		// nút sound
		sound = new JButton();
		sound.setBounds(181, 511, 78, 65);
		if (isPlaySound) {
			sound.setIcon(new ImageIcon(loadImage("/img/musicOn.png", 78, 65)));
			sound.setActionCommand("sound on");
		} else {
			sound.setIcon(new ImageIcon(loadImage("/img/musicOff.png", 78, 65)));
			sound.setActionCommand("sound off");
		}
		sound.addActionListener(this);
		sound.setBackground(Color.decode("#D2EDFE"));
		sound.setBorder(new LineBorder(Color.decode("#D2EDFE")));
		// nút game mode
		gamemode = new JButton("Độ khó: Dễ");
		gamemode.setBounds(291, 511, 156, 65);
		gamemode.setFont(new Font("Arial", Font.PLAIN, 25));

		gamemode.setActionCommand("easy");
		gamemode.addActionListener(this);
		gamemode.setBackground(Color.decode("#D2EDFE"));
		gamemode.setBorder(new LineBorder(Color.decode("#D2EDFE")));
		// nút high score
		highScore = new JButton();
		highScore.setBounds(643, 511, 100, 65);
		highScore.setIcon(new ImageIcon(loadImage("/img/trophy.png", 100, 65)));
		highScore.setActionCommand("highScore off");
		highScore.addActionListener(this);
		highScore.setBackground(Color.decode("#D2EDFE"));
		highScore.setBorder(new LineBorder(Color.decode("#D2EDFE")));

		menu.add(sound);
		menu.add(start);
		menu.add(gamemode);
		menu.add(highScore);
		menu.add(exit);

		// tạo trước numShip tránh exception null pointer
		numShip = new JButton[4];
		next = new JButton();
		back = new JButton();
		for (int i = 1; i < 4; i++) {
			numShip[i] = new JButton();
			numShip[i].setText("1");
			numShip[i].setFont(new Font("Arial", Font.PLAIN, 20));
			numShip[i].setBackground(Color.white);
			numShip[i].setForeground(Color.red);
			numShip[i].setActionCommand("2");
			numShip[i].addActionListener(this);
		}
		numShip[1].setText("2");
		showScore = new JLabel();
		score = new JLabel[6];
		score[0] = new JLabel("");
		showScore.add(score[0]);
		showScore.setLayout(new GridLayout(7, 1));
		for (int i = 1; i < 6; i++) {
			score[i] = new JLabel("");
			showScore.add(score[i]);
		}
		resetScore = new JButton("RESET");
		resetScore.setActionCommand("reset");
		resetScore.addActionListener(this);
		resetScore.setBackground(Color.decode("#D2EDFE"));
		resetScore.setBorder(new LineBorder(Color.decode("#D2EDFE")));
		showScore.add(resetScore);
		showScore.setBounds(510, 180, 100, 180);
		showScore.setVisible(false);
		menu.add(showScore);
		// gỡ welcome, cài menu
		welcome.setVisible(false);
		frame.remove(welcome);
		frame.add(menu);
		menu3();
	}

	// menu chứa lựa chọn số lượng tàu
	public void menu3() {
		select = new JLabel();
		select.setSize(1120, 690);
		select.setIcon(new ImageIcon(loadImage("/img/blue.png", 1120, 690)));
		ship = new JLabel[4];

		next.setIcon(new ImageIcon(loadImage("/img/arrowRight.png", 50, 50)));
		back.setIcon(new ImageIcon(loadImage("/img/arrowLeft.png", 50, 50)));
		back.setBounds(70, 552, 50, 50);
		next.setBounds(990, 552, 50, 50);
		next.addActionListener(this);
		back.addActionListener(this);

		for (int i = 1; i < ship.length; i++) {
			ship[i] = new JLabel();
//			numShip[i] = new JButton();
			ship[i].setIcon(new ImageIcon(loadImage("/img/" + i + ".png", i * 100, 100)));
		}

		ship[1].setBounds(500, 180, 100, 100);
		ship[2].setBounds(500, 330, 200, 100);
		ship[3].setBounds(500, 480, 300, 100);
		numShip[1].setBounds(400, 200, 50, 50);
		numShip[2].setBounds(400, 350, 50, 50);
		numShip[3].setBounds(400, 500, 50, 50);

		for (int i = 1; i < ship.length; i++) {
			select.add(ship[i]);
			select.add(numShip[i]);
		}

		select.add(back);
		select.add(next);

//		menu.setVisible(false);
//		frame.remove(menu);
//		frame.add(select);
	}

	private void showHighScore() {
		showScore.setVisible(true);
//		URL url = MainMenu.class.getResource("/HighScore/highscore.txt");
//		File file = new File(url.getPath());
//		System.out.println(url.getPath());
		File file = new File("D:/high.txt");

		int[] A = new int[6];
		try {		
			file.createNewFile();
			Scanner scan = new Scanner(file);
			for (int i = 1; i < 6; i++) {
				A[i] = scan.nextInt();
			}
			scan.close();
		} catch (Exception e) {
			System.out.println("File not found");
		}

		score[0].setText("Điểm cao: ");
		score[0].setForeground(Color.decode("#EB5A37"));
		score[0].setFont(new Font("Arial", Font.PLAIN, 20));
		for (int i = 1; i < 6; i++) {
			score[i].setText("" + i + ". " + A[i]);
			score[i].setForeground(Color.decode("#EB5A37"));
			score[i].setFont(new Font("Arial", Font.PLAIN, 20));
		}
//		JOptionPane.showMessageDialog(frame, "show " + url.getPath());
	}

	private void resetHighScore() {
		String s = "0 0 0 0 0";
		try {
//			URL url = MainMenu.class.getResource("/HighScore/highscore.txt");
//			FileWriter fw = new FileWriter(url.getPath());
			FileWriter fw = new FileWriter("D:/high.txt");
			fw.write(s);
			fw.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private Image loadImage(String s, int w, int h) {
		BufferedImage i = null; // doc anh duoi dang Buffered Image
		try {
			i = ImageIO.read(MainMenu.class.getResource(s));
		} catch (Exception e) {
			System.out.println("Duong dan anh k hop le!");
		}

		Image dimg = i.getScaledInstance(w, h, Image.SCALE_SMOOTH); // thay doi kich thuoc anh
		return dimg;

	}

	private void playSound(String link) {
		try {
			URL url = this.getClass().getResource(link);
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
			clip = AudioSystem.getClip();
			clip.open(audioIn);
			clip.loop(Clip.LOOP_CONTINUOUSLY);
			clip.start();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if ("Play".equals(e.getActionCommand())) {
			menu2();
		}
		if ("exit".equals(e.getActionCommand())) {
			frame.setVisible(false);
			System.exit(0);
		}

		if ("sound on".equals(e.getActionCommand())) {
			sound.setIcon(new ImageIcon(loadImage("/img/musicOff.png", 78, 65)));
			sound.setActionCommand("sound off");
			isPlaySound = false;
			clip.stop();
		}

		if ("sound off".equals(e.getActionCommand())) {
			sound.setIcon(new ImageIcon(loadImage("/img/musicOn.png", 78, 65)));
			sound.setActionCommand("sound on");
			clip.start();
			clip.loop(Clip.LOOP_CONTINUOUSLY);
			isPlaySound = true;
		}

		if ("start".equals(e.getActionCommand())) {
			menu.setVisible(false);
			frame.remove(menu);
			frame.add(select);
			select.setVisible(true);
		}

		if ("highScore off".equals(e.getActionCommand())) {
			showHighScore();
			highScore.setActionCommand("highScore on");
		}
		
		if ("highScore on".equals(e.getActionCommand())) {
			showScore.setVisible(false);
			highScore.setActionCommand("highScore off");
		}
		
		if ("easy".equals(e.getActionCommand())) {
			gamemode.setText("Độ khó: Khó");
			gamemode.setActionCommand("hard");
		}

		if ("hard".equals(e.getActionCommand())) {
			gamemode.setText("Độ khó: Dễ");
			gamemode.setActionCommand("easy");
		}

		if (e.getSource() == numShip[1]) {
			String s = numShip[1].getText();
			if (s == "1")
				numShip[1].setText("2");
			if (s == "2")
				numShip[1].setText("3");
			if (s == "3")
				numShip[1].setText("2");
		}

		if (e.getSource() == numShip[2]) {
			String s = numShip[2].getText();
			if (s == "1")
				numShip[2].setText("2");
			if (s == "2")
				numShip[2].setText("3");
			if (s == "3")
				numShip[2].setText("1");
		}

		if (e.getSource() == numShip[3]) {
			String s = numShip[3].getText();
			if (s == "1")
				numShip[3].setText("2");
			if (s == "2")
				numShip[3].setText("3");
			if (s == "3")
				numShip[3].setText("1");
		}

		if (e.getSource() == back) {
			select.setVisible(false);
			frame.remove(select);
			menu.setVisible(true);
			frame.add(menu);
		}

		if (e.getSource() == next) {
			int n1 = Integer.parseInt(numShip[1].getText());
			int n2 = Integer.parseInt(numShip[2].getText());
			int n3 = Integer.parseInt(numShip[3].getText());
			this.clip.stop();
			creator = new Creator(1120, 690, n3, n2, n1, gamemode.getActionCommand(), isPlaySound);
			creator.back.setActionCommand("back");
			creator.back.addActionListener(this);
			frame.setVisible(false);
		}

		if ("back".equals(e.getActionCommand())) {
			creator.clip.stop();
			creator.frame.setVisible(false);
			
			creator = null;
			System.gc();
			frame.setVisible(true);
			if (isPlaySound) {
			clip.start();
			}
		}

		if ("reset".equals(e.getActionCommand())) {
			resetHighScore();
			showHighScore();
		}

	}

}
