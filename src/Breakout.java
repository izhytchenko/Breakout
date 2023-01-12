import acm.graphics.GLabel;
import acm.graphics.GObject;
import acm.graphics.GOval;
import acm.graphics.GRect;
import acm.util.RandomGenerator;
import com.shpp.cs.a.graphics.WindowProgram;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class Breakout extends WindowProgram {
    /**
     * Width and height of application window in pixels
     */
    public static final int APPLICATION_WIDTH = 400;
    public static final int APPLICATION_HEIGHT = 600;

    /**
     * Dimensions of game board (usually the same)
     */
    private static final int WIDTH = APPLICATION_WIDTH;
    private static final int HEIGHT = APPLICATION_HEIGHT;

    /**
     * Dimensions of the paddle
     */
    private static final int PADDLE_WIDTH = 60;
    private static final int PADDLE_HEIGHT = 10;

    /**
     * Offset of the paddle up from the bottom
     */
    private static final int PADDLE_Y_OFFSET = 30;

    /**
     * Number of bricks per row
     */
    private static final int NBRICKS_PER_ROW = 10;

    /**
     * Number of rows of bricks
     */
    private static final int NBRICK_ROWS = 10;

    /**
     * Separation between bricks
     */
    private static final int BRICK_SEP = 4;

    /**
     * Width of a brick
     */
    private static final int BRICK_WIDTH =
            (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

    /**
     * Height of a brick
     */
    private static final int BRICK_HEIGHT = 8;

    /**
     * Radius of the ball in pixels
     */
    private static final int BALL_RADIUS = 10;

    /**
     * Offset of the top brick row from the top
     */
    private static final int BRICK_Y_OFFSET = 70;

    /**
     * Number of turns
     */
    private static final int NTURNS = 3;
    /**
     * Time in milliseconds between every animation cycle
     */
    private static final int PAUSE_TIME = 10;
    /**
     * Paddle in game, which user use for playing
     */
    private GRect rocket;
    /**
     * distance for ball on one step
     */
    private double vx, vy;
    /**
     * count players attempts for playing
     */
    private int gameCountGame = 0;

    /**
     * The application creates a breakout game. A ball appears in the center of the screen,
     * after clicking on the mouse the game starts.
     * The goal of the game is to destroy all the bricks at the top of the screen.
     * The player has three attempts to complete the game.
     * If the player does not hit the ball with the racket, and it hits the bottom of the screen,
     * one attempt is considered lost.
     * The ball will bounce horizontally and diagonally depending on what it hits
     */
    public void run() {
        rocket = createRocket();
        addMouseListeners();
        GOval ball = createBall();
        ArrayList<GRect> bricks = createBricksWall();
        moveBall(ball, bricks);
    }

    /**
     * Create block with bricks on the screen. Bricks are centered automatically
     * Amount of bricks depends on params in constants NBRICKS_PER_ROW and NBRICK_ROWS
     *
     * @return ArrayList with created bricks
     */
    private ArrayList<GRect> createBricksWall() {
        ArrayList<GRect> bricks = new ArrayList<>();

        for (int i = 0; i < NBRICK_ROWS; i++) {
            for (int j = 0; j < NBRICKS_PER_ROW; j++) {
                GRect brick = new GRect((getWidth() - (NBRICKS_PER_ROW * BRICK_WIDTH + (NBRICKS_PER_ROW - 1) *
                        BRICK_SEP)) / 2.0 + (j * (BRICK_WIDTH + BRICK_SEP)),
                        BRICK_Y_OFFSET + ((BRICK_SEP + BRICK_HEIGHT) * i),
                        BRICK_WIDTH, BRICK_HEIGHT);
                brick.setFilled(true);
                brick.setColor(chooseColor(i));
                bricks.add(brick);
                add(brick);
            }
        }
        return bricks;
    }

    /**
     * Choose color for brick depend on brick row
     *
     * @param nColor bricks line number, which use for choosing bricks color in that line
     * @return Color is used for brick
     */
    private Color chooseColor(int nColor) {
        int colorNumber = nColor % 10;
        if (colorNumber == 0 || colorNumber == 1) {
            return Color.RED;
        } else if (colorNumber == 2 || colorNumber == 3) {
            return Color.ORANGE;
        } else if (colorNumber == 4 || colorNumber == 5) {
            return Color.YELLOW;
        } else if (colorNumber == 6 || colorNumber == 7) {
            return Color.GREEN;
        } else {
            return Color.CYAN;
        }
    }

    /**
     * Check if player has extra attempts for game.
     * If there is attempt game starts
     * Ball moves randomly depends on generated direction. And change its way if collision
     * with rocket, walls, bricks.
     * Also check if player win or lose
     *
     * @param ball   GOval that used for playing game like ball
     * @param bricks ArrayList with all present bricks on screen
     */
    private void moveBall(GOval ball, ArrayList<GRect> bricks) {
        if (gameCountGame < NTURNS) {
            waitForClick();
            generateDirectionForBall();
            while (true) {
                ball.move(vx, vy);
                GObject collider = getCollidingObject(ball);

                if (collider == rocket) {
                    changeYDirection();
                } else if (collider != null) {
                    removeBricks(bricks, collider);
                    changeYDirection();
                }
                checkTouchWall(ball, bricks);
                if (checkIfBricksLeft(bricks)) {
                    showMessage("You win"); // if there are no any bricks
                    break;
                }
                pause(PAUSE_TIME);
            }
        } else { // if there are no any attempts
            showMessage("You lose");
        }
    }

    /**
     * Get random direction for ball path
     */
    private void generateDirectionForBall() {
        RandomGenerator rgen = RandomGenerator.getInstance();
        vy = 3;
        vx = rgen.nextDouble(1.0, 3.0);
        if (rgen.nextBoolean(0.5))
            vx = -vx;
    }

    /**
     * Show message in the middle of screen
     *
     * @param text showed message on the screen
     */
    private void showMessage(String text) {
        GLabel message = new GLabel(text);
        message.setFont("Verdana-24");
        message.setLocation(getWidth() / 2.0 - message.getWidth() / 2.0,
                getHeight() / 2.0 - message.getHeight() / 2.0);
        add(message);
    }

    /**
     * Check size of ArrayList with bricks. If there are no bricks game should stop
     *
     * @param bricks ArrayList with bricks
     * @return boolean value. True if there are no any bricks
     */
    private boolean checkIfBricksLeft(ArrayList<GRect> bricks) {
        return bricks.size() == 0;
    }

    /**
     * Remove brick which was collision with ball from the screen and from ArrayList with all bricks
     *
     * @param bricks   ArrayList with bricks
     * @param collider GObject which was collision with ball
     */
    private void removeBricks(ArrayList<GRect> bricks, GObject collider) {
        bricks.remove(collider);
        remove(collider);
    }

    /**
     * Check four side of the ball, topLeft, topRight, downLeft, downRight.
     * If there is any object on this pointers return this object
     * If there is no any object return null
     *
     * @param ball GOval which used like ball in game
     * @return GObject which was faced with ball
     */
    private GObject getCollidingObject(GOval ball) {
        GObject objectNearBall = null;
        GObject topLeft = getElementAt(ball.getX(), ball.getY());
        GObject topRight = getElementAt(ball.getX() + BALL_RADIUS * 2, ball.getY());
        GObject downLeft = getElementAt(ball.getX(), ball.getY() + BALL_RADIUS * 2);
        GObject downRight = getElementAt(ball.getX() + BALL_RADIUS * 2, ball.getY() + BALL_RADIUS * 2);

        if (downLeft != null && vy > 0) {
            objectNearBall = downLeft;
        } else if (downRight != null && vy > 0) {
            objectNearBall = downRight;
        } else if (topLeft != null && vy < 0) {
            objectNearBall = topLeft;
        } else if (topRight != null && vy < 0) {
            objectNearBall = topRight;
        }

        return objectNearBall;
    }

    /**
     * Check if ball touched every side of screen. After faced right or left wall change X direction,
     * after faced top wall change Y direction, after faced bottom wall failed player attempt
     *
     * @param ball   GOval which used like ball in game
     * @param bricks ArrayList with bricks
     */
    private void checkTouchWall(GOval ball, ArrayList<GRect> bricks) {
        boolean touchRightWall = ball.getX() <= 0;
        boolean touchLeftWall = ball.getX() >= getWidth() - BALL_RADIUS * 2;
        boolean touchTopWall = ball.getY() <= 0;
        boolean touchBottomWall = ball.getY() >= getHeight();

        if (touchRightWall || touchLeftWall) {
            vx *= -1;
        } else if (touchTopWall) {
            changeYDirection();
        } else if (touchBottomWall) {
            gameCountGame++;
            remove(ball);
            moveBall(createBall(), bricks);
        }
    }

    /**
     * Change ball direction on Y coordinate
     */
    private void changeYDirection() {
        vy *= -1;
    }

    /**
     * Create GOval in the middle of the screen which used like ball for game
     *
     * @return GOval ball
     */
    private GOval createBall() {
        GOval ball = new GOval(getWidth() / 2.0 - BALL_RADIUS, getHeight() / 2.0 - BALL_RADIUS,
                BALL_RADIUS * 2, BALL_RADIUS * 2);
        ball.setFilled(true);
        ball.setFillColor(Color.BLACK);
        add(ball);
        return ball;
    }

    /**
     * Change rocket location depends on mouse moved
     *
     * @param mouseEvent the event to be processed
     */
    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        double newX = mouseEvent.getX() - rocket.getWidth() / 2.0;
        if (newX > 0 && newX < getWidth() - PADDLE_WIDTH) { // don't allow paddle moves over edges of screen
            rocket.setLocation(newX, rocket.getY());
        }
    }

    /**
     * Crate rocket for game. Player use it by dragged mouse
     *
     * @return GRect which used like rocket for game
     */
    private GRect createRocket() {
        GRect rocket = new GRect(getWidth() / 2.0 - PADDLE_WIDTH / 2.0,
                getHeight() - PADDLE_Y_OFFSET - PADDLE_HEIGHT, PADDLE_WIDTH, PADDLE_HEIGHT);
        rocket.setFilled(true);
        rocket.setFillColor(Color.BLACK);
        add(rocket);
        return rocket;
    }
}

