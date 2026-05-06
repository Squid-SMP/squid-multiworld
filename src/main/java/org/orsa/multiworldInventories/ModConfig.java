package org.orsa.multiworldInventories;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "multiworld-inventories")
public class ModConfig implements ConfigData {
    public boolean netherSharesInventory = true;
    public boolean theendSharesInventory = true;
}
