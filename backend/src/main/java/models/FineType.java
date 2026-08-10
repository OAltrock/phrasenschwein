package models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "fine_type")
@NoArgsConstructor
public class FineType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private Name name;

    @Getter
    @AllArgsConstructor
    public enum Name {
        LEICHT(new BigDecimal("0.50")),
        STANDARD(new BigDecimal("1.00")),
        SCHWER(new BigDecimal("2.00"));

        private final BigDecimal defaultAmount;
    }
}