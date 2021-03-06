package com.val;

import com.val.beans.Client;
import com.val.beans.Event;
import com.val.beans.EventType;
import com.val.loggers.EventLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

import static java.lang.String.format;

@Service
public class App {

    @Autowired
    Client client;

    @Autowired
    @Qualifier("consoleEventLogger")
    EventLogger defaultLogger;

    @Resource(name = "loggerMap")
    Map<EventType, EventLogger> loggerMap;

    public App() {
    }

    public App(Client client, EventLogger defaultLogger, Map loggerMap) {
        this.client = client;
        this.defaultLogger = defaultLogger;
        this.loggerMap = loggerMap;
    }

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(com.val.spring.AppConfig.class);
        context.refresh();

        App app = (App) context.getBean("app");
        Event e = (Event) context.getBean("event");
        e.setMsg("event msg for 1");
        e.setType(EventType.ERROR);

        app.logEvent(e);

        context.registerShutdownHook();
    }

    public void logEvent(Event event) {

        String message = event.getMsg().replace(
                String.valueOf(client.getId()),
                format("%s %s", client.getName(), client.getGreeting()));
        event.setMsg(message);

        if(loggerMap == null){
            defaultLogger.logEvent(event);
            return;
        }

        EventLogger logger = loggerMap.get(event.getType());

        if(logger == null){
            logger = defaultLogger;
        }

        logger.logEvent(event);
    }

    public void setLoggerMap(Map<EventType, EventLogger> loggersMap) {
        this.loggerMap = loggersMap;
    }
}
