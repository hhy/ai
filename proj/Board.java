package proj;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Board {

	static public enum Player {
		BLACK, WHITE;
		boolean isAI;

		public Player shift() {
			if (this == BLACK)
				return WHITE;
			return BLACK;
		}

		public void setAI() {
			this.isAI = true;
		}
	}

	static public class Cell {
		int n;
		Player player;

		public Cell() {
			this.n = 0;
			this.player = null;
		}

		public Cell(int n, Player player) {
			this.n = n;
			this.player = player;
		}

		public void setCell(int n, Player player) {
			this.n = n;
			this.player = player;
		}

		@Override
		protected Object clone() throws CloneNotSupportedException {
			return new Cell(n, player);
		}

	}

	static public enum TypeMove {
		Up(1), UpRight(2), Right(3), DownRight(4), Down(5), DownLeft(6), Left(7), UpLeft(
				8);
		int i;

		private TypeMove(int i) {
			this.i = i;
		}

		static TypeMove getValue(int i) {
			for (TypeMove m : TypeMove.values()) {
				if (m.i == i)
					return m;
			}
			return null;
		}

	}

	Cell[][] cells;

	final static int RowMax = 4, RowMin = 1;
	final static char ColMax = 'D', ColMin = 'A';

	public static class BoardPosition {
		static final int rMax = RowMax - RowMin, cMax = ColMax - ColMin;
		int row;
		int col;

		@Override
		public int hashCode() {
			return this.row * rMax + this.col;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (obj == this)
				return true;
			if (obj.getClass() != BoardPosition.class)
				return false;
			BoardPosition p = (BoardPosition) obj;
			return (this.row == p.row && this.col == p.col);
		}

		@Override
		protected Object clone() throws CloneNotSupportedException {
			return new BoardPosition(this.row, this.col);
		}

		BoardPosition(String pos) {
			this.row = Integer.parseInt(pos.substring(0, 1)) - 1;
			this.col = pos.charAt(1) - 'A';
		}

		BoardPosition(int row, int col) {
			this.row = row;
			this.col = col;
		}

		BoardPosition getNeighbor(TypeMove m) {
			switch (m) {
			case Down:
				if (this.row < rMax) {
					return new BoardPosition(this.row + 1, this.col);
				}
				break;
			case Up:
				if (this.row > 0) {
					return new BoardPosition(this.row - 1, this.col);
				}
				break;
			case Left:
				if (this.col > 0) {
					return new BoardPosition(this.row, this.col - 1);
				}
				break;
			case Right:
				if (this.col < cMax) {
					return new BoardPosition(this.row, this.col + 1);
				}
				break;
			case UpRight:
				if (this.row > 0 && this.col < cMax) {
					return new BoardPosition(this.row - 1, this.col + 1);
				}
				break;
			case UpLeft:
				if (this.row > 0 && this.col > 0) {
					return new BoardPosition(this.row - 1, this.col - 1);
				}
				break;
			case DownLeft:
				if (this.row < rMax && this.col > 0) {
					return new BoardPosition(this.row + 1, this.col - 1);
				}
				break;
			case DownRight:
				if (this.row < rMax && this.col < cMax) {
					return new BoardPosition(this.row + 1, this.col + 1);
				}
				break;
			}
			return null;
		}

		public String toString() {
			return String.format("%d%c", this.row + 1, 'A' + this.col);
		}
	}

	private BoardPosition posCurrent;
	private Player[] players;

	public Player getPlayer() {
		if (this.players == null) {
			this.players = new Player[2];
			this.players[1] = Player.WHITE;
			this.players[0] = Player.BLACK;
		}
		return this.players[0];
	}

	public BoardPosition getCurrentPos() {
		if (this.posCurrent == null)
			return null;
		return new BoardPosition(this.posCurrent.row, this.posCurrent.col);
	}

	private void switchPlayer() {

		Player p = this.players[0];
		this.players[0] = this.players[1];
		this.players[1] = p;
	}

	public void setPosition(BoardPosition p) throws Exception {

		if (!this.getAvailablePos().contains(p)) {
			throw new Exception("u r trying select  a Illegal Position: "
					+ p.toString());
		}
		this.posCurrent = (BoardPosition) p.clone();
	}

	public List<BoardPosition> getAvailablePos() {
		List<BoardPosition> poss = new ArrayList<BoardPosition>();
		for (int row = 0; row < this.cells.length; row++) {
			for (int col = 0; col < this.cells[row].length; col++) {
				Cell c = this.cells[row][col];
				if (c.player != null && c.player == this.getPlayer())
					poss.add(new BoardPosition(row, col));
			}
		}
		return poss;
	}

	public List<TypeMove> getAvailableMove(BoardPosition p) {
		System.out.print(p + " available moves: [");
		List<TypeMove> moves = new ArrayList<TypeMove>();
		for (TypeMove m : TypeMove.values()) {
			BoardPosition pp = p.getNeighbor(m);
			if (pp == null)
				continue;
			if (this.getCell(pp).player == null
					|| this.getCell(pp).player == this.getPlayer()) {
				System.out.print(m + ", ");
				moves.add(m);
			}

		}
		System.out.println("]");
		return moves;
	}

	public Cell getCell(BoardPosition p) {

		return this.cells[p.row][p.col];
	}

	public String move(TypeMove m) throws Exception {

		if (!this.getAvailableMove(this.posCurrent).contains(m))
			throw new Exception("This is illegal move.");
		this.save();
		BoardPosition pos0 = this.posCurrent.getNeighbor(m);
		int availableFreeCells = 1;
		BoardPosition pos1 = pos0.getNeighbor(m);
		BoardPosition pos2 = null;
		if (pos1 != null
				&& (this.getCell(pos1).player == this.getPlayer() || this
						.getCell(pos1).player == null)) {
			availableFreeCells++;
			pos2 = pos1.getNeighbor(m);
			if (pos2 != null
					&& (this.getCell(pos2).player == this.getPlayer() || this
							.getCell(pos2).player == null)) {
				availableFreeCells++;
			}
		}
		// System.out.println(availableFreeCells);

		Cell c = this.getCell(posCurrent);
		int cn = c.n;
		c.player = null;
		c.n = 0;

		Cell c0 = this.getCell(pos0);
		c0.player = this.getPlayer();
		if (availableFreeCells == 1 || cn == 1) {
			c0.n += cn;
		} else {
			Cell c1 = this.getCell(pos1);
			c0.n += 1;
			c1.player = this.getPlayer();
			if (availableFreeCells == 2 || cn <= 3) {
				c1.n += cn - 1;
			} else {
				c1.n += 2;
				Cell c2 = this.getCell(pos2);
				c2.player = this.getPlayer();
				c2.n += cn - 3;
			}
		}
		String msg = String.format("[ %s - %s ] move ( %s ) %d steps",
				this.getPlayer(), this.posCurrent, m, availableFreeCells);
		this.switchPlayer();
		this.posCurrent = null;
		this.updateUI();
		return msg;
	}

	private Stack<Cell[][]> history;

	public boolean isUndoable() {
		return !this.history.empty();
	}

	public void undo() {
		if (this.history.empty())
			return;
		Cell[][] cs = this.history.pop();
		this.cells = cs;
		this.switchPlayer();
		this.updateUI();
	}

	private void save() {
		Cell[][] cs = new Cell[this.cells.length][];
		for (int row = 0; row < this.cells.length; row++) {
			cs[row] = new Cell[this.cells[row].length];
			for (int col = 0; col < this.cells[row].length; col++) {
				Cell c = this.cells[row][col];
				cs[row][col] = new Cell(c.n, c.player);
			}
		}
		this.history.push(cs);
	}

	public void updateUI() {
		if (this.ui == null)
			return;

		this.ui.updateBoardUI();
	}

	BoardUI ui;

	public void setUI(BoardUI ui) {
		this.ui = ui;
	}

	private boolean undoable = false;

	public Board() {

		this.cells = new Cell[RowMax - RowMin + 1][];

		for (int row = 0; row < this.cells.length; row++) {
			this.cells[row] = new Cell[this.ColMax - this.ColMin + 1];
			for (char col = 0; col < this.cells[row].length; col++) {
				this.cells[row][col] = new Cell();
			}
		}
		this.getCell(new BoardPosition("1A")).setCell(10, Player.BLACK);
		this.getCell(new BoardPosition("4D")).setCell(10, Player.WHITE);
		this.history = new Stack<Cell[][]>();
		if (this.players == null) {
			this.players = new Player[2];
			this.players[1] = Player.WHITE;
			this.players[0] = Player.BLACK;
		}
		// this.player = Player.BLACK;
	}

	public boolean lost() {
		List<BoardPosition> posList = this.getAvailablePos();
		// if(posList.size()==0) return true;
		for (BoardPosition pos : posList) {
			List<TypeMove> moves = this.getAvailableMove(pos);
			if (moves.size() != 0)
				return false;
		}
		return true;
	}

	void setPlayer(Player p) {
		if (this.players[0] != p)
			this.switchPlayer();
	}

	void setPlayerToAI(Player p) {
		if (this.players[0] == p)
			this.players[0].setAI();
		else
			this.players[1].setAI();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Board b = new Board();

	}

}
