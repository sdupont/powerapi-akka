/**
 * Copyright (C) 2012 Inria, University Lille 1
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA.
 *
 * Contact: powerapi-user-list@googlegroups.com
 */
package fr.inria.powerapi.example.adamdemo.oneprocess

import fr.inria.powerapi.formula.cpu.api.CpuFormulaValues
import fr.inria.powerapi.sensor.cpu.proc.CpuSensor
import fr.inria.powerapi.core.Process
import scalax.io.Resource
import fr.inria.powerapi.library.PowerAPI
import fr.inria.powerapi.formula.cpu.general.CpuFormula
import akka.util.duration._
import java.lang.management.ManagementFactory
import scalax.file.Path
import scalax.io.StandardOpenOption.WriteTruncate

class CpuListener extends fr.inria.powerapi.listener.cpu.console.CpuListener {
  override def process(cpuFormulaValues: CpuFormulaValues) {
    println(cpuFormulaValues.energy.power)
  }
}

object Demo extends App {
  Path.fromString("/tmp/powerapi.demo-oneprocess.pid").outputStream(WriteTruncate: _*).write(ManagementFactory.getRuntimeMXBean.getName.split("@")(0))

  Array(
    classOf[CpuSensor],
    classOf[CpuFormula]).foreach(PowerAPI.startEnergyModule(_))

  val PSFormat = """^\s*(\d+).*""".r
  val pids = Resource.fromInputStream(Runtime.getRuntime.exec(Array("ps", "-C", "firefox", "ho", "pid")).getInputStream).lines().toList.map({
    pid =>
      pid match {
        case PSFormat(id) => id.toInt
        case _ => 1
      }
  })

  pids.foreach(pid => PowerAPI.startMonitoring(Process(pid), 1 second, classOf[CpuListener]))
  Thread.sleep((2 hours).toMillis)
  pids.foreach(pid => PowerAPI.stopMonitoring(Process(pid), 1 second, classOf[CpuListener]))

  Array(
    classOf[CpuSensor],
    classOf[CpuFormula]).foreach(PowerAPI.stopEnergyModule(_))

}