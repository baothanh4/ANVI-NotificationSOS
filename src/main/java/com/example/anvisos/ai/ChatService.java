package com.example.anvisos.ai;

import com.example.anvisos.model.entity.RescueConnection;
import com.example.anvisos.model.entity.User;
import com.example.anvisos.model.repository.RescueConnectionRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final RescueConnectionRepository rescueConnectionRepository;

    @Value("${anvi.ai.chatbot.system-prompt}")
    private String systemPrompt;

    public ChatService(ChatClient.Builder builder, RescueConnectionRepository rescueConnectionRepository) {
        this.chatClient = builder.build();
        this.rescueConnectionRepository = rescueConnectionRepository;
    }

    private String getUserSosNetworkSummary(User currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            return "Bạn chưa có thông tin người dùng nên không thể tra cứu mạng lưới cứu hộ.";
        }

        List<RescueConnection> connectionsAsRequester = rescueConnectionRepository
                .findByRequesterIdAndStatus(currentUser.getId(), "ACCEPTED");
        List<RescueConnection> connectionsAsTarget = rescueConnectionRepository
                .findByTargetIdAndStatus(currentUser.getId(), "ACCEPTED");

        if (connectionsAsRequester.isEmpty() && connectionsAsTarget.isEmpty()) {
            return "Bạn chưa có ai trong mạng lưới cứu hộ.";
        }

        StringBuilder summary = new StringBuilder("Danh sách những người trong mạng lưới cứu hộ của bạn:\n");

        for (RescueConnection conn : connectionsAsRequester) {
            summary.append("- ").append(conn.getTarget().getFullName())
                    .append(" (Mối quan hệ: ")
                    .append(conn.getRelationship() != null ? conn.getRelationship() : "Người quen")
                    .append(")\n");
        }
        for (RescueConnection conn : connectionsAsTarget) {
            summary.append("- ").append(conn.getRequester().getFullName())
                    .append(" (Mối quan hệ: ")
                    .append(conn.getRelationship() != null ? conn.getRelationship() : "Người quen")
                    .append(")\n");
        }

        return summary.toString();
    }

    public String generateResponse(String userMessage, User currentUser) {
        String personalizedPrompt = systemPrompt + "\nBạn đang trò chuyện với người dùng: " + (currentUser != null ? currentUser.getFullName() : "Khách") + 
                                     ". ID người dùng là: " + (currentUser != null ? currentUser.getId() : "null") + "." +
                                     "\nThông tin mạng lưới cứu hộ hiện tại:\n" + getUserSosNetworkSummary(currentUser);

        return chatClient.prompt()
                .system(personalizedPrompt)
                .user(userMessage)
                .call()
                .content();
    }
}





