package com.itulos.controller.rest

import com.itulos.controller.exception.ParameterMissingException
import org.json4s.JsonAST.JObject

/**
 * Generic parameter validator for the payload of the requests
 */
trait ParameterValidator {

  def getParameter(data: JObject, parameter: String, isRequired: Boolean = true, isList: Boolean = false): Option[String] = {
    if ((!data.values.contains(parameter)
      || data.values(parameter).toString.trim == "")
      && isRequired) throw new ParameterMissingException(parameter)

    if ((!data.values.contains(parameter)
      || data.values(parameter).toString.trim == "")
      && !isRequired) return None
    Some(data.values(parameter).toString)
  }


  def getList(data: JObject, parameter: String, isRequired: Boolean = true): Option[List[Any]] = {
    if ((!data.values.contains(parameter)
      || data.values(parameter).toString.trim == "")
      && isRequired) throw new ParameterMissingException(parameter)

    if ((!data.values.contains(parameter)
      || data.values(parameter).toString.trim == "")
      && !isRequired) return None
    Some(data.values(parameter).asInstanceOf[List[AnyRef]])
  }

}
