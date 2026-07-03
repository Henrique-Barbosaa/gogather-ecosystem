package com.role.net.tripmaker.service;

import gogather.framework.chat.core.ChatMessageInterceptor;
import gogather.framework.chat.dto.ChatMessageDTO;
import gogather.framework.chat.dto.SendMessageCommand;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ExpenseDetectorInterceptor implements ChatMessageInterceptor {

    @Override
    public void preProcess(SendMessageCommand command) {
        String content = command.content();
        if (content != null && (content.toLowerCase().startsWith("/expense") || content.toLowerCase().startsWith("/gasto"))) {
            // Extract the amount and reason
            String[] parts = content.split(" ", 3);
            if (parts.length >= 3) {
                try {
                    double amount = Double.parseDouble(parts[1]);
                    String reason = parts[2];
                    
                    Map<String, Object> metadata = command.metadata();
                    metadata.put("isExpense", true);
                    metadata.put("amount", amount);
                    metadata.put("reason", reason);
                } catch (NumberFormatException e) {
                    // Ignore, not a valid expense command
                }
            }
        }
    }

    @Override
    public void postProcess(ChatMessageDTO savedMessage, Map<String, Object> metadata) {
        if (metadata != null && Boolean.TRUE.equals(metadata.get("isExpense"))) {
            // Here we could trigger a billing service to create the expense!
            System.out.println("====== PLUGIN DE DESPESA DISPARADO ======");
            System.out.println("Viagem: " + savedMessage.roomId());
            System.out.println("Quem pagou: " + savedMessage.senderId());
            System.out.println("Valor: " + metadata.get("amount"));
            System.out.println("Motivo: " + metadata.get("reason"));
            System.out.println("=========================================");
        }
    }
}
