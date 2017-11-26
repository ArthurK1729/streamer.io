import actors.{DirectorActor, SparkActor, WebSocketActor}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.

 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure() = {
    // Bind static actors
    bindActor[DirectorActor]("director")

    // Bind actor factories
    bindActorFactory[SparkActor, SparkActor.Factory]
    bindActorFactory[WebSocketActor, WebSocketActor.Factory]
  }

}
