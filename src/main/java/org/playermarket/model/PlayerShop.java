package org.playermarket.model;

import java.sql.Timestamp;
import java.util.UUID;

public class PlayerShop {
    private UUID playerUuid;
    private String playerName;
    private String shopName;
    private String description;
    private boolean isOpen;
    private Timestamp createdTime;
    private Timestamp lastActiveTime;
    private int totalListings;
    private int totalBuyOrders;
    private boolean isRecommended;
    private int recommendationWeight;
    
    public PlayerShop() {
    }
    
    public PlayerShop(UUID playerUuid, String playerName) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.shopName = playerName + "的店铺";
        this.description = "欢迎来到" + playerName + "的店铺！";
        this.isOpen = true;
        this.createdTime = new Timestamp(System.currentTimeMillis());
        this.lastActiveTime = new Timestamp(System.currentTimeMillis());
        this.totalListings = 0;
        this.totalBuyOrders = 0;
        this.isRecommended = false;
        this.recommendationWeight = 0;
    }
    
    public PlayerShop(UUID playerUuid, String playerName, String shopName, String description) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.shopName = shopName;
        this.description = description;
        this.isOpen = true;
        this.createdTime = new Timestamp(System.currentTimeMillis());
        this.lastActiveTime = new Timestamp(System.currentTimeMillis());
        this.totalListings = 0;
        this.totalBuyOrders = 0;
        this.isRecommended = false;
        this.recommendationWeight = 0;
    }
    
    // Getters and Setters
    public UUID getPlayerUuid() {
        return playerUuid;
    }
    
    public void setPlayerUuid(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public String getShopName() {
        return shopName;
    }
    
    public void setShopName(String shopName) {
        this.shopName = shopName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isOpen() {
        return isOpen;
    }
    
    public void setOpen(boolean open) {
        isOpen = open;
    }
    
    public Timestamp getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }
    
    public Timestamp getLastActiveTime() {
        return lastActiveTime;
    }
    
    public void setLastActiveTime(Timestamp lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }
    
    public int getTotalListings() {
        return totalListings;
    }
    
    public void setTotalListings(int totalListings) {
        this.totalListings = totalListings;
    }
    
    public int getTotalBuyOrders() {
        return totalBuyOrders;
    }
    
    public void setTotalBuyOrders(int totalBuyOrders) {
        this.totalBuyOrders = totalBuyOrders;
    }
    
    public boolean isRecommended() {
        return isRecommended;
    }
    
    public void setRecommended(boolean recommended) {
        isRecommended = recommended;
    }
    
    public int getRecommendationWeight() {
        return recommendationWeight;
    }
    
    public void setRecommendationWeight(int recommendationWeight) {
        this.recommendationWeight = recommendationWeight;
    }
    
    public void incrementListings() {
        this.totalListings++;
        updateLastActiveTime();
    }
    
    public void decrementListings() {
        this.totalListings = Math.max(0, this.totalListings - 1);
    }
    
    public void incrementBuyOrders() {
        this.totalBuyOrders++;
        updateLastActiveTime();
    }
    
    public void decrementBuyOrders() {
        this.totalBuyOrders = Math.max(0, this.totalBuyOrders - 1);
    }
    
    private void updateLastActiveTime() {
        this.lastActiveTime = new Timestamp(System.currentTimeMillis());
    }
    
    public boolean hasActiveItems() {
        return totalListings > 0 || totalBuyOrders > 0;
    }
    
    @Override
    public String toString() {
        return "PlayerShop{" +
                "playerUuid=" + playerUuid +
                ", playerName='" + playerName + '\'' +
                ", shopName='" + shopName + '\'' +
                ", isOpen=" + isOpen +
                ", totalListings=" + totalListings +
                ", totalBuyOrders=" + totalBuyOrders +
                ", isRecommended=" + isRecommended +
                '}';
    }
}