package com.itulos.controller.service.protocol

import com.itulos.controller.domain.User
import com.itulos.controller.rest.dto.AuthToken


/**
 * Describes the messages needed for communication to manage tokens
 */
sealed trait TokenProtocol extends Protocol

/**
 * Message to update a token's expire time
 * @param user the user to update token for
 * @param existingToken the existing token in use
 */
case class UpdateTokenExpireTime(user: User, existingToken: String) extends TokenProtocol

/**
 * Message to generate token
 * @param user the user to generate token for
 */
case class GenerateTokenRq(user: User) extends TokenProtocol

/**
 * Response message for GenerateTokenRq
 * @param tokenData the AuthToken
 */
case class GenerateTokenRs(tokenData: AuthToken) extends TokenProtocol

/**
 * Message to deactivate/delete token
 * @param user the user the token is associated to 
 * @param token the token to delete
 */
case class DeactivateToken(user: User, token: String) extends TokenProtocol

/**
 * Response message for DeactivateToken
 */
case class DeactivateTokenRs() extends TokenProtocol
