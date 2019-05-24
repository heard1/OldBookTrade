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

}
