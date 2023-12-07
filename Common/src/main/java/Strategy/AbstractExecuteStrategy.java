package Strategy;

/**
 * @author xiu
 * @create 2023-12-05 18:31
 */
public interface AbstractExecuteStrategy<REQUEST, RESPONSE> {
    default String mark() {
        return null;
    }

    /**
     * 无返回值策略
     * @param request
     */
   default void execute(REQUEST request){
   }

    /**
     * 可以传入正则来找到对应的策略,如果mark符合对应的正则，就匹配
     * @return
     */
    default String patternMatchMark() {
        return null;
    }

    /**
     * 有返回值策略
     * @param request
     * @return
     */
   default RESPONSE executeResp(REQUEST request){
        return null;
   }

}
