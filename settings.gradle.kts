plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "deep_thinker"
include("agent")
include("model")
include("cartpole_dqn")
include("cartpole_environment")
include("zeromq_client")
include("zeromq_server")
include("zeromq_client")
include("deep_learning")
