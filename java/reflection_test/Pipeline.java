package reflection_test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.Callable;

import java.net.URL;
import java.io.File;


public class Pipeline{

    public static void run(Class<?> c, String[] args){
        System.out.println("start!!!");
        scan_stages(c);
        System.out.println("scan stage done.");
        resolve_dependencies();
        System.out.println("resolve dep done.");
        inject_dependencies();
        System.out.println("inject dep done.");
        run_stages();
        System.out.println("all done.");
    }

    // list of stages (each defined as class)
    private final static Map<Class<?>, Object> stage_map = new HashMap<>();

    private static List<Class<?>> listup_stages(String package_name){
        String package_path = package_name.replace('.', '/');
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL root = cl.getResource(package_path);

        File[] files = new File(root.getFile()).listFiles((dir, name) -> name.endsWith(".class"));

        return Arrays.asList(files).stream()
            .map(file -> package_name + "." + file.getName().replaceAll(".class$", ""))
            .map(c_name -> uncheck(() -> Class.forName(c_name)))
            //.map(c_name -> Class.forName(c_name))
            .collect(Collectors.toList());
    }

    private static void scan_stages(Class<?> c){
        List<Class<?>> classes = listup_stages(c.getPackage().getName());
        classes.stream()
            .filter(cc -> cc.isAnnotationPresent(Stage.class))
            .forEach(cc -> uncheck(() -> {
                Object instance = cc.newInstance();
                stage_map.put(cc, instance);
            }));
            /*.forEach(cc -> {
                Object instance = cc.newInstance();
                stage_map.put(cc, instance);
            });*/
        System.out.println("Annotated stages: " + stage_map);
    }



    private final static List<Class<?>> solved_stages = new ArrayList<>();
    // just for parallel execution
    private final static List<List<Class<?>>> stepped_stages = new ArrayList<>();

    private static void resolve_dependencies(){
        // initialize
        Set<Class<?>> keys = stage_map.keySet().stream().filter(c->true).collect(Collectors.toSet());

        // naive
        while(!keys.isEmpty()){
            List<Class<?>> nexts = keys.stream()
                .filter(c ->
                    Arrays.asList(c.getDeclaredFields()).stream()
                        .allMatch(field -> !(field.isAnnotationPresent(InputStage.class)) || solved_stages.contains(field.getType())))
                .collect(Collectors.toList());
            stepped_stages.add(nexts);
            solved_stages.addAll(nexts);
            keys.removeAll(nexts);
        }
        
    }

    private static void inject_dependencies(){
        solved_stages.forEach(c -> {
            Object insta = stage_map.get(c);
            Arrays.asList(c.getDeclaredFields()).stream()
                .filter(field -> field.isAnnotationPresent(InputStage.class))
                .forEach(field -> uncheck(()->{
                    field.setAccessible(true);
                    field.set(insta, stage_map.get(field.getType()));
                }));
                /*.forEach(field -> {
                    field.setAccessible(true);
                    field.set(insta, stage_map.get(field.getType()));
                });*/
        });
    }

    // naive and hacky
    private static void run_stages(){
        solved_stages.forEach(c -> {
            Object insta = stage_map.get(c);
            Arrays.asList(c.getDeclaredMethods()).stream()
                .filter(m -> m.isAnnotationPresent(ToRun.class))
                // not considering the order of ToRuns...
                .forEach(m -> uncheck(() -> {
                    m.setAccessible(true);
                    // assume no argument is needed
                    // no check
                    m.invoke(insta);
                }));
                /*.forEach(m -> {
                    m.setAccessible(true);
                    m.invoke(insta);
                });*/
        });
    }
    
    private static <T> T uncheck(Callable<T> callable){
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static void uncheck(ThrowsRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    private static interface ThrowsRunnable {
        void run() throws Exception;
    }
}