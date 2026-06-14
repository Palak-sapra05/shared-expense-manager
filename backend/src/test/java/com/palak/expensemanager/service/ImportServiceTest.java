package com.palak.expensemanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.palak.expensemanager.dto.ImportReport;
import com.palak.expensemanager.entity.ExpenseGroup;
import com.palak.expensemanager.entity.GroupMembership;
import com.palak.expensemanager.entity.ImportAnomaly;
import com.palak.expensemanager.entity.User;
import com.palak.expensemanager.repository.ExpenseGroupRepository;
import com.palak.expensemanager.repository.GroupMembershipRepository;
import com.palak.expensemanager.repository.ImportAnomalyRepository;
import com.palak.expensemanager.repository.UserRepository;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

class ImportServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExpenseGroupRepository expenseGroupRepository;

    @Mock
    private GroupMembershipRepository groupMembershipRepository;

    @Mock
    private ImportAnomalyRepository importAnomalyRepository;

    private ImportService importService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        importService = new ImportService(userRepository, expenseGroupRepository, groupMembershipRepository, importAnomalyRepository);
    }

    @Test
    void shouldFlagMemberAfterLeaving() {
        String csv = "Description,Amount,Currency,PaidBy,Group,Participants,ExpenseDate\n"
                + "Cleaning,1000,INR,Aisha,Home,Aisha;Meera,10-04-2026\n";

        User aisha = new User(1L, "Aisha", "aisha@example.com", "pass");
        User meera = new User(2L, "Meera", "meera@example.com", "pass");
        ExpenseGroup group = new ExpenseGroup(1L, "Home");
        GroupMembership membership = new GroupMembership(1L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), meera, group);

        when(userRepository.findByNameIgnoreCase("Aisha")).thenReturn(Optional.of(aisha));
        when(userRepository.findByNameIgnoreCase("Meera")).thenReturn(Optional.of(meera));
        when(expenseGroupRepository.findByNameIgnoreCase("Home")).thenReturn(Optional.of(group));
        when(groupMembershipRepository.findByUserAndExpenseGroup(meera, group)).thenReturn(List.of(membership));
        when(importAnomalyRepository.save(any(ImportAnomaly.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile("csvFile", "expenses.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
        ImportReport report = importService.importCsv(file);

        assertThat(report.getAnomaliesFound()).isGreaterThan(0);
        assertThat(report.getAnomalies()).anyMatch(anomaly -> "MEMBER_AFTER_LEAVING".equals(anomaly.getAnomalyType()));
    }

    @Test
    void shouldNotChargeSamBeforeJoining() {
        String csv = "Description,Amount,Currency,PaidBy,Group,Participants,ExpenseDate\n"
                + "Travel,200,INR,Aisha,Trip,Sam,10-03-2026\n";

        User aisha = new User(1L, "Aisha", "aisha@example.com", "pass");
        when(userRepository.findByNameIgnoreCase("Aisha")).thenReturn(Optional.of(aisha));
        when(userRepository.findByNameIgnoreCase("Sam")).thenReturn(Optional.empty());
        when(importAnomalyRepository.save(any(ImportAnomaly.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile("csvFile", "expenses.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
        ImportReport report = importService.importCsv(file);

        assertThat(report.getAnomaliesFound()).isGreaterThan(0);
        assertThat(report.getAnomalies()).anyMatch(anomaly -> "UNKNOWN_PARTICIPANT".equals(anomaly.getAnomalyType()));
    }

    @Test
    void shouldDetectSettlementDisguisedAsExpense() {
        String csv = "Description,Amount,Currency,PaidBy,Group,Participants,ExpenseDate\n"
                + "Rohan paid Aisha back,5000,INR,Rohan,Friends,Aisha,10-04-2026\n";

        User rohan = new User(1L, "Rohan", "rohan@example.com", "pass");
        when(userRepository.findByNameIgnoreCase("Rohan")).thenReturn(Optional.of(rohan));
        when(userRepository.findByNameIgnoreCase("Aisha")).thenReturn(Optional.of(new User(2L, "Aisha", "aisha@example.com", "pass")));
        when(importAnomalyRepository.save(any(ImportAnomaly.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile("csvFile", "settlement.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
        ImportReport report = importService.importCsv(file);

        assertThat(report.getAnomaliesFound()).isGreaterThan(0);
        assertThat(report.getAnomalies()).anyMatch(anomaly -> "SETTLEMENT_DISGUISED_AS_EXPENSE".equals(anomaly.getAnomalyType()));
    }

    @Test
    void shouldDetectUsdExpense() {
        String csv = "Description,Amount,Currency,PaidBy,Group,Participants,ExpenseDate\n"
                + "Trip,540,USD,Aisha,Travel,Aisha,10-04-2026\n";

        User aisha = new User(1L, "Aisha", "aisha@example.com", "pass");
        when(userRepository.findByNameIgnoreCase("Aisha")).thenReturn(Optional.of(aisha));
        when(importAnomalyRepository.save(any(ImportAnomaly.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile("csvFile", "usd.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
        ImportReport report = importService.importCsv(file);

        assertThat(report.getTotalRows()).isEqualTo(1);
        assertThat(report.getAnomaliesFound()).isEqualTo(0);
    }
}
