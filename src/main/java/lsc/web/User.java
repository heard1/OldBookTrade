package lsc.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class User {
    public String account;
    public String password;
    public String email;
    public String username;

    public User(){}
    // 生成一个用户，密码用md5加密
    public User(String account, String password, String email, String username) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            this.password = new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e){}
        this.account = account;
        this.email = email;
        this.username = username;
    }
    // 数据库操作：查找账户是否匹配
    public static boolean login(JdbcTemplate sql, String account, String password) {
        String tem = "select password from user where account = ?";
        List<User> userList = new ArrayList<User>();
        sql.query(tem, new Object[]{account}, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                User user = new User(null,null,null,null);
                user.password = rs.getString("password");
                userList.add(user);
            }
        });
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            String webpass = new BigInteger(1, md.digest()).toString(16);
            if(userList.get(0).password.equals(webpass)) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
    // 数据库操作：记录一个新用户
    public static boolean signUp(JdbcTemplate sql, String account, String password, String email, String username) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            password = new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e){}
        try{
            sql.update("insert into user values(?,?,?,?)",new Object[] {account, password, email, username});
            return true;
        } catch (Exception e) {
            return false;
        }

    }
    // 数据库操作：根据账户名找username
    public static String findusername(JdbcTemplate sql, String info) {
         String find = "select * from user where account=?";
         List<User> users = new ArrayList<User>();
         sql.query(find, new Object[]{info}, new RowCallbackHandler() {
             public void processRow(ResultSet rs) throws SQLException {
                 User single = new User();
                 single.username = rs.getString("username");
                 users.add(single);
             }
         });
        return users.get(0).username;
    }
    // 数据库操作：根据bookID找account
    public static String findaccount(JdbcTemplate sql, Boolean SB, int bookID) {
        String find;
        List<User> users = new ArrayList<User>();
        if(SB) {
            find = "select * from trade where bookID=?";
            sql.query(find, new Object[]{bookID}, new RowCallbackHandler() {
                public void processRow(ResultSet rs) throws SQLException {
                    User single = new User();
                    single.account = rs.getString("sale");
                    users.add(single);
                }
            });
        }else {
            find = "select * from trade where bookID=?";
            sql.query(find, new Object[]{bookID}, new RowCallbackHandler() {
                public void processRow(ResultSet rs) throws SQLException {
                    User single = new User();
                    single.account = rs.getString("buy");
                    users.add(single);
                }
            });
        }
        return users.get(0).account;
    }

}
