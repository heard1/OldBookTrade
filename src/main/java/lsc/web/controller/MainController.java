package lsc.web.controller;

import lsc.web.Book;
import lsc.web.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;


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
    public String postlogin(Model model, @RequestParam String account, @RequestParam String password){
        if(User.login(sql, account, password))
            return "home";
        else {
            model.addAttribute("error", true);
            return "index";
        }
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
    public String salebook(Model model, @RequestParam String bookname, @RequestParam String category, @RequestParam double oriprice,
                           @RequestParam double curprice, @RequestParam String link, @RequestParam String intro, @RequestParam MultipartFile upload){
        if(Book.salebook(sql, bookID, bookname, category, oriprice, curprice, link, intro)) {
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
    public String buybook(Model model, @RequestParam String bookname, @RequestParam String category, @RequestParam double curprice,
                            @RequestParam String link, @RequestParam String intro, @RequestParam MultipartFile upload){
        if(Book.buybook(sql, bookID, bookname, category, curprice, link, intro)) {
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
    public String trade() {
        return "trade";
    }

    @RequestMapping("home.html")
    public String home(Model model) {
        ArrayList<Book> books = new ArrayList<>();
        for(int i=0; i<10; i++) {
            books.add(new Book());
        }
        model.addAttribute("books", books);
        return "home";
    }
}