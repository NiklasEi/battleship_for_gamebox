package me.nikl.battleship;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GameManager implements Listener{

	private Main plugin;
	private Set<Game> games;
	private AcceptTimer timer;
	
	public GameManager(Main plugin){
		this.plugin = plugin;
		this.timer = new AcceptTimer(this);
		this.games = new HashSet<Game>();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onInvClick(InventoryClickEvent e){
		//e.getWhoClicked().sendMessage("number of games: " + games.size()); // XXX
		if(!isIngame(e.getWhoClicked().getUniqueId()) || e.getClickedInventory() == null || e.getCurrentItem() == null){
			//e.getWhoClicked().sendMessage("ingame: "+isIngame(e.getWhoClicked().getUniqueId())+" || inv == null || currentItem == null"); // XXX
			return;
		}
		e.setCancelled(true);
		if(!e.getAction().equals(InventoryAction.PICKUP_ALL) && !e.getAction().equals(InventoryAction.PICKUP_HALF)){
			return;
		}
		Player player = (Player) e.getWhoClicked();
		Game game = getGame(player.getUniqueId());
		if(!getGame(player.getUniqueId()).isCurrentInventory(e.getClickedInventory())){
			//Bukkit.getConsoleSender().sendMessage("not current inv."); // XXX
			return;
		}
		boolean isFirst = isFirst(player.getUniqueId(), game);
		int slot = e.getSlot();
		
		switch(game.getState()){
		
		case SETTING_SHIP1:
			if(game.getShipsSet(isFirst, true)){
				return;
			}
			if(game.isWater(e.getCurrentItem())){
				//player.sendMessage("clicked water"); // XXX
				game.setShip(slot, isFirst);
			} else if(game.isShip(e.getCurrentItem())){
				//player.sendMessage("clicked something else"); // XXX
				game.setWater(slot, isFirst);
			}
			if(game.shipsSet(1, isFirst)){
				game.lockShips(isFirst);
				game.setShipsSet(isFirst, true);
				if(isFirst){
					game.setFirstCurrentState("&2Battleship   " + "&rWaiting...           &2");
				} else {
					game.setSecondCurrentState("&2Battleship   " + "&rWaiting...           &2");
				}
				game.setState("&2Battleship   " + "&rWaiting...", isFirst, true);
				if(game.getShipsSet(!isFirst, true)){
					game.setState(GameState.SETTING_SHIP2);
					game.showInventory(!isFirst, true);
				}
			}
			game.showInventory(isFirst, true);
			//e.getWhoClicked().sendMessage("number of games: " + games.size()); // XXX
			return;
			
		case SETTING_SHIP2:
			if(game.getShipsSet(isFirst, true)){
				return;
			}
			if(game.isWater(e.getCurrentItem())){
				game.setShip(slot, isFirst);
			} else if(game.isShip(e.getCurrentItem())){
				game.setWater(slot, isFirst);
			}
			if(game.shipsSet(2, isFirst)){
				game.lockShips(isFirst);
				game.setShipsSet(isFirst, true);
				if(isFirst){
					game.setFirstCurrentState("&2Battleship   " + "&rWaiting...           &2");
				} else {
					game.setSecondCurrentState("&2Battleship   " + "&rWaiting...           &2");
				}
				game.setState("&2Battleship   &rWaiting...", isFirst, true);
				if(game.getShipsSet(!isFirst, true)){
					game.setState(GameState.SETTING_SHIP3);
					game.showInventory(!isFirst, true);
				}
			}
			game.showInventory(isFirst, true);
			return;
			
		case SETTING_SHIP3:
			if(game.getShipsSet(isFirst, true)){
				return;
			}
			if(game.isWater(e.getCurrentItem())){
				game.setShip(slot, isFirst);
			} else if(game.isShip(e.getCurrentItem())){
				game.setWater(slot, isFirst);
			}
			if(game.shipsSet(3, isFirst)){
				game.lockShips(isFirst);
				game.setShipsSet(isFirst, true);
				if(isFirst){
					game.setFirstCurrentState("&2Battleship   " + "&rWaiting...           &2");
				} else {
					game.setSecondCurrentState("&2Battleship   " + "&rWaiting...           &2");
				}
				game.setState("&2Battleship   " + "&rWaiting...", isFirst, true);
				if(game.getShipsSet(!isFirst, true)){
					game.setState(GameState.SETTING_SHIP4);
					game.showInventory(!isFirst, true);
				}
			}
			game.showInventory(isFirst, true);
			return;
			
		case SETTING_SHIP4:
			if(game.getShipsSet(isFirst, true)){
				return;
			}
			if(game.isWater(e.getCurrentItem())){
				game.setShip(slot, isFirst);
			} else if(game.isShip(e.getCurrentItem())){
				game.setWater(slot, isFirst);
			}
			if(game.shipsSet(4, isFirst)){
				game.lockShips(isFirst);
				game.setShipsSet(isFirst, true);
				if(isFirst){
					game.setFirstCurrentState("&2Battleship   " + "&rWaiting...           &2");
				} else {
					game.setSecondCurrentState("&2Battleship   " + "&rWaiting...           &2");
				}
				game.setState("&2Battleship   " + "&rWaiting...", isFirst, true);
				if(game.getShipsSet(!isFirst, true)){
					// if this is true, all ships are set and the game can start
					game.unLockShips();
					game.readyToStart();
					game.setState(GameState.FIRST_TURN);
					return;
				}
			}
			game.showInventory(isFirst, true);
			return;
			
		case BUILDING:
			break;
			
		case FINISHED:
			break;
			
		case FIRST_TURN:
			if(!isFirst) return;
			if(game.isCover(e.getCurrentItem())){
				if(!game.fire(isFirst, slot)){
					game.setState(GameState.SECOND_TURN);
				} else if(game.isWon(isFirst)){
					game.setState(GameState.FINISHED);
					game.won(isFirst);
				}
			}
			break;
			
		case SECOND_TURN:
			if(isFirst) return;
			if(game.isCover(e.getCurrentItem())){
				if(!game.fire(isFirst, slot)){
					game.setState(GameState.FIRST_TURN);
				} else if(game.isWon(isFirst)){
					game.setState(GameState.FINISHED);
					game.won(isFirst);
				}
			}
			break;
			
		default:
			break;
			
		}
	}
	

	private Game getGame(UUID uuid) {
		for(Iterator<Game> gameI = games.iterator(); gameI.hasNext();){
			Game game = gameI.next();
			if(isIngame(uuid, game)){
				return game;
			}
		}
		return null;
	}

	@EventHandler
	public void onInvClose(InventoryCloseEvent e){
		//Bukkit.getConsoleSender().sendMessage("called inventoryClose"); // XXX
		if(!isIngame(e.getPlayer().getUniqueId())){
			//Bukkit.getConsoleSender().sendMessage("not ingame"); // XXX
			return;
		}
		if(getGame(e.getPlayer().getUniqueId()).getClosingInv()){
			//Bukkit.getConsoleSender().sendMessage("the game closed this!"); // XXX
			return;			
		}
		if(!getGame(e.getPlayer().getUniqueId()).isCurrentInventory(e.getInventory())){
			return;
		}
		Game game = getGame(e.getPlayer().getUniqueId());
		game.cancelTimer();
		Player first = Bukkit.getPlayer(game.getFirstUUID());
		Player second = Bukkit.getPlayer(game.getSecondUUID());
		boolean winner;
		if(first == null || second == null){
			Bukkit.getConsoleSender().sendMessage(chatColor(Main.prefix + " &4Error on inv. close"));
			Bukkit.getConsoleSender().sendMessage(chatColor(Main.prefix + " &4Deleting game..."));
			removeGame(game);
			if(first != null){
				first.closeInventory();
			}
			if(second != null){
				second.closeInventory();
			}
		}
		if(e.getPlayer().getUniqueId().equals(game.getFirstUUID())){
			winner = false;
		} else {
			winner = true;
		}
		removeGame(getGame(e.getPlayer().getUniqueId()));
		if(!game.getState().equals(GameState.FINISHED)){
			if(!winner){
				if(plugin.getEconEnabled()){
					Main.econ.depositPlayer(second, plugin.getReward());
					second.sendMessage(chatColor(Main.prefix + " &2You won &1" + plugin.getReward() + "&2! "+first.getName()+" gave up."));
				} else {
					second.sendMessage(chatColor(Main.prefix + " &2You won! "+first.getName()+" gave up."));
				}
				first.sendMessage(chatColor(Main.prefix + " &4You gave up"));
				second.closeInventory();
			} else {
				if(plugin.getEconEnabled()){
					Main.econ.depositPlayer(first, plugin.getReward());
					first.sendMessage(chatColor(Main.prefix + " &2You won &1" + plugin.getReward() + "&2! "+first.getName()+" gave up."));
				} else {
					first.sendMessage(chatColor(Main.prefix + " &2You won! "+second.getName()+" gave up."));
				}
				second.sendMessage(chatColor(Main.prefix + " &4You gave up"));
				first.closeInventory();				
			}
		} else {
			if(!winner){
				second.closeInventory();
			} else {
				first.closeInventory();
			}
			
		}
	}
	
	public void startGame(UUID firstUUID, UUID secondUUID){
		games.add(new Game(plugin, firstUUID, secondUUID));
	}
	
	public Main getPlugin(){
		return this.plugin;
	}

	public AcceptTimer getTimer() {
		return timer;
	}
	
	public boolean isIngame(UUID uuid){
		for(Game game : games){
			if(isFirst(uuid, game) || isSecond(uuid, game)){
				return true;
			}
		}
		return false;
	}

	private boolean isIngame(UUID uuid, Game game) {
		if(isFirst(uuid, game) || isSecond(uuid, game)){
			return true;
		}		
		return false;
	}
	
	public boolean isFirst(UUID uuid, Game game){
		if(game.getFirstUUID().equals(uuid)){
			return true;
		}
		return false;
	}
	
	public boolean isSecond(UUID uuid, Game game){
		if(game.getSecondUUID().equals(uuid)){
			return true;
		}
		return false;
	}
	
	public boolean isSecond(UUID uuid){
		for(Game game : games){
			if(isSecond(uuid, game)){
				return true;
			}
		}
		return false;
	}
	
	public boolean isFirst(UUID uuid){
		for(Game game : games){
			if(isFirst(uuid, game)){
				return true;
			}
		}
		return false;
	}

	public void removeGame(Game game) {
		games.remove(game);		
	}
	
	String chatColor(String message){
		return ChatColor.translateAlternateColorCodes('&', message);
	}
}
