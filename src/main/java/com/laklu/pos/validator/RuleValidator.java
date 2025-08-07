package com.laklu.pos.validator;

import com.laklu.pos.exceptions.RuleNotValidException;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class RuleValidator {
    public static void validate(Rule businessRule) {
        if (businessRule.isValid()) {
            return;
        }
        HashMap<String, String> errors = new HashMap<>();
        errors.put(businessRule.field(), businessRule.getMessage());
        throw new RuleNotValidException(errors);
    }

    public static <T> T getValidatedValue(ValuableValidationRule<T> rule) {
        validate(rule);
        return rule.getValue();
    }
}

