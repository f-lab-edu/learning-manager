package me.chan99k.learningmanager.controller.admin.requests;

import jakarta.validation.constraints.NotNull;
import me.chan99k.learningmanager.member.SystemRole;

public record GrantRoleRequest(@NotNull SystemRole role) {
}
