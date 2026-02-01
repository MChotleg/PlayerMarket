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

public class MarketItem {
    private int id;
    private UUID sellerUuid;
    private String sellerName;
    private String itemBase64;
    private int amount;
    private double unitPrice;
    private double price;
    private Timestamp listTime;
    private boolean sold;
    private UUID buyerUuid;
    private String buyerName;
    private Timestamp soldTime;
    
    public MarketItem() {
    }
    
    public MarketItem(UUID sellerUuid, String sellerName, ItemStack item, double price) {
        this.sellerUuid = sellerUuid;
        this.sellerName = sellerName;
        
        // 确保序列化时使用数量为1的样本
        ItemStack sampleItem = item.clone();
        sampleItem.setAmount(1);
        this.itemBase64 = itemStackToBase64(sampleItem);
        
        this.amount = item.getAmount();
        this.price = price;
        this.unitPrice = price / item.getAmount();
        this.listTime = new Timestamp(System.currentTimeMillis());
        this.sold = false;
    }
    
    public MarketItem(UUID sellerUuid, String sellerName, ItemStack item, double unitPrice, int amount, double totalPrice) {
        this.sellerUuid = sellerUuid;
        this.sellerName = sellerName;
        
        // 确保序列化时使用数量为1的样本
        ItemStack sampleItem = item.clone();
        sampleItem.setAmount(1);
        this.itemBase64 = itemStackToBase64(sampleItem);
        
        this.amount = amount;
        this.unitPrice = unitPrice;
        this.price = totalPrice;
        this.listTime = new Timestamp(System.currentTimeMillis());
        this.sold = false;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public UUID getSellerUuid() {
        return sellerUuid;
    }
    
    public void setSellerUuid(UUID sellerUuid) {
        this.sellerUuid = sellerUuid;
    }
    
    public String getSellerName() {
        return sellerName;
    }
    
    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
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
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public double getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }
    
    public Timestamp getListTime() {
        return listTime;
    }
    
    public void setListTime(Timestamp listTime) {
        this.listTime = listTime;
    }
    
    public boolean isSold() {
        return sold;
    }
    
    public void setSold(boolean sold) {
        this.sold = sold;
    }
    
    public UUID getBuyerUuid() {
        return buyerUuid;
    }
    
    public void setBuyerUuid(UUID buyerUuid) {
        this.buyerUuid = buyerUuid;
    }
    
    public String getBuyerName() {
        return buyerName;
    }
    
    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }
    
    public Timestamp getSoldTime() {
        return soldTime;
    }
    
    public void setSoldTime(Timestamp soldTime) {
        this.soldTime = soldTime;
    }
    
    // 工具方法：ItemStack 转 Base64
    public static String itemStackToBase64(ItemStack item) {
        try {
            // 克隆物品并强制设置数量为1，确保比较时忽略数量差异
            ItemStack itemToSerialize = item.clone();
            itemToSerialize.setAmount(1);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(itemToSerialize);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("无法将物品序列化为Base64", e);
        }
    }
    
    // 工具方法：Base64 转 ItemStack
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
    
    // 获取ItemStack对象
    public ItemStack getItemStack() {
        ItemStack item = itemStackFromBase64(itemBase64);
        item.setAmount(amount);  // 设置正确的数量
        return item;
    }
    
    // 设置ItemStack对象
    public void setItemStack(ItemStack item) {
        this.itemBase64 = itemStackToBase64(item);
    }
    
    @Override
    public String toString() {
        return "MarketItem{" +
                "id=" + id +
                ", sellerUuid=" + sellerUuid +
                ", sellerName='" + sellerName + '\'' +
                ", price=" + price +
                ", listTime=" + listTime +
                ", sold=" + sold +
                '}';
    }
}