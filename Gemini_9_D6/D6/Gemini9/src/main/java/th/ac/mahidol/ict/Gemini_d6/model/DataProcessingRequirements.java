package th.ac.mahidol.ict.Gemini_d6.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Embeddable
public class DataProcessingRequirements {

    @NotNull
    @Enumerated(EnumType.STRING)
    private FileType fileType; // PNG, JPEG, RAW

    @NotNull
    @Enumerated(EnumType.STRING)
    private FileQuality fileQuality; // Low, Fine

    @NotNull
    @Enumerated(EnumType.STRING)
    private ColorType colorType; // Color, B&W

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal contrast; // Decimal

    @DecimalMin("0.0")
    private BigDecimal brightness; // Only for color mode

    @DecimalMin("0.0")
    private BigDecimal saturation; // Only for color mode

    @DecimalMin("0.0")
    private BigDecimal highlights; // Only for B&W mode

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal exposure; // Decimal

    @DecimalMin("0.0")
    private BigDecimal shadows; // Only for B&W mode

    @DecimalMin("0.0")
    private BigDecimal whites; // Only for B&W mode

    @DecimalMin("0.0")
    private BigDecimal blacks; // Only for B&W mode

    @DecimalMin("0.0")
    private BigDecimal luminance; // Only for color mode

    @DecimalMin("0.0")
    private BigDecimal hue; // Only for color mode

    // ไม่ต้องมี getter/setter ที่เขียนเองแล้ว เพราะ @Data จัดการให้
}