package controllers

import models.Job
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.bson.types.ObjectId
import models.JobEntity
import java.util.Date

@RunWith(classOf[JUnitRunner])
class JobControllerTest extends Specification {

  "postAJob" in {
    val result = controllers.JobController.postAJob(FakeRequest())
    status(result) must equalTo(OK)
    contentType(result) must beSome("text/html")
  }

  "newJob" in {
    val result = controllers.JobController.newJob(FakeRequest())
    status(result) must equalTo(400)
    contentType(result) must beSome("text/html")
  }

  "findAJob" in {
    val result = controllers.JobController.findAJob("Scala", "false")(FakeRequest())
    status(result) must equalTo(OK)
    contentType(result) must beSome("text/html")
  }

  "findJobDetail" in {
    val job1 = JobEntity(new ObjectId, new ObjectId, "Consultant", "HCL", " Noida", "Contract", "narender@gmail.com", List(), "Description", new Date)
    Job.addJob(job1)
    val result = controllers.JobController.findJobDetail((job1.id).toString)(FakeRequest())
    status(result) must equalTo(OK)
    contentType(result) must beSome("text/html")
  }

}