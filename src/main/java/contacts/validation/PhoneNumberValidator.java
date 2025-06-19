package contacts.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+)?[0-9 ]+$");

    // Count the number of digits in the phone number
    private boolean isValidDigitCount(String value) {
        int digitCount = value.replaceAll("[^0-9]", "").length();
        return digitCount == 10 || digitCount == 11;
    }

    @Override
    public void initialize(PhoneNumber constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true; // Let @NotBlank handle empty values
        }

        boolean matchesPattern = PHONE_PATTERN.matcher(value).matches();
        boolean hasValidDigitCount = isValidDigitCount(value);

        if (!matchesPattern || !hasValidDigitCount) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Phone number should be either 11 numbers")
                   .addConstraintViolation();
            return false;
        }

        return true;
    }
}
