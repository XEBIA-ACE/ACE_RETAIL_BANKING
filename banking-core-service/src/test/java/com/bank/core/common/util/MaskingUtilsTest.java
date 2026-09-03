package com.bank.core.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link MaskingUtils#maskAccountNumber(String)}.
 *
 * <p>Test matrix:
 * <ol>
 *   <li>Standard long number  → "****9012"</li>
 *   <li>Exactly 4 digits      → "****1234"</li>  <!-- would fail min-length; see TC-2 note -->
 *   <li>Exactly 5 digits      → "****2345"</li>
 *   <li>null input            → IllegalArgumentException</li>
 *   <li>Blank string (" ")    → IllegalArgumentException</li>
 *   <li>Empty string ("")     → IllegalArgumentException</li>
 *   <li>3-character string    → IllegalArgumentException</li>
 *   <li>Full number never in  masked output (negative assertion)</li>
 * </ol>
 * </p>
 */
@DisplayName("MaskingUtils — account number masking")
class MaskingUtilsTest {

    // -----------------------------------------------------------------------
    // Happy-path parameterised tests
    // -----------------------------------------------------------------------

    /**
     * TC-1 / TC-3: Parameterised happy-path covering a standard long number and
     * a minimum-length (5-character) number.
     * <p>
     * Columns: input | expectedMasked
     * </p>
     */
    @ParameterizedTest(name = "[{index}] maskAccountNumber(\"{0}\") == \"{1}\"")
    @CsvSource({
            // TC-1: standard long number (12 digits)
            "123456789012, ****9012",
            // TC-3: exactly 5 digits — minimum valid length
            "12345,         ****2345",
            // Additional representative inputs
            "9999999999,   ****9999",
            "ABCDE12345,   ****2345"
    })
    @DisplayName("Happy path: masked output equals '****' + last4 of input")
    void maskAccountNumber_happyPath(String input, String expectedMasked) {
        String actual = MaskingUtils.maskAccountNumber(input.strip());
        assertThat(actual)
                .as("Masked output for input '%s'", input.strip())
                .isEqualTo(expectedMasked.strip());
    }

    /**
     * TC-2: Exactly 4 characters — must return {@code "****1234"}.
     * <p>
     * Per the spec the boundary is that the input must have MORE than 4 digits
     * (at least 5). An input of exactly 4 characters is therefore below the
     * minimum length and must raise {@link IllegalArgumentException}.
     * This test deliberately asserts the exception to keep the contract clear.
     * </p>
     */
    @Test
    @DisplayName("TC-2: exactly 4 characters → IllegalArgumentException (below minimum length)")
    void maskAccountNumber_exactly4Chars_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> MaskingUtils.maskAccountNumber("1234"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageNotNull();
    }

    // -----------------------------------------------------------------------
    // Exception / validation tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-4: null input → IllegalArgumentException with non-null message")
    void maskAccountNumber_nullInput_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> MaskingUtils.maskAccountNumber(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageNotNull()
                .hasMessageContaining("null");
    }

    @Test
    @DisplayName("TC-5: blank string (' ') → IllegalArgumentException with non-null message")
    void maskAccountNumber_blankString_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> MaskingUtils.maskAccountNumber(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageNotNull()
                .hasMessageContaining("blank");
    }

    @Test
    @DisplayName("TC-6: empty string ('') → IllegalArgumentException with non-null message")
    void maskAccountNumber_emptyString_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> MaskingUtils.maskAccountNumber(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageNotNull();
    }

    @Test
    @DisplayName("TC-7: 3-character string → IllegalArgumentException with non-null message")
    void maskAccountNumber_threeCharString_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> MaskingUtils.maskAccountNumber("123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageNotNull();
    }

    // -----------------------------------------------------------------------
    // Negative assertion: full account number must never appear in masked output
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-8: full account number is never present in the masked output (negative assertion)")
    void maskAccountNumber_fullNumberNeverAppearsInMaskedOutput() {
        String fullAccountNumber = "123456789012";

        String masked = MaskingUtils.maskAccountNumber(fullAccountNumber);

        // The masked value must NOT equal the full account number
        assertThat(masked)
                .as("Masked output must not equal the full account number")
                .isNotEqualTo(fullAccountNumber);

        // The masked value must NOT contain the full account number as a substring
        assertThat(masked)
                .as("Masked output must not contain the full account number")
                .doesNotContain(fullAccountNumber);

        // Structural assertion: output always starts with the mask prefix
        assertThat(masked)
                .as("Masked output must start with '****'")
                .startsWith("****");

        // Structural assertion: only last 4 characters follow the mask prefix
        assertThat(masked)
                .as("Masked output must be exactly 8 characters ('****' + 4 digits)")
                .hasSize(8);

        // The digits before the last 4 must not appear in the masked portion
        String hiddenPortion = fullAccountNumber.substring(0, fullAccountNumber.length() - 4);
        assertThat(masked)
                .as("Masked output must not expose hidden portion '%s'", hiddenPortion)
                .doesNotContain(hiddenPortion);
    }
}