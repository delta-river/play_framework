package sample;

import reflection_test.Stage;
import reflection_test.InputStage;
import reflection_test.ToRun;
import reflection_test.Pipeline;


@Stage
public class sample3{
    @InputStage
    private sample2 x;

    public String value;
    public int y = -100;

    @ToRun
    public void hoge(){
        this.value = x.value + ": this is sample 3";
        this.y = x.y + this.y;
        System.out.println(this.value);
        System.out.println("sample3's num is " + this.y);
    }
}