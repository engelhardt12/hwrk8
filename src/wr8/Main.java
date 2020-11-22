package wr8;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.Random;

public class Main extends JFrame implements ChangeListener, ActionListener {
    private JSlider slider;
    private JButton oButton, xButton;
    private Board board;
    private int lineThickness=4;
    private Color oColor=Color.BLUE, xColor=Color.RED;
    static final char BLANK=' ', O='O', X='X';
    private char position[]={
            BLANK, BLANK, BLANK,
            BLANK, BLANK, BLANK,
            BLANK, BLANK, BLANK};
    private int wins=0, losses=0, draws=0; // счет игрока

    // Начало игры
    public static void main(String args[]) {
        new Main();
    }


    public Main() {
        super("Крестики Нолики");
        JPanel topPanel=new JPanel();
        topPanel.setLayout(new FlowLayout());
        topPanel.add(new JLabel("Толщина линии:"));
        topPanel.add(slider=new JSlider(SwingConstants.HORIZONTAL, 1, 20, 4));
        slider.setMajorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.addChangeListener(this);
        topPanel.add(oButton=new JButton("O Цвет"));
        topPanel.add(xButton=new JButton("X Цвет"));
        oButton.addActionListener(this);
        xButton.addActionListener(this);
        add(topPanel, BorderLayout.NORTH);
        add(board=new Board(), BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setVisible(true);
    }

    // изменение толщины линий
    public void stateChanged(ChangeEvent e) {
        lineThickness = slider.getValue();
        board.repaint();
    }

    // изменение цввета символов крестика и нолика
    public void actionPerformed(ActionEvent e) {
        if (e.getSource()==oButton) {
            Color newColor = JColorChooser.showDialog(this, "Выбор цвета для O", oColor);
            if (newColor!=null)
                oColor=newColor;
        }
        else if (e.getSource()==xButton) {
            Color newColor = JColorChooser.showDialog(this, "Выбор цвета для X", xColor);
            if (newColor!=null)
                xColor=newColor;
        }
        board.repaint();
    }


    private class Board extends JPanel implements MouseListener {
        private Random random=new Random();
        private int rows[][]={{0,2},{3,5},{6,8},{0,6},{1,7},{2,8},{0,8},{2,6}};


        public Board() {
            addMouseListener(this);
        }


        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            int w=getWidth();
            int h=getHeight();
            Graphics2D g2d = (Graphics2D) g;

            g2d.setPaint(Color.WHITE);
            g2d.fill(new Rectangle2D.Double(0, 0, w, h));
            g2d.setPaint(Color.BLACK);
            g2d.setStroke(new BasicStroke(lineThickness));
            g2d.draw(new Line2D.Double(0, h/3, w, h/3));
            g2d.draw(new Line2D.Double(0, h*2/3, w, h*2/3));
            g2d.draw(new Line2D.Double(w/3, 0, w/3, h));
            g2d.draw(new Line2D.Double(w*2/3, 0, w*2/3, h));

// прорисовка х и о
            for (int i=0; i<9; ++i) {
                double xpos=(i%3+0.5)*w/3.0;
                double ypos=(i/3+0.5)*h/3.0;
                double xr=w/8.0;
                double yr=h/8.0;
                if (position[i]==O) {
                    g2d.setPaint(oColor);
                    g2d.draw(new Ellipse2D.Double(xpos-xr, ypos-yr, xr*2, yr*2));
                }
                else if (position[i]==X) {
                    g2d.setPaint(xColor);
                    g2d.draw(new Line2D.Double(xpos-xr, ypos-yr, xpos+xr, ypos+yr));
                    g2d.draw(new Line2D.Double(xpos-xr, ypos+yr, xpos+xr, ypos-yr));
                }
            }
        }

        // Рисование нолика в выбраном мышкой квадрате
        public void mouseClicked(MouseEvent e) {
            int xpos=e.getX()*3/getWidth();
            int ypos=e.getY()*3/getHeight();
            int pos=xpos+3*ypos;
            if (pos>=0 && pos<9 && position[pos]==BLANK) {
                position[pos]=O;
                repaint();
                putX();
                repaint();
            }
        }

        // игонрирование лишних лействий мыши
        public void mousePressed(MouseEvent e) {}
        public void mouseReleased(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}

        // ИИ использует крестик
        void putX() {

// Проверка на окончание игры
            if (won(O))
                newGame(O);
            else if (isDraw())
                newGame(BLANK);


            else {
                nextMove();
                if (won(X))
                    newGame(X);
                else if (isDraw())
                    newGame(BLANK);
            }
        }

        // проверка на победу человека
        boolean won(char player) {
            for (int i=0; i<8; ++i)
                if (testRow(player, rows[i][0], rows[i][1]))
                    return true;
            return false;
        }


        boolean

        testRow(char player, int a, int b) {
            return position[a]==player && position[b]==player
                    && position[(a+b)/2]==player;
        }


        void nextMove() {
            int r=findRow(X); // заполнение ячеек иксами для победы
            if (r<0)
                r=findRow(O); // попытка блокировки противника
            if (r<0) { // рандомный ход
                do
                    r=random.nextInt(9);
                while (position[r]!=BLANK);
            }
            position[r]=X;
        }


        int findRow(char player) {
            for (int i=0; i<8; ++i) {
                int result=find1Row(player, rows[i][0], rows[i][1]);
                if (result>=0)
                    return result;
            }
            return -1;
        }


        int find1Row(char player, int a, int b) {
            int c=(a+b)/2; // Центральная ячейка
            if (position[a]==player && position[b]==player && position[c]==BLANK)
                return c;
            if (position[a]==player && position[c]==player && position[b]==BLANK)
                return b;
            if (position[b]==player && position[c]==player && position[a]==BLANK)
                return a;
            return -1;
        }

        // Проверка на заполненость ячеек
        boolean isDraw() {
            for (int i=0; i<9; ++i)
                if (position[i]==BLANK)
                    return false;
            return true;
        }

        // Начало игры
        void newGame(char winner) {
            repaint();

// оглашение результатов игры
            String result;
            if (winner==O) {
                ++wins;
                result = "Вы выиграли!";
            }
            else if (winner==X) {
                ++losses;
                result = "Я выигрпал!";
            }
            else {
                result = "Ничья";
                ++draws;
            }
            if (JOptionPane.showConfirmDialog(null, "У вас "+wins+ " побед, "+losses+" поражентий, "+draws+" ничей\n" +"Продолжить игру?", result, JOptionPane.YES_NO_OPTION) !=JOptionPane.YES_OPTION) {
                System.exit(0);
            }

// очистка стола
            for (int j=0; j<9; ++j)
                position[j]=BLANK;

// ИИ всегда ходит первым
            if ((wins+losses+draws)%2 == 1)
                nextMove();
        }
    }
}



