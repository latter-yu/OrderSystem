package OrderSystem.model;

import OrderSystem.util.OrderSystemException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {
    // 1. 插入用户 - 注册的时候使用
    // 2. 按名字查找用户 - 登陆时使用
    // 3. 按照用户 id 查找 - 展示信息时使用

    public void add(User user) throws OrderSystemException {
        // 新增用户

        // JDBC 编程的基本流程
        // 1. 先获取和数据库的连接(DataSource)
        Connection connection = DBUtil.getConnection();
        // 2. 拼装 SQL 语句(PrepareStatement)
        String sql = "insert into user values(null, ?, ?, ?)";
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setString(1, user.getName());
            ps.setString(2, user.getPassword());
            ps.setInt(3, user.getIsAdmin());
            // 3. 执行 SQL 语句(executeQuery, executeUpdate)
            int ret = ps.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("插入用户失败！");
            }
            System.out.println("插入用户成功！");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("插入用户失败！");
        } finally {
            // 4. 关闭连接(close) (如果是查询语句, 还需要遍历结果集合)
            DBUtil.close(connection, ps, null);
        }
    }

    public User selectByName(String name) throws OrderSystemException {
        // 按姓名查找用户(要求姓名不能重复)

        Connection connection = DBUtil.getConnection();
        String sql = "select * from user where name = ?";
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setString(1, name);
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                User user = new User();
                user.setUserId(resultSet.getInt("userId"));
                user.setName(resultSet.getString("name"));
                user.setPassword(resultSet.getString("password"));
                user.setIsAdmin(resultSet.getInt("isAdmin"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("按姓名查找用户失败！");
        } finally {
            DBUtil.close(connection, ps, resultSet);
        }
        return null;
    }

    public User selectById(int userId) throws OrderSystemException {
        // 按 id 查找用户

        Connection connection = DBUtil.getConnection();
        String sql = "select * from user where userId = ?";
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            ps = connection.prepareStatement(sql);
            ps.setInt(1, userId);
            resultSet = ps.executeQuery();
            // userId 是主键. 不会重复的. 最多只能查到一条记录
            if (resultSet.next()) {
                User user = new User();
                user.setUserId(resultSet.getInt("userId"));
                user.setName(resultSet.getString("name"));
                user.setPassword(resultSet.getString("password"));
                user.setIsAdmin(resultSet.getInt("isAdmin"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("按 id 查找用户失败！");
        } finally {
            DBUtil.close(connection, ps, resultSet);
        }
        return null;
    }

//    public static void main(String[] args) throws OrderSystemException {

//        User user = new User();

//        user.setName("kxy");
//        user.setPassword("123");
//        user.setIsAdmin(1);
//        UserDao.add(user);

//        System.out.println(UserDao.selectByName("kxy"));
//        System.out.println(UserDao.selectByName("kcy"));

//        System.out.println(UserDao.selectById(1));
//        System.out.println(UserDao.selectById(9));

//    }
}
