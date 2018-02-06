package com.worrel.scathe.database

import scalikejdbc.config._

object ConnectionPool {
  def init() {
    DBs.setupAll()
  }
}
