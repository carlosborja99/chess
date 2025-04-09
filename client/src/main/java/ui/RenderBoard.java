package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.Map;

public class RenderBoard {
    private final ChessBoard board;

    public RenderBoard() {
        this.board = new ChessBoard();
        this.board.resetBoard(); //Resets Board to starting position
    }

    public void render(ChessBoard board, boolean whitePerspective, Map<String, ChessPosition> highlightedMoves) {
        System.out.println(EscapeSequences.ERASE_SCREEN); //Clears terminal
        int startRow = whitePerspective ? 8 : 1;
        int endRow = whitePerspective ? 0 : 9;
        int rowStep = whitePerspective ? -1 : 1;
        char startCol = whitePerspective ? 'a' : 'h';
        char endCol = whitePerspective ? 'i' : '`'; // '`'Is past 'a'
        int colStep = whitePerspective ? 1 : -1;

        System.out.print("   ");
        for (char i = startCol; i != endCol; i += colStep) {
            System.out.print(" " + i + "  ");
        }
        System.out.println();

        for (int row = startRow; row != endRow; row += rowStep) {
            System.out.print(row + "  ");
            for (int col = 1; col <= 8; col++) {
                int adjustedCol = whitePerspective ? col : 9 - col;
                ChessPosition position = new ChessPosition(row, adjustedCol);
                ChessPiece piece = board.getPiece(position);
                String bgColor = (row + adjustedCol) % 2 == 0 ?
                        EscapeSequences.SET_BG_COLOR_DARK_GREY : EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
                String highlightColor = highlightedMoves.containsKey(positionToNotation(position)) ?
                        EscapeSequences.SET_TEXT_COLOR_GREEN : "";
                System.out.print(bgColor + highlightColor);
                System.out.print(getPieceString(piece));
            }
            System.out.print(EscapeSequences.RESET_BG_COLOR + " " + row);
            System.out.println();
        }
        System.out.print("   ");
        for (char i = startCol; i != endCol; i += colStep) {
            System.out.print(" " + i + "  ");
        }
        System.out.println();
    }

    private String getPieceString(ChessPiece piece) {
        if (piece == null) {
            return EscapeSequences.EMPTY;
        }
        ChessGame.TeamColor teamColor = piece.getTeamColor();
        ChessPiece.PieceType pieceType = piece.getPieceType();
        if (teamColor == ChessGame.TeamColor.BLACK) {
            return switch (pieceType) {
                case KING -> EscapeSequences.BLACK_KING;
                case QUEEN -> EscapeSequences.BLACK_QUEEN;
                case KNIGHT -> EscapeSequences.BLACK_KNIGHT;
                case BISHOP -> EscapeSequences.BLACK_BISHOP;
                case ROOK -> EscapeSequences.BLACK_ROOK;
                case PAWN -> EscapeSequences.BLACK_PAWN;

            };
        } else {
            return switch (pieceType){
                case KING -> EscapeSequences.WHITE_KING;
                case QUEEN -> EscapeSequences.WHITE_QUEEN;
                case KNIGHT -> EscapeSequences.WHITE_KNIGHT;
                case BISHOP -> EscapeSequences.WHITE_BISHOP;
                case ROOK -> EscapeSequences.WHITE_ROOK;
                case PAWN -> EscapeSequences.WHITE_PAWN;
            };
        }
    }

    private String positionToNotation(ChessPosition position) {
        char file = (char) ('a' + position.getColumn() - 1);
        int rank = position.getRow();
        return String.valueOf(file) + rank;
    }
}
