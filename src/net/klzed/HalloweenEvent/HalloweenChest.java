package net.klzed.HalloweenEvent;

import org.bukkit.Location;

public class HalloweenChest {
	
	private Location location;
	private int itemId;
	private int dataValue;
	
	public HalloweenChest(Location l, int i, int d, String n) {
		location = l;
		itemId = i;
		dataValue = d;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getDataValue() {
		return dataValue;
	}

	public void setDataValue(int dataValue) {
		this.dataValue = dataValue;
	}
	
	
}
