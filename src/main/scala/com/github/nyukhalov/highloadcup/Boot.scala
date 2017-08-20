package com.github.nyukhalov.highloadcup

import com.github.nyukhalov.highloadcup.core.AppLogger
import com.github.nyukhalov.highloadcup.web.WebBoot

object Boot extends App with AppLogger {
  WebBoot.run
}
