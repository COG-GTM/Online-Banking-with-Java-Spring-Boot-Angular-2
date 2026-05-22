package com.userFront.identity.api;

public interface UserLookup {
    Long resolveUserId(String username);
    boolean isEnabled(String username);
}
