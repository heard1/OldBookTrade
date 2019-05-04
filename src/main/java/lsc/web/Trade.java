package lsc.web;

import org.springframework.jdbc.core.JdbcTemplate;

public class Trade {
    private String sale;
    private String buy;
    private int bookID;
    private boolean isdeal;
    Trade() {}
    public static void addTrade(JdbcTemplate sql, String sale, String buy, int bookID) {
        sql.update("insert into trade (bookID, sale, buy, isdeal) values(?,?,?,?)",new Object[] {bookID, sale, buy, false});
        return;
    }
}
