import JsonHelper.JsonParser;
import JsonHelper.JsonsRootBean;
import JsonPointHelper.JsonPointParser;
import JsonPointHelper.JsonsPointBean;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.WebDriver;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

/**
 * Created by n on 2017/8/22.
 */
public class URLConn {

    private static String point_url = "http://index.baidu.com/Interface/IndexShow/show/" ;
    private static String points_url = "http://index.baidu.com/Interface/Search/getSubIndex/" ;

    public static String test(){
        JobLogFactory.getLogger().log(Level.INFO,"URLConn test running",URLConn.class);
        return "";
    }

    /*
     * request the point data ,download the image then crop and paste , finally ocr the image and return the number
     */
    public static String handlePoint( WebDriver driver ,String res3 , String dateString ){
        String res = PTJSApp.getRes(driver);
        String res2 = PTJSApp.getRes2(driver) ;
        String index_show_url = point_url+"?res="+res+"&res2="+res2+"&classType=1&res3[]="+res3+"&className=view-value&"
                + System.currentTimeMillis() ;
        String rt = URLConn.getURL(index_show_url , PTJSApp.getCookieStr());
        JsonPointParser jpp = new JsonPointParser(rt) ;
        JsonsPointBean jpb = jpp.getJpb() ;
        String htmlStr = jpb.getData().getCode().get(0) ;
        Document doc = Jsoup.parseBodyFragment(htmlStr);
        Element body = doc.body();
        Element stEle = body.select("style").first();
        String styleHtmlStr = stEle.html() ;
        int pos1 = styleHtmlStr.indexOf("(\"");
        int pos2 = styleHtmlStr.indexOf("\")");
        String numberCodeImgUrl = "http://index.baidu.com/"+styleHtmlStr.substring(pos1+3,pos2) ;

        JobLogFactory.getLogger().log(Level.INFO,"handlePoint ing  " + dateString , URLConn.class);
        JobLogFactory.getLogger().log(Level.INFO,"style html : " + styleHtmlStr , URLConn.class) ;
        JobLogFactory.getLogger().log(Level.INFO,"background image : " + numberCodeImgUrl , URLConn.class) ;
        String imgPath = System.getProperty("user.dir") + File.separator + "temp" + File.separator + dateString+".png" ;
        URLConn.downloadImg(numberCodeImgUrl , imgPath , PTJSApp.getCookieStr()) ;

        ArrayList<Integer> mlArr = new ArrayList<Integer>();
        ArrayList<Integer> wdArr = new ArrayList<Integer>();

        for(Element spanEle : body.select("span")){
            String wdStr = spanEle.attr("style").replace("width:","").replace("px","").replace(";","") ;
            String mlStr = spanEle.select("div").attr("style").replace("margin-left:","").replace("px","").
                    replace(";","")  ;
            mlArr.add( new Integer(mlStr) ) ;
            wdArr.add( new Integer(wdStr) ) ;
            JobLogFactory.getLogger().log(Level.INFO,"span element outer html :"+spanEle.outerHtml()  , URLConn.class) ;
            JobLogFactory.getLogger().log(Level.INFO,"width : "+wdStr+"  margin-left: "+mlStr  , URLConn.class) ;
        }
        int ocrResult = ImgHelper.genImage(imgPath, wdArr, mlArr);
        if(ocrResult != -1){
            JobLogFactory.getLogger().log(Level.INFO,String.format("%s\t%d\r\n" , dateString , ocrResult)
                    , URLConn.class);
            return String.format("%s\t%d\r\n" , dateString , ocrResult);
        }else{
            return "";
        }
    }


    /*
     * get point list using baidu index api with date , type , keyword params
     */
    public static String getEncList(WebDriver driver , String startDate , String endDate , String typeName
            , String keywordStr){
        try {
            keywordStr = URLEncoder.encode(keywordStr, "GBK");
        }catch (Exception e){
            e.printStackTrace();
        }

        String res = PTJSApp.getRes(driver);
        String res2 = PTJSApp.getRes2(driver) ;
        String all_index_url = points_url+ "?res="+res+"&res2="+res2+"&type=0&startdate="+startDate+"&enddate="
                + endDate + "&forecast=0&word="+keywordStr ;
        JobLogFactory.getLogger().log(Level.INFO,"getEncList:"+all_index_url,URLConn.class) ;
        String rt = URLConn.getURL(all_index_url , PTJSApp.getCookieStr());
        JobLogFactory.getLogger().log(Level.INFO,rt,URLConn.class);
        JsonParser jp = new JsonParser(rt);
        JsonsRootBean jrb = jp.getJrb() ;
        String encListStr;
        if(typeName.equals("all")) {
            encListStr = jrb.getData().getAll().get(0).getUserindexesEnc();
        }else{
            encListStr = jrb.getData().getPc().get(0).getUserindexesEnc();
        }
        JobLogFactory.getLogger().log(Level.INFO,"encListStr : "+encListStr ,URLConn.class);
        return encListStr ;
    }

    /*
     *
     * request url using URLConnection with UA and cookie headers , and unextract the gzip result
     */
    public static String getURL(String urlStr , String cookieStr){
        String rt = "" ;
        BufferedReader reader = null;
        StringBuffer sbf = new StringBuffer();
        try {
            URL url = new URL(urlStr);
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:25.0) Gecko/20100101 Firefox/25.0 ");
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            conn.setRequestProperty("Cookie", cookieStr);

            conn.connect();

            //InputStream is = conn.getInputStream();
            InputStream is = new GZIPInputStream(conn.getInputStream());
            reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
            //reader = new BufferedReader(new InputStreamReader(is, "gbk"));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
                sbf.append("\n");
            }
            reader.close();
            rt = sbf.toString();

        } catch (Exception e) {
            JobLogFactory.getLogger().log(Level.INFO,e.getMessage(), URLConn.class);
            e.printStackTrace();
        }
        return rt;
    }

    /*
     * download image with 30 seconds time-out and cookie headers
     */
    public static void downloadImg(String urlStr, String savePath , String cookieStr) {
        int bytesum = 0;
        int byteread = 0;
        try {
            URL url = new URL(urlStr);
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:25.0) Gecko/20100101 Firefox/25.0 ");
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            conn.setRequestProperty("Cookie", cookieStr);

            InputStream inStream = new GZIPInputStream(conn.getInputStream());
            //InputStream inStream = conn.getInputStream();
            FileOutputStream fs = new FileOutputStream(savePath );

            byte[] buffer = new byte[1204];
            int length;
            while ((byteread = inStream.read(buffer)) != -1) {
                bytesum += byteread;
                // System.out.println(bytesum);
                fs.write(buffer, 0, byteread);
            }
            inStream.close();
            fs.close();
        } catch (Exception e) {
            JobLogFactory.getLogger().log(Level.INFO,e.getMessage(),URLConn.class);
            e.printStackTrace();
        }
    }
}
