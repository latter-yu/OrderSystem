package OrderSystem.api;

import OrderSystem.model.User;
import OrderSystem.model.UserDao;
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

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private Gson gson = new GsonBuilder().create();

    static class Request {
        public String name;
        public String password;
    }

    static class Response {
        public int ok;
        public String reason;
        public String name;
        public int isAdmin;
    }

    // 登录功能 API
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        Response response = new Response();
        try {
            // 1. 读取 body 数据
            String body = OrderSystemUtil.readBody(req);
            // 2. 将读取的数据解析成对象
            Request request = gson.fromJson(body, Request.class);
            // 3. 按照用户名进行查找, 并校验密码
            UserDao userDao = new UserDao();
            User user = userDao.selectByName(request.name);
            if (user == null || !user.getPassword().equals(request.password)) {
                throw new OrderSystemException("用户名或密码错误");
            }
            // 4. 登录成功，则创建 session 对象，绑定用户
            HttpSession session = req.getSession(true);
            // request.getSession() 和 request.getSession(true) 意思相同：
            // 获取 session, 如果 session 不存在，就新建一个
            // reqeust.getSession(false) 获取 session, 如果 session 不存在，则返回 null
            session.setAttribute("user", user);
            response.ok = 1;
            response.reason = "";
            response.name = user.getName();
            response.isAdmin = user.getIsAdmin();
        } catch (OrderSystemException e) {
            // 登录失败，返回错误提示
            response.ok = 0;
            response.reason = e.getMessage();
        } finally {
            // 5. 结果写回客户端
            resp.setContentType("application/json; charset=utf-8");
            String jsonString = gson.toJson(response);
            resp.getWriter().write(jsonString);
        }
    }

    // 检测登录状态
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        Response response = new Response();
        try {
            // 1. 获取当前用户的 session，获取不到则为未登录状态
            HttpSession session = req.getSession(false);
            if (session == null) {
                throw new OrderSystemException("当前未登录");
            }
            // 2. 从 session 中获取 user 对象
            User user = (User)session.getAttribute("user");
            if (user == null) {
                throw new OrderSystemException("当前未登录");
            }
            // 3. 把 user 中的信息填充到返回值结果中
            response.ok = 1;
            response.reason = "";
            response.name = user.getName();
            response.isAdmin = user.getIsAdmin();
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
