package io.pivotal.literx;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import reactor.core.publisher.Computations;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.scheduler.Scheduler;
import reactor.core.subscriber.SignalEmitter;
import reactor.core.subscriber.SignalEmitter.Emission;

public class Part10SubscribeOnPublishOn {

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
	public void stopwatchReading() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(2);
		
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
	
	public class RnApp implements Subscriber<Float>{
		
		private String name;
		private Subscription subscription;
		private List<Float> randomNumbers = new ArrayList<Float>();
		
		public RnApp(String name){
			this.name = name;
		}
		
		@Override
		public void onComplete() {
			System.out.println("onComplete");
		}

		@Override
		public void onError(Throwable err) {
			err.printStackTrace();
		}

		@Override
		public void onNext(Float f) {
			System.out.println(Thread.currentThread().getName()+"-"+name+ " got ------> "+f);
			System.out.println(Thread.currentThread().getName()+"-"+name+ " got ------> "+this.randomNumbers);
			this.randomNumbers.add(f);
		}

		@Override
		public void onSubscribe(Subscription subs) {
			this.subscription = subs;
		}
		
		public void request(long n){
			this.subscription.request(n);
		}
	}
	
	@Test
	public void randomNumberReading() throws InterruptedException {
		
		CountDownLatch latch = new CountDownLatch(1);
		Scheduler concurrent = Computations.concurrent();
		/*
		Flux<Float> coldRandomNumberGenerator = Flux.<Float, SecureRandom>using(
		    () -> new SecureRandom(),
		    sr -> Flux
		    	.interval(Duration.of(1000, ChronoUnit.MILLIS))
		    	.<Float>map(v -> sr.nextFloat()),
	    	sr -> { }
		).log().onBackpressureDrop().subscribeOn(Computations.concurrent());
		
		ConnectableFlux<Float> randomNumberGenerator = coldRandomNumberGenerator.subscribeOn(Computations.concurrent()).publish(1);
		randomNumberGenerator.connect();
			*/
		/*
		ConnectableFlux<Float> randomNumberGenerator = ConnectableFlux.<Float>create( (c) -> {
			SecureRandom sr = new SecureRandom();
			int i = 1;
			while(true){
				try {
					Thread.sleep(1);
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("-----------------------------------------------------"+(i++));
				c.onNext(sr.nextFloat());
			}
		}).log().publish();
		randomNumberGenerator.connect();
		
		
		Flux<Float> noCacheRandomNumberGenerator = randomNumberGenerator
														.log()
														.subscribeOn(Computations.concurrent())
														.onBackpressureDrop();
														*/
		
		/*
		Flux<Float> randomNumberGenerator = Flux.<Float>yield( consumer -> {
			SecureRandom sr = new SecureRandom();
			int i = 1;
			while(true){
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				Emission emission = consumer.emit(sr.nextFloat());
			}
		});
		randomNumberGenerator.log().subscribeOn(Computations.concurrent()).subscribe();
		*/
		
		Generator generator = new Generator();
		generator.start();
		Thread.sleep(6000);
		System.out.println("WAKE UP");
		RnApp app = new RnApp("APP");
		RnApp xxx = new RnApp("XXX");
		generator.getRandomNumberGenerator().subscribe(app);
		generator.getRandomNumberGenerator().subscribe(xxx);
		Thread.sleep(6000);
		System.out.println("WAKE UP 2"); 
		app.request(5);
		xxx.request(5);
		
		Thread.sleep(30000);
		System.out.println("WAKE UP 3");
		app.request(5);
		xxx.request(5);
	
		latch.await();
	}

	public class Generator{
		
		private EmitterProcessor<Float> randomNumberGenerator;
		private Scheduler concurrent = Computations.concurrent();
		
		public void start(){
			randomNumberGenerator = EmitterProcessor.create();
			randomNumberGenerator.log().subscribeOn(concurrent).publishOn(concurrent).onBackpressureDrop().subscribe();    

			Computations.single().schedule( () -> {
				SignalEmitter<Float> emitter = randomNumberGenerator.connectEmitter();
				SecureRandom sr = new SecureRandom();
				int i = 1;
				while(true){
				     try {
				             Thread.sleep(1000);
				     } catch (Exception e) {
				             e.printStackTrace();
				     }
				     Emission emission = emitter.emit(sr.nextFloat());
				}
			});
		}
		
		public EmitterProcessor<Float> getRandomNumberGenerator() {
			return randomNumberGenerator;
		}
	}
	
	 @Test
     public void testPublishSubscribe() throws InterruptedException {
	 	
        Scheduler concurrent = Computations.concurrent();

        EmitterProcessor<Float> timeGenerator = EmitterProcessor.create();
        timeGenerator
        	.subscribeOn(concurrent)
        	.publishOn(concurrent);

        SignalEmitter<Float> emitter = timeGenerator.connectEmitter();

        Computations.single().schedule(() -> {
        	SecureRandom sr = new SecureRandom();
            while (true) {
                try {
                    Thread.sleep(1000);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                float random = sr.nextFloat();
                System.out.println("NEW VALUE ---------------"+random);
                emitter.emit(random);
            }
        });
        
        FluxProcessor<Float,Float> droppingProcessor = FluxProcessor.<Float,Float>wrap(timeGenerator, timeGenerator.onBackpressureDrop());

        Thread.sleep(4000);

        System.out.println("WAKE UP");

        RnApp aaa = new RnApp("AAA");
        RnApp zzz = new RnApp("ZZZ");
        droppingProcessor.subscribe(aaa);
        droppingProcessor.subscribe(zzz);

        Thread.sleep(4000);

        System.out.println("REQUESTING 5");
        aaa.request(5);
        zzz.request(5);
        
        Thread.sleep(4000);

        System.out.println("ONE MORE SUBSCRIBER");

        timeGenerator.subscribe(v -> System.out.println("3: " + v));

        Thread.sleep(2000);
     }
	 
	 @Test
     public void testPublishSubscribe2() throws InterruptedException {
	 	
        Scheduler concurrent = Computations.concurrent();

        DirectProcessor<Float> timeGenerator = DirectProcessor.<Float>create();
        
        timeGenerator
        .subscribeOn(concurrent)
        .publishOn(concurrent)
        .subscribe(v -> System.out.println("0: " + v));

        SignalEmitter<Float> emitter = timeGenerator.connectEmitter();

        Computations.single().schedule(() -> {
        	SecureRandom sr = new SecureRandom();
            while (true) {
                try {
                    Thread.sleep(1000);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                float random = sr.nextFloat();
                System.out.println("NEW VALUE ---------------"+random);
                emitter.emit(random);
            }

        });

        Thread.sleep(4000);

        System.out.println("WAKE UP");

        RnApp aaa = new RnApp("AAA");
        RnApp zzz = new RnApp("ZZZ");
        timeGenerator.subscribe(aaa);
        timeGenerator.subscribe(zzz);

        Thread.sleep(4000);

        System.out.println("REQUESTING 5");
        aaa.request(5);
        zzz.request(5);
        
        Thread.sleep(4000);

        System.out.println("ONE MORE SUBSCRIBER");

        timeGenerator.subscribe(v -> System.out.println("3: " + v));

        Thread.sleep(2000);
     }
	 
}
