package com.university.management;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IntentionalFailureTest {

    @Test
    void intentionallyFailsToDemonstrateGitHubActionsFailure() {
        assertEquals("expected value", "intentional failure demo");
    }
}
