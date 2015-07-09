package com.itylos.core.rest.dto

/**
 * The root response. Every response from API should be wrapped within RootResponse object
 * @param meta the metadata object
 * @param response the result object
 */
case class RootResponse(meta: Metadata = Metadata(200, None, None), response: AnyRef) {

}

