package com.soraxus.clickhelp;

import lombok.Getter;
import lombok.Setter;

public interface HelpSectionObject {
    HelpSection getParent();
    void setParent(HelpSection section);
}
