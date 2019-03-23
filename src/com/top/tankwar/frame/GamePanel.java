package com.top.tankwar.frame;

import com.top.tankwar.enumtype.GameType;
import com.top.tankwar.enumtype.TankType;
import com.top.tankwar.model.*;
import com.top.tankwar.model.wall.BaseWall;
import com.top.tankwar.model.wall.Wall;
import com.top.tankwar.util.AudioPlayer;
import com.top.tankwar.util.AudioUtil;
import com.top.tankwar.util.ImageUtil;

import javax.swing.*;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;


@SuppressWarnings({"deprecation", "serial"})
public class GamePanel extends JPanel implements KeyListener {

    public static final int FRESHTIME = 20;
    private BufferedImage image;
    private Graphics g;
    private MainFrame frame;
    private GameType gameType;
    private Tank play1, play2;
    private boolean y_key, s_key, w_key, a_key, d_key, up_key, down_key, left_key, right_key, num1_key;
    private int level = Level.previsousLevel();
    private List<Bullet> bullets;
    private volatile List<Tank> allTanks;
    private List<Tank> botTanks;
    private static final int botCount = 30;
    private int botReadyCount = botCount;
    private int botSurplusCount = botCount;
    private int botMaxInMap = 6;
    private int botX[] = {10, 367, 754};
    private List<Tank> playerTanks;
    private volatile boolean finish = false;
    private BaseWall base;
    private List<Wall> walls;
    private List<Boom> boomImage;
    private Random r = new Random();
    private int createBotTimer = 0;
    private Tank survivor;
    private List<AudioClip> audios = AudioUtil.getAudios();
    private Tool tool = Tool.getToolInstance(r.nextInt(500), r.nextInt(500));
    private int toolTimer = 0;
    private int pauseTimer = 0;


    public GamePanel(MainFrame frame, int level, GameType gameType) {
        this.frame = frame;
        frame.setSize(775, 600);
        this.level = level;
        this.gameType = gameType;
        setBackground(Color.BLACK);
        init();
        Thread t = new FreshThead();
        t.start();
        new AudioPlayer(AudioUtil.START).new AudioThread().start();
        addListener();
    }

    private void init() {
        bullets = new ArrayList<>();
        allTanks = new ArrayList<>();
        walls = new ArrayList<>();
        boomImage = new ArrayList<>();

        image = new BufferedImage(794, 572, BufferedImage.TYPE_INT_BGR);
        g = image.getGraphics();

        playerTanks = new Vector<>();
        play1 = new Tank(278, 537, ImageUtil.PLAYER1_UP_IMAGE_URL, this, TankType.PLAYER1);
        if (gameType == GameType.TWO_PLAYER) {
            play2 = new Tank(448, 537, ImageUtil.PLAYER2_UP_IMAGE_URL, this, TankType.PLAYER2);
            playerTanks.add(play2);
        }
        playerTanks.add(play1);
        botTanks = new ArrayList<>();
        botTanks.add(new BotTank(botX[0], 1, this, TankType.BOTTANK));
        botTanks.add(new BotTank(botX[1], 1, this, TankType.BOTTANK));
        botTanks.add(new BotTank(botX[2], 1, this, TankType.BOTTANK));
        botReadyCount -= 3;
        allTanks.addAll(playerTanks);
        allTanks.addAll(botTanks);
        base = new BaseWall(360, 520);
        initWalls();
    }


    private void addListener() {
        frame.addKeyListener(this);
    }


    @SuppressWarnings("static-access")
    public void initWalls() {
        Map map = Map.getMap(level);
        walls.addAll(map.getWalls());
        walls.add(base);
    }


    public void paint(Graphics g) {
        paintTankActoin();
        createBotTank();
        paintImage();
        g.drawImage(image, 0, 0, this);
        System.gc();
    }


    private void paintImage() {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        panitBoom();
        paintBotCount();
        panitBotTanks();
        panitPlayerTanks();
        allTanks.addAll(playerTanks);
        allTanks.addAll(botTanks);
        panitWalls();
        panitBullets();
        paintTool();
        if (botSurplusCount == 0) {
            stopThread();
            paintBotCount();
            gotoNextLevel();
        }

        if (gameType == GameType.ONE_PLAYER) {
            if (!play1.isAlive() && play1.getLife() == 0) {
                stopThread();
                boomImage.add(new Boom(play1.x, play1.y));
                panitBoom();
                paintGameOver();
                gotoPrevisousLevel();
            }
        } else if (gameType == GameType.TWO_PLAYER) {
            if (play1.isAlive() && !play2.isAlive() && play2.getLife() == 0) {
                survivor = play1;
            } else if (!play1.isAlive() && play1.getLife() == 0 && play2.isAlive()) {
                survivor = play2;
            } else if (!(play1.isAlive() || play2.isAlive())) {
                stopThread();
                boomImage.add(new Boom(survivor.x, survivor.y));
                panitBoom();
                paintGameOver();
                gotoPrevisousLevel();
            }
        }

        if (!base.isAlive()) {
            stopThread();
            paintGameOver();
            base.setImage(ImageUtil.BREAK_BASE_IMAGE_URL);
            gotoPrevisousLevel();
        }
        g.drawImage(base.getImage(), base.x, base.y, this);
    }

    private void paintTool() {
        if (toolTimer >= 4500) {
            toolTimer = 0;
            tool.changeToolType();
        } else {
            toolTimer += FRESHTIME;
        }
        if (tool.getAlive()) {
            tool.draw(g);
        }
    }

    private void paintBotCount() {
        g.setColor(Color.ORANGE);
        g.drawString("敌方坦克剩余：" + botSurplusCount, 337, 15);
    }


    private void paintGameOver() {
        g.setFont(new Font("Game Over !", Font.BOLD, 50));
        g.setColor(Color.RED);
        g.drawString("Game Over !", 250, 400);
        new AudioPlayer(AudioUtil.GAMEOVER).new AudioThread().start();
    }


    private void panitBoom() {
        for (int i = 0; i < boomImage.size(); i++) {
            Boom boom = boomImage.get(i);
            if (boom.isAlive()) {
                AudioClip blast = audios.get(2);
                blast.play();
                boom.show(g);
            } else {
                boomImage.remove(i);
                i--;
            }
        }
    }


    private void panitWalls() {
        for (int i = 0; i < walls.size(); i++) {
            Wall w = walls.get(i);
            if (w.isAlive()) {
                g.drawImage(w.getImage(), w.x, w.y, this);
            } else {
                walls.remove(i);
                i--;
            }
        }
    }


    private void panitBullets() {
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            if (b.isAlive()) {
                b.move();
                b.hitBase();
                b.hitWall();
                b.hitTank();
                b.hitBullet();
                g.drawImage(b.getImage(), b.x, b.y, this);
            } else {
                bullets.remove(i);
                i--;
            }
        }
    }


    private void panitBotTanks() {
        for (int i = 0; i < botTanks.size(); i++) {
            BotTank t = (BotTank) botTanks.get(i);
            if (t.isAlive()) {
                if (!t.isPause()) {
                    t.go();
                }
                if (t.isPause()) {
                    if (pauseTimer > 2500) {
                        t.setPause(false);
                        pauseTimer = 0;
                    }
                    pauseTimer += FRESHTIME;
                }
                g.drawImage(t.getImage(), t.x, t.y, this);
            } else {
                botTanks.remove(i);
                i--;
                boomImage.add(new Boom(t.x, t.y));
                decreaseBot();
            }
        }
    }


    private void panitPlayerTanks() {
        for (int i = 0; i < playerTanks.size(); i++) {
            Tank t = playerTanks.get(i);
            if (t.isAlive()) {
                t.hitTool();
                t.addStar();
                g.drawImage(t.getImage(), t.x, t.y, this);
            } else {
                playerTanks.remove(i);
                boomImage.add(new Boom(t.x, t.y));
                AudioClip blast = audios.get(2);
                blast.play();
                t.setLife();
                if (t.getLife() > 0) {
                    if (t.getTankType() == TankType.PLAYER1) {
                        play1 = new Tank(278, 537, ImageUtil.PLAYER1_UP_IMAGE_URL, this, TankType.PLAYER1);
                        playerTanks.add(play1);
                    }
                    if (t.getTankType() == TankType.PLAYER2) {
                        play2 = new Tank(448, 537, ImageUtil.PLAYER2_UP_IMAGE_URL, this, TankType.PLAYER2);
                        playerTanks.add(play2);
                    }
                }

            }
        }
    }


    private synchronized void stopThread() {
        frame.removeKeyListener(this);
        finish = true;
    }


    private class FreshThead extends Thread {
        @Override
        public void run() {
            while (!finish) {
                repaint();
                System.gc();
                try {
                    Thread.sleep(FRESHTIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void createBotTank() {
        int index = r.nextInt(3);
        createBotTimer += FRESHTIME;
        if (botTanks.size() < botMaxInMap && botReadyCount > 0 && createBotTimer >= 1500) {

            Rectangle bornRect = new Rectangle(botX[index], 1, 35, 35);
            for (int i = 0, lengh = allTanks.size(); i < lengh; i++) {
                Tank t = allTanks.get(i);
                if (t.isAlive() && t.hit(bornRect)) {
                    return;
                }
            }
            botTanks.add(new BotTank(botX[index], 1, GamePanel.this, TankType.BOTTANK));
            new AudioPlayer(AudioUtil.ADD).new AudioThread().start();
            botReadyCount--;
            createBotTimer = 0;
        }
    }


    private void gotoNextLevel() {
        Thread jump = new JumpPageThead(Level.nextLevel());
        jump.start();
    }


    private void gotoPrevisousLevel() {
        Thread jump = new JumpPageThead(Level.previsousLevel());
        jump.start();
    }


    public void decreaseBot() {
        botSurplusCount--;
    }


    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_Y:
                y_key = true;
                break;
            case KeyEvent.VK_W:
                w_key = true;
                a_key = false;
                s_key = false;
                d_key = false;
                break;
            case KeyEvent.VK_A:
                w_key = false;
                a_key = true;
                s_key = false;
                d_key = false;
                break;
            case KeyEvent.VK_S:
                w_key = false;
                a_key = false;
                s_key = true;
                d_key = false;
                break;
            case KeyEvent.VK_D:
                w_key = false;
                a_key = false;
                s_key = false;
                d_key = true;
                break;
            case KeyEvent.VK_HOME:
            case KeyEvent.VK_NUMPAD1:
                num1_key = true;
                break;
            case KeyEvent.VK_UP:
                up_key = true;
                down_key = false;
                right_key = false;
                left_key = false;
                break;
            case KeyEvent.VK_DOWN:
                up_key = false;
                down_key = true;
                right_key = false;
                left_key = false;
                break;
            case KeyEvent.VK_LEFT:
                up_key = false;
                down_key = false;
                right_key = false;
                left_key = true;
                break;
            case KeyEvent.VK_RIGHT:
                up_key = false;
                down_key = false;
                right_key = true;
                left_key = false;
                break;
        }
    }


    private void paintTankActoin() {
        if (y_key) {
            play1.attack();
        }
        if (w_key) {
            play1.upWard();
        }
        if (d_key) {
            play1.rightWard();
        }
        if (a_key) {
            play1.leftWard();
        }
        if (s_key) {
            play1.downWard();
        }
        if (gameType == GameType.TWO_PLAYER) {
            if (num1_key) {
                play2.attack();
            }
            if (up_key) {
                play2.upWard();
            }
            if (right_key) {
                play2.rightWard();
            }
            if (left_key) {
                play2.leftWard();
            }
            if (down_key) {
                play2.downWard();
            }
        }
    }


    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_Y:
                y_key = false;
                break;
            case KeyEvent.VK_W:
                w_key = false;
                break;
            case KeyEvent.VK_A:
                a_key = false;
                break;
            case KeyEvent.VK_S:
                s_key = false;
                break;
            case KeyEvent.VK_D:
                d_key = false;
                break;
            case KeyEvent.VK_HOME:
            case KeyEvent.VK_NUMPAD1:
                num1_key = false;
                break;
            case KeyEvent.VK_UP:
                up_key = false;
                break;
            case KeyEvent.VK_DOWN:
                down_key = false;
                break;
            case KeyEvent.VK_LEFT:
                left_key = false;
                break;
            case KeyEvent.VK_RIGHT:
                right_key = false;
                break;
        }
    }


    public void addBullet(Bullet b) {
        bullets.add(b);
    }


    public List<Wall> getWalls() {
        return walls;
    }


    public BaseWall getBase() {
        return base;
    }


    public List<Tank> getTanks() {
        return allTanks;
    }

    public List<Bullet> getBullets() {
        return bullets;
    }

    public List<Tank> getBotTanks() {
        return botTanks;
    }

    public Tool getTool() {
        return tool;
    }

    private class JumpPageThead extends Thread {
        int level;

        public JumpPageThead(int level) {
            this.level = level;
        }


        public void run() {
            try {
                Thread.sleep(1000);
                frame.setPanel(new LevelPanel(level, frame, gameType));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void keyTyped(KeyEvent e) {
    }

    public List<Tank> getPlayerTanks() {
        return playerTanks;
    }
}
