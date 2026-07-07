package com.role.net.tripmaker.dto.group;

import com.role.net.tripmaker.dto.chat.ChatMessageResponse;
import com.role.net.tripmaker.dto.expense.TripDebtResponse;
import com.role.net.tripmaker.dto.expense.TripExpenseResponse;
import com.role.net.tripmaker.dto.itinerary.ItineraryResponse;
import com.role.net.tripmaker.entity.ChatMessage;
import com.role.net.tripmaker.entity.Group;
import java.time.LocalDate;
import java.util.List;

public record GroupResponse(
    Long id,
    String inviteCode,
    String name,
    String description,
    String destination,
    LocalDate startDate,
    LocalDate endDate,
    Integer maxTravelers,
    String coverUrl,
    List<MemberResponse> members,
    List<ChatMessageResponse> messages,
    List<TripExpenseResponse> expenses,
    List<TripDebtResponse> debts,
    List<ItineraryResponse> roadmap
) {
    public static GroupResponse from(Group group) {
        return from(group, List.of());
    }

    public static GroupResponse from(Group group, List<ChatMessage> messages) {
        List<MemberResponse> membersList = group.getMembers() != null
            ? group.getMembers().stream().map(MemberResponse::from).toList()
            : List.of();
        
        List<ChatMessageResponse> messagesList = messages != null
            ? messages.stream().map(ChatMessageResponse::from).toList()
            : List.of();

        List<TripExpenseResponse> expensesList = group.getExpenses() != null
            ? group.getExpenses().stream().map(TripExpenseResponse::from).toList()
            : List.of();

        List<TripDebtResponse> debtsList = group.getExpenses() != null
            ? group.getExpenses().stream()
                .flatMap(e -> e.getDebts() != null ? e.getDebts().stream() : java.util.stream.Stream.empty())
                .map(TripDebtResponse::from)
                .toList()
            : List.of();

        List<ItineraryResponse> roadmapList = group.getItineraryEvents() != null
            ? group.getItineraryEvents().stream().map(ItineraryResponse::from).toList()
            : List.of();

        return new GroupResponse(
            group.getId(),
            group.getInviteCode(),
            group.getName(),
            group.getDescription(),
            group.getDestination(),
            group.getStartDate(),
            group.getEndDate(),
            group.getMaxTravelers(),
            group.getCoverUrl(),
            membersList,
            messagesList,
            expensesList,
            debtsList,
            roadmapList
        );
    }
}
