
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.SystemClock;
import sun.applet.Main;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.logging.Level;


public class MainSpyder {

    public static void doSpyder(String startParam , String endParam  , WebDriver obj , String keyword ){
        String keywordStrUtf8 = "" ;
        try {
            keywordStrUtf8 = URLEncoder.encode(keyword, "GBK");

            int retries = 5 ;
            while(retries -- > 0 ){
                obj.get("http://index.baidu.com/?tpl=trend&word="+keywordStrUtf8);
                Thread.sleep(1000);
                String res = PTJSApp.getRes(obj) ;
                String res2 = PTJSApp.getRes2(obj) ;
                JobLogFactory.getLogger().log(Level.INFO , String.format("res=%s res2=%s", res , res2 )
                        , MainSpyder.class);
                if(res.length() > 0 && res2.length() > 4){
                    break;
                }
            }

            PTJSApp.takeScreenShot(obj,"screenshot_3_when_doSpyder.jpg");
            if( CommonHelper.str2Date(startParam).before( CommonHelper.str2Date("2011-01-01") ) ){
                if( CommonHelper.str2Date("2010-12-31").after( CommonHelper.str2Date(endParam) ) ){
                    doSpyderPart( startParam , endParam , "pc" , obj , keyword ) ;
                }else {
                    doSpyderPart( startParam, "2010-12-31", "pc", obj, keyword);
                    doSpyderPart( "2011-01-01" , endParam , "all" , obj , keyword ) ;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void doSpyderPart(String startParam , String endParam , String typeParam , WebDriver obj
            , String keyword ){

        ArrayList <String> dateStrList = CommonHelper.getDateBetween( startParam , endParam );
        int start = 0 ;
        int skip = 180 ;
        int end = dateStrList.size();
        while( start < end){
            String startDateStr = dateStrList.get(start) ;
            String endDateStr ;
            if(start + skip >= end -1 ){
                endDateStr = dateStrList.get( dateStrList.size()-1 ) ;
            }else{
                endDateStr = dateStrList.get(start + skip) ;
            }
            try{
                PTJSApp.saveCookies(obj);
            }catch(Exception e){
                e.printStackTrace();
            }
            String encListStr = URLConn.getEncList(obj, startDateStr , endDateStr ,typeParam , keyword) ;
            JobLogFactory.getLogger().log(Level.INFO, String.format("getEncList %s %s %s %s" , startDateStr
                    , endDateStr ,typeParam , keyword) , MainSpyder.class);
            int encListIndex = 0 ;
            for(String res3:encListStr.split(",")){
                try {
                    String pointStr = URLConn.handlePoint(obj, res3, dateStrList.get(encListIndex+start)) ;
                    JobLogFactory.getLogger().log(Level.INFO,String.format("handlePoint %s done"
                            , dateStrList.get(encListIndex+start)), MainSpyder.class) ;
                    if (pointStr.length() > 2) {
                        CommonHelper.appendData(keyword + "\t" + pointStr);
                    }
                }catch (Exception e){
                    JobLogFactory.getLogger().log(Level.INFO,String.format("error when handlePoint %s "
                            , dateStrList.get(encListIndex+start) ), MainSpyder.class);
                    e.printStackTrace();
                }
                encListIndex ++ ;
            }
            start += skip + 1 ;
        }
    }


    public static void main(String[] args){

        JobLogFactory.init();
        JobLogFactory.getLogger().log(Level.INFO,"logger init done,try to init WebDriver", MainSpyder.class);

        int checkDirResult = CommonHelper.checkDir() ;
        if ( checkDirResult == 0 ){
            JobLogFactory.getLogger().log(Level.INFO,"cannot find temp directory , create directory done"
                    , MainSpyder.class);
        }
        if ( CommonHelper.checkPhantomJS() == -1 ){
            JobLogFactory.getLogger().log(Level.INFO,"cannot find PhantomJS directory , " +
                    "you need to get phantomjs-2.1.1-windows first", MainSpyder.class);
            return ;
        }

        //inti Selenium PhantomJS WebDriver
        String keyword = "";
        WebDriver obj = PTJSApp.initWebDriver() ;
        JobLogFactory.getLogger().log(Level.INFO,"WebDriver init done",MainSpyder.class);

        //do login or set cookie  , and then refresh
        PTJSApp.clearCookies(obj);
        obj.get("http://index.baidu.com");
        PTJSApp.takeScreenShot(obj , "screenshot_1_before_setcookie.jpg");
        if( PTJSApp.setCookies(obj) == -1){
            if( PTJSApp.login(obj) == -1){
                JobLogFactory.getLogger().log(Level.INFO,"fail to login or get cookies.txt !!!!!", MainSpyder.class);
                return ;
            }
        }
        obj.get("http://index.baidu.com");
        PTJSApp.takeScreenShot(obj , "screenshot_2_after_setcookie.jpg");

        //get keywords and begin collecting data
        ArrayList<String> kwList = CommonHelper.getKeywords();
        for(String keyword1:kwList ) {
            doSpyder("2008-01-01", "2017-08-10", obj, keyword1);
        }

        //quit PhantomJS or you get Process PhantomJS still running background
        PTJSApp.quitWebDriver(obj) ;

    }
}
