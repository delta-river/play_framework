package sample;

import reflection_test.Stage;
import reflection_test.InputStage;
import reflection_test.ToRun;
import reflection_test.Pipeline;

@Stage
public class sample1{
    public String value = "this is sample 1";

    @ToRun
    public void piyo(){
        System.out.println(value);
    }

}