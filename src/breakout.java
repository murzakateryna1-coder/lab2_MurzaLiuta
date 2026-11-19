import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.sound.sampled.*;

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

    /** Пауза між кроками м'яча в мілісекундах (менше = швидша гра) */
    private static final int PAUSE_TIME = 10;

    /** Очки за одну цеглину та поріг для 2-го рівня */
    private static final int BRICK_POINTS = 10;
    private static final int LEVEL_2_MIN_SCORE = 50;

    /** Кнопка "Почати" на стартовій сторінці */
    private static final int START_W = 160;
    private static final int START_H = 45;
    private static final int START_X = (WIDTH - START_W) / 2;
    private static final int START_Y = 415;

    /** Налаштування фото сердечок (життів) у правому верхньому кутку */
    private static final String HEART_IMAGE = "heart.png";
    private static final int HEART_SIZE = 35;
    private static final int HEART_SEP = 6;
    private static final int HEART_Y = 20;

    /** Налаштування фото монети у лівому верхньому кутку (навпроти сердечок) */
    private static final String COIN_IMAGE = "coin.png";
    private static final int COIN_SIZE = 35;

    /** Назви аудіофайлів (.wav) у корені проєкту */
    private static final String SOUND_BG_MUSIC = "sounds/bg_music.wav";
    private static final String SOUND_BRICK_HIT = "sounds/bricks.wav";
    private static final String SOUND_WIN = "sounds/win.wav";
    private static final String SOUND_LOSE = "lose.wav";

    /** Аудіокліпи */
    private Clip bg_music;
    private Clip bricks;
    private Clip win;
    private Clip loseClip;

    /** Ракетка (змінна класу, щоб її бачили всі методи) */
    private GRect paddle;

    /** М'яч */
    private GOval ball;

    /** Три сердечка для відображення життів */
    private GImage heart1;
    private GImage heart2;
    private GImage heart3;

    /** Значок монети та текст поточного рахунку */
    private GImage coinIcon;
    private GLabel scoreLabel;

    /** Поточна кількість очок */
    private int score = 0;

    /** Швидкість м'яча: зміна позиції за один крок */
    private double vx, vy;

    /** Скільки цеглин ще залишилось */
    private int bricksLeft;

    /** Чи натиснули кнопку "Почати" */
    private boolean gameStarted = false;

    /** Генератор випадкових чисел */
    private RandomGenerator rgen = RandomGenerator.getInstance();

    /** Встановлює розмір вікна та завантажує звуки при запуску */
    public void init() {
        setSize(APPLICATION_WIDTH, APPLICATION_HEIGHT);

        bg_music = loadSound(SOUND_BG_MUSIC);
        bricks = loadSound(SOUND_BRICK_HIT);
        win = loadSound(SOUND_WIN);
        loseClip = loadSound(SOUND_LOSE);
    }
    

    /* Method: run() */
    /** Runs the Breakout program. */
    public void run() {
        // Враховуємо рамки та меню вікна, щоб ігрове поле було рівно 400x600
        int borderW = APPLICATION_WIDTH - getWidth();
        int borderH = APPLICATION_HEIGHT - getHeight();
        if (borderW > 0 || borderH > 0) {
            setSize(APPLICATION_WIDTH + borderW, APPLICATION_HEIGHT + borderH);
            pause(50);
        }

        addMouseListeners();
        showStartScreen();
        startBgMusic();
        setupScore();
        setupHearts();
        setupBricks();
        setupPaddle();
        playGame();
    }

    /** Стартова сторінка: назва гри, правила і кнопка "Почати". Чекає, поки натиснуть кнопку. */
    private void showStartScreen() {
        // темний фон
        GRect background = new GRect(WIDTH, HEIGHT);
        background.setColor(new Color(30, 30, 60));
        background.setFilled(true);
        add(background, 0, 0);

        // назва гри
        addCenteredText("BREAKOUT", 115, "SansSerif-bold-48", Color.WHITE);
        addCenteredText("Збий усі цеглини!", 155, "SansSerif-22", Color.YELLOW);

        // п'ять кольорових цеглин для краси (кольори такі самі, як у грі)
        int decorWidth = 60;
        int decorSep = 6;
        int decorX = (WIDTH - (5 * decorWidth + 4 * decorSep)) / 2;
        for (int i = 0; i < 5; i++) {
            GRect decor = new GRect(decorX + i * (decorWidth + decorSep), 185, decorWidth, 18);
            decor.setColor(getBrickColor(i * 2));
            decor.setFilled(true);
            add(decor);
        }

        // правила гри та умова переходу на 2-й рівень
        addCenteredText("Рухайте мишею ракетку", 240, "SansSerif-15", Color.WHITE);
        addCenteredText("Не дайте м'ячу впасти. У вас " + NTURNS + " життя", 265, "SansSerif-15", Color.WHITE);
        addCenteredText("1 цеглина = " + BRICK_POINTS + " очок (усього 100 цеглин)", 295, "SansSerif-bold-15", Color.YELLOW);
        addCenteredText("Поки не наберете " + LEVEL_2_MIN_SCORE + " очок,", 325, "SansSerif-15", new Color(100, 255, 180));
        addCenteredText("на 2-й рівень не переходите", 347, "SansSerif-15", new Color(100, 255, 180));
        addCenteredText("(2-го рівня поки що немає)", 372, "SansSerif-14", Color.LIGHT_GRAY);

        // кнопка "Почати"
        GRect button = new GRect(START_W, START_H);
        button.setColor(new Color(0, 200, 120));
        button.setFilled(true);
        add(button, START_X, START_Y);

        GLabel buttonText = new GLabel("Почати");
        buttonText.setFont("SansSerif-bold-22");
        buttonText.setColor(Color.WHITE);
        add(buttonText, START_X + (START_W - buttonText.getWidth()) / 2,
                START_Y + (START_H + buttonText.getAscent()) / 2 - 3);

        // чекаємо, поки гравець натисне кнопку
        while (!gameStarted) {
            pause(50);
        }

        // прибираємо стартову сторінку
        removeAll();
    }

    /** Додає значок монети та лічильник очок у лівому верхньому кутку (навпроти сердечок). */
    private void setupScore() {
        int leftMargin = 15;

        coinIcon = new GImage(COIN_IMAGE);
        coinIcon.setSize(COIN_SIZE, COIN_SIZE);
        add(coinIcon, leftMargin, HEART_Y);

        score = 0;
        scoreLabel = new GLabel("Очки: " + score);
        scoreLabel.setFont("SansSerif-bold-18");
        scoreLabel.setColor(Color.BLACK);
        add(scoreLabel, leftMargin + COIN_SIZE + 8,
                HEART_Y + (COIN_SIZE + scoreLabel.getAscent()) / 2.0 - 2);
    }

    /** Додає три фото сердечок у правому верхньому кутку. */
    private void setupHearts() {
        int rightMargin = 15;

        heart3 = new GImage(HEART_IMAGE);
        heart3.setSize(HEART_SIZE, HEART_SIZE);
        add(heart3, WIDTH - rightMargin - HEART_SIZE, HEART_Y);

        heart2 = new GImage(HEART_IMAGE);
        heart2.setSize(HEART_SIZE, HEART_SIZE);
        add(heart2, WIDTH - rightMargin - 2 * HEART_SIZE - HEART_SEP, HEART_Y);

        heart1 = new GImage(HEART_IMAGE);
        heart1.setSize(HEART_SIZE, HEART_SIZE);
        add(heart1, WIDTH - rightMargin - 3 * HEART_SIZE - 2 * HEART_SEP, HEART_Y);
    }

    /** Пише текст по центру вікна заданим кольором. */
    private void addCenteredText(String text, double y, String font, Color color) {
        GLabel label = new GLabel(text);
        label.setFont(font);
        label.setColor(color);
        add(label, (WIDTH - label.getWidth()) / 2, y);
    }

    /** Клік мишею: якщо влучили в кнопку "Почати", починаємо гру. */
    public void mouseClicked(MouseEvent e) {
        if (!gameStarted
                && e.getX() >= START_X && e.getX() <= START_X + START_W
                && e.getY() >= START_Y && e.getY() <= START_Y + START_H) {
            gameStarted = true;
        }
    }

    /** Колір цеглини залежить від номера ряду (по два ряди кожного кольору). */
    private Color getBrickColor(int row) {
        if (row < 2) {
            return new Color(233, 30, 99);      // малиновий
        } else if (row < 4) {
            return new Color(156, 39, 176);     // фіолетовий
        } else if (row < 6) {
            return new Color(33, 150, 243);     // синій
        } else if (row < 8) {
            return new Color(0, 150, 136);      // бірюзовий
        } else {
            return new Color(139, 195, 74);     // салатовий
        }
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

                // колір залежить від номера ряду
                brick.setColor(getBrickColor(row));

                add(brick);

                // рахуємо цеглини
                bricksLeft++;
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
        // на стартовій сторінці ракетки ще немає
        if (paddle == null) {
            return;
        }

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

    /** Уся гра: життя, м'ячі, перемога або програш. */
    private void playGame() {
        int lives = NTURNS;

        // грає, поки є життя і є цеглини
        while (lives > 0 && bricksLeft > 0) {
            setupBall();
            pause(1000); // час приготуватись

            // цикл одного м'яча: поки він не впав на нижню стінку
            while (bricksLeft > 0 && ball.getY() + 2 * BALL_RADIUS < HEIGHT) {
                moveBall();
                pause(PAUSE_TIME);
            }

            remove(ball);

            // життя втрачається, тільки якщо цеглини ще є
            if (bricksLeft > 0) {
                lives--;

                // прибираємо одне сердечко з екрана
                if (lives == 2 && heart3 != null) {
                    remove(heart3);
                    heart3 = null;
                } else if (lives == 1 && heart2 != null) {
                    remove(heart2);
                    heart2 = null;
                } else if (lives == 0 && heart1 != null) {
                    remove(heart1);
                    heart1 = null;
                }
            }
        }

        // зупиняємо фонову музику в кінці гри
        stopBgMusic();

        if (bricksLeft == 0) {
            playSound(win);
            addCenteredText("YOU WIN! Очки: " + score, HEIGHT / 2.0, "SansSerif-bold-22", new Color(0, 150, 80));
            addCenteredText("2-го рівня поки що немає", HEIGHT / 2.0 + 30, "SansSerif-16", Color.DARK_GRAY);
        } else if (score >= LEVEL_2_MIN_SCORE) {
            playSound(loseClip);
            addCenteredText("GAME OVER! Очки: " + score, HEIGHT / 2.0, "SansSerif-bold-22", Color.RED);
            addCenteredText("50+ очок набрано, але 2-го рівня поки немає", HEIGHT / 2.0 + 30, "SansSerif-15", Color.DARK_GRAY);
        } else {
            playSound(loseClip);
            addCenteredText("GAME OVER! Очки: " + score + " / " + LEVEL_2_MIN_SCORE, HEIGHT / 2.0, "SansSerif-bold-20", Color.RED);
            addCenteredText("Не вистачило очок для 2-го рівня", HEIGHT / 2.0 + 30, "SansSerif-16", Color.DARK_GRAY);
        }
    }

    /** Робить один крок м'яча, відбиває його від стінок і перевіряє колізії. */
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

        // нижню стінку тут не перевіряємо: її обробляє playGame (м'яч втрачено)

        // перевіряємо, чи м'яч у щось влучив
        GObject collider = getCollidingObject();

        if (collider == paddle) {
            // відбиваємо вгору; abs не дає м'ячу "приклеїтися" до ракетки
            vy = -Math.abs(vy);
        } else if (collider != null
                && collider != heart1 && collider != heart2 && collider != heart3
                && collider != coinIcon && collider != scoreLabel) {
            // це цеглина: прибираємо її, додаємо 10 очок, вмикаємо звук і відбиваємо м'яч
            remove(collider);
            bricksLeft--;
            score += BRICK_POINTS;
            scoreLabel.setLabel("Очки: " + score);
            playSound(bricks);
            vy = -vy;
        }
    }

    /**
     * Перевіряє 4 кути квадрата, в який вписано м'яч.
     * Повертає об'єкт, у який влучив м'яч, або null, якщо нічого немає.
     */
    private GObject getCollidingObject() {
        double x = ball.getX();
        double y = ball.getY();
        double d = 2 * BALL_RADIUS;

        // лівий верхній кут
        GObject obj = getElementAt(x, y);
        if (obj != null) {
            return obj;
        }

        // правий верхній кут
        obj = getElementAt(x + d, y);
        if (obj != null) {
            return obj;
        }

        // лівий нижній кут
        obj = getElementAt(x, y + d);
        if (obj != null) {
            return obj;
        }

        // правий нижній кут (якщо тут null, то колізії немає)
        obj = getElementAt(x + d, y + d);
        return obj;
    }

    // =====================================
    // РОБОТА ЗІ ЗВУКАМИ (.WAV)
    // =====================================

    /** Завантажує звуковий файл .wav у пам'ять. */
    private Clip loadSound(String filename) {
        try {
            File file = new File(filename);
            if (!file.exists()) {
                return null;
            }
            AudioInputStream audio = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            audio.close();
            return clip;
        } catch (Exception e) {
            return null;
        }
    }

    /** Відтворює короткий звуковий ефект з початку. */
    private void playSound(Clip sound) {
        if (sound != null) {
            if (sound.isRunning()) {
                sound.stop();
            }
            sound.setFramePosition(0);
            sound.start();
        }
    }

    /** Запускає фонову музику по колу. */
    private void startBgMusic() {
        if (bg_music != null) {
        	bg_music.setFramePosition(0);
        	bg_music.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    /** Зупиняє фонову музику. */
    private void stopBgMusic() {
        if (bg_music != null && bg_music.isRunning()) {
        	bg_music.stop();
        }
    }
}
