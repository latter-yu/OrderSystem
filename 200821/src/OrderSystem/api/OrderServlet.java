package OrderSystem.api;

import OrderSystem.model.Dish;
import OrderSystem.model.Order;
import OrderSystem.model.OrderDao;
import OrderSystem.model.User;
import OrderSystem.util.OrderSystemException;
import OrderSystem.util.OrderSystemUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/order")
public class OrderServlet extends HttpServlet {
    private Gson gson = new GsonBuilder().create();

    // 订单相关操作.
    // 只有新增订单是带 body 的.
    // 而且 body 又是整数数组. 不需要创建专门的类来表示请求的 body 内容了.

    static class Response {
        public int ok;
        public String reason;
    }

    // 新增订单.(普通用户才能新增, 管理员不能新增)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Response response = new Response();
        req.setCharacterEncoding("utf-8");
        try {
            // 1. 检查用户登陆状态.
            HttpSession session = req.getSession(false);
            if (session == null) {
                throw new OrderSystemException("您尚未登陆");
            }
            User user = (User) session.getAttribute("user");
            if (user == null) {
                throw new OrderSystemException("您尚未登陆");
            }
            // 2. 判断用户是否是管理员
            if (user.getIsAdmin() == 1) {
                // 管理员, 就禁止新增订单
                throw new OrderSystemException("您是管理员");
            }
            // 3. 读取 body 中的数据, 进行解析.
            String body = OrderSystemUtil.readBody(req);
            // 4. 按照 JSON 格式解析 body(转换成整型数组比较简单）
            Integer[] dishIds = gson.fromJson(body, Integer[].class);
            // 也可转换成整型 list（涉及到泛型）
            // List<Integer> dishIds = gson.fromJson(body, new TypeToken<List<Integer>>() {}.getType());
            // 5. 构造订单对象, 此处 orderId, time, isDone, Dish 中的 name, price 这些都不需要填充
            //    不影响订单插入.
            Order order = new Order();
            order.setUserId(user.getUserId());
            List<Dish> dishes = new ArrayList<>();
            for (Integer dishId : dishIds) {
                Dish dish = new Dish();
                dish.setDishId(dishId);
                dishes.add(dish);
            }
            order.setDishes(dishes);
            // 6. 把 Order 对象插入到数据库中
            OrderDao orderDao = new OrderDao();
            orderDao.add(order);
            response.ok = 1;
            response.reason = "";
        } catch (OrderSystemException e) {
            response.ok = 0;
            response.reason = e.getMessage();
        } finally {
            resp.setContentType("application/json; charset=utf-8");
            String jsonString = gson.toJson(response);
            resp.getWriter().write(jsonString);
        }
    }

    // 查看所有订单.
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        resp.setContentType("application/json; charset=utf-8");
        Response response = new Response();
        List<Order> orders = new ArrayList<>();
        try {
            // 1. 验证用户登陆状态.
            HttpSession session = req.getSession(false);
            if (session == null) {
                throw new OrderSystemException("您尚未登陆");
            }
            User user = (User) session.getAttribute("user");
            if (user == null) {
                throw new OrderSystemException("您尚未登陆");
            }
            // 2. 读取 orderId 字段, 看该字段是否存在.
            OrderDao orderDao = new OrderDao();
            String orderIdStr = req.getParameter("orderId");
            if (orderIdStr == null) {
                // 3. 查找数据库, 查找所有订单
                // 判断用户是管理员还是普通用户
                if (user.getIsAdmin() == 0) {
                    // 普通用户, 只查看自己的订单
                    orders = orderDao.selectByUserId(user.getUserId());
                } else {
                    // 管理员, 查看所有订单
                    orders = orderDao.selectAll();
                }
                // 4. 构造响应结果
                String jsonString = gson.toJson(orders);
                resp.getWriter().write(jsonString);
            } else {
                // 3. 查找数据库, 查找指定订单.
                int orderId = Integer.parseInt(orderIdStr);
                Order order = orderDao.selectByOrderId(orderId);
                // 如果是普通用户, 查找时发现自身的 userId 和订单的 userId 不相符,
                // 这种就返回一个出错数据.
                // 如果是管理员, 才允许查看所有用户的订单
                if (user.getIsAdmin() == 0
                        && order.getUserId() != user.getUserId()) {
                    throw new OrderSystemException("当年您无权查看其他人的订单");
                }
                // 4. 构造响应结果
                String jsonString = gson.toJson(order);
                resp.getWriter().write(jsonString);
            }
        } catch (OrderSystemException e) {
            // 5. 处理异常情况
            String jsonString = gson.toJson(orders);
            resp.getWriter().write(jsonString);
        }
    }

    // 修改订单状态.
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        Response response = new Response();
        try {
            // 1. 检查用户的登陆状态.
            HttpSession session = req.getSession(false);
            if (session == null) {
                throw new OrderSystemException("您尚未登陆");
            }
            User user = (User) session.getAttribute("user");
            if (user == null) {
                throw new OrderSystemException("您尚未登陆");
            }
            // 2. 判断用户是否是管理员
            if (user.getIsAdmin() == 0) {
                throw new OrderSystemException("您不是管理员");
            }
            // 3. 读取请求中的字段 orderId 和 isDone
            String orderIdStr = req.getParameter("orderId");
            String isDoneStr = req.getParameter("isDone");
            if (orderIdStr == null || isDoneStr == null) {
                throw new OrderSystemException("参数有误");
            }
            // 4. 修改数据库.
            OrderDao orderDao = new OrderDao();
            int orderId = Integer.parseInt(orderIdStr);
            int isDone = Integer.parseInt(isDoneStr);
            orderDao.changeState(orderId, isDone);
            // 5. 返回响应结果.
            response.ok = 1;
            response.reason = "";
        } catch (OrderSystemException e) {
            response.ok = 0;
            response.reason = e.getMessage();
        } finally {
            resp.setContentType("application/json; charset=utf-8");
            String jsonString = gson.toJson(response);
            resp.getWriter().write(jsonString);
        }
    }
}