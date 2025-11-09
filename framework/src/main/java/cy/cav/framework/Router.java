package cy.cav.framework;

import java.util.*;

/// Gives a handy syntax to process incoming messages by their type.
///
/// ## Example
///
/// ```java
///
/// class Cook extends Actor {
///     Cook(ActorInit init) { super(init); }
///
///     static final Router<Cook> router = new Router<Cook>()
///             .route(SandwichRequest.class, Cook::giveSandwich)
///             .route(PizzaRequest.class, Cook::givePizza)
///             .route(YouGotPaidMessage.class, Cook::paid)
///             .route(YouAreFiredMessage.class, Cook::fired);
///
///     @Override
///     protected void process(Envelope<?> envelope) {
///         router.process(this, envelope);
///     }
///
///     SandwichResponse giveSandwich(SandwichRequest request) {
///         if (request.vegetarian()) {
///             return new SandwichResponse("Sandwich à la betterave");
///         } else {
///             return new SandwichResponse("Sandwich au thon");
///         }
///     }
///
///     PizzaResponse givePizza(Envelope<PizzaRequest> envelope) {
///         if (envelope.sender().equals(ELON_MUSK_ADDRESS)) {
///             return new PizzaResponse("Pizza Tesla");
///         } else {
///             return new PizzaResponse("Pizza quatre fromages");
///         }
///     }
///
///     void paid(YouGotPaidMessage message) {
///         System.out.println("J'ai été payé " + message.amount() + "€ !");
///     }
///
///     void fired(Envelope<YouAreFiredMessage> envelope) {
///         System.out.println("J'ai été viré par " + envelope.sender());
///     }
/// }
///
/// ```
///
/// @see Message
/// @see Envelope
/// @see Actor
public class Router<A extends Actor> {
    // Class ---> Function for requests
    private final Map<Class<? extends Message>, SyncEnvelopeHandler<A, ?, ?>> syncMappings = new HashMap<>();
    // Class ---> Function for notifications
    private final Map<Class<? extends Message>, AsyncEnvelopeHandler<A, ?>> asyncMappings = new HashMap<>();

    /// Processes the envelope using the functions given to this router; more precisely,
    /// the function matching the envelope's message type.
    ///
    /// Does nothing if the message type isn't recognized. (For now)
    ///
    /// @param actor    the actor who's processing this envelope
    /// @param envelope the envelope the actor just received
    @SuppressWarnings("unchecked")
    public void process(A actor, Envelope<?> envelope) {
        if (envelope.body() instanceof Message.Request) {
            // This is a request, find a function that can respond to the request.
            Message.Response response = respond(actor, (Envelope<Message.Request<Message.Response>>) envelope);

            if (response != null) {
                // We called the function and got a response message in return; send our response now.
                actor.respond(envelope, response);
            }
        } else {
            // This is a notification, find a function that can respond to the notification.
            receive(actor, envelope);
        }

        // todo: respond err if message type not handled by either handler in requests
        //       Would be bad to let a request timeout because we don't know how to answer...
    }

    /// Used internally. Finds the function to use for a request-response envelope, and calls it.
    ///
    /// @return the message created by the function; null when no function found, can't be null if there's a function
    @SuppressWarnings("unchecked")
    private <O extends Message.Response, I extends Message.Request<O>> O respond(A actor, Envelope<I> envelope) {
        var handler = (SyncEnvelopeHandler<A, I, O>) syncMappings.getOrDefault(envelope.body().getClass(), null);

        if (handler == null) {
            return null;
        } else {
            return Objects.requireNonNull(handler.respond(actor, envelope), "Response can't be null!");
        }
    }

    /// Used internally. Finds the function to use for a notification envelope, and calls it.
    @SuppressWarnings("unchecked")
    private <I extends Message> void receive(A actor, Envelope<I> envelope) {
        var handler = (AsyncEnvelopeHandler<A, I>) asyncMappings.getOrDefault(envelope.body().getClass(), null);

        if (handler != null) {
            handler.receive(actor, envelope);
        }
    }

    /// Calls the function when receiving a **request** of the given class.
    ///
    /// @param messageClass the class of the message
    /// @param function     the function to call when receiving the message of this class,
    ///                      with signature `ResponseType func(Actor, Envelope<MessageType>)`
    public <O extends Message.Response, I extends Message.Request<O>> Router<A> route(Class<I> messageClass,
                                                                             SyncEnvelopeHandler<A, I, O> function) {
        syncMappings.put(messageClass, function);
        return this;
    }

    /// Calls the function when receiving a **request** of the given class.
    ///
    /// @param messageClass the class of the message
    /// @param function     the function to call when receiving the message of this class,
    ///                      with signature `ResponseType func(Actor, MessageType)`
    public <O extends Message.Response, I extends Message.Request<O>> Router<A> route(Class<I> messageClass,
                                                                             SyncBodyHandler<A, I, O> function) {
        return route(messageClass, (A actor, Envelope<I> envelope) -> function.respond(actor, envelope.body()));
    }

    // TODO: Completable future for request/response... If we ever need that?

    /// Calls the function when receiving a **notification** of the given class.
    ///
    /// @param messageClass the class of the message
    /// @param function     the function to call when receiving the message of this class,
    ///                      with signature `void func(Actor, Envelope<MessageType>)`
    public <I extends Message.Notification> Router<A> route(Class<I> messageClass, AsyncEnvelopeHandler<A, I> function) {
        asyncMappings.put(messageClass, function);
        return this;
    }

    /// Calls the function when receiving a **notification** of the given class.
    ///
    /// @param messageClass the class of the message
    /// @param function     the function to call when receiving the message of this class,
    ///                      with signature `void func(Actor, MessageType>`
    public <I extends Message.Notification> Router<A> route(Class<I> messageClass, AsyncBodyHandler<A, I> function) {
        asyncMappings.put(messageClass, (A actor, Envelope<I> envelope) -> function.receive(actor, envelope.body()));
        return this;
    }

    // The following looks like entire gibberish, but that's how we need to define lambdas in Java!

    @FunctionalInterface
    public interface SyncEnvelopeHandler<A extends Actor, I extends Message.Request<O>, O extends Message.Response> {
        O respond(A actor, Envelope<I> envelope);
    }

    @FunctionalInterface
    public interface SyncBodyHandler<A extends Actor, I extends Message.Request<O>, O extends Message.Response> {
        O respond(A actor, I body);
    }

    @FunctionalInterface
    public interface AsyncEnvelopeHandler<A extends Actor, I extends Message> {
        void receive(A actor, Envelope<I> envelope);
    }

    @FunctionalInterface
    public interface AsyncBodyHandler<A extends Actor, I extends Message> {
        void receive(A actor, I body);
    }
}
