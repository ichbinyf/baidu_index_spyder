package JsonHelper;

import java.util.List;

/**
 * Created by n on 2017/8/23.
 */
public class Data {

    private List<All> all;
    private List<Pc> pc;
    private List<Wise> wise;
    public void setAll(List<All> all) {
        this.all = all;
    }
    public List<All> getAll() {
        return all;
    }

    public void setPc(List<Pc> pc) {
        this.pc = pc;
    }
    public List<Pc> getPc() {
        return pc;
    }

    public void setWise(List<Wise> wise) {
        this.wise = wise;
    }
    public List<Wise> getWise() {
        return wise;
    }

}