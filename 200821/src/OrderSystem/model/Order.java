package OrderSystem.model;

import java.sql.Timestamp; // Timestamp 对应数据库的 datetime（时间戳）
import java.util.List;

public class Order {
    // 通过这个 Order 来表示一个完整的订单.
    // 包括订单中都有哪些菜
    // Order 类对应到两张表: order_user + order_dish

    private int orderId;
    private int userId;
    private Timestamp time; // 下单时间
    private int isDone; // 是否下单完成
    private List<Dish> dishes; // 一个订单包含了多个菜

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public int getIsDone() {
        return isDone;
    }

    public void setIsDone(int isDone) {
        this.isDone = isDone;
    }

    public List<Dish> getDishes() {
        return dishes;
    }

    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", userId=" + userId +
                ", time=" + time +
                ", isDone=" + isDone +
                ", dishes=" + dishes +
                '}';
    }
}
