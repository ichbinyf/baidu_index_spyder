/**
 * Created by n on 2017/8/22.
 */

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;


public class PTJSApp {

    public static WebDriver  initWebDriver(){
        String ptjsPath = System.getProperty("user.dir")+ File.separator+"phantomjs-2.1.1-windows"
                +File.separator+"bin"+File.separator+"phantomjs.exe" ;
        JobLogFactory.getLogger().log(Level.INFO,"ptjsPath:"+ptjsPath,PTJSApp.class);

        DesiredCapabilities caps = DesiredCapabilities.phantomjs();
        caps.setJavascriptEnabled(true);
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,ptjsPath);
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_CUSTOMHEADERS_PREFIX
                + "User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:25.0) Gecko/20100101 Firefox/25.0 " );

        WebDriver driver = new PhantomJSDriver(caps);
        driver.get("http://index.baidu.com");
        return driver ;
    }

    public static int login(WebDriver driver){
        ArrayList<String> baiduAccount = CommonHelper.getBaiduUserPwd();
        if(baiduAccount.size() == 2){
            String user = baiduAccount.get(0);
            String pwd = baiduAccount.get(1) ;
            int rt = login(driver,user,pwd);
            if(rt == 1 ){
                saveCookies(driver);
            }
            return 1 ;
        }else{
            return -1 ;
        }
    }

    public static int login(WebDriver driver , String username , String pwd ){
        try {
            JobLogFactory.getLogger().log(Level.INFO,"try to login with username and password",PTJSApp.class) ;
            ((JavascriptExecutor) driver).executeScript("BID.popLogin()");
            Thread.sleep(1000);
            int retries = 10 ;
            while (retries -- > 0 ){
                if(driver.findElements(By.id("TANGRAM_12__submit")).size() >= 1 )
                    break;
            }
            WebElement weUser = driver.findElement(By.id("TANGRAM_12__userName"));
            WebElement wePwd = driver.findElement(By.id("TANGRAM_12__password"));
            WebElement weSmt = driver.findElement(By.id("TANGRAM_12__submit"));
            weUser.sendKeys(username);
            wePwd.sendKeys(pwd);
            Thread.sleep(1000);
            weSmt.click();
            Thread.sleep(1000);
            driver.get("http://index.baidu.com");
            Thread.sleep(1000);
        }catch(Exception e){
            JobLogFactory.getLogger().log(Level.SEVERE,"try to login fail" , PTJSApp.class) ;
            JobLogFactory.getLogger().log(Level.SEVERE,e.getMessage() , PTJSApp.class) ;
            return 0 ;
        }
        return 1 ;
    }

    public static String getRes(WebDriver driver){
        String rt = (String)((JavascriptExecutor) driver).executeScript("return PPval.ppt;");
        return rt ;
    }
    public static String getRes2(WebDriver driver){
        String rt = (String)((JavascriptExecutor) driver).executeScript("return PPval.res2;");
        return rt ;
    }

    public static int clearCookies(WebDriver driver){
        driver.manage().deleteAllCookies();
        return 1 ;
    }

    public static int takeScreenShot(WebDriver driver , String picName){
        try {
            JobLogFactory.getLogger().log(Level.INFO,"try to take screenshot" , PTJSApp.class);
            File srcFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(srcFile, new File(System.getProperty("user.dir")+File.separator+picName));
        }catch(Exception e){
            JobLogFactory.getLogger().log(Level.SEVERE,e.getMessage(),PTJSApp.class);
            return 0 ;
        }
        return 1 ;
    }

    public static String getCookieStr(){
        String cookieStr = "";
        String cookiePath = System.getProperty("user.dir") + File.separator + "cookies.txt" ;
        try {
            File fr = new File(cookiePath);
            if (fr.exists()) {
                InputStreamReader reader = new InputStreamReader(
                        new FileInputStream(fr));
                BufferedReader br = new BufferedReader(reader);
                String line = "";
                line = br.readLine();
                while (line != null) {
                    cookieStr = cookieStr+line.split("\t")[0] + "=" + line.split("\t")[1] + ";" ;
                    line = br.readLine();
                }
            }
        }catch(Exception e){
            JobLogFactory.getLogger().log(Level.SEVERE,e.getMessage(),PTJSApp.class) ;
            return "";
        }
        cookieStr = cookieStr + ";;" ;
        cookieStr = cookieStr.replace(";;;" , "");
        return cookieStr;
    }
    public static int search(WebDriver driver , String keyword){

        WebElement weWord = driver.findElement(By.id("schword"));
        WebElement weSearch = driver.findElement(By.id("searchWords"));
        weWord.sendKeys(keyword);
        weSearch.click();

        return 1 ;
    }


    public static int setCookies(WebDriver driver){
        String cookiePath = System.getProperty("user.dir") + File.separator + "cookies.txt" ;
        ArrayList<Cookie> cookieArrayList = new ArrayList<Cookie>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1);

        try {
            File fr = new File(cookiePath);
            if (fr.exists()) {
                InputStreamReader reader = new InputStreamReader(
                        new FileInputStream(fr));
                BufferedReader br = new BufferedReader(reader);
                String line = "";
                line = br.readLine();
                while (line != null) {
                    Cookie cookie = new Cookie(line.split("\t")[0], line.split("\t")[1], line.split("\t")[2]
                            ,  line.split("\t")[3] , calendar.getTime() );
                    cookieArrayList.add(cookie);
                    line = br.readLine();
                }
            }else{
                return -1 ;
            }
        }catch(Exception e){
            JobLogFactory.getLogger().log(Level.SEVERE,e.getMessage() , PTJSApp.class) ;
            return 0 ;
        }

        driver.get("http://index.baidu.com");
        for(Cookie cookie:cookieArrayList) {
            try{
                driver.manage().addCookie(cookie);
            }catch(Exception e){
                JobLogFactory.getLogger().log(Level.SEVERE,e.getMessage() , PTJSApp.class) ;
            }
        }

        try {
            Thread.sleep(1000);
            driver.get(driver.getCurrentUrl());
            Thread.sleep(1000);
        }catch(Exception e){
            JobLogFactory.getLogger().log(Level.SEVERE,e.getMessage() , PTJSApp.class) ;
        }
        return 1 ;
    }
    public static int saveCookies(WebDriver driver){
        driver.get(driver.getCurrentUrl()) ;
        String cookiePath = System.getProperty("user.dir") + File.separator + "cookies.txt" ;
        try {
            File fw = new File(cookiePath);
            if (!fw.exists() ) {
                fw.createNewFile();
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(fw));

            Set<Cookie> cookies = driver.manage().getCookies();
            for (Cookie cookie : cookies) {
                out.write(cookie.getName());
                out.write("\t");
                out.write(cookie.getValue());
                out.write("\t");
                out.write(cookie.getDomain());
                out.write("\t");
                out.write(cookie.getPath());
                out.write("\n");
                JobLogFactory.getLogger().log(Level.INFO, "cookie:"+cookie.getName()+"\t"+cookie.getValue()
                        +"\t"+cookie.getDomain()+"\t"+cookie.getPath()+"\n"  , PTJSApp.class);
            }
            out.flush();
            out.close();
        }catch(Exception e){
            JobLogFactory.getLogger().log(Level.SEVERE,e.getMessage()) ;
        }
        return 1 ;
    }

    public static int refresh(WebDriver driver){
        driver.get(driver.getCurrentUrl());
        try {
            JobLogFactory.getLogger().log(Level.INFO,"refresh ... " , PTJSApp.class);
            Thread.sleep(1000);
        }catch(Exception e){
            JobLogFactory.getLogger().log(Level.SEVERE,e.getMessage() , PTJSApp.class) ;
            return 0 ;
        }
        return 1 ;
    }

    public static int quitWebDriver(WebDriver driver){
        driver.quit();
        return 1 ;
    }
}
