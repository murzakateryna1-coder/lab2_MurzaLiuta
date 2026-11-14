
import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class breakout extends GraphicsProgram {

    /** Width and height of application window in pixels */
    public static final int APPLICATION_WIDTH = 400;
    public static final int APPLICATION_HEIGHT = 600;

    /** Dimensions of game board (usually the same) */
    private static final int WIDTH = APPLICATION_WIDTH;
    private static final int HEIGHT = APPLICATION_HEIGHT;

    /** Dimensions of the paddle */
    private static final int PADDLE_WIDTH = 60;
    private static final int PADDLE_HEIGHT = 10;

    /** Offset of the paddle up from the bottom */
    private static final int PADDLE_Y_OFFSET = 30;

    /** Number of bricks per row */
    private static final int NBRICKS_PER_ROW = 10;

    /** Number of rows of bricks */
    private static final int NBRICK_ROWS = 10;

    /** Separation between bricks */
    private static final int BRICK_SEP = 4;

    /** Width of a brick */
    private static final int BRICK_WIDTH =
            (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

    /** Height of a brick */
    private static final int BRICK_HEIGHT = 8;

    /** Radius of the ball in pixels */
    private static final int BALL_RADIUS = 10;

    /** Offset of the top brick row from the top */
    private static final int BRICK_Y_OFFSET = 70;

    /** Number of turns */
    private static final int NTURNS = 3;

    /** Ракетка (змінна класу, щоб її бачили всі методи) */
    private GRect paddle;

    /** М'яч */
    private GOval ball;

    /** Швидкість м'яча: зміна позиції за один крок */
    private double vx, vy;

    /** Генератор випадкових чисел */
    private RandomGenerator rgen = RandomGenerator.getInstance();

    /* Method: run() */
    /** Runs the Breakout program. */
    public void run() {
        setupBricks();
        setupPaddle();
        addMouseListeners();
        setupBall();
        playGame();
    }

    /** Створює ряди цеглин по центру вікна. */
    private void setupBricks() {
        // загальна ширина одного ряду цеглин разом з проміжками
        int rowWidth = NBRICKS_PER_ROW * BRICK_WIDTH + (NBRICKS_PER_ROW - 1) * BRICK_SEP;

        // x першої цеглини, щоб весь ряд стояв по центру вікна
        int startX = (WIDTH - rowWidth) / 2;

        for (int row = 0; row < NBRICK_ROWS; row++) {
            for (int col = 0; col < NBRICKS_PER_ROW; col++) {

                int x = startX + col * (BRICK_WIDTH + BRICK_SEP);
                int y = BRICK_Y_OFFSET + row * (BRICK_HEIGHT + BRICK_SEP);

                GRect brick = new GRect(x, y, BRICK_WIDTH, BRICK_HEIGHT);
                brick.setFilled(true);

                // колір залежить від номера ряду (по два ряди кожного кольору)
                if (row < 2) {
                    brick.setColor(Color.RED);
                } else if (row < 4) {
                    brick.setColor(Color.ORANGE);
                } else if (row < 6) {
                    brick.setColor(Color.YELLOW);
                } else if (row < 8) {
                    brick.setColor(Color.GREEN);
                } else {
                    brick.setColor(Color.CYAN);
                }

                add(brick);
            }
        }
    }

    /** Створює ракетку внизу по центру. */
    private void setupPaddle() {
        int x = (WIDTH - PADDLE_WIDTH) / 2;
        int y = HEIGHT - PADDLE_Y_OFFSET - PADDLE_HEIGHT;

        paddle = new GRect(x, y, PADDLE_WIDTH, PADDLE_HEIGHT);
        paddle.setFilled(true);
        add(paddle);
    }

    /** Рухає ракетку за мишею (лише по x). */
    public void mouseMoved(MouseEvent e) {
        // ракетка центрується по курсору
        int x = e.getX() - PADDLE_WIDTH / 2;

        // не виходимо за ліву і праву стінки
        if (x < 0) {
            x = 0;
        }
        if (x > WIDTH - PADDLE_WIDTH) {
            x = WIDTH - PADDLE_WIDTH;
        }

        paddle.setLocation(x, paddle.getY());
    }

    /** Створює м'яч по центру вікна і задає йому початкову швидкість. */
    private void setupBall() {
        // GOval ставиться за лівий верхній кут, тому віднімаємо радіус
        int x = WIDTH / 2 - BALL_RADIUS;
        int y = HEIGHT / 2 - BALL_RADIUS;

        ball = new GOval(x, y, 2 * BALL_RADIUS, 2 * BALL_RADIUS);
        ball.setFilled(true);
        add(ball);

        // м'яч летить вниз
        vy = 3.0;

        // vx випадкове від 1.0 до 3.0, а потім випадково вліво або вправо
        vx = rgen.nextDouble(1.0, 3.0);
        if (rgen.nextBoolean(0.5)) {
            vx = -vx;
        }
    }

    /** Головний цикл гри: м'яч рухається, потім коротка пауза. */
    private void playGame() {
        while (true) {
            moveBall();
            pause(10);
        }
    }

    /** Робить один крок м'яча і відбиває його від стінок. */
    private void moveBall() {
        ball.move(vx, vy);

        // права стінка: права межа м'яча = x + діаметр
        if (ball.getX() + 2 * BALL_RADIUS >= WIDTH) {
            vx = -vx;
        }

        // ліва стінка
        if (ball.getX() <= 0) {
            vx = -vx;
        }

        // верхня стінка
        if (ball.getY() <= 0) {
            vy = -vy;
        }

        // нижня стінка (поки що теж відбиває, пізніше тут буде програш)
        if (ball.getY() + 2 * BALL_RADIUS >= HEIGHT) {
            vy = -vy;
        }
    }

}