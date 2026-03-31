package com.m10.integration.test.infrastructure.vault;

import java.util.List;

public record VaultResponse(
        VaultKeysList data
) {

    public record VaultKeysList(
            List<String> keys
    ) {
    }

}
