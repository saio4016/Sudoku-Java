package com.sudoku;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * 数独プログラム 解の検証、解法アルゴリズムの適応、問題の設定などが可能 r2の変更点
 * 問題を入れ替える際に、前の問題のセルが残ってしまう、色が灰色から変更されないなどを修正
 */
public class Sudoku extends JFrame implements ActionListener {

    /**
     * UID
     */
    private static final long serialVersionUID = 3883151525928534467L;

    // UI要素をもつ配列
    private SudokuCell[][] sudokuCells;

    // 数独に入力される数値を保持する配列
    private int[][] cellValues;

    // 難易度の設定
    private int difficultyNum;
    private String difficultyString;

    // 空白セルへアクセスした回数を計測する
    private int accessNum = 0;

    // 適応するアルゴリズムの番号を格納する
    private int algorithmNum = 0;

    private final int[][] easyVal
            = {{0, 8, 0, 7, 0, 1, 0, 5, 0}, // 1
            {0, 0, 2, 0, 4, 0, 9, 0, 0}, // 2
            {9, 3, 0, 0, 0, 0, 0, 7, 4}, // 3
            {8, 0, 0, 1, 0, 4, 0, 0, 6}, // 4
            {0, 0, 6, 0, 0, 0, 1, 0, 0}, // 5
            {7, 0, 0, 9, 0, 3, 0, 0, 8}, // 6
            {2, 4, 0, 0, 0, 0, 0, 1, 5}, // 7
            {0, 0, 7, 0, 5, 0, 3, 0, 0}, // 8
            {0, 5, 0, 8, 0, 7, 0, 4, 0},}; // 9

    private final int[][] normalVal
            = {{0, 0, 0, 2, 0, 9, 0, 0, 0}, // 1
            {0, 0, 5, 8, 0, 6, 3, 0, 0}, // 2
            {0, 6, 0, 0, 0, 0, 0, 4, 0}, // 3
            {7, 4, 0, 0, 0, 0, 0, 8, 2}, // 4
            {0, 0, 0, 0, 6, 0, 0, 0, 0}, // 5
            {3, 8, 0, 0, 0, 0, 0, 9, 6}, // 6
            {0, 9, 0, 0, 0, 0, 0, 3, 0}, // 7
            {0, 0, 2, 7, 0, 4, 8, 0, 0}, // 8
            {0, 0, 0, 5, 0, 2, 0, 0, 0},}; // 9

    private final int[][] hardlVal
            = {{0, 0, 9, 0, 0, 0, 0, 0, 0}, // 1
            {0, 8, 0, 6, 0, 5, 0, 2, 0}, // 2
            {5, 0, 1, 0, 7, 8, 0, 0, 0}, // 3
            {0, 0, 0, 0, 0, 0, 7, 0, 0}, // 4
            {7, 0, 6, 0, 4, 0, 1, 0, 2}, // 5
            {0, 0, 4, 0, 0, 0, 0, 0, 0}, // 6
            {0, 0, 0, 7, 2, 0, 9, 0, 3}, // 7
            {0, 9, 0, 3, 0, 1, 0, 8, 0}, // 8
            {0, 0, 0, 0, 0, 0, 6, 0, 0},}; // 9

    /**
     * コンポーネントの設定と数独の初期化
     */
    private Sudoku() {
        super("Sudoku");

        prepareSudokuUI();

        // myAlgorithmで使用する配列を作成
        secureCandidates();

        // JFrame property
        setLayout(new FlowLayout(FlowLayout.CENTER));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((d.width / 2 - 175), (d.height / 2 - 275));
        setResizable(false);
        setVisible(true);
    }

    /**
     * UIのセットアップ.
     */
    private void prepareSudokuUI() {
        // タイトルと数独グリッド線、ボタンパネルを垂直
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // タイトルパネルの生成
        JPanel title = new JPanel();
        title.add(new JLabel(new ImageIcon(getClass().getResource("/Resources/title.png"))));

        // 数独のグリッドパネルを生成
        JPanel sudokuPanel = new JPanel();
        sudokuPanel.setLayout(new GridLayout(3, 3, 1, 1));

        // 3x3のボックスパネルを生成
        JPanel[] boxes = new JPanel[9];
        boxes = prepare3x3BoxUI(sudokuPanel, boxes);

        // 数値を入力する配列を生成
        cellValues = new int[9][9];
        // 数独のGUI部分を管理するセルの生成
        sudokuCells = new SudokuCell[9][9];
        prepareSudokuCellsUI(boxes);

        // １列目のボタン
        JPanel buttonsPanel = new JPanel();
        JButton submitButton = new JButton("Submit");
        JButton solveButton = new JButton("Solve");
        JButton eraseButton = new JButton("Erase");
        JButton eraseAllButton = new JButton("Erase All");

        buttonsPanel.add(submitButton);
        buttonsPanel.add(solveButton);
        buttonsPanel.add(eraseButton);
        buttonsPanel.add(eraseAllButton);

        // ２列目のボタン
        JPanel buttonsPanel2 = new JPanel();
        JButton presetEasyButton = new JButton("Easy");
        JButton presetNormalButton = new JButton("Normal");
        JButton presetHardButton = new JButton("Hard");
        JButton presetLoadButton = new JButton("Load");

        buttonsPanel2.add(presetEasyButton);
        buttonsPanel2.add(presetNormalButton);
        buttonsPanel2.add(presetHardButton);
        buttonsPanel2.add(presetLoadButton);

        // ３列目のボタン
        JPanel buttonsPanel3 = new JPanel();
        JButton backTrackButton = new JButton("BackTrack");
        JButton advancedButton = new JButton("Advanced");
        JButton myAlgorithmButton = new JButton("MyAlgorithm");

        buttonsPanel3.add(backTrackButton);
        buttonsPanel3.add(advancedButton);
        buttonsPanel3.add(myAlgorithmButton);

        submitButton.addActionListener(this);
        solveButton.addActionListener(this);
        presetEasyButton.addActionListener(this);
        presetNormalButton.addActionListener(this);
        presetHardButton.addActionListener(this);
        presetLoadButton.addActionListener(this);
        eraseButton.addActionListener(this);
        eraseAllButton.addActionListener(this);
        backTrackButton.addActionListener(this);
        advancedButton.addActionListener(this);
        myAlgorithmButton.addActionListener(this);

        panel.add(title);
        panel.add(sudokuPanel);
        panel.add(buttonsPanel);
        panel.add(buttonsPanel2);
        panel.add(buttonsPanel3);
        add(panel);
    }

    /**
     * 3x3 9マスの数独パネルを生成
     */
    private JPanel[] prepare3x3BoxUI(JPanel sudokuPanel, JPanel[] boxes) {
        for (int i = 0; i < 9; i++) {
            boxes[i] = new JPanel();
            boxes[i].setLayout(new GridLayout(3, 3, 0, 0));
            boxes[i].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
            sudokuPanel.add(boxes[i]);
        }

        return boxes;
    }

    /**
     * UIを含む配列(SudokuCell)を生成し、パネルへ追加する
     */
    private void prepareSudokuCellsUI(JPanel[] boxes) {
        int index = 0;

        // Adjust current row
        for (int i = 0; i < 9; i++) {
            if (i <= 2) {
                index = 0;
            } else if (i <= 5) {
                index = 3;
            } else {
                index = 6;
            }

            for (int j = 0; j < 9; j++) {
                sudokuCells[i][j] = new SudokuCell(i, j);
                boxes[index + (j / 3)].add(sudokuCells[i][j]);
            }
        }
    }

    /**
     * クリックしたボタンに応じてメソッドを実行
     *
     * @param event
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        JButton button = (JButton) event.getSource();
        String buttonType = button.getText();

        switch (buttonType) {
            case "Submit":
                submitSudoku();
                break;
            case "Solve":
                startSolving();
                break;
            case "Erase":
                erase();
                break;
            case "Erase All":
                eraseAllIncludingPresetCells();
                break;
            case "Easy":
                if (checkPresetCells()) {
                    difficultyNum = 0;
                    difficultyString = "易しい";
                    markAsPresetCells(difficultyNum);
                }
                break;
            case "Normal":
                if (checkPresetCells()) {
                    difficultyNum = 1;
                    difficultyString = "普通";
                    markAsPresetCells(difficultyNum);
                }
                break;
            case "Hard":
                if (checkPresetCells()) {
                    difficultyNum = 2;
                    difficultyString = "難しい";
                    markAsPresetCells(difficultyNum);
                }
                break;
            case "Load":
                if (checkPresetCells()) {
                    difficultyString = "オリジナル";
                    loadFile();
                }
                break;
            case "BackTrack":
                algorithmNum = 0;
                System.out.println("現在のアルゴリズム：BackTrack");
                break;
            case "Advanced":
                algorithmNum = 1;
                System.out.println("現在のアルゴリズム：Advanced");
                break;
            case "MyAlgorithm":
                algorithmNum = 2;
                System.out.println("現在のアルゴリズム：自作");
                break;
        }
    }

    /**
     * 現在のセルへの入力データが数独のルールに適しているか否かを表示
     */
    private void submitSudoku() {
        if (isSudokuSolved()) {
            JOptionPane.showMessageDialog(getRootPane(),
                    "<html><center>Congratulations!<br>Sudoku has been Completed!</center></html>",
                    "Sudoku Validation", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(getRootPane(),
                    "<html><center>Failed!<br>Sudoku is not complete!</center></html>",
                    "Sudoku Validation", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 数独解法アルゴリズムが開始可能であるかの確認
     */
    private void startSolving() {
        // 既に全てのセルへ数字が入力されている場合はエラー文を表示する
        if (isSudokuFull()) {
            JOptionPane.showMessageDialog(getRootPane(),
                    "<html><center>There are no cells open to start from.</center></html>",
                    "Solving Sudoku", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 解法開始前の確認
        if (isValidToStart()) {
            //時間の計測
            long start = System.currentTimeMillis();
            if (algorithmNum == 0) {
                if (!bruteForceSolve(0, 0)) {
                    JOptionPane.showMessageDialog(getRootPane(),
                            "<html><center>Unable to solve.</center></html>",
                            "Solving Sudoku", JOptionPane.ERROR_MESSAGE);
                }
            } else if (algorithmNum == 1) {
                if (!advanced()) {
                    JOptionPane.showMessageDialog(getRootPane(),
                            "<html><center>Unable to solve.</center></html>",
                            "Solving Sudoku", JOptionPane.ERROR_MESSAGE);
                }
            } else if (algorithmNum == 2) {
                if (!myAlgorithm()) {
                    JOptionPane.showMessageDialog(getRootPane(),
                            "<html><center>Unable to solve.</center></html>",
                            "Solving Sudoku", JOptionPane.ERROR_MESSAGE);
                }
            }

            System.out.println("難易度：" + difficultyString);
            long end = System.currentTimeMillis();
            System.out.print("アルゴリズム処理時間：");
            System.out.println((end - start) + "ms");
            System.out.println("空白セルアクセス回数：" + accessNum);
            accessNum = 0;
        } else {
            JOptionPane.showMessageDialog(getRootPane(),
                    "<html><center>This is not a valid Sudoku to start.</center></html>",
                    "Solving Sudoku", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 編集可能なセルデータを全て削除
     */
    private void erase() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (cellValues[i][j] != 0 && sudokuCells[i][j].editable) {
                    setCellValues(i, j, 0);
                }
            }
        }
    }

    /**
     * 問題部分を含む全セルデータの削除
     */
    private void eraseAllIncludingPresetCells() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (cellValues[i][j] != 0) {
                    if (!sudokuCells[i][j].editable) {
                        sudokuCells[i][j].editable = true;
                        sudokuCells[i][j].setEditable(true);
                        sudokuCells[i][j].setForeground(Color.BLACK);
                    }
                    setCellValues(i, j, 0);
                }
            }
        }
    }

    /**
     * 最初に入力した問題が数独として正しいか判定
     */
    private boolean checkPresetCells() {
        if (!isValidToStart()) {
            JOptionPane.showMessageDialog(getRootPane(),
                    "<html><center>This is not a valid Sudoku to start.</center></html>",
                    "Sudoku Solver", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * セルへ入力した値が数独として正しいかどうか判定
     */
    private boolean isSudokuSolved() {
        for (int i = 0; i < 9; i++) {
            int[] checkRow = new int[9];
            int[] checkCol = new int[9];

            for (int j = 0; j < 9; j++) {
                //空の値がセルに含まれていないか確認
                if (cellValues[i][j] == 0) {
                    return false;
                }

                checkRow[j] = cellValues[i][j];
                checkCol[j] = cellValues[j][i];

                if (isContainedIn3x3Box(i, j, cellValues[i][j])) {
                    return false;
                }
            }

            if (!isRowColumnCorrect(checkRow, checkCol)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 行と列に1〜9までの数字が正しく含まれているかの確認
     */
    private boolean isRowColumnCorrect(int[] aRow, int[] aCol) {
        Arrays.sort(aRow);
        Arrays.sort(aCol);

        for (int i = 0; i < 9; i++) {
            if (aRow[i] != i + 1 && aCol[i] != i + 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * 数独が開始できる状況であるかの確認
     */
    private boolean isValidToStart() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (cellValues[i][j] != 0) {
                    if (isContainedIn3x3Box(i, j, cellValues[i][j])
                            || isContainedInRowColumn(i, j, cellValues[i][j])) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 現在セルに入力している値を数独問題として固定する
     */
    private void markAsPresetCells(int level) {

        int[][] ValList = easyVal;
        if (level == 0) {
            ValList = easyVal;
        } else if (level == 1) {
            ValList = normalVal;
        } else if (level == 2) {
            ValList = hardlVal;
        }

        eraseAllIncludingPresetCells();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                setCellValues(i, j, ValList[i][j]);
                if (!(ValList[i][j] == 0)) {
                    sudokuCells[i][j].editable = false;
                    sudokuCells[i][j].setEditable(false);
                    sudokuCells[i][j].setForeground(new Color(150, 150, 150));
                }
            }
        }
    }

    /**
     * ファイルを読み込む エラーチェックなし
     */
    private void loadFile() {
        // FileChooserの設定
        JFileChooser fc = new JFileChooser(
                System.getProperty("user.dir") + "/Problems");
        fc.setDialogTitle("ファイルを選択");

        //ファイル選択
        int selected = fc.showOpenDialog(this);
        if (selected == JFileChooser.APPROVE_OPTION) {
            // ファイル読み込み
            File file = fc.getSelectedFile();
            boolean flag = true;
            String[] tmpVal = new String[9];
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String str;
                for (int i = 0; i < 9; i++) {
                    str = br.readLine();
                    if (str == null) {
                        flag = false;
                        break;
                    }
                    if (str.length() != 9) {
                        flag = false;
                        break;
                    }
                    if (i == 8 && br.readLine() != null) {
                        flag = false;
                        break;
                    }
                    tmpVal[i] = str;
                }
            } catch (IOException e) {
                System.err.println("ファイル読み込みエラー");
            }

            // ファイルを読み込めた
            if (flag) {
                eraseAllIncludingPresetCells();
                int val;
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        val = Character.getNumericValue(tmpVal[i].charAt(j));
                        setCellValues(i, j, val);
                        if (!(val == 0)) {
                            sudokuCells[i][j].editable = false;
                            sudokuCells[i][j].setEditable(false);
                            sudokuCells[i][j].setForeground(new Color(150, 150, 150));
                        }
                    }
                }
            }
        }
    }

    /**
     * 3×3のゼル内に同じ数字が含まれてないか確認する
     */
    private boolean isContainedIn3x3Box(int row, int col, int value) {
        // 3x3のボックス内で左上の配列の要素番号を特定する
        int startRow = row / 3 * 3;
        int startCol = col / 3 * 3;

        // 同じ数字がセルに含まれている場合はtrueを返す
        for (int i = startRow; i < startRow + 3; i++) {
            for (int j = startCol; j < startCol + 3; j++) {
                if (!(i == row && j == col)) {
                    if (cellValues[i][j] == value) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 指定した行と列内に同じ数字が含まれていないか確認する
     */
    private boolean isContainedInRowColumn(int row, int col, int value) {
        for (int i = 0; i < 9; i++) {
            if (i != col) {
                if (cellValues[row][i] == value) {
                    return true;
                }
            }
            if (i != row) {
                if (cellValues[i][col] == value) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 全てのセルに値が入っているか判定する
     */
    private boolean isSudokuFull() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (cellValues[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * ランダムに重複しない数を9つ生成する
     */
    private Integer[] generateRandomNumbers() {
        ArrayList<Integer> randoms = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            randoms.add(i + 1);
        }
        Collections.shuffle(randoms);

        return randoms.toArray(new Integer[9]);
    }

    /**
     * セルののUIや値が編集可否などの要素を保持するクラス
     */
    private class SudokuCell extends JTextField {

        /**
         * UID
         */
        private static final long serialVersionUID = 4690751052748480438L;

        //セルが編集可能であるか否かの状態を保持するフラグ
        private boolean editable;

        /**
         * Constructor
         */
        public SudokuCell(final int row, final int col) {
            super(1);

            editable = true;

            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.GRAY));
            setHorizontalAlignment(CENTER);
            setPreferredSize(new Dimension(35, 35));
            setFont(new Font("Lucida Console", Font.BOLD, 28));

            addFocusListener(new FocusListener() {

                @Override
                public void focusGained(FocusEvent arg0) {
                    //選択しているセルと同じ行、列、ボックス内の色を変更するメソッド
                    int startRow = row / 3 * 3;
                    int startCol = col / 3 * 3;

                    for (int i = 0; i < 9; i++) {
                        // 水平
                        sudokuCells[i][col].setBackground(new Color(255, 227, 209));
                        // 垂直
                        sudokuCells[row][i].setBackground(new Color(255, 227, 209));
                    }

                    // 3x3 box
                    for (int i = startRow; i < startRow + 3; i++) {
                        for (int j = startCol; j < startCol + 3; j++) {
                            sudokuCells[i][j].setBackground(new Color(255, 227, 209));
                        }
                    }
                }

                @Override
                public void focusLost(FocusEvent arg0) {
                    //別のセルへカーソルが動いた場合に、注目セルの行、列、ボックス内のセルを白に戻す
                    int startRow = row / 3 * 3;
                    int startCol = col / 3 * 3;

                    for (int i = 0; i < 9; i++) {
                        // 水平
                        sudokuCells[i][col].setBackground(Color.WHITE);
                        // 垂直
                        sudokuCells[row][i].setBackground(Color.WHITE);
                    }

                    // 3x3 box
                    for (int i = startRow; i < startRow + 3; i++) {
                        for (int j = startCol; j < startCol + 3; j++) {
                            sudokuCells[i][j].setBackground(Color.WHITE);
                        }
                    }
                }
            });

            addKeyListener(new KeyAdapter() {

                @Override
                public void keyPressed(KeyEvent e) {
                    // 数字の入力のみ有効
                    if (editable) {
                        if (e.getKeyChar() >= '1' && e.getKeyChar() <= '9') {
                            setEditable(true);
                            setText(""); // １文字にする
                            cellValues[row][col] = e.getKeyChar() - 48;
                        } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                            setEditable(true);
                            setText("0"); // ビープ音を避ける
                            cellValues[row][col] = 0;
                        } else {
                            setEditable(false);
                        }
                    }

                    // 矢印キーによるナビゲーション
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_DOWN:
                            sudokuCells[(row + 1) % 9][col].requestFocusInWindow();
                            break;
                        case KeyEvent.VK_RIGHT:
                            sudokuCells[row][(col + 1) % 9].requestFocusInWindow();
                            break;
                        case KeyEvent.VK_UP:
                            sudokuCells[(row == 0) ? 8 : (row - 1)][col].requestFocusInWindow();
                            break;
                        case KeyEvent.VK_LEFT:
                            sudokuCells[row][(col == 0) ? 8 : (col - 1)].requestFocusInWindow();
                            break;
                    }
                }
            });
        }

    }

    /**
     * 値を保持する配列とUIを保持する配列の 両方へ値を代入するメソッド
     *
     */
    private void setCellValues(int row, int col, int value) {
        cellValues[row][col] = value;
        //値がゼロの場合には0でなく空文字を入力
        if (value == 0) {
            sudokuCells[row][col].setText(String.valueOf(""));
            sudokuCells[row][col].setForeground(Color.WHITE);
            sudokuCells[row][col].setEditable(true);
        } else {
            sudokuCells[row][col].setText(String.valueOf(value));
            sudokuCells[row][col].setForeground(Color.BLACK);
        }
    }

    /**
     * Base Line Algorithm (BackTrack) 再起的数独解法アルゴリズム（総当たりアルゴリズム）
     *
     * @param 現在の行番号.
     * @param 現在の列番号.
     * @return 解が出た場合true、解がでない場合falseを返す.
     */
    private boolean bruteForceSolve(int row, int col) {
        //全てのセルへの入力が終了している場合にメソッドを閉じていく
        if (row == 9) {
            return true;
        }

        // 注目したセルへ既に数字が入力されている場合に処理を次のセルへ移動する
        if (!(checkEmptyCell(row, col))) {         //
            if (bruteForceSolve(col == 8 ? (row + 1) : row, (col + 1) % 9)) {
                return true;
            }
        } else {
            Integer[] randoms = generateRandomNumbers();
            for (int i = 0; i < 9; i++) {
                // 行、列、3x3ボックス内に数字が重複しない場合に値を代入し、次のセルへ進む
                if (!isContainedInRowColumn(row, col, randoms[i])
                        && !isContainedIn3x3Box(row, col, randoms[i])) {
                    setCellValues(row, col, randoms[i]);
                    // 次のマス（左から右へ、上から下へという順）に進む
                    if (bruteForceSolve(col == 8 ? (row + 1) : row, (col + 1) % 9)) {
                        return true;
                    } else {
                        // 次のマスに入る数字がなく、このマスに別の数字を入れないといけないので、一度入れた数字を初期化する
                        setCellValues(row, col, 0);
                    }
                }
            }
        }

        return false;
    }

    //セルが空白であるか調べて、空白セルをチェックした回数をカウントする
    private boolean checkEmptyCell(int row, int col) {
        if (cellValues[row][col] == 0) {
            accessNum++;
            return true;
        }
        return false;
    }

    /**
     * Advanced Algorithm（初級） 空白マスの行、列、3x3ボックス内の数字を調べ、一意に当てはまる数字を入力する
     * 一意に当てはまる数字がなくなった場合は、総当たりアルゴリズムに処理を移す
     *
     * @return 解が全てのセルで定まるとtrueを返す
     */
    private boolean advanced() {
        //解の候補を保持するリスト
        List<Integer> candidateSolution = new ArrayList<>();
        listFormat(candidateSolution);

        //候補から除外する数字を一時格納するリスト
        List<Integer> removeCell = new ArrayList<>();

        //解の候補を一意に特定できる空欄がなくなったか判定するフラグ
        boolean loopFlag;

        //全ての空欄がなくなるまで繰り返す
        while (checkEmpty()) {
            loopFlag = false;
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (checkEmptyCell(i, j)) {
                        //System.out.print("注目番号["+i+","+j+"]");
                        //列１列か行１列に既に数字が入っているかチェック
                        removeCell.addAll(checkCellRowColmun(i, j));
                        //3x3のボックス内に数字が入っているかチェックし、重複があれば削除
                        removeCell.addAll(checkCell3x3Box(i, j));
                        //行、列、ボックス内に既に入っている数字を削除する
                        candidateSolution = unique(candidateSolution, removeCell);
                        //解の候補が１つになった場合その数を代入する
                        if (candidateSolution.size() == 1) {
                            setCellValues(i, j, candidateSolution.get(0));
                            loopFlag = true;
                        }
                        //解の候補の初期化
                        listFormat(candidateSolution);
                        removeCell.clear();
                    }
                }
            }
            if (loopFlag == false) {
                bruteForceSolve(0, 0);
            }
        }
        return true;
    }

    /**
     * リストを初期化し、1から9までの数値を入力するメソッド
     */
    private List<Integer> listFormat(List<Integer> list) {
        list.clear();
        for (int x = 1; x <= 9; x++) {
            list.add(x);
        }
        return list;
    }

    /**
     * ２つのリストからユニークな数字を抜き出して返す
     */
    public static List<Integer> unique(List<Integer> arg0, List<Integer> arg1) {
        int tmp;
        //重複要素の削除
        for (Integer arg1_1 : arg1) {
            tmp = arg1_1;
            for (int j = 0; j < arg0.size(); j++) {
                if (tmp == arg0.get(j)) {
                    arg0.remove(j);
                }
            }
        }
        return arg0;
    }

    /**
     * 全てセルの中に空欄がないかチェックする
     */
    private boolean checkEmpty() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (cellValues[i][j] == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 3x3のボックス内に数字が含まれていないかチェックする
     */
    private List<Integer> checkCell3x3Box(int row, int col) {
        List<Integer> removeCell = new ArrayList<>();
        // 3x3のボックス内で左上の配列の要素番号を特定する
        int startRow = row / 3 * 3;
        int startCol = col / 3 * 3;

        for (int i = startRow; i < startRow + 3; i++) {
            for (int j = startCol; j < startCol + 3; j++) {
                if (cellValues[i][j] > 0) {
                    removeCell.add(cellValues[i][j]);
                }
            }
        }
        return removeCell;
    }

    /**
     * 入力した数字と同じ行か列に数字が含まれているか確認
     */
    private List<Integer> checkCellRowColmun(int row, int col) {
        int tmpCol = col;
        int tmpRow = row;

        List<Integer> removeCell = new ArrayList<>();
        for (col = 0; col < 9; col++) {
            if (!(cellValues[tmpRow][col] == 0)) {
                removeCell.add(cellValues[tmpRow][col]);
            }
        }
        for (row = 0; row < 9; row++) {
            if (!(cellValues[row][tmpCol] == 0)) {
                removeCell.add(cellValues[row][tmpCol]);
            }
        }
        return removeCell;
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    // number.get(row).get(col)には(row,col)に入る可能性のある数字が入っている
    // cellValue[row][col]に数字が入っている場合には無効
    private ArrayList<ArrayList<ArrayList<Integer>>> candidates;

    /**
     * セルの情報を持てるクラス
     */
    private class Cell {

        public int row;
        public int col;
        public int value;

        public Cell() {
            this(0, 0, 0);
        }

        public Cell(int row, int col, int value) {
            this.row = row;
            this.col = col;
            this.value = value;
        }
    }

    /**
     * 配列作成(コンストラクタで使用)
     */
    private void secureCandidates() {
        // 配列の確保のみ
        candidates = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            candidates.add(new ArrayList<ArrayList<Integer>>());
            for (int j = 0; j < 9; j++) {
                candidates.get(i).add(new ArrayList<Integer>());
            }
        }
    }

    /**
     * 配列初期化
     */
    private void initCandidates() {
        // 配列numberを初期化(全ての要素に1から9までの数字を入れる)
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                candidates.get(i).get(j).clear();
                for (int num = 1; num <= 9; num++) {
                    candidates.get(i).get(j).add(num);
                }
            }
        }

        // 配列から初期要素を消す
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                // 数字で埋まっているとき
                if (!checkEmptyCell(i, j)) {
                    erase(i, j, cellValues[i][j]);
                    // 下は無くても良い
                    candidates.get(i).get(j).clear();
                }
            }
        }
    }

    /**
     * 縦、横、ボックスからvalueを削除 削除したCellを返す
     */
    private ArrayList<Cell> erase(int row, int col, int value) {
        ArrayList<Cell> ret = new ArrayList<>();

        // 含まれているボックスの左上の配列の要素番号を特定する
        int x = col / 3 * 3;
        int y = row / 3 * 3;

        for (int k = 0; k < 9; k++) {
            // 縦に削除
            if (candidates.get(k).get(col).contains(value) && checkEmptyCell(k, col)) {
                candidates.get(k).get(col).remove(candidates.get(k).get(col).indexOf(value));
                ret.add(new Cell(k, col, value));
            }
            // 横に削除 
            if (candidates.get(row).get(k).contains(value) && checkEmptyCell(row, k)) {
                candidates.get(row).get(k).remove(candidates.get(row).get(k).indexOf(value));
                ret.add(new Cell(row, k, value));
            }
            // ボックスから削除
            if (candidates.get(y).get(x).contains(value) && checkEmptyCell(y, x)) {
                candidates.get(y).get(x).remove(candidates.get(y).get(x).indexOf(value));
                ret.add(new Cell(y, x, value));
            }
            // ボックス内の次の要素へ
            x += 1;
            if (x % 3 == 0) {
                x -= 3;
                y += 1;
            }
        }

        return ret;
    }

    /**
     * 自作アルゴリズム
     */
    private boolean myAlgorithm() {
        initCandidates();
        return mainAlgorithm(0, 0);
    }

    /**
     * メインの再帰関数
     */
    private boolean mainAlgorithm(int row, int col) {
        // 全てのセルへの入力が終了している場合にメソッドを閉じていく
        if (row == 9) {
            return true;
        }

        // 確実に埋めれるセルを埋める
        ArrayList<Cell> in = fill();

        // あるセルに入れれる数字がなかった場合
        if (!check()) {
            // セルを戻す
            for (Cell c : in) {
                candidates.get(c.row).get(c.col).add(c.value);
                setCellValues(c.row, c.col, 0);
            }
            return false;
        }

        // 注目したセルへ既に数字が入力されている場合に処理を次のセルへ移動する
        if (!(checkEmptyCell(row, col))) {
            if (mainAlgorithm(col == 8 ? (row + 1) : row, (col + 1) % 9)) {
                return true;
            }
        } else {
            // 解の候補を保持するリスト
            ArrayList<Integer> candidateSolution = new ArrayList<>(candidates.get(row).get(col));

            for (Integer value : candidateSolution) {
                ArrayList<Cell> ed = erase(row, col, value);
                setCellValues(row, col, value);

                // 次のマス（左から右へ、上から下へという順）に進む
                if (mainAlgorithm(col == 8 ? (row + 1) : row, (col + 1) % 9)) {
                    return true;
                } else {
                    // 次のマスに入る数字がなく、このマスに別の数字を入れないといけないので、一度入れた数字を初期化する
                    for (Cell c : ed) {
                        candidates.get(c.row).get(c.col).add(c.value);
                        setCellValues(c.row, c.col, 0);
                    }
                }
            }
        }

        // 入る数字がなかったので、一度入れた数字を初期化する
        for (Cell c : in) {
            candidates.get(c.row).get(c.col).add(c.value);
            setCellValues(c.row, c.col, 0);
        }
        return false;
    }

    /**
     * CellValuesを埋めれるだけ埋める
     */
    private ArrayList<Cell> fill() {
        ArrayList<Cell> ret = new ArrayList<>();
        boolean flag;
        int value;
        do {
            flag = false;
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (checkEmptyCell(i, j)) {
                        // 解の候補が１つになった場合その数を代入する
                        if (candidates.get(i).get(j).size() == 1) {
                            value = candidates.get(i).get(j).get(0);
                            ret.addAll(erase(i, j, value));
                            setCellValues(i, j, value);
                            flag = true;
                        }
                    }
                }
            }
        } while (flag);
        return ret;
    }

    /**
     * どこかのセルに入れれる数字がなくなったらfalseを返す
     */
    private boolean check() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (checkEmptyCell(i, j) && candidates.get(i).get(j).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    /**
     * @param args
     */
    public static void main(String[] args) {
        new Sudoku();
    }
}
