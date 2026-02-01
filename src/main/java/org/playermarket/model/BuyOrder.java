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

public class BuyOrder {
    private int id;
    private UUID buyerUuid;
    private String buyerName;
    private String itemBase64;
    private int amount;
    private int remainingAmount;
    private double unitPrice;
    private double totalPrice;
    private double remainingTotalPrice;
    private Timestamp createTime;
    private boolean fulfilled;
    private UUID sellerUuid;
    private String sellerName;
    private Timestamp fulfillTime;
    
    public BuyOrder() {
    }
    
    public BuyOrder(UUID buyerUuid, String buyerName, ItemStack item, double unitPrice, int amount, double totalPrice) {
        this.buyerUuid = buyerUuid;
        this.buyerName = buyerName;
        
        // 确保序列化时使用数量为1的样本
        ItemStack sampleItem = item.clone();
        sampleItem.setAmount(1);
        this.itemBase64 = itemStackToBase64(sampleItem);
        
        this.amount = amount;
        this.remainingAmount = amount;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.remainingTotalPrice = totalPrice;
        this.createTime = new Timestamp(System.currentTimeMillis());
        this.fulfilled = false;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
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

    public int getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(int remainingAmount) {
        this.remainingAmount = remainingAmount;
    }
    
    public double getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }
    
    public double getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public double getRemainingTotalPrice() {
        return remainingTotalPrice;
    }

    public void setRemainingTotalPrice(double remainingTotalPrice) {
        this.remainingTotalPrice = remainingTotalPrice;
    }
    
    public Timestamp getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
    
    public boolean isFulfilled() {
        return fulfilled;
    }
    
    public void setFulfilled(boolean fulfilled) {
        this.fulfilled = fulfilled;
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
    
    public Timestamp getFulfillTime() {
        return fulfillTime;
    }
    
    public void setFulfillTime(Timestamp fulfillTime) {
        this.fulfillTime = fulfillTime;
    }
    
    // 获取ItemStack对象
    public ItemStack getItemStack() {
        ItemStack item = itemStackFromBase64(itemBase64);
        item.setAmount(1);
        return item;
    }
    
    @Override
    public String toString() {
        return "BuyOrder{" +
                "id=" + id +
                ", buyerUuid=" + buyerUuid +
                ", buyerName='" + buyerName + '\'' +
                ", amount=" + amount +
                ", unitPrice=" + unitPrice +
                ", fulfilled=" + fulfilled +
                '}';
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
            throw new IllegalStateException("无法将Base64反序列化为ItemStack", e);
        }
    }
}