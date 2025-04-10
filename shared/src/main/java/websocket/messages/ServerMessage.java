package websocket.messages;

import chess.ChessGame;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * 
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    ServerMessageType serverMessageType;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage)) {
            return false;
        }
        ServerMessage that = (ServerMessage) o;
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }

    class LoadGameMessage extends ServerMessage {
        private ChessGame game;
        public  LoadGameMessage() {
            super(ServerMessageType.LOAD_GAME);
        }
        public ChessGame getGame() {
            return game;
        }
    }
    class ErrorMessage extends ServerMessage {
        private String errorMessage;
        public ErrorMessage() {
            super(ServerMessageType.ERROR);
        }
        public String getErrorMessage() {
            return errorMessage;
        }
    }
    class NotificationMessage extends ServerMessage {
        private String message;
        public NotificationMessage() {
            super(ServerMessageType.NOTIFICATION);
        }
        public String getMessage() {
            return message;
        }
    }
}
