package org.wjx.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author xiu
 * @create 2023-12-06 14:44
 */
public class ApplicationInitializingEvent extends ApplicationEvent {
    public ApplicationInitializingEvent(Object source) {
        super(source);
    }
}
