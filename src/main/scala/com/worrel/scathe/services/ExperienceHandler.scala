package com.worrel.scathe.services

import com.worrel.scathe.models.JSON.Schema.{ExperienceData, ExperienceDataBatch}

abstract trait ExperienceHandler {

  sealed abstract trait StoreResult
  case object InvalidData extends StoreResult
  case class StoreFailed(err: Throwable) extends StoreResult
  case class DataStored(id: String) extends  StoreResult

  def storeExperienceData(batch: ExperienceDataBatch): Map[ExperienceData,StoreResult]
}
