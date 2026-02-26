package com.m10.demo.integration_tests.helpers;

import java.util.List;

public record VaultResponse(
        VaultKeysList data
) {

    public record VaultKeysList(
            List<String> keys
    ) {
    }

}
