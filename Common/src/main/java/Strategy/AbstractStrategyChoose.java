package Strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.wjx.Exception.ServiceException;
import org.wjx.event.ApplicationInitializingEvent;
import org.wjx.user.core.ApplicationContextHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 策略选择器
 *
 * @author xiu
 * @create 2023-12-06 14:36
 */
@Slf4j
public class AbstractStrategyChoose implements ApplicationListener<ApplicationInitializingEvent> {
   static final HashMap<String, AbstractExecuteStrategy> abstractExecuteStrategyMap = new HashMap<>();

    public <REQUEST> void chooseAndExecute(String mark, REQUEST requestParam) {
        AbstractExecuteStrategy choose = choose(mark, null);
        choose.execute(requestParam);
    }
    public <REQUEST> void chooseAndExecute(String mark, REQUEST requestParam,Boolean predicateFlag) {
        AbstractExecuteStrategy choose = choose(mark, predicateFlag);
        choose.execute(requestParam);
    }
    public <REQUEST,RESPONSE> RESPONSE  chooseAndExecuteResp(String mark, REQUEST requestParam) {
        AbstractExecuteStrategy choose = choose(mark, null);
        return (RESPONSE) choose.executeResp(requestParam);
    }

    private AbstractExecuteStrategy choose(String mark, Boolean predicateFlag) {
        if (predicateFlag != null && predicateFlag) {
            return abstractExecuteStrategyMap.values().stream()
                    .filter(a -> StringUtils.hasLength(a.patternMatchMark()))
                    .filter(a -> Pattern.compile(a.patternMatchMark()).matcher(a.mark()).matches()).findFirst()
                    .orElseThrow(() -> new ServiceException("策略未定义"));
        }
        return Optional.ofNullable(abstractExecuteStrategyMap.get(mark))
                .orElseThrow(() -> new ServiceException("策略未定义"));
    }


    /**
     * 检测是否有重复的bean，并且将不重复的加入到map中
     */
    @Override
    public void onApplicationEvent(ApplicationInitializingEvent event) {
        Map<String, AbstractExecuteStrategy> beansOfType = ApplicationContextHolder.getBeansOfType(AbstractExecuteStrategy.class);
        beansOfType.forEach((name, bean) -> {
            if (abstractExecuteStrategyMap.get(name) != null) {
                throw new ServiceException(String.format("[%s] Duplicate execution policy", bean.mark()));
            }
            log.info("choosename-----------------{}",bean.mark());
            abstractExecuteStrategyMap.put(bean.mark(), bean);
        });

    }

    public AbstractStrategyChoose() {
    }
}
