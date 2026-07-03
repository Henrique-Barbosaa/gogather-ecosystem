package com.role.net.roomiesapp.service;

import com.role.net.roomiesapp.dto.billing.*;
import com.role.net.roomiesapp.entity.*;
import com.role.net.roomiesapp.exception.ResourceNotFoundException;
import com.role.net.roomiesapp.repository.*;
import gogather.framework.billing.dto.DebtStatus;
import gogather.framework.billing.orchestrator.BillingOrchestrator;
import gogather.framework.billing.pix.PixCodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoomiesBillingService {

    private final HouseBillRepository billRepository;
    private final HouseDebtRepository debtRepository;
    private final RoomiesGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final PixInfoRepository pixInfoRepository;
    private final UserPixService userPixService;
    private final BillingOrchestrator billingOrchestrator;
    private final PixCodeGenerator pixCodeGenerator;

    public RoomiesBillingService(
            HouseBillRepository billRepository,
            HouseDebtRepository debtRepository,
            RoomiesGroupRepository groupRepository,
            UserRepository userRepository,
            PixInfoRepository pixInfoRepository,
            UserPixService userPixService,
            BillingOrchestrator billingOrchestrator,
            PixCodeGenerator pixCodeGenerator) {
        this.billRepository = billRepository;
        this.debtRepository = debtRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.pixInfoRepository = pixInfoRepository;
        this.userPixService = userPixService;
        this.billingOrchestrator = billingOrchestrator;
        this.pixCodeGenerator = pixCodeGenerator;
    }

    @Transactional
    public BillResponse createBill(UUID groupExternalId, CreateBillRequest request, User creator) {
        Group group = groupRepository.findByExternalId(groupExternalId)
                .orElseGet(() -> groupRepository.findByInviteCode(groupExternalId.toString())
                        .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado: " + groupExternalId)));

        HouseBill bill = new HouseBill();
        bill.setGroup(group);
        bill.setTitle(request.title());
        bill.setDescription(request.description());
        bill.setTotalCents(request.totalCents());
        bill.setBillType(request.billType());
        bill.setRecurrenceInterval(request.recurrenceInterval() != null ? request.recurrenceInterval() : RecurrenceInterval.NONE);
        bill.setCustomIntervalDays(request.customIntervalDays());
        bill.setDueDate(request.dueDate());

        if (request.contributorId() != null) {
            User contributor = userRepository.findByExternalId(request.contributorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contribuidor não encontrado: " + request.contributorId()));
            // REGRA CRÍTICA: Impedir de definir um contribuidor que não tenha chave Pix cadastrada!
            userPixService.validateUserHasPixKey(contributor);
            bill.setContributor(contributor);
        } else if (request.billType() == BillType.NORMAL) {
            // Em contas normais, se não enviou contributorId no payload, assumimos que quem pagou foi o creator!
            userPixService.validateUserHasPixKey(creator);
            bill.setContributor(creator);
        }
        // Em contas recorrentes, contributor pode ser null (opcional).

        if (request.participantIds() != null && !request.participantIds().isEmpty()) {
            List<User> participants = new ArrayList<>();
            for (UUID uid : request.participantIds()) {
                User u = userRepository.findByExternalId(uid)
                        .orElseThrow(() -> new ResourceNotFoundException("Participante não encontrado: " + uid));
                participants.add(u);
            }
            bill.setParticipants(participants);
        }

        bill = billRepository.save(bill);

        // Chama o Orquestrador de Billing do Framework para realizar o rateio (Princípio de Hollywood)
        billingOrchestrator.settleExpense(bill.getExternalId().toString());

        return BillResponse.fromEntity(bill);
    }

    @Transactional
    public BillResponse updateBill(UUID billExternalId, UpdateBillRequest request) {
        HouseBill bill = billRepository.findByExternalId(billExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + billExternalId));

        if (request.title() != null) bill.setTitle(request.title());
        if (request.description() != null) bill.setDescription(request.description());
        if (request.totalCents() != null) bill.setTotalCents(request.totalCents());
        if (request.billType() != null) bill.setBillType(request.billType());
        if (request.recurrenceInterval() != null) bill.setRecurrenceInterval(request.recurrenceInterval());
        if (request.customIntervalDays() != null) bill.setCustomIntervalDays(request.customIntervalDays());
        if (request.dueDate() != null) bill.setDueDate(request.dueDate());

        if (request.contributorId() != null) {
            User contributor = userRepository.findByExternalId(request.contributorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contribuidor não encontrado: " + request.contributorId()));
            userPixService.validateUserHasPixKey(contributor);
            bill.setContributor(contributor);
        }

        if (request.participantIds() != null) {
            List<User> participants = new ArrayList<>();
            for (UUID uid : request.participantIds()) {
                User u = userRepository.findByExternalId(uid)
                        .orElseThrow(() -> new ResourceNotFoundException("Participante não encontrado: " + uid));
                participants.add(u);
            }
            bill.setParticipants(participants);
        }

        bill = billRepository.save(bill);
        billingOrchestrator.settleExpense(bill.getExternalId().toString());
        return BillResponse.fromEntity(bill);
    }

    @Transactional(readOnly = true)
    public List<BillResponse> getBillsByGroup(UUID groupExternalId) {
        Group group = groupRepository.findByExternalId(groupExternalId)
                .orElseGet(() -> groupRepository.findByInviteCode(groupExternalId.toString())
                        .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado: " + groupExternalId)));
        return billRepository.findByGroup(group).stream()
                .map(BillResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BillResponse getBillDetails(UUID billExternalId) {
        HouseBill bill = billRepository.findByExternalId(billExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + billExternalId));
        return BillResponse.fromEntity(bill);
    }

    @Transactional(readOnly = true)
    public List<DebtResponse> getDebtsByBill(UUID billExternalId) {
        HouseBill bill = billRepository.findByExternalId(billExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + billExternalId));
        return debtRepository.findByBill(bill).stream()
                .map(DebtResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DebtResponse> getMyDebts(User user) {
        return debtRepository.findByDebtor(user).stream()
                .map(DebtResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DebtResponse> getMyCredits(User user) {
        return debtRepository.findByCreditor(user).stream()
                .map(DebtResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public PixCodeResponse generatePixForDebt(UUID debtExternalId) {
        HouseDebt debt = debtRepository.findByExternalId(debtExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Dívida não encontrada: " + debtExternalId));

        if (debt.getCreditor() == null) {
            throw new IllegalStateException("Esta conta (recorrente sem pagador definido) ainda não possui um credor/contribuidor atribuído para recebimento de Pix.");
        }

        PixInfo pixInfo = pixInfoRepository.findByUser(debt.getCreditor())
                .orElseThrow(() -> new IllegalStateException("O credor " + debt.getCreditor().getDisplayName() + " não possui chave Pix cadastrada."));

        String pixCode = pixCodeGenerator.gerarPixCopiaECola(pixInfo, debt.getAmountInCents());
        debt.setPixCodeCache(pixCode);
        debtRepository.save(debt);

        return new PixCodeResponse(
                debt.getExternalId(),
                pixCode,
                pixInfo.getMerchantName(),
                pixInfo.getMerchantCity(),
                debt.getAmountInCents()
        );
    }

    @Transactional
    public DebtResponse updateDebtStatus(UUID debtExternalId, DebtStatus newStatus) {
        HouseDebt debt = debtRepository.findByExternalId(debtExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Dívida não encontrada: " + debtExternalId));
        debt.setStatus(newStatus);
        debt = debtRepository.save(debt);
        return DebtResponse.fromEntity(debt);
    }
}
