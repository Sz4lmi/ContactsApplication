package contacts.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.regex.Pattern;

public class PhoneNumberListValidator implements ConstraintValidator<PhoneNumberList, List<String>> {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+)?[0-9 ]+$");

    // Count the number of digits in the phone number
    private boolean isValidDigitCount(String value) {
        int digitCount = value.replaceAll("[^0-9]", "").length();
        return digitCount == 10 || digitCount == 11;
    }

    @Override
    public void initialize(PhoneNumberList constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(List<String> values, ConstraintValidatorContext context) {
        if (values == null || values.isEmpty()) {
            return true; // Let other validators handle empty lists
        }

        boolean isValid = true;

        // Disable default error message
        context.disableDefaultConstraintViolation();

        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            if (value != null && !value.isEmpty()) {
                boolean matchesPattern = PHONE_PATTERN.matcher(value).matches();
                boolean hasValidDigitCount = isValidDigitCount(value);

                if (!matchesPattern || !hasValidDigitCount) {
                    // Add a custom error message for each invalid phone number
                    context.buildConstraintViolationWithTemplate("Phone number should be either 11 numbers")
                           .addPropertyNode("phoneNumbers")
                           .addBeanNode()
                           .inIterable().atIndex(i)
                           .addConstraintViolation();
                    isValid = false;
                }
            }
        }

        return isValid;
    }
}
