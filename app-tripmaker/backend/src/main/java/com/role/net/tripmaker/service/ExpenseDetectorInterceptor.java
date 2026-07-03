package com.role.net.tripmaker.service;

import gogather.framework.chat.core.ChatMessageInterceptor;
import gogather.framework.chat.dto.ChatMessageDTO;
import gogather.framework.chat.dto.SendMessageCommand;
import org.springframework.stereotype.Component;

import com.role.net.tripmaker.service.billing.TripBillingService;
import com.role.net.tripmaker.dto.expense.CreateExpenseRequest;
import com.role.net.tripmaker.dto.expense.ContributionRequest;
import com.role.net.tripmaker.entity.Group;
import com.role.net.tripmaker.entity.User;
import com.role.net.tripmaker.repository.TripGroupRepository;
import com.role.net.tripmaker.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class ExpenseDetectorInterceptor implements ChatMessageInterceptor {

    private final TripBillingService tripBillingService;
    private final UserRepository userRepository;
    private final TripGroupRepository groupRepository;

    public ExpenseDetectorInterceptor(
            TripBillingService tripBillingService,
            UserRepository userRepository,
            TripGroupRepository groupRepository) {
        this.tripBillingService = tripBillingService;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

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
            try {
                Group group = groupRepository.findByInviteCode(savedMessage.roomId()).orElseThrow();
                User payer = userRepository.findById(Long.parseLong(savedMessage.senderId())).orElseThrow();

                long amountInCents = (long) (((Double) metadata.get("amount")) * 100);
                String reason = (String) metadata.get("reason");

                List<ContributionRequest> contributions = List.of(
                    new ContributionRequest(payer.getId(), amountInCents)
                );

                List<Long> participantIds = group.getMembers().stream()
                    .map(m -> m.getUser().getId())
                    .toList();

                CreateExpenseRequest request = new CreateExpenseRequest(
                    reason,
                    LocalDate.now(),
                    null, // defaults to OUTROS
                    contributions,
                    participantIds
                );

                tripBillingService.createExpense(group.getId(), request, payer);
                
                System.out.println("====== PLUGIN DE DESPESA DISPARADO ======");
                System.out.println("Despesa gerada com sucesso via Chat!");
                System.out.println("Viagem: " + group.getName());
                System.out.println("Quem pagou: " + payer.getUsername());
                System.out.println("Valor: " + metadata.get("amount"));
                System.out.println("Motivo: " + reason);
                System.out.println("=========================================");
            } catch (Exception e) {
                System.err.println("Erro ao criar despesa via chat: " + e.getMessage());
            }
        }
    }
}
