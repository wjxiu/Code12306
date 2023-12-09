package org.wjx.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.wjx.page.PageRequest;
import org.wjx.page.PageResponse;

import java.util.List;
import java.util.function.Function;

/**
 * @author xiu
 * @create 2023-12-07 14:26
 */
public class PageUtil {

    /**
     * {@link PageRequest} to {@link Page}
     */
    public static Page convert(PageRequest pageRequest) {
        return convert(pageRequest.getCurrent(), pageRequest.getSize());
    }

    /**
     * {@link PageRequest} to {@link Page}
     */
    public static Page convert(long current, long size) {
        return new Page(current, size);
    }

    /**
     * {@link IPage} to {@link PageResponse}
     */
    public static PageResponse convert(IPage iPage) {
        return buildConventionPage(iPage);
    }

    /**
     * {@link IPage} to {@link PageResponse}
     */
    public static <TARGET, ORIGINAL> PageResponse<TARGET> convert(IPage<ORIGINAL> iPage, Class<TARGET> targetClass) {
        iPage.convert(each -> BeanUtil.convert(each, targetClass));
        return buildConventionPage(iPage);
    }

    /**
     *
     *转换前执行函数表达式f，唯一一个参数表示被转换的每一个元素
     * {@link IPage} to {@link PageResponse}
     * @param iPage
     * @param respClass
     * @param f
     * @return PageResponse<RESP>
     * @param <RESP> 转换后的class
     * @param <ORIGINAL> 未转换的class
     */
    public static <RESP, ORIGINAL> PageResponse<RESP> convert(IPage<ORIGINAL> iPage, Class<RESP> respClass, Function <? super ORIGINAL,? super RESP>  f) {
        List<? super RESP> list = iPage.getRecords().stream().map(f).toList();
        List<RESP> targets = BeanUtil.convertToList(list, respClass);
       return   PageResponse.<RESP>builder()
                .current(iPage.getCurrent())
                .size(iPage.getSize())
                .records(targets)
                .total(iPage.getTotal())
                .build();
    }


        /**
         * {@link IPage} build to {@link PageResponse}
         */
    private static PageResponse buildConventionPage(IPage iPage) {
        return PageResponse.builder()
                .current(iPage.getCurrent())
                .size(iPage.getSize())
                .records(iPage.getRecords())
                .total(iPage.getTotal())
                .build();
    }
}
