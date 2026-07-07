package com.role.net.tripmaker.dto.group;

import gogather.framework.group.jpa.domain.GroupMember;

public record MemberResponse(
    String id,
    String name,
    String email,
    String role,
    String avatar,
    String pixKey,
    String pixType
) {
    public static MemberResponse from(GroupMember member) {
        String name = member.getUser().getName() != null ? member.getUser().getName() : "Viajante";
        String email = member.getUser().getEmail() != null ? member.getUser().getEmail() : "";
        String roleStr = member.getRole() != null ? member.getRole().name().toLowerCase() : "membro";
        String avatar = "https://ui-avatars.com/api/?name=" + name.replace(" ", "+") + "&background=random&color=fff&size=256&font-size=0.4";
        String pixKeyStr = null;
        String pixTypeStr = null;
        if (member.getUser() instanceof com.role.net.tripmaker.entity.User u && u.getPixInfo() != null) {
            pixKeyStr = u.getPixInfo().getPixKey();
            pixTypeStr = u.getPixInfo().getPixType();
        }
        return new MemberResponse(
            String.valueOf(member.getUser().getId()),
            name,
            email,
            roleStr,
            avatar,
            pixKeyStr,
            pixTypeStr
        );
    }
}
