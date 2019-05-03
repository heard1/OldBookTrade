package lsc.web;

import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigInteger;
import java.security.MessageDigest;

public class Book {
    public String bookname;
    public boolean isdeal;
    public boolean SBtype;
    public int bookID;
    public int oriprice;
    public int curprice;
    public String link;
    public String intro;
    public String category;

    public Book() {}

    public static boolean salebook(JdbcTemplate sql, int bookID, String bookname, String category, double oriprice, double curprice, String link, String intro) {
        try{
            sql.update("insert into book (bookID, bookname, isdeal, SBtype, oriprice, curprice, link, intro, category) values(?,?,?,?,?,?,?,?,?)",new Object[] {bookID, bookname, false, true, oriprice, curprice, link, intro, category});
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
