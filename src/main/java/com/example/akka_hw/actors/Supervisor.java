package com.example.akka_hw.actors;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import akka.util.Timeout;
import com.example.akka_hw.parser.SimpleRequestParser;
import com.example.akka_hw.stub_server.*;
import scala.Option;
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
                getContext().actorOf(Props.create(ChildActor.class, this.createStubServer(msg)), name);
            }
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

    private static final Timeout kTimeout = new Timeout(50, TimeUnit.MILLISECONDS);
    private final ActorRef ref;
    private final ActorSystem system;
    private final List<Future<Object>> tasks = new ArrayList<>();

    public Supervisor(final String request, final int delay_1, final int delay_2, final int delay_3) {
        var parsedRequest = SimpleRequestParser.parse(request);
        this.system = ActorSystem.create("MySystem");
        this.ref = system.actorOf(
                Props.create(SupervisorImpl.class), "parent");
        tasks.add(ask(ref, new Messages.StartMsg(YANDEX, parsedRequest, delay_1), kTimeout));
        tasks.add(ask(ref, new Messages.StartMsg(GOOGLE, parsedRequest, delay_2), kTimeout));
        tasks.add(ask(ref, new Messages.StartMsg(BING, parsedRequest, delay_3), kTimeout));
    }

    public List<Response> get() {
        List<Response> result = new ArrayList<>();
        for (var task : tasks) {
            var res_opt = task.value();
            if (res_opt.nonEmpty()) {
                var res = res_opt.get();
                if (res.isSuccess()) {
                    result.add((Response) (res.get()));
                } else {
                    System.err.println("Task is failure");
                }
            } else {
                System.err.println("Task is empty");
            }
        }
        system.stop(ref);
        return result;
    }
}
