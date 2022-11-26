package tf.bug.flowbt.cli

import cats._
import cats.effect.Clock
import cats.syntax.all._
import fs2._
import java.nio.ByteBuffer
import java.nio.file.{Path, Paths}
import scribe.Scribe
import tf.bug.flowbt

case class FileCache[F[_]](sheetPath: Path, cacheDir: Path, files: fs2.io.file.Files[F], scribe: Scribe[F])(implicit monad: Monad[F], clock: Clock[F], compiler: Compiler[F, F]) extends flowbt.Cache[F] {

  private def checkCache(filePath: Path, cachePath: Path): F[Boolean] = {
    ???
  }

  private def updateCache(path: Path): F[Unit] = {
    clock.realTime.flatMap { now =>
      val nowBytes = ByteBuffer.allocate(java.lang.Long.BYTES)
      nowBytes.asLongBuffer().put(now.toNanos)
      Stream.chunk(Chunk.byteBuffer(nowBytes))
        .through(files.writeAll(fs2.io.file.Path.fromNioPath(path)))
        .compile.drain
    }
  }

  override def buildSheetChanged: F[Boolean] =
    checkCache(sheetPath, cacheDir.resolve(Paths.get("build-sheet")))

  override def buildSheetCache: F[Unit] =
    updateCache(cacheDir.resolve(Paths.get("build-sheet")))

  override def rowChanged(sheetName: String, rowId: String): F[Boolean] =
    ???

  override def rowCache(sheetName: String, rowId: String): F[Unit] =
    updateCache(cacheDir.resolve(Paths.get("data-sheet", sheetName, rowId)))

  override def assetChanged(relPath: Path): F[Boolean] =
    checkCache(sheetPath.getParent.resolve(relPath), cacheDir.resolve(Paths.get("assets")).resolve(relPath))

  override def assetCache(relPath: Path): F[Unit] =
    updateCache(cacheDir.resolve(Paths.get("assets")).resolve(relPath))

}

object FileCache {

  def apply[F[_]](sheetPath: Path, cacheDir: Path)(implicit monad: Monad[F], clock: Clock[F], compiler: Compiler[F, F], files: fs2.io.file.Files[F], scribe: Scribe[F]): FileCache[F] =
    new FileCache[F](sheetPath, cacheDir, files, scribe)

}
