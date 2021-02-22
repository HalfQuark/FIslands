package me.halfquark.fislands.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.countercraft.movecraft.warfare.assault.Assault;

public class AssaultWinEvent extends Event {
	
	private Assault assault;
	private static final HandlerList HANDLERS_LIST = new HandlerList();
	
	public AssaultWinEvent(Assault assault) {
		this.assault = assault;
	}
	
	@Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
	
	public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
	
	public Assault getAssault() {
		return assault;
	}
	
}
