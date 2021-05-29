package BatteShip;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class PlayGame extends Container implements ActionListener {
	private JFrame frame;
	private SmallMap playerMap; // bản đồ của người chơi -> máy bắn trên này
	private SmallMap computerMap; // bản đồ của computer -> người chơi bắn trên này
	private JPanel panel1; // panel1 chứa 2 bản đồ
	private JPanel panel2; // panel2 chứa thông tin chơi (turn,...)
	private Container cn; // cn chứa panel1 và panel2
	private JLabel turnPlayer; // hiển thị turn của player
	private JLabel turnComputer;
	private JLabel turnP, turnC, curP, curC, hitP, hitC, pointP, pointC, ratioP, ratioC, shipDeadP, shipDeadC;
	private JButton back, backIgnore; // quay về menu khi win/lose

	private ImageIcon hit; // bắn trúng

	private static int playerHit = 0, computerHit = 0; // số điểm bắn trúng hiện tại
	private static int sumPoint; // tổng số điểm ship trên mỗi map
	private static boolean isPlayer; // turn của player hay của computer
	private static boolean[][] markP, markC; // markP : đánh dấu điểm đã bắn trên computerMap, ngược lại với markC
	private static boolean isHard; // chế độ khó
	private static Queue<String> Q = new LinkedList<>();

	private static ArrayList<String> A; // ứng với bản đồ của máy chơi
	private static ArrayList<String> B; // ứng với bản đồ của người chơi

	private static int currentPlayer = 0; // số điểm đã bắn của player
	private static int currentComputer = 0;
	private static int playerPoint = 0, computerPoint = 0, shipDeadByPlayer = 0, shipDeadByComputer = 0;
	private Clip clip;
	private static boolean isPlaySound;
	
	public PlayGame(int w, int h, SmallMap player, SmallMap computer, boolean gamemode, ArrayList<String> pl,
			ArrayList<String> cm, boolean playSound) {
		super();
		isPlaySound = playSound;
		if (gamemode)
			isHard = true;
		else
			isHard = false;
		A = pl;
		B = cm;

		// add map và thông tin
		panel1 = new JPanel();
		playerMap = player;
		computerMap = computer;

		panel1.setLayout(new GridLayout(1, 2, 20, 10));
		panel1.add(computerMap);
		panel1.add(playerMap);

		panel1.setBounds(0, 120, w - 15, h - 160);

		// tao khung
//		SwingConstants.CENTER
		panel2 = new JPanel();
		panel2.setLayout(new GridLayout(1, 2));
		turnPlayer = new JLabel();
		turnComputer = new JLabel();
		turnP = new JLabel("", SwingConstants.CENTER);
		turnC = new JLabel("", SwingConstants.CENTER);
		curP = new JLabel("", SwingConstants.CENTER);
		curC = new JLabel("", SwingConstants.CENTER);
		hitP = new JLabel("", SwingConstants.CENTER);
		hitC = new JLabel("", SwingConstants.CENTER);
		pointP = new JLabel("", SwingConstants.CENTER);
		pointC = new JLabel("", SwingConstants.CENTER);
		ratioP = new JLabel("", SwingConstants.CENTER);
		ratioC = new JLabel("", SwingConstants.CENTER);
		shipDeadP = new JLabel("", SwingConstants.CENTER);
		shipDeadC = new JLabel("", SwingConstants.CENTER);
		backIgnore = new JButton("Quay về Menu");
		backIgnore.setActionCommand("backIgnore");
		backIgnore.setBackground(Color.decode("#D2EDFE"));
		backIgnore.setBorder(new LineBorder(Color.decode("#D2EDFE")));
		backIgnore.addActionListener(this);
		turnPlayer.setLayout(new GridLayout(3, 2));
		turnComputer.setLayout(new GridLayout(3, 2));

		turnPlayer.add(turnP);
		turnPlayer.add(curP);
		turnPlayer.add(hitP);
		turnPlayer.add(ratioP);
		turnPlayer.add(shipDeadP);
		turnPlayer.add(pointP);

		turnComputer.add(turnC);
		turnComputer.add(curC);
		turnComputer.add(hitC);
		turnComputer.add(ratioC);
		turnComputer.add(shipDeadC);
//		turnComputer.add(pointC);
		turnComputer.add(backIgnore);
		panel2.add(turnPlayer);
		panel2.add(turnComputer);

		turnPlayer.setBackground(Color.decode("#D2EDFE"));
		turnPlayer.setOpaque(true);
		turnComputer.setBackground(Color.decode("#D2EDFE"));
		turnComputer.setOpaque(true);
		init();
		update();
		panel2.setSize(w, 120);
		addAction(); // tạo listener trên button
		panel1.setBackground(Color.decode("#D2EDFE"));
		panel1.setOpaque(true);
		this.add(panel2);
		this.add(panel1, "North");
		this.setSize(w, h);

		// tạo image icon
		hit = new ImageIcon(loadImage("/img/hit.png", w / 20, 56));
		back = new JButton();

		frame = new JFrame("Battle Ship");
		frame.setIconImage(loadImage("/img/logo.png", 90, 90));
		frame.add(this);
		frame.setSize(w, h);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
		playSound("/sound/sound.wav");
		if (!isPlaySound) clip.stop();
	}

	private void init() {
		int cnt = 0;
		markP = new boolean[11][11];
		markC = new boolean[11][11];
		for (int i = 1; i <= 10; i++) {
			for (int j = 1; j <= 10; j++) {
				if (playerMap.isShip[i][j]) {
					cnt++;
				}
				markP[i][j] = false;
				markC[i][j] = false;
			}
		}
		sumPoint = cnt;
		playerHit = 0;
		computerHit = 0;
		isPlayer = true;
		playerPoint = 0;
		computerPoint = 0;
		currentPlayer = 0;
		currentComputer = 0;
		shipDeadByPlayer = 0;
		shipDeadByComputer = 0;
	}

	private void update() {
		if (currentComputer != 0 && currentPlayer != 0) {
			playerPoint = playerHit * 100 + shipDeadByPlayer * 1000 + ((playerHit * 100) / currentPlayer) * 100;
			computerPoint = computerHit * 100 + shipDeadByComputer * 1000
					+ ((computerHit * 100) / currentComputer) * 100;
		}

		turnP.setText("Player");
		curP.setText("Số điểm đã bắn: " + currentPlayer);
		hitP.setText("Số điểm bắn trúng: " + playerHit + "/" + sumPoint);
		if (currentPlayer != 0) {
			ratioP.setText("Tỷ lệ bắn trúng: " + ((playerHit * 100) / currentPlayer) + " %");
		} else {
			ratioP.setText("Tỷ lệ bắn trúng: 0 %");
		}
		pointP.setText("Điểm: " + playerPoint);
		shipDeadP.setText("Số tàu bị hạ: " + shipDeadByPlayer + "/" + (A.size()-1));

		if (isHard) {
			turnC.setText("Computer (Hard)");
		} else {
			turnC.setText("Computer (Easy)");
		}

		curC.setText("Số điểm đã bắn: " + currentComputer);
		hitC.setText("Số điểm bắn trúng: " + computerHit + "/" + sumPoint);
		if (currentComputer != 0) {
			ratioC.setText("Tỷ lệ bắn trúng: " + ((computerHit * 100) / currentComputer) + " %");
		} else {
			ratioC.setText("Tỷ lệ bắn trúng: 0 %");
		}
		pointC.setText("Điểm: " + computerPoint);
		shipDeadC.setText("Số tàu bị hạ: " + shipDeadByComputer + "/" + (B.size()-1));

		turnP.setForeground(Color.decode("#EB5A37"));
		turnP.setFont(new Font("Arial", Font.PLAIN, 20));
		curP.setForeground(Color.decode("#EB5A37"));
		curP.setFont(new Font("Arial", Font.PLAIN, 15));
		hitP.setForeground(Color.decode("#EB5A37"));
		hitP.setFont(new Font("Arial", Font.PLAIN, 15));
		ratioP.setForeground(Color.decode("#EB5A37"));
		ratioP.setFont(new Font("Arial", Font.PLAIN, 15));
		shipDeadP.setForeground(Color.decode("#EB5A37"));
		shipDeadP.setFont(new Font("Arial", Font.PLAIN, 15));
		pointP.setForeground(Color.decode("#EB5A37"));
		pointP.setFont(new Font("Arial", Font.PLAIN, 15));

	}

	private boolean shot(int i, int j) {
		currentComputer++;
		markC[i][j] = true;
		if (playerMap.isShip[i][j]) {
			playerMap.mapPiece[i][j].setIcon(hit);
			computerHit++;
			return true;
		} else
			playerMap.mapPiece[i][j].setFont(new Font("Arial", Font.PLAIN, 30));
		playerMap.mapPiece[i][j].setForeground(Color.white);
		playerMap.mapPiece[i][j].setText("X");

		return false;
	}

	private void hitRandom() {
		Random rd = new Random();
		int i = rd.nextInt(10) + 1;
		int j = rd.nextInt(10) + 1;
		if (!markC[i][j]) {
			shot(i, j);
			isPlayer = true;
		} else
			hitRandom();
	}

	// random cho chế độ Hard
	private void hitRandomHard() {
		if (!Q.isEmpty()) {
			int x = Integer.parseInt(Q.peek());
			int i = x / 100;
			int j = x % 100;
//			System.out.println("Queue " + i + " " + j);
			Q.remove();
			boolean c = shot(i, j);
			if (c) {

				if (j < 10 && !markC[i][j + 1])
					Q.add("" + (i * 100 + j + 1));
				if (j > 1 && !markC[i][j - 1])
					Q.add("" + (i * 100 + j - 1));

			}
			isPlayer = true;
			return;
		}
		Random rd = new Random();
		int i = rd.nextInt(10) + 1;
		int j = rd.nextInt(10) + 1;
		if (!markC[i][j]) {
			boolean c = shot(i, j);
			if (c) {
				if (j < 10 && !markC[i][j + 1])
					Q.add("" + (i * 100 + j + 1));
				if (j > 1 && !markC[i][j - 1])
					Q.add("" + (i * 100 + j - 1));
			}
			isPlayer = true;
			return;
		} else
			hitRandom();
	}

	private void addAction() {
		for (int i = 1; i <= 10; i++) {
			for (int j = 1; j <= 10; j++) {
				computerMap.mapPiece[i][j].setActionCommand("" + (i * 100 + j));
				computerMap.mapPiece[i][j].addActionListener(this);
			}
		}
	}

	private boolean isPlayerWin() {
		if (playerHit == sumPoint)
			return true;
		return false;
	}

	private boolean isComputerWin() {
		if (computerHit == sumPoint)
			return true;
		return false;
	}

	private void checkDeadOnComputerMap() {
		int cnt = 0;
		for (String s : B) {
			if (s == "")
				continue;
			int x = Integer.parseInt(s);
			int leng = x / 10000;
			int j = (x - leng * 10000) / 100;
			int i = x % 100;
			boolean c = true;

			for (int t = 0; t < leng; t++) {
				if (!markP[i][j + t])
					c = false;
			}
			if (!c)
				continue;
			cnt++;
			for (int t = 0; t < leng; t++) {
				computerMap.mapPiece[i][j + t]
						.setIcon(new ImageIcon(loadImage("/img/" + leng + (t + 1) + ".png", 56, 46)));
			}
		}
		shipDeadByPlayer = cnt;
	}

	private void checkDeadOnPlayerMap() {
		int cnt = 0;
		for (String s : A) {
			if (s == "")
				continue;
			int x = Integer.parseInt(s);
			int leng = x / 10000;
			int j = (x - leng * 10000) / 100;
			int i = x % 100;
			boolean c = true;

			for (int t = 0; t < leng; t++) {
				if (!markC[i][j + t])
					c = false;

			}
			if (!c)
				continue;
			cnt++;
			for (int t = 0; t < leng; t++) {
				playerMap.mapPiece[i][j + t]
						.setIcon(new ImageIcon(loadImage("/img/" + leng + (t + 1) + ".png", 56, 46)));
			}
		}
		shipDeadByComputer = cnt;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		if ("backIgnore".equals(e.getActionCommand())) {
			MainMenu menu = new MainMenu(1120, 690, isPlaySound);
			frame.setVisible(false);
			clip.stop();
			menu.menu2();
			return;
		}
		
		if ("back".equals(e.getActionCommand())) {
			MainMenu menu = new MainMenu(1120, 690, isPlaySound);
			frame.setVisible(false);
			clip.stop();
			menu.menu2();
			return;
		}

		if (!isPlayer)
			return;
		int x = Integer.parseInt(e.getActionCommand());
		int i = x / 100;
		int j = x % 100;
		if (markP[i][j]) {
			return;
		}
		markP[i][j] = true;
		currentPlayer++;
		if (computerMap.isShip[i][j]) {
			computerMap.mapPiece[i][j].setIcon(hit);
			playerHit++;
			checkDeadOnComputerMap();
		} else {
			computerMap.mapPiece[i][j].setFont(new Font("Arial", Font.PLAIN, 30));
			computerMap.mapPiece[i][j].setForeground(Color.white);
			computerMap.mapPiece[i][j].setText("X");
		}
		isPlayer = false;
		update();

		if (isPlayerWin()) {
			JOptionPane.showMessageDialog(this, "You Win!!!");
			gameOver(true);
			return;
		}

		try {
			Thread.sleep(10);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (isHard)
			hitRandomHard();
		else
			hitRandom();

		checkDeadOnPlayerMap();
		if (isComputerWin()) {
			JOptionPane.showMessageDialog(this, "You Lose!!!");
			gameOver(false);
		}
		update();
	}

	private void gameOver(boolean isPlayerWin) {
		setHighScore();
		turnComputer.remove(turnC);
		turnComputer.remove(curC);
		turnComputer.remove(hitC);
		turnComputer.remove(shipDeadC);
		turnComputer.remove(ratioC);
		turnComputer.remove(pointC);
		turnComputer.remove(backIgnore);
		turnComputer.setLayout(new GridLayout(2, 1));

		JLabel over = new JLabel("", SwingConstants.CENTER);
		if (isPlayerWin) {
			over.setText("YOU WIN");
			over.setForeground(Color.decode("#EB5A37"));
			over.setFont(new Font("Arial", Font.PLAIN, 20));
		} else {
			over.setText("YOU LOSE");
			over.setForeground(Color.decode("#EB5A37"));
			over.setFont(new Font("Arial", Font.PLAIN, 20));
		}

		back.setText("Quay về Menu");
		turnComputer.add(over);
		turnComputer.add(back);
		back.setBackground(Color.decode("#D2EDFE"));
		back.setBorder(new LineBorder(Color.decode("#D2EDFE")));
		back.setActionCommand("back");
		back.addActionListener(this);
	}

	private void setHighScore() {
//		JOptionPane.showMessageDialog(frame, "set ");
//		URL url = MainMenu.class.getResource("/HighScore/highscore.txt");
//		File file = new File(url.getPath());
		File file = new File("D:/high.txt");
		int[] A = new int[6];
		try {
			file.createNewFile();
			Scanner scan = new Scanner(file);
			for (int i = 0; i < 5; i++) {
				if (scan.hasNextInt()) {
					A[i] = scan.nextInt();
				}
				else {
					A[i] = 0;
				}
			}
			scan.close();
			A[5] = playerPoint;
			Arrays.parallelSort(A);
//			file.delete();
//			file.createNewFile();
		} catch (Exception e) {
			System.out.println("File not found");
		}
		String s = "" + A[5] + " " + A[4] + " " + A[3] + " " + A[2] + " " + A[1];
		try {
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
			clip.loop(clip.LOOP_CONTINUOUSLY);
			clip.start();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
}
