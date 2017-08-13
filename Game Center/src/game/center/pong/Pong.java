package game.center.pong;


import acm.graphics.*;
import acm.program.*;
import acm.util.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class Pong extends GraphicsProgram implements MouseListener {

    private static final int GAME_WIDTH = 480;
    private static final int GAME_HEIGHT = 620;
    private static final int PADDLE_WIDTH = 100;
    private static final int PADDLE_HEIGHT = 11;
    private static final int PADDLE_RADIUS = PADDLE_WIDTH / 2;
    private static final int PADDLE_OFFSET = 30;
    private static GRect PADDLE1;
    private static GRect PADDLE2;
    private static final int BALL_DIAMETER = 18;
    private static final int INIT_SLOWNESS = 5;
    private static final int FINAL_SLOWNESS = 2;
    private static double SLOWNESS = INIT_SLOWNESS;
    private static GOval BALL;
    private static GLabel USER_LABEL, Lives,Instructions;
    private double vx, vy;
    private double Pvx, Pvy;

    public static int USER_SCORE, CPU_SCORE;
    private static int PADDLE_HIT_COUNT = 0;

    private static final Color[] rowColors = {Color.red, Color.red, Color.orange, Color.orange,
        Color.yellow, Color.yellow, Color.green, Color.green,
        Color.cyan, Color.cyan};

    private RandomGenerator rgen = new RandomGenerator();

    public static void main(String[] args) {

        String[] sizeArgs = {"width=" + (GAME_WIDTH), "height=" + (GAME_HEIGHT)};
        new Pong().start(sizeArgs);
    }

    public void setScoreButton() {
        Lives = new GLabel("Lives= "+(3-CPU_SCORE)); 
        Lives.setFont(new Font("Arial", Font.PLAIN, 20));
        double width1 = Lives.getWidth() + 10;
        double height1 = Lives.getHeight() + 10;
        Lives.setColor(Color.black);
        Lives.setLocation(GAME_WIDTH - (3.0 / 2) * width1, (GAME_HEIGHT / 2) + 0 - height1);
        USER_LABEL = new GLabel("Rally: " + PADDLE_HIT_COUNT, 0, 0);
        USER_LABEL.setFont(new Font("Arial", Font.PLAIN, 20));
        double width = USER_LABEL.getWidth() + 10;
        double height = USER_LABEL.getHeight() + 10;
        USER_LABEL.setColor(Color.black);
        USER_LABEL.setLocation(GAME_WIDTH - (3.0 / 2) * width, (GAME_HEIGHT / 2) + 0 + height);
        
        add(Lives);
        add(USER_LABEL);

        GLine Divider = new GLine(0, (GAME_HEIGHT / 2), GAME_WIDTH, (GAME_HEIGHT / 2));
        add(Divider);
    }

    private void addUserPaddle() {
        PADDLE1 = new GRect((GAME_WIDTH - PADDLE_WIDTH) / 2, GAME_HEIGHT - (PADDLE_OFFSET + PADDLE_HEIGHT), PADDLE_WIDTH, PADDLE_HEIGHT);
        PADDLE1.setColor(Color.BLACK);
        PADDLE1.setFillColor(Color.BLACK);
        PADDLE1.setFilled(true);
        add(PADDLE1);
    }

    private void addCPUPaddle() {
        PADDLE2 = new GRect((GAME_WIDTH - PADDLE_WIDTH) / 2, PADDLE_OFFSET, PADDLE_WIDTH, PADDLE_HEIGHT);
        PADDLE2.setColor(Color.BLACK);
        PADDLE2.setFillColor(Color.BLACK);
        PADDLE2.setFilled(true);
        add(PADDLE2);
    }

    private void setUpBall() {
        BALL = new GOval(GAME_WIDTH / 2 - (BALL_DIAMETER / 2), GAME_HEIGHT / 2 - (BALL_DIAMETER / 2), BALL_DIAMETER, BALL_DIAMETER);
        BALL.setFillColor(Color.BLACK);
        BALL.setFilled(true);
        add(BALL);
        vx = rgen.nextDouble(1.0, 2.0);
        if (!rgen.nextBoolean(0.5)) {
            vx = -vx;
        }
        vy = 2.0;
        waitForClick();
    }

    private int moveBall() {
        GPoint ballPoint = BALL.getLocation();
        if (ballPoint.getX() + BALL_DIAMETER > GAME_WIDTH) {
            vx = -vx;
        }
        if (ballPoint.getX() <= 0) {
            vx = -vx;
        }
        if (ballPoint.getY() <= 0) {
            return 1;
        }
        if (ballPoint.getY() > GAME_HEIGHT) {
            return 2;//ballVY = -ballVY; // basically lost
        }
        BALL.move(vx, vy);
        return 0;
    }

    private Boolean movePaddle() {
        GPoint PaddlePoint = PADDLE2.getLocation();
        GPoint ballPoint = BALL.getLocation();
        double cenBall = ballPoint.getX() + (BALL_DIAMETER / 2);
        double cenPaddle = PaddlePoint.getX() + (PADDLE_WIDTH / 2);
        Pvx = (GAME_WIDTH / 40) * (cenBall - cenPaddle) / (GAME_WIDTH / 2);
        if (Math.abs(Pvx) < 1) {
            Pvx = Pvx < 0 ? -2 : 2;
        }
        double pos = Math.max(Math.min(PaddlePoint.getX() + Pvx, GAME_WIDTH - PADDLE2.getWidth()), 0);
        PADDLE2.setLocation(pos, PaddlePoint.getY());
        return false;
    }

    private GObject getCollidingObject(GObject ballObj) {
        GPoint ballPoint = ballObj.getLocation();
        GObject coll;
        GPoint addingPoints[] = new GPoint[4];
        addingPoints[0] = new GPoint(0, 0);
        addingPoints[1] = new GPoint(0, BALL_DIAMETER);
        addingPoints[2] = new GPoint(BALL_DIAMETER, BALL_DIAMETER);
        addingPoints[3] = new GPoint(BALL_DIAMETER, 0);
        for (int i = 0; i < 4; i++) {
            coll = getElementAt(ballPoint.getX() + addingPoints[i].getX(), ballPoint.getY() + addingPoints[i].getY());
            if (coll != null) {
                return coll;
            }
        }
        return null;
    }

    public void setup() {
        setScoreButton();
        addUserPaddle();
        addCPUPaddle();
        setUpBall();
        addMouseListeners();
    }

    public void HyperBounceAlgo() {
        double Ballx = BALL.getX() + (BALL_DIAMETER / 2);
        double Padx = PADDLE1.getX() + (PADDLE_WIDTH / 2);
        vy = -vy;
        double multiplier = (Ballx - Padx) / (PADDLE_WIDTH / 4);
        if (Math.abs(multiplier * vx) < 1) {
            multiplier = multiplier < 0 ? (-1 / vx) : (1 / vx);
        }
        vx = multiplier * vx;
    }

    public void AirHockeyAlgo() {
        double signvx = vx > 0 ? 1 : -1;
        double signvy = vy > 0 ? 1 : -1;
        double magvx = Math.abs(vx);
        double magvy = Math.abs(vy);
        vx = magvy * signvx;
        vy = -magvx * signvy;
    }

    public void PaddleHit() {
        
        PADDLE_HIT_COUNT++;
        if (SLOWNESS > FINAL_SLOWNESS) {
            SLOWNESS -= 0.5;
        }
        GPoint ballPoint = BALL.getLocation();
        GPoint paddlePoint = PADDLE1.getLocation();
        if (ballPoint.getY() > GAME_HEIGHT - (PADDLE_OFFSET + PADDLE_HEIGHT + BALL_DIAMETER)) 
        {
            BALL.setLocation(ballPoint.getX(), GAME_HEIGHT - (PADDLE_OFFSET + PADDLE_HEIGHT + BALL_DIAMETER));
        }
        USER_LABEL.setLabel("Rally: " + PADDLE_HIT_COUNT);
        
        AirHockeyAlgo();

    }

    public void Paddle2Hit() {
        if (SLOWNESS > FINAL_SLOWNESS) {
            SLOWNESS -= 0.5;
        }
        GPoint ballPoint = BALL.getLocation();
        GPoint paddlePoint = PADDLE2.getLocation();
        if (ballPoint.getY() < PADDLE_OFFSET + PADDLE_HEIGHT)
        {
            BALL.setLocation(ballPoint.getX(), PADDLE_OFFSET + PADDLE_HEIGHT);
        }

        AirHockeyAlgo();
    }

    public void play() {
        while (USER_SCORE < 3 && CPU_SCORE < 3) {
            Lives.setLabel("Lives= "+(3-CPU_SCORE));
            int x = moveBall();
            if (x == 1) {
                USER_SCORE++;
                Instructions = new GLabel("Good job... click to continue");
                Instructions.setColor(Color.black);
                add(Instructions, 10, 290);
                reset();
            } else if (x == 2) {
                CPU_SCORE++;
                Instructions = new GLabel("Oops... click to continue");
                Instructions.setColor(Color.black);
                add(Instructions, 10, 290);
                reset();
            }
            if (BALL.getY() < GAME_HEIGHT / 2) {
                movePaddle();
            }
            pause(SLOWNESS);
            GObject collider = getCollidingObject(BALL);
            if (collider == PADDLE1) {
                PaddleHit();
            }
            if (collider == PADDLE2) {
                Paddle2Hit();
            }
        }
        removeAll();
        GLabel Over = new GLabel("Gamover. You Scored: "+PADDLE_HIT_COUNT+" points");
        Over.setFont("Serif-bold-20");
        Over.setColor(Color.red);
        
        add(Over, 50, 450);
    }

    public void reset() {

        pause(1100);
        remove(Instructions);
        remove(PADDLE1);
        remove(PADDLE2);
        remove(BALL);
        SLOWNESS = INIT_SLOWNESS;
        addUserPaddle();
        addCPUPaddle();
        USER_LABEL.setLabel("Rally: " + PADDLE_HIT_COUNT);
        if (USER_SCORE < 10 && CPU_SCORE < 10) {
            setUpBall();
        }
    }

    public void run() {
        setup();
        play();
    }

    public void mouseMoved(MouseEvent e) {
        GPoint p = new GPoint(e.getPoint());

        double pos = Math.max(Math.min(p.getX() - (PADDLE1.getWidth() / 2), GAME_WIDTH - PADDLE1.getWidth()), 0);
        PADDLE1.setLocation((int) pos, GAME_HEIGHT - PADDLE_OFFSET - PADDLE1.getHeight());
    }

    public static String toString(String[] b) {
        if (b == null) {
            return null;
        }

        String res = "[";
        for (int k = 0; k < b.length; k = k + 1) {
            if (k > 0) {
                res = res + ", ";
            }
            res = res + b[k];
        }
        return res + "]";
    }
}
