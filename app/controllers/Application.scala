package controllers

import play.api.mvc.Controller
import play.api._
import play.api.mvc._
import play.api.data.Forms
import play.api.data._
import models.SignUp
import models.SignUpForm
import play.api.mvc.Controller
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.mvc.Http.Request
import play.libs._
import models.User
import org.bson.types.ObjectId
import models.LogInForm
import models.LogIn
import utils.PasswordHashing
import models.PostAJob

object Application extends Controller {

  val signUpForm = Form(
    mapping(
      "EmailId" -> nonEmptyText,
      "Password" -> nonEmptyText,
      "Confirm Password" -> nonEmptyText)(SignUpForm.apply)(SignUpForm.unapply))

  /**
   * Login Form Mapping
   */

  val logInForm = Form(
    mapping(
      "EmailId" -> nonEmptyText,
      "Password" -> nonEmptyText)(LogInForm.apply)(LogInForm.unapply))

  def index = Action { implicit request =>
    Ok(views.html.index("Hi Welcome To scalajobz.com", request.session.get("userId").getOrElse(null), PostAJob.findAllJobs))
  }

  /**
   * Signup on scalajobz.com
   */

  def signUpOnScalaJobz = Action { implicit request =>
    Ok(views.html.signup(signUpForm, request.session.get("userId").getOrElse(null)))
  }

  /**
   * Create A New User
   */
  def newUser = Action { implicit request =>
    signUpForm.bindFromRequest.fold(
      errors => BadRequest(views.html.index("Hi Welcome To scalajobz.com", request.session.get("userId").getOrElse(null), PostAJob.findAllJobs)),
      signUpForm => {

        if (!SignUp.findUserByEmail(signUpForm.emailId).isEmpty) Ok("This Email Is Already registered With ScalaJobz")
        else if (!signUpForm.password.equals(signUpForm.confirmPassword)) Ok("Passwords Do Not match. Please try again")
        else {
          val encryptedPassword = (new PasswordHashing).encryptThePassword(signUpForm.password)
          val newUser = User(new ObjectId, signUpForm.emailId, encryptedPassword)
          val userId = SignUp.createUser(newUser)
          val userSession = request.session + ("userId" -> userId.get.toString)
          Ok(views.html.jobs(PostAJob.findAllJobs, userId.get.toString)).withSession(userSession)
        }
      })
  }

  /**
   * Login On ScalaJobz
   */

  def logIn = Action { implicit request =>
    logInForm.bindFromRequest.fold(
      errors => BadRequest(views.html.index("There Was Some Errors During The Login", request.session.get("userId").getOrElse(null), PostAJob.findAllJobs)),
      logInForm => {
        val encryptedPassword = (new PasswordHashing).encryptThePassword(logInForm.password)
        val users = LogIn.findUser(logInForm.emailId, encryptedPassword)

        if (!users.isEmpty) {
          val userSession = request.session + ("userId" -> users(0).id.toString)
          Ok(views.html.jobs(PostAJob.findAllJobs, users(0).id.toString)).withSession(userSession)
        } else Ok("Login Unsuccessfull")
      })
  }
  /**
   * Login on scalajobz.com
   */

  def loginOnScalaJobz = Action { implicit request =>
    Ok(views.html.login(logInForm, request.session.get("userId").getOrElse(null)))
  }

  /**
   * Log Out
   */

  def logOutFromScalaJobz = Action {
    Ok(views.html.jobs(PostAJob.findAllJobs, null)).withNewSession
  }

}