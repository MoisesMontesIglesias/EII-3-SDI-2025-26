package com.uniovi.sdi.gradeManager.validators;

import com.uniovi.sdi.gradeManager.entities.Mark;
import com.uniovi.sdi.gradeManager.services.MarksService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class AddMarkFormValidator implements Validator {
    private final MarksService marksService;

    public AddMarkFormValidator(MarksService marksService) {
        this.marksService = marksService;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return Mark.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Mark mark = (Mark) target;

        if (mark.getScore() < 0 || mark.getScore() > 10) {
            errors.rejectValue("score", "Error.add.score.range");
        }

        if (mark.getDescription().length() < 20){
            errors.rejectValue("description", "Error.add.description.length");
        }
    }
}
