package OrderSystem.model;

import OrderSystem.util.OrderSystemException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DishDao {
    // 操作菜品表.
    // 1. 新增菜品
    // 2. 删除菜品
    // 3. 查询所有菜品
    // 4. 查询指定菜品
    // 修改菜品信息, 也是可以支持的. (主要就是改价格)

    public void add(Dish dish) throws OrderSystemException {
        // 添加菜品

        Connection connection = DBUtil.getConnection();
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("insert into dishes values(null, ?, ?)");
            ps.setString(1, dish.getName());
            ps.setInt(2, dish.getPrice());
            int ret = ps.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("添加菜品失败！");
            }
            System.out.println("添加菜品成功！");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("添加菜品失败！");
        } finally {
            DBUtil.close(connection, ps, null);
        }
    }

    public static void delete(int dishId) throws OrderSystemException {
        // 按 id 删除菜品

        Connection connection = DBUtil.getConnection();
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("delete from dishes where dishId = ?");
            ps.setInt(1, dishId);
            int ret = ps.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("删除菜品失败！");
            }
            System.out.println("删除菜品成功！");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("删除菜品失败！");

        } finally {
            DBUtil.close(connection, ps, null);
        }
    }

    public Dish selectById(int dishId) throws OrderSystemException {
        // 根据 id 查询指定菜品

        Connection connection = DBUtil.getConnection();
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        String sql = "select * from dishes where dishId = ?";
        try {
            ps = connection.prepareStatement(sql);
            ps.setInt(1, dishId);
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                Dish dish = new Dish();
                dish.setDishId(resultSet.getInt("dishId"));
                dish.setName(resultSet.getString("name"));
                dish.setPrice(resultSet.getInt("price"));
                return dish;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("查询菜品失败！");
        } finally {
            DBUtil.close(connection, ps, resultSet);
        }
        return null;
    }

    public List<Dish> selectAll() throws OrderSystemException {
        // 查询所有菜品
        List<Dish> dishes = new ArrayList<>();
        Connection connection = DBUtil.getConnection();
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        String sql = "select * from dishes";
        try {
            ps = connection.prepareStatement(sql);
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Dish dish = new Dish();
                dish.setDishId(resultSet.getInt("dishId"));
                dish.setName(resultSet.getString("name"));
                dish.setPrice(resultSet.getInt("price"));
                dishes.add(dish);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("查询所有菜品失败！");
        } finally {
            DBUtil.close(connection, ps, resultSet);
        }
        return dishes;
    }

    public static void update(int price,int dishId) throws OrderSystemException {
        // 按 id 修改菜品价格

        Connection connection = DBUtil.getConnection();
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("update dishes set price = ? where dishId = ?");
            ps.setInt(1, price);
            ps.setInt(2, dishId);
            int ret = ps.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("修改菜品失败！");
            }
            System.out.println("修改菜品成功！");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("修改菜品失败！");

        } finally {
            DBUtil.close(connection, ps, null);
        }
    }

//    public static void main(String[] args) throws OrderSystemException {
//        Dish dish = new Dish();

//        dish.setName("鱼香肉丝");
//        dish.setPrice(20);
//        DishDao.add(dish);

//        System.out.println(selectById(2));

//        System.out.println(selectAll());

//        update(18, 1);

//        delete(1);
//    }
}
