/*
 * Copyright (c) 2011-2017 Interfaculty Department of Geoinformatics, University of
 * Salzburg (Z_GIS) & Institute of Geological and Nuclear Sciences Limited (GNS Science)
 * in the SMART Aquifer Characterisation (SAC) programme funded by the New Zealand
 * Ministry of Business, Innovation and Employment (MBIE)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import javax.inject.{Inject, Singleton}

import models.ErrorResult
import models.users.{User, UserSession}
import play.api.http.MimeTypes
import play.api.libs.json._
import play.api.mvc.{ActionRefiner, _}
import services.{AdminService, UserService}
import utils.ClassnameLogger

import scala.concurrent.Future
import scala.util.parsing.json.JSON

/**
  * Use [[ActionBuilder]] when you want to add before/after logic in your actions.
  * Use [[ActionRefiner]] when you want to add custom information to a request under some conditions.
  * Use [[ActionTransformer]] when you want to always add custom information to a request.
  * Use [[ActionFilter]] when you want to filter requests under some condition and immediately return a result.
  * Always mix [[ActionRefiner]], [[ActionTransformer]] and [[ActionFilter]] to [[ActionBuilder]] so
  * you can use factory methods to easily construct your actions.
  * Use the andThen combinator to compose multiple ActionFunctions together.
  */

class AuthenticatedRequest[A](val userSession: UserSession, val request: Request[A])
  extends WrappedRequest[A](request)

@Singleton
class AuthenticatedPlainAction @Inject()(val userService: UserService)
  extends ActionBuilder[AuthenticatedRequest] with ClassnameLogger {

  def invokeBlock[A](request: Request[A],
                     block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {

    request.cookies.get(AuthTokenCookieKey).fold {
      Future.successful(Results.Unauthorized(Json.obj("status" -> "ERR", "message" -> "Invalid XSRF Token cookie")))
    } { xsrfTokenCookie =>
      logger.trace(s"cookie ${xsrfTokenCookie.value}")
      request.headers.get(AuthTokenHeader).orElse(request.getQueryString(AuthTokenUrlKey)).map {
        headerToken =>
          // ua needed to differentiate between different devices/sessions
          val uaIdentifier: String = request.headers.get(UserAgentHeader).getOrElse(UserAgentHeaderDefault)
          logger.trace(s"headerToken: $headerToken")
          logger.trace(s"ua: $uaIdentifier")
          userService.getUserSessionByToken(headerToken, xsrfTokenCookie.value, uaIdentifier).fold {
            Future.successful(Results.Unauthorized(Json.obj("status" -> "ERR", "message" -> "No server-side session"))
              .discardingCookies(DiscardingCookie(name = AuthTokenCookieKey)))
          } {
            userSession =>
              block(new AuthenticatedRequest(userSession, request))
          }

      }.getOrElse(Future.successful(Results.Unauthorized(Json.obj("status" -> "ERR", "message" -> "No token header found"))
        .discardingCookies(DiscardingCookie(name = AuthTokenCookieKey))))
    }
  }
}

@Singleton
class AuthenticationAction @Inject()(val userService: UserService) extends ActionBuilder[AuthenticatedRequest]
  with ActionRefiner[Request, AuthenticatedRequest] with ClassnameLogger {

  def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {

    val result = request.cookies.get(AuthTokenCookieKey).fold[Either[Result, AuthenticatedRequest[A]]] {
      Left(Results.Unauthorized(Json.obj("status" -> "ERR", "message" -> "Invalid XSRF Token cookie")))
    } { xsrfTokenCookie =>
      logger.trace(s"cookie ${xsrfTokenCookie.value}")
      request.headers.get(AuthTokenHeader).orElse(request.getQueryString(AuthTokenUrlKey)).map {
        headerToken =>
          // ua needed to differentiate between different devices/sessions
          val uaIdentifier: String = request.headers.get(UserAgentHeader).getOrElse(UserAgentHeaderDefault)
          logger.trace(s"headerToken: $headerToken")
          logger.trace(s"ua: $uaIdentifier")
          userService.getUserSessionByToken(headerToken, xsrfTokenCookie.value, uaIdentifier).fold[Either[Result, AuthenticatedRequest[A]]] {
            Left(Results.Unauthorized(Json.obj("status" -> "ERR", "message" -> "No server-side session"))
              .discardingCookies(DiscardingCookie(name = AuthTokenCookieKey)))
          } {
            userSession =>
              Right(new AuthenticatedRequest[A](userSession, request))
          }

      }.getOrElse(Left(Results.Unauthorized(Json.obj("status" -> "ERR", "message" -> "No token header found"))
        .discardingCookies(DiscardingCookie(name = AuthTokenCookieKey))))
    }
    Future.successful(result)
  }
}

class UserRequest[A](val user: User, val authenticatedRequest: AuthenticatedRequest[A])
  extends WrappedRequest[A](authenticatedRequest)

@Singleton
class AdminPermissionCheckAction @Inject()(val adminService: AdminService) extends ActionFilter[UserRequest] with ClassnameLogger {
  def filter[A](input: UserRequest[A]) = Future.successful {
    if (!adminService.isAdmin(input.user.email)) {
      logger.error("User email not Admin.")
      val error = ErrorResult("User email not Admin.", None)
      Some(Results.Forbidden(Json.toJson(error)))
    } else {
      None
    }
  }
}
