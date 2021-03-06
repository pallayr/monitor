/*
 * Copyright (c) 2013 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eigengo.monitor.agent.akka

import com.typesafe.config.Config
import org.eigengo.monitor.agent.akka.ActorFilter._
import org.eigengo.monitor.agent.akka.ActorFilter.NamedActorSystem
import org.eigengo.monitor.agent.akka.ActorFilter.SameType

/**
 * Configures the Akka agent by specifying the ``included`` and ``excluded`` actor filters.
 *
 * @param includeRoutees include the routed instances in the monitoring?
 * @param includeSystemAgents include the system agents in the monitoring?
 * @param incuded the filter that matches the included actors
 * @param excluded the filter that matches the excluded actors
 */
case class AkkaAgentConfiguration(includeRoutees: Boolean, includeSystemAgents: Boolean, incuded: ActorFilter, excluded: ActorFilter)

/**
 * Companion for AkkaAgentConfiguration that provides a method to turn a ``Config`` into
 * ``AkkaAgentConfiguration``.
 */
object AkkaAgentConfiguration {
  private val ActorPathPattern = "akka://([^/]*)/(.*)".r
  private val ActorTypePattern = "akka:([^.]*)\\.(.*)".r

  /**
   * Parses the given ``config`` into a valid ``AkkaAgentConfiguration``
   *
   * @param config the agent-specific configuration
   * @return the AkkaAgentConfiguration
   */
  def apply(config: Config): AkkaAgentConfiguration = {
    import scala.collection.JavaConversions._

    val includeRoutees = if (config.hasPath("includeRoutees")) config.getBoolean("includeRoutees") else false
    val included = if (config.hasPath("included")) config.getStringList("included").map(parseFilter).toList else Nil
    val excluded = if (config.hasPath("excluded")) config.getStringList("included").map(parseFilter).toList else Nil
    AkkaAgentConfiguration(includeRoutees, false, AnyAcceptActorFilter(included, true), AnyAcceptActorFilter(excluded, false))
  }

  private def parseActorSystemFilter(actorSystemName: String): ActorSystemNameFilter =
    if (actorSystemName == "*") AnyActorSystem else NamedActorSystem(actorSystemName)

  private def parseActorPathElements(path: String): List[ActorPathElement] =
    path.split("/").map {
      case "*" => SingleWildcardPathElement
      case x   => NamedPathElement(x)
    }.toList

  private def parseFilter(expression: String): ActorFilter = expression match {
    case ActorPathPattern(name, path)  => ActorPathFilter(parseActorSystemFilter(name), parseActorPathElements(path))
    case ActorTypePattern(name, clazz) => ActorTypeFilter(parseActorSystemFilter(name), SameType(clazz))
  }

}

/**
 * Exposes the ``AkkaAgentConfiguration``'s Java-friendly API
 */
object AkkaAgentConfigurationJapi {

  /**
   * Lifted ``AkkaAgentConfiguration.apply``
   *
   * @return the lifted function
   */
  def apply: Config => AkkaAgentConfiguration = AkkaAgentConfiguration.apply

}

//TODO: complete me
//case class SamplingRate(included: ActorFilter, sampleEvery: Int)