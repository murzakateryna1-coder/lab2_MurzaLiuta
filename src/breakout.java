/**
 * Файл: breakout.java
 * Назва: breakout
 * Автори: Мурза Катерина, Люта Ольга
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.sound.sampled.*;

/**
 * Аркадна гра Breakout
 * Містить 3 рівні складності, систему очок, життя, бонуси, рухому перешкоду та звукові ефекти.
 */
public class breakout extends GraphicsProgram {

    /** Ширина вікна програми в пікселях */
    public static final int APPLICATION_WIDTH = 600;
    /** Висота вікна програми в пікселях */
    public static final int APPLICATION_HEIGHT = 700;

    /** Ширина ігрового поля */
    private static final int WIDTH = APPLICATION_WIDTH;
    /** Висота ігрового поля */
    private static final int HEIGHT = APPLICATION_HEIGHT;

    /** Початкова ширина ракетки */
    private static final int PADDLE_WIDTH = 60;
    /** Висота ракетки */
    private static final int PADDLE_HEIGHT = 10;

    /** Відступ ракетки від нижнього краю вікна */
    private static final int PADDLE_Y_OFFSET = 30;

    /** Кількість цеглин в одному ряду */
    private static final int NBRICKS_PER_ROW = 10;

    /** Кількість рядів цеглин */
    private static final int NBRICK_ROWS = 10;

    /** Відстань між сусідніми цеглинами */
    private static final int BRICK_SEP = 4;

    /** Ширина однієї цеглини */
    private static final int BRICK_WIDTH =
            (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

    /** Висота однієї цеглини */
    private static final int BRICK_HEIGHT = 8;

    /** Радіус м'яча в пікселях */
    private static final int BALL_RADIUS = 10;

    /** Відступ верхнього ряду цеглин від верхнього краю вікна */
    private static final int BRICK_Y_OFFSET = 70;

    /** Кількість життів на кожному рівні */
    private static final int NTURNS = 3;

    /** Ширина ракетки на 2-му та 3-му рівнях */
    private static final int PADDLE_WIDTH_LEVEL2 = 40;
    /** Множник швидкості м'яча на 2-му рівні */
    private static final double LEVEL2_SPEED = 1.25;
    /** Множник швидкості м'яча на 3-му рівні */
    private static final double LEVEL3_SPEED = 1.4;

    /** Ширина рухомої перешкоди */
    private static final int OBSTACLE_WIDTH = 100;
    /** Висота рухомої перешкоди */
    private static final int OBSTACLE_HEIGHT = 12;
    /** Координата Y рухомої перешкоди */
    private static final int OBSTACLE_Y = 260;
    /** Швидкість руху перешкоди по горизонталі */
    private static final double OBSTACLE_SPEED = 2;

    /** Коефіцієнт кута відбиття м'яча від краю ракетки */
    private static final double BOUNCE_ANGLE_FACTOR = 0.8;

    /** Імовірність випадіння бонусу зі збитої цеглини */
    private static final double BONUS_CHANCE = 0.2;
    /** Радіус бонусу */
    private static final int BONUS_RADIUS = 10;
    /** Швидкість падіння бонусу */
    private static final double BONUS_SPEED = 2.5;
    /** На скільки пікселів розширюється ракетка від одного бонусу */
    private static final int PADDLE_BONUS_EXTRA = 20;
    /** Максимальна можлива ширина ракетки */
    private static final int MAX_PADDLE_WIDTH = 200;

    /** Затримка між кадрами анімації в мілісекундах */
    private static final int PAUSE_TIME = 10;

    /** Кількість очок за одну знищену цеглину */
    private static final int BRICK_POINTS = 10;
    /** Мінімальна кількість очок для переходу на 2-й рівень */
    private static final int LEVEL_2_MIN_SCORE = 50;
    /** Мінімальна кількість очок для переходу на 3-й рівень */
    private static final int LEVEL_3_MIN_SCORE = 120;

    /** Кількість очок, необхідна для перемоги у грі */
    private static final int WIN_SCORE = 220;

    /** Ширина кнопки перезапуску гри */
    private static final int RESTART_W = 220;
    /** Висота кнопки перезапуску гри */
    private static final int RESTART_H = 50;
    /** Координата X кнопки перезапуску гри */
    private static final int RESTART_X = (WIDTH - RESTART_W) / 2;
    /** Координата Y кнопки перезапуску гри */
    private static final int RESTART_Y = 390;

    /** Ширина кнопки «Почати» */
    private static final int START_W = 160;
    /** Висота кнопки «Почати» */
    private static final int START_H = 45;
    /** Координата X кнопки «Почати» */
    private static final int START_X = (WIDTH - START_W) / 2;
    /** Координата Y кнопки «Почати» */
    private static final int START_Y = 415;

    /** Назва файлу зображення сердечка */
    private static final String HEART_IMAGE = "heart.png";
    /** Розмір іконки сердечка в пікселях */
    private static final int HEART_SIZE = 35;
    /** Відстань між сердечками */
    private static final int HEART_SEP = 6;
    /** Відступ сердечок і монети від верхнього краю */
    private static final int HEART_Y = 20;

    /** Назва файлу зображення монети */
    private static final String COIN_IMAGE = "coin.png";
    /** Розмір іконки монети в пікселях */
    private static final int COIN_SIZE = 35;

    /** Шлях до фонової музики */
    private static final String SOUND_BG_MUSIC = "sounds/bg_music.wav";
    /** Шлях до звуку удару об цеглину */
    private static final String SOUND_BRICK_HIT = "sounds/bricks.wav";
    /** Шлях до звуку відбиття від ракетки */
    private static final String SOUND_PADDLE_HIT = "sounds/paddle.wav";
    /** Шлях до звуку переходу на новий рівень */
    private static final String SOUND_LEVEL_UP = "sounds/level_up.wav";
    /** Шлях до звуку перемоги */
    private static final String SOUND_WIN = "sounds/win.wav";
    /** Шлях до звуку поразки */
    private static final String SOUND_LOSE = "sounds/lose.wav";

    /** Кліп фонової музики */
    private Clip bg_music;
    /** Кліп звуку розбиття цеглини */
    private Clip bricks;
    /** Кліп звуку удару об ракетку */
    private Clip paddleSound;
    /** Кліп звуку нового рівня */
    private Clip levelUpSound;
    /** Кліп звуку перемоги */
    private Clip win;
    /** Кліп звуку програшу */
    private Clip lose;

    /** Графічний об'єкт ракетки гравця */
    private GRect paddle;

    /** Графічний об'єкт м'яча */
    private GOval ball;

    /** Іконка першого життя */
    private GImage heart1;
    /** Іконка другого життя */
    private GImage heart2;
    /** Іконка третього життя */
    private GImage heart3;

    /** Іконка монети біля лічильника очок */
    private GImage coinIcon;
    /** Текстовий напис із поточним рахунком */
    private GLabel scoreLabel;

    /** Поточна кількість очок гравця */
    private int score = 0;

    /** Поточний номер рівня (1, 2 або 3) */
    private int level = 1;
    /** Кількість життів, що залишилися */
    private int lives = NTURNS;
    /** Поточний множник швидкості м'яча */
    private double speedFactor = 1.0;
    /** Поточна ширина ракетки */
    private int paddleWidth = PADDLE_WIDTH;

    /** Падаючий бонус (null, якщо бонусу зараз немає на екрані) */
    private GOval bonus;

    /** Рухома перешкода на 2-му та 3-му рівнях */
    private GRect obstacle;
    /** Горизонтальна швидкість рухомої перешкоди */
    private double obstacleVx;

    /** Швидкість м'яча по осях X та Y */
    private double vx, vy;

    /** Кількість цеглин, що залишилися на полі */
    private int bricksLeft;

    /** Прапорець початку гри після натискання кнопки «Почати» */
    private boolean gameStarted = false;

    /** Прапорець очікування натискання кнопки перезапуску */
    private boolean waitingRestart = false;
    /** Прапорець запиту на перезапуск гри */
    private boolean restartRequested = false;

    /** Генератор випадкових чисел */
    private RandomGenerator rgen = RandomGenerator.getInstance();

    /**
     * Вікно програми та завантажує всі звукові файли.
     */
    public void init() {
        setSize(APPLICATION_WIDTH, APPLICATION_HEIGHT);

        // Завантажуємо аудіофайли перед початком гри
        bg_music = loadSound(SOUND_BG_MUSIC);
        bricks = loadSound(SOUND_BRICK_HIT);
        paddleSound = loadSound(SOUND_PADDLE_HIT);
        levelUpSound = loadSound(SOUND_LEVEL_UP);
        win = loadSound(SOUND_WIN);
        lose = loadSound(SOUND_LOSE);
    }

    /**
     * Головний метод програми: налаштовує розміри вікна, показує меню
     * та запускає нескінченний цикл ігрових сесій.
     */
    public void run() {
        // Коригуємо розмір вікна з урахуванням рамок ОС
        int borderW = APPLICATION_WIDTH - getWidth();
        int borderH = APPLICATION_HEIGHT - getHeight();
        if (borderW > 0 || borderH > 0) {
            setSize(APPLICATION_WIDTH + borderW, APPLICATION_HEIGHT + borderH);
            pause(50);
        }

        // Підключаємо обробку подій миші та показуємо стартовий екран
        addMouseListeners();
        showStartScreen();

        // Повторюємо гру заново після кожного натискання кнопки перезапуску
        while (true) {
            playGameSession();
            resetGame();
        }
    }

    /**
     * Проводить одну повну ігрову сесію через 3 рівні та показує фінальний екран.
     */
    private void playGameSession() {
        startBgMusic();
        setupScore();
        setupHearts();
        setupBricks();
        setupPaddle();

        playLevel();                 // Граємо 1-й рівень
        if (lives > 0) {             // Якщо набрано 50 очок і є життя — переходимо на 2-й
            startLevel2();
            playLevel();             // Граємо 2-й рівень
        }
        if (lives > 0) {             // Якщо набрано 100 очок і є життя — переходимо на 3-й
            startLevel3();
            playLevel();             // Граємо 3-й рівень
        }
        finishGame();
    }

    /**
     * Очищає ігрове поле та скидає всі параметри до початкових значень.
     */
    private void resetGame() {
        removeAll();

        score = 0;
        level = 1;
        lives = NTURNS;
        speedFactor = 1.0;
        paddleWidth = PADDLE_WIDTH;
        bricksLeft = 0;

        paddle = null;
        ball = null;
        bonus = null;
        obstacle = null;
        heart1 = null;
        heart2 = null;
        heart3 = null;

        waitingRestart = false;
        restartRequested = false;
    }

    /**
     * Відображає стартову сторінку з назвою гри, правилами та кнопкою «Почати».
     */
    private void showStartScreen() {
        // Малюємо темний фон
        GRect background = new GRect(WIDTH, HEIGHT);
        background.setColor(new Color(30, 30, 60));
        background.setFilled(true);
        add(background, 0, 0);

        // Заголовок та підзаголовок гри
        addCenteredText("BREAKOUT", 115, "SansSerif-bold-50", Color.WHITE);
        addCenteredText("Збий усі цеглини!", 155, "SansSerif-25", Color.YELLOW);

        // Декоративний ряд із п'яти кольорових цеглин
        int decorWidth = 60;
        int decorSep = 6;
        int decorX = (WIDTH - (5 * decorWidth + 4 * decorSep)) / 2;
        for (int i = 0; i < 5; i++) {
            GRect decor = new GRect(decorX + i * (decorWidth + decorSep), 185, decorWidth, 18);
            decor.setColor(getBrickColor(i * 2));
            decor.setFilled(true);
            add(decor);
        }

        // Текст правил гри та опис рівнів
        addCenteredText("Рухайте мишею ракетку", 240, "SansSerif-20", Color.WHITE);
        addCenteredText("Не дайте м'ячу впасти. У вас " + NTURNS + " життя на кожному рівні", 265, "SansSerif-20", Color.WHITE);
        addCenteredText("1 цеглина = " + BRICK_POINTS + " очок (усього 100 цеглин)", 295, "SansSerif-bold-20", Color.YELLOW);
        addCenteredText("Набери " + LEVEL_2_MIN_SCORE + " очок і перейдеш на 2-й рівень", 325, "SansSerif-20", new Color(100, 255, 180));
        addCenteredText("2-й рівень: швидший м'яч, менша ракетка", 347, "SansSerif-20", Color.LIGHT_GRAY);
        addCenteredText("і рухома перешкода!", 372, "SansSerif-20", Color.LIGHT_GRAY);
        addCenteredText("3-й рівень: цеглини міцні, потрібно 2 удари", 397, "SansSerif-20", Color.LIGHT_GRAY);

        // Малюємо кнопку «Почати»
        GRect button = new GRect(START_W, START_H);
        button.setColor(new Color(0, 200, 120));
        button.setFilled(true);
        add(button, START_X, START_Y);

        GLabel buttonText = new GLabel("Почати");
        buttonText.setFont("SansSerif-bold-25");
        buttonText.setColor(Color.WHITE);
        add(buttonText, START_X + (START_W - buttonText.getWidth()) / 2,
                START_Y + (START_H + buttonText.getAscent()) / 2 - 3);

        // Чекаємо, поки гравець натисне кнопку «Почати»
        while (!gameStarted) {
            pause(50);
        }

        // Очищаємо екран перед стартом першого рівня
        removeAll();
    }

    /**
     * Додає іконку монети та текстовий лічильник очок у лівому верхньому кутку.
     */
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

    /**
     * Відображає три іконки сердечок у правому верхньому кутку екрана.
     */
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

    /**
     * Створює та додає текстовий напис, вирівняний по центру вікна по горизонталі.
     *
     * @param text  текст напису
     * @param y     координата Y базової лінії тексту
     * @param font  назва та розмір шрифту
     * @param color колір тексту
     */
    private void addCenteredText(String text, double y, String font, Color color) {
        GLabel label = new GLabel(text);
        label.setFont(font);
        label.setColor(color);
        add(label, (WIDTH - label.getWidth()) / 2, y);
    }

    /**
     * Обробляє натискання кнопки миші на стартовому та фінальному екранах.
     *
     * @param e об'єкт події кліку миші
     */
    public void mouseClicked(MouseEvent e) {
        // Перевіряємо клік по кнопці «Почати»
        if (!gameStarted
                && e.getX() >= START_X && e.getX() <= START_X + START_W
                && e.getY() >= START_Y && e.getY() <= START_Y + START_H) {
            gameStarted = true;
        }

        // Перевіряємо клік по кнопці перезапуску в кінці гри
        if (waitingRestart
                && e.getX() >= RESTART_X && e.getX() <= RESTART_X + RESTART_W
                && e.getY() >= RESTART_Y && e.getY() <= RESTART_Y + RESTART_H) {
            restartRequested = true;
        }
    }

    /**
     * Повертає колір цеглини залежно від номера її ряду.
     *
     * @param row номер ряду цеглин (від 0 до 9)
     * @return колір для заданого ряду
     */
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

    /**
     * Створює сітку цеглин у верхній частині поля.
     * На 3-му рівні цеглини робляться темнішими (міцними, на 2 удари).
     */
    private void setupBricks() {
        // Обчислюємо загальну ширину ряду та початкову координату X для центрування
        int rowWidth = NBRICKS_PER_ROW * BRICK_WIDTH + (NBRICKS_PER_ROW - 1) * BRICK_SEP;
        int startX = (WIDTH - rowWidth) / 2;

        for (int row = 0; row < NBRICK_ROWS; row++) {
            for (int col = 0; col < NBRICKS_PER_ROW; col++) {
                int x = startX + col * (BRICK_WIDTH + BRICK_SEP);
                int y = BRICK_Y_OFFSET + row * (BRICK_HEIGHT + BRICK_SEP);

                GRect brick = new GRect(x, y, BRICK_WIDTH, BRICK_HEIGHT);
                brick.setFilled(true);

                // На 3-му рівні робимо колір темнішим для позначення міцної цеглини
                if (level == 3) {
                    brick.setColor(getBrickColor(row).darker());
                } else {
                    brick.setColor(getBrickColor(row));
                }

                add(brick);
                bricksLeft++;
            }
        }
    }

    /**
     * Створює ракетку внизу ігрового поля по центру.
     */
    private void setupPaddle() {
        int x = (WIDTH - paddleWidth) / 2;
        int y = HEIGHT - PADDLE_Y_OFFSET - PADDLE_HEIGHT;

        paddle = new GRect(x, y, paddleWidth, PADDLE_HEIGHT);
        paddle.setFilled(true);
        add(paddle);
    }

    /**
     * Переміщує ракетку по горизонталі слідом за рухом курсора миші.
     *
     * @param e об'єкт події руху миші
     */
    public void mouseMoved(MouseEvent e) {
        if (paddle == null) {
            return;
        }

        // Центруємо ракетку відносно курсора та обмежуємо межами вікна
        int x = e.getX() - paddleWidth / 2;
        if (x < 0) {
            x = 0;
        }
        if (x > WIDTH - paddleWidth) {
            x = WIDTH - paddleWidth;
        }

        paddle.setLocation(x, paddle.getY());
    }

    /**
     * Створює м'яч по центру вікна та задає йому випадковий напрямок руху вниз.
     */
    private void setupBall() {
        int x = WIDTH / 2 - BALL_RADIUS;
        int y = HEIGHT / 2 - BALL_RADIUS;

        ball = new GOval(x, y, 2 * BALL_RADIUS, 2 * BALL_RADIUS);
        ball.setFilled(true);
        add(ball);

        // Вертикальна швидкість залежить від множника поточного рівня
        vy = 3.0 * speedFactor;

        // Горизонтальна швидкість вибирається випадково вліво або вправо
        vx = rgen.nextDouble(1.0, 3.0) * speedFactor;
        if (rgen.nextBoolean(0.5)) {
            vx = -vx;
        }
    }

    /**
     * Перевіряє, чи набрав гравець достатньо очок для завершення поточного рівня.
     *
     * @return true, якщо умову завершення рівня виконано, інакше false
     */
    private boolean levelDone() {
        if (level == 1) {
            return score >= LEVEL_2_MIN_SCORE;
        }
        if (level == 2) {
            return score >= LEVEL_3_MIN_SCORE;
        }
        return score >= WIN_SCORE;
    }

    /**
     * Виконує ігровий цикл одного рівня, поки у гравця є життя і не набрано потрібні очки.
     */
    private void playLevel() {
        while (lives > 0 && !levelDone()) {
            setupBall();
            pause(1000); // Пауза 1 секунда перед запуском м'яча

            // Рухаємо м'яч, перешкоду та бонус, поки м'яч не впаде за нижній край
            while (!levelDone() && ball.getY() + 2 * BALL_RADIUS < HEIGHT) {
                moveBall();
                moveObstacle();
                moveBonus();
                pause(PAUSE_TIME);
            }

            removeBonus();
            remove(ball);

            // Якщо рівень ще не пройдено, гравець втрачає одне життя
            if (!levelDone()) {
                lives--;

                // Прибираємо відповідне сердечко з екрана
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

    /**
     * Налаштовує 2-й рівень: прискорює м'яч, зменшує ракетку та додає рухому перешкоду.
     */
    private void startLevel2() {
        level = 2;
        removeBonus();
        speedFactor = LEVEL2_SPEED;
        paddleWidth = PADDLE_WIDTH_LEVEL2;

        resetLives();
        clearBricks();
        remove(paddle);
        setupPaddle();
        setupBricks();
        setupObstacle();

        showLevelBanner("РІВЕНЬ 2", "Швидший м'яч, менша ракетка, рухома перешкода");
    }

    /**
     * Налаштовує 3-й рівень: збільшує швидкість м'яча та створює міцні цеглини на 2 удари.
     */
    private void startLevel3() {
        level = 3;
        speedFactor = LEVEL3_SPEED;
        removeBonus();
        paddleWidth = PADDLE_WIDTH_LEVEL2;

        resetLives();
        clearBricks();
        remove(paddle);
        setupPaddle();
        setupBricks();

        showLevelBanner("РІВЕНЬ 3", "Цеглини міцні: потрібно 2 удари!");
    }

    /**
     * Обчислює новий вектор швидкості м'яча при відбитті від ракетки.
     * Чим ближче до краю ракетки влучив м'яч, тим гостріший кут польоту.
     */
    private void bounceFromPaddle() {
        double speed = Math.sqrt(vx * vx + vy * vy);

        // Обчислюємо зміщення від центру ракетки в межах від -1 до 1
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

        // Задаємо мінімальну горизонтальну швидкість, щоб м'яч не літав строго вертикально
        double minVx = 0.5 * speedFactor;
        if (Math.abs(newVx) < minVx) {
            if (offset >= 0) {
                newVx = minVx;
            } else {
                newVx = -minVx;
            }
        }

        vx = newVx;
        // Зберігаємо загальну швидкість м'яча незмінною
        vy = -Math.sqrt(speed * speed - vx * vx);
    }

    /**
     * Із заданою ймовірністю створює падаючий бонус на місці розбитої цеглини.
     *
     * @param brick знищена цеглина
     */
    private void maybeDropBonus(GObject brick) {
        if (bonus == null && rgen.nextBoolean(BONUS_CHANCE)) {
            double x = brick.getX() + BRICK_WIDTH / 2.0 - BONUS_RADIUS;
            bonus = new GOval(x, brick.getY(), 2 * BONUS_RADIUS, 2 * BONUS_RADIUS);
            bonus.setFilled(true);
            bonus.setColor(new Color(255, 193, 7));
            add(bonus);
        }
    }

    /**
     * Переміщує бонус вниз та перевіряє, чи спіймала його ракетка.
     */
    private void moveBonus() {
        if (bonus == null) {
            return;
        }

        bonus.move(0, BONUS_SPEED);

        // Перевіряємо перетин бонусу з ракеткою
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

    /**
     * Збільшує ширину ракетки після підбору бонусу (не більше MAX_PADDLE_WIDTH).
     */
    private void growPaddle() {
        int newWidth = paddleWidth + PADDLE_BONUS_EXTRA;
        if (newWidth > MAX_PADDLE_WIDTH) {
            newWidth = MAX_PADDLE_WIDTH;
        }
        int delta = newWidth - paddleWidth;
        paddleWidth = newWidth;
        paddle.setSize(paddleWidth, PADDLE_HEIGHT);

        // Розширюємо ракетку симетрично в обидва боки, не виходячи за межі вікна
        double x = paddle.getX() - delta / 2.0;
        if (x < 0) {
            x = 0;
        }
        if (x > WIDTH - paddleWidth) {
            x = WIDTH - paddleWidth;
        }
        paddle.setLocation(x, paddle.getY());
    }

    /**
     * Видаляє бонус з екрана, якщо він існує.
     */
    private void removeBonus() {
        if (bonus != null) {
            remove(bonus);
            bonus = null;
        }
    }

    /**
     * Визначає номер ряду цеглини за її координатою Y.
     *
     * @param brick об'єкт цеглини
     * @return номер ряду (від 0 до 9)
     */
    private int getBrickRow(GObject brick) {
        return (int) Math.round((brick.getY() - BRICK_Y_OFFSET) / (BRICK_HEIGHT + BRICK_SEP));
    }

    /**
     * Перевіряє, чи є цеглина міцною (на 3-му рівні ще не отримала перший удар).
     *
     * @param brick об'єкт цеглини
     * @return true, якщо цеглина потребує ще одного удару, інакше false
     */
    private boolean isStrongBrick(GObject brick) {
        if (level != 3) {
            return false;
        }
        return !brick.getColor().equals(getBrickColor(getBrickRow(brick)));
    }

    /**
     * Відновлює всі 3 життя та заново малює сердечка у верхньому кутку.
     */
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

    /**
     * Показує по центру екрана заставку з назвою та описом нового рівня на 2.5 секунди.
     *
     * @param titleText заголовок рівня
     * @param hintText  короткий опис особливостей рівня
     */
    private void showLevelBanner(String titleText, String hintText) {
        playSound(levelUpSound);

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

    /**
     * Повертає графічний об'єкт у вказаній точці, ігноруючи м'яч та бонус.
     *
     * @param x координата X точки перевірки
     * @param y координата Y точки перевірки
     * @return знайдений об'єкт або null
     */
    private GObject getObjectAt(double x, double y) {
        GObject obj = getElementAt(x, y);
        if (obj == ball || obj == bonus) {
            return null;
        }
        return obj;
    }

    /**
     * Видаляє з ігрового поля всі цеглини, що залишилися від попереднього рівня.
     */
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

    /**
     * Створює сіру рухому перешкоду посередині ігрового поля.
     */
    private void setupObstacle() {
        obstacle = new GRect((WIDTH - OBSTACLE_WIDTH) / 2, OBSTACLE_Y, OBSTACLE_WIDTH, OBSTACLE_HEIGHT);
        obstacle.setFilled(true);
        obstacle.setColor(Color.DARK_GRAY);
        add(obstacle);
        obstacleVx = OBSTACLE_SPEED;
    }

    /**
     * Переміщує перешкоду вліво-вправо та відбиває її від бічних стінок вікна.
     */
    private void moveObstacle() {
        if (obstacle == null) {
            return;
        }

        obstacle.move(obstacleVx, 0);

        if (obstacle.getX() <= 0 || obstacle.getX() + OBSTACLE_WIDTH >= WIDTH) {
            obstacleVx = -obstacleVx;
        }
    }

    /**
     * Показує фінальний екран перемоги або поразки з підсумковим рахунком та кнопкою перезапуску.
     */
    private void finishGame() {
        stopBgMusic();

        boolean won = lives > 0;

        // Накладаємо напівпрозорий фон поверх ігрового поля
        GRect overlay = new GRect(WIDTH, HEIGHT);
        overlay.setFilled(true);
        if (won) {
            overlay.setColor(new Color(0, 70, 30, 200));
        } else {
            overlay.setColor(new Color(0, 0, 0, 200));
        }
        add(overlay, 0, 0);

        String buttonCaption;
        if (won) {
            playSound(win);
            addCenteredText("ВІТАЄМО З ПЕРЕМОГОЮ!", 280, "SansSerif-bold-36", new Color(120, 255, 160));
            addCenteredText("Ви набрали " + score + " очків", 330, "SansSerif-bold-24", Color.WHITE);
            buttonCaption = "Грати з початку";
        } else {
            playSound(lose);
            addCenteredText("ВИ ПРОГРАЛИ!:(", 280, "SansSerif-bold-48", new Color(255, 80, 80));
            addCenteredText("Зароблено очків: " + score, 330, "SansSerif-bold-24", Color.WHITE);
            buttonCaption = "Почати спочатку";
        }

        // Малюємо кнопку для нової гри
        GRect button = new GRect(RESTART_W, RESTART_H);
        button.setFilled(true);
        button.setColor(new Color(0, 200, 120));
        add(button, RESTART_X, RESTART_Y);

        GLabel buttonText = new GLabel(buttonCaption);
        buttonText.setFont("SansSerif-bold-22");
        buttonText.setColor(Color.WHITE);
        add(buttonText, RESTART_X + (RESTART_W - buttonText.getWidth()) / 2,
                RESTART_Y + (RESTART_H + buttonText.getAscent()) / 2 - 3);

        // Чекаємо натискання кнопки перезапуску
        waitingRestart = true;
        while (!restartRequested) {
            pause(50);
        }
    }

    /**
     * Виконує один крок руху м'яча та обробляє зіткнення зі стінками, ракеткою,
     * рухомою перешкодою й цеглинами.
     */
    private void moveBall() {
        ball.move(vx, vy);

        // Відбиття від правої стінки
        if (ball.getX() + 2 * BALL_RADIUS >= WIDTH) {
            ball.setLocation(WIDTH - 2 * BALL_RADIUS, ball.getY());
            vx = -Math.abs(vx);
        }

        // Відбиття від лівої стінки
        if (ball.getX() <= 0) {
            ball.setLocation(0, ball.getY());
            vx = Math.abs(vx);
        }

        // Відбиття від верхньої стінки
        if (ball.getY() <= 0) {
            ball.setLocation(ball.getX(), 0);
            vy = Math.abs(vy);
        }

        // Перевіряємо зіткнення м'яча з об'єктами на полі
        GObject collider = getCollidingObject();

        if (collider == paddle) {
            if (vy > 0) {
                playSound(paddleSound);
                bounceFromPaddle();
            }

            // Вирівнюємо м'яч над ракеткою, щоб він не застрягав усередині неї
            ball.setLocation(ball.getX(), paddle.getY() - 2 * BALL_RADIUS);
            vy = -Math.abs(vy);
        } else if (collider != null && collider == obstacle) {
            // Виштовхуємо м'яч за межі перешкоди та змінюємо вертикальний напрямок
            if (vy > 0) {
                ball.setLocation(ball.getX(), obstacle.getY() - 2 * BALL_RADIUS);
            } else {
                ball.setLocation(ball.getX(), obstacle.getY() + OBSTACLE_HEIGHT);
            }
            vy = -vy;
        } else if (collider != null
                && collider != heart1 && collider != heart2 && collider != heart3
                && collider != coinIcon && collider != scoreLabel) {
            // Зіткнення з цеглиною (ігноруємо елементи інтерфейсу)
            if (isStrongBrick(collider)) {
                // Перший удар по міцній цеглині робить її світлішою
                collider.setColor(getBrickColor(getBrickRow(collider)));
            } else {
                // Знищуємо цеглину, додаємо очки та перевіряємо випадіння бонусу
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
     * Перевіряє 4 кути описаного навколо м'яча квадрата на наявність зіткнень.
     *
     * @return об'єкт, у який влучив м'яч, або null, якщо зіткнення немає
     */
    private GObject getCollidingObject() {
        double x = ball.getX();
        double y = ball.getY();
        double d = 2 * BALL_RADIUS;

        // Лівий верхній кут
        GObject obj = getObjectAt(x, y);
        if (obj != null) {
            return obj;
        }

        // Правий верхній кут
        obj = getObjectAt(x + d, y);
        if (obj != null) {
            return obj;
        }

        // Лівий нижній кут
        obj = getObjectAt(x, y + d);
        if (obj != null) {
            return obj;
        }

        // Правий нижній кут
        obj = getObjectAt(x + d, y + d);
        return obj;
    }

    // =====================================
    // РОБОТА ЗІ ЗВУКАМИ (.WAV)
    // =====================================

    /**
     * Завантажує аудіофайл формату .wav у пам'ять.
     *
     * @param filename шлях до звукового файлу
     * @return завантажений аудіокліп Clip або null у разі помилки
     */
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

    /**
     * Відтворює звуковий ефект з самого початку.
     *
     * @param sound аудіокліп для відтворення
     */
    private void playSound(Clip sound) {
        if (sound != null) {
            if (sound.isRunning()) {
                sound.stop();
            }
            sound.setFramePosition(0);
            sound.start();
        }
    }

    /**
     * Запускає безперервне відтворення фонової музики по колу.
     */
    private void startBgMusic() {
        if (bg_music != null) {
            bg_music.setFramePosition(0);
            bg_music.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    /**
     * Зупиняє відтворення фонової музики.
     */
    private void stopBgMusic() {
        if (bg_music != null && bg_music.isRunning()) {
            bg_music.stop();
        }
    }
}
