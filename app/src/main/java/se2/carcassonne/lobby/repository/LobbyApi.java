package se2.carcassonne.lobby.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import se2.carcassonne.helper.network.WebSocketClient;
import se2.carcassonne.lobby.model.Lobby;
import se2.carcassonne.player.model.Player;

public class LobbyApi {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final WebSocketClient webSocketClient;


    public LobbyApi(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

//    public void requestLobbies(Lobby lobby){
//        try {
//            webSocketClient.subscribeToListLobbiesTopic("/app/list-lobby", objectMapper.writeValueAsString(lobby));
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public void createLobby(Lobby lobby, Player currentPlayer){
        try {
            String message = objectMapper.writeValueAsString(lobby) + "|" + objectMapper.writeValueAsString(currentPlayer);
            webSocketClient.sendMessage("/app/create-lobby", message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
