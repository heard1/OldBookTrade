package lsc.web;

import com.huaban.analysis.jieba.JiebaSegmenter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Book {
    public String bookname;
    public boolean isdeal;
    public boolean SBtype;
    public int bookID;
    public double oriprice;
    public double curprice;
    public String link;
    public String intro;
    public String category;
    public String picURL;

    public Book() {}
    // 数据库操作：加入卖出书籍
    public static boolean salebook(JdbcTemplate sql, int bookID, String bookname, String category, double oriprice, double curprice, String link, String intro) {
        try{
            sql.update("insert into book (bookID, bookname, isdeal, SBtype, oriprice, curprice, link, intro, category) values(?,?,?,?,?,?,?,?,?)",new Object[] {bookID, bookname, false, true, oriprice, curprice, link, intro, category});
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    // 数据库操作：加入买入书籍
    public static boolean buybook(JdbcTemplate sql, int bookID, String bookname, String category, double curprice, String link, String intro) {
        try{
            sql.update("insert into book (bookID, bookname, isdeal, SBtype, curprice, link, intro, category) values(?,?,?,?,?,?,?,?)",new Object[] {bookID, bookname, false, false, curprice, link, intro, category});
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    // 数据库操作：查找有关书籍
    public static List<Book> search(JdbcTemplate sql, String info) {
        List<Book> books = new ArrayList<Book>();
        Set<Integer> bookSet = new HashSet<>();
        JiebaSegmenter segmenter = new JiebaSegmenter();
        List<String> words = segmenter.sentenceProcess(info);
        for(String word : words) {
            if(!word.equals(" ")){
                String find = "select * from book where isdeal=0 and bookname like '%" + word + "%' or intro like '%" + word + "%' or category like '%" + word + "%'";
                Book singleBook = new Book();
                sql.query(find, new Object[]{}, new RowCallbackHandler() {
                    public void processRow(ResultSet rs) throws SQLException {
                        Book singleBook = new Book();
                        singleBook.bookID = Integer.parseInt(rs.getString("bookID"));
                        singleBook.bookname = rs.getString("bookname");
                        singleBook.picURL = "img/" + rs.getString("bookID")+".jpg";
                        singleBook.curprice = Double.parseDouble(rs.getString("curprice"));
                        singleBook.intro = rs.getString("intro");
                        if(!bookSet.contains(singleBook.bookID)) {
                            books.add(singleBook);
                            bookSet.add(singleBook.bookID);
                        }
                    }
                });
            }
        }
        return books;
    }
}
