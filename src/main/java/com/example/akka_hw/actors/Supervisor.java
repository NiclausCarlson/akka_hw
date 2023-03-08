package com.example.akka_hw.actors;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import com.example.akka_hw.parser.SimpleRequestParser;
import com.example.akka_hw.stub_server.*;
import scala.Option;

import java.util.ArrayList;
import java.util.List;

import static com.example.akka_hw.actors.Messages.StubServerType.*;

public class Supervisor {
    private static final int kReceiveTimeout = 50;

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

    public class ChildActor<T extends StubServerBase> extends UntypedActor {
        private T stubServer;

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
                    stubServer.get();
                } else if (message.equals("restart")) {
                    throw new RestartException();
                } else if (message.equals("stop")) {
                    throw new StopException();
                } else if (message.equals("escalate")) {
                    throw new EscalateException();
                } else {
                    System.out.println(self().path() + " got message: " + message);
                }
            }
        }
    }

    public class SupervisorImpl extends UntypedActor {
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

    private ActorRef parent;
    private List<String> child = new ArrayList<>();

    public Supervisor(final String request, final int delay_1, final int delay_2, final int delay_3) {
        var parsedRequest = SimpleRequestParser.parse(request);
        ActorSystem system = ActorSystem.create("MySystem");
        // Create actor
        this.parent = system.actorOf(
                Props.create(SupervisorImpl.class), "parent");

        parent.tell(new Messages.StartMsg(GOOGLE, parsedRequest, delay_1), ActorRef.noSender());
        parent.tell(new Messages.StartMsg(YANDEX, parsedRequest, delay_2), ActorRef.noSender());
        parent.tell(new Messages.StartMsg(BING, parsedRequest, delay_3), ActorRef.noSender());
        child.add("child_" + GOOGLE);
        child.add("child_" + YANDEX);
        child.add("child_" + BING);
    }

    public List<Response> get() {
        return null;
    }
}
