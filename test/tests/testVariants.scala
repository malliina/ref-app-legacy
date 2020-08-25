package tests

import com.malliina.refapp.WithComponents
import org.scalatest.TestSuite
import org.scalatestplus.play._
import play.api.BuiltInComponents

// Similar to https://github.com/gmethvin/play-2.5.x-scala-compile-di-with-tests/blob/master/test/ScalaTestWithComponents.scala

trait WithTestComponents[T <: BuiltInComponents]
  extends WithComponents[T]
  with FakeApplicationFactory {
  lazy val components: T = createComponents(TestAppLoader.createTestAppContext)

  override def fakeApplication() = components.application
}

trait OneAppPerSuite2[T <: BuiltInComponents]
  extends BaseOneAppPerSuite
  with WithTestComponents[T] {
  self: TestSuite =>
}

trait OneAppPerTest2[T <: BuiltInComponents] extends BaseOneAppPerTest with WithTestComponents[T] {
  self: TestSuite =>
}

trait OneServerPerSuite2[T <: BuiltInComponents]
  extends BaseOneServerPerSuite
  with WithTestComponents[T] {
  self: TestSuite =>
}

trait OneServerPerTest2[T <: BuiltInComponents]
  extends BaseOneServerPerTest
  with WithTestComponents[T] {
  self: TestSuite =>
}
