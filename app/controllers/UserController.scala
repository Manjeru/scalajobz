package controllers

import play.api.mvc.Controller
import play.api.data.Form
import models.User
import models.SignUpForm
import play.api.data.Forms._
import play.mvc.Http.Request
import org.bson.types.ObjectId
import models.LogInForm
import utils.PasswordHashing
import models.Job
import models.Alert
import models.UserEntity
import models.EditUserProfileForm
import utils.SendEmail
import models.Common
import play.api.mvc.Action

object UserController extends Controller {
  val activeUserId = "userId"
  val errorString="error"

  val editUserProfileForm = Form(
    mapping(
      "CurrentPassword" -> nonEmptyText,
      "NewPassword" -> nonEmptyText,
      "ConfirmPassword" -> nonEmptyText)(EditUserProfileForm.apply)(EditUserProfileForm.unapply))

  /**
   * Redirect To User Profile edit page
   */

  def editUserProfile: Action[play.api.mvc.AnyContent] = Action { implicit request =>
    val userProfile = User.findUserById(request.session.get(activeUserId).get)
    Ok(views.html.editUserProfile(new Alert("", ""), userProfile.get, editUserProfileForm, request.session.get(activeUserId).getOrElse("")))
  }

  /**
   * Find list of job post by a user
   */

  def findJobPostByUserId: Action[play.api.mvc.AnyContent] = Action { implicit request =>
    val alert = Common.alert
    Common.setAlert(new Alert("", ""))
    val jobPostByUserList = Job.findJobsPostByUserId(new ObjectId(request.session.get(activeUserId).get))
    Ok(views.html.index(alert, request.session.get(activeUserId).getOrElse(""), jobPostByUserList, true))
  }

  /**
   * Update User Profile
   */
  def updateUserProfile: Action[play.api.mvc.AnyContent] = Action { implicit request =>
    val userProfile = User.findUserById(request.session.get(activeUserId).get).get
    editUserProfileForm.bindFromRequest.fold(
      errors => BadRequest(views.html.editUserProfile(new Alert(errorString, "There Was Some Errors During Profile Editing"),
        userProfile, editUserProfileForm, request.session.get(activeUserId).getOrElse(""))),
      editUserProfileForm => {
        val currentEncryptedPassword = (new PasswordHashing).encryptThePassword(editUserProfileForm.currentPassword)
        val encryptedPassword = (new PasswordHashing).encryptThePassword(editUserProfileForm.newPassword)
        if ((currentEncryptedPassword.equals(userProfile.password)) && (!currentEncryptedPassword.equals(encryptedPassword))) {
          User.updateUser(userProfile, encryptedPassword)
          Ok(views.html.editUserProfile(new Alert("success", "Profile Updated"),
            userProfile, UserController.editUserProfileForm, request.session.get(activeUserId).getOrElse("")))
        } else if(currentEncryptedPassword.equals(encryptedPassword)){
          Ok(views.html.editUserProfile(new Alert(errorString, "Current Password & New Password are same"),
            userProfile, UserController.editUserProfileForm, request.session.get(activeUserId).getOrElse("")))
        } else{
          Ok(views.html.editUserProfile(new Alert(errorString, "Invalid Current Password"),
            userProfile, UserController.editUserProfileForm, request.session.get(activeUserId).getOrElse("")))
      }})
  }

  /**
   * Redirect To Forget Password Page
   */
  def forgetPassword: Action[play.api.mvc.AnyContent] = Action { implicit request =>
    Ok(views.html.forgetPassword(new Alert("", "")))
  }

  /**
   * Send Password To User Email Id
   */

  def sendForgetPassword(emailId: String): Action[play.api.mvc.AnyContent] = Action { implicit request =>
    if (User.findUserByEmail(emailId).isEmpty) {
      Ok(false.toString)
    } else {
      val userList = User.findUserByEmail(emailId)
      val user = Option(userList.toList(0)).get
      SendEmail.sendPassword(user.emailId, (new PasswordHashing).decryptThePassword(user.password))
      Ok(true.toString)
    }
  }

  /**
   * Register Job seeker for getting Job alert
   */
  def registerJobSeeker(emailId: String, skillsToken: String): Action[play.api.mvc.AnyContent] = Action { implicit request =>
    val newJobSeeker = UserEntity(new ObjectId, emailId, "", skillsToken.split(" ").toList.filter(x => !(x == "")), true)
    val userId = User.createUser(newJobSeeker)
    Ok
  }

  /**
   * UnSubscribe From Job Alerts By Using JobSeeker Id(UserId)
   */

  def unSubscribeJobSeeker(userId: String): Action[play.api.mvc.AnyContent] = Action { implicit request =>
    User.unSubscribeJobSeeker(userId) match {
      case true => Ok(views.html.index(new Alert("success", "Unsubscribed From ScalaJobz"),
          request.session.get(activeUserId).getOrElse(""), Job.findAllJobs, false))
      case false => Ok(views.html.errorPage("There Is Some Error :User Not Subscribed With ScalaJobz"))
    }
  }
}
