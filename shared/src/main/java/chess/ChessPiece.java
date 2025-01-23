package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor color;
    private PieceType type;
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
    this.color = pieceColor;
    this.type = type;
    }

//    @Override
//    public int hashCode() {
//        return super.hashCode();
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        boolean result = super.equals(obj);
//        if (result) {
//            ChessPiece j = (ChessPiece) obj;
//            return (this.color == j.color && this.type == j.type);
//        } else {
//            return false;
//        }
//    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return color == that.color && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> validMoves = new ArrayList<>();

        switch (type){
            case KING:
                addKingMove(board, myPosition, validMoves);
                break;
            case QUEEN:
                addKingMove(board, myPosition, validMoves);
                break;
            case ROOK:
                addKingMove(board, myPosition, validMoves);
                break;
            case BISHOP:
                addKingMove(board, myPosition, validMoves);
                break;
            case KNIGHT:
                addKingMove(board, myPosition, validMoves);
                break;
            case PAWN:
                addKingMove(board, myPosition, validMoves);
                break;

        }
        return validMoves;
    }
    private void addKingMove(ChessBoard board, ChessPosition myPosition, List<ChessMove> validMoves){
        int[] deltas = {1, 0, 1};
        for (int rowDelta : deltas){
            for (int colDelta : deltas){
                if (rowDelta == 0 && colDelta == 0) continue;
                ChessPosition newPosition = new ChessPosition(myPosition.getRow() + rowDelta, myPosition.getColumn() + colDelta);
                if ((myPosition.getRow() + rowDelta >= 1 || myPosition.getRow() + rowDelta <= 8) && (myPosition.getColumn() + colDelta >= 1 || myPosition.getColumn() + colDelta <= 8)){
                    ChessPiece pieceAtNewPosition = board.getPiece(newPosition);
                    if (pieceAtNewPosition == null || pieceAtNewPosition.getTeamColor() != this.color){
                        validMoves.add(new ChessMove(myPosition, newPosition, null));
                    }

                }
            }
        }
    }
}
