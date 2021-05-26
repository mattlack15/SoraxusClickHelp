package com.soraxus.clickhelp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HelpMessage implements HelpSectionObject {
    @Getter
    @Setter
    private HelpSection parent = null;

    private String message;

    public HelpMessage(String message) {
        this.message = message;
    }

}
