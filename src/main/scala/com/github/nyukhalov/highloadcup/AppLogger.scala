package com.github.nyukhalov.highloadcup

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

trait AppLogger {
  def logger: Logger = Logger(LoggerFactory.getLogger("highloadcup"))
}
