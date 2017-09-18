package ua.rd.services;

import ua.rd.domain.Tweet;
import ua.rd.repository.TweetRepository;

public class SimpleTweetService implements TweetService {

    private final TweetRepository tweetRepository;
    private Tweet tweet;

    public SimpleTweetService(TweetRepository tweetRepository) {
        this.tweetRepository = tweetRepository;
    }

    @Override
    public Iterable<Tweet> allTweets() {
        return tweetRepository.allTweets();
    }

	public Tweet getTweet() {
		return tweet;
	}

	public void setTweet(Tweet tweet) {
		this.tweet = tweet;
	}
}
