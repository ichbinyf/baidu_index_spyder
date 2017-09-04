package JsonHelper;

import com.alibaba.fastjson.JSON;

/**
 * Created by n on 2017/8/23.
 */
public class JsonParser {

    public JsonsRootBean getJrb() {
        return jrb;
    }

    public void setJrb(JsonsRootBean jrb) {
        this.jrb = jrb;
    }

    private JsonsRootBean jrb ;
    public JsonParser(String text) {
        this.jrb = JSON.parseObject(text, JsonsRootBean.class);
    }
}
