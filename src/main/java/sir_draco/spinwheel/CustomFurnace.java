package sir_draco.spinwheel;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

public class CustomFurnace {

    private final Location location;
    private final int speed;
    private final FurnaceInventory inventory;

    private int ticksPassed;
    private int fuel;

    public CustomFurnace(Location location, int speed) {
        this.location = location;
        this.speed = speed;
        String name;
        if (speed == 1) name = "2x Speed Furnace";
        else if (speed == 2) name = "3x Speed Furnace";
        else if (speed == 3) name = "4x Speed Furnace";
        else name = "2 Tick Furnace";
        inventory = (FurnaceInventory) Bukkit.createInventory(null, InventoryType.FURNACE, name);
    }

    public void tickFurnace() {
        if (getSmelting() == null) return;
        if (fuel <= 0 && getFuel() == null) return;
        if (fuel != 0) {
            if (speed == 1) fuel -= 1;
            else if (speed == 2) fuel -= 2;
            else if (speed == 3) fuel -= 4;
            else fuel -= 50;
        }
        if (!CustomFurnaceChecker.getFurnaceRecipes().containsKey(getSmelting().getType())) return;
        if (getResult() != null && getResult().getAmount() == 64) return;

        if (getFuel() != null && !CustomFurnaceChecker.getBurnTimeList().containsKey(getFuel().getType())) return;

        // Fuel
        if (fuel <= 0 && getFuel() != null) {
            fuel = CustomFurnaceChecker.getBurnTimeList().get(getFuel().getType());
            if (getFuel().getType().equals(Material.LAVA_BUCKET)) {
                inventory.setFuel(new ItemStack(Material.BUCKET));
            } else {
                int fuelLeft = getFuel().getAmount() - 1;
                if (fuelLeft == 0) inventory.setFuel(null);
                else inventory.setFuel(new ItemStack(getFuel().getType(), fuelLeft));
            }
        }
        if (fuel == 0) return;

        // Burn
        if (speed == 1) ticksPassed += 2;
        else if (speed == 2) ticksPassed += 3;
        else if (speed == 3) ticksPassed += 4;
        else ticksPassed += 100;

        // Cook item
        if (ticksPassed >= 200) {
            Material mat = CustomFurnaceChecker.getFurnaceRecipes().get(getSmelting().getType());
            if (getResult() != null) {
                int amount = getResult().getAmount() + 1;
                inventory.setResult(new ItemStack(mat, amount));
            }
            else inventory.setResult(new ItemStack(mat, 1));
            int amount = getSmelting().getAmount() - 1;
            if (amount == 0) inventory.setSmelting(null);
            else inventory.setSmelting(new ItemStack(getSmelting().getType(), amount));
            ticksPassed = 0;
        }
    }

    public void openFurnace(Player p) {
        p.openInventory(inventory);
    }

    public ItemStack getFuel() {
        return inventory.getFuel();
    }

    public ItemStack getSmelting() {
        return inventory.getSmelting();
    }

    public ItemStack getResult() {
        return inventory.getResult();
    }

    public int getSpeed() {
        return speed;
    }

    public Location getLocation() {
        return location;
    }

    public FurnaceInventory getInventory() {
        return inventory;
    }
}
