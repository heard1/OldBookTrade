package lsc.web.controller;

import lsc.web.Book;
import lsc.web.MyTrade;
import lsc.web.adminTrade;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Controller
public class MainController {

    // 操作数据库
    @Autowired
    private JdbcTemplate sql;
    // 初始化book ID
    int bookID = 0;
    // 初始化登录界面
    @RequestMapping(value={"/index","/"})
    public String login() {
        return "index";
    }

    // 登录操作
    @PostMapping("/postlogin")
    public String postlogin(HttpServletResponse response, Model model, @RequestParam String account, @RequestParam String password){
        if(User.login(sql, account, password)) {
            // save the cookie of an account
            Cookie cookie=new Cookie("account", account);
            response.addCookie(cookie);
            if(account.equals("administrator"))
                return "admin";
            return home(model);
        }
        else {
            model.addAttribute("error", true);
            return "index";
        }
    }
    // 查看欲购书籍的列表
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
    // 登录界面
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
    // 管理员界面
    @RequestMapping("admin.html")
    public String admin() {
        return "admin";
    }
    // 管理员查找账户
    @RequestMapping("Ausersearch")
    public String Ausersearch(Model model, @RequestParam String account) {
        List<adminTrade> trades = new ArrayList<>();
        String tem = "select * from book natural join trade where sale = ? or buy = ?";
        sql.query(tem, new Object[]{account,account}, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                adminTrade single = new adminTrade();
                single.bookID = rs.getString("bookID");
                single.bookname = rs.getString("bookname");
                if(rs.getBoolean("isdeal"))
                    single.isdeal = "是";
                else
                    single.isdeal = "否";
                if(rs.getBoolean("SBtype"))
                    single.SBtype = "卖方";
                else
                    single.SBtype = "买方";
                single.price = rs.getDouble("curprice");
                single.category = rs.getString("category");
                String sale = rs.getString("sale");
                String buy = rs.getString("buy");
                if(sale.equals(account)) {
                    single.obj = buy;
                    single.username = User.findusername(sql, sale);
                }
                else {
                    single.obj = sale;
                    single.username = User.findusername(sql, buy);
                }
                trades.add(single);
            }
        });
        model.addAttribute("account", account);
        model.addAttribute("trades", trades);
        return "admin";
    }
    // 管理员查找书籍
    @RequestMapping("Abooksearch")
    public String Abooksearch(Model model, @RequestParam String bookname) {
        List<adminTrade> tradesB = new ArrayList<>();
        String tem = "select * from book natural join trade where bookname = ?";
        sql.query(tem, new Object[]{bookname}, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                adminTrade single = new adminTrade();
                single.bookID = rs.getString("bookID");
                single.bookname = rs.getString("bookname");
                single.isdeal = rs.getString("isdeal");
                if(rs.getBoolean("SBtype"))
                    single.SBtype = "卖方";
                else
                    single.SBtype = "买方";
                single.price = rs.getDouble("curprice");
                single.category = rs.getString("category");
                single.sale = rs.getString("sale");
                single.buy = rs.getString("buy");
                if(rs.getBoolean("isdeal"))
                    single.isdeal = "是";
                else
                    single.isdeal = "否";
                tradesB.add(single);
            }
        });
        model.addAttribute("bookname", bookname);
        model.addAttribute("tradesB", tradesB);
        return "admin";
    }
    // 管理员删除数据
    @RequestMapping("AdeleteTrade")
    public String AdeleteTrade(@CookieValue("account") String account, Model model, @RequestParam String bookID) {
        Trade trade = new Trade();
        sql.update("delete from book where bookID = "+bookID);
        sql.update("delete from trade where bookID = "+bookID);
        model.addAttribute("success", true);
        return "admin";
    }

    // 发布购买书籍信息
    @RequestMapping("buy.html")
    public String buy() {
        return "buy";
    }
    // 发布卖出书籍信息
    @RequestMapping("sale.html")
    public String sale() {
        return "sale";
    }
    // 卖出书籍post信息
    @PostMapping("/salebook")
    public String salebook(@CookieValue("account") String account, Model model, @RequestParam String bookname, @RequestParam String category, @RequestParam double oriprice,
                           @RequestParam double curprice, @RequestParam String link, @RequestParam String intro, @RequestParam MultipartFile upload){
        if(Book.salebook(sql, bookID, bookname, category, oriprice, curprice, link, intro)) {
            Trade.addTrade(sql, account, null, bookID);
            String newfileloc = System.getProperty("user.dir")+"/src/main/resources/static/img/"+bookID+".jpg";
            File newFile = new File(newfileloc);
            String newfileloc1 = System.getProperty("user.dir")+"/target/classes/static/img/"+bookID+".jpg";
            File newFile1 = new File(newfileloc1);
            try{
                upload.transferTo(newFile);
                upload.transferTo(newFile1);
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
    // 买入书籍信息录入
    @PostMapping("/buybook")
    public String buybook(@CookieValue("account") String account, Model model, @RequestParam String bookname, @RequestParam String category, @RequestParam double curprice,
                            @RequestParam String link, @RequestParam String intro, @RequestParam MultipartFile upload){
        if(Book.buybook(sql, bookID, bookname, category, curprice, link, intro)) {
            Trade.addTrade(sql, null, account, bookID);
            String newfileloc = System.getProperty("user.dir")+"/src/main/resources/static/img/"+bookID+".jpg";
            File newFile = new File(newfileloc);
            String newfileloc1 = System.getProperty("user.dir")+"/target/classes/static/img/"+bookID+".jpg";
            File newFile1 = new File(newfileloc1);
            try{
                upload.transferTo(newFile);
                upload.transferTo(newFile1);
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
    // 返回查询界面
    @RequestMapping("search.html")
    public String search(Model model) {
        return "search";
    }
    // 查看交易
    @RequestMapping("trade.html")
    public String trade(@CookieValue("account") String account, Model model) {
        String tem = "select * from trade natural join book where sale = ? or buy = ?";
        List<MyTrade> trades = new ArrayList<MyTrade>();
        sql.query(tem, new Object[]{account, account}, new RowCallbackHandler() {
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
            String temsql = "select username from user where account = ?";
            sql.query(temsql, new Object[]{temp.tradeObject}, new RowCallbackHandler() {
                public void processRow(ResultSet rs) throws SQLException {
                    temp.tradeObjectName = rs.getString("username");
                }
            });
        }
        model.addAttribute("trades", trades);
        return "trade";
    }
    // 返回书籍首页
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
    // 单个书籍的详细信息
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
        if(singleBook.SBtype) {
            condition="出售书籍";
            model.addAttribute("obj", User.findaccount(sql, true, singleBook.bookID));
        }
        model.addAttribute("condition", condition);
        model.addAttribute("book", singleBook);
        return "singleBook";
    }
    // 加入交易
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
    // 确认交易
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
    // 删除交易
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
    // 查找书籍
    @RequestMapping("searchbook")
    public String SearchBook(Model model, @RequestParam String info) {
        List<Book> books = Book.search(sql, info);
        model.addAttribute("books", books);
        return search(model);
    }
    // 聊天界面
    @RequestMapping("chat.html")
    public String chat(@CookieValue("account") String account, Model model) {
        List<User> users = new ArrayList<User>();
        String tem = "select * from message where accountto=? or accountfrom=?";
        sql.query(tem, new Object[]{account, account}, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                User singleUser = new User();
                String tem1 = rs.getString("accountto");
                String tem2 = rs.getString("accountfrom");
                if(account == tem1)
                    singleUser.account = tem2;
                else
                    singleUser.account = tem1;
                singleUser.username = User.findusername(sql, singleUser.account);
                users.add(singleUser);
            }
        });
        Set<User> Fusers = new HashSet<>();
        Set<String> useraccount = new HashSet<>();
        for(User user : users) {
            if(!useraccount.contains(user.account) && !user.account.equals(account)) {
                //System.out.println(user.account+" "+account);
                Fusers.add(user);
                useraccount.add(user.account);
            }
        }
        model.addAttribute("users", Fusers);
        return "chat";
    }
    // 单个对象一对一聊天
    @RequestMapping("singleChat.html")
    public String singleChat(@CookieValue("account") String account, Model model, @RequestParam String ID) {
        List<String> sentences = new ArrayList<String>();
        String tem = "select * from message where accountto=? and accountfrom=? or accountto=? and accountfrom=?";
        sql.query(tem, new Object[]{account,ID,ID,account}, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                String from = rs.getString("accountfrom");
                String to = rs.getString("accountto");
                String mess = rs.getString("mess");
                String addsen = User.findusername(sql, from)+" tells "+User.findusername(sql, to)+": "+mess;
                sentences.add(addsen);
            }
        });
        model.addAttribute("obj", ID);
        model.addAttribute("sentences", sentences);
        return "singleChat";
    }
    // 发送信息
    @RequestMapping("message")
    public String message(@CookieValue("account") String account, Model model, @RequestParam String mess, @RequestParam String sentto) {
        sql.update("insert into message (accountfrom, accountto, mess) values(?,?,?)",new Object[] {account, sentto, mess});
        return singleChat(account, model, sentto);
    }
}