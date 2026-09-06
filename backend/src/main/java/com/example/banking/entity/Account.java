package com.example.banking.entity;

import com.example.banking.enums.AccountStatus;
import com.example.banking.enums.AccountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a single bank account belonging to a User.
 *
 * IMPORTANT: 'balance' must only ever be mutated through AccountService
 * business methods (credit/debit) which run inside a transaction and
 * re-check the account status. It is never set directly from a controller.
 *
 * BigDecimal is used (never double/float) because floating point binary
 * representations cannot exactly represent most decimal fractions
 * (e.g. 0.1 + 0.2 != 0.3 in double), which is unacceptable for money.
 */
@Entity
@Table(name = "accounts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_accounts_account_number", columnNames = "account_number")
}, indexes = {
        @Index(name = "idx_accounts_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_accounts_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    /**
     * precision=19, scale=2 -> up to 17 digits before the decimal point and
     * 2 digits after, which comfortably covers real-world currency amounts.
     */
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Optimistic locking guard against lost updates on concurrent balance changes. */
    @Version
    @Column(name = "version")
    private Long version;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.balance == null) {
            this.balance = BigDecimal.ZERO;
        }
        if (this.status == null) {
            this.status = AccountStatus.ACTIVE;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account = (Account) o;
        return id != null && id.equals(account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
