package io.github.timemachinelab.core.qatree;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class FormQA extends QA {

    private String question;

    private Map<String, String> answerMap;

    public FormQA() {
        super(QAType.FORM);
        answerMap = new HashMap<>();
    }
}
