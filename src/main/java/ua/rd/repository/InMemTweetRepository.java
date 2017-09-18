package ua.rd.repository;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import ua.rd.domain.Tweet;
import ua.rd.ioc.annotation.Benchmark;
import ua.rd.ioc.annotation.MyPostConstruct;

public class InMemTweetRepository implements TweetRepository {

    private List<Tweet> tweets;
   
    @PostConstruct
    public void init() {
//    	tweets = Arrays.asList(
//                new Tweet(1L, "First Mesg", null),
//                new Tweet(2L, "Second Mesg", null)
//        );
    }

    @Override
    @Benchmark(enabled = true)
    public Iterable<Tweet> allTweets() {
        return tweets;
    }
    
}
