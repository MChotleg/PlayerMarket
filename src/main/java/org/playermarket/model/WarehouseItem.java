package org.playermarket.model;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.UUID;

public class WarehouseItem {
    private int id;
    private UUID ownerUuid;
    private String ownerName;
    private String itemBase64;
    private int amount;
    private double originalPrice;
    private double originalUnitPrice;
    private Timestamp delistingTime;
    private int originalMarketItemId;
    
    public WarehouseItem() {
    }
    
    public WarehouseItem(UUID ownerUuid, String ownerName, ItemStack item, double originalPrice, double originalUnitPrice, int originalMarketItemId) {
        this.ownerUuid = ownerUuid;
        this.ownerName = ownerName;
        this.amount = item.getAmount();
        ItemStack sample = item.clone();
        sample.setAmount(1);
        this.itemBase64 = itemStackToBase64(sample);
        this.originalPrice = originalPrice;
        this.originalUnitPrice = originalUnitPrice;
        this.delistingTime = new Timestamp(System.currentTimeMillis());
        this.originalMarketItemId = originalMarketItemId;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public UUID getOwnerUuid() {
        return ownerUuid;
    }
    
    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }
    
    public String getOwnerName() {
        return ownerName;
    }
    
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    
    public String getItemBase64() {
        return itemBase64;
    }
    
    public void setItemBase64(String itemBase64) {
        this.itemBase64 = itemBase64;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public void setAmount(int amount) {
        this.amount = amount;
    }
    
    public double getOriginalPrice() {
        return originalPrice;
    }
    
    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }
    
    public double getOriginalUnitPrice() {
        return originalUnitPrice;
    }
    
    public void setOriginalUnitPrice(double originalUnitPrice) {
        this.originalUnitPrice = originalUnitPrice;
    }
    
    public Timestamp getDelistingTime() {
        return delistingTime;
    }
    
    public void setDelistingTime(Timestamp delistingTime) {
        this.delistingTime = delistingTime;
    }
    
    public int getOriginalMarketItemId() {
        return originalMarketItemId;
    }
    
    public void setOriginalMarketItemId(int originalMarketItemId) {
        this.originalMarketItemId = originalMarketItemId;
    }
    
    public static String itemStackToBase64(ItemStack item) {
        try {
            ItemStack sample = item.clone();
            sample.setAmount(1);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(sample);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("无法将物品序列化为Base64", e);
        }
    }
    
    public static ItemStack itemStackFromBase64(String base64) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("无法从Base64反序列化物品", e);
        }
    }
    
    public ItemStack getItemStack() {
        ItemStack item = itemStackFromBase64(itemBase64);
        item.setAmount(1);
        return item;
    }
    
    public void setItemStack(ItemStack item) {
        ItemStack sample = item.clone();
        sample.setAmount(1);
        this.itemBase64 = itemStackToBase64(sample);
    }
}