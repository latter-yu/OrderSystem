package OrderSystem.util;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class OrderSystemUtil {
    // 需要实现读取 body 的功能.
    // 需要先把整个 body 读取出来, 然后才能去解析 JSON.

    public static String readBody(HttpServletRequest request) throws UnsupportedEncodingException {
        // 先去获取到 body 的长度(单位为字节)
        int len = request.getContentLength();
        byte[] buffer = new byte[len];
        try (InputStream inputStream = request.getInputStream()) {
            // 读取 body buffer: 缓冲区 [0, len)
            inputStream.read(buffer, 0, len);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 构造 String 的时候, 必须要指定该字符串的编码方式.
        // 这个操作相当于就是把字节数据转成字符数据
        return new String(buffer, "UTF-8");
    }
}
