package io.github.mirvmir.identity.domain;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum Role {
    USER,
    ADMIN;

    public String fromRoleToString() {
        return "ROLE_" + name();
    }

    private static final Set<String> VALID = Arrays.stream(Role.values())
            .map(Role::name)
            .collect(Collectors.toSet());

    public static boolean isValidAuthority(String authority) {
        return authority != null
                && authority.startsWith("ROLE_")
                && VALID.contains(authority.substring(5));
    }
}
