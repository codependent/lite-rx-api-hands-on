package io.pivotal.literx;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import reactor.core.publisher.Computations;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

public class Part10SubscribeOnPublishOn {

	private CountDownLatch latch = new CountDownLatch(2);
	
	public class Coach implements Subscriber<Duration>{

		private String name;
		private int maxTime;
		
		private Subscription subscription;
		
		public Coach(String name, int maxTime){
			this.name = name;
			this.maxTime = maxTime;
		}
		
		public void start(){
			subscription.request(Long.MAX_VALUE);
		}
		
		@Override
		public void onComplete() {
			Instant finishTime = Clock.systemDefaultZone().instant();
		}

		@Override
		public void onError(Throwable e) {
		}

		@Override
		public void onNext(Duration t) {
			if(t.getSeconds() >= maxTime){
				subscription.cancel();
			}
		}

		@Override
		public void onSubscribe(Subscription subs) {
			this.subscription = subs;
		}
		
	}
	
	@Test
	public void sensorReading() throws InterruptedException {
		
		Flux<Duration> stopWatch = Flux.<Duration>create( (c) -> {
			Clock clock = Clock.systemDefaultZone();
			Instant instant = clock.instant();
			while(!c.isCancelled()){
				Duration duration = Duration.between(instant, clock.instant());
				c.onNext( duration );
			}
			c.onComplete();
			latch.countDown();
		}).log().subscribeOn(Computations.concurrent());
		
		Coach c = new Coach("Jose", 4);
		Coach c2 = new Coach("Ana", 2);
		
		stopWatch.subscribe(c);
		stopWatch.subscribe(c2);
	
		c.start();
		c2.start();
		
		latch.await();
	}
	
}
