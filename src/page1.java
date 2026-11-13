import acm.graphics.*;
import acm.program.*;
import acm.util.RandomGenerator;
import java.awt.Color;
import java.awt.event.MouseEvent;

/*
 * Клітинки поля пронумеровані так:
 *   1 2 3
 *   4 5 6
 *   7 8 9
 * Значення в клітинках: 0 - порожньо, 1 - хрестик, 2 - нулик
 */
public class game1  extends GraphicsProgram {

    // розміри вікна та поля
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 760;
    private static final int BOARD_X = 75;
    private static final int BOARD_Y = 75;
    private static final int CELL = 150;

    // який зараз екран: 0 - початок, 1 - вибір фігури, 2 - гра, 3 - кінець гри
    private int screen = 0;

    // дев'ять клітинок
    private int c1, c2, c3, c4, c5, c6, c7, c8, c9;

    private int turn;          // чий зараз хід: 1 - хрестики, 2 - нулики
    private int human;         // за кого грає людина: 1 або 2
    private int computer;      // за кого грає комп'ютер
    private int winner;        // 0 - нічия, 1 - хрестики, 2 - нулики
    private boolean gameOver;  // чи закінчилась гра

    // початок і кінець лінії перемоги
    private int winA;
    private int winC;

    private RandomGenerator rgen = RandomGenerator.getInstance();

    public void run() {
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        addMouseListeners();
        showStartScreen();
    }


    private void showStartScreen() {
        screen = 0;
        removeAll();
        addText("Хрестики-нулики", 300, 225, 45);
        addButton("Почати", 200, 375, 200, 70);
    }
}

