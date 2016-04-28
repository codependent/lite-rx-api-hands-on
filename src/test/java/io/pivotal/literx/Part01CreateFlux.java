package io.pivotal.literx;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import org.junit.Test;

import reactor.core.publisher.Flux;
import reactor.core.test.TestSubscriber;

/**
 * Learn how to create Flux instances.
 *
 * @author Sebastien Deleuze
 * @see <a href="http://projectreactor.io/core/docs/api/reactor/core/publisher/Flux.html">Flux Javadoc</a>
 * @see <a href="http://projectreactor.io/core/docs/api/reactor/core/test/TestSubscriber.html">TestSubscriber Javadoc</a>
 */
public class Part01CreateFlux {

//========================================================================================

	@Test
	public void empty() {
		Flux<String> flux = emptyFlux();
		TestSubscriber<String> testSubscriber = new TestSubscriber<String>();
		testSubscriber
				.bindTo(flux)
				.assertValueCount(0)
				.assertComplete();
	}

	Flux<String> emptyFlux() {
		return Flux.empty();
	}

//========================================================================================

	@Test
	public void fromValues() {
		Flux<String> flux = fooBarFluxFromValues();
		TestSubscriber<String> testSubscriber = new TestSubscriber<>();
		testSubscriber
				.bindTo(flux)
				.assertValues("foo", "bar")
				.assertComplete();
	}

	Flux<String> fooBarFluxFromValues() {
		return Flux.just("foo","bar");
	}

//========================================================================================

	@Test
	public void fromList() {
		Flux<String> flux = fooBarFluxFromList();
		TestSubscriber<String> testSubscriber = new TestSubscriber<>();
		testSubscriber
				.bindTo(flux)
				.assertValues("foo", "bar")
				.assertComplete();
	}

	Flux<String> fooBarFluxFromList() {
		return Flux.fromIterable(Arrays.asList(new String[]{"foo","bar"}));
	}

//========================================================================================

	@Test
	public void error() {
		Flux<String> flux = errorFlux();
		TestSubscriber<String> testSubscriber = new TestSubscriber<>();
		testSubscriber
				.bindTo(flux)
				.assertError(IllegalStateException.class)
				.assertNotComplete();
	}

	Flux<String> errorFlux() {
		return Flux.error(new IllegalStateException());
	}

//========================================================================================

	@Test
	public void neverTerminates() {
		Flux<String> flux = neverTerminatedFlux();
		TestSubscriber<String> testSubscriber = new TestSubscriber<>();
		testSubscriber
				.bindTo(flux)
				.assertNotTerminated();
	}

	Flux<String> neverTerminatedFlux() {
		return Flux.never();
	}

//========================================================================================

	@Test
	public void countEachSecond() {
		Flux<Long> flux = counter();
		TestSubscriber<Long> testSubscriber = new TestSubscriber<>();
		testSubscriber
				.bindTo(flux)
				.assertNotTerminated()
				.awaitAndAssertNextValues(0L, 1L, 2L);
	}

	Flux<Long> counter() {
		return Flux.interval(Duration.of(100, ChronoUnit.MILLIS));
	}

}
