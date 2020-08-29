package OrderSystem.model;

import OrderSystem.util.OrderSystemException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {
    // 操作订单
    // 1. 新增订单
    // 2. 查看所有订单(管理员, 商家)
    // 3. 查看指定用户的订单(普通用户, 顾客)
    // 4. 查看指定订单的详细信息
    // 5. 修改订单状态(订单是否已经完成)

    // 新增订单
    // 订单是和两个表关联的.
    // 第一个表 order_user
    // 第二个表 order_dish, 一个订单中可能会涉及点多个菜, 就需要给这个表一次性插入多个记录.
    public static void add(Order order) throws OrderSystemException {
        // 1. 先操作 order_user 表
        addOrderUser(order);
        // 2. 再操作 order_dish 表
        //    执行 add 方法的时候, order 对象中的 orderId 字段还没有得到
        //    这个字段要交给数据库, 由自增主键来决定.
        addOrderDish(order);
    }

    private static void addOrderUser(Order order) throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        String sql = "insert into orderuser values(null, ?, now(), 0)";
        // now(): 时间
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            // PreparedStatement.RETURN_GENERATED_KEYS: 插入的同时获取到自增主键的值（orderId）-- 在 orderdish 中用到
            ps.setInt(1, order.getUserId());
            int ret = ps.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("插入订单失败");
            }
            // 读取自增主键
            resultSet = ps.getGeneratedKeys();
            if (resultSet.next()) {
                // 1. 读取 resultSet 的结果时, 可以使用列名, 也可以使用下标.
                // 由于一个表中的自增列可以有多个. 返回的时候都返回回来了.
                // 下标填成 1 , 就表示想获取到第一个自增列生成的值.
                order.setOrderId(resultSet.getInt(1));
            }
            System.out.println("插入订单第一步成功");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("插入订单失败");
        } finally {
            DBUtil.close(connection, ps, resultSet);
        }
    }

    private static void addOrderDish(Order order) throws OrderSystemException {
        // 把菜品信息给插入到表 order_dish 中.

        Connection connection = DBUtil.getConnection();
        String sql = "insert into orderdish values(?, ?)";
        PreparedStatement ps = null;
        try {
            // 关闭自动提交
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(sql);
            // 由于一个订单对应到多个菜品, 就需要遍历 Order 中包含的菜品数组, 把每个记录都取出来
            // 遍历 dishes 给 SQL 添加多个 values 的值
            List<Dish> dishes = order.getDishes();
            for (Dish dish : dishes) {
                // OrderId 是在刚刚进行插入 orderuser 表的时候, 获取到的自增主键
                ps.setInt(1, order.getOrderId());
                ps.setInt(2, dish.getDishId());
                ps.addBatch(); // 给 sql 新增一个片段
            }
            ps.executeBatch(); // 执行 sql 语句（并不是真的执行, 因为取消了自动提交，并没有发送给服务器）
            connection.commit();
            // 发送给服务器 (真的执行), commit 可以去执行多个 SQL, 一次调用 commit 统一发给服务器.
        } catch (SQLException e) {
            e.printStackTrace();
            // 如果上面的操作出现异常, 就认为整体的新增订单操作失败, 回滚之前的插入 orderuser 表的内容
            deleteOrderUser(order.getOrderId());
        } finally {
            DBUtil.close(connection, ps, null);
        }
    }

    private static void deleteOrderUser(int orderId) throws OrderSystemException {
        // 删除 orderuser 表中的记录
        Connection connection = DBUtil.getConnection();
        String sql = "delete from orderuser where orderId = ?";
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setInt(1, orderId);
            int ret = ps.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("回滚失败");
            }
            System.out.println("回滚成功");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("回滚失败");
        } finally {
            DBUtil.close(connection, ps, null);
        }
    }

    public List<Order> selectAll() throws OrderSystemException {
        // 查询所有订单
        // Order 对象里, 有一些 orderId, userId 这些属性. 直接借助 orderuser 表就获取到了
        // 还有一个重要的属性, dishes (List<Dish>) .
        // 详细信息需要先根据 orderdish 表, 获取到所有相关的 dishId, 然后再根据 dishId 在 dishes 表中查.
        // 这里的订单获取, 不需要获取订单的详细内容. 只获取到订单的一些基本信息.
        // 菜品信息, 有查看指定订单详细信息的接口(selectByOrderId)
        // 因此当前这个接口返回的 Order 对象中, 不包含 dishes 详细数据.
        // 让代码更简单, 更高效.

        List<Order> orders = new ArrayList<>();
        Connection connection = DBUtil.getConnection();
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        String sql = "select * from orderuser";
        try {
            ps = connection.prepareStatement(sql);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                // 此时 order 对象中, 没有 dishes 字段的.
                Order order = new Order();
                order.setOrderId(resultSet.getInt("orderId"));
                order.setUserId(resultSet.getInt("userId"));
                order.setTime(resultSet.getTimestamp("time"));
                order.setIsDone(resultSet.getInt("isDone"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("查询所有订单失败！");
        } finally {
            DBUtil.close(connection, ps, resultSet);
        }
        return orders;
    }

    public List<Order> selectByUserId(int userId) throws OrderSystemException {
        // 查看指定用户的订单(普通用户, 顾客)
        // 一个用户可能有多个订单

        List<Order> orders = new ArrayList<>();
        Connection connection = DBUtil.getConnection();
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        String sql = "select * from orderuser where userId = ?";
        try {
            ps = connection.prepareStatement(sql);
            ps.setInt(1, userId);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Order order = new Order();
                order.setOrderId(resultSet.getInt("orderId"));
                order.setUserId(resultSet.getInt("userId"));
                order.setTime(resultSet.getTimestamp("time"));
                order.setIsDone(resultSet.getInt("isDone"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("查询指定订单失败！");
        } finally {
            DBUtil.close(connection, ps, resultSet);
        }
        return orders;
    }

    public Order selectByOrderId(int orderId) throws OrderSystemException {
        // 查看指定订单的详细信息
        // 用这个方法查找完整的 Order 对象.
        // 包括 Order 中有哪些的菜品, 以及菜品的详情

        // 1. 先根据 orderId 得到一个 Order 对象
        Order order = buildOrder(orderId);
        // 2. 根据 orderId 得到该 orderId 对应的菜品 id 列表
        List<Integer> dishIds = selectDishIds(orderId);
        // 3. 根据 菜品 id 列表, 查询 dishes 表, 获取到菜品详情
        order = getDishDetail(order, dishIds);
        return order;
    }

    private Order buildOrder(int orderId) throws OrderSystemException {
        // 根据 orderId 来查询对应的 Order 对象的基本信息
        // 查找 orderUser 表

        Connection connection = DBUtil.getConnection();
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        String sql = "select * from orderuser where orderId = ?";
        try {
            ps = connection.prepareStatement(sql);
            ps.setInt(1, orderId);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                // 此时 order 对象中, 没有 dishes 字段的.
                Order order = new Order();
                order.setOrderId(resultSet.getInt("orderId"));
                order.setUserId(resultSet.getInt("userId"));
                order.setTime(resultSet.getTimestamp("time"));
                order.setIsDone(resultSet.getInt("isDone"));
                return order;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("查询 orderId 订单失败！");
        } finally {
            DBUtil.close(connection, ps, resultSet);
        }
        return null;
    }

    private List<Integer> selectDishIds(int orderId) throws OrderSystemException {
        // 根据 orderId 查找 orderdishIds(在 orderdish 表中查找)

        List<Integer> dishIds = new ArrayList<>();
        Connection connection = DBUtil.getConnection();
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        String sql = "select * from orderdish where orderId = ?";
        try {
            ps = connection.prepareStatement(sql);
            ps.setInt(1, orderId);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                dishIds.add(resultSet.getInt("dishId"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("查找 orderdish 失败！");
        }
        return dishIds;
    }

    private Order getDishDetail(Order order, List<Integer> dishIds) throws OrderSystemException {
        // 根据 dishId 查询菜品详情

        List<Dish> dishes = new ArrayList<>();
        // 遍历 dishIds 在 dishes 表中查.  (直接调用 DishDao 内的方法.)
        DishDao dishDao = new DishDao();
        for (Integer dishId : dishIds) {
            Dish dish = dishDao.selectById(dishId);
            dishes.add(dish);
        }
        // 把 dishes 设置到 order 对象中
        order.setDishes(dishes);
        return order;
    }

    public void changeState(int orderId, int isDone) throws OrderSystemException {
        // 修改订单状态(订单是否已经完成)

        Connection connection = DBUtil.getConnection();
        PreparedStatement ps = null;
        String sql = "update orderuser set isDone = ? where orderId = ?";
        try {
            ps = connection.prepareStatement(sql);
            ps.setInt(1, isDone);
            ps.setInt(2, orderId);
            int ret = ps.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("修改订单状态失败！");
            }
            System.out.println("修改订单状态成功！");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("修改订单状态失败！");
        } finally {
            DBUtil.close(connection, ps, null);
        }
    }

    public static void main(String[] args) {

    }
}
