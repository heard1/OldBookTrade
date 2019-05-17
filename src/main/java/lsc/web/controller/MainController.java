package lsc.web.controller;

import lsc.web.Book;
import lsc.web.MyTrade;
import lsc.web.Trade;
import lsc.web.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@Controller
public class MainController {

    @Autowired
    private JdbcTemplate sql;

    int bookID = 0;

    @RequestMapping(value={"/index","/"})
    public String login() {
        return "index";
    }


    @PostMapping("/postlogin")
    public String postlogin(HttpServletResponse response, Model model, @RequestParam String account, @RequestParam String password){
        if(User.login(sql, account, password)) {
            // save the cookie of an account
            Cookie cookie=new Cookie("account", account);
            response.addCookie(cookie);
            return home(model);
        }
        else {
            model.addAttribute("error", true);
            return "index";
        }
    }

    @PostMapping("/seebuy")
    public String seeBuy(Model model){
        String tem = "select * from book where SBtype=0  and isdeal=0 order by bookID desc limit 12";
        List<Book> books = new ArrayList<Book>();
        sql.query(tem, new Object[]{}, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                Book singleBook = new Book();
                singleBook.bookID = Integer.parseInt(rs.getString("bookID"));
                singleBook.bookname = rs.getString("bookname");
                singleBook.picURL = "img/" + rs.getString("bookID")+".jpg";
                singleBook.curprice = Double.parseDouble(rs.getString("curprice"));
                singleBook.intro = rs.getString("intro");
                books.add(singleBook);
            }
        });
        model.addAttribute("books", books);
        return "buyhome";
    }
    @PostMapping("/seesale")
    public String seeSale(Model model){
        return home(model);
    }

    @PostMapping("/signup")
    public String signUp(Model model, @RequestParam String account, @RequestParam String email, @RequestParam String username, @RequestParam String password){
        if(User.signUp(sql, account, password, email, username)) {
            model.addAttribute("success", true);
            return "index";
        }
        else {
            model.addAttribute("alreadyExist", true);
            return "index";
        }
    }

    @RequestMapping("admin.html")
    public String admin() {
        return "admin";
    }

    @RequestMapping("buy.html")
    public String buy() {
        return "buy";
    }

    @RequestMapping("sale.html")
    public String sale() {
        return "sale";
    }

    @PostMapping("/salebook")
    public String salebook(@CookieValue("account") String account, Model model, @RequestParam String bookname, @RequestParam String category, @RequestParam double oriprice,
                           @RequestParam double curprice, @RequestParam String link, @RequestParam String intro, @RequestParam MultipartFile upload){
        if(Book.salebook(sql, bookID, bookname, category, oriprice, curprice, link, intro)) {
            Trade.addTrade(sql, account, null, bookID);
            String newfileloc = System.getProperty("user.dir")+"/src/main/resources/static/img/"+bookID+".jpg";
            File newFile = new File(newfileloc);
            try{
                upload.transferTo(newFile);
            } catch (Exception e){
                e.printStackTrace();
            }
            bookID++;
            model.addAttribute("success", true);
            return "sale";
        }
        else {
            model.addAttribute("error", true);
            return "sale";
        }
    }

    @PostMapping("/buybook")
    public String buybook(@CookieValue("account") String account, Model model, @RequestParam String bookname, @RequestParam String category, @RequestParam double curprice,
                            @RequestParam String link, @RequestParam String intro, @RequestParam MultipartFile upload){
        if(Book.buybook(sql, bookID, bookname, category, curprice, link, intro)) {
            Trade.addTrade(sql, null, account, bookID);
            String newfileloc = System.getProperty("user.dir")+"/src/main/resources/static/img/"+bookID+".jpg";
            File newFile = new File(newfileloc);
            try{
                upload.transferTo(newFile);
            } catch (Exception e){
                e.printStackTrace();
            }
            bookID++;
            model.addAttribute("success", true);
            return "buy";
        }
        else {
            model.addAttribute("error", true);
            return "buy";
        }
    }

    @RequestMapping("search.html")
    public String search() {
        return "search";
    }

    @RequestMapping("trade.html")
    public String trade(@CookieValue("account") String account, Model model) {
        String tem = "select * from trade natural join book where sale = "+account+" or buy = "+account;
        List<MyTrade> trades = new ArrayList<MyTrade>();
        sql.query(tem, new Object[]{}, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                MyTrade singleTrade = new MyTrade();
                singleTrade.bookID = rs.getInt("bookID");
                singleTrade.isdeal = rs.getBoolean("isdeal");
                singleTrade.picURL = "img/" + rs.getString("bookID")+".jpg";
                singleTrade.bookname = rs.getString("bookname");
                singleTrade.price = Double.parseDouble(rs.getString("curprice"));
                singleTrade.category = rs.getString("category");
                if (rs.getBoolean("SBtype") == true)
                    singleTrade.SBtypeStr = "卖家";
                else
                    singleTrade.SBtypeStr = "买家";
                if(rs.getString("buy").equals(account))
                    singleTrade.tradeObject = rs.getString("sale");
                else
                    singleTrade.tradeObject = rs.getString("buy");
                trades.add(singleTrade);
            }
        });
        for(MyTrade temp : trades) {
            String temsql = "select username from user where account = "+temp.tradeObject;
            sql.query(temsql, new Object[]{}, new RowCallbackHandler() {
                public void processRow(ResultSet rs) throws SQLException {
                    temp.tradeObjectName = rs.getString("username");
                }
            });
        }
        model.addAttribute("trades", trades);
        return "trade";
    }

    @RequestMapping("home.html")
    public String home(Model model) {
        String tem = "select * from book where SBtype=1 and isdeal=0 order by bookID desc limit 12";
        List<Book> books = new ArrayList<Book>();
        sql.query(tem, new Object[]{}, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                Book singleBook = new Book();
                singleBook.bookID = Integer.parseInt(rs.getString("bookID"));
                singleBook.bookname = rs.getString("bookname");
                singleBook.picURL = "img/" + rs.getString("bookID")+".jpg";
                singleBook.curprice = Double.parseDouble(rs.getString("curprice"));
                singleBook.intro = rs.getString("intro");
                books.add(singleBook);
            }
        });
        model.addAttribute("books", books);
        return "home";
    }

    @RequestMapping("singleBook.html")
    public String singleBook(Model model, @RequestParam String bookID) {
        String tem = "select * from book where bookID="+bookID;
        Book singleBook = new Book();
        String condition = "欲购书籍";
        sql.query(tem, new Object[]{}, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                singleBook.bookID = Integer.parseInt(rs.getString("bookID"));
                singleBook.bookname = rs.getString("bookname");
                try{
                    rs.getString("SBtype");
                    singleBook.SBtype = true;
                    singleBook.oriprice = Double.parseDouble(rs.getString("oriprice"));
                }catch(Exception e){
                    singleBook.SBtype = false;
                }
                singleBook.picURL = "img/" + rs.getString("bookID")+".jpg";
                singleBook.curprice = Double.parseDouble(rs.getString("curprice"));
                singleBook.intro = rs.getString("intro");
                singleBook.category = rs.getString("category");
                singleBook.link = rs.getString("link");
            }
        });
        if(singleBook.SBtype) condition="出售书籍";
        model.addAttribute("condition", condition);
        model.addAttribute("book", singleBook);
        return "singleBook";
    }

    @RequestMapping("makeTrade")
    public String makeTrade(@CookieValue("account") String account, Model model, @RequestParam String bookID) {
        Trade trade = new Trade();
        trade.isdeal = false;
        String tem = "select buy,sale from trade where bookID="+bookID;
        sql.query(tem, new Object[]{}, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                if(rs.getString("buy") == null)
                    Trade.addbuy(sql, account, Integer.parseInt(bookID));
                else if(rs.getString("sale") == null)
                    Trade.addsale(sql, account, Integer.parseInt(bookID));
                else
                    trade.isdeal = true;
            }
        });
        model.addAttribute("success", !trade.isdeal);
        model.addAttribute("fail", trade.isdeal);
        return trade(account, model);
    }

    @RequestMapping("confirm")
    public String Confirm(@CookieValue("account") String account, Model model, @RequestParam String bookID) {
        Trade trade = new Trade();
        sql.query("select * from trade where bookID = "+bookID, new Object[]{}, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                trade.buy = rs.getString("buy");
                trade.sale = rs.getString("sale");
                trade.isdeal = rs.getBoolean("isdeal");
            }
        });
        if(trade.buy != null && trade.sale != null && trade.isdeal == false) {
            sql.update("update trade set isdeal=? where bookID=?", new Object[]{true, bookID});
            sql.update("update book set isdeal=? where bookID=?", new Object[]{true, bookID});
            model.addAttribute("confirm", true);
        }
        else if(trade.isdeal == true) {
            model.addAttribute("alreadyconfirm", true);
        }
        else
            model.addAttribute("errorconfirm", true);
        return trade(account, model);
    }

    @RequestMapping("deleteTrade")
    public String deleteTrade(@CookieValue("account") String account, Model model, @RequestParam String bookID) {
        Trade trade = new Trade();
        sql.query("select * from trade where bookID = "+bookID, new Object[]{}, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                trade.buy = rs.getString("buy");
                trade.sale = rs.getString("sale");
                trade.isdeal = rs.getBoolean("isdeal");
            }
        });
        if(trade.buy == null || trade.sale == null) {
            sql.update("delete from book where bookID = "+bookID);
            sql.update("delete from trade where bookID = "+bookID);
            model.addAttribute("deletesuccess", true);
        }
        else {
            model.addAttribute("cannotdelete", true);
        }
        return trade(account, model);
    }
}