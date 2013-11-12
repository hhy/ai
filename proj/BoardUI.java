package proj;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import proj.Board.BoardPosition;
import proj.Board.Player;
import proj.Board.TypeMove;
import proj.BoardState.MoveInfo;

public class BoardUI extends JFrame implements ActionListener {

	Board board;

	public JPanel setActPanel() {
		JPanel jp = new JPanel(new GridLayout(1, 2));
		JPanel jpBtns = new JPanel(new GridLayout(3, 1));
		JPanel jpAI = new JPanel(new GridLayout(1, 3));

		this.btnAiPlay = new JButton(this.board.getPlayer() + " Robot Play");
		this.btnAiPlay.addActionListener(this);
		JLabel lbMinMax = new JLabel("  Depth of MinMax: ");
		this.txtMinMax = new JTextField();

		jpAI.add(btnAiPlay);
		jpAI.add(lbMinMax);
		jpAI.add(txtMinMax);

		this.btnPlay = new JButton(this.board.getPlayer() + " Play");
		this.btnPlay.addActionListener(this);
		jpBtns.add(jpAI);
		jpBtns.add(this.btnPlay);
		this.btnRollback = new JButton("Undo");
		this.btnRollback.addActionListener(this);
		jpBtns.add(this.btnRollback);

		jp.add(jpBtns);

		this.dirG = new ButtonGroup();

		JPanel jpG = new JPanel(new GridLayout(Math.round(Math.round(Math
				.ceil(TypeMove.values().length / 2))), 2));
		this.dirs = new JRadioButton[TypeMove.values().length];
		for (TypeMove tm : TypeMove.values()) {
			this.dirs[tm.i - 1] = new JRadioButton("" + tm.i + ". " + tm.name());
			this.dirs[tm.i - 1].setActionCommand("" + tm.i);
			this.dirs[tm.i - 1].addActionListener(this);
			dirG.add(this.dirs[tm.i - 1]);
			jpG.add(this.dirs[tm.i - 1]);
		}
		jp.add(jpG);
		return jp;

	}

	public JPanel setBoardPanel() {
		JPanel jp = new JPanel();
		int lengthRow = this.board.ColMax - this.board.ColMin + 1;
		int numRow = this.board.RowMax - this.board.RowMin + 1;
		jp.setLayout(new GridLayout(numRow + 1, lengthRow + 1));

		jp.add(new JLabel());
		for (char c = 'A'; c < 'A' + lengthRow; c++) {
			jp.add(new JLabel("   " + c));
		}

		this.cells = new JButton[numRow][];
		for (int row = 0; row < numRow; row++) {
			jp.add(new JLabel("" + (1 + row)));
			this.cells[row] = new JButton[lengthRow];
			for (int col = 0; col < lengthRow; col++) {
				cells[row][col] = new JButton();
				cells[row][col].setActionCommand(String.format("%d%c",
						(row + 1), ('A' + col)));

				cells[row][col].addActionListener(this);
				jp.add(this.cells[row][col]);
			}
		}
		return jp;
	}

	public JPanel setMsgPanel() {

		JPanel jp = new JPanel();

		this.txtLog = new JTextArea();
		JScrollPane sp = new JScrollPane(this.txtLog);
		sp.setPreferredSize(new Dimension(400, 470));
		// this.txt.setAutoscrolls(true);
		jp.add(sp);
		return jp;
	}

	JRadioButton[] dirs;
	JButton btnPlay, btnAiPlay;
	JButton btnRollback;
	JButton[][] cells;
	JTextArea txtLog;
	JTextField txtMinMax;
	ButtonGroup dirG;

	public BoardUI() {
		super("project 1, board game");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBounds(50, 50, 1080, 600);
		this.setLayout(new BorderLayout());

		this.board = new Board();

		this.getContentPane().add(this.setActPanel(), BorderLayout.NORTH);
		this.getContentPane().add(this.setBoardPanel(), BorderLayout.WEST);
		this.getContentPane().add(this.setMsgPanel(), BorderLayout.EAST);

		this.board.setUI(this);

		this.updateBoardUI();

		this.sDirs = new HashSet<JRadioButton>();
		this.sCells = new HashSet<JButton>();
		for (int i = 0; i < this.dirs.length; i++) {
			this.sDirs.add(this.dirs[i]);
		}
		for (int row = 0; row < this.cells.length; row++) {
			for (int col = 0; col < this.cells[row].length; col++)
				this.sCells.add(this.cells[row][col]);
		}

	}

	public void updateBoardUI() {

		this.btnPlay.setText(this.board.getPlayer() + " play");
		this.btnAiPlay.setText(this.board.getPlayer() + " Robot play");
		this.btnPlay.setEnabled(false);
		for (int i = 0; i < this.dirs.length; i++) {
			this.dirs[i].setEnabled(false);
		}
		for (int i = 0; i < this.cells.length; i++) {
			for (int j = 0; j < this.cells[i].length; j++) {
				Board.Cell c = this.board.getCell(new BoardPosition(i, j));
				if (c.player != null) {
					this.cells[i][j].setText(c.player + "-" + c.n);
				} else {
					this.cells[i][j].setText("                       ");
				}
				this.cells[i][j].setEnabled(false);
			}
		}
		List<BoardPosition> ps = this.board.getAvailablePos();
		for (BoardPosition p : ps) {
			this.cells[p.row][p.col].setEnabled(true);
		}
		this.btnRollback.setEnabled(this.board.isUndoable());
		if (this.board.lost()) {
			String msg = this.board.getPlayer() + " can't move, then "
					+ this.board.getPlayer() + " lose!";
			JOptionPane.showMessageDialog(this, msg);
		}
		this.dirG.clearSelection();
		if (this.txtMinMax.getText() == null
				|| this.txtMinMax.getText().length() == 0
				|| !this.txtMinMax.getText().matches("\\d+"))
			this.txtMinMax.setText("3");
	}

	public static void main(String[] args) {
		BoardUI board = new BoardUI();
		board.setVisible(true);

	}

	Set<JRadioButton> sDirs;
	Set<JButton> sCells;

	public void log(String s) {
		this.txtLog.setText(this.txtLog.getText() + "\n" + s);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object btn = e.getSource();
		if (btn == this.btnPlay) {
			try {
				this.move();
			} catch (Exception e1) {
				this.log(e1.getMessage());
			}
		} else if (btn == this.btnAiPlay) {
			BoardState bs = new BoardState(this.board.cells, null, null, null);
			try {
				int depth=3;
				try {
					depth = Integer.parseInt(this.txtMinMax.getText());
				} catch (Exception ee) {

				}
				MoveInfo mi = bs.getBestMove(this.board.getPlayer(), depth);
				String msg = "get the move suggestion, heuristic value: "
						+ mi.h.getValue();
				JOptionPane.showMessageDialog(this, msg);
				this.board.setPosition(mi.pos);
				this.toMove = mi.m;
				this.move();

			} catch (Exception e1) {
				this.log(e1.getMessage());
			}
		} else if (btn == this.btnRollback) {
			this.board.undo();
		} else if (this.sDirs.contains(btn)) {
			JRadioButton _btn = (JRadioButton) btn;
			int i = Integer.parseInt(_btn.getText().substring(0, 1));
			this.toMove = TypeMove.getValue(i);
			this.btnPlay.setEnabled(true);

		} else if (this.sCells.contains(btn)) {
			JButton _btn = (JButton) btn;
			BoardPosition p = new BoardPosition(e.getActionCommand()), pp = this.board
					.getCurrentPos();
			if (pp != null)
				if (p.equals(pp)) {
					return;
				} else {
					JButton btnOld = this.cells[pp.row][pp.col];
					btnOld.setText(btnOld.getText().substring(1,
							btnOld.getText().length() - 1));
				}

			try {
				// this.log("set current postion: " + p);
				this.board.setPosition(p);
			} catch (Exception e1) {
				this.log(e1.getMessage());
				return;
			}
			_btn.setText("[" + _btn.getText() + "]");
			this.updateAvailableDir(p);

		}

	}

	public void updateAvailableDir(BoardPosition p) {
		List<TypeMove> moves = this.board.getAvailableMove(p);
		for (int i = 0; i < this.dirs.length; i++) {
			int nn = Integer.parseInt(this.dirs[i].getText().substring(0, 1));
			TypeMove m = TypeMove.getValue(nn);
			this.dirs[i].setEnabled(moves.contains(m));
		}
	}

	TypeMove toMove;

	public void move() throws Exception {
		this.log(this.board.move(toMove));
		this.toMove = null;
	}

}
