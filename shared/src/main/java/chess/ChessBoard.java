package chess;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private Map<ChessPosition, ChessPiece> board;
    public ChessBoard() {
        board = new HashMap<>();
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.equals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(board);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board.put(position, piece);
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board.get(position);
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        //Board should look like the start of a chess game, use addPiece()
        startingPieces(ChessGame.TeamColor.WHITE, 1, 2);
        startingPieces(ChessGame.TeamColor.BLACK, 8, 7);
    }
    /**
     * Sets the starting pieces in the board game
     * @param side is for the team color
     * @param nonPawns defines the row where the ranked pieces will go
     * @param pawns is the row where the pawns will go
     */
    public void startingPieces(ChessGame.TeamColor side, int  nonPawns, int pawns) {
        addPiece(new ChessPosition(nonPawns, 1), new ChessPiece(side, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(nonPawns, 2), new ChessPiece(side, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(nonPawns, 3), new ChessPiece(side, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(nonPawns, 4), new ChessPiece(side, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(nonPawns, 5), new ChessPiece(side, ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(nonPawns, 6), new ChessPiece(side, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(nonPawns, 7), new ChessPiece(side, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(nonPawns, 8), new ChessPiece(side, ChessPiece.PieceType.ROOK));
        for(int i = 1; i <= 8; i++){
            addPiece(new ChessPosition(pawns, i), new ChessPiece(side, ChessPiece.PieceType.PAWN));
        }
    }
}
