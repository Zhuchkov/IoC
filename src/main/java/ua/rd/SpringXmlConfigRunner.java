package ua.rd;

import java.util.Arrays;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ua.rd.domain.Tweet;
import ua.rd.services.SimpleTweetService;

public class SpringXmlConfigRunner {

	public static void main(String[] args) {
		ConfigurableApplicationContext reopContext = new ClassPathXmlApplicationContext("repo.xml");
		ConfigurableApplicationContext serviceContext = new ClassPathXmlApplicationContext(
				new String[] { "services.xml" }, reopContext);
		System.out.println(Arrays.asList(serviceContext.getBeanDefinitionNames()));
		System.out.println(Arrays.asList(reopContext.getBeanDefinitionNames()));
		SimpleTweetService tweetService = (SimpleTweetService) serviceContext.getBean("simpleTweetService");
		Tweet tweet1 = tweetService.getTweet();
		System.out.println(tweet1.getRand());
		Tweet tweet2 = tweetService.getTweet();
		System.out.println(tweet2.getRand());
		// System.out.println(tweetService.allTweets());

	}

}
