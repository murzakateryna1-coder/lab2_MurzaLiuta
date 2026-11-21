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
    public static final int APPLICATION_WIDTH = 600;
    public static final int APPLICATION_HEIGHT = 700;

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

    /** Налаштування 2-го рівня */
    private static final int PADDLE_WIDTH_LEVEL2 = 40;
    private static final double LEVEL2_SPEED = 1.5;   // м'яч швидший у 1.5 раза

    /** Рухома перешкода */
    private static final int OBSTACLE_WIDTH = 100;
    private static final int OBSTACLE_HEIGHT = 12;
    private static final int OBSTACLE_Y = 260;
    private static final double OBSTACLE_SPEED = 2;

    /** Кут відбиття від ракетки: частка швидкості по x на краю ракетки (0.8 = дуже похило) */
    private static final double BOUNCE_ANGLE_FACTOR = 0.8;

    /** Бонус "ширша ракетка": шанс випадання, розмір, швидкість падіння,
     *  на скільки пікселів ракетка росте за кожен бонус і максимальна ширина */
    private static final double BONUS_CHANCE = 0.2;
    private static final int BONUS_RADIUS = 10;
    private static final double BONUS_SPEED = 2.5;
    private static final int PADDLE_BONUS_EXTRA = 20;
    private static final int MAX_PADDLE_WIDTH = 200;

    /** Пауза між кроками м'яча в мілісекундах (менше = швидша гра) */
    private static final int PAUSE_TIME = 10;

    /** Очки за одну цеглину та поріг для переходу на 2-й рівень */
    private static final int BRICK_POINTS = 10;
    private static final int LEVEL_2_MIN_SCORE = 50;
    private static final int LEVEL_3_MIN_SCORE = 100;

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

    /** Назви аудіофайлів (.wav) */
    private static final String SOUND_BG_MUSIC = "sounds/bg_music.wav";
    private static final String SOUND_BRICK_HIT = "sounds/bricks.wav";
    private static final String SOUND_PADDLE_HIT = "sounds/paddle.wav";
    private static final String SOUND_WIN = "sounds/win.wav";
    private static final String SOUND_LOSE = "lose.wav";

    /** Аудіокліпи */
    private Clip bg_music;
    private Clip bricks;
    private Clip paddleSound;
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

    /** Поточний рівень, життя, коефіцієнт швидкості та ширина ракетки */
    private int level = 1;
    private int lives = NTURNS;
    private double speedFactor = 1.0;
    private int paddleWidth = PADDLE_WIDTH;

    /** Бонус, що падає (null = бонусу на екрані немає) */
    private GOval bonus;

    /** Рухома перешкода та її швидкість */
    private GRect obstacle;
    private double obstacleVx;

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
        paddleSound = loadSound(SOUND_PADDLE_HIT);
        win = loadSound(SOUND_WIN);
        loseClip = loadSound(SOUND_LOSE);
    }

    /** Runs the Breakout program. */
    public void run() {
        // Враховуємо рамки та меню вікна, щоб ігрове поле відповідало заданим розмірам
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

        playLevel();                 // 1-й рівень
        if (lives > 0) {             // набрано 50 очок і ще є життя, переходимо далі
            startLevel2();
            playLevel();             // 2-й рівень
        }
        if (lives > 0) {             // набрано 100 очок і ще є життя
            startLevel3();
            playLevel();             // 3-й рівень
        }
        finishGame();
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

        // правила гри
        addCenteredText("Рухайте мишею ракетку", 240, "SansSerif-15", Color.WHITE);
        addCenteredText("Не дайте м'ячу впасти. У вас " + NTURNS + " життя на кожному рівні", 265, "SansSerif-15", Color.WHITE);
        addCenteredText("1 цеглина = " + BRICK_POINTS + " очок (усього 100 цеглин)", 295, "SansSerif-bold-15", Color.YELLOW);
        addCenteredText("Набери " + LEVEL_2_MIN_SCORE + " очок і перейдеш на 2-й рівень", 325, "SansSerif-15", new Color(100, 255, 180));
        addCenteredText("2-й рівень: швидший м'яч, менша ракетка", 347, "SansSerif-15", Color.LIGHT_GRAY);
        addCenteredText("і рухома перешкода!", 372, "SansSerif-15", Color.LIGHT_GRAY);
        addCenteredText("3-й рівень: цеглини міцні, потрібно 2 удари", 397, "SansSerif-14", Color.LIGHT_GRAY);

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

                // колір залежить від номера ряду; на 3-му рівні цеглини темніші (міцні, 2 удари)
                if (level == 3) {
                    brick.setColor(getBrickColor(row).darker());
                } else {
                    brick.setColor(getBrickColor(row));
                }

                add(brick);

                // рахуємо цеглини
                bricksLeft++;
            }
        }
    }

    /** Створює ракетку внизу по центру. */
    private void setupPaddle() {
        int x = (WIDTH - paddleWidth) / 2;
        int y = HEIGHT - PADDLE_Y_OFFSET - PADDLE_HEIGHT;

        paddle = new GRect(x, y, paddleWidth, PADDLE_HEIGHT);
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
        int x = e.getX() - paddleWidth / 2;

        // не виходимо за ліву і праву стінки
        if (x < 0) {
            x = 0;
        }
        if (x > WIDTH - paddleWidth) {
            x = WIDTH - paddleWidth;
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

        // м'яч летить вниз, швидкість залежить від рівня
        vy = 3.0 * speedFactor;

        // vx випадкове від 1.0 до 3.0, а потім випадково вліво або вправо
        vx = rgen.nextDouble(1.0, 3.0) * speedFactor;
        if (rgen.nextBoolean(0.5)) {
            vx = -vx;
        }
    }

    /** Чи завершено поточний рівень: 1-й і 2-й за очками, 3-й коли збито всі цеглини. */
    private boolean levelDone() {
        if (level == 1) {
            return score >= LEVEL_2_MIN_SCORE;
        }
        if (level == 2) {
            return score >= LEVEL_3_MIN_SCORE;
        }
        return bricksLeft == 0;
    }

    /** Один рівень: грає, поки є життя і рівень не завершено. */
    private void playLevel() {
        while (lives > 0 && !levelDone()) {
            setupBall();
            pause(1000); // час приготуватись

            // цикл одного м'яча: поки він не впав на нижню стінку
            while (!levelDone() && ball.getY() + 2 * BALL_RADIUS < HEIGHT) {
                moveBall();
                moveObstacle();
                moveBonus();
                pause(PAUSE_TIME);
            }

            removeBonus();
            remove(ball);

            // життя втрачається, тільки якщо рівень ще не завершено
            if (!levelDone()) {
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
    }

    /** Готує 2-й рівень: швидший м'яч, менша ракетка, нові цеглини і перешкода. */
    private void startLevel2() {
        level = 2;
        removeBonus();
        speedFactor = LEVEL2_SPEED;
        paddleWidth = PADDLE_WIDTH_LEVEL2;   // початкова ширина ракетки на 2-му рівні

        resetLives();
        clearBricks();
        remove(paddle);
        setupPaddle();
        setupBricks();
        setupObstacle();

        showLevelBanner("РІВЕНЬ 2", "Швидший м'яч, менша ракетка, рухома перешкода");
    }

    /** Готує 3-й рівень: нові цеглини на 2 удари, життя і ракетка як на початку рівня. */
    private void startLevel3() {
        level = 3;
        removeBonus();
        paddleWidth = PADDLE_WIDTH_LEVEL2;   // початкова ширина ракетки (бонуси скинуто)

        resetLives();
        clearBricks();
        remove(paddle);
        setupPaddle();
        setupBricks();

        showLevelBanner("РІВЕНЬ 3", "Цеглини міцні: потрібно 2 удари!");
    }

    /** Відбиття від ракетки: чим далі від центру влучила кулька, тим похиліше вона летить. */
    private void bounceFromPaddle() {
        double speed = Math.sqrt(vx * vx + vy * vy);

        // offset: -1 = лівий край ракетки, 0 = центр, 1 = правий край
        double ballCenter = ball.getX() + BALL_RADIUS;
        double paddleCenter = paddle.getX() + paddle.getWidth() / 2;
        double offset = (ballCenter - paddleCenter) / (paddle.getWidth() / 2);
        if (offset > 1) {
            offset = 1;
        }
        if (offset < -1) {
            offset = -1;
        }

        double newVx = offset * BOUNCE_ANGLE_FACTOR * speed;

        // щоб кулька не літала суворо вертикально
        double minVx = 0.5 * speedFactor;
        if (Math.abs(newVx) < minVx) {
            if (offset >= 0) {
                newVx = minVx;
            } else {
                newVx = -minVx;
            }
        }

        vx = newVx;
        // загальна швидкість лишається такою самою
        vy = -Math.sqrt(speed * speed - vx * vx);
    }

    /** Іноді з розбитої цеглини випадає бонус (одночасно на екрані не більше одного). */
    private void maybeDropBonus(GObject brick) {
        if (bonus == null && rgen.nextBoolean(BONUS_CHANCE)) {
            double x = brick.getX() + BRICK_WIDTH / 2.0 - BONUS_RADIUS;
            bonus = new GOval(x, brick.getY(), 2 * BONUS_RADIUS, 2 * BONUS_RADIUS);
            bonus.setFilled(true);
            bonus.setColor(new Color(255, 193, 7));
            add(bonus);
        }
    }

    /** Рухає бонус вниз і ловить його ракеткою. */
    private void moveBonus() {
        if (bonus == null) {
            return;
        }

        bonus.move(0, BONUS_SPEED);

        double bonusCenter = bonus.getX() + BONUS_RADIUS;
        boolean aboveOrOnPaddle = bonusCenter >= paddle.getX()
                && bonusCenter <= paddle.getX() + paddle.getWidth();
        boolean touchesPaddle = bonus.getY() + 2 * BONUS_RADIUS >= paddle.getY()
                && bonus.getY() <= paddle.getY() + PADDLE_HEIGHT;

        if (aboveOrOnPaddle && touchesPaddle) {
            removeBonus();
            growPaddle();
        } else if (bonus.getY() >= HEIGHT) {
            removeBonus();
        }
    }

    /** Кожен спійманий бонус робить ракетку ширшою (до MAX_PADDLE_WIDTH). */
    private void growPaddle() {
        int newWidth = paddleWidth + PADDLE_BONUS_EXTRA;
        if (newWidth > MAX_PADDLE_WIDTH) {
            newWidth = MAX_PADDLE_WIDTH;
        }
        int delta = newWidth - paddleWidth;
        paddleWidth = newWidth;
        paddle.setSize(paddleWidth, PADDLE_HEIGHT);

        // розширюємо в обидва боки і не виходимо за стінки
        double x = paddle.getX() - delta / 2.0;
        if (x < 0) {
            x = 0;
        }
        if (x > WIDTH - paddleWidth) {
            x = WIDTH - paddleWidth;
        }
        paddle.setLocation(x, paddle.getY());
    }

    /** Прибирає бонус, що падає, з екрана. */
    private void removeBonus() {
        if (bonus != null) {
            remove(bonus);
            bonus = null;
        }
    }

    /** Номер ряду цеглини, обчислений за її координатою y. */
    private int getBrickRow(GObject brick) {
        return (int) Math.round((brick.getY() - BRICK_Y_OFFSET) / (BRICK_HEIGHT + BRICK_SEP));
    }

    /** Чи це міцна цеглина (3-й рівень, ще не було удару): її колір темніший за звичайний. */
    private boolean isStrongBrick(GObject brick) {
        if (level != 3) {
            return false;
        }
        return !brick.getColor().equals(getBrickColor(getBrickRow(brick)));
    }

    /** Повертає всі 3 життя: прибирає старі сердечка (якщо лишились) і малює нові. */
    private void resetLives() {
        if (heart1 != null) {
            remove(heart1);
        }
        if (heart2 != null) {
            remove(heart2);
        }
        if (heart3 != null) {
            remove(heart3);
        }

        lives = NTURNS;
        setupHearts();
    }

    /** Показує на 2.5 секунди назву рівня і підказку. */
    private void showLevelBanner(String titleText, String hintText) {
        GLabel title = new GLabel(titleText);
        title.setFont("SansSerif-bold-40");
        title.setColor(new Color(30, 30, 60));
        add(title, (WIDTH - title.getWidth()) / 2, 340);

        GLabel hint = new GLabel(hintText);
        hint.setFont("SansSerif-16");
        hint.setColor(Color.DARK_GRAY);
        add(hint, (WIDTH - hint.getWidth()) / 2, 370);

        pause(2500);
        remove(title);
        remove(hint);
    }

    /** Повертає об'єкт у точці, але не рахує кульки і бонус. */
    private GObject getObjectAt(double x, double y) {
        GObject obj = getElementAt(x, y);
        if (obj == ball || obj == bonus) {
            return null;
        }
        return obj;
    }

    /** Прибирає всі цеглини, що залишились з 1-го рівня (шукає їх за координатами сітки). */
    private void clearBricks() {
        int rowWidth = NBRICKS_PER_ROW * BRICK_WIDTH + (NBRICKS_PER_ROW - 1) * BRICK_SEP;
        int startX = (WIDTH - rowWidth) / 2;

        for (int row = 0; row < NBRICK_ROWS; row++) {
            for (int col = 0; col < NBRICKS_PER_ROW; col++) {
                double x = startX + col * (BRICK_WIDTH + BRICK_SEP) + BRICK_WIDTH / 2.0;
                double y = BRICK_Y_OFFSET + row * (BRICK_HEIGHT + BRICK_SEP) + BRICK_HEIGHT / 2.0;
                GObject brick = getElementAt(x, y);
                if (brick != null) {
                    remove(brick);
                }
            }
        }
        bricksLeft = 0;
    }

    /** Створює сіру рухому перешкоду посередині поля. */
    private void setupObstacle() {
        obstacle = new GRect((WIDTH - OBSTACLE_WIDTH) / 2, OBSTACLE_Y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
        obstacle.setFilled(true);
        obstacle.setColor(Color.DARK_GRAY);
        add(obstacle);
        obstacleVx = OBSTACLE_SPEED;
    }

    /** Рухає перешкоду вліво-вправо і розвертає її біля стінок. */
    private void moveObstacle() {
        if (obstacle == null) {
            return;
        }

        obstacle.move(obstacleVx, 0);

        if (obstacle.getX() <= 0 || obstacle.getX() + OBSTACLE_WIDTH >= WIDTH) {
            obstacleVx = -obstacleVx;
        }
    }

    /** Кінець гри: перемога або програш. */
    private void finishGame() {
        stopBgMusic();

        if (lives > 0) {
            playSound(win);
            addCenteredText("YOU WIN! Очки: " + score, HEIGHT / 2.0, "SansSerif-bold-22", new Color(0, 150, 80));
            addCenteredText("Ти пройшов усі три рівні!", HEIGHT / 2.0 + 30, "SansSerif-16", Color.DARK_GRAY);
        } else {
            playSound(loseClip);
            addCenteredText("GAME OVER! Очки: " + score, HEIGHT / 2.0, "SansSerif-bold-22", Color.RED);
            addCenteredText("Ти дійшов до рівня " + level, HEIGHT / 2.0 + 30, "SansSerif-16", Color.DARK_GRAY);
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

        // нижню стінку тут не перевіряємо: її обробляє playLevel (м'яч втрачено)

        // перевіряємо, чи м'яч у щось влучив
        GObject collider = getCollidingObject();

        if (collider == paddle) {
            if (vy > 0) {
                playSound(paddleSound);
                bounceFromPaddle();   // кут залежить від місця влучання
            }

            // ставимо м'яч рівно на верхню межу ракетки, щоб він лише торкався її
            ball.setLocation(ball.getX(), paddle.getY() - 2 * BALL_RADIUS);

            // відбиваємо вгору; abs не дає м'ячу "приклеїтися" до ракетки
            vy = -Math.abs(vy);
        } else if (collider != null && collider == obstacle) {
            // виставляємо м'яч з того боку, з якого він прилетів, щоб не застряг
            if (vy > 0) {
                ball.setLocation(ball.getX(), obstacle.getY() - 2 * BALL_RADIUS);
            } else {
                ball.setLocation(ball.getX(), obstacle.getY() + OBSTACLE_HEIGHT);
            }
            vy = -vy;
        } else if (collider != null
                && collider != heart1 && collider != heart2 && collider != heart3
                && collider != coinIcon && collider != scoreLabel) {
            // це цеглина
            if (isStrongBrick(collider)) {
                // перший удар по міцній цеглині: вона світлішає, але лишається
                collider.setColor(getBrickColor(getBrickRow(collider)));
            } else {
                // прибираємо цеглину (інколи з неї випадає бонус) і додаємо 10 очок
                maybeDropBonus(collider);
                remove(collider);
                bricksLeft--;
                score += BRICK_POINTS;
                scoreLabel.setLabel("Очки: " + score);
            }
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
        GObject obj = getObjectAt(x, y);
        if (obj != null) {
            return obj;
        }

        // правий верхній кут
        obj = getObjectAt(x + d, y);
        if (obj != null) {
            return obj;
        }

        // лівий нижній кут
        obj = getObjectAt(x, y + d);
        if (obj != null) {
            return obj;
        }

        // правий нижній кут (якщо тут null, то колізії немає)
        obj = getObjectAt(x + d, y + d);
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