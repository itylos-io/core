package com.itylos.core.domain

case class KerberosInstance(var instanceName: String,
                            var ip: String,
                            var username: String,
                            var password: String) {
}
