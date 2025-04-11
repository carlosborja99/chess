package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor turnTeam;
    private boolean gameOver;

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.turnTeam = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turnTeam;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.turnTeam = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null){
            return null;
        }
        Collection<ChessMove> canMove = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> realMoves = new ArrayList<>();
        for(ChessMove movement : canMove){
            ChessPosition endPos = movement.getEndPosition();
            ChessPiece captured = board.getPiece(endPos);
            board.addPiece(startPosition, null);
            board.addPiece(endPos, piece);
            if(!isInCheck(piece.getTeamColor())){
                realMoves.add(movement);
            }
            board.addPiece(startPosition, piece);
            board.addPiece(endPos, captured);
        }
        return realMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece piece = board.getPiece(start);

        if (piece == null){
            throw new InvalidMoveException("No piece at the starting position");
        }
        if(!piece.getTeamColor().equals(turnTeam)){
            throw new InvalidMoveException("It's not your turn");
        }
        Collection<ChessMove> validMoves = validMoves(start);
        if(!validMoves.contains(move)){
            throw new InvalidMoveException("Invalid move");
        }
        ChessPiece reverseInCaseOfCheck = board.getPiece(end);
        board.addPiece(end, piece);
        board.removePiece(start);
        if(piece.getPieceType() == ChessPiece.PieceType.PAWN && (end.getRow() == 1 || end.getRow() == 8)){
            board.addPiece(end, new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        }

        if(isInCheck(turnTeam)){
            board.addPiece(start, piece);
            board.addPiece(end, reverseInCaseOfCheck);
            throw new InvalidMoveException("Move results in Check");
        }
        board.clearEmptySpace();
        turnTeam = (turnTeam == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition whereIsTheKing = null;
        TeamColor opponentTeam = (teamColor == TeamColor.BLACK) ? TeamColor.WHITE : TeamColor.BLACK;
        for(Map.Entry<ChessPosition, ChessPiece> entry : new ArrayList<>(board.getBoard().entrySet())){
            ChessPiece piece = entry.getValue();
            if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor){
                whereIsTheKing = entry.getKey();
                break;
            }
        }
        for(Map.Entry<ChessPosition, ChessPiece> entry : new ArrayList<>(board.getBoard().entrySet())){
            ChessPiece piece = entry.getValue();
            if (piece != null && piece.getTeamColor() == opponentTeam){
                for (ChessMove movement : piece.pieceMoves(board, entry.getKey())){
                    if (movement.getEndPosition().equals(whereIsTheKing)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if(!isInCheck(teamColor)){
            return false;
        }
        for(Map.Entry<ChessPosition, ChessPiece> entry : new ArrayList<>(board.getBoard().entrySet())){
            ChessPiece piece = entry.getValue();
            ChessPosition inTheStart = entry.getKey();
            if(piece != null && piece.getTeamColor() == teamColor){
                Collection<ChessMove> realMoves = validMoves(inTheStart);
                for(ChessMove movement : realMoves){
                    ChessPiece captured = board.getPiece(movement.getEndPosition());
                    board.addPiece(movement.getEndPosition(), piece);
                    board.addPiece(inTheStart, null);
                    if (!isInCheck(teamColor)){
                        board.addPiece(inTheStart, piece);
                        board.addPiece(movement.getEndPosition(), captured);
                        return false;
                    }
                    board.addPiece(inTheStart, piece);
                    board.addPiece(movement.getEndPosition(), captured);
                }
            }
        }

            return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)){
            return false;
        }
        for(Map.Entry<ChessPosition, ChessPiece> entry : new ArrayList<>(board.getBoard().entrySet())){
            ChessPiece piece = entry.getValue();
            ChessPosition inTheStart = entry.getKey();
            if(piece != null && piece.getTeamColor() == teamColor){
                Collection<ChessMove> realMoves = validMoves(inTheStart);
                if(!realMoves.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public boolean isGameOver() {
        return gameOver || isInCheckmate(TeamColor.WHITE) || isInCheckmate(TeamColor.BLACK) ||
                isInStalemate(TeamColor.WHITE) || isInStalemate(TeamColor.BLACK);
    }
}
