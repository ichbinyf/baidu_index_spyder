import java.io.File;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.logging.*;

public class JobLogFactory {
    private static Logger logger ;
    public static Logger getLogger(){
        return Logger.getLogger("bdindex_spyder") ;
    }
    public static void init() {
        Logger log = Logger.getLogger("bdindex_spyder");
        log.setLevel(Level.INFO);
        Logger log1 = Logger.getLogger("bdindex_spyder");
        System.out.println(log==log1);
        ConsoleHandler consoleHandler =new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        log.addHandler(consoleHandler);

        try {
            FileHandler fileHandler = new FileHandler(System.getProperty("user.dir") + File.separator + "log.txt");
            fileHandler.setFormatter(new MyLogHander());
            fileHandler.setLevel(Level.INFO);
            log.addHandler(fileHandler);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

class MyLogHander extends Formatter {
    @Override
    public String format(LogRecord record) {
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//将毫秒级long值转换成日期格式
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(record.getMillis() );
        String dateStr = dateformat.format(gc.getTime());
        return "["+dateStr+"]<"+record.getLevel() + ">"+record.getSourceClassName()
                +"."+record.getSourceMethodName()+":" + record.getMessage()+"\r\n";
    }
}