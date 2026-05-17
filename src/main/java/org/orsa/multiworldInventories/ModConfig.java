package org.orsa.multiworldInventories;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Config(name = "multiworld-squid")
public class ModConfig implements ConfigData {
    public boolean netherSharesInventory = true;
    public boolean theendSharesInventory = true;
    public Map<String, List<String>> dimensionGamemodes = new HashMap<>();
}
