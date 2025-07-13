package sir_draco.spinwheel;

public class WheelStats {

    private int spins;
    private int time;
    private int rare;
    private int epic;
    private int legendary;

    public WheelStats(int spins, int time, int rare, int epic, int legendary) {
        this.spins = spins;
        this.time = time;
        this.rare = rare;
        this.epic = epic;
        this.legendary = legendary;
    }

    public int getSpins() {
        return spins;
    }

    public void setSpins(int spins) {
        this.spins = spins;
    }

    public void changeSpins(int amount) {
        spins += amount;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void increaseTime() {
        time++;
    }

    public int getRare() {
        return rare;
    }

    public void setRare(int rare) {
        this.rare = rare;
    }

    public void changeRare(int amount) {
        rare += amount;
    }

    public int getEpic() {
        return epic;
    }

    public void setEpic(int epic) {
        this.epic = epic;
    }

    public void changeEpic(int amount) {
        epic += amount;
    }

    public int getLegendary() {
        return legendary;
    }

    public void setLegendary(int legendary) {
        this.legendary = legendary;
    }

    public void changeLegendary(int amount) {
        legendary += amount;
    }
}
