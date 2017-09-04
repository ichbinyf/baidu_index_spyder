package JsonPointHelper;

import JsonHelper.JsonsRootBean;
import com.alibaba.fastjson.JSON;

/**
 * Created by n on 2017/8/23.
 */
public class JsonPointParser {
    private JsonsPointBean jpb ;

    public JsonPointParser(String text) {
        this.jpb = JSON.parseObject(text, JsonsPointBean.class);;
    }

    public JsonsPointBean getJpb() {
        return jpb;
    }

    public void setJpb(JsonsPointBean jpb) {
        this.jpb = jpb;
    }
}
