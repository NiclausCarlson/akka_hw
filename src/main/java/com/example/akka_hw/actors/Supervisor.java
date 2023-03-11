package com.example.akka_hw.actors;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import akka.util.Timeout;
import com.example.akka_hw.parser.SimpleRequestParser;
import com.example.akka_hw.stub_server.*;
import scala.Option;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;

import static com.example.akka_hw.actors.Messages.StubServerType.*;

public class Supervisor {

    public static class RestartException extends RuntimeException {
        public RestartException() {
            super();
        }
    }

    public static class StopException extends RuntimeException {
        public StopException() {
            super();
        }
    }

    public static class EscalateException extends RuntimeException {
        public EscalateException() {
            super();
        }
    }

    public static class ChildActor<T extends StubServerBase> extends UntypedActor {
        private final T stubServer;

        public ChildActor(T stubServer) {
            this.stubServer = stubServer;
        }

        @Override
        public void postStop() {
            System.out.println(self().path() + " was stopped");
        }

        @Override
        public void postRestart(Throwable cause) {
            System.out.println(self().path() + " was restarted after: " + cause);
        }

        @Override
        public void preRestart(Throwable cause, Option<Object> message) {
            System.out.println(self().path() + " is dying because of " + cause);
        }

        @Override
        public void onReceive(Object message) throws Throwable {
            if (message instanceof String) {
                if (message.equals("get")) {
                    getSender().tell(stubServer.get(), getSelf());
                } else if (message.equals("restart")) {
                    throw new RestartException();
                } else if (message.equals("stop")) {
                    throw new StopException();
                } else if (message.equals("escalate")) {
                    throw new EscalateException();
                } else {
                    System.out.println(self().path() + " got message: " + message);
                    var ex = new RuntimeException("Got unsupported command");
                    getSender().tell(new Status.Failure(ex), getSelf());
                    throw ex;
                }
            }
        }
    }

    public static class SupervisorImpl extends UntypedActor {
        private final List<Future<Object>> tasks = new ArrayList<>();

        @Override
        public SupervisorStrategy supervisorStrategy() {
            return new OneForOneStrategy(false, DeciderBuilder
                    .match(RestartException.class, e -> OneForOneStrategy.restart())
                    .match(StopException.class, e -> OneForOneStrategy.stop())
                    .match(EscalateException.class, e -> OneForOneStrategy.escalate())
                    .build());
        }

        @Override
        public void onReceive(Object message) throws Throwable {
            if (message instanceof Messages.StartMsg msg) {
                String name = "child_" + msg.stubType.toString();
                System.out.println("Create child: " + name);
                var ref = getContext().actorOf(Props.create(ChildActor.class, this.createStubServer(msg)), name);
                tasks.add(ask(ref, "get", kTimeout));
                return;
            } else if (message instanceof String msg) {
                List<Response> result = new ArrayList<>();
                if (msg.equals("get")) {
                    for (var task : tasks) {
                        try {
                            var res = Await.result(task, kTimeout.duration());
                            if (res instanceof Response response) {
                                result.add(response);
                            } else {
                                System.err.println("Failed to get response: " + res.toString());
                            }
                        } catch (Exception ex) {
                            System.err.println("Failed to get response: " + ex);
                        }
                    }
                    getSender().tell(result, getSelf());
                    return;
                }
            }
            System.out.println(self().path() + " got message: " + message);
            var ex = new RuntimeException("Got unsupported command");
            getSender().tell(new Status.Failure(ex), getSelf());
            throw ex;
        }

        private StubServerBase createStubServer(final Messages.StartMsg msg) {
            if (msg.stubType == GOOGLE) {
                return new Google(msg.predicates, msg.delay);
            } else if (msg.stubType == YANDEX) {
                return new Yandex(msg.predicates, msg.delay);
            } else {
                return new Bing(msg.predicates, msg.delay);
            }
        }
    }

    private static final Timeout kTimeout = new Timeout(100, TimeUnit.MILLISECONDS);
    private final ActorRef ref;
    private final ActorSystem system;
    private final List<Future<Object>> tasks = new ArrayList<>();

    public Supervisor(final String request, final int delay_1, final int delay_2, final int delay_3) {
        var parsedRequest = SimpleRequestParser.parse(request);
        this.system = ActorSystem.create("MySystem");
        this.ref = system.actorOf(
                Props.create(SupervisorImpl.class), "parent");
        ref.tell(new Messages.StartMsg(YANDEX, parsedRequest, delay_1), ActorRef.noSender());
        ref.tell(new Messages.StartMsg(GOOGLE, parsedRequest, delay_2), ActorRef.noSender());
        ref.tell(new Messages.StartMsg(BING, parsedRequest, delay_3), ActorRef.noSender());
    }

    public List<Response> get() {
        try {
            var res = ask(ref, "get", kTimeout);
            var result = (ArrayList<Response>) Await.result(res, kTimeout.duration());
            system.stop(ref);
            return result;
        } catch (Exception ex) {
            System.err.println("Can't get response: " + ex);
            return new ArrayList<>();
        }
    }
}
