package com.good.ivrstand.extern.infrastructure.bot;

import org.springframework.context.ApplicationEvent;

/**
 * Событие - вызов помощи на стенде
 */
public class HelpEvent extends ApplicationEvent {

    public HelpEvent(Object source) {
        super(source);
    }
}
