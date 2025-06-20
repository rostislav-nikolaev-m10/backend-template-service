package com.m10.demo.component_tests.helpers;

import java.util.List;

public record VaultResponse(
        VaultKeysList data
) {

    public record VaultKeysList(
            List<String> keys
    ) {
    }

}
