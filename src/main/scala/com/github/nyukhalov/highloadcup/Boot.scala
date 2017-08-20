package com.github.nyukhalov.highloadcup

import com.github.nyukhalov.highloadcup.core.AppLogger
import com.github.nyukhalov.highloadcup.web.WebBoot

object Boot extends App with AppLogger {

  Runtime.getRuntime.traceInstructions(false)
  Runtime.getRuntime.traceMethodCalls(false)

  WebBoot.run
}
