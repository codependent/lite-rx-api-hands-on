package io.pivotal.literx;

import io.pivotal.literx.domain.User;
import io.pivotal.literx.repository.ReactiveRepository;
import io.pivotal.literx.repository.ReactiveUserRepository;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.test.TestSubscriber;

/**
 * Learn how to merge flux.
 *
 * @author Sebastien Deleuze
 */
public class Part04Merge {

	final static User MARIE = new User("mschrader", "Marie", "Schrader");
	final static User MIKE = new User("mehrmantraut", "Mike", "Ehrmantraut");

	ReactiveRepository<User> repository1 = new ReactiveUserRepository(500);
	ReactiveRepository<User> repository2 = new ReactiveUserRepository(MARIE, MIKE);

//========================================================================================

	@Test
	public void mergeWithInterleave() {
		Flux<User> flux = mergeFluxWithInterleave(repository1.findAll(), repository2.findAll());
		TestSubscriber<User> testSubscriber = new TestSubscriber<>();
		testSubscriber
				.bindTo(flux)
				.await()
				.assertValues(MARIE, MIKE, User.SKYLER, User.JESSE, User.WALTER, User.SAUL)
				.assertComplete();
	}

	Flux<User> mergeFluxWithInterleave(Flux<User> flux1, Flux<User> flux2) {
		return flux1.mergeWith(flux2);
	}

//========================================================================================

	@Test
	public void mergeWithNoInterleave() {
		Flux<User> flux = mergeFluxWithNoInterleave(repository1.findAll(), repository2.findAll());
		TestSubscriber<User> testSubscriber = new TestSubscriber<>();
		testSubscriber
				.bindTo(flux)
				.await()
				.assertValues(User.SKYLER, User.JESSE, User.WALTER, User.SAUL, MARIE, MIKE)
				.assertComplete();
	}

	Flux<User> mergeFluxWithNoInterleave(Flux<User> flux1, Flux<User> flux2) {
		return Flux.concat(flux1, flux2);
	}

//========================================================================================

	@Test
	public void multipleMonoToFlux() {
		Mono<User> skylerMono = repository1.findFirst();
		Mono<User> marieMono = repository2.findFirst();
		Flux<User> flux = createFluxFromMultipleMono(skylerMono, marieMono);
		TestSubscriber<User> testSubscriber = new TestSubscriber<>();
		testSubscriber
				.bindTo(flux)
				.await()
				.assertValues(User.SKYLER, MARIE)
				.assertComplete();
	}

	Flux<User> createFluxFromMultipleMono(Mono<User> mono1, Mono<User> mono2) {
		return Flux.concat(mono1, mono2);
	}

}
