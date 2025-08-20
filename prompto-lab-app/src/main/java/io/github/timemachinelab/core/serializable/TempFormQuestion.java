package io.github.timemachinelab.core.serializable;

import io.github.timemachinelab.core.question.*;
import lombok.Builder;
import lombok.Data;

import java.util.*;

@Data
@Builder
public class TempFormQuestion {
    public String id;
    public String question;
    public String type;
    public List<Option> options;
    public String desc;
}