package th.ac.mahidol.ict.Gemini_d6.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class SciencePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planId;

    @NotBlank(message = "Plan name cannot be blank")
    private String planName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_username")
    private User creator;

    @NotNull(message = "Funding is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Funding must be non-negative")
    private BigDecimal funding;

    @NotBlank(message = "Objective cannot be blank")
    @Lob
    private String objective;

    @NotNull(message = "Start date/time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @NotNull(message = "End date/time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    @NotNull(message = "Telescope location is required")
    @Enumerated(EnumType.STRING)
    private TelescopeLocation telescopeLocation;

    @NotBlank(message = "Target star system cannot be blank")
    private String targetStarSystem;

    @Embedded
    @Valid
    private DataProcessingRequirements dataProcessingRequirements = new DataProcessingRequirements();

    @Enumerated(EnumType.STRING)
    private SciencePlanStatus status = SciencePlanStatus.CREATED;

    // isDateRangeValid() method remains the same
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !startDate.isAfter(endDate);
    }
    private Integer ocsPlanNo;

    public Integer getOcsPlanNo() {
        return ocsPlanNo;
    }

    public void setOcsPlanNo(Integer ocsPlanNo) {
        this.ocsPlanNo = ocsPlanNo;
    }
}