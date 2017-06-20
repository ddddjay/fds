import com.kakaobank.data.EventDetectorConfiguration;
import com.kakaobank.data.service.EventConsumerProcess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by djyun on 2017. 6. 17..
 */
@Slf4j
public class MainRunner {
    private static ApplicationContext ctx;

    public static void main(String[] args) {

        try {

            ctx = new AnnotationConfigApplicationContext(EventDetectorConfiguration.class);
            EventConsumerProcess detector = (EventConsumerProcess) ctx.getBean("eventConsumerProcess");

            detector.run();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
