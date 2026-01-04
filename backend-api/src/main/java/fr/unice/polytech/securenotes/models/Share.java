package fr.unice.polytech.securenotes.models;

public class Share {
    private String userId;
    private SharePermission permission;
    
    public enum SharePermission {
        READ_ONLY,
        READ_WRITE
    }
    
    public Share() {}
    
    public Share(String userId, SharePermission permission) {
        this.userId = userId;
        this.permission = permission;
    }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public SharePermission getPermission() { return permission; }
    public void setPermission(SharePermission permission) { this.permission = permission; }
}