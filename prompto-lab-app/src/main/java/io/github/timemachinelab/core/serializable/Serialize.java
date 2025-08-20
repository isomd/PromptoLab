package io.github.timemachinelab.core.serializable;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface Serialize<T> {
    String serialize(T t) throws JsonProcessingException;

    T deserialize(String str);
}
