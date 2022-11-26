package tf.bug.flowbt

import java.nio.file.Path

trait Cache[F[_]] {

  def buildSheetChanged: F[Boolean]
  def buildSheetCache: F[Unit]

  def rowChanged(sheetName: String, rowId: String): F[Boolean]
  def rowCache(sheetName: String, rowId: String): F[Unit]

  def assetChanged(relPath: Path): F[Boolean]
  def assetCache(relPath: Path): F[Unit]

}
