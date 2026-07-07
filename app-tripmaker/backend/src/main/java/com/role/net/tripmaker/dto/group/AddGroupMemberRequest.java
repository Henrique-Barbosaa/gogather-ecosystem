package com.role.net.tripmaker.dto.group;

public record AddGroupMemberRequest(
    String email,
    Long userId
) {}
