package com.palak.expensemanager.service;

import com.palak.expensemanager.dto.CsvRecord;
import com.palak.expensemanager.entity.ImportAnomaly;
import com.palak.expensemanager.entity.User;
import com.palak.expensemanager.repository.GroupMembershipRepository;
import com.palak.expensemanager.repository.ExpenseGroupRepository;
import com.palak.expensemanager.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AnomalyDetectionService {

    private static final Logger LOGGER = Logger.getLogger(AnomalyDetectionService.class.getName());
    private final UserRepository userRepository;
    private final ExpenseGroupRepository expenseGroupRepository;
    private final GroupMembershipRepository groupMembershipRepository;

    public AnomalyDetectionService(
            UserRepository userRepository,
            ExpenseGroupRepository expenseGroupRepository,
            GroupMembershipRepository groupMembershipRepository) {
        this.userRepository = userRepository;
        this.expenseGroupRepository = expenseGroupRepository;
        this.groupMembershipRepository = groupMembershipRepository;
    }

    public List<ImportAnomaly> detectAnomalies(List<CsvRecord> records) {
        List<ImportAnomaly> anomalies = new ArrayList<>();
        Map<String, Integer> duplicateIndex = new HashMap<>();

        for (CsvRecord record : records) {
            String key = normalize(record.getDescription()) + "|" + record.getAmount() + "|" + normalize(record.getPaidBy()) + "|" + record.getExpenseDate();
            if (duplicateIndex.containsKey(key)) {
                anomalies.add(buildAnomaly(record, "DUPLICATE_EXPENSE", "Duplicate expense detected", "Awaiting approval"));
            } else {
                duplicateIndex.put(key, record.getRowNumber());
            }

            if (record.getPaidBy() == null || record.getPaidBy().isBlank()) {
                anomalies.add(buildAnomaly(record, "MISSING_PAYER", "Missing payer", "Needs review"));
            }

            if (record.getDescription() != null && record.getDescription().toLowerCase().contains("paid") && record.getDescription().toLowerCase().contains("back")) {
                anomalies.add(buildAnomaly(record, "SETTLEMENT_DISGUISED_AS_EXPENSE", "Settlement detected in expense description", "Review before import"));
            }

            if (record.getCurrency() == null || record.getCurrency().isBlank()) {
                anomalies.add(buildAnomaly(record, "MISSING_CURRENCY", "Currency is missing", "Default to INR"));
            }

            if (record.getAmount() == null) {
                anomalies.add(buildAnomaly(record, "INVALID_AMOUNT", "Amount is invalid", "Review data"));
            } else {
                if (record.getAmount().compareTo(BigDecimal.ZERO) == 0) {
                    anomalies.add(buildAnomaly(record, "ZERO_AMOUNT", "Expense amount is zero", "Flag for removal"));
                }
                if (record.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                    anomalies.add(buildAnomaly(record, "NEGATIVE_AMOUNT", "Expense amount is negative", "Flag for review"));
                }
                if (record.getAmount().scale() > 2) {
                    anomalies.add(buildAnomaly(record, "DECIMAL_PRECISION_ISSUE", "Amount has too many decimal places", "Normalize to two decimals"));
                }
            }

            if (record.getExpenseDate() == null) {
                anomalies.add(buildAnomaly(record, "AMBIGUOUS_DATE", "Expense date is ambiguous", "Requires manual review"));
            }

            if (record.getPaidBy() != null && !record.getPaidBy().isBlank()) {
                Optional<User> payer = userRepository.findByNameIgnoreCase(record.getPaidBy());
                if (payer.isEmpty()) {
                    anomalies.add(buildAnomaly(record, "UNKNOWN_PAYER", "Payer is not a known user", "Needs review"));
                }
            }

            if (record.getParticipants() != null && !record.getParticipants().isBlank() && record.getExpenseDate() != null) {
                String[] participants = record.getParticipants().split("[;,\\|]");
                for (String name : participants) {
                    String participantName = name.trim();
                    if (participantName.isBlank()) {
                        continue;
                    }
                    Optional<User> participant = userRepository.findByNameIgnoreCase(participantName);
                    if (participant.isEmpty()) {
                        anomalies.add(buildAnomaly(record, "UNKNOWN_PARTICIPANT", "Participant is not recognized", "Needs review"));
                        continue;
                    }
                    Optional.of(record.getGroup())
                            .filter(g -> !g.isBlank())
                            .flatMap(expenseGroupRepository::findByNameIgnoreCase)
                            .ifPresent(group -> {
                                var memberships = groupMembershipRepository.findByUserAndExpenseGroup(participant.get(), group);
                                boolean isAfterLeaving = memberships.stream()
                                        .anyMatch(m -> m.getLeftAt() != null && m.getLeftAt().isBefore(record.getExpenseDate()));
                                if (isAfterLeaving) {
                                    anomalies.add(buildAnomaly(record, "MEMBER_AFTER_LEAVING", "Participant was included after leaving", "Exclude participant"));
                                }
                            });
                }
            }

            if (record.getPaidBy() != null && !record.getPaidBy().isBlank()) {
                userRepository.findByNameIgnoreCase(record.getPaidBy()).ifPresent(payer -> {
                    if (!payer.getName().equals(record.getPaidBy())) {
                        anomalies.add(buildAnomaly(record, "NAME_CASING_MISMATCH", "Name casing mismatch detected", "Normalize name casing"));
                    }
                });
            }

            if (record.getDescription() != null && record.getDescription().toLowerCase().contains("alias")) {
                anomalies.add(buildAnomaly(record, "ALIAS_NAMES", "Alias names may be used", "Verify participant identities"));
            }
        }

        return anomalies;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private ImportAnomaly buildAnomaly(CsvRecord record, String type, String description, String action) {
        return new ImportAnomaly(null, record.getRowNumber(), type, description, action, false);
    }
}
