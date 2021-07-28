package sample;

import reflection_test.Stage;
import reflection_test.InputStage;
import reflection_test.ToRun;
import reflection_test.Pipeline;

public class sample_main{
    public static void main(String[] args){
        Pipeline.run(sample_main.class, args);
    }
}