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
        return "ChessPiece{" +
                "color=" + color +
                ", type=" + type +
                '}';
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
                addRookMove(board, myPosition, validMoves);
                addBishopMove(board, myPosition, validMoves);
                break;
            case ROOK:
                addRookMove(board, myPosition, validMoves);
                break;
            case BISHOP:
                addBishopMove(board, myPosition, validMoves);
                break;
            case KNIGHT:
                addKnightMove(board, myPosition, validMoves);
                break;
            case PAWN:
                addPawnMove(board, myPosition, validMoves);
                break;

        }
        return validMoves;
    }
    private boolean isValidPosition(ChessPosition position) {
        return position.getRow() >= 1 && position.getRow() <= 8 &&
                position.getColumn() >= 1 && position.getColumn() <= 8;
    }

    private void addKingMove(ChessBoard board, ChessPosition myPosition, List<ChessMove> validMoves){
        int[] deltas = {-1, 0, 1};
        for (int rowDelta : deltas){
            for (int colDelta : deltas){
                if (rowDelta == 0 && colDelta == 0) continue;
                ChessPosition newPosition = new ChessPosition(myPosition.getRow() + rowDelta, myPosition.getColumn() + colDelta);
                if (isValidPosition(newPosition)) {
                    ChessPiece pieceAtNewPosition = board.getPiece(newPosition);
                    if (pieceAtNewPosition == null || pieceAtNewPosition.getTeamColor() != this.color){
                        validMoves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
        }
    }
    private void addRookMove(ChessBoard board, ChessPosition myPosition, List<ChessMove> validMoves){
        int[] rowDeltas = {-1, 1};
        int[] colDeltas = {-1, 1};
        for (int rowDelta : rowDeltas){
            addMovesInDirection(board, myPosition, validMoves, rowDelta, 0);
        }
        for (int colDelta : colDeltas){
            addMovesInDirection(board, myPosition, validMoves, 0, colDelta);
        }
    }
    private void addBishopMove(ChessBoard board, ChessPosition myPosition, List<ChessMove> validMoves){
        int[] deltas = {-1, 1};
        for (int rowDelta : deltas){
            for (int colDelta : deltas){
                addMovesInDirection(board, myPosition, validMoves, rowDelta, colDelta);
            }
        }
    }
    private void addKnightMove(ChessBoard board, ChessPosition myPosition, List<ChessMove> validMoves) {
        int[][] knightMoves = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}};
        for (int[] movement : knightMoves) {
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + movement[0], myPosition.getColumn() + movement[1]);
            if(isValidPosition(newPosition)){
                ChessPiece pieceAtNewPosition = board.getPiece(newPosition);
                if (pieceAtNewPosition == null || pieceAtNewPosition.getTeamColor() != this.color){
                    validMoves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }
    }
    private void addPawnMove(ChessBoard board, ChessPosition myPosition, List<ChessMove> validMoves) {
        int direction = (this.color == ChessGame.TeamColor.BLACK) ? -1 : 1;
        ChessPosition normalMovement = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn());
        if(isValidPosition(normalMovement) && board.getPiece(normalMovement) == null){
            if (normalMovement.getRow() == (this.color == ChessGame.TeamColor.WHITE ? 8 : 1)){
                validMoves.add(new ChessMove(myPosition, normalMovement, PieceType.QUEEN));
                validMoves.add(new ChessMove(myPosition, normalMovement, PieceType.BISHOP));
                validMoves.add(new ChessMove(myPosition, normalMovement, PieceType.ROOK));
                validMoves.add(new ChessMove(myPosition, normalMovement, PieceType.KNIGHT));
            }
            else{validMoves.add(new ChessMove(myPosition, normalMovement, null));}
        }
        if((this.color == ChessGame.TeamColor.BLACK && myPosition.getRow() == 7) || (this.color == ChessGame.TeamColor.WHITE && myPosition.getRow() == 2)){
            ChessPosition firstMovement = new ChessPosition(myPosition.getRow() + 2 * direction, myPosition.getColumn());
            if (board.getPiece(firstMovement) == null && board.getPiece(normalMovement) == null){
                validMoves.add(new ChessMove(myPosition, firstMovement, null));
            }
        }
        int[] captureDeltas = {-1, 1};
        for (int delta : captureDeltas) {
            ChessPosition capturePosition = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn() + delta);
            if (isValidPosition(capturePosition)) {
                ChessPiece pieceAtNewPosition = board.getPiece(capturePosition);
                if (pieceAtNewPosition != null && pieceAtNewPosition.getTeamColor() != this.color){
                    if (normalMovement.getRow() == (this.color == ChessGame.TeamColor.WHITE ? 8 : 1)){
                        validMoves.add(new ChessMove(myPosition, capturePosition, PieceType.QUEEN));
                        validMoves.add(new ChessMove(myPosition, capturePosition, PieceType.BISHOP));
                        validMoves.add(new ChessMove(myPosition, capturePosition, PieceType.ROOK));
                        validMoves.add(new ChessMove(myPosition, capturePosition, PieceType.KNIGHT));
                    }else{validMoves.add(new ChessMove(myPosition, capturePosition, null));}
                }
            }
        }

    }
    private void addMovesInDirection(ChessBoard board, ChessPosition myPosition, List<ChessMove> validMoves, int rowDelta, int colDelta){
        for (int i = 1; i <= 8; i++){
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + rowDelta * i, myPosition.getColumn() + colDelta * i);
            if (isValidPosition(newPosition)) {
                ChessPiece pieceAtNewPosition = board.getPiece(newPosition);
                if (pieceAtNewPosition == null){
                    validMoves.add(new ChessMove(myPosition, newPosition, null));
                }else if(pieceAtNewPosition.getTeamColor() != this.color){
                    validMoves.add(new ChessMove(myPosition, newPosition, null));
                    break;
                }else{
                    break;
                }
            } else{
                break;
            }
        }
    }
}
