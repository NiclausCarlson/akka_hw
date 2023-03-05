package com.example.akka_hw.actors;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import com.example.akka_hw.stub_server.Bing;
import com.example.akka_hw.stub_server.Google;
import com.example.akka_hw.stub_server.Yandex;
import scala.Option;

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

    public class ChildActor extends UntypedActor {

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
                if (message.equals("restart")) {
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
        private int number = 0;

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
            if (message.equals("start")) {
                String name = "child" + number++;
                System.out.println("Create child: " + name);
                getContext().actorOf(Props.create(ChildActor.class), name);
            }
        }
    }

    public Supervisor() {
        ActorSystem system = ActorSystem.create("MySystem");
        // Create actor
        ActorRef parent = system.actorOf(
                Props.create(SupervisorImpl.class), "parent");

        parent.tell("start", ActorRef.noSender());
        parent.tell("start", ActorRef.noSender());
        parent.tell("start", ActorRef.noSender());

        for (int i = 0; i < 3; i++) {
            system.actorSelection("user/parent/child" + i).tell("Hello!", ActorRef.noSender());
        }

        // restart and send new message for child1
        system.actorSelection("user/parent/child1").tell("restart", ActorRef.noSender());
        system.actorSelection("user/parent/child1").tell("Hello2", ActorRef.noSender());

        // stop and send new message for child1 (message wouldn't be received)
        system.actorSelection("user/parent/child1").tell("escalate", ActorRef.noSender());
        system.actorSelection("user/parent/child1").tell("Hello3", ActorRef.noSender());

        system.actorSelection("user/parent/child2").tell("Hello3", ActorRef.noSender());
    }

    public Supervisor(final Yandex yandex, final Google google, final Bing bing) {
    }

}
