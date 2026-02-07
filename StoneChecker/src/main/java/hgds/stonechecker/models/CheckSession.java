package hgds.stonechecker.models;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class CheckSession {
    
    public enum State { WAITING_METHOD, WAITING_CONTACT, IN_PROGRESS, FINISHED }
    public enum Method { DISCORD, TELEGRAM, ANYDESK }
    
    private final UUID targetId;
    private final UUID moderatorId;
    private Method method;
    private State state;
    private final long startTime;
    private boolean freeze;
    private String contactInfo;
    private BukkitTask timer;
    private long extraTimeMs;
    private Integer banDuration; // дней
    private String banReason;
    
    public CheckSession(Player target, Player moderator) {
        this.targetId = target.getUniqueId();
        this.moderatorId = moderator.getUniqueId();
        this.state = State.WAITING_METHOD;
        this.startTime = System.currentTimeMillis();
        this.freeze = true;
        this.contactInfo = null;
        this.extraTimeMs = 0;
        this.banDuration = null;
        this.banReason = null;
    }
    
    public UUID getTargetId() { return targetId; }
    public UUID getModeratorId() { return moderatorId; }
    public Method getMethod() { return method; }
    public void setMethod(Method method) { this.method = method; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    public long getStartTime() { return startTime; }
    public boolean isFreeze() { return freeze; }
    public void setFreeze(boolean freeze) { this.freeze = freeze; }
    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    public BukkitTask getTimer() { return timer; }
    public void setTimer(BukkitTask timer) { this.timer = timer; }
    public long getExtraTimeMs() { return extraTimeMs; }
    public void addExtraTimeMs(long ms) { this.extraTimeMs += ms; }
    public Integer getBanDuration() { return banDuration; }
    public void setBanDuration(Integer banDuration) { this.banDuration = banDuration; }
    public String getBanReason() { return banReason; }
    public void setBanReason(String banReason) { this.banReason = banReason; }
}
