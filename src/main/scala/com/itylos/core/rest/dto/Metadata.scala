package com.itylos.core.rest.dto

/**
 * Metadata that should be include in each response. In case of error or request processing
 * errorMessage and moreInfo should be edited
 * @param code the status code or error code
 * @param errorMessage the error message if any
 * @param moreInfo the more info url if any
 */
case class Metadata(code: Integer, errorMessage: Option[String], moreInfo: Option[String]) {
}
