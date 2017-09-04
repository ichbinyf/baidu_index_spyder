import java.io.*;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;

/**
 * Created by n on 2017/8/23.
 *
 */
public class CommonHelper {

    /*
     * check temp directory , if not exists then create and return 0
     */
    public static int checkDir(){
        String tempDirPath = System.getProperty("user.dir") + File.separator + "temp" ;
        File tempDir = new File(tempDirPath) ;
        if( tempDir.exists() ){
            if( tempDir.isDirectory() ){
                return 1 ;
            }
        }
        tempDir.mkdir() ;
        return 0 ;
    }

    /*
     * check PhantomJS directory , if not exists then return -1
     * should be phantomjs-2.1.1-windows , other version has not been tested yet
     */
    public static int checkPhantomJS(){
        String phantomJSDirPath = System.getProperty("user.dir") + File.separator + "phantomjs-2.1.1-windows" ;
        File tempDir = new File(phantomJSDirPath) ;
        if( tempDir.exists() ){
            if( tempDir.isDirectory() ){
                return 1 ;
            }
        }
        return -1 ;
    }

    /*
     * read baidu user account and password from "user.txt"
     * first line is user name
     * second line is password
     */
    public static ArrayList<String> getBaiduUserPwd(){
        ArrayList<String> rt = new ArrayList<String>();

        String kwPath = System.getProperty("user.dir") + File.separator + "user.txt" ;
        try {
            JobLogFactory.getLogger().log(Level.INFO,"try to getBaiduUserPwd",CommonHelper.class);
            File fr = new File(kwPath);
            if (fr.exists()) {
                InputStreamReader reader = new InputStreamReader(
                        new FileInputStream(fr) , "GBK" );
                BufferedReader br = new BufferedReader(reader);
                String line = "";
                line = br.readLine(); //user
                if (line != null) {
                    String kw = line.trim().replace("\r","").replace("\n","");
                    rt.add(kw);
                    line = br.readLine();//pwd
                }
                if (line != null) {
                    String kw = line.trim().replace("\r","").replace("\n","");
                    rt.add(kw);
                }
            }
        }catch(Exception e){
            JobLogFactory.getLogger().log(Level.INFO,"getBaiduUserPwd fail"+e.getMessage(),CommonHelper.class) ;
            e.printStackTrace();
            return rt;
        }
        return rt ;
    }

    /*
     * read keyword.txt
     * each line as a keyword
     *
     * this txt file better be created using windows notepad
     * or else you will entercounter encoding errors
     */
    public static ArrayList<String> getKeywords(){
        ArrayList<String> rt = new ArrayList<String>();

        String kwPath = System.getProperty("user.dir") + File.separator + "keyword.txt" ;
        try {
            File fr = new File(kwPath);
            if (fr.exists()) {
                InputStreamReader reader = new InputStreamReader(
                        new FileInputStream(fr) , "GBK" );
                BufferedReader br = new BufferedReader(reader);
                String line = "";
                line = br.readLine();
                while (line != null) {
                    String kw = line.trim().replace("\r","").replace("\n","");
                    if( kw.length() >= 1)rt.add(kw);
                    line = br.readLine();
                }
            }
        }catch(Exception e){
            JobLogFactory.getLogger().log(Level.INFO,e.getMessage() , CommonHelper.class) ;
            return rt;
        }
        return rt ;
    }

    /*
     * save a index point data to the end of the "result.txt" file
     * each point as one line
     */
    public static void appendData(String dataStr){
        String fileFullPath = System.getProperty("user.dir") + File.separator + "result.txt";
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileFullPath, true);
            fos.write(dataStr.getBytes("GBK"));

        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(fos != null){
                try {
                    fos.flush();
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static Date str2Date(String dateStr){
        try {
            SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateInstance();
            Date rtDate = df.parse(dateStr);
            return rtDate ;
        }catch (Exception e){
            e.printStackTrace();
        }
        return new Date();
    }

    public static String checkCoding(String str){
        String type = "utf-8";
        try {
            if (str.equals(new String(str.getBytes(type ), type ))) {
                JobLogFactory.getLogger().log(Level.INFO,"checkCoding "+ str, CommonHelper.class);
                JobLogFactory.getLogger().log(Level.INFO,"gbk urlencode="+ URLEncoder.encode(str, "GBK")
                        , CommonHelper.class);
                JobLogFactory.getLogger().log(Level.INFO,"utf-8 urlencode="+ URLEncoder.encode(str, "UTF-8")
                        , CommonHelper.class);
                return type;
            }
        } catch (Exception e) {
            return "not utf-8" ;
        }
        return "not anything";
    }

    /*
     * parse String like 2010-01-01 to Date
     */
    public static String date2Str(Date date){
        try {
            SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateInstance();
            String dateStr = df.format( date );
            return dateStr ;
        }catch (Exception e){
            e.printStackTrace();
        }
        return "2008-01-01" ;
    }

    /*
     * return a list of days between startDate and endDate
     */
    public static ArrayList<String> getDateBetween(String startDate , String endDate){
        ArrayList<String> rt = new ArrayList<String>();
        Date dNow = new Date();
        SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateInstance();
        try
        {
            df.applyPattern("yyyy-MM-dd");
            Date startDate1 = df.parse(startDate);
            Date endDate1 = df.parse(endDate);
            if( endDate1.before(startDate1) ){
                return rt ;
            }

            Calendar calendar   =   new GregorianCalendar();
            calendar.setTime(startDate1);
            while( !endDate.equals( df.format(calendar.getTime()) ) ) {
                rt.add(df.format(calendar.getTime()));
                calendar.add(calendar.DATE, 1);
            }
            rt.add(df.format(calendar.getTime()));
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rt;
    }
}
