package BatteShip;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

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
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class Creator extends JLabel implements MouseListener, MouseMotionListener, ActionListener {
	public Ship[] shipArray; // ảnh ship
	public JPanel shipMap; // panel chứa ship và map
	public JLabel map; // label chứa ảnh map
	public JLabel[] ship; // label chứa ship (ảnh ship)
	private int X, Y; // biến dùng để get tọa độ khi di chuyển
	private static int xLeft; // tọa độ cua map trên frame
	private static int yUp; // tọa độ của map trên frame
	private static int xRight;
	private static int yDown;
	private static int xStart; // tọa độ điểm bắt đầu khi kéo thả
	private static int yStart; // tọa độ điểm bắt đầu khi kéo thả
	private static int numShip;
	public static int nShip1;
	public static int nShip2;
	public static int nShip3;
	public static boolean[][] M = new boolean[12][12]; // đánh dấu ô đã có tàu
	public static int[] xS; // đánh dấu tọa độ của ship khi chưa vào map
	public static int[] yS;
	public static boolean[] isNgang; // tàu nằm ngang hay nằm dọc
	public static int[] lengShip; // độ dài tàu i
	public SmallMap playerMap, computerMap;
	
	public JButton start, back; // nút start và back;
	public JButton random;

	public static int xRandom; // get tọa độ của nút Random
	public static int yRandom;
	public JFrame frame;
	public static boolean isHard; // chế độ khó hay dễ

	public static ArrayList<String> A; // aray lưu tọa độ tàu của người chơi
	public static ArrayList<String> B; // lưu tọa độ tàu của computer
	public Clip clip;
	public static boolean isPlaySound;
		
	public Creator(int w, int h, int numShip3, int numShip2, int numShip1, String gamemode, boolean playSound) {
		super();
		this.setSize(w, h);
		
		isPlaySound = playSound;
		if (gamemode == "easy")
			isHard = false;
		else
			isHard = true;
		
		// shipmap
		map = new JLabel();
		map.setIcon(new ImageIcon(loadImage("/img/bigMap.png", 560, 560)));
		map.setSize(560, 560);
		
		// numShipi = số lượng tàu độ dài i
		// x: số lượng tàu
		int x = numShip3 + numShip2 + numShip1;
		nShip1 = numShip1;
		nShip2 = numShip2;
		nShip3 = numShip3;

		numShip = x;

		A = new ArrayList<String>();
		B = new ArrayList<String>();
		A.add("");
		B.add("");

		shipArray = new Ship[x + 1];
		xS = new int[x + 1];
		yS = new int[x + 1];
		isNgang = new boolean[x + 1];
		lengShip = new int[x + 1];
		ship = new JLabel[x + 1];
		int cnt = 1;
		for (int i = 1; i <= numShip3; i++) {
			shipArray[cnt] = new Ship(3, 150, 50);
			ship[cnt] = new JLabel();
			ship[cnt].setIcon(new ImageIcon(shipArray[cnt].getShip()));
			A.add("30000");
			B.add("30000");
			cnt++;
		}
		for (int i = 1; i <= numShip2; i++) {
			shipArray[cnt] = new Ship(2, 100, 50);
			ship[cnt] = new JLabel();
			ship[cnt].setIcon(new ImageIcon(shipArray[cnt].getShip()));
			A.add("20000");
			B.add("20000");
			cnt++;
		}

		for (int i = 1; i <= numShip1; i++) {
			shipArray[cnt] = new Ship(1, 50, 50);
			ship[cnt] = new JLabel();
			ship[cnt].setIcon(new ImageIcon(shipArray[cnt].getShip()));
			A.add("10000");
			B.add("10000");
			cnt++;
		}

		for (int i = 1; i <= x; i++) {
			ship[i].addMouseListener(this);
			ship[i].addMouseMotionListener(this);
		}

		build(x);
		init();

		frame = new JFrame("Battle Ship");
		frame.setIconImage(loadImage("/img/logo.png",90,90));
		frame.add(this);
		frame.setSize(w, h);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);

		xLeft = map.getX(); // lấy tọa độ map để căn góc :v
		xRight = xLeft + 560;
		yUp = map.getY();
		yDown = yUp + 560;

		xRandom = random.getX();
		yRandom = random.getY();
		playSound("/sound/sound.wav");
		if (!isPlaySound) clip.stop();
	}

	// hàm itit(): khởi tạo mảng M và xS,yS để random.
	public void init() {
		for (int i = 1; i <= 10; i++) {
			for (int j = 1; j <= 10; j++) {
				M[i][j] = false;
				if (i <= numShip) {
					xS[i] = ship[i].getX();
					yS[i] = ship[i].getY();
					isNgang[i] = true;
				}
			}
		}

		for (int i = 1; i <= nShip3; i++)
			lengShip[i] = 3;
		for (int i = nShip3 + 1; i <= nShip3 + nShip2; i++)
			lengShip[i] = 2;
		for (int i = nShip3 + nShip2 + 1; i <= numShip; i++)
			lengShip[i] = 1;
	}

	public void build(int x) {
		// tạo Button
		start = new JButton();
		back = new JButton();
		random = new JButton("Random");

		back.setBackground(Color.decode("#D2EDFE"));
		back.setBorder(new LineBorder(Color.decode("#D2EDFE")));
		start.setBackground(Color.decode("#D2EDFE"));
		start.setBorder(new LineBorder(Color.decode("#D2EDFE")));
		back.setIcon(new ImageIcon(loadImage("/img/back.png", 50, 50)));
		start.setIcon(new ImageIcon(loadImage("/img/next.png", 50, 50)));

		random.setFont(new Font("Arial", Font.PLAIN, 30));
		random.setBackground(Color.decode("#D2EDFE"));
		random.setBorder(new LineBorder(Color.decode("#D2EDFE")));
		random.setForeground(Color.red);

		// add action
		random.setActionCommand("random");
		random.addActionListener(this);
		back.setActionCommand("back");
		back.addActionListener(this);
		start.setActionCommand("start");
		start.addActionListener(this);

		this.add(back);
		this.add(random);
		this.add(start);
		for (int i = 1; i <= x; i++) {
			this.add(ship[i]);
		}
		start.setSize(50, 50);
		back.setSize(50, 50);
		random.setSize(100, 50);

		this.add(map);

		this.setLayout(new FlowLayout());
		this.setIcon(new ImageIcon(loadImage("/img/blue.png", 1120, 690)));

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

	// đặt thử tàu thứ tmp vào vị trí x,y ứng với ô i,j
	public boolean put(int tmp, int x, int y, int i, int j) {

		int leng = lengShip[tmp];

		if ((x + leng * 50 > xRight && leng > 1) || (leng == 1 && x > xRight)) {
			ship[tmp].setLocation(xStart, yStart);

			if (xStart >= xLeft && xStart <= xRight && yStart >= yUp && yStart <= yDown) {
				int u = (xStart - xLeft) / 56 + 1;
				int v = (yStart - yUp) / 56 + 1;
				for (int q = 0; q < leng; q++) {
					M[u + q][v] = true;
				}
			}

			return false;
		}

		for (int t = 0; t < leng; t++) {
			if (M[i + t][j] == true) {
				ship[tmp].setLocation(xStart, yStart);

				if (xStart >= xLeft && xStart <= xRight && yStart >= yUp && yStart <= yDown) {
					int u = (xStart - xLeft) / 56 + 1;
					int v = (yStart - yUp) / 56 + 1;
					for (int q = 0; q < leng; q++) {
						M[u + q][v] = true;
					}
				}

				return false;
			}
		}

		if (leng > 1) {
			ship[tmp].setLocation(x, y);
		} else {
			ship[tmp].setLocation(x - 10, y);
		}
		for (int t = 0; t < leng; t++) {
			M[i + t][j] = true;
		}

		int v = (Integer.parseInt(A.get(tmp))) / 10000;
		A.set(tmp, "" + (v * 10000 + i * 100 + j));

		return true;
	}

	public boolean putForRandom(int tmp, int x, int y, int i, int j, int leng, boolean isForPlayer) {
		if (x + 50 * leng > xRight && leng > 1)
			return false;
		for (int t = 0; t < leng; t++) {
			if (M[i + t][j] == true)
				return false;
		}

		for (int t = 0; t < leng; t++) {
			M[i + t][j] = true;
		}
		ship[tmp].setLocation(x, y);

		if (isForPlayer) {
			int v = (Integer.parseInt(A.get(tmp))) / 10000;
			A.set(tmp, "" + (v * 10000 + i * 100 + j));
		} else {
			int v = (Integer.parseInt(B.get(tmp))) / 10000;
			B.set(tmp, "" + (v * 10000 + i * 100 + j));
		}

		return true;
	}

	public void setRandom(boolean isForPlayer) {
		for (int i = 1; i <= 10; i++) {
			for (int j = 1; j <= 10; j++) {
				M[i][j] = false;
			}
		}
		Random rd = new Random();
		Queue<JLabel> Q = new LinkedList<>();

		for (int i = 1; i <= numShip; i++) {
			ship[i].setLocation(xS[i], yS[i]);
			Q.add(ship[i]);
		}
		int cnt = 1;
		while (!Q.isEmpty()) {
			int i = rd.nextInt(10);
			int j = rd.nextInt(10);
			int x = xLeft + 56 * i + 10;
			int y = yUp + 56 * j + 5;
			xStart = xS[cnt];
			yStart = yS[cnt];

			boolean is = true;
			if (!isForPlayer)
				is = false;

			boolean c = putForRandom(cnt, x, y, i + 1, j + 1, lengShip[cnt], is);
			if (c) {
				cnt++;
				Q.remove();
			}
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub

		if (xStart >= xLeft && xStart <= xRight && yStart >= yUp && yStart <= yDown) {
			int i = (xStart - xLeft) / 56 + 1;
			int j = (yStart - yUp) / 56 + 1;
			int tmp = 0;
			for (int k = 1; k <= numShip; k++) {
				if (e.getSource() == ship[k]) {
					tmp = k;
					break;
				}
			}
			int leng = lengShip[tmp];
			for (int t = 0; t < leng; t++) {
				M[i + t][j] = false;
			}
		}

		int xNew = e.getX() + e.getComponent().getX() - X;
		int yNew = e.getY() + e.getComponent().getY() - Y;
		e.getComponent().setLocation(xNew, yNew);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		X = e.getX();
		Y = e.getY();
		xStart = e.getComponent().getX();
		yStart = e.getComponent().getY();

	}

	// Nếu ship nằm ngoài map -> trả về vị trí cũ
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		int xNew = e.getX() + e.getComponent().getX() - X;
		int yNew = e.getY() + e.getComponent().getY() - Y;
		if (xNew < xLeft || xNew > xRight || yNew < yUp || yNew > yDown) {
			e.getComponent().setLocation(xStart, yStart);
			return;
		}

		int i = (xNew - xLeft) / 56;
		int j = (yNew - yUp) / 56;
		xNew = xLeft + i * 56 + 10;
		yNew = yUp + j * 56 + 3; // tàu sẽ ở ô i+1 và j+1
		int tmp = 0;
		for (int k = 1; k <= numShip; k++) {
			if (e.getSource() == ship[k]) {
				tmp = k;
				break;
			}
		}

		put(tmp, xNew, yNew, i + 1, j + 1);

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if ("random".equals(e.getActionCommand())) {
			setRandom(true);
		}

		if ("start".equals(e.getActionCommand())) {
			boolean c = isReady();
			if (!c)
				return;
			playerMap = getFinalMap();
			setRandom(false);
			computerMap = getFinalMap();
			goToPlay();
		}

	}

	private boolean isReady() {
		for (int i = 1; i <= numShip; i++) {
			int x = ship[i].getX();
			int y = ship[i].getY();
			if (x < xLeft || x > xRight || y < yUp || y > yDown)
				return false;
		}
		return true;
	}

	private SmallMap getFinalMap() {
		SmallMap s = new SmallMap(560, 560);
		for (int i = 1; i <= 10; i++) {
			for (int j = 1; j <= 10; j++) {
				s.isShip[j][i] = M[i][j];
			}
		}
		return s;
	}

	public void goToPlay() {
		this.setVisible(false);
		clip.stop();
		frame.remove(this);
		new PlayGame(1120, 680, playerMap, computerMap, isHard, A, B, isPlaySound);
		frame.setVisible(false);
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
}