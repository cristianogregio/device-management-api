package cris.greg.io.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum DeviceState {
    AVAILABLE,
    IN_USE,
    INACTIVE;

    public static List<String> names() {
        return Arrays.stream(values())
                .map(Enum::name)
                .collect(Collectors .toList());
    }
}
