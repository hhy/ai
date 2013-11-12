package proj;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import proj.Board.BoardPosition;
import proj.Board.Cell;
import proj.Board.Player;
import proj.Board.TypeMove;

class BoardState {
	Cell[][] current;
	TypeMove by;
	BoardPosition posMoved;
	BoardState parent;
	List<BoardState> children;

	/**
	 * give the number difference of possible moves that the players have (Black
	 * - White)
	 * 
	 * @param p
	 * @return
	 */
	static public class Heuristic implements Comparable<Heuristic> {
		String msg;
		int black;
		int white;
		BoardState bs;

		public Heuristic(int black, int white, BoardState bs) {
			this.black = black;
			this.white = white;
			this.bs = bs;

		}

		@Override
		public String toString() {
			return String.format("possible moves: %d(Black) - %d(White) = %d",
					black, white, this.getValue());
		}

		public int getValue() {
			if (white <= 1 && black > 5)
				return Integer.MAX_VALUE;
			if (black <= 1 && white > 5)
				return Integer.MIN_VALUE;
			return black - white;
		}

		@Override
		public int compareTo(Heuristic h2) {
			Heuristic h1 = this;
			// if (h1.getValue() == Integer.MAX_VALUE
			// || h2.getValue() == Integer.MIN_VALUE
			// || h1.getValue() == Integer.MIN_VALUE
			// || h2.getValue() == Integer.MAX_VALUE)
			// System.out.println(String.format(
			// "compare, this: %d, other: %d", this.getValue(),
			// h2.getValue()));

			if (h1.getValue() == Integer.MAX_VALUE
					|| h2.getValue() == Integer.MIN_VALUE)
				return 1;
			if (h1.getValue() == Integer.MIN_VALUE
					|| h2.getValue() == Integer.MAX_VALUE)
				return -1;
			return h1.getValue() - h2.getValue();
		}

	}

	static public class MoveInfo {
		Board.BoardPosition pos;
		TypeMove m;
		Heuristic h;

		public MoveInfo(BoardPosition pos, TypeMove m, Heuristic h) {
			this.pos = pos;
			this.m = m;
			this.h = h;
		}
	}

	final static long TIMEOUT = 3000;
	static long startingSearching;
	static boolean isTimeout = false;

	public MoveInfo getBestMove(Player p, int n) throws Exception {
		BoardState.startingSearching = Calendar.getInstance().getTimeInMillis()
				+ TIMEOUT;

		Heuristic h = this.getHeuristic(p, n);
		BoardState child = h.bs;
		int i = 0;
		while (child.parent != this) {
			if (child.parent == null) {
				System.out.println("Error in function of searching heuristic.");
				throw new Exception("Error in function of searching heuristic.");
			}
			child = child.parent;
			if (i++ > n) {
				System.out.println("Error in MinMax.");
				throw new Exception("Error in MinMax");
			}

		}

		return new MoveInfo(child.posMoved, child.by, h);
	}

	Heuristic getHeuristic(Player p) throws Exception {

		int movesBlack = this.getChildren(Player.BLACK).size();
		int movesWhite = this.getChildren(Player.WHITE).size();
		return new Heuristic(movesBlack, movesWhite, this);

	}

	Heuristic getHeuristic(Player p, int n) throws Exception {
		if (n == 0) {
			return this.getHeuristic(p);
		}

		Heuristic current = this.getHeuristic(p);
		if (p == Player.BLACK && current.getValue() == Integer.MAX_VALUE) {
			return current;

		}
		if (p == Player.WHITE && current.getValue() == Integer.MIN_VALUE) {
			return current;

		}

		List<BoardState> bss = this.getChildren(p);
		// Set<Heuristic> hs = new HashSet<Heuristic>();
		n--;
		if (bss.size() == 0)
			return this.getHeuristic(p);

		Heuristic h = bss.get(0).getHeuristic(p.shift(), n);
		if (p == Player.BLACK) {
			if (h.getValue() == Integer.MAX_VALUE) {
				return h;
			}
		}
		if (p == Player.WHITE) {
			if (h.getValue() == Integer.MIN_VALUE) {
				return h;
			}
		}

		// hs.add(h);
		for (BoardState bs : bss) {
			 if (BoardState.isTimeout)
			 break;
			 if (Calendar.getInstance().getTimeInMillis() >
			 BoardState.startingSearching) {
			 System.out
			 .println("I couldn't finish the iteration, but timeout, Take whaterever there are");
			 BoardState.isTimeout = true;
			 break;
			 }

			if (bs == bss.get(0))
				continue;

			Heuristic bsH = bs.getHeuristic(p.shift(), n);

			if (p == Player.BLACK) {
				if (bsH.getValue() == Integer.MAX_VALUE) {
					h = bsH;
					break;
				}
				if (bsH.compareTo(h) <= 0)
					continue;
			}
			if (p == Player.WHITE) {
				if (bsH.getValue() == Integer.MIN_VALUE) {
					h = bsH;
					break;

				}
				if (bsH.compareTo(h) >= 0)
					continue;
			}

			h = bsH;
			// hs.add(bsH);

		}

		// if (p == Player.BLACK) {
		// return Collections.max(hs);
		// } else {
		// return Collections.min(hs);
		// }
		return h;

	}

	public BoardState(Cell[][] current, BoardState parent, TypeMove by,
			BoardPosition bp) {
		this.current = current;
		this.by = by;
		this.posMoved = bp;
		this.parent = parent;

	}

	private List<BoardState> getChildren(Player p) throws Exception {
		List<BoardState> ss = new ArrayList<BoardState>();
		Board b = new Board();
		b.setPlayer(p);
		b.cells = this.current;
		List<BoardPosition> poss = b.getAvailablePos();
		for (BoardPosition pos : poss) {
			List<TypeMove> ms = b.getAvailableMove(pos);
			for (TypeMove m : ms) {
				BoardState s = this.getNext(m, p, pos);
				ss.add(s);
			}
		}
		return ss;
	}

	private BoardState getNext(TypeMove m, Player p, BoardPosition pos)
			throws Exception {
		Cell[][] cs = new Cell[this.current.length][];
		for (int i = 0; i < this.current.length; i++) {
			cs[i] = new Cell[this.current[i].length];
			for (int j = 0; j < this.current[i].length; j++) {
				cs[i][j] = (Cell) this.current[i][j].clone();
			}
		}
		Board b = new Board();
		b.cells = cs;
		b.setPlayer(p);
		b.setPosition(pos);
		b.move(m);
		return new BoardState(cs, this, m, pos);
	}

	boolean isFinalFailureState(Player p) throws Exception {
		if (children == null)
			this.children = this.getChildren(p);
		if (this.children.size() == 0)
			return true;
		return false;
	}

	boolean isPathFailure(Player p) throws Exception {
		if (this.isFinalFailureState(p))
			return true;
		for (BoardState bs : this.getChildren(p)) {
			return bs.isPathFailure(p.shift());
		}
		return false;
	}

}