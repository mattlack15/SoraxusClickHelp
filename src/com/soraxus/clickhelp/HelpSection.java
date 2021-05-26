package com.soraxus.clickhelp;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HelpSection implements HelpSectionObject {

    @Getter
    private ArrayList<HelpSectionObject> objects = new ArrayList<>();

    @Getter
    @Setter
    private String displayName = "no_display_name";

    @Getter
    private UUID id = UUID.randomUUID();

    @Getter
    @Setter
    private HelpSection parent = null;

    @Getter
    private Map<String, String> data = new HashMap<>();

    public void addObject(HelpSectionObject object) {
        if (!this.objects.contains(object)) {
            this.objects.add(object);
            object.setParent(this);
        }
    }

    public HelpSection findSection(UUID id){
        for(HelpSectionObject object : objects){
            if(!(object instanceof HelpSection))
                continue;
            if(((HelpSection) object).getId().equals(id))
                return (HelpSection) object;
            HelpSection s = ((HelpSection) object).findSection(id);
            if(s != null){
                s.setParent(this);
                return s;
            }
        }
        return null;
    }

}
