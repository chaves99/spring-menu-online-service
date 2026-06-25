package com.menuonline.payloads;

import com.menuonline.entity.Customization;

public record CustomizationResponse(Long id, String name, String mainColor, String secondaryColor, String font,
        String theme, boolean builtin, boolean active) {
    public static CustomizationResponse from(Customization c) {
        return new CustomizationResponse(c.getId(), c.getName(), c.getMainColor(), c.getSecondaryColor(), c.getFont(),
                c.getThemeType().name(), c.getBuiltin(), c.getActive());
    }
}
