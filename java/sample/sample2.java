package sample;

import reflection_test.Stage;
import reflection_test.InputStage;
import reflection_test.ToRun;
import reflection_test.Pipeline;

@Stage
public class sample2{
    @InputStage
    private sample1 x;

    public String value;
    public int y = 100;

    @ToRun
    public void pogela(){
        this.value = x.value + ": this is sample 2";
        System.out.println(this.value);
        System.out.println("sample2's num is " + this.y);
    }
}