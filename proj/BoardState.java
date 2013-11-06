package proj;

import java.util.ArrayList;
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
	static public class Heuristic implements Comparable {
		String msg;
		int black;
		int white;
		BoardState bs;
		public Heuristic(int black, int white, BoardState bs) {
			this.black = black;
			this.white = white;
			this.bs=bs;
		}

		@Override
		public String toString() {
			return String.format("possible moves: %d(Black) - %d(White) = %d",
					black, white, this.getValue());
		}

		public int getValue() {
			if (white == 0)
				return Integer.MAX_VALUE;
			if (black == 0)
				return Integer.MIN_VALUE;
			return black - white;
		}

		@Override
		public int compareTo(Object o) {
			Heuristic h1 = this, h2 = (Heuristic) o;
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

		public MoveInfo(BoardPosition pos, TypeMove m) {
			this.pos = pos;
			this.m = m;
		}
	}

	public MoveInfo getBestMove(Player p, int n) throws Exception {
		Heuristic h=this.getHeuristic(p, n);
		BoardState child=h.bs;
		while(child.parent!=this){
			if(child.parent==null) throw new Exception("Error in function of searching heuristic."); 
			child=child.parent;
		}
		
		return new MoveInfo(child.posMoved, child.by);
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
		
		List<BoardState> bss=this.getChildren(p);
		Set<Heuristic> hs=new HashSet<Heuristic>();
		for(BoardState bs: bss){
			hs.add(bs.getHeuristic(p.shift(), --n));
		}
		if(p==Player.BLACK){
			return Collections.max(hs);
		}else{
			return Collections.min(hs);
		}
		
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
		for (BoardPosition pos : poss)
			for (TypeMove m : b.getAvailableMove(pos)) {
				BoardState s = this.getNext(m, p, pos);
				ss.add(s);
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

	static public void main(String[] args) throws Exception {
		Board b = new Board();
		BoardState bs = new BoardState(b.cells, null, null, null);
		Player p = b.getPlayer();
		bs.isPathFailure(p);
	}
}