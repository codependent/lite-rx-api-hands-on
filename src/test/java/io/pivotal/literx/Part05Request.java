package io.pivotal.literx;

import io.pivotal.literx.domain.User;
import io.pivotal.literx.repository.ReactiveRepository;
import io.pivotal.literx.repository.ReactiveUserRepository;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.test.TestSubscriber;

/**
 * Learn how to control the demand.
 *
 * @author Sebastien Deleuze
 */
public class Part05Request {

	ReactiveRepository<User> repository = new ReactiveUserRepository();

//========================================================================================

	@Test
	public void requestNoValue() {
		Flux<User> flux = repository.findAll();
		TestSubscriber<User> testSubscriber = createSubscriber();
		testSubscriber
				.bindTo(flux)
				.await()
				.assertNoValues();
	}

	TestSubscriber<User> createSubscriber() {
		return new TestSubscriber<User>(0);
	}

//========================================================================================

	@Test
	public void requestValueOneByOne() {
		Flux<User> flux = repository.findAll();
		TestSubscriber<User> testSubscriber = createSubscriber();
		testSubscriber
				.bindTo(flux)
				.assertValueCount(0);
		requestOne(testSubscriber);
		testSubscriber
				.awaitAndAssertNextValues(User.SKYLER)
				.assertNotTerminated();
		requestOne(testSubscriber);
		testSubscriber
				.awaitAndAssertNextValues(User.JESSE)
				.assertNotTerminated();
		requestOne(testSubscriber);
		testSubscriber
				.awaitAndAssertNextValues(User.WALTER)
				.assertNotTerminated();
		requestOne(testSubscriber);
		testSubscriber
				.awaitAndAssertNextValues(User.SAUL)
				.assertComplete();
	}

	void requestOne(TestSubscriber<User> testSubscriber) {
		testSubscriber.request(1);
	}

//========================================================================================

	@Test
	public void experimentWithLog() {
	Flux<User> flux = fluxWithLog();
		TestSubscriber<User> testSubscriber = createSubscriber();
		testSubscriber
				.bindTo(flux)
				.assertValueCount(0);
		requestOne(testSubscriber);
		testSubscriber
				.awaitAndAssertNextValues(User.SKYLER)
				.assertNotTerminated();
		requestOne(testSubscriber);
		testSubscriber
				.awaitAndAssertNextValues(User.JESSE)
				.assertNotTerminated();
		requestOne(testSubscriber);
		testSubscriber
				.awaitAndAssertNextValues(User.WALTER)
				.assertNotTerminated();
		requestOne(testSubscriber);
		testSubscriber
				.awaitAndAssertNextValues(User.SAUL)
				.assertComplete();
	}

	Flux<User> fluxWithLog() {
		return repository.findAll().log();
	}


//========================================================================================

	@Test
	public void experimentWithDoOn() {
		Flux<User> flux = fluxWithDoOnPrintln();
		TestSubscriber<User> testSubscriber = createSubscriber();
		testSubscriber
				.bindTo(flux)
				.assertValueCount(0);
		requestOne(testSubscriber);
		testSubscriber
				.awaitAndAssertNextValues(User.SKYLER)
				.assertNotTerminated();
		requestOne(testSubscriber);
		testSubscriber
				.awaitAndAssertNextValues(User.JESSE)
				.assertNotTerminated();
		requestOne(testSubscriber);
		testSubscriber
				.awaitAndAssertNextValues(User.WALTER)
				.assertNotTerminated();
		requestOne(testSubscriber);
		testSubscriber
				.awaitAndAssertNextValues(User.SAUL)
				.assertComplete();
	}

	Flux<User> fluxWithDoOnPrintln() {
		return repository.findAll().doOnSubscribe( s -> System.out.println("Starring:") )
				.doOnNext( u -> System.out.println(u.getFirstname() + " "+ u.getLastname()))
				.doOnComplete( () -> System.out.println("The end!"));
	}

}
