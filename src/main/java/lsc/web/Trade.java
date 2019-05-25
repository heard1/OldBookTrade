package lsc.web;

import org.springframework.jdbc.core.JdbcTemplate;

public class Trade {
    public String sale;
    public String buy;
    public int bookID;
    public boolean isdeal;
    public Trade() {}
    // 数据库操作：加入一个交易
    public static void addTrade(JdbcTemplate sql, String sale, String buy, int bookID) {
        sql.update("insert into trade (bookID, sale, buy, isdeal) values(?,?,?,?)",new Object[] {bookID, sale, buy, false});
        return;
    }
    // 数据库操作：加入买家
    public static void addbuy(JdbcTemplate sql, String user, int bookID) {
        sql.update("update trade set buy=? where bookID=?", new Object[] {user, bookID});
        return;
    }
    // 数据库操作：加入卖家
    public static void addsale(JdbcTemplate sql, String user, int bookID) {
        sql.update("update trade set sale=? where bookID=?", new Object[] {user, bookID});
        return;
    }
}
