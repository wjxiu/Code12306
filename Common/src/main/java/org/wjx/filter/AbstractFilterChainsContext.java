package org.wjx.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author xiu
 * @create 2023-11-20 19:24
 */
@Slf4j
public class AbstractFilterChainsContext implements CommandLineRunner {
    private final ApplicationContext applicationContext;

    HashMap<String, List<AbstractFilter>> map=new HashMap<>();
    @Autowired
    public AbstractFilterChainsContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    @Override
    public void run(String... args) throws Exception {
        Map<String, AbstractFilter> beans = applicationContext.getBeansOfType(AbstractFilter.class);
        beans.forEach((k,v)->{
            AbstractFilter abstractFilter = beans.get(k);
            String mark = abstractFilter.mark();
            List<AbstractFilter> list = map.getOrDefault(mark, new ArrayList<>());
            list.add(abstractFilter);
            map.put(mark,list);
        });
        map.forEach((k,v)->{
            List<AbstractFilter> abstractFilters = map.get(k);
            abstractFilters.sort((filter, filter1) -> filter.getOrder()-filter1.getOrder());
        });
        log.info("map内容");
        log.info("{}",map);
    }
    public <T> void execute(String chainName,T  req){
        if (!StringUtils.hasLength(chainName))return;
        List<AbstractFilter> abstractFilters = map.get(chainName);
        if (abstractFilters==null||abstractFilters.isEmpty())throw new NullPointerException(String.format("[%s] Chain of Responsibility ID is undefined.", chainName));
//        这里不需要排序在上面的run()已经排序了
//        abstractFilters.sort(Comparator.comparingInt(Ordered::getOrder));
        for (AbstractFilter abstractFilter : abstractFilters) {
            abstractFilter.handle(req);
        }
    }
    public <T> void execute(Class clazz,T  req){
        Collection values = applicationContext.getBeansOfType(clazz).values();
        AbstractFilter[] array = (AbstractFilter[])values.toArray(new AbstractFilter[0]);
        String mark = array[0].mark();
        execute(mark,req);
    }
}
